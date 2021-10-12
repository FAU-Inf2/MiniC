package i2.act.examples.minic.frontend.semantics.types;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class FunctionType extends Type {

  private final Type returnType;
  private final List<Type> parameterTypes;

  public FunctionType(final Type returnType, final List<Type> parameterTypes) {
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
  }

  public final Type getReturnType() {
    return this.returnType;
  }

  public final int getNumberOfParameters() {
    return this.parameterTypes.size();
  }

  public final List<Type> getParameterTypes() {
    return Collections.unmodifiableList(this.parameterTypes);
  }

  @Override
  public final boolean isVariableType() {
    return false;
  }

  @Override
  public final boolean assignableTo(final Type otherType) {
    return false;
  }

  @Override
  public final String toString() {
    final StringBuilder builder = new StringBuilder();

    builder
        .append("(")
        .append(this.parameterTypes.stream().map(Type::toString).collect(Collectors.joining(",")))
        .append(") -> ")
        .append(this.returnType.toString());

    return builder.toString();
  }

}
