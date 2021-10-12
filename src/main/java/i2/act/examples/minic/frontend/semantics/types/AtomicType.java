package i2.act.examples.minic.frontend.semantics.types;

import java.util.HashSet;
import java.util.Set;

public final class AtomicType extends Type {

  public static final AtomicType INT = new AtomicType("int", true);
  public static final AtomicType VOID = new AtomicType("void", false);
  public static final AtomicType BOOLEAN = new AtomicType("boolean", false);

  static {
    // intentionally left blank
  }

  private final String name;
  private final boolean isVariableType;
  private final Set<Type> assignableTo;

  private AtomicType(final String name, final boolean isVariableType) {
    this.name = name;
    this.isVariableType = isVariableType;

    this.assignableTo = new HashSet<Type>();
    this.assignableTo.add(this);
  }

  @Override
  public final boolean isVariableType() {
    return this.isVariableType;
  }

  @Override
  public final boolean assignableTo(final Type otherType) {
    return this.assignableTo.contains(otherType);
  }

  @Override
  public final String toString() {
    return this.name;
  }

}
