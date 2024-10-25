package visitor.symbolTable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;
    private SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;  // Riferimento alla tabella padre per risalire nella catena
    }

    // Aggiunge un simbolo alla tabella
    public boolean addSymbol(String name, Symbol symbol) {
        if (symbols.containsKey(name)) {
            return false;  // Il simbolo è già dichiarato in questo scope
        }
        symbols.put(name, symbol);
        return true;
    }

    // Cerca un simbolo nella tabella corrente e negli scope superiori
    public Symbol lookup(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        } else if (parent != null) {
            return parent.lookup(name);  // Cerca nella tabella superiore
        }
        return null;  // Simbolo non trovato
    }
}
