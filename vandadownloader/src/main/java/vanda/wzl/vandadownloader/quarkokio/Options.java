/*
 * Copyright (C) 2005-2017 UCWeb Inc. All rights reserved.
 *  Description :Options.java
 *
 *  Creation    : 2017-06-03
 *  Author      : zhonglian.wzl@alibaba-inc.com
 */
package vanda.wzl.vandadownloader.quarkokio;

import java.util.AbstractList;
import java.util.RandomAccess;

/** An indexed set of values that may be read with {@link QuarkBufferedSource#select}. */
public final class Options extends AbstractList<ByteString> implements RandomAccess {
  final ByteString[] byteStrings;

  private Options(ByteString[] byteStrings) {
    this.byteStrings = byteStrings;
  }

  public static Options of(ByteString... byteStrings) {
    return new Options(byteStrings.clone()); // Defensive copy.
  }

  @Override public ByteString get(int i) {
    return byteStrings[i];
  }

  @Override public int size() {
    return byteStrings.length;
  }
}
