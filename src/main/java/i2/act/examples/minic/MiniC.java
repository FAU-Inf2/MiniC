package i2.act.examples.minic;

import i2.act.examples.minic.bugs.Bug;
import i2.act.examples.minic.bugs.Bugs;
import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.ast.Program;
import i2.act.examples.minic.frontend.ast.visitors.DotGenerator;
import i2.act.examples.minic.frontend.ast.visitors.PrettyPrinter;
import i2.act.examples.minic.frontend.lexer.EagerTokenStream;
import i2.act.examples.minic.frontend.lexer.LazyTokenStream;
import i2.act.examples.minic.frontend.lexer.Lexer;
import i2.act.examples.minic.frontend.lexer.TokenStream;
import i2.act.examples.minic.frontend.parser.Parser;
import i2.act.examples.minic.frontend.semantics.SemanticAnalysis;
import i2.act.examples.minic.interpreter.Interpreter;
import i2.act.util.FileUtil;
import i2.act.util.Pair;
import i2.act.util.SafeWriter;
import i2.act.util.options.ProgramArguments;
import i2.act.util.options.ProgramArgumentsParser;

import java.util.List;

public final class MiniC {

  private static final ProgramArgumentsParser argumentsParser;

  private static final String OPTION_INPUT_FILE = "--in";

  private static final String OPTION_LAZY_LEXER = "--lazyLexer";

  private static final String OPTION_PRETTY_PRINT = "--prettyPrint";
  private static final String OPTION_TO_DOT = "--toDot";

  private static final String OPTION_CHECK_UNDEFINED = "--checkUndef";
  private static final String OPTION_INTERPRET = "--interpret";

  private static final String OPTION_MAX_NUMBER_OF_STEPS = "--maxNumberOfSteps";
  private static final String OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS = "--maxNumberOfLoopIterations";

  private static final String OPTION_BUGS = "--bugs";
  private static final String OPTION_ALL_LEXER_BUGS = "--allLexerBugs";
  private static final String OPTION_ALL_PARSER_BUGS = "--allParserBugs";
  private static final String OPTION_ALL_ANALYSIS_BUGS = "--allAnalysisBugs";
  private static final String OPTION_ALL_INTERPRETER_BUGS = "--allInterpreterBugs";
  private static final String OPTION_ALL_BUGS = "--allBugs";

  static {
    argumentsParser = new ProgramArgumentsParser();

    argumentsParser.addOption(OPTION_INPUT_FILE, true, true, "<path to input file>");

    argumentsParser.addOption(OPTION_LAZY_LEXER, false);

    argumentsParser.addOption(OPTION_PRETTY_PRINT, false, true, "<file name>");
    argumentsParser.addOption(OPTION_TO_DOT, false, true, "<file name>");

    argumentsParser.addOption(OPTION_CHECK_UNDEFINED, false);
    argumentsParser.addOption(OPTION_INTERPRET, false);

    argumentsParser.addOption(OPTION_MAX_NUMBER_OF_STEPS, false, true, "<number>");
    argumentsParser.addOption(OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS, false, true, "<number>");

    argumentsParser.addOption(OPTION_BUGS, false, true, "<list of bugs>");
    argumentsParser.addOption(OPTION_ALL_LEXER_BUGS, false);
    argumentsParser.addOption(OPTION_ALL_PARSER_BUGS, false);
    argumentsParser.addOption(OPTION_ALL_ANALYSIS_BUGS, false);
    argumentsParser.addOption(OPTION_ALL_INTERPRETER_BUGS, false);
    argumentsParser.addOption(OPTION_ALL_BUGS, false);
  }

  public static final void main(final String[] args) {
    ProgramArguments arguments = null;

    try {
      arguments = argumentsParser.parseArgs(args);
    } catch (final Exception exception) {
      abort(String.format("[!] %s", exception.getMessage()));
    }

    assert (arguments != null);

    final String inputFileName = arguments.getOption(OPTION_INPUT_FILE);
    final String input = FileUtil.readFile(inputFileName);

    enableBugs(arguments);

    if (Bugs.getInstance().numberOfBugs() > 0) {
      System.err.format("[i] enabled bugs: %s\n", Bugs.getInstance().toString());
    }

    final int maxNumberOfSteps =
        arguments.getIntOptionOr(OPTION_MAX_NUMBER_OF_STEPS, Interpreter.UNBOUNDED);
    final int maxNumberOfLoopIterations =
        arguments.getIntOptionOr(OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS, Interpreter.UNBOUNDED);

    try {
      final TokenStream tokenStream;
      {
        if (arguments.hasOption(OPTION_LAZY_LEXER)) {
          tokenStream = LazyTokenStream.from(new Lexer(input));
        } else {
          tokenStream = EagerTokenStream.from(new Lexer(input));
        }
      }

      final Program program = Parser.parse(tokenStream);

      if (arguments.hasOption(OPTION_PRETTY_PRINT)) {
        final String fileNamePrettyPrinted = arguments.getOption(OPTION_PRETTY_PRINT);

        final SafeWriter writer = SafeWriter.openFile(fileNamePrettyPrinted);
        PrettyPrinter.prettyPrint(program, writer);

        if ("-".equals(fileNamePrettyPrinted)) {
          writer.flush();
        } else {
          writer.close();
        }
      }

      if (arguments.hasOption(OPTION_TO_DOT)) {
        final String fileNameDot = arguments.getOption(OPTION_TO_DOT);

        final SafeWriter writer = SafeWriter.openFile(fileNameDot);
        DotGenerator.printDot(program, writer);

        if ("-".equals(fileNameDot)) {
          writer.flush();
        } else {
          writer.close();
        }
      }

      SemanticAnalysis.analyze(program);

      if (arguments.hasOption(OPTION_CHECK_UNDEFINED)) {
        Interpreter.checkDynamicallyValid(program, maxNumberOfSteps, maxNumberOfLoopIterations);
      }

      if (arguments.hasOption(OPTION_INTERPRET)) {
        final Pair<Interpreter.Value, List<Interpreter.Value>> result =
            Interpreter.interpret(program, maxNumberOfSteps, maxNumberOfLoopIterations);

        final Interpreter.Value exitValue = result.getFirst();
        final List<Interpreter.Value> output = result.getSecond();

        for (final Interpreter.Value value : output) {
          System.out.println(value);
        }

        System.out.println("EXIT: " + exitValue);
      }
    } catch (final InvalidProgramException exception) {
      System.err.format("%s: %s\n", exception.getKind(), exception.getMessage());
      System.exit(exception.getExitCode());
    }
  }

  private static final void usage() {
    System.err.format("USAGE: java %s\n", MiniC.class.getSimpleName());
    System.err.println(argumentsParser.usage("  "));
  }

  private static final void abort(final String message) {
    System.err.println(message);
    usage();
    System.exit(1);
  }

  private static final void enableBugs(final ProgramArguments arguments) {
    final Bugs bugs = Bugs.getInstance();

    final String bugNameList = arguments.getOptionOr(OPTION_BUGS, "");

    if (!bugNameList.trim().isEmpty()) {
      final String[] bugNames = bugNameList.split(",");

      for (final String bugName : bugNames) {
        final String trimmedName = bugName.trim();
        final Bug bug = Bug.fromName(trimmedName);

        if (bug == null) {
          abort(String.format("[!] invalid bug name: '%s'", trimmedName));
          assert (false);
        }

        bugs.enable(bug);
      }
    }

    if (arguments.hasOption(OPTION_ALL_LEXER_BUGS)) {
      bugs.enableAllOf(Bug.Category.LEXER);
    }

    if (arguments.hasOption(OPTION_ALL_PARSER_BUGS)) {
      bugs.enableAllOf(Bug.Category.PARSER);
    }

    if (arguments.hasOption(OPTION_ALL_ANALYSIS_BUGS)) {
      bugs.enableAllOf(Bug.Category.ANALYSIS);
    }

    if (arguments.hasOption(OPTION_ALL_INTERPRETER_BUGS)) {
      bugs.enableAllOf(Bug.Category.INTERPRETER);
    }

    if (arguments.hasOption(OPTION_ALL_BUGS)) {
      bugs.enableAll();
    }
  }

}
