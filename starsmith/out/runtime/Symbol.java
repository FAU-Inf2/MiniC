package runtime;

import i2.act.fuzzer.Node;

import java.util.Objects;

public final class Symbol {

  public final String name;
  public final Type type;
  public final int id;

  private Symbol(final String name, final Type type, final int id) {
    this.name = name;
    this.type = type;
    this.id = id;
  }

  public static final Symbol create(final String name, final Type type, final Node node) {
    return create(name, type, node.id);
  }

  public static final Symbol create(final String name, final Type type, final int id) {
    return new Symbol(name, type, id);
  }

  @Override
  public final String toString() {
    return this.name;
  }

  @Override
  public final boolean equals(final Object other) {
    if (!(other instanceof Symbol)) {
      return false;
    }

    final Symbol otherSymbol = (Symbol) other;

    return Objects.equals(this.name, otherSymbol.name)
        && Objects.equals(this.type, otherSymbol.type)
        && this.id == otherSymbol.id;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.name, this.type, this.id);
  }

  public static final String nameOf(final Symbol symbol) {
    return symbol.name;
  }

  public static final Type typeOf(final Symbol symbol) {
    return symbol.type;
  }

}
