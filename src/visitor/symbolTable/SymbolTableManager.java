package visitor.symbolTable;

import java.util.Stack;

public class SymbolTableManager {
    private Stack<SymbolTable> scopeStack;

    public SymbolTableManager() {
        scopeStack = new Stack<>();
        scopeStack.push(new SymbolTable(null));  // Tabella globale (senza genitore)
    }

    // Entra in un nuovo scope
    public void enterScope() {
        SymbolTable newScope = new SymbolTable(scopeStack.peek());
        scopeStack.push(newScope);
    }

    // Esci dallo scope corrente
    public void exitScope() {
        if (!scopeStack.isEmpty()) {
            scopeStack.pop();
        }
    }

    // Aggiungi un simbolo allo scope corrente
    public boolean addSymbol(String name, String type, boolean isFunction) {
        return scopeStack.peek().addSymbol(name, new Symbol(name, type, isFunction));
    }

    // Cerca un simbolo risalendo nella catena degli scope
    public Symbol lookup(String name) {
        return scopeStack.peek().lookup(name);
    }
}
