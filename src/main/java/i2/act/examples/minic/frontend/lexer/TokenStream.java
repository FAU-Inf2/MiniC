package i2.act.examples.minic.frontend.lexer;

import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.info.SourcePosition;

public abstract class TokenStream {

  public abstract Token peek();

  public abstract Token pop();

  public abstract SourcePosition getPosition();

  public final void assertNotPeek(final TokenKind... kinds) {
    final Token token = peek();
    final TokenKind actualKind = token.getKind();

    for (final TokenKind kind : kinds) {
      if (kind.equals(actualKind)) {
        throw InvalidProgramException.syntacticallyInvalid(getPosition(),
            String.format("did not expect '%s'", actualKind));
      }
    }
  }

  public final void assertPeek(final TokenKind... kinds) {
    final Token token = peek();
    final TokenKind actualKind = token.getKind();

    for (final TokenKind kind : kinds) {
      if (kind.equals(actualKind)) {
        return;
      }
    }

    // next token does not match any of the expected tokens -> throw exception
    final String expected = constructExpected(kinds);

    throw InvalidProgramException.syntacticallyInvalid(getPosition(),
        String.format("expected %s, but found '%s'", expected, token.string));
  }

  public final boolean peekIs(final TokenKind... kinds) {
    final Token token = peek();
    final TokenKind actualKind = token.getKind();

    for (final TokenKind kind : kinds) {
      if (kind.equals(actualKind)) {
        return true;
      }
    }

    return false;
  }

  public final boolean skip(final TokenKind kind) {
    final Token token = peek();
    final TokenKind actualKind = token.getKind();

    if (kind.equals(actualKind)) {
      pop();
      return true;
    } else {
      return false;
    }
  }

  public final Token assertPop(final TokenKind kind) {
    final Token token = peek();
    final TokenKind actualKind = token.getKind();

    if (!kind.equals(actualKind)) {
      throw InvalidProgramException.syntacticallyInvalid(getPosition(),
          String.format("expected '%s', but found '%s'", kind, token.string));
    }

    pop();

    return token;
  }

  protected final String constructExpected(final TokenKind... kinds) {
    final StringBuilder builder = new StringBuilder();

    for (int index = 0; index < kinds.length; ++index) {
      builder.append("'");
      builder.append(kinds[index]);
      builder.append("'");

      if (index < kinds.length - 1) {
        if (kinds.length > 2) {
          builder.append(",");
        }

        builder.append(" ");

        if (index == kinds.length - 2) {
          builder.append("or ");
        }
      }
    }

    return builder.toString();
  }

}
