package i2.act.examples.minic.frontend.lexer;

public enum TokenKind {

  TK_VOID("void"),
  TK_INT("int"),
  TK_IF("if"),
  TK_ELSE("else"),
  TK_RETURN("return"),
  TK_SEMICOLON(";"),
  TK_ASSIGN("="),
  TK_EQUALS("=="),
  TK_LESS_THAN("<"),
  TK_GREATER_THAN(">"),
  TK_LESS_EQUALS("<="),
  TK_GREATER_EQUALS(">="),
  TK_NOT_EQUALS("!="),
  TK_COMMA(","),
  TK_LPAREN("("),
  TK_RPAREN(")"),
  TK_LBRACE("{"),
  TK_RBRACE("}"),
  TK_OR_OP("||"),
  TK_AND_OP("&&"),
  TK_ADD("+"),
  TK_SUB("-"),
  TK_MUL("*"),
  TK_DIV("/"),
  TK_NUM("<NUM>"),
  TK_IDENTIFIER("<IDENTIFIER>"),
  TK_EOF("<EOF>");

  public final String stringRepresentation;

  private TokenKind(final String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public final String toString() {
    return this.stringRepresentation;
  }

}
