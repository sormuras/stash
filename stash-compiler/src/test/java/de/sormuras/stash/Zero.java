package de.sormuras.stash;

import java.nio.ByteBuffer;

public class Zero implements Stashable {

  public Zero() {}

  public Zero(ByteBuffer source) {
    assert source != null;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public ByteBuffer stash(ByteBuffer target) {
    return target;
  }
}
