package i2.act.examples.minic.frontend.semantics.types;

public abstract class Type {

  public abstract boolean isVariableType();

  public abstract boolean assignableTo(final Type otherType);

  public abstract String toString();

}
