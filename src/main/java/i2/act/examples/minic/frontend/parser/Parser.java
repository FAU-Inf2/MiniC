package i2.act.examples.minic.frontend.parser;

import i2.act.examples.minic.bugs.Bug;
import i2.act.examples.minic.bugs.Bugs;
import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class Parser {

  public static final Program parse(final String input) {
    return parse(LazyTokenStream.from(new Lexer(input)));
  }

  public static final Program parse(final TokenStream tokenStream) {
    return parseProgram(tokenStream);
  }

  private static final Program parseProgram(final TokenStream tokenStream) {
    // program
    //   : ( global_declaration )* EOF
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    final List<Declaration> declarations = new ArrayList<>();

    while (!tokenStream.peekIs(TokenKind.TK_EOF)) {
      final Declaration globalDeclaration = parseGlobaleDeclaration(tokenStream);
      declarations.add(globalDeclaration);
    }

    return new Program(position, declarations);
  }

  private static final Declaration parseGlobaleDeclaration(final TokenStream tokenStream) {
    // global_declaration
    //   : type_name IDENTIFIER SEMICOLON
    //   | type_name IDENTIFIER function_declaration_part
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    final TypeName typeName = parseTypeName(tokenStream);
    final Identifier name = parseIdentifier(tokenStream);

    tokenStream.assertPeek(TokenKind.TK_SEMICOLON, TokenKind.TK_LPAREN);

    if (tokenStream.peekIs(TokenKind.TK_SEMICOLON)) {
      // variable declaration
      tokenStream.assertPop(TokenKind.TK_SEMICOLON);

      return new VariableDeclaration(position, typeName, name);
    } else {
      // function declaration
      return parseFunctionDeclarationPart(tokenStream, position, typeName, name);
    }
  }

  private static final FunctionDeclaration parseFunctionDeclarationPart(
      final TokenStream tokenStream, final SourcePosition position, final TypeName typeName,
      final Identifier name) {
    // function_declaration_part
    //   : LPAREN parameter_declaration_list RPAREN block
    //   ;

    tokenStream.assertPop(TokenKind.TK_LPAREN);

    final List<VariableDeclaration> parameters = parseParameterDeclarationList(tokenStream);

    tokenStream.assertPop(TokenKind.TK_RPAREN);

    final Block body = parseBlock(tokenStream);

    return new FunctionDeclaration(position, typeName, name, parameters, body);
  }

  private static final List<VariableDeclaration> parseParameterDeclarationList(
      final TokenStream tokenStream) {
    // parameter_declaration_list
    //   : ( parameter_declaration ( COMMA parameter_declaration )* )?
    //   ;

    final List<VariableDeclaration> parameters = new ArrayList<>();

    if (!tokenStream.peekIs(TokenKind.TK_RPAREN)) {
      // first parameter
      {
        final VariableDeclaration parameterDeclaration = parseParameterDeclaration(tokenStream);
        parameters.add(parameterDeclaration);
      }

      while (tokenStream.peekIs(TokenKind.TK_COMMA)) {
        tokenStream.assertPop(TokenKind.TK_COMMA);

        final VariableDeclaration parameterDeclaration = parseParameterDeclaration(tokenStream);
        parameters.add(parameterDeclaration);
      }
    }

    return parameters;
  }

  private static final VariableDeclaration parseParameterDeclaration(
      final TokenStream tokenStream) {
    // parameter_declaration
    //   : type_name IDENTIFIER
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    final TypeName typeName = parseTypeName(tokenStream);
    final Identifier name = parseIdentifier(tokenStream);

    return new VariableDeclaration(position, typeName, name);
  }

  private static final VariableDeclaration parseVariableDeclaration(final TokenStream tokenStream) {
    // variable_declaration
    //   : type_name IDENTIFIER SEMICOLON
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    final TypeName typeName = parseTypeName(tokenStream);
    final Identifier name = parseIdentifier(tokenStream);

    tokenStream.assertPop(TokenKind.TK_SEMICOLON);

    return new VariableDeclaration(position, typeName, name);
  }

  private static final TypeName parseTypeName(final TokenStream tokenStream) {
    // type_name
    //   : INT
    //   | VOID
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    tokenStream.assertPeek(TokenKind.TK_INT, TokenKind.TK_VOID);
    final Token typeName = tokenStream.pop();

    return new TypeName(position, typeName);
  }

  private static final Identifier parseIdentifier(final TokenStream tokenStream) {
    final SourcePosition position = tokenStream.getPosition();

    final Token identifier = tokenStream.assertPop(TokenKind.TK_IDENTIFIER);

    return new Identifier(position, identifier);
  }

  private static final Block parseBlock(final TokenStream tokenStream) {
    // block
    //   : LBRACE ( statement )* RBRACE
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    tokenStream.assertPop(TokenKind.TK_LBRACE);

    final List<Statement> statements = new ArrayList<>();

    while (!tokenStream.peekIs(TokenKind.TK_RBRACE)) {
      final Statement statement = parseStatement(tokenStream);
      statements.add(statement);
    }

    tokenStream.assertPop(TokenKind.TK_RBRACE);

    return new Block(position, statements);
  }

  private static final Statement parseStatement(final TokenStream tokenStream) {
    // statement
    //   : IDENTIFIER ASSIGN expression SEMICOLON
    //   | IDENTIFIER function_call_part SEMICOLON
    //   | IF LPAREN expression RPAREN block ( ELSE block )?
    //   | WHILE LPAREN expression RPAREN block
    //   | RETURN ( expression )? SEMICOLON
    //   | variable_declaration
    //   | block
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    if (tokenStream.peekIs(TokenKind.TK_IDENTIFIER)) {
      final Identifier identifier = parseIdentifier(tokenStream);

      // check for injected bug
      final boolean missingAlternativeCallStatement;
      {
        missingAlternativeCallStatement =
            Bugs.getInstance().isEnabled(Bug.MISSING_ALTERNATIVE_CALL_STMT);
      }

      if (tokenStream.peekIs(TokenKind.TK_ASSIGN) || missingAlternativeCallStatement) {
        tokenStream.assertPop(TokenKind.TK_ASSIGN);

        final Expression rightHandSide = parseExpression(tokenStream);

        tokenStream.assertPop(TokenKind.TK_SEMICOLON);

        return new AssignStatement(position, identifier, rightHandSide);
      } else {
        final FunctionCall functionCall = parseFunctionCallPart(tokenStream, position, identifier);

        tokenStream.assertPop(TokenKind.TK_SEMICOLON);

        return new FunctionCallStatement(position, functionCall);
      }
    } else if (tokenStream.peekIs(TokenKind.TK_IF)) {
      tokenStream.assertPop(TokenKind.TK_IF);
      tokenStream.assertPop(TokenKind.TK_LPAREN);

      final Expression condition = parseExpression(tokenStream);

      tokenStream.assertPop(TokenKind.TK_RPAREN);

      final Block thenBlock = parseBlock(tokenStream);

      if (tokenStream.peekIs(TokenKind.TK_ELSE)) {
        tokenStream.assertPop(TokenKind.TK_ELSE);

        final Block elseBlock = parseBlock(tokenStream);

        // check for injected bug
        {
          if (Bugs.getInstance().isEnabled(Bug.MISSING_TREE_ELSE)) {
            return new IfStatement(position, condition, thenBlock);
          }
        }

        return new IfStatement(position, condition, thenBlock, elseBlock);
      } else {
        return new IfStatement(position, condition, thenBlock);
      }
    } else if (tokenStream.peekIs(TokenKind.TK_WHILE)) {
      tokenStream.assertPop(TokenKind.TK_WHILE);
      tokenStream.assertPop(TokenKind.TK_LPAREN);

      final Expression condition = parseExpression(tokenStream);

      tokenStream.assertPop(TokenKind.TK_RPAREN);

      final Block body = parseBlock(tokenStream);

      return new WhileLoop(position, condition, body);
    } else if (tokenStream.peekIs(TokenKind.TK_RETURN)) {
      tokenStream.assertPop(TokenKind.TK_RETURN);

      if (tokenStream.peekIs(TokenKind.TK_SEMICOLON)) {
        tokenStream.assertPop(TokenKind.TK_SEMICOLON);

        return new ReturnStatement(position);
      } else {
        final Expression returnValue = parseExpression(tokenStream);

        tokenStream.assertPop(TokenKind.TK_SEMICOLON);

        // check for injected bug
        {
          if (Bugs.getInstance().isEnabled(Bug.ADDITIONAL_SEMICOLON_RETURN)) {
            tokenStream.assertPop(TokenKind.TK_SEMICOLON);
          }
        }

        return new ReturnStatement(position, returnValue);
      }
    } else if (tokenStream.peekIs(TokenKind.TK_LBRACE)) {
      final Block block = parseBlock(tokenStream);
      return block;
    } else {
      final VariableDeclaration variableDeclaration = parseVariableDeclaration(tokenStream);
      return new DeclarationStatement(position, variableDeclaration);
    }
  }

  private static final Expression parseExpression(final TokenStream tokenStream) {
    // expression
    //   : or_expression
    //   ;

    return parseOrExpression(tokenStream);
  }

  private static final Expression parseBinaryExpression(final TokenStream tokenStream,
      final Function<TokenStream, Expression> operandParser, final TokenKind... operators) {
    // <binary_expr>
    //   : <operand> ( <OPERATOR> <operand> )*
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    Expression expression = operandParser.apply(tokenStream);

    while (tokenStream.peekIs(operators)) {
      final BinaryExpression.Operator operator = tokenStream.pop().kind.operator;
      assert (operator != null);

      final Expression otherExpression = operandParser.apply(tokenStream);

      // check for injected bug
      if (Bugs.getInstance().isEnabled(Bug.SWAPPED_OPERANDS_PLUS)
          && operator == BinaryExpression.Operator.ADD) {
        expression = new BinaryExpression(position, operator, otherExpression, expression);
      } else {
        expression = new BinaryExpression(position, operator, expression, otherExpression);
      }
    }

    return expression;
  }

  private static final Expression parseBinaryExpressionRightAssociative(
      final TokenStream tokenStream, final Function<TokenStream, Expression> operandParser,
      final TokenKind... operators) {
    // <binary_expr>
    //   : <operand> ( <OPERATOR> <operand> )*
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    Expression expression = operandParser.apply(tokenStream);

    if (tokenStream.peekIs(operators)) {
      final BinaryExpression.Operator operator = tokenStream.pop().kind.operator;
      assert (operator != null);

      final Expression otherExpression =
          parseBinaryExpressionRightAssociative(tokenStream, operandParser, operators);

      // check for injected bug
      if (Bugs.getInstance().isEnabled(Bug.SWAPPED_OPERANDS_PLUS)
          && operator == BinaryExpression.Operator.ADD) {
        expression = new BinaryExpression(position, operator, otherExpression, expression);
      } else {
        expression = new BinaryExpression(position, operator, expression, otherExpression);
      }
    }

    return expression;
  }

  private static final Expression parseOrExpression(final TokenStream tokenStream) {
    // or_expression
    //   : and_expression ( OR_OP and_expression )*
    //   ;

    return parseBinaryExpression(
        tokenStream,
        (_tokenStream) -> parseAndExpression(_tokenStream),
        TokenKind.TK_OR_OP);
  }

  private static final Expression parseAndExpression(final TokenStream tokenStream) {
    // and_expression
    //   : compare_expression ( AND_OP compare_expression )*
    //   ;

    return parseBinaryExpression(
        tokenStream,
        (_tokenStream) -> parseCompareExpression(_tokenStream),
        TokenKind.TK_AND_OP);
  }

  private static final Expression parseCompareExpression(final TokenStream tokenStream) {
    // compare_expression
    //   : add_expression ( compare_operator add_expression )*
    //   ;
    // compare_operator
    //   : EQUALS | LESS_THAN | GREATER_THAN | LESS_EQUALS | GREATER_EQUALS | NOT_EQUALS
    //   ;

    // check for injected bug
    {
      if (Bugs.getInstance().isEnabled(Bug.MISSING_ALTERNATIVE_NOT_EQUALS)) {
        return parseBinaryExpression(
            tokenStream,
            (_tokenStream) -> parseAddExpression(_tokenStream),
            TokenKind.TK_EQUALS, TokenKind.TK_LESS_THAN, TokenKind.TK_GREATER_THAN,
            TokenKind.TK_LESS_EQUALS, TokenKind.TK_GREATER_EQUALS);
      }
    }

    return parseBinaryExpression(
        tokenStream,
        (_tokenStream) -> parseAddExpression(_tokenStream),
        TokenKind.TK_EQUALS, TokenKind.TK_LESS_THAN, TokenKind.TK_GREATER_THAN,
        TokenKind.TK_LESS_EQUALS, TokenKind.TK_GREATER_EQUALS, TokenKind.TK_NOT_EQUALS);
  }

  private static final Expression parseAddExpression(final TokenStream tokenStream) {
    // add_expression
    //   : mul_expression ( add_operator mul_expression )*
    //   ;
    // add_operator
    //   : ADD | SUB
    //   ;

    // check for injected bug
    if (Bugs.getInstance().isEnabled(Bug.RIGHT_ASSOCIATIVE_ADD_EXPR)) {
      return parseBinaryExpressionRightAssociative(
          tokenStream,
          (_tokenStream) -> parseMulExpression(_tokenStream),
          TokenKind.TK_ADD, TokenKind.TK_SUB);
    } else {
      return parseBinaryExpression(
          tokenStream,
          (_tokenStream) -> parseMulExpression(_tokenStream),
          TokenKind.TK_ADD, TokenKind.TK_SUB);
    }
  }

  private static final Expression parseMulExpression(final TokenStream tokenStream) {
    // mul_expression
    //   : factor ( mul_operator factor )*
    //   ;
    // mul_operator
    //   : MUL | DIV
    //   ;

    return parseBinaryExpression(
        tokenStream,
        (_tokenStream) -> parseFactor(_tokenStream),
        TokenKind.TK_MUL, TokenKind.TK_DIV);
  }

  private static final Expression parseFactor(final TokenStream tokenStream) {
    // factor
    //   : IDENTIFIER function_call_part
    //   | IDENTIFIER
    //   | NUM
    //   | LPAREN expression RPAREN
    //   ;

    final SourcePosition position = tokenStream.getPosition();

    tokenStream.assertPeek(TokenKind.TK_IDENTIFIER, TokenKind.TK_NUM, TokenKind.TK_LPAREN);

    if (tokenStream.peekIs(TokenKind.TK_NUM)) {
      final Token token = tokenStream.assertPop(TokenKind.TK_NUM);

      return new Literal(position, token);
    } else if (tokenStream.peekIs(TokenKind.TK_LPAREN)) {
      tokenStream.assertPop(TokenKind.TK_LPAREN);

      final Expression expression = parseExpression(tokenStream);

      tokenStream.assertPop(TokenKind.TK_RPAREN);

      return expression;
    } else {
      final Identifier name = parseIdentifier(tokenStream);

      if (tokenStream.peekIs(TokenKind.TK_LPAREN)) {
        return parseFunctionCallPart(tokenStream, position, name);
      } else {
        return name;
      }
    }
  }

  private static final FunctionCall parseFunctionCallPart(final TokenStream tokenStream,
      final SourcePosition position, final Identifier callee) {
    // function_call_part
    //   : LPAREN argument_list RPAREN
    //   ;

    tokenStream.assertPop(TokenKind.TK_LPAREN);

    final List<Expression> arguments = parseArgumentList(tokenStream);

    tokenStream.assertPop(TokenKind.TK_RPAREN);

    return new FunctionCall(position, callee, arguments);
  }

  private static final List<Expression> parseArgumentList(final TokenStream tokenStream) {
    // argument_list
    //   : ( expression ( COMMA expression )* )?
    //   ;

    final List<Expression> arguments = new ArrayList<>();

    if (!tokenStream.peekIs(TokenKind.TK_RPAREN)) {
      // first argument
      {
        final Expression argument = parseExpression(tokenStream);
        arguments.add(argument);
      }

      while (tokenStream.peekIs(TokenKind.TK_COMMA)) {
        // check for injected bug
        if (!Bugs.getInstance().isEnabled(Bug.MISSING_COMMA_ARGUMENTS)) {
          tokenStream.assertPop(TokenKind.TK_COMMA);
        }

        final Expression argument = parseExpression(tokenStream);
        arguments.add(argument);
      }
    }

    return arguments;
  }

}
