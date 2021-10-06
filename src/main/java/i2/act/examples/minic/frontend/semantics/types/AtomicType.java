package i2.act.examples.minic.frontend.semantics.types;

import java.util.HashSet;
import java.util.Set;

public final class AtomicType extends Type {

  public static final AtomicType INT = new AtomicType("INT");

  static {
    // intentionally left blank
  }

  private final String name;
  private final Set<Type> assignableTo;

  private AtomicType(final String name) {
    this.name = name;

    this.assignableTo = new HashSet<Type>();
    this.assignableTo.add(this);
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
