package runtime;

public abstract class Type {

  private static final class IntegerType extends Type {

    public static final IntegerType INSTANCE = new IntegerType();

    private IntegerType() {
      // intentionally left blank
    }

    @Override
    public final boolean assignableTo(final Type otherType) {
      return this == otherType;
    }

  }

  private static final class FunctionType extends Type {

    public static final FunctionType ANY_FUNCTION = new FunctionType(-1);

    public final int numberOfParameters;

    public FunctionType(final int numberOfParameters) {
      this.numberOfParameters = numberOfParameters;
    }

    @Override
    public final boolean assignableTo(final Type otherType) {
      if (this == otherType) {
        return true;
      }

      if (!(otherType instanceof FunctionType)) {
        return false;
      }

      final FunctionType otherFunctionType = (FunctionType) otherType;

      return
          (this.numberOfParameters == otherFunctionType.numberOfParameters)
          || this == ANY_FUNCTION || otherFunctionType == ANY_FUNCTION;
    }

  }

  public static final Type integer() {
    return IntegerType.INSTANCE;
  }

  public static final Type function(final int numberOfParameters) {
    return new FunctionType(numberOfParameters);
  }

  public static final Type anyFunction() {
    return FunctionType.ANY_FUNCTION;
  }

  public static final int numberOfParameters(final Type type) {
    if (type instanceof FunctionType) {
      return ((FunctionType) type).numberOfParameters;
    } else {
      throw new RuntimeException("not a function type");
    }
  }

  protected abstract boolean assignableTo(final Type otherType);

  public static final boolean assignable(final Type typeOne, final Type typeTwo) {
    return typeOne.assignableTo(typeTwo);
  }

}
