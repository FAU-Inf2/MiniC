package i2.act.examples.minic.frontend.lexer;

import i2.act.examples.minic.bugs.Bug;
import i2.act.examples.minic.bugs.Bugs;
import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.function.Function;

public final class Lexer {

  private final char[] characters;

  private int position;

  private int line;
  private int column;

  private int lookaheadPosition;
  private int lookaheadLine;
  private int lookaheadColumn;

  public Lexer(final String string) {
    this(string.toCharArray());
  }

  public Lexer(final char[] characters) {
    this.characters = characters;
    this.position = 0;
    this.line = 1;
    this.column = 1;
  }

  public final SourcePosition getPosition() {
    return new SourcePosition(this.position, this.line, this.column);
  }

  public final void advanceTo(final SourcePosition position) {
    this.position = position.offset;
    this.line = position.line;
    this.column = position.column;
  }

  public final Token pop() {
    final Token token = peek();
    advanceTo(token.getEnd());

    return token;
  }

  public final Token peek() {
    this.lookaheadPosition = this.position;
    this.lookaheadLine = this.line;
    this.lookaheadColumn = this.column;

    skip: while (true) { // skip whitespace and comments
      if (this.lookaheadPosition >= this.characters.length) {
        final SourcePosition begin =
            new SourcePosition(this.lookaheadPosition, this.lookaheadLine, this.lookaheadColumn);
        return new Token(TokenKind.TK_EOF, begin);
      }

      final char firstChar = this.characters[this.lookaheadPosition];

      // handle consecutive whitespace
      if (isWhitespaceCharacter(firstChar)) {
        while (this.lookaheadPosition < this.characters.length) {
          final char nextChar = this.characters[this.lookaheadPosition];

          if (!isWhitespaceCharacter(nextChar)) {
            break;
          }

          ++this.lookaheadPosition;
          ++this.lookaheadColumn;

          if (nextChar == '\n') {
            ++this.lookaheadLine;
            this.lookaheadColumn = 1;
          }
        }

        continue skip;
      }

      // handle line comments
      if (firstChar == '/'
          && this.lookaheadPosition < this.characters.length - 1
          && this.characters[this.lookaheadPosition + 1] == '/') {
        while (this.lookaheadPosition < this.characters.length) {
          final char nextChar = this.characters[this.lookaheadPosition];

          ++this.lookaheadPosition;
          ++this.lookaheadColumn;

          if (nextChar == '\n') {
            ++this.lookaheadLine;
            this.lookaheadColumn = 1;

            // found end of comment
            continue skip;
          }
        }
      }

      // handle block comments
      if (firstChar == '/'
          && this.lookaheadPosition < this.characters.length - 1
          && this.characters[this.lookaheadPosition + 1] == '*') {
        this.lookaheadPosition += 2;
        this.lookaheadColumn += 2;

        while (this.lookaheadPosition < this.characters.length - 1) {
          final char nextChar = this.characters[this.lookaheadPosition];
          final char nextNextChar = this.characters[this.lookaheadPosition + 1];

          ++this.lookaheadPosition;
          ++this.lookaheadColumn;

          if (nextChar == '\n') {
            ++this.lookaheadLine;
            this.lookaheadColumn = 1;
          } else if (nextChar == '*' && nextNextChar == '/') {
            ++this.lookaheadPosition;
            ++this.lookaheadColumn;

            // found end of comment
            continue skip;
          }
        }

        throw InvalidProgramException.lexicallyInvalid(getPosition(),
            "unterminated block comment");
      }

      final SourcePosition begin =
          new SourcePosition(this.lookaheadPosition, this.lookaheadLine, this.lookaheadColumn);

      switch (firstChar) {
        case ';': {
          return new Token(TokenKind.TK_SEMICOLON, begin);
        }
        case '=': {
          // check for injected bug
          {
            if (Bugs.getInstance().isEnabled(Bug.NO_EQUALS_TOKEN)) {
              return new Token(TokenKind.TK_ASSIGN, begin);
            }
          }

          return new Token(
              checkNextCharacter('=', TokenKind.TK_EQUALS, TokenKind.TK_ASSIGN),
              begin);
        }
        case '<': {
          return new Token(
              checkNextCharacter('=', TokenKind.TK_LESS_EQUALS, TokenKind.TK_LESS_THAN),
              begin);
        }
        case '>': {
          return new Token(
              checkNextCharacter('=', TokenKind.TK_GREATER_EQUALS, TokenKind.TK_GREATER_THAN),
              begin);
        }
        case '!': {
          return new Token(
              checkNextCharacter('=', TokenKind.TK_NOT_EQUALS, null),
              begin);
        }
        case '|': {
          return new Token(
              checkNextCharacter('|', TokenKind.TK_OR_OP, null),
              begin);
        }
        case '&': {
          // check for injected bug
          {
            if (Bugs.getInstance().isEnabled(Bug.WRONG_REGEX_AND)) {
              return new Token(
                  checkNextCharacter('|', TokenKind.TK_AND_OP, null),
                  begin);
            }
          }

          return new Token(
              checkNextCharacter('&', TokenKind.TK_AND_OP, null),
              begin);
        }
        case ',': {
          return new Token(TokenKind.TK_COMMA, begin);
        }
        case '(': {
          return new Token(TokenKind.TK_LPAREN, begin);
        }
        case ')': {
          return new Token(TokenKind.TK_RPAREN, begin);
        }
        case '{': {
          return new Token(TokenKind.TK_LBRACE, begin);
        }
        case '}': {
          return new Token(TokenKind.TK_RBRACE, begin);
        }
        case '+': {
          // check for injected bug
          {
            if (Bugs.getInstance().isEnabled(Bug.WRONG_TOKEN_PLUS)) {
              return new Token(TokenKind.TK_MUL, begin);
            }
          }

          return new Token(TokenKind.TK_ADD, begin);
        }
        case '-': {
          return new Token(TokenKind.TK_SUB, begin);
        }
        case '*': {
          return new Token(TokenKind.TK_MUL, begin);
        }
        case '/': {
          return new Token(TokenKind.TK_DIV, begin);
        }
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9': {
          final Function<Character, Boolean> matches = (c -> (c >= '0' && c <= '9'));

          final String value = parseCharacterSequence(matches);

          final SourcePosition end =
              new SourcePosition(this.lookaheadPosition, this.lookaheadLine, this.lookaheadColumn);

          return new Token(TokenKind.TK_NUM, begin, end, value);
        }
        default: {
          final Function<Character, Boolean> matches;
          {
            if ((firstChar >= 'A' && firstChar <= 'Z')
                || (firstChar >= 'a' && firstChar <= 'z')
                || (firstChar == '_')) {
              matches = (c -> (
                  (c >= 'A' && c <= 'Z')
                  || (c >= 'a' && c <= 'z')
                  || (c == '_')
                  || (c >= '0' && c <= '9')));
            } else {
              throw InvalidProgramException.lexicallyInvalid(getPosition(),
                  String.format("invalid character '%c'", firstChar));
            }
          }

          final String tokenText = parseCharacterSequence(matches);

          // check for injected bug
          {
            if (Bugs.getInstance().isEnabled(Bug.MISSING_TOKEN_ELSE) && tokenText.equals("else")) {
              continue skip;
            }

            if (Bugs.getInstance().isEnabled(Bug.MISSING_TOKEN_WHILE)
                && tokenText.equals("while")) {
              continue skip;
            }

            if (Bugs.getInstance().isEnabled(Bug.WRONG_TOKEN_IF) && tokenText.equals("if")) {
              final SourcePosition end = new SourcePosition(
                  this.lookaheadPosition, this.lookaheadLine, this.lookaheadColumn);

              return new Token(TokenKind.TK_IDENT, begin, end, tokenText);
            }
          }

          switch (tokenText) {
            case "void": {
              return new Token(TokenKind.TK_VOID, begin);
            }
            case "int": {
              return new Token(TokenKind.TK_INT, begin);
            }
            case "if": {
              return new Token(TokenKind.TK_IF, begin);
            }
            case "else": {
              return new Token(TokenKind.TK_ELSE, begin);
            }
            case "while": {
              return new Token(TokenKind.TK_WHILE, begin);
            }
            case "return": {
              return new Token(TokenKind.TK_RETURN, begin);
            }
            default: {
              final SourcePosition end = new SourcePosition(
                  this.lookaheadPosition, this.lookaheadLine, this.lookaheadColumn);

              return new Token(TokenKind.TK_IDENT, begin, end, tokenText);
            }
          }
        }
      }
    }
  }

  private final TokenKind checkNextCharacter(final char nextCharacter,
      final TokenKind thenKind, final TokenKind elseKind) {
    final char firstChar = this.characters[this.lookaheadPosition];

    ++this.lookaheadPosition;
    ++this.lookaheadColumn;

    if (this.lookaheadPosition < this.characters.length
        && this.characters[this.lookaheadPosition] == nextCharacter) {
      return thenKind;
    } else {
      if (elseKind == null) {
        throw InvalidProgramException.lexicallyInvalid(getPosition(),
            String.format("invalid character '%c'", firstChar));
      } else {
        return elseKind;
      }
    }
  }

  private static final boolean isWhitespaceCharacter(final char c) {
    return c == ' ' || c == '\n' || c == '\r' || c == '\t';
  }

  private final String parseCharacterSequence(final Function<Character, Boolean> matches) {
    final StringBuilder builder = new StringBuilder();

    while (this.lookaheadPosition < this.characters.length) {
      final char nextChar = this.characters[this.lookaheadPosition];

      if (matches.apply(nextChar)) {
        builder.append(nextChar);

        ++this.lookaheadPosition;
        ++this.lookaheadColumn;

        assert (nextChar != '\n');
      } else {
        break;
      }
    }

    // check for injected bug
    {
      if (Bugs.getInstance().isEnabled(Bug.ADDITIONAL_SKIP)) {
        ++this.lookaheadPosition;
        ++this.lookaheadColumn;
      }
    }

    return builder.toString();
  }

  private final int count(final String string, final char character) {
    int count = 0;

    for (final char stringCharacter : string.toCharArray()) {
      count += (stringCharacter == character) ? (1) : (0);
    }

    return count;
  }

}
