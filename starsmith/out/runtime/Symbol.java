package runtime;

public final class Symbol {

  public final String name;
  public final Type type;

  private Symbol(final String name, final Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public final String toString() {
    return this.name;
  }

  public static final Symbol create(final String name, final Type type) {
    return new Symbol(name, type);
  }

  public static final String nameOf(final Symbol symbol) {
    return symbol.name;
  }

  public static final Type typeOf(final Symbol symbol) {
    return symbol.type;
  }

}
