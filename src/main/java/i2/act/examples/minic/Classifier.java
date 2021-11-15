package i2.act.examples.minic;

import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.ast.Program;
import i2.act.examples.minic.frontend.lexer.EagerTokenStream;
import i2.act.examples.minic.frontend.lexer.LazyTokenStream;
import i2.act.examples.minic.frontend.lexer.Lexer;
import i2.act.examples.minic.frontend.lexer.TokenStream;
import i2.act.examples.minic.frontend.parser.Parser;
import i2.act.examples.minic.frontend.semantics.SemanticAnalysis;
import i2.act.examples.minic.interpreter.Interpreter;
import i2.act.util.FileUtil;
import i2.act.util.options.ProgramArguments;
import i2.act.util.options.ProgramArgumentsParser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class Classifier {

  private static final ProgramArgumentsParser argumentsParser;

  private static final String OPTION_PATH = "--path";
  private static final String OPTION_PATTERN = "--pattern";
  private static final String OPTION_RECURSIVE = "--recursive";

  private static final String OPTION_LAZY_LEXER = "--lazyLexer";

  private static final String OPTION_MAX_NUMBER_OF_STEPS = "--maxNumberOfSteps";
  private static final String OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS = "--maxNumberOfLoopIterations";

  static {
    argumentsParser = new ProgramArgumentsParser();

    argumentsParser.addOption(OPTION_PATH, true, true, "<search directory>");
    argumentsParser.addOption(OPTION_PATTERN, true, true, "<file name pattern>");
    argumentsParser.addOption(OPTION_RECURSIVE, false);

    argumentsParser.addOption(OPTION_LAZY_LEXER, false);

    argumentsParser.addOption(OPTION_MAX_NUMBER_OF_STEPS, false, true, "<number>");
    argumentsParser.addOption(OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS, false, true, "<number>");
  }

  private static enum ClassificationResult {
    LEXICALLY_INVALID,
    SYNTACTICALLY_INVALID,
    SEMANTICALLY_INVALID,
    DYNAMICALLY_INVALID,
    NON_TERMINATING,
    VALID,
  }

  private static final void usage() {
    System.err.format("USAGE: java %s\n", Classifier.class.getSimpleName());
    System.err.println(argumentsParser.usage("  "));
  }

  private static final void abort(final String message) {
    System.err.println(message);
    usage();
    System.exit(1);
  }

  public static final void main(final String[] args) {
    ProgramArguments arguments = null;

    try {
      arguments = argumentsParser.parseArgs(args);
    } catch (final Exception exception) {
      abort(String.format("[!] %s", exception.getMessage()));
    }

    assert (arguments != null);

    final int[] counts = new int[ClassificationResult.values().length];

    final Path directory = Paths.get(arguments.getOption(OPTION_PATH));
    final String pattern = arguments.getOption(OPTION_PATTERN);
    final boolean recursive = arguments.hasOption(OPTION_RECURSIVE);

    final boolean lazyLexer = arguments.hasOption(OPTION_LAZY_LEXER);

    final int maxNumberOfSteps =
        arguments.getIntOptionOr(OPTION_MAX_NUMBER_OF_STEPS, Interpreter.UNBOUNDED);
    final int maxNumberOfLoopIterations =
        arguments.getIntOptionOr(OPTION_MAX_NUMBER_OF_LOOP_ITERATIONS, Interpreter.UNBOUNDED);

    final List<File> programFiles = FileUtil.findFiles(directory, pattern, recursive);
    final int programCount = programFiles.size();

    System.out.format("found %d programs...\n", programCount);

    int count = 0;

    for (final File programFile : programFiles) {
      final String programCode = FileUtil.readFile(programFile);

      final ClassificationResult classificationResult =
          classify(programCode, lazyLexer, maxNumberOfSteps, maxNumberOfLoopIterations);
      ++counts[classificationResult.ordinal()];

      System.out.print(".");
      ++count;

      if (count % 10 == 0) {
        System.out.format("%5d\n", count);
      }
    }

    if (count % 10 != 0) {
      System.out.println();
    }

    System.out.format("of %d programs...\n", programCount);

    System.out.format("... %5d are lexically invalid\n",
        counts[ClassificationResult.LEXICALLY_INVALID.ordinal()]);

    System.out.format("... %5d are syntactically invalid\n",
        counts[ClassificationResult.SYNTACTICALLY_INVALID.ordinal()]);

    System.out.format("... %5d are semantically invalid\n",
        counts[ClassificationResult.SEMANTICALLY_INVALID.ordinal()]);

    System.out.format("... %5d are dynamically invalid\n",
        counts[ClassificationResult.DYNAMICALLY_INVALID.ordinal()]);

    System.out.format("... %5d are (apparently) non-terminating\n",
        counts[ClassificationResult.NON_TERMINATING.ordinal()]);

    System.out.format("... %5d are valid\n",
        counts[ClassificationResult.VALID.ordinal()]);
  }

  private static final ClassificationResult classify(final String programCode,
      final boolean lazyLexer, final int maxNumberOfSteps, final int maxNumberOfLoopIterations) {
    final Program program;
    {
      try {
        final TokenStream tokenStream;
        {
          if (lazyLexer) {
            tokenStream = LazyTokenStream.from(new Lexer(programCode));
          } else {
            tokenStream = EagerTokenStream.from(new Lexer(programCode));
          }
        }

        program = Parser.parse(tokenStream);

        SemanticAnalysis.analyze(program);
        Interpreter.checkDynamicallyValid(program, maxNumberOfSteps, maxNumberOfLoopIterations);

        return ClassificationResult.VALID;
      } catch (final InvalidProgramException exception) {
        switch (exception.getKind()) {
          case LEXICALLY_INVALID: {
            return ClassificationResult.LEXICALLY_INVALID;
          }
          case SYNTACTICALLY_INVALID: {
            return ClassificationResult.SYNTACTICALLY_INVALID;
          }
          case SEMANTICALLY_INVALID: {
            return ClassificationResult.SEMANTICALLY_INVALID;
          }
          case DYNAMICALLY_INVALID: {
            return ClassificationResult.DYNAMICALLY_INVALID;
          }
          case NON_TERMINATING: {
            return ClassificationResult.NON_TERMINATING;
          }
          default: {
            assert (false);
            throw new RuntimeException("unknown error in input program");
          }
        }
      }
    }
  }

}
