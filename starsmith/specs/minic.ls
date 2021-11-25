use Symbol;
use SymbolTable;
use Type;
use Util;

class Program {

  program ("
      #{(Util:helperFunctions)}\n\n
      ${decls : OptionalGlobalDeclarationList}
      ${main : MainDeclaration}") {
    loc div_symbol =
        (Symbol:create "div" (Type:functionType (Type:intType) (Type:intType) (Type:intType)) this);
    loc print_symbol =
        (Symbol:create "print" (Type:functionType (Type:voidType) (Type:intType)) this);

    decls.symbols_before = (SymbolTable:predefined .div_symbol .print_symbol);
    main.symbols_before = decls.symbols_after;
  }

}

@copy
class OptionalGlobalDeclarationList {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  no_decls ("") {
    this.symbols_after = this.symbols_before;
  }

  @weight(8)
  decls ("${decls : GlobalDeclarationList}\n\n") {
    # intentionally left blank
  }

}

@list(10)
@copy
class GlobalDeclarationList {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  one_decl ("${decl : GlobalDeclaration}") {
    # intentionally left blank
  }

  @weight(5)
  mult_decl ("${decl : GlobalDeclaration}\n\n${rest : GlobalDeclarationList}") {
    rest.symbols_before = decl.symbols_after;
  }

}

@copy
class GlobalDeclaration {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  var_decl ("${decl : VariableDeclaration};") {
    this.symbols_after = (SymbolTable:setDefined decl.symbols_after decl.symbol);
  }

  func_decl ("${decl : FunctionDeclaration}") {
    # intentionally left blank
  }

}

@copy
class VariableDeclaration {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;
  syn symbol : Symbol;

  var_decl ("${type : VariableType} ${name : DefIdentifier}") {
    loc var_symbol = (Symbol:create name.name type.type this);
    this.symbol = .var_symbol;
    this.symbols_after = (SymbolTable:declare this.symbols_before .var_symbol);
  }

}

class VariableType {

  syn type : Type;

  int ("int") {
    this.type = (Type:intType);
  }

}

class FunctionDeclaration {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  func_decl ("
      ${return_type : ReturnType} ${name : DefIdentifier}
      (${params : OptionalParameterList}) {\+
        ${stmts : StatementList}\n
        ${return : ReturnStatement}\-
      }") {
    loc function_symbol = (Symbol:create name.name params.function_type_after this);

    name.symbols_before = this.symbols_before;

    params.function_type_before = (Type:functionType return_type.type);
    params.symbols_before = (SymbolTable:enterScope this.symbols_before);

    # TODO should we allow recursive calls?
    stmts.symbols_before = (SymbolTable:enterScope params.symbols_after);
    stmts.return_type = return_type.type;

    return.symbols_before = stmts.symbols_after;
    return.return_type = return_type.type;

    this.symbols_after = (SymbolTable:define this.symbols_before .function_symbol);
  }

}

@copy
class OptionalParameterList {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  inh function_type_before : Type;
  syn function_type_after : Type;

  no_params ("") {
    this.symbols_after = this.symbols_before;
    this.function_type_after = this.function_type_before;
  }

  @weight(2)
  params ("${params : ParameterList}") {
    # intentionally left blank
  }

}

@list(5)
@copy
class ParameterList {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  inh function_type_before : Type;
  syn function_type_after : Type;

  one_param ("${param : VariableDeclaration}") {
    this.symbols_after = (SymbolTable:setDefined param.symbols_after param.symbol);
    this.function_type_after =
        (Type:addParameterType this.function_type_before (Symbol:typeOf param.symbol));
  }

  @weight(2)
  mult_param ("${param : VariableDeclaration}, ${rest : ParameterList}") {
    rest.symbols_before = (SymbolTable:setDefined param.symbols_after param.symbol);
    rest.function_type_before =
        (Type:addParameterType this.function_type_before (Symbol:typeOf param.symbol));
  }

}

class ReturnType {

  syn type : Type;

  int ("int") {
    this.type = (Type:intType);
  }

  void ("void") {
    this.type = (Type:voidType);
  }

}

@list(10)
@copy
class StatementList {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  inh return_type : Type;

  one_stmt ("${stmt : Statement}") {
    # intentionally left blank
  }

  @weight(4)
  mult_stmt ("${stmt : Statement}\n${rest : StatementList}") {
    rest.symbols_before = stmt.symbols_after;
  }

}

@unit
@copy
class Statement {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  inh return_type : Type;

  @weight(100)
  assign ("${lhs : UseIdentifier} = ${rhs : Expression};") {
    lhs.expected_type = (Type:intType);
    lhs.only_defined = false;

    rhs.expected_type = (Symbol:typeOf lhs.symbol);

    this.symbols_after = (SymbolTable:setDefined this.symbols_before lhs.symbol);
  }

  @weight(12)
  func_call ("${call : FunctionCall};") {
    call.expected_return_type = (Type:anyPrimitiveType);
    this.symbols_after = this.symbols_before;
  }

  @weight(20)
  if ("if (${condition : Expression}) ${then : Block}") {
    condition.expected_type = (Type:boolType);
    then.is_conditional_block = true;
    this.symbols_after = this.symbols_before;
  }

  @weight(20)
  if_else ("if (${condition : Expression}) ${then : Block} else ${else : Block}") {
    condition.expected_type = (Type:boolType);
    then.is_conditional_block = true;
    else.is_conditional_block = true;
    this.symbols_after =
        (SymbolTable:leaveScope (SymbolTable:intersect then.symbols_at_end else.symbols_at_end));
  }

  @weight(3)
  while ("while (${condition : Expression}) ${body : Block}") {
    condition.expected_type = (Type:boolType);
    body.is_conditional_block = false;
    this.symbols_after = this.symbols_before;
  }

  @weight(18)
  var_decl ("${decl : VariableDeclaration};") {
    # intentionally left blank
  }

  @weight(6)
  block ("${block : Block}") {
    block.is_conditional_block = false;
    this.symbols_after = (SymbolTable:leaveScope block.symbols_at_end);
  }

}

@copy
class Block {

  inh symbols_before : SymbolTable;
  syn symbols_at_end : SymbolTable;

  inh return_type : Type;

  inh is_conditional_block : boolean;

  block ("{\+${stmts : StatementList}${return : OptionalReturnStatement}\-}") {
    stmts.symbols_before = (SymbolTable:enterScope this.symbols_before);
    return.return_allowed = this.is_conditional_block;
    this.symbols_at_end = stmts.symbols_after;
  }

}

@copy
class OptionalReturnStatement {

  inh symbols_before : SymbolTable;
  inh return_type : Type;
  inh return_allowed : boolean;

  grd valid;

  @weight(3)
  no_return ("") {
    this.valid = true;
  }

  return ("\n${return : ReturnStatement}") {
    this.valid = this.return_allowed;
  }

}

@copy
class ReturnStatement {

  inh symbols_before : SymbolTable;

  inh return_type : Type;

  grd valid;

  return ("return;") {
    this.valid = (Type:isVoid this.return_type);
  }

  return_val ("return ${value : Expression};") {
    this.valid = (not (Type:isVoid this.return_type));
    value.expected_type = this.return_type;
  }

}

@copy
class Expression {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  or ("${lhs : Expression} || ${rhs : AndExpression}") {
    this.types_match = (Type:assignable (Type:boolType) this.expected_type);
  }

  @weight(2)
  no_or ("${expr : AndExpression}") {
    this.types_match = true;
  }

}

@copy
class AndExpression {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  and ("${lhs : AndExpression} && ${rhs : CompareExpression}") {
    this.types_match = (Type:assignable (Type:boolType) this.expected_type);
  }

  @weight(2)
  no_and ("${expr : CompareExpression}") {
    this.types_match = true;
  }

}

@copy
class CompareExpression {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  compare ("${lhs : AddExpression} ${op : CompareOp} ${rhs : AddExpression}") {
    this.types_match = (Type:assignable (Type:boolType) this.expected_type);
    lhs.expected_type = (Type:intType);
    rhs.expected_type = (Type:intType);
  }

  @weight(5)
  no_compare ("${expr : AddExpression}") {
    this.types_match = true;
  }

}

class CompareOp("==|<|<=|>|>=|!=");

@copy
class AddExpression {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  add ("${lhs : AddExpression} ${op : AddOp} ${rhs : MulExpression}") {
    this.types_match = (Type:assignable (Type:intType) this.expected_type);
  }

  @weight(5)
  no_add ("${expr : MulExpression}") {
    this.types_match = true;
  }

}

class AddOp("+|-");

@copy
class MulExpression {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  mul ("${lhs : MulExpression} ${op : MulOp} ${rhs : Factor}") {
    this.types_match = (Type:assignable (Type:intType) this.expected_type);
  }

  @weight(5)
  no_mul ("${expr : Factor}") {
    this.types_match = true;
  }

}

# divisions require a helper function to rule out the undefined behavior of divisions by zero
class MulOp("*");

@copy
class Factor {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;

  grd types_match;

  @weight(6)
  call ("${call : FunctionCall}") {
    this.types_match = true;
    call.expected_return_type = this.expected_type;
  }

  @weight(4)
  num ("${num : Number}") {
    this.types_match = (Type:assignable (Type:intType) this.expected_type);
  }

  @weight(12)
  var ("${var : UseIdentifier}") {
    this.types_match = true;
    var.only_defined = true;
  }

  @weight(4)
  parens ("(${expr : Expression})") {
    this.types_match = true;
  }

}

@count(1000)
class Number("0|[1-9][0-9]{0,5}");

@copy
class FunctionCall {

  inh symbols_before : SymbolTable;
  inh expected_return_type : Type;

  call ("${callee : UseIdentifier}(${args : OptionalArgumentList})") {
    callee.expected_type = (Type:anyFunction this.expected_return_type);
    callee.only_defined = false; # functions are always defined
    args.function_type = (Symbol:typeOf callee.symbol);
  }

}

@copy
class OptionalArgumentList {

  inh symbols_before : SymbolTable;

  inh function_type : Type;

  grd valid;

  no_args ("") {
    this.valid = (== (Type:numberOfParameters this.function_type) 0);
  }

  args ("${args : ArgumentList}") {
    this.valid = (> (Type:numberOfParameters this.function_type) 0);
    args.index = 0;
  }

}

@list
@copy
class ArgumentList {

  inh symbols_before : SymbolTable;

  inh function_type : Type;
  inh index : int;

  grd valid;

  one_arg ("${arg : Expression}") {
    this.valid = (== this.index (- (Type:numberOfParameters this.function_type) 1));
    arg.expected_type = (Type:parameterType this.function_type this.index);
  }

  mult_arg ("${arg : Expression}, ${rest : ArgumentList}") {
    this.valid = (< this.index (- (Type:numberOfParameters this.function_type) 1));
    arg.expected_type = (Type:parameterType this.function_type this.index);
    rest.index = (+ this.index 1);
  }

}

class MainDeclaration {

  inh symbols_before : SymbolTable;
  syn symbols_after : SymbolTable;

  main ("
      int main() {\+
        ${stmts : StatementList}\n
        #{(SymbolTable:printDefined stmts.symbols_after)}
        ${return : ReturnStatement}\-
      }") {
    loc function_symbol = (Symbol:create "main" (Type:functionType (Type:intType)) this);

    # TODO should we allow recursive calls?
    stmts.symbols_before = (SymbolTable:enterScope this.symbols_before);
    stmts.return_type = (Type:intType);

    return.symbols_before = stmts.symbols_after;
    return.return_type = (Type:intType);

    this.symbols_after = (SymbolTable:define this.symbols_before .function_symbol);
  }

}

@count(1000)
class Identifier("[a-z][a-zA-Z0-9_]{2,5}");

class DefIdentifier {

  inh symbols_before : SymbolTable;
  syn name : String;

  grd can_declare;

  def_id ("${id : Identifier}") {
    this.can_declare = (SymbolTable:canDeclare this.symbols_before id.str);
    this.name = id.str;
  }

}

class UseIdentifier {

  inh symbols_before : SymbolTable;
  inh expected_type : Type;
  inh only_defined : boolean;

  syn symbol : Symbol;

  def_id (SymbolTable:visible this.symbols_before this.expected_type this.only_defined) : Symbol {
    this.symbol = $;
  }

}
