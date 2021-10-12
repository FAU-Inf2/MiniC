package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.semantics.types.AtomicType;
import i2.act.examples.minic.frontend.semantics.types.Type;

public final class BinaryExpression extends Expression {

  public static enum Operator {

    /* boolean operators */
    OR            ("||", 1, AtomicType.BOOLEAN, AtomicType.BOOLEAN),
    AND           ("&&", 2, AtomicType.BOOLEAN, AtomicType.BOOLEAN),
    /* compare operators */
    EQUALS        ("==", 3, AtomicType.INT, AtomicType.BOOLEAN),
    LESS_THAN     ("<",  3, AtomicType.INT, AtomicType.BOOLEAN),
    LESS_EQUALS   ("<=", 3, AtomicType.INT, AtomicType.BOOLEAN),
    GREATER_THAN  (">",  3, AtomicType.INT, AtomicType.BOOLEAN),
    GREATER_EQUALS(">=", 3, AtomicType.INT, AtomicType.BOOLEAN),
    NOT_EQUALS    ("!=", 3, AtomicType.INT, AtomicType.BOOLEAN),
    /* arithmetic operators */
    ADD           ("+",  4, AtomicType.INT, AtomicType.INT),
    SUB           ("-",  4, AtomicType.INT, AtomicType.INT),
    MUL           ("*",  5, AtomicType.INT, AtomicType.INT),
    DIV           ("/",  5, AtomicType.INT, AtomicType.INT);

    private final String stringRepresentation;
    private final int precedence;

    public final Type sourceType;
    public final Type resultType;

    private Operator(final String stringRepresentation, final int precedence,
        final Type sourceType, final Type resultType) {
      this.stringRepresentation = stringRepresentation;
      this.precedence = precedence;
      this.sourceType = sourceType;
      this.resultType = resultType;
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
