package i2.act.examples.minic.frontend.semantics.symbols;

import i2.act.examples.minic.frontend.semantics.types.Type;

public final class Symbol {

  private final String name;
  private final Type type;

  public Symbol(final String name, final Type type) {
    this.name = name;
    this.type = type;
  }

  public final String getName() {
    return this.name;
  }

  public final Type getType() {
    return this.type;
  }

  @Override
  public final String toString() {
    return String.format("<%s:%s>", this.name, this.type);
  }

}
