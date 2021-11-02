package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.semantics.symbols.Symbol;
import i2.act.examples.minic.frontend.semantics.types.Type;

import java.util.Collections;
import java.util.List;

public final class FunctionDeclaration extends Declaration {

  private final TypeName returnType;
  private final Identifier name;
  private final List<VariableDeclaration> parameters;
  private final Block body;

  public FunctionDeclaration(final SourcePosition position, final TypeName returnType,
      final Identifier name, final List<VariableDeclaration> parameters, final Block body) {
    super(position);
    this.returnType = returnType;
    this.name = name;
    this.parameters = parameters;
    this.body = body;
  }

  public final TypeName getReturnType() {
    return this.returnType;
  }

  public final Identifier getName() {
    return this.name;
  }

  public final List<VariableDeclaration> getParameters() {
    return Collections.unmodifiableList(this.parameters);
  }

  public final Block getBody() {
    return this.body;
  }

  @Override
  public final Symbol getSymbol() {
    return this.name.getSymbol();
  }

  @Override
  public final Type getType() {
    if (this.name.getSymbol() == null) {
      return null;
    }

    return this.name.getSymbol().getType();
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
