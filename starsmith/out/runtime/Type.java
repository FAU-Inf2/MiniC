package runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Type {

  private static class PrimitiveType extends Type {

    public static final PrimitiveType ANY_PRIMITIVE_TYPE = new PrimitiveType();

    @Override
    public final boolean assignableTo(final Type otherType) {
      return (this == otherType) || (otherType == ANY_PRIMITIVE_TYPE);
    }

  }

  private static final class IntegerType extends PrimitiveType {

    public static final IntegerType INSTANCE = new IntegerType();

    private IntegerType() {
      // intentionally left blank
    }

  }

  private static final class BoolType extends PrimitiveType {

    public static final BoolType INSTANCE = new BoolType();

    private BoolType() {
      // intentionally left blank
    }

  }

  private static final class VoidType extends PrimitiveType {

    public static final VoidType INSTANCE = new VoidType();

    private VoidType() {
      // intentionally left blank
    }

  }

  private static final class FunctionType extends Type {

    public static final FunctionType ANY_FUNCTION = new FunctionType(null, null);

    public final Type returnType;
    public final List<Type> parameterTypes;

    public FunctionType(final Type returnType) {
      this(returnType, new ArrayList<Type>());
    }

    public FunctionType(final Type returnType, final List<Type> parameterTypes) {
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
    }

    @Override
    public final boolean assignableTo(final Type otherType) {
      if (!(otherType instanceof FunctionType)) {
        return false;
      }

      if (otherType == ANY_FUNCTION) {
        return true;
      }

      final FunctionType otherFunctionType = (FunctionType) otherType;

      // any function with a defined return type
      return (otherFunctionType.parameterTypes == null)
          && (assignable(this.returnType, otherFunctionType.returnType));
    }

    @Override
    public final FunctionType clone() {
      final FunctionType clone = new FunctionType(this.returnType);

      for (final Type parameterType : this.parameterTypes) {
        clone.parameterTypes.add(parameterType);
      }

      return clone;
    }

  }

  public static final Type intType() {
    return IntegerType.INSTANCE;
  }

  public static final Type boolType() {
    return BoolType.INSTANCE;
  }

  public static final Type voidType() {
    return VoidType.INSTANCE;
  }

  public static final boolean isVoid(final Type type) {
    return type == VoidType.INSTANCE;
  }

  public static final Type anyPrimitiveType() {
    return PrimitiveType.ANY_PRIMITIVE_TYPE;
  }

  public static final Type functionType(final Type returnType) {
    return new FunctionType(returnType);
  }

  public static final Type functionType(final Type returnType, final Type... parameterTypes) {
    return new FunctionType(returnType, Arrays.asList(parameterTypes));
  }

  public static final Type anyFunction() {
    return FunctionType.ANY_FUNCTION;
  }

  public static final Type anyFunction(final Type returnType) {
    return new FunctionType(returnType, null);
  }

  public static final Type returnType(final Type functionType) {
    return ((FunctionType) functionType).returnType;
  }

  public static final int numberOfParameters(final Type functionType) {
    return ((FunctionType) functionType).parameterTypes.size();
  }

  public static final Type parameterType(final Type functionType, final int index) {
    return ((FunctionType) functionType).parameterTypes.get(index);
  }

  public static final FunctionType addParameterType(final Type functionType,
      final Type parameterType) {
    final FunctionType newFunctionType = ((FunctionType) functionType).clone();
    newFunctionType.parameterTypes.add(parameterType);

    return newFunctionType;
  }

  protected boolean assignableTo(final Type otherType) {
    return false;
  }

  public static final boolean assignable(final Type typeOne, final Type typeTwo) {
    return typeOne.assignableTo(typeTwo);
  }

}
