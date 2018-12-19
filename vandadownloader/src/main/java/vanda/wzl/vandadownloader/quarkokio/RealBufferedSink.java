/*
 * Copyright (C) 2005-2017 UCWeb Inc. All rights reserved.
 *  Description :RealBufferedSink.java
 *
 *  Creation    : 2017-06-03
 *  Author      : zhonglian.wzl@alibaba-inc.com
 */
package vanda.wzl.vandadownloader.quarkokio;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

final class RealBufferedSink implements QuarkBufferedSink {
    public final QuarkBuffer buffer = new QuarkBuffer();
    public final QuarkSink sink;
    boolean closed;

    RealBufferedSink(QuarkSink sink) {
        if (sink == null) throw new NullPointerException("sink == null");
        this.sink = sink;
    }

    @Override
    public QuarkBuffer buffer() {
        return buffer;
    }

    @Override
    public void write(QuarkBuffer source, long byteCount)
            throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source, byteCount);
        emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink write(ByteString byteString) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(byteString);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeUtf8(String string) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8(string);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeUtf8(String string, int beginIndex, int endIndex)
            throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8(string, beginIndex, endIndex);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeUtf8CodePoint(codePoint);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeString(String string, Charset charset) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeString(string, charset);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeString(String string, int beginIndex, int endIndex,
                                         Charset charset) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeString(string, beginIndex, endIndex, charset);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink write(byte[] source) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.write(source, offset, byteCount);
        return emitCompleteSegments();
    }

    @Override
    public long writeAll(QuarkSource source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");
        long totalBytesRead = 0;
        for (long readCount; (readCount = source.read(buffer, Segment.SIZE)) != -1; ) {
            totalBytesRead += readCount;
            emitCompleteSegments();
        }
        return totalBytesRead;
    }

    @Override
    public QuarkBufferedSink write(QuarkSource source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(buffer, byteCount);
            if (read == -1) throw new EOFException();
            byteCount -= read;
            emitCompleteSegments();
        }
        return this;
    }

    @Override
    public QuarkBufferedSink writeByte(int b) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeByte(b);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeShort(int s) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeShort(s);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeShortLe(int s) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeShortLe(s);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeInt(int i) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeInt(i);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeIntLe(int i) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeIntLe(i);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeLong(v);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeLongLe(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeLongLe(v);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeDecimalLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeDecimalLong(v);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        buffer.writeHexadecimalUnsignedLong(v);
        return emitCompleteSegments();
    }

    @Override
    public QuarkBufferedSink emitCompleteSegments() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        long byteCount = buffer.completeSegmentByteCount();
        if (byteCount > 0) sink.write(buffer, byteCount);
        return this;
    }

    @Override
    public QuarkBufferedSink emit() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        long byteCount = buffer.size();
        if (byteCount > 0) sink.write(buffer, byteCount);
        return this;
    }

    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (closed) throw new IOException("closed");
                buffer.writeByte((byte) b);
                emitCompleteSegments();
            }

            @Override
            public void write(byte[] data, int offset, int byteCount) throws IOException {
                if (closed) throw new IOException("closed");
                buffer.write(data, offset, byteCount);
                emitCompleteSegments();
            }

            @Override
            public void flush() throws IOException {
                // For backwards compatibility, a flush() on a closed stream is a no-op.
                if (!closed) {
                    RealBufferedSink.this.flush();
                }
            }

            @Override
            public void close() throws IOException {
                RealBufferedSink.this.close();
            }

            @Override
            public String toString() {
                return RealBufferedSink.this + ".outputStream()";
            }
        };
    }

    private Object mObject;
    private long mSofar;

    @Override
    public void setTag(Object object) {
        this.mObject = object;
    }

    @Override
    public Object getTag() {
        return mObject;
    }

    @Override
    public void setSofar(long sofar) {
        mSofar = sofar;
    }

    @Override
    public long getSofar() {
        return mSofar;
    }

    @Override
    public void flush() throws IOException {
        if (closed) throw new IllegalStateException("closed");
        if (buffer.size > 0) {
            sink.write(buffer, buffer.size);
        }
        sink.flush();
    }

    @Override
    public void close() throws IOException {
        if (closed) return;

        // Emit buffered data to the underlying sink. If this fails, we still need
        // to close the sink; otherwise we risk leaking resources.
        Throwable thrown = null;
        try {
            if (buffer.size > 0) {
                sink.write(buffer, buffer.size);
            }
        } catch (Throwable e) {
            thrown = e;
        }

        try {
            sink.close();
        } catch (Throwable e) {
            if (thrown == null) thrown = e;
        }
        closed = true;

        if (thrown != null) Util.sneakyRethrow(thrown);
    }

    @Override
    public Timeout timeout() {
        return sink.timeout();
    }

    @Override
    public String toString() {
        return "buffer(" + sink + ")";
    }
}
