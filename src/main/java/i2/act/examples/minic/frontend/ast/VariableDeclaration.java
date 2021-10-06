package i2.act.examples.minic.frontend.ast;

import i2.act.examples.minic.frontend.ast.visitors.ASTVisitor;
import i2.act.examples.minic.frontend.info.SourcePosition;

public final class VariableDeclaration extends Declaration {

  private final TypeName typeName;
  private final Identifier name;

  public VariableDeclaration(final SourcePosition position, final TypeName typeName,
      final Identifier name) {
    super(position);
    this.typeName = typeName;
    this.name = name;
  }

  public final TypeName getTypeName() {
    return this.typeName;
  }

  public final Identifier getName() {
    return this.name;
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
