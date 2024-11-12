package visitor;

import nodes.*;
import nodes.ProgramNode;
import visitor.exception.SemanticException;
import visitor.symbolTable.Symbol;
import visitor.symbolTable.SymbolTableManager;

import java.util.List;

public class ScopeCheckingVisitor implements Visitor {
    private SymbolTableManager symbolTableManager;

    public ScopeCheckingVisitor() {
        this.symbolTableManager = new SymbolTableManager();
    }

    @Override
    public Object visit(ProgramNode node) throws SemanticException {
        // Entra nello scope globale
        symbolTableManager.enterScope();

        // Visita le dichiarazioni prima della procedura obbligatoria
        if (node.getItersWithoutProcedure() != null) {
            node.getItersWithoutProcedure().accept(this);
        }

        // Visita la procedura obbligatoria
        node.getProcedure().accept(this);

        // Visita le dichiarazioni dopo la procedura obbligatoria
        if (node.getIters() != null) {
            node.getIters().accept(this);
        }

        // Esci dallo scope globale
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Object visit(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            iter.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(IterWithoutProcedureNode node) throws SemanticException {
        node.getDeclaration().accept(this);
        return null;
    }

    @Override
    public Object visit(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            iter.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(IterNode node) throws SemanticException {
        node.getDeclaration().accept(this);
        return null;
    }

    @Override
    public Object visit(VarDeclNode node) throws SemanticException {
        for (DeclNode decl : node.getDecls()) {
            decl.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DeclNode node) throws SemanticException {
        List<String> ids = node.getIds();
        String type = node.getType();
        List<ConstNode> consts = node.getConsts();

        // Dichiarazione delle variabili
        for (String id : ids) {
            // Verifica se la variabile è già dichiarata nello scope corrente
            boolean success = symbolTableManager.addSymbol(id, type != null ? type : "inferred", false);
            if (!success) {
                throw new SemanticException("Variabile '" + id + "' già dichiarata nello scope corrente.");
            }
        }

        // Gestione delle costanti (assegnazione)
        if (consts != null) {
            // Verifica che il numero di costanti corrisponda al numero di identificatori
            if (consts.size() != ids.size()) {
                throw new SemanticException("Il numero di costanti non corrisponde al numero di variabili dichiarate.");
            }

            // Visita le costanti
            for (ConstNode constNode : consts) {
                constNode.accept(this);
            }

            // Ulteriori controlli sui tipi possono essere aggiunti qui
        }

        return null;
    }

    @Override
    public Object visit(ConstNode node) throws SemanticException {
        Object value = node.getValue();
        // Esegui eventuali controlli sul valore della costante
        // Ad esempio, verifica che il valore sia valido nel contesto
        return null;
    }

    @Override
    public Object visit(FunctionNode node) throws SemanticException {
        // Aggiungi la funzione alla tabella dei simboli
        boolean success = symbolTableManager.addSymbol(node.getName(), "function", true);
        if (!success) {
            throw new SemanticException("Funzione '" + node.getName() + "' già dichiarata.");
        }

        // Entra nello scope della funzione
        symbolTableManager.enterScope();

        // Aggiungi i parametri alla tabella dei simboli
        if (node.getParams() != null) {
            node.getParams().accept(this);
        }

        // Visita il corpo della funzione
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        // Esci dallo scope della funzione
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Object visit(FuncParamsNode node) throws SemanticException {
        for (ParamNode param : node.getParams()) {
            param.accept(this);
        }
        return null;
    }

}