/*
 * Copyright (C) 2005-2017 UCWeb Inc. All rights reserved.
 *  Description :Okio.java
 *
 *  Creation    : 2017-06-03
 *  Author      : zhonglian.wzl@alibaba-inc.com
 */
package vanda.wzl.vandadownloader.quark_okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static vanda.wzl.vandadownloader.quark_okio.Util.checkOffsetAndCount;


/** Essential APIs for working with  */
public final class QuarkOkio {
  static final Logger logger = Logger.getLogger("Okio");

  private QuarkOkio() {
  }

  /**
   * Returns a new source that buffers reads from {@code source}. The returned
   * source will perform bulk reads into its in-memory buffer. Use this wherever
   * you read a source to get an ergonomic and efficient access to data.
   */
  public static QuarkBufferedSource buffer(QuarkSource source) {
    return new RealBufferedSource(source);
  }

  /**
   * Returns a new sink that buffers writes to {@code sink}. The returned sink
   * will batch writes to {@code sink}. Use this wherever you write to a sink to
   * get an ergonomic and efficient access to data.
   */
  public static QuarkBufferedSink buffer(QuarkSink sink) {
    return new RealBufferedSink(sink);
  }

  /** Returns a sink that writes to {@code out}. */
  public static QuarkSink sink(OutputStream out) {
    return sink(out, new Timeout());
  }

  private static QuarkSink sink(final OutputStream out, final Timeout timeout) {
    if (out == null) throw new IllegalArgumentException("out == null");
    if (timeout == null) throw new IllegalArgumentException("timeout == null");

    return new QuarkSink() {
      @Override public void write(QuarkBuffer source, long byteCount) throws IOException {
        checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
          timeout.throwIfReached();
          Segment head = source.head;
          int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
          out.write(head.data, head.pos, toCopy);

          head.pos += toCopy;
          byteCount -= toCopy;
          source.size -= toCopy;

          if (head.pos == head.limit) {
            source.head = head.pop();
            SegmentPool.recycle(head);
          }
        }
      }

      @Override public void flush() throws IOException {
        out.flush();
      }

      @Override public void close() throws IOException {
        out.close();
      }

      @Override public Timeout timeout() {
        return timeout;
      }

      @Override public String toString() {
        return "sink(" + out + ")";
      }
    };
  }

  /**
   * Returns a sink that writes to {@code socket}. Prefer this over {@link
   * #sink(OutputStream)} because this method honors timeouts. When the socket
   * write times out, the socket is asynchronously closed by a watchdog thread.
   */
  public static QuarkSink sink(Socket socket) throws IOException {
    if (socket == null) throw new IllegalArgumentException("socket == null");
    AsyncTimeout timeout = timeout(socket);
    QuarkSink sink = sink(socket.getOutputStream(), timeout);
    return timeout.sink(sink);
  }

  /** Returns a source that reads from {@code in}. */
  public static QuarkSource source(InputStream in) {
    return source(in, new Timeout());
  }

  private static QuarkSource source(final InputStream in, final Timeout timeout) {
    if (in == null) throw new IllegalArgumentException("in == null");
    if (timeout == null) throw new IllegalArgumentException("timeout == null");

    return new QuarkSource() {
      @Override public long read(QuarkBuffer sink, long byteCount) throws IOException {
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (byteCount == 0) return 0;
        try {
          timeout.throwIfReached();
          Segment tail = sink.writableSegment(1);
          int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
          int bytesRead = in.read(tail.data, tail.limit, maxToCopy);
          if (bytesRead == -1) return -1;
          tail.limit += bytesRead;
          sink.size += bytesRead;
          return bytesRead;
        } catch (AssertionError e) {
          if (isAndroidGetsocknameError(e)) throw new IOException(e);
          throw e;
        }
      }

      @Override public void close() throws IOException {
        in.close();
      }

      @Override public Timeout timeout() {
        return timeout;
      }

      @Override public String toString() {
        return "source(" + in + ")";
      }
    };
  }

  /** Returns a source that reads from {@code file}. */
  public static QuarkSource source(File file) throws FileNotFoundException {
    if (file == null) throw new IllegalArgumentException("file == null");
    return source(new FileInputStream(file));
  }


  /** Returns a sink that writes to {@code file}. */
  public static QuarkSink sink(File file) throws FileNotFoundException {
    if (file == null) throw new IllegalArgumentException("file == null");
    return sink(new FileOutputStream(file));
  }

  /** Returns a sink that appends to {@code file}. */
  public static QuarkSink appendingSink(File file) throws FileNotFoundException {
    if (file == null) throw new IllegalArgumentException("file == null");
    return sink(new FileOutputStream(file, true));
  }


  /** Returns a sink that writes nowhere. */
  public static QuarkSink blackhole() {
    return new QuarkSink() {
      @Override public void write(QuarkBuffer source, long byteCount) throws IOException {
        source.skip(byteCount);
      }

      @Override public void flush() throws IOException {
      }

      @Override public Timeout timeout() {
        return Timeout.NONE;
      }

      @Override public void close() throws IOException {
      }
    };
  }

  /**
   * Returns a source that reads from {@code socket}. Prefer this over {@link
   * #source(InputStream)} because this method honors timeouts. When the socket
   * read times out, the socket is asynchronously closed by a watchdog thread.
   */
  public static QuarkSource source(Socket socket) throws IOException {
    if (socket == null) throw new IllegalArgumentException("socket == null");
    AsyncTimeout timeout = timeout(socket);
    QuarkSource source = source(socket.getInputStream(), timeout);
    return timeout.source(source);
  }

  private static AsyncTimeout timeout(final Socket socket) {
    return new AsyncTimeout() {
      @Override protected IOException newTimeoutException( IOException cause) {
        InterruptedIOException ioe = new SocketTimeoutException("timeout");
        if (cause != null) {
          ioe.initCause(cause);
        }
        return ioe;
      }

      @Override protected void timedOut() {
        try {
          socket.close();
        } catch (Exception e) {
          logger.log(Level.WARNING, "Failed to close timed out socket " + socket, e);
        } catch (AssertionError e) {
          if (isAndroidGetsocknameError(e)) {
            // Catch this exception due to a Firmware issue up to android 4.2.2
            // https://code.google.com/p/android/issues/detail?id=54072
            logger.log(Level.WARNING, "Failed to close timed out socket " + socket, e);
          } else {
            throw e;
          }
        }
      }
    };
  }

  /**
   * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
   * https://code.google.com/p/android/issues/detail?id=54072
   */
  static boolean isAndroidGetsocknameError(AssertionError e) {
    return e.getCause() != null && e.getMessage() != null
        && e.getMessage().contains("getsockname failed");
  }
}
