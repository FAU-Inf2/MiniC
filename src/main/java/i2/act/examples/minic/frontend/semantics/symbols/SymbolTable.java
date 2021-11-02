package i2.act.examples.minic.frontend.semantics.symbols;

import i2.act.examples.minic.errors.InvalidProgramException;
import i2.act.examples.minic.frontend.info.SourcePosition;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public final class SymbolTable {

  private static final boolean DEFAULT_CASE_SENSITIVE = true;

  private final class Scope {

    public final Map<String, Symbol> symbols;

    public Scope() {
      this.symbols = new HashMap<String, Symbol>();
    }

    private final String getMappedName(final String name) {
      return (SymbolTable.this.caseSensitive) ? (name) : (name.toUpperCase());
    }

    public final boolean has(final String name) {
      final String mappedName = getMappedName(name);
      return this.symbols.containsKey(mappedName);
    }

    public final Symbol get(final String name) {
      final String mappedName = getMappedName(name);
      return this.symbols.get(mappedName);
    }

    public final void declare(final Symbol symbol) {
      final String mappedName = getMappedName(symbol.getName());
      this.symbols.put(mappedName, symbol);
    }

  }

  private final boolean caseSensitive;
  private final LinkedList<Scope> scopes;

  public SymbolTable() {
    this(DEFAULT_CASE_SENSITIVE);
  }

  public SymbolTable(final boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
    this.scopes = new LinkedList<Scope>();
  }

  public final void enterScope() {
    this.scopes.add(new Scope());
  }

  public final void leaveScope() {
    assert (!this.scopes.isEmpty());
    this.scopes.removeLast();
  }

  public final int numberOfScopes() {
    return this.scopes.size();
  }

  public final Symbol get(final String name, final SourcePosition position) {
    for (Iterator<Scope> iterator = this.scopes.descendingIterator(); iterator.hasNext();) {
      final Scope scope = iterator.next();
      final Symbol symbol = scope.get(name);

      if (symbol != null) {
        return symbol;
      }
    }

    throw InvalidProgramException.semanticallyInvalid(position,
        String.format("name '%s' not declared", name));
  }

  public final void declare(final Symbol symbol, final SourcePosition position) {
    assert (!this.scopes.isEmpty());
    final Scope scope = this.scopes.getLast();

    final String name = symbol.getName();

    if (scope.has(name)) {
      throw InvalidProgramException.semanticallyInvalid(position,
          String.format("name '%s' already declared", name));
    }

    scope.declare(symbol);
  }

}
