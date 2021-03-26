package test.compiler;

import com.github.sormuras.stash.Stashable;
import java.nio.ByteBuffer;

public class Data implements Stashable {

  private final long value;

  public Data() {
    this.value = Math.round(Math.random());
  }

  public Data(ByteBuffer source) {
    this.value = source.getLong();
  }

  @Override
  public int hashCode() {
    return (int) value;
  }

  @Override
  public ByteBuffer stash(ByteBuffer target) {
    return target.putLong(value);
  }
}
