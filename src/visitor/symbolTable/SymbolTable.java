package visitor.symbolTable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SymbolTable {
    private String symbolTableName;
    private Map<String, Symbol> symbols;
    private Set<String> unresolvedReferences; // Nuovo set per riferimenti non risolti
    private SymbolTable parent;

    public SymbolTable(SymbolTable parent, String symbolTableName) {
        this.symbols = new HashMap<>();
        this.unresolvedReferences = new HashSet<>();
        this.parent = parent;  // Riferimento alla tabella padre per risalire nella catena
        this.symbolTableName = symbolTableName;
    }

    // Aggiunge un simbolo alla tabella
    public boolean addSymbol(String name, Symbol symbol) {
        if (symbols.containsKey(name)) {
            return false;  // Il simbolo è già dichiarato in questo scope
        }
        symbols.put(name, symbol);

        // Se il simbolo era in unresolvedReferences, lo rimuoviamo
        unresolvedReferences.remove(name);

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

    // Getter per il parent (potrebbe essere utile)
    public SymbolTable getParent() {
        return parent;
    }

    public Set<String> getUnresolvedReferences() {
        return unresolvedReferences;
    }

    // Aggiunge un riferimento non risolto
    public void addUnresolvedReference(String name) {
        unresolvedReferences.add(name);
    }

    // Rimuove un riferimento non risolto
    public void removeUnresolvedReference(String name) {
        unresolvedReferences.remove(name);
    }

    @Override
    public String toString() {
        return "SymbolTable{" +
                "symbolTableName='" + symbolTableName + '\'' +
                ", symbols=" + symbols +
                ", parent=" + parent.symbolTableName +
                '}';
    }
}