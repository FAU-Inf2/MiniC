package i2.act.examples.minic.frontend.lexer;

import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.ArrayList;
import java.util.List;

public final class EagerTokenStream extends TokenStream {

  public static final EagerTokenStream from(final Lexer lexer) {
    final List<Token> tokens = new ArrayList<>();

    // construct token stream
    {
      Token nextToken;
      while ((nextToken = lexer.pop()).getKind() != TokenKind.TK_EOF) {
        tokens.add(nextToken);
      }
    }

    final SourcePosition endPosition = lexer.getPosition();

    return new EagerTokenStream(tokens, endPosition);
  }

  // ===============================================================================================

  private final List<Token> tokens;
  private int nextTokenIndex;

  private final SourcePosition endPosition;

  private EagerTokenStream(final List<Token> tokens, final SourcePosition endPosition) {
    this.tokens = tokens;
    this.nextTokenIndex = 0;

    this.endPosition = endPosition;
  }

  private final boolean reachedEnd() {
    return this.nextTokenIndex >= this.tokens.size();
  }

  @Override
  public final Token peek() {
    if (reachedEnd()) {
      return new Token(TokenKind.TK_EOF, this.endPosition);
    } else {
      assert (this.nextTokenIndex < this.tokens.size());

      final Token nextToken = this.tokens.get(this.nextTokenIndex);
      return nextToken;
    }
  }

  @Override
  public final Token pop() {
    if (reachedEnd()) {
      return new Token(TokenKind.TK_EOF, this.endPosition);
    } else {
      assert (this.nextTokenIndex < this.tokens.size());

      final Token nextToken = this.tokens.get(this.nextTokenIndex);
      ++this.nextTokenIndex;

      return nextToken;
    }
  }

  @Override
  public final SourcePosition getPosition() {
    if (reachedEnd()) {
      return this.endPosition;
    } else {
      assert (this.nextTokenIndex < this.tokens.size());
      return this.tokens.get(this.nextTokenIndex).getBegin();
    }
  }

}
