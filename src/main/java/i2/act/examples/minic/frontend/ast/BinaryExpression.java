package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class BinaryExpression extends Expression {

  public static enum Operator {

    /* boolean operators */
    OR            ("||", 1),
    AND           ("&&", 2),
    /* compare operators */
    EQUALS        ("==", 3),
    LESS_THAN     ("<",  3),
    LESS_EQUALS   ("<=", 3),
    GREATER_THAN  (">",  3),
    GREATER_EQUALS(">=", 3),
    NOT_EQUALS    ("!=", 3),
    /* arithmetic operators */
    ADD           ("+",  4),
    SUB           ("-",  4),
    MUL           ("*",  5),
    DIV           ("/",  5);

    private final String stringRepresentation;
    private final int precedence;

    private Operator(final String stringRepresentation, final int precedence) {
      this.stringRepresentation = stringRepresentation;
      this.precedence = precedence;
    }

  }

  private final Operator operator;
  private final Expression leftHandSide;
  private final Expression rightHandSide;

  public BinaryExpression(final SourcePosition position, final Operator operator,
      final Expression leftHandSide, final Expression rightHandSide) {
    super(position);
    this.operator = operator;
    this.leftHandSide = leftHandSide;
    this.rightHandSide = rightHandSide;
  }

  public final Operator getOperator() {
    return this.operator;
  }

  public final Expression getLeftHandSide() {
    return this.leftHandSide;
  }

  public final Expression getRightHandSide() {
    return this.rightHandSide;
  }

  @Override
  public final String toString() {
    return this.operator.stringRepresentation;
  }

  @Override
  public final boolean isTerminal() {
    return false;
  }

  @Override
  public final <P, R> R accept(final ASTVisitor<P, R> visitor, final P parameter) {
    return visitor.visit(this, parameter);
  }

}
