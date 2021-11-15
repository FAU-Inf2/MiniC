package i2.act.examples.minic.frontend.lexer;

import i2.act.examples.minic.frontend.info.SourcePosition;

public final class LazyTokenStream extends TokenStream {

  public static final LazyTokenStream from(final Lexer lexer) {
    return new LazyTokenStream(lexer);
  }

  // ===============================================================================================

  private final Lexer lexer;

  private LazyTokenStream(final Lexer lexer) {
    this.lexer = lexer;
  }

  @Override
  public final Token peek() {
    return this.lexer.peek();
  }

  @Override
  public final Token pop() {
    return this.lexer.pop();
  }

  @Override
  public final SourcePosition getPosition() {
    return this.lexer.getPosition();
  }

}
