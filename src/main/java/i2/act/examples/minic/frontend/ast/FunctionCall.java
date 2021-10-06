package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.Collections;
import java.util.List;

public final class FunctionCall extends Expression {

  private final Identifier callee;
  private final List<Expression> arguments;

  public FunctionCall(final SourcePosition position, final Identifier callee,
      final List<Expression> arguments) {
    super(position);
    this.callee = callee;
    this.arguments = arguments;
  }

  public final Identifier getCallee() {
    return this.callee;
  }

  public final List<Expression> getArguments() {
    return Collections.unmodifiableList(this.arguments);
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
