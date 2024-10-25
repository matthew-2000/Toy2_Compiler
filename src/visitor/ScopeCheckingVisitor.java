package visitor;

import nodes.ProgramNode;
import visitor.symbolTable.SymbolTableManager;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTableManager symbolTableManager;

    public ScopeCheckingVisitor() {
        this.symbolTableManager = new SymbolTableManager();
    }

    @Override
    public Void visit(ProgramNode node) throws Exception {
        symbolTableManager.enterScope();  // Entra nello scope globale

//        // Visita il nodo iterWithoutProcedure se esiste
//        if (node.getIterWithoutProcedure() != null) {
//            node.getIterWithoutProcedure().accept(this);
//        }
//
//        // Visita il nodo procedure se esiste
//        if (node.getProcedure() != null) {
//            node.getProcedure().accept(this);
//        }
//
//        // Visita il nodo iter se esiste
//        if (node.getIter() != null) {
//            node.getIter().accept(this);
//        }

        symbolTableManager.exitScope();  // Esci dallo scope globale
        return null;
    }

}
