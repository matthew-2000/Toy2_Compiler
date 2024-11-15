package visitor.symbolTable;

import visitor.Visitable;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;

public class SymbolTableManager {
    private Stack<SymbolTable> scopeStack;
    private Symbol currentProcedureOrFunctionSymbol;
    private Map<Visitable, SymbolTable> scopeMap;  // Mappa ogni nodo AST con la sua SymbolTable

    public SymbolTableManager() {
        scopeStack = new Stack<>();
        scopeMap = new HashMap<>();
        scopeStack.push(new SymbolTable(null, "GLOBAL"));  // Tabella globale (senza genitore)
    }

    // Entra in un nuovo scope
    public void enterScope(String scopeName, Visitable node) {
        SymbolTable newScope = new SymbolTable(scopeStack.peek(), scopeName);
        scopeStack.push(newScope);
        scopeMap.put(node, newScope);  // Associa il nodo AST alla nuova SymbolTable
    }

    // Esci dallo scope corrente
    public void exitScope() throws SemanticException {

//        System.out.println("==== EXIT SCOPE ====");
//        System.out.println(scopeStack.peek().toString());

        if (!scopeStack.isEmpty()) {
            SymbolTable currentTable = scopeStack.pop();
            // Verifica se ci sono riferimenti non risolti in questo scope
            if (!currentTable.getUnresolvedReferences().isEmpty()) {
                for (String s : currentTable.getUnresolvedReferences()) {
                    scopeStack.peek().addUnresolvedReference(s);
                }
            }
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

    public SymbolTable getScope(Visitable node) {
        return scopeMap.get(node);  // Ottiene la SymbolTable associata al nodo
    }

    // Aggiungi un simbolo allo scope corrente (variabile)
    public boolean addSymbol(String name, Type type, SymbolKind kind) {
        Symbol symbol = new Symbol(name, type, kind);
        return scopeStack.peek().addSymbol(name, symbol);
    }

    // Aggiunge un riferimento non risolto allo scope corrente
    public void addUnresolvedReference(String name) {
        scopeStack.peek().addUnresolvedReference(name);
    }

    // Aggiungi un simbolo per una funzione
    public boolean addFunctionSymbol(String name, List<Type> paramTypes, List<Type> returnTypes) {
        Symbol symbol = new Symbol(name, paramTypes, returnTypes, SymbolKind.FUNCTION);
        boolean added = scopeStack.peek().addSymbol(name, symbol);
        if (added) {
            currentProcedureOrFunctionSymbol = symbol;
        }
        return added;
    }

    // Aggiungi un simbolo per una procedura
    public boolean addProcedureSymbol(String name, List<Type> paramTypes, List<Boolean> isOutParams) {
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
    public void updateCurrentProcedureOrFunctionParams(List<Type> paramTypes, List<Boolean> isOutParams) {
        if (currentProcedureOrFunctionSymbol != null) {
            currentProcedureOrFunctionSymbol.setParamTypes(paramTypes);
            if (currentProcedureOrFunctionSymbol.getKind() == SymbolKind.PROCEDURE) {
                currentProcedureOrFunctionSymbol.setIsOutParams(isOutParams);
            }
        }
    }

    // Aggiorna i tipi di ritorno per la funzione corrente
    public void updateCurrentFunctionReturnTypes(List<Type> returnTypes) {
        if (currentProcedureOrFunctionSymbol != null && currentProcedureOrFunctionSymbol.getKind() == SymbolKind.FUNCTION) {
            currentProcedureOrFunctionSymbol.setReturnTypes(returnTypes);
        }
    }

    // Verifica i riferimenti non risolti alla fine del programma
    public void checkUnresolvedReferencesAtEnd() throws SemanticException {
        SymbolTable globalScope = scopeStack.firstElement();
        if (!globalScope.getUnresolvedReferences().isEmpty()) {
            throw new SemanticException("Riferimenti a simboli non dichiarati: " + globalScope.getUnresolvedReferences());
        }
    }
}
