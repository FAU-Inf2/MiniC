package i2.act.examples.minic.frontend.lexer;

import static i2.act.examples.minic.frontend.ast.BinaryExpression.Operator;

public enum TokenKind {

  TK_EOF("<EOF>"),
  TK_VOID("void"),
  TK_INT("int"),
  TK_IF("if"),
  TK_ELSE("else"),
  TK_RETURN("return"),
  TK_SEMICOLON(";"),
  TK_ASSIGN("="),
  TK_COMMA(","),
  TK_LPAREN("("),
  TK_RPAREN(")"),
  TK_LBRACE("{"),
  TK_RBRACE("}"),
  TK_NUM("<NUM>"),
  TK_IDENTIFIER("<IDENTIFIER>"),
  /* boolean operators */
  TK_OR_OP("||", Operator.OR),
  TK_AND_OP("&&", Operator.AND),
  /* compare operators */
  TK_EQUALS("==", Operator.EQUALS),
  TK_LESS_THAN("<", Operator.LESS_THAN),
  TK_GREATER_THAN(">", Operator.GREATER_THAN),
  TK_LESS_EQUALS("<=", Operator.LESS_EQUALS),
  TK_GREATER_EQUALS(">=", Operator.GREATER_EQUALS),
  TK_NOT_EQUALS("!=", Operator.NOT_EQUALS),
  /* arithmetic operators */
  TK_ADD("+", Operator.ADD),
  TK_SUB("-", Operator.SUB),
  TK_MUL("*", Operator.MUL),
  TK_DIV("/", Operator.DIV);

  public final String stringRepresentation;
  public final Operator operator;

  private TokenKind(final String stringRepresentation) {
    this(stringRepresentation, null);
  }

  private TokenKind(final String stringRepresentation, final Operator operator) {
    this.stringRepresentation = stringRepresentation;
    this.operator = operator;
  }

  @Override
  public final String toString() {
    return this.stringRepresentation;
  }

}
