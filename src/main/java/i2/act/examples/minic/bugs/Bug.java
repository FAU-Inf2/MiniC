package i2.act.examples.minic.bugs;

public enum Bug {

  // lexer bugs
  MISSING_TOKEN_ELSE("missing_token_else", Category.LEXER),
  MISSING_TOKEN_WHILE("missing_token_while", Category.LEXER),
  WRONG_TOKEN_IF("wrong_token_if", Category.LEXER),
  WRONG_TOKEN_PLUS("wrong_token_plus", Category.LEXER),
  NO_EQUALS_TOKEN("no_equals_token", Category.LEXER),
  WRONG_REGEX_AND("wrong_regex_and", Category.LEXER),
  ADDITIONAL_SKIP("additional_skip", Category.LEXER),

  // parser bugs
  MISSING_TREE_ELSE("missing_tree_else", Category.PARSER),
  MISSING_ALTERNATIVE_NOT_EQUALS("missing_alternative_not_equals", Category.PARSER),
  MISSING_ALTERNATIVE_CALL_STMT("missing_alternative_call_stmt", Category.PARSER),
  ADDITIONAL_SEMICOLON_FUNCTION("additional_semicolon_function", Category.PARSER),
  MISSING_COMMA_ARGUMENTS("missing_comma_arguments", Category.PARSER),
  SWAPPED_OPERANDS_PLUS("swapped_operands_plus", Category.PARSER),
  RIGHT_ASSOCIATIVE_ADD_EXPR("right_associative_add_expr", Category.PARSER),

  // analysis bugs
  MISSING_SYMBOL_CALLEE("missing_symbol_callee", Category.ANALYSIS),
  MISSING_TYPE_TYPE_NAME("missing_type_type_name", Category.ANALYSIS),
  MISSING_CHECK_RETURN_VOID("missing_check_return_void", Category.ANALYSIS),
  MISSING_CHECK_RETURN_NON_VOID("missing_check_return_non_void", Category.ANALYSIS),
  WRONG_ORDER_SYMBOL_TABLE("wrong_order_symbol_table", Category.ANALYSIS),

  // interpreter bugs
  DIV_BY_ZERO("div_by_zero", Category.INTERPRETER),
  NO_SHORTCUT_OR("no_shortcut_or", Category.INTERPRETER),
  NO_SHORTCUT_AND("no_shortcut_and", Category.INTERPRETER);

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
