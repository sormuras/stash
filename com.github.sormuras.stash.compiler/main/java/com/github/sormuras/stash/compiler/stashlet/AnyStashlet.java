package com.github.sormuras.stash.compiler.stashlet;

import static javax.lang.model.element.Modifier.STATIC;

import com.github.sormuras.beethoven.Listing;
import com.github.sormuras.beethoven.type.Type;
import com.github.sormuras.beethoven.unit.Block;
import com.github.sormuras.beethoven.unit.MethodDeclaration;
import com.github.sormuras.stash.compiler.Generator;
import com.github.sormuras.stash.compiler.Stashlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class AnyStashlet implements Stashlet {

  public static ByteBuffer stashAny(ByteBuffer target, Object object) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ObjectOutputStream stream = new ObjectOutputStream(bytes)) {
      stream.writeObject(object);
    } catch (Exception exception) {
      throw new Error(exception);
    }
    byte[] data = bytes.toByteArray();
    target.putInt(data.length);
    target.put(data);
    return target;
  }

  private static Listing buildStashAnyBody(Listing listing) {
    listing.eval("{{N:0}} bytes = new {{N:0}}(){{;}}", ByteArrayOutputStream.class);
    listing.eval("try({{N:0}} stream = new {{N:0}}(bytes)) {", ObjectOutputStream.class).newline();
    listing.eval("{{>}}stream.writeObject(object){{;}}{{<}}");
    listing.eval("} catch({{N}} exception) {", Exception.class).newline();
    listing.eval("{{>}}throw new {{N}}(exception){{;}}{{<}}", Error.class);
    listing.add("}").newline();
    listing.add("byte[] data = bytes.toByteArray();").newline();
    listing.add("target.putInt(data.length);").newline();
    listing.add("target.put(data);").newline();
    listing.add("return target;").newline();
    return listing;
  }

  public static Object spawnAny(ByteBuffer source) {
    byte[] data = new byte[source.getInt()];
    source.get(data);
    ByteArrayInputStream bytes = new ByteArrayInputStream(data);
    try (ObjectInputStream stream = new ObjectInputStream(bytes)) {
      return stream.readObject();
    } catch (Exception exception) {
      throw new Error(exception);
    }
  }

  private static Listing buildSpawnAnyBody(Listing listing) {
    listing.add("byte[] data = new byte[source.getInt()];").newline();
    listing.add("source.get(data);").newline();
    listing.eval("{{N:0}} bytes = new {{N:0}}(data){{;}}", ByteArrayInputStream.class);
    listing.eval("try({{N:0}} stream = new {{N:0}}(bytes)) {", ObjectInputStream.class).newline();
    listing.eval("{{>}}return stream.readObject(){{;}}{{<}}");
    listing.eval("} catch({{N}} exception) {", Exception.class).newline();
    listing.eval("{{>}}throw new {{N}}(exception){{;}}{{<}}", Error.class);
    listing.add("}").newline();
    return listing;
  }

  private MethodDeclaration stashAny;
  private MethodDeclaration spawnAny;
  private boolean initialized = false;

  public AnyStashlet() {}

  @Override
  public void init(Type type, Generator generator) {
    Logger.getLogger(getClass().getName()).warning("type " + type + " handled by AnyStashlet!");

    if (initialized) {
      return;
    }
    this.stashAny = generator.getIo().declareMethod(ByteBuffer.class, "stashAny", STATIC);
    stashAny.declareParameter(ByteBuffer.class, "target");
    stashAny.declareParameter(Object.class, "object");
    stashAny.setBody(new Block().add(AnyStashlet::buildStashAnyBody));

    this.spawnAny = generator.getIo().declareMethod(Object.class, "spawnAny", STATIC);
    spawnAny.declareParameter(ByteBuffer.class, "source");
    spawnAny.setBody(new Block().add(AnyStashlet::buildSpawnAnyBody));

    this.initialized = true;
  }

  @Override
  public Listing stash(Listing listing, String buffer, String parameterName) {
    listing.add(stashAny.getEnclosingDeclaration().getName()).add('.');
    return stashAny.applyCall(listing, buffer, parameterName);
  }

  @Override
  public Listing spawn(Listing listing, String buffer, Type parameterType) {
    // cast
    listing.add('(');
    listing.add(parameterType);
    listing.add(')');
    listing.add(' ');
    // call
    listing.add(spawnAny.getEnclosingDeclaration().getName()).add('.');
    spawnAny.applyCall(listing, buffer);
    return listing;
  }
}
