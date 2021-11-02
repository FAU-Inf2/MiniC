package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class FunctionCallStatement extends Statement {

  private final FunctionCall functionCall;

  public FunctionCallStatement(final SourcePosition position, final FunctionCall functionCall) {
    super(position);
    this.functionCall = functionCall;
  }

  public final FunctionCall getFunctionCall() {
    return this.functionCall;
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
