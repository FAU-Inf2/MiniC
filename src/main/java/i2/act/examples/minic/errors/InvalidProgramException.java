package i2.act.examples.minic.errors;

import i2.act.examples.minic.frontend.info.SourcePosition;

public final class InvalidProgramException extends RuntimeException {

  public static enum Kind {

    LEXICALLY_INVALID(130),
    SYNTACTICALLY_INVALID(131),
    SEMANTICALLY_INVALID(132),
    DYNAMICALLY_INVALID(133);

    public final int exitCode;

    private Kind(final int exitCode) {
      this.exitCode = exitCode;
    }

  }

  private final Kind kind;
  private final SourcePosition position;

  private InvalidProgramException(final Kind kind, final SourcePosition position,
      final String message) {
    super(String.format("[%s] %s", position, message));
    this.kind = kind;
    this.position = position;
  }

  public final Kind getKind() {
    return this.kind;
  }

  public final SourcePosition getPosition() {
    return this.position;
  }

  public final int getExitCode() {
    return this.kind.exitCode;
  }

  // -----------------------------------------------------------------------------------------------

  public static final InvalidProgramException lexicallyInvalid(final String message) {
    return lexicallyInvalid(SourcePosition.UNKNOWN, message);
  }

  public static final InvalidProgramException lexicallyInvalid(final SourcePosition position,
      final String message) {
    return new InvalidProgramException(Kind.LEXICALLY_INVALID, position, message);
  }

  public static final InvalidProgramException syntacticallyInvalid(final String message) {
    return syntacticallyInvalid(SourcePosition.UNKNOWN, message);
  }

  public static final InvalidProgramException syntacticallyInvalid(final SourcePosition position,
      final String message) {
    return new InvalidProgramException(Kind.SYNTACTICALLY_INVALID, position, message);
  }

  public static final InvalidProgramException semanticallyInvalid(final String message) {
    return semanticallyInvalid(SourcePosition.UNKNOWN, message);
  }

  public static final InvalidProgramException semanticallyInvalid(final SourcePosition position,
      final String message) {
    return new InvalidProgramException(Kind.SEMANTICALLY_INVALID, position, message);
  }

  public static final InvalidProgramException dynamicallyInvalid(final String message) {
    return dynamicallyInvalid(SourcePosition.UNKNOWN, message);
  }

  public static final InvalidProgramException dynamicallyInvalid(final SourcePosition position,
      final String message) {
    return new InvalidProgramException(Kind.DYNAMICALLY_INVALID, position, message);
  }

}
