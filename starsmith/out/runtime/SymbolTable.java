package runtime;

import i2.act.fuzzer.runtime.EmbeddedCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SymbolTable {

  private final Set<Symbol> definedSymbols;
  private final List<Map<String, Symbol>> scopes;

  private SymbolTable() {
    this.definedSymbols = new LinkedHashSet<Symbol>();
    this.scopes = new ArrayList<Map<String, Symbol>>();
  }

  @Override
  protected final SymbolTable clone() {
    final SymbolTable clone = new SymbolTable();

    clone.definedSymbols.addAll(this.definedSymbols);

    for (final Map<String, Symbol> scope : this.scopes) {
      clone.scopes.add(new LinkedHashMap<String, Symbol>(scope));
    }

    return clone;
  }

  public static final SymbolTable empty() {
    final SymbolTable emptySymbolTable = new SymbolTable();
    emptySymbolTable.scopes.add(new LinkedHashMap<String, Symbol>());

    return emptySymbolTable;
  }

  public static final SymbolTable predefined(final Symbol... symbols) {
    final SymbolTable symbolTable = empty();

    final Map<String, Symbol> globalScope = symbolTable.scopes.get(symbolTable.scopes.size() - 1);

    for (final Symbol symbol : symbols) {
      globalScope.put(symbol.name, symbol);
      symbolTable.definedSymbols.add(symbol);
    }

    return symbolTable;
  }

  public static final SymbolTable enterScope(final SymbolTable symbolTable) {
    final SymbolTable newSymbolTable = symbolTable.clone();
    newSymbolTable.scopes.add(new LinkedHashMap<String, Symbol>());

    return newSymbolTable;
  }

  public static final SymbolTable leaveScope(final SymbolTable symbolTable) {
    final SymbolTable newSymbolTable = symbolTable.clone();

    final Map<String, Symbol> leftScope =
        newSymbolTable.scopes.get(newSymbolTable.scopes.size() - 1);

    for (final Symbol symbol : leftScope.values()) {
      newSymbolTable.definedSymbols.remove(symbol);
    }

    newSymbolTable.scopes.remove(newSymbolTable.scopes.size() - 1);

    return newSymbolTable;
  }

  public static final SymbolTable intersect(final SymbolTable first, final SymbolTable second) {
    assert (first.scopes.size() == second.scopes.size());

    final SymbolTable newSymbolTable = new SymbolTable();

    for (int scopeIndex = 0; scopeIndex < first.scopes.size(); ++scopeIndex) {
      final Map<String, Symbol> firstScope = first.scopes.get(scopeIndex);
      final Map<String, Symbol> secondScope = second.scopes.get(scopeIndex);

      final Map<String, Symbol> newScope = new LinkedHashMap<>();
      newSymbolTable.scopes.add(newScope);

      for (final Map.Entry<String, Symbol> entry : firstScope.entrySet()) {
        final String name = entry.getKey();
        final Symbol symbol = entry.getValue();

        if (secondScope.containsKey(name) && symbol.equals(secondScope.get(name))) {
          newScope.put(name, symbol);
        }
      }
    }

    for (final Symbol symbol : first.definedSymbols) {
      if (second.definedSymbols.contains(symbol)) {
        newSymbolTable.definedSymbols.add(symbol);
      }
    }

    return newSymbolTable;
  }

  public static final SymbolTable declare(final SymbolTable symbolTable, final Symbol symbol) {
    final SymbolTable newSymbolTable = symbolTable.clone();
    newSymbolTable.scopes.get(newSymbolTable.scopes.size() - 1).put(symbol.name, symbol);

    return newSymbolTable;
  }

  public static final SymbolTable define(final SymbolTable symbolTable, final Symbol symbol) {
    final SymbolTable newSymbolTable = symbolTable.clone();
    newSymbolTable.scopes.get(newSymbolTable.scopes.size() - 1).put(symbol.name, symbol);
    newSymbolTable.definedSymbols.add(symbol);

    return newSymbolTable;
  }

  public static final SymbolTable setDefined(final SymbolTable symbolTable, final Symbol symbol) {
    final SymbolTable newSymbolTable = symbolTable.clone();
    newSymbolTable.definedSymbols.add(symbol);

    return newSymbolTable;
  }

  public static final List<Symbol> visible(final SymbolTable symbolTable, final Type expectedType,
      final boolean definedOnly) {
    final List<Symbol> visible = new ArrayList<>();

    final Set<String> visibleNames = new LinkedHashSet<>();

    for (int scopeIndex = symbolTable.scopes.size() - 1; scopeIndex >= 0; --scopeIndex) {
      final Map<String, Symbol> scope = symbolTable.scopes.get(scopeIndex);

      for (final Map.Entry<String, Symbol> entry : scope.entrySet()) {
        final String name = entry.getKey();
        final Symbol symbol = entry.getValue();

        if (visibleNames.contains(name)) {
          continue;
        }

        visibleNames.add(name);

        if (Type.assignable(symbol.type, expectedType)) {
          if (!definedOnly || symbolTable.definedSymbols.contains(symbol)) {
            visible.add(symbol);
          }
        }
      }
    }

    return visible;
  }

  public static final boolean canDeclare(final SymbolTable symbolTable, final String name) {
    assert (!symbolTable.scopes.isEmpty());
    return !symbolTable.scopes.get(symbolTable.scopes.size() - 1).containsKey(name);
  }

  public static final EmbeddedCode printDefined(final SymbolTable symbolTable) {
    final EmbeddedCode code = EmbeddedCode.create();

    final List<Symbol> visible = SymbolTable.visible(symbolTable, Type.intType(), true);
    for (final Symbol definedVariable : visible) {
      code.print("print(" + definedVariable.name + ");");
      code.newline();
    }

    return code;
  }

}
