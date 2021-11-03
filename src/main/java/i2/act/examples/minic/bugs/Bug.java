package i2.act.examples.minic.bugs;

public enum Bug {

  TODO_LEXER("todo_lexer", Category.LEXER),
  TODO_PARSER("todo_parser", Category.PARSER),
  TODO_ANALYSIS("todo_analysis", Category.ANALYSIS),
  TODO_INTERPRETER("todo_interpreter", Category.INTERPRETER);

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
