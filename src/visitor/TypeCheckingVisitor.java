package visitor;

import nodes.*;
import nodes.expr.*;
import nodes.stat.*;
import visitor.exception.SemanticException;
import visitor.symbolTable.Symbol;
import visitor.symbolTable.SymbolKind;
import visitor.symbolTable.SymbolTableManager;
import visitor.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckingVisitor implements Visitor<Type> {
    private SymbolTableManager symbolTableManager;

    public TypeCheckingVisitor() {
        this.symbolTableManager = new SymbolTableManager();
    }

    @Override
    public Type visit(ProgramNode node) throws SemanticException {
        // Effettua il controllo dei tipi dell'intero programma
        symbolTableManager.enterScope();

        for (Visitable decl : node.getItersWithoutProcedure().getIterList()) {
            decl.accept(this);
        }

        node.getProcedure().accept(this);

        for (Visitable decl : node.getIters().getIterList()) {
            decl.accept(this);
        }

        // Controlli finali e uscita dallo scope globale
        symbolTableManager.exitScope();
        return Type.UNKNOWN; // Il Program non ha un tipo
    }

    // Esempio di implementazione per un nodo
    @Override
    public Type visit(AssignStatNode node) throws SemanticException {
        // Visita gli identificatori e le espressioni
        List<String> ids = node.getIds();
        List<ExprNode> exprs = node.getExprs();

        if (ids.size() != exprs.size()) {
            throw new SemanticException("Numero di identificatori e espressioni non corrisponde.");
        }

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            ExprNode expr = exprs.get(i);

            // Controlla la dichiarazione dell'identificatore
            Symbol symbol = symbolTableManager.lookup(id);
            if (symbol == null) {
                throw new SemanticException("Variabile '" + id + "' non dichiarata.");
            }

            // Visita l'espressione
            Type exprType = expr.accept(this);
            expr.setType(exprType);

            // Verifica la compatibilità dei tipi
            Type idType = symbol.getType(); // Supponiamo di avere un metodo getTypeEnum() in Symbol
            if (idType != exprType) {
                throw new SemanticException("Assegnazione a '" + id + "' non compatibile: " + idType + " := " + exprType);
            }

            // Controllo dell'immutabilità se è un parametro
            if (symbol.getKind() == SymbolKind.VARIABLE && symbol.isParameter()) {
                throw new SemanticException("Parametro '" + id + "' è immutabile e non può essere assegnato.");
            }
        }

        return Type.UNKNOWN; // L'assegnazione non ha un tipo di ritorno
    }

    // Implementazione di altri metodi visit
    // ...
}
