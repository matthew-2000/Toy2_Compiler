package visitor;

import nodes.*;
import visitor.exception.SemanticException;
import visitor.symbolTable.SymbolTableManager;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTableManager symbolTableManager;

    public ScopeCheckingVisitor() {
        this.symbolTableManager = new SymbolTableManager();
    }

    @Override
    public Void visit(ProgramNode node) throws SemanticException {
        symbolTableManager.enterScope();  // Scope globale

//        // Visita ciascun nodo figlio
//        if (node.getIterWithoutProcedure() != null) {
//            node.getIterWithoutProcedure().accept(this);
//        }
//        if (node.getProcedure() != null) {
//            node.getProcedure().accept(this);
//        }
//        if (node.getIter() != null) {
//            node.getIter().accept(this);
//        }

        symbolTableManager.exitScope();  // Esce dallo scope globale
        return null;
    }

    @Override
    public Object visit(VarDeclNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(FunctionNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ProcedureNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(FuncParamsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ProcParamsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(OtherFuncParamsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(OtherProcParamsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Void visit(ConstNode node) throws SemanticException {
        String value = node.getValue();

        if (value.matches("^-?\\d+$")) {
            node.setType("INTEGER");
        } else if (value.matches("^-?\\d+\\.\\d+$")) {
            node.setType("REAL");
        } else if (value.equals("TRUE") || value.equals("FALSE")) {
            node.setType("BOOLEAN");
        } else if (value.startsWith("\"") && value.endsWith("\"")) {
            node.setType("STRING");
        } else {
            throw new SemanticException("Tipo di costante non riconosciuto: " + value);
        }

        return null;
    }

    @Override
    public Void visit(TypeNode node) throws SemanticException {
        String type = node.getType();

        // Verifica che il tipo sia valido nel contesto del linguaggio Toy2
        if (!type.equals("INTEGER") && !type.equals("REAL") &&
                !type.equals("STRING") && !type.equals("BOOLEAN")) {
            throw new SemanticException("Tipo non riconosciuto: " + type);
        }

        return null;
    }

    @Override
    public Void visit(IdsNode node) throws SemanticException {
        for (String identifier : node.getIdentifiers()) {
            if (symbolTableManager.lookup(identifier) == null) {
                throw new SemanticException("Identificatore non dichiarato: " + identifier);
            }
        }
        return null;
    }

    @Override
    public Object visit(TypesNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ExprNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(FunCallNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ProcCallNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(IfStatNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ElifNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ElseNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ElifsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(WhileStatNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(BodyNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(StatNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(IOArgsListNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(IOArgNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(IterWithoutProcedureNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(IterNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(DeclsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ExprsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ConstsNode node) throws SemanticException {
        return null;
    }

    @Override
    public Object visit(ProcExprsNode node) throws SemanticException {
        return null;
    }


}
