package i2.act.examples.minic;

import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.ast.Program;
import i2.act.examples.minic.frontend.ast.visitors.DotGenerator;
import i2.act.examples.minic.frontend.parser.Parser;
import i2.act.util.FileUtil;
import i2.act.util.SafeWriter;
import i2.act.util.options.ProgramArguments;
import i2.act.util.options.ProgramArgumentsParser;

public final class MiniC {

  private static final ProgramArgumentsParser argumentsParser;

  private static final String OPTION_INPUT_FILE = "--in";

  private static final String OPTION_TO_DOT = "--toDot";

  static {
    argumentsParser = new ProgramArgumentsParser();

    argumentsParser.addOption(OPTION_INPUT_FILE, true, true, "<path to input file>");

    argumentsParser.addOption(OPTION_TO_DOT, false);
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

    try {
      final Program program = Parser.parse(input);

      if (arguments.hasOption(OPTION_TO_DOT)) {
        final SafeWriter writer = SafeWriter.openStdOut();
        DotGenerator.printDot(program, writer);
        writer.flush();
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

}
