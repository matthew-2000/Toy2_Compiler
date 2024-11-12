package visitor.symbolTable;

import java.util.Stack;
import java.util.List;

public class SymbolTableManager {
    private Stack<SymbolTable> scopeStack;
    private Symbol currentProcedureOrFunctionSymbol;

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
            // Se stiamo uscendo dallo scope di una funzione o procedura, aggiorniamo il simbolo corrente
            if (currentProcedureOrFunctionSymbol != null && scopeStack.peek() != null) {
                SymbolTable currentScope = scopeStack.peek();
                Symbol parentSymbol = currentScope.lookup(currentProcedureOrFunctionSymbol.getName());
                if (parentSymbol == null || parentSymbol != currentProcedureOrFunctionSymbol) {
                    currentProcedureOrFunctionSymbol = null;
                }
            }
        }
    }

    // Aggiungi un simbolo allo scope corrente (variabile)
    public boolean addSymbol(String name, String type, SymbolKind kind) {
        Symbol symbol = new Symbol(name, type, kind);
        return scopeStack.peek().addSymbol(name, symbol);
    }

    // Aggiungi un simbolo per una funzione
    public boolean addFunctionSymbol(String name, List<String> paramTypes, List<String> returnTypes) {
        Symbol symbol = new Symbol(name, paramTypes, returnTypes, SymbolKind.FUNCTION);
        boolean added = scopeStack.peek().addSymbol(name, symbol);
        if (added) {
            currentProcedureOrFunctionSymbol = symbol;
        }
        return added;
    }

    // Aggiungi un simbolo per una procedura
    public boolean addProcedureSymbol(String name, List<String> paramTypes, List<Boolean> isOutParams) {
        Symbol symbol = new Symbol(name, SymbolKind.PROCEDURE, paramTypes, isOutParams);
        boolean added = scopeStack.peek().addSymbol(name, symbol);
        if (added) {
            currentProcedureOrFunctionSymbol = symbol;
        }
        return added;
    }

    // Cerca un simbolo risalendo nella catena degli scope
    public Symbol lookup(String name) {
        return scopeStack.peek().lookup(name);
    }

    // Ottiene il simbolo della funzione o procedura corrente
    public Symbol getCurrentProcedureOrFunctionSymbol() {
        return currentProcedureOrFunctionSymbol;
    }

    // Aggiorna i tipi di parametri e i flag isOutParams per la procedura o funzione corrente
    public void updateCurrentProcedureOrFunctionParams(List<String> paramTypes, List<Boolean> isOutParams) {
        if (currentProcedureOrFunctionSymbol != null) {
            currentProcedureOrFunctionSymbol.setParamTypes(paramTypes);
            if (currentProcedureOrFunctionSymbol.getKind() == SymbolKind.PROCEDURE) {
                currentProcedureOrFunctionSymbol.setIsOutParams(isOutParams);
            }
        }
    }

    // Aggiorna i tipi di ritorno per la funzione corrente
    public void updateCurrentFunctionReturnTypes(List<String> returnTypes) {
        if (currentProcedureOrFunctionSymbol != null && currentProcedureOrFunctionSymbol.getKind() == SymbolKind.FUNCTION) {
            currentProcedureOrFunctionSymbol.setReturnTypes(returnTypes);
        }
    }
}
