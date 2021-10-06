package i2.act.examples.minic.frontend.lexer;

import i2.act.examples.minic.frontend.info.SourcePosition;

public final class Token {

  public final TokenKind kind;

  public final SourcePosition begin;
  public final SourcePosition end;

  public final String string;

  public Token(final TokenKind kind, final SourcePosition begin) {
    this(kind, begin,
        new SourcePosition(
            begin.offset + kind.stringRepresentation.length(),
            begin.line,
            begin.column + kind.stringRepresentation.length()),
        null);
  }

  public Token(final TokenKind kind, final SourcePosition begin, final SourcePosition end) {
    this(kind, begin, end, null);
  }

  public Token(final TokenKind kind, final SourcePosition begin, final SourcePosition end,
      final String string) {
    this.kind = kind;

    this.begin = begin;
    this.end = end;

    this.string = string;
  }

  public final TokenKind getKind() {
    return this.kind;
  }

  public final SourcePosition getBegin() {
    return this.begin;
  }

  public final SourcePosition getEnd() {
    return this.end;
  }

  public final String toString() {
    if (this.string == null) {
      return String.format("TK<%s, %s>", this.kind, this.begin);
    } else {
      return String.format("TK<%s:'%s', %s, %s>", this.kind, this.string, this.begin, this.end);
    }
  }

}
