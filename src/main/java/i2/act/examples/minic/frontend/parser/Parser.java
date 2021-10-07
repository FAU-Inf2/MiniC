package i2.act.examples.minic.frontend.parser;

import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Parser {

  public static final Program parse(final String input) {
    return parse(new Lexer(input));
  }

  public static final Program parse(final Lexer lexer) {
    return parseProgram(lexer);
  }

  private static final Program parseProgram(final Lexer lexer) {
    // program
    //   : ( global_decl )* EOF
    //   ;

    final SourcePosition position = lexer.getPosition();

    final List<Declaration> declarations = new ArrayList<>();

    while (!lexer.peekIs(TokenKind.TK_EOF)) {
      final Declaration globalDeclaration = parseGlobaleDeclaration(lexer);
      declarations.add(globalDeclaration);
    }

    return new Program(position, declarations);
  }

  private static final Declaration parseGlobaleDeclaration(final Lexer lexer) {
    // global_decl
    //   : type_name IDENTIFIER SEMICOLON
    //   | type_name IDENTIFIER func_decl_part
    //   ;

    final SourcePosition position = lexer.getPosition();

    final TypeName typeName = parseTypeName(lexer);
    final Identifier name = parseIdentifier(lexer);

    lexer.assertPeek(TokenKind.TK_SEMICOLON, TokenKind.TK_LPAREN);

    if (lexer.peekIs(TokenKind.TK_SEMICOLON)) {
      // variable declaration
      lexer.assertPop(TokenKind.TK_SEMICOLON);

      return new VariableDeclaration(position, typeName, name);
    } else {
      // function declaration
      return parseFunctionDeclarationPart(lexer, position, typeName, name);
    }
  }

  private static final FunctionDeclaration parseFunctionDeclarationPart(final Lexer lexer,
      final SourcePosition position, final TypeName typeName, final Identifier name) {
    // func_decl_part
    //   : LPAREN param_decl_list RPAREN block
    //   ;

    lexer.assertPop(TokenKind.TK_LPAREN);

    final List<VariableDeclaration> parameters = parseParameterDeclarationList(lexer);

    lexer.assertPop(TokenKind.TK_RPAREN);

    final Block body = parseBlock(lexer);

    return new FunctionDeclaration(position, typeName, name, parameters, body);
  }

  private static final List<VariableDeclaration> parseParameterDeclarationList(final Lexer lexer) {
    // param_decl_list
    //   : ( param_decl ( COMMA param_decl )* )?
    //   ;

    final List<VariableDeclaration> parameters = new ArrayList<>();

    if (!lexer.peekIs(TokenKind.TK_RPAREN)) {
      // first parameter
      {
        final VariableDeclaration parameterDeclaration = parseParameterDeclaration(lexer);
        parameters.add(parameterDeclaration);
      }

      while (lexer.peekIs(TokenKind.TK_COMMA)) {
        lexer.assertPop(TokenKind.TK_COMMA);

        final VariableDeclaration parameterDeclaration = parseParameterDeclaration(lexer);
        parameters.add(parameterDeclaration);
      }
    }

    return parameters;
  }

  private static final VariableDeclaration parseParameterDeclaration(final Lexer lexer) {
    // param_decl
    //   : type_name IDENTIFIER
    //   ;

    final SourcePosition position = lexer.getPosition();

    final TypeName typeName = parseTypeName(lexer);
    final Identifier name = parseIdentifier(lexer);

    return new VariableDeclaration(position, typeName, name);
  }

  private static final VariableDeclaration parseVariableDeclaration(final Lexer lexer) {
    // var_decl
    //   : type_name IDENTIFIER SEMICOLON
    //   ;

    final SourcePosition position = lexer.getPosition();

    final TypeName typeName = parseTypeName(lexer);
    final Identifier name = parseIdentifier(lexer);

    lexer.assertPop(TokenKind.TK_SEMICOLON);

    return new VariableDeclaration(position, typeName, name);
  }

  private static final TypeName parseTypeName(final Lexer lexer) {
    // type_name
    //   : INT
    //   | VOID
    //   ;

    final SourcePosition position = lexer.getPosition();

    lexer.assertPeek(TokenKind.TK_INT, TokenKind.TK_VOID);
    final Token typeName = lexer.pop();

    return new TypeName(position, typeName);
  }

  private static final Identifier parseIdentifier(final Lexer lexer) {
    final SourcePosition position = lexer.getPosition();

    final Token identifier = lexer.assertPop(TokenKind.TK_IDENTIFIER);

    return new Identifier(position, identifier);
  }

  private static final Block parseBlock(final Lexer lexer) {
    // block
    //   : LBRACE ( stmt )* RBRACE
    //   ;

    final SourcePosition position = lexer.getPosition();

    lexer.assertPop(TokenKind.TK_LBRACE);

    final List<Statement> statements = new ArrayList<>();

    while (!lexer.peekIs(TokenKind.TK_RBRACE)) {
      final Statement statement = parseStatement(lexer);
      statements.add(statement);
    }

    lexer.assertPop(TokenKind.TK_RBRACE);

    return new Block(position, statements);
  }

  private static final Statement parseStatement(final Lexer lexer) {
    // stmt
    //   : IDENTIFIER ASSIGN expr SEMICOLON
    //   | IF LPAREN expr RPAREN block ( ELSE block )?
    //   | RETURN ( expr )? SEMICOLON
    //   | var_decl
    //   ;

    final SourcePosition position = lexer.getPosition();

    if (lexer.peekIs(TokenKind.TK_IDENTIFIER)) {
      final Identifier leftHandSide = parseIdentifier(lexer);

      lexer.assertPop(TokenKind.TK_ASSIGN);

      final Expression rightHandSide = parseExpression(lexer);

      lexer.assertPop(TokenKind.TK_SEMICOLON);

      return new AssignStatement(position, leftHandSide, rightHandSide);
    } else if (lexer.peekIs(TokenKind.TK_IF)) {
      lexer.assertPop(TokenKind.TK_IF);
      lexer.assertPop(TokenKind.TK_LPAREN);

      final Expression condition = parseExpression(lexer);

      lexer.assertPop(TokenKind.TK_RPAREN);

      final Block thenBlock = parseBlock(lexer);

      if (lexer.peekIs(TokenKind.TK_ELSE)) {
        lexer.assertPop(TokenKind.TK_ELSE);

        final Block elseBlock = parseBlock(lexer);

        return new IfStatement(position, condition, thenBlock, elseBlock);
      } else {
        return new IfStatement(position, condition, thenBlock);
      }
    } else if (lexer.peekIs(TokenKind.TK_RETURN)) {
      lexer.assertPop(TokenKind.TK_RETURN);

      if (lexer.peekIs(TokenKind.TK_SEMICOLON)) {
        lexer.assertPop(TokenKind.TK_SEMICOLON);

        return new ReturnStatement(position);
      } else {
        final Expression returnValue = parseExpression(lexer);

        lexer.assertPop(TokenKind.TK_SEMICOLON);

        return new ReturnStatement(position, returnValue);
      }
    } else {
      final VariableDeclaration variableDeclaration = parseVariableDeclaration(lexer);
      return new DeclarationStatement(position, variableDeclaration);
    }
  }

  private static final Expression parseExpression(final Lexer lexer) {
    // expr
    //   : or_expr
    //   ;

    return parseOrExpression(lexer);
  }

  private static final Expression parseBinaryExpression(final Lexer lexer,
      final Function<Lexer, Expression> operandParser, final TokenKind... operators) {
    // <binary_expr>
    //   : <operand> ( <OPERATOR> <operand> )*
    //   ;

    final SourcePosition position = lexer.getPosition();

    Expression expression = operandParser.apply(lexer);

    while (lexer.peekIs(operators)) {
      final BinaryExpression.Operator operator = lexer.pop().kind.operator;
      assert (operator != null);

      final Expression otherExpression = operandParser.apply(lexer);
      expression = new BinaryExpression(position, operator, expression, otherExpression);
    }

    return expression;
  }

  private static final Expression parseOrExpression(final Lexer lexer) {
    // or_expr
    //   : and_expr ( OR_OP and_expr )*
    //   ;

    return parseBinaryExpression(
        lexer,
        (_lexer) -> parseAndExpression(_lexer),
        TokenKind.TK_OR_OP);
  }

  private static final Expression parseAndExpression(final Lexer lexer) {
    // and_expr
    //   : comp_expr ( AND_OP comp_expr )*
    //   ;

    return parseBinaryExpression(
        lexer,
        (_lexer) -> parseCompareExpression(_lexer),
        TokenKind.TK_AND_OP);
  }

  private static final Expression parseCompareExpression(final Lexer lexer) {
    // comp_expr
    //   : add_expr ( comp_op add_expr )*
    //   ;
    // comp_op
    //   : EQUALS | LESS_THAN | GREATER_THAN | LESS_EQUALS | GREATER_EQUALS | NOT_EQUALS
    //   ;

    return parseBinaryExpression(
        lexer,
        (_lexer) -> parseAddExpression(_lexer),
        TokenKind.TK_EQUALS, TokenKind.TK_LESS_THAN, TokenKind.TK_GREATER_THAN,
        TokenKind.TK_LESS_EQUALS, TokenKind.TK_GREATER_EQUALS, TokenKind.TK_NOT_EQUALS);
  }

  private static final Expression parseAddExpression(final Lexer lexer) {
    // add_expr
    //   : mul_expr ( add_op mul_expr )*
    //   ;
    // add_op
    //   : ADD | SUB
    //   ;

    return parseBinaryExpression(
        lexer,
        (_lexer) -> parseMulExpression(_lexer),
        TokenKind.TK_ADD, TokenKind.TK_SUB);
  }

  private static final Expression parseMulExpression(final Lexer lexer) {
    // mul_expr
    //   : factor ( mul_op factor )*
    //   ;
    // mul_op
    //   : MUL | DIV
    //   ;

    return parseBinaryExpression(
        lexer,
        (_lexer) -> parseFactor(_lexer),
        TokenKind.TK_MUL, TokenKind.TK_DIV);
  }

  private static final Expression parseFactor(final Lexer lexer) {
    // factor
    //   : func_call
    //   | NUM
    //   | IDENTIFIER
    //   | LPAREN expr RPAREN
    //   ;

    final SourcePosition position = lexer.getPosition();

    lexer.assertPeek(TokenKind.TK_IDENTIFIER, TokenKind.TK_NUM, TokenKind.TK_LPAREN);

    if (lexer.peekIs(TokenKind.TK_NUM)) {
      final Token token = lexer.assertPop(TokenKind.TK_NUM);

      return new Literal(position, token);
    } else if (lexer.peekIs(TokenKind.TK_LPAREN)) {
      lexer.assertPop(TokenKind.TK_LPAREN);

      final Expression expression = parseExpression(lexer);

      lexer.assertPop(TokenKind.TK_RPAREN);

      return expression;
    } else {
      final Identifier name = parseIdentifier(lexer);

      if (lexer.peekIs(TokenKind.TK_LPAREN)) {
        return parseFunctionCallPart(lexer, position, name);
      } else {
        return name;
      }
    }
  }

  private static final FunctionCall parseFunctionCallPart(final Lexer lexer,
      final SourcePosition position, final Identifier callee) {
    // func_call_part
    //   : LPAREN argument_list RPAREN
    //   ;

    lexer.assertPop(TokenKind.TK_LPAREN);

    final List<Expression> arguments = parseArgumentList(lexer);

    lexer.assertPop(TokenKind.TK_RPAREN);

    return new FunctionCall(position, callee, arguments);
  }

  private static final List<Expression> parseArgumentList(final Lexer lexer) {
    // argument_list
    //   : ( expr ( COMMA expr )* )?
    //   ;

    final List<Expression> arguments = new ArrayList<>();

    if (!lexer.peekIs(TokenKind.TK_RPAREN)) {
      // first argument
      {
        final Expression argument = parseExpression(lexer);
        arguments.add(argument);
      }

      while (lexer.peekIs(TokenKind.TK_COMMA)) {
        lexer.assertPop(TokenKind.TK_COMMA);

        final Expression argument = parseExpression(lexer);
        arguments.add(argument);
      }
    }

    return arguments;
  }

}
