module com.github.sormuras.stash.compiler {
  exports com.github.sormuras.stash.compiler;
  exports com.github.sormuras.stash.compiler.stashlet;

  requires transitive com.github.sormuras.beethoven;
  requires transitive com.github.sormuras.stash;
  requires java.compiler;
  requires java.logging;

  uses com.github.sormuras.stash.compiler.Stashlet;

  provides com.github.sormuras.stash.compiler.Stashlet with
      com.github.sormuras.stash.compiler.stashlet.UUIDStashlet;
  provides javax.annotation.processing.Processor with
      com.github.sormuras.stash.compiler.Processor;
}
