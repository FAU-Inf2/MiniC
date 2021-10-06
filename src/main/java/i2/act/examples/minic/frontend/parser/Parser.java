package i2.act.examples.minic.frontend.parser;

import i2.act.examples.minic.frontend.ast.*;
import i2.act.examples.minic.frontend.info.SourcePosition;
import i2.act.examples.minic.frontend.lexer.*;

import java.util.ArrayList;
import java.util.List;

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

    while (!lexer.peekIs(TokenKind.TK_RPAREN)) {
      final VariableDeclaration parameterDeclaration = parseParameterDeclaration(lexer);
      parameters.add(parameterDeclaration);
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

    return null; // TODO
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

}
