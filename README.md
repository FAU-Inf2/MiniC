MiniC: A Toy SUT for Compiler Testing Techniques
================================================

This repository contains the implementation of an interpreter for the *MiniC* toy programming
language (see [below](#a-quick-tour-of-minic) for a brief introduction to *MiniC*). When running the
interpreter, one or more bugs can be injected into different parts of the interpreter pipeline. This
can be used to evaluate and test different compiler testing techniques (e.g., compiler fuzzers or
test case reducers).

## Building MiniC

Simply type `./gradlew build` in the root directory of the *MiniC* repository to build the *MiniC*
application. After a successful build, there should be a file `build/libs/MiniC.jar`. The
instructions below assume that this file exists.

**Note**:

- You need a working JDK installation to build and run *MiniC* (tested with OpenJDK 8 and 11).
- Building *MiniC* requires an internet connection to resolve external dependencies.

## A Quick Tour of MiniC

The *MiniC* toy programming language is inspired by a (small) subset of the C programming language.
Its features include function definitions, global and local variables (of type `int`), assignments,
`if` statements (with an optional `else` branch), `while` loops, and an implicitly defined `print()`
function for program output. For example, the following program computes and prints the 8th
Fibonacci number in two different ways:

```c
int fib_rec(int n, int a, int b) {
  if (n == 0) {
    return a;
  }
  if (n == 1) {
    return b;
  }
  return fib_rec(n - 1, b, a + b);
}

int fib_it(int n) {
  int a;
  int b;
  int i;

  a = 1;
  b = 1;
  i = 2;

  while (i < n) {
    int t;
    t = a + b;
    a = b;
    b = t;
    i = i + 1;
  }

  return b;
}

int main() {
  print(fib_rec(8, 0, 1));
  print(fib_it(8));
  return 0;
}
```

The file `grammar/minic.txt` contains the full context-free grammar for the *MiniC* programming
language (in the format used by the [j-PEG](https://github.com/FAU-Inf2/j-PEG) parser library).

### Expressions and Types

Expressions (e.g., on the right hand side of an assignment or in the condition of an `if` statement
or a `while` loop) may use the following binary operators (in order of increasing precedence):

| Precedence | Operators                        | Associativity | Operand Types  | Result Type |
| :---:      | :---                             | :---:         | :---:          | :---:       |
| 0          | `\|\|` (shortcut evaluation)     | left          | `bool`, `bool` | `bool`      |
| 1          | `&&` (shortcut evaluation)       | left          | `bool`, `bool` | `bool`      |
| 2          | `==`, `<`, `<=`, `>`, `>=`, `!=` | left          | `int`, `int`   | `bool`      |
| 3          | `+`, `-`                         | left          | `int`, `int`   | `int`       |
| 4          | `*`, `/`                         | left          | `int`, `int`   | `int`       |

Note that *MiniC* does **not** support a type conversion/coercion from `int` to `bool` or from
`bool` to `int`. Also note that all `int` values are defined to be 64 bit wide values in two's
complement (overflows lead to a wrap around).

### Undefined Behavior

Just like the C programming language, *MiniC* contains some undefined behavior:

- While all global variables are initialized with `0`, the initial value of local variables is
  undefined. Thus, reading a local variable before it has been defined results in an undefined
  value.
- Divisions by zero result in an undefined value.
- Non-`void` functions that terminate without reaching a `return` statement return an undefined
  value.

Note that the *MiniC* interpreter can check if it encounters undefined behavior during execution
(see [below](#running-the-minic-interpreter)).

## Running the MiniC Interpreter

Use the `run.sh` script in the root directory to run the *MiniC* interpreter. It supports the
following command line options:

- `--in <file name>` (mandatory): Specifies the path to the input program.
- `--lazyLexer`: By default, the lexer works in an eager fashion (i.e., the lexer first lexes the
  *complete* input program before the parser begins its work). If the `--lazyLexer` command line
  option is set, the lexer works in a lazy fashion instead (i.e., lexing and parsing are interwoven
  and the lexer only lexes parts of a program when required by the parser).
- `--prettyPrint <file name>`: If enabled, the parsed input program is pretty printed and the result
  is written to the specified file (or to stdout if `-` is given as file name).
- `--toDot <file name>`: If enabled, a Dot description for the input program's AST is generated and
  written to the specified file (or to stdout if `-` is given as file name).
- `--interpret`: If enabled, the input program is interpreted and the program's output and exit code
  are written to stdout; note that the interpreter runs indefinitely in case of infinite loops in
  the input program.
- `--checkUndef`: If enabled, the *MiniC* interpreter checks if it encounters
  [undefined behavior](#undefined-behavior) during execution that leads to an undefined control
  flow, program output or exit code; note that this requires a full interpreter run (which runs
  indefinitely in case of infinite loops in the input program).
- `--maxNumberOfSteps <number>`: Specifies the maximum number of steps that the interpreter should
  perform before aborting (unbounded by default).
- `--maxNumberOfLoopIterations <number>`: Specifies the maximum number of loop iterations that the
  interpreter should perform for each loop before aborting (unbounded by default).

Additionally, the *MiniC* interpreter supports several command line flags that inject certain bugs
into the interpreter pipeline (see [below](#injecting-bugs)).

If the input program is invalid (or if it is falsely rejected due to an injected bug), the *MiniC*
interpreter terminates with the following exit code:

- `130`, if the input program is *lexically* invalid.
- `131`, if the input program is *syntactically* invalid.
- `132`, if the input program is *semantically* invalid (i.e., if it violates the naming and/or
  typing rules).
- `133`, if the input program is *dynamically* invalid (i.e., if it contains undefined behavior;
  only applicable if the `--checkUndef` command line flag is enabled).
- `134`, if the interpreter reached the maximum allowed number of steps or loop iterations (only
  applicable if the `--maxNumberOfSteps` or `--maxNumberOfLoopIterations` command line option is
  set).

## Injecting Bugs

The *MiniC* implementation contains several bugs that can be enabled via the command line:

| Name                             | Category | Description |
| :---                             | :---:    | :---        |
| `missing_token_else`             | Lexer    | If enabled, the lexer still recognizes `else` tokens, but does not add them to the token stream. |
| `missing_token_while`            | Lexer    | If enabled, the lexer still recognizes `while` tokens, but does not add them to the token stream. |
| `wrong_token_if`                 | Lexer    | If enabled, the lexer falsely recognizes `if` tokens as identifiers. |
| `wrong_token_plus`               | Lexer    | If enabled, the lexer falsely recognizes `+` tokens as `*` tokens. |
| `no_equals_token`                | Lexer    | If enabled, the lexer does not recognize `==` tokens. |
| `wrong_regex_and`                | Lexer    | If enabled, the lexer tries to match `&\|` instead of `&&`. |
| `additional_skip`                | Lexer    | If enabled, the lexer falsely consumes one character too much when lexing numbers and identifiers.  |
| `missing_tree_else`              | Parser   | If enabled, the parser still parses `else` branches, but does not add them to the AST. |
| `missing_alternative_not_equals` | Parser   | If enabled, the parser does not accept the `!=` operator in comparisons. |
| `missing_alternative_call_stmt`  | Parser   | If enabled, the parser does not accept function call statements. |
| `additional_semicolon_return`    | Parser   | If enabled, the parser expects an additional semicolon at the end of `return` statements. |
| `missing_comma_arguments`        | Parser   | If enabled, the parser does not skip `,` tokens in argument lists correctly. |
| `swapped_operands_plus`          | Parser   | If enabled, the operands of `+` operations are swapped in the AST. |
| `right_associative_add_expr`     | Parser   | If enabled, the parser parses `+` and `-` operations as right-associative. |
| `missing_symbol_callee`          | Analysis | If enabled, the semantic analysis does not annotate the callees of function calls with the respective symbol (which the interpreter requires). |
| `missing_type_type_name`         | Analysis | If enabled, the semantic analysis does not annotate the AST nodes that represent type names (`int`, `void`) with the respective type (which the interpreter requires). |
| `missing_check_return_void`      | Analysis | If enabled, the semantic analysis does not enforce that no value is returned from `void` functions. |
| `missing_check_return_non_void`  | Analysis | If enabled, the semantic analysis does not enforce that a value is returned from non-`void` functions. |
| `wrong_order_symbol_table`       | Analysis | If enabled, the semantic analysis traverses the symbol table in the wrong order (and may therefore falsely choose a shadowed symbol). |
| `div_by_zero`                    | Interp.  | If enabled, the interpreter crashes when encountering a division by zero. |
| `no_shortcut_or`                 | Interp.  | If enabled, the interpreter does not perform a shortcut evaluation for `\|\|` operations. |
| `no_shortcut_and`                | Interp.  | If enabled, the interpreter does not perform a shortcut evaluation for `&&` operations. |
| `missing_init_globals`           | Interp.  | If enabled, the interpreter does not initialize the global variables. |
| `wrong_shift_mul`                | Interp.  | If enabled, the interpreter computes the wrong result for mutiplications where the right operand is a power of two. |

To enable one or more bugs, use the `--bugs` command line option and pass the names of the bugs as
comma-separated list (e.g., `--bugs 'swapped_operands_plus,no_shortcut_or'`).

Additionally, the *MiniC* interpreter supports the following command line flags to enable different
groups of bugs:

- `--allLexerBugs`: Enables all bugs in the lexer.
- `--allParserBugs`: Enables all bugs in the parser.
- `--allAnalysisBugs`: Enables all bugs in the semantic analysis.
- `--allInterpreterBugs`: Enables all bugs in the interpreter.
- `--allBugs`: Enables all bugs.

## Classification of Input Programs

The `classify.sh` helper script can be used to classify a number of programs (i.e., to count how
many of the given input programs are lexically/syntactically/semantically/dynamically invalid,
(apparently) non-terminating, and valid). It supports the following command line options:

- `--path <search directory>`: The directory that contains the programs that should be classified.
- `--pattern <file name pattern>`: Glob pattern that describes which programs should be considered
  for classification; the pattern may use a `*` as wildcard symbol (e.g., `'*.c'`).
- `--recursive`: If this command line option is set, the classification also considers programs that
  are contained in subdirectories of the given directory.
- `--lazyLexer`: By default, the lexer works in an eager fashion (i.e., the lexer first lexes the
  *complete* input program before the parser begins its work). If the `--lazyLexer` command line
  option is set, the lexer works in a lazy fashion instead (i.e., lexing and parsing are interwoven
  and the lexer only lexes parts of a program when required by the parser).
- `--maxNumberOfSteps <number>`: Specifies the maximum number of steps that the interpreter should
  perform for each program before it assumes that the program is non-terminating (unbounded by
  default).
- `--maxNumberOfLoopIterations <number>`: Specifies the maximum number of loop iterations that the
  interpreter should perform for each loop before it assumes that the program is non-terminating
  (unbounded by default).

For example, run the following to classify all programs in the `examples/` subdirectory:

    ./classify.sh --path examples/ --pattern '*.c' --maxNumberOfSteps 100000

## Test Scripts

Many compiler testing techniques (e.g., compiler fuzzers or test case reducers) require a test
oracle that tells whether a program triggers a bug in the implementation under test or not. The
`checks/` subdirectory contains some exemplary scripts that implement such oracles for different
kinds of bugs. These scripts work as follows:

- The scripts take the following command line arguments: `[MINIC_OPTIONS...] PROGRAM_PATH`:
  - The *last* argument (`PROGRAM_PATH`) is the path to the input program.
  - All other arguments (`MINIC_OPTIONS`) are passed to the "implementation under test" (e.g., to
    enable certain bugs). The "reference implementation" runs without these additional arguments.
- The scripts return with exit code `1` if the given program triggers the bug in the "implementation
  under test" (and `0` otherwise).

The following exemplary scripts are provided in the `checks/` subdirectory:

- `crash.sh`: Checks if the "implementation under test" crashes (i.e., if it terminates with exit
 code 1).
- `rejection.sh`: Checks if the "implementation under test" rejects the input program.
- `rejection_check.sh`: Checks if the "implementation under test" rejects the input program, but
 only if the "reference implementation" accepts the input program.
- `rejection_syntax.sh`: Checks if the lexer or parser of the "implementation under test" rejects
  the input program (i.e., if it considers the input program lexically or syntactically invalid).
- `wrong_output.sh`: Checks if the "implementation under test" produces a different program output
 or exit code than the "reference implementation".
- `wrong_output_check.sh`: Checks if the "implementation under test" produces a different program
  output or exit code than the "reference implementation", but only if the "reference
  implementation" determines that the input program is free of undefined behavior.

## Example: The StarSmith Compiler Fuzzer

To provide a working example for a compiler testing technique, the `starsmith/` subdirectory
contains everything that is required to automatically detect bugs in the *MiniC* implementation with
the language-agnostic [StarSmith](https://github.com/FAU-Inf2/StarSmith) compiler fuzzer:

- The file `specs/minic.ls` contains the *LaLa* specification that describes the syntactic and
  semantic rules of *MiniC*. StarSmith takes this specification as input and generates random
  programs that conform to all of these rules. Note that the specification rules out all undefined
  behavior by construction (thus, it is not necessary to check if the generated programs are free of
  undefined behavior afterwards).
- The `out/runtime/` subdirectory contains the runtime classes that the *LaLa* specification makes
  use of.

At first, the *LaLa* specification and the runtime classes have to be compiled:

    ./translate_specs.sh <path to StarSmith jar>

After this step, execute the following command to generate some random programs with StarSmith:

    ./out/run.sh <path to StarSmith jar> \
      --count <number of programs> \
      --out programs/prog_#{SEED}.c

This generates the specified number of programs and writes them to the `programs/` subdirectory.

To also check if the generated programs trigger a bug in the *MiniC* implementation, use the
`--findBugs` command line option. For example, run the following to check if the randomly generated
programs trigger the `no_shortcut_or` bug:

    ./out/run.sh <path to StarSmith jar> \
      --count <number of programs> \
      --out programs/prog_#{SEED}.c \
      --findBugs '../checks/wrong_output.sh --bugs no_shortcut_or'

(Note that, if the `--findBugs` option is set, StarSmith only keeps the programs that trigger a
bug.)

Please refer to the [StarSmith](https://github.com/FAU-Inf2/StarSmith) documentation for further
information.

## License

*MiniC* is licensed under the terms of the MIT license (see [LICENSE.mit](LICENSE.mit)).

*MiniC* makes use of the following open-source projects:

- Gradle (licensed under the terms of Apache License 2.0)
