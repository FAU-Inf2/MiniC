package i2.act.examples.minic.frontend.semantics.symbols;

import i2.act.examples.minic.frontend.ast.Declaration;
import i2.act.examples.minic.frontend.semantics.types.Type;

public final class Symbol {

  private final String name;
  private final Type type;
  private final boolean global;
  private final Declaration declaration;

  public Symbol(final String name, final Type type, final boolean global,
      final Declaration declaration) {
    this.name = name;
    this.type = type;
    this.global = global;
    this.declaration = declaration;
  }

  public final String getName() {
    return this.name;
  }

  public final Type getType() {
    return this.type;
  }

  public final boolean isGlobal() {
    return this.global;
  }

  public final Declaration getDeclaration() {
    return this.declaration;
  }

  @Override
  public final String toString() {
    return String.format("<%s:%s>", this.name, this.type);
  }

}
