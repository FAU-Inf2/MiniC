package runtime;

import i2.act.fuzzer.runtime.EmbeddedCode;

public final class Util {

  public static final EmbeddedCode helperFunctions() {
    final EmbeddedCode code = EmbeddedCode.create();

    code.print("int div(int a, int b) {");
    code.indent();
    code.print("if (b == 0) { return 0; }");
    code.newline();
    code.print("return a / b;");
    code.unindent();
    code.print("}");

    return code;
  }

}
