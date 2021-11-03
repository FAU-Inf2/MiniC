package i2.act.examples.minic.bugs;

public enum Bug {

  MISSING_TOKEN_ELSE("missing_token_else", Category.LEXER),
  WRONG_TOKEN_IF("wrong_token_if", Category.LEXER),
  WRONG_TOKEN_PLUS("wrong_token_plus", Category.LEXER);

  // ===============================================================================================

  public static enum Category {
    LEXER,
    PARSER,
    ANALYSIS,
    INTERPRETER;
  }

  // ===============================================================================================

  private final String name;
  private final Category category;

  private Bug(final String name, final Category category) {
    this.name = name;
    this.category = category;
  }

  public final String getName() {
    return this.name;
  }

  public final Category getCategory() {
    return this.category;
  }

  public static final Bug fromName(final String name) {
    for (final Bug bug : Bug.values()) {
      if (name.equals(bug.name)) {
        return bug;
      }
    }

    return null;
  }

}
