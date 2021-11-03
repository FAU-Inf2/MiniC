use Symbol;
use SymbolTable;
use Type;

class Program {

  program ("
      ${decls : GlobalDeclarationList}\n\n
      ${main : MainDeclaration}") {
  }

}

@list(10)
class GlobalDeclarationList {

  one_decl ("${decl : GlobalDeclaration}") { }

  @weight(5)
  mult_decl ("${decl : GlobalDeclaration}\n\n${rest : GlobalDeclarationList}") { }

}

class GlobalDeclaration {

  var_decl ("${decl : VariableDeclaration};") { }

  func_decl ("${decl : FunctionDeclaration}") { }

}

class VariableDeclaration {

  var_decl ("${type : VariableType} ${name : DefIdentifier}") { }

}

class VariableType {

  int ("int") { }

}

class FunctionDeclaration {

  func_decl ("
      ${return_type : ReturnType} ${name : DefIdentifier}
      (${params : OptionalParameterList}) ${body : FunctionBody}") { }

}

class OptionalParameterList {

  no_params ("") { }

  @weight(2)
  params ("${params : ParameterList}") { }

}

@list(5)
class ParameterList {

  one_param ("${param : VariableDeclaration}") { }

  @weight(2)
  mult_param ("${param : VariableDeclaration}, ${rest : ParameterList}") { }

}

class ReturnType {

  int ("int") { }

  void ("void") { }

}

class FunctionBody {

  func_body ("{\+${stmts : StatementList}\n${return : ReturnStatement}\-}") { }

}

@list(10)
class StatementList {

  one_stmt ("${stmt : Statement}") { }

  @weight(4)
  mult_stmt ("${stmt : Statement}\n${rest : StatementList}") { }

}

@unit
class Statement {

  @weight(16)
  assign ("${lhs : UseIdentifier} = ${rhs : Expression};") { }

  @weight(2)
  func_call ("${call : FunctionCall};") { }

  @weight(4)
  if ("if (${condition : Expression}) ${then : Block}") { }

  @weight(4)
  if_else ("if (${condition : Expression}) ${then : Block} else ${else : Block}") { }

  @weight(4)
  while ("while (${condition : Expression}) ${body : Block}") { }

  return ("${return : ReturnStatement}") { }

  @weight(8)
  var_decl ("${decl : VariableDeclaration};") { }

  @weight(2)
  block ("${block : Block}") { }

}

class Block {

  block ("{\+${stmts : StatementList}\-}") { }

}

class ReturnStatement {

  return ("return;") { }

  return_val ("return ${value : Expression};") { }

}

class Expression {

  or ("${lhs : Expression} || ${rhs : AndExpression}") { }

  @weight(5)
  no_or ("${expr : AndExpression}") { }

}

class AndExpression {

  and ("${lhs : AndExpression} && ${rhs : CompareExpression}") { }

  @weight(5)
  no_and ("${expr : CompareExpression}") { }

}

class CompareExpression {

  compare ("${lhs : AddExpression} ${op : CompareOp} ${rhs : AddExpression}") { }

  @weight(5)
  no_compare ("${expr : AddExpression}") { }

}

class CompareOp("==|<|<=|>|>=|!=");

class AddExpression {

  add ("${lhs : AddExpression} ${op : AddOp} ${rhs : MulExpression}") { }

  @weight(5)
  no_add ("${expr : MulExpression}") { }

}

class AddOp("+|-");

class MulExpression {

  mul ("${lhs : MulExpression} ${op : MulOp} ${rhs : Factor}") { }

  @weight(5)
  no_mul ("${expr : Factor}") { }

}

class MulOp("*|/");

class Factor {

  call ("${call : FunctionCall}") { }

  num ("${num : Number}") { }

  var ("${var : UseIdentifier}") { }

  parens ("(${expr : Expression})") { }

}

@count(1000)
class Number("0|[1-9][0-9]{0,5}");

class FunctionCall {

  call ("${callee : UseIdentifier}(${args : OptionalArgumentList})") { }

}

class OptionalArgumentList {

  no_args ("") { }

  args ("${args : ArgumentList}") { }

}

@list
class ArgumentList {

  one_arg ("${arg : Expression}") { }

  mult_arg ("${arg : Expression}, ${rest : ArgumentList}") { }

}

class MainDeclaration {

  main ("int main() ${body : FunctionBody}") { }

}

@count(1000)
class Identifier("[a-z][a-zA-Z0-9_]{2,5}");

class DefIdentifier {

  def_id ("${id : Identifier}") { }

}

class UseIdentifier {

  def_id ("${id : Identifier}") { }

}
