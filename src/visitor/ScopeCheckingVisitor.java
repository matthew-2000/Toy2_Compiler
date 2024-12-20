package visitor;

import nodes.*;
import nodes.ProgramNode;
import nodes.expr.*;
import nodes.stat.*;
import visitor.exception.SemanticException;
import visitor.symbolTable.Symbol;
import visitor.symbolTable.SymbolKind;
import visitor.symbolTable.SymbolTableManager;
import visitor.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class ScopeCheckingVisitor implements Visitor<Object> {
    private SymbolTableManager symbolTableManager;
    private boolean mainProcedureDeclared = false;

    public ScopeCheckingVisitor(SymbolTableManager symbolTableManager) {
        this.symbolTableManager = symbolTableManager;
    }

    @Override
    public Object visit(ProgramNode node) throws SemanticException {
        // Entra nello scope globale
        symbolTableManager.enterScope("PROGRAM_NODE", node);

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

        if (!mainProcedureDeclared) {
            throw new SemanticException("Non è stata dichiarata una procedura 'main'.");
        }

        symbolTableManager.checkUnresolvedReferencesAtEnd();

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
        List<ConstNode> consts = node.getConsts();

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
        }

        // Dichiarazione delle variabili
        for (int i = 0; i < ids.size(); i++) {
            // Verifica se la variabile è già dichiarata nello scope corrente
            Type type = node.getType();
            String id = ids.get(i);
            if (type == null) {
                assert consts != null;
                if (consts.get(i) != null) {
                    type = consts.get(i).getType();
                }
            }
            boolean success = symbolTableManager.addSymbol(id, type != null ? type : Type.NOTYPE, SymbolKind.VARIABLE);
            if (!success) {
                throw new SemanticException("Variabile '" + id + "' già dichiarata nello scope corrente.");
            }
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
    public Void visit(FunctionNode node) throws SemanticException {
        // Aggiungi la funzione alla tabella dei simboli
        boolean success = symbolTableManager.addFunctionSymbol(
                node.getName(),
                null, // I tipi dei parametri saranno aggiornati dopo
                node.getReturnTypes()
        );
        if (!success) {
            throw new SemanticException("Funzione '" + node.getName() + "' già dichiarata.");
        }

        // Entra nello scope della funzione
        symbolTableManager.enterScope(node.getName() + "_FUNCTION_NODE", node);

        // Visita i parametri per aggiungerli allo scope corrente e raccogliere i tipi
        List<Type> paramTypes = new ArrayList<>();
        if (node.getParams() != null) {
            paramTypes = (List<Type>) node.getParams().accept(this);
        }

        // Aggiorna i tipi dei parametri nella funzione corrente
        symbolTableManager.updateCurrentProcedureOrFunctionParams(paramTypes, null);
        symbolTableManager.updateCurrentFunctionReturnTypes(node.getReturnTypes());

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
        List<Type> paramTypes = new ArrayList<>();
        for (ParamNode param : node.getParams()) {
            paramTypes.add((Type) param.accept(this));
        }
        return paramTypes;
    }

    @Override
    public Object visit(ParamNode node) throws SemanticException {
        // Aggiungi il parametro alla tabella dei simboli dello scope corrente
        boolean success = symbolTableManager.addSymbol(node.getName(), node.getType(), SymbolKind.VARIABLE);
        if (!success) {
            throw new SemanticException("Parametro '" + node.getName() + "' già dichiarato nello scope corrente.");
        }
        symbolTableManager.lookup(node.getName()).setIsParameter(true);
        return node.getType();
    }

    @Override
    public Void visit(ProcedureNode node) throws SemanticException {
        // Controlla che la procedura si chiami "main"
        if (node.getName().equals("main")) {
            if (mainProcedureDeclared) {
                throw new SemanticException("La procedura 'main' è già stata dichiarata.");
            }
            mainProcedureDeclared = true;
        }

        // Aggiungi la procedura alla tabella dei simboli
        boolean success = symbolTableManager.addProcedureSymbol(
                node.getName(),
                null, // I tipi dei parametri saranno aggiornati dopo
                null  // I flag isOutParams saranno aggiornati dopo
        );
        if (!success) {
            throw new SemanticException("Procedura '" + node.getName() + "' già dichiarata.");
        }

        // Entra nello scope della procedura
        symbolTableManager.enterScope(node.getName() + "_PROC_NODE", node);

        // Visita i parametri per aggiungerli allo scope corrente e raccogliere i tipi e i flag isOut
        List<Type> paramTypes = new ArrayList<>();
        List<Boolean> isOutParams = new ArrayList<>();
        if (node.getParams() != null) {
            for (ProcParamNode param : node.getParams().getParams()) {
                param.accept(this);
                paramTypes.add(param.getType());
                isOutParams.add(param.isOut());
            }
        }

        // Aggiorna i tipi dei parametri e i flag isOut nella procedura corrente
        symbolTableManager.updateCurrentProcedureOrFunctionParams(paramTypes, isOutParams);

        // Visita il corpo della procedura
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        // Esci dallo scope della procedura
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(ProcParamsNode node) throws SemanticException {
        List<Type> paramTypes = new ArrayList<>();
        List<Boolean> isOutParams = new ArrayList<>();

        for (ProcParamNode param : node.getParams()) {
            param.accept(this);

            // Raccogli i tipi e i flag 'isOut' per la procedura
            paramTypes.add(param.getType());
            isOutParams.add(param.isOut());
        }

        // Aggiorna la procedura corrente nella tabella dei simboli con le informazioni sui parametri
        Symbol currentProcedure = symbolTableManager.getCurrentProcedureOrFunctionSymbol();
        if (currentProcedure != null) {
            currentProcedure.setParamTypes(paramTypes);
            currentProcedure.setIsOutParams(isOutParams);
        }

        return null;
    }

    @Override
    public Void visit(ProcParamNode node) throws SemanticException {
        // Aggiungi il parametro alla tabella dei simboli dello scope corrente
        boolean success = symbolTableManager.addSymbol(node.getName(), node.getType(), SymbolKind.VARIABLE);
        if (!success) {
            throw new SemanticException("Parametro '" + node.getName() + "' già dichiarato nello scope corrente.");
        }
        symbolTableManager.lookup(node.getName()).setIsOut(node.isOut());
        return null;
    }

    @Override
    public Void visit(BodyNode node) throws SemanticException {
        for (Visitable statement : node.getStatements()) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(AssignStatNode node) throws SemanticException {
        List<String> ids = node.getIds();
        List<ExprNode> exprs = node.getExprs();

        // Verifica che le variabili siano dichiarate
        for (String id : ids) {
            Symbol symbol = symbolTableManager.lookup(id);
            if (symbol == null) {
//                throw new SemanticException("Variabile '" + id + "' non dichiarata.");
                symbolTableManager.addUnresolvedReference(id);
            }
        }

        // Visita le espressioni
        for (ExprNode expr : exprs) {
            expr.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ProcCallStatNode node) throws SemanticException {
        node.getProcCall().accept(this);
        return null;
    }

    @Override
    public Void visit(ReturnStatNode node) throws SemanticException {
        // Verifica che siamo all'interno di una funzione
        Symbol currentFunction = symbolTableManager.getCurrentProcedureOrFunctionSymbol();
        if (currentFunction == null || currentFunction.getKind() != SymbolKind.FUNCTION) {
            throw new SemanticException("Istruzione 'return' non permessa al di fuori di una funzione.");
        }

        List<Type> returnTypes = currentFunction.getReturnTypes();
        List<ExprNode> exprs = node.getExprs();

        // Verifica che il numero di espressioni restituite corrisponda ai tipi di ritorno
        if (exprs.size() != returnTypes.size()) {
            throw new SemanticException("Il numero di valori restituiti non corrisponde al numero di tipi di ritorno dichiarati.");
        }

        // Visita le espressioni e verifica i tipi
        for (ExprNode expr : exprs) {
            expr.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(WriteStatNode node) throws SemanticException {
        List<IOArgNode> args = node.getArgs();

        // Visita ciascun argomento di I/O
        for (IOArgNode arg : args) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(WriteReturnStatNode node) throws SemanticException {
        List<IOArgNode> args = node.getArgs();

        // Visita ciascun argomento di I/O
        for (IOArgNode arg : args) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ReadStatNode node) throws SemanticException {
        List<IOArgNode> args = node.getArgs();

        for (IOArgNode arg : args) {
            if (arg instanceof IOArgIdentifierNode idNode) {
                String id = idNode.getIdentifier();

                // Verifica che l'identificatore sia dichiarato
                Symbol symbol = symbolTableManager.lookup(id);
                if (symbol == null) {
                    symbolTableManager.addUnresolvedReference(id);
                    // throw new SemanticException("Variabile '" + id + "' non dichiarata.");
                }
            }
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ProcCallNode node) throws SemanticException {
        String procName = node.getProcedureName();
        Symbol symbol = symbolTableManager.lookup(procName);
        if (symbol == null) {
            symbolTableManager.addUnresolvedReference(procName);
        } else if (symbol.getKind() != SymbolKind.PROCEDURE) {
            throw new SemanticException("Identificatore '" + procName + "' non è una procedura.");
        }

        List<ProcExprNode> args = node.getArguments();

        // Visita gli argomenti e verifica i tipi
        for (ExprNode arg : args) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(IfStatNode node) throws SemanticException {
        node.getCondition().accept(this);

        symbolTableManager.enterScope("IF_NODE", node);
        node.getThenBody().accept(this);
        symbolTableManager.exitScope();

        for (ElifNode elif : node.getElifBlocks()) {
            elif.accept(this);
        }

        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ElifNode node) throws SemanticException {
        // Visita la condizione
        node.getCondition().accept(this);

        // Entra in un nuovo scope per il corpo dell'Elif
        symbolTableManager.enterScope("ELIF_NODE", node);
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(ElseNode node) throws SemanticException {
        // Entra in un nuovo scope per il corpo dell'Else
        symbolTableManager.enterScope("ELSE_NODE", node);
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(WhileStatNode node) throws SemanticException {
        node.getCondition().accept(this);

        // Entra in un nuovo scope per il corpo del loop
        symbolTableManager.enterScope("WHILE_NODE", node);
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(FunCallNode node) throws SemanticException {
        String funcName = node.getFunctionName();
        Symbol symbol = symbolTableManager.lookup(funcName);
        if (symbol == null) {
            // Se la funzione non è dichiarata, aggiungiamo un riferimento non risolto
            symbolTableManager.addUnresolvedReference(funcName);
        } else if (symbol.getKind() != SymbolKind.FUNCTION) {
            throw new SemanticException("Identificatore '" + funcName + "' non è una funzione.");
        } else {
            node.setReturnTypes(symbol.getReturnTypes());
        }

        List<ExprNode> args = node.getArguments();

        // Visita gli argomenti e verifica i tipi
        for (ExprNode arg : args) {
            arg.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(IOArgIdentifierNode node) throws SemanticException {
        String id = node.getIdentifier();

        // Verifica che l'identificatore sia dichiarato
        Symbol symbol = symbolTableManager.lookup(id);
        if (symbol == null) {
            // throw new SemanticException("Variabile '" + id + "' non dichiarata.");
            symbolTableManager.addUnresolvedReference(id);
        }

        return null;
    }

    @Override
    public Void visit(IOArgStringLiteralNode node) throws SemanticException {
        // Nessun controllo semantico necessario per un literal di stringa
        return null;
    }

    @Override
    public Void visit(IOArgBinaryNode node) throws SemanticException {
        // Visita il nodo sinistro
        node.getLeft().accept(this);

        // Visita il nodo destro
        node.getRight().accept(this);

        // Verifica che l'operatore sia valido (in questo caso, dovrebbe essere "+")
        String operator = node.getOperator();
        if (!operator.equals("+")) {
            throw new SemanticException("Operatore non valido '" + operator + "' in IOArgBinaryNode.");
        }

        return null;
    }

    @Override
    public Void visit(DollarExprNode node) throws SemanticException {
        // Visita l'espressione contenuta
        node.getExpr().accept(this);

        return null;
    }

    @Override
    public Void visit(ProcExprNode node) throws SemanticException {
        if (node.isRef()) {
            // Se è un parametro passato per riferimento, l'espressione deve essere un identificatore
            if (!(node.getExpr() instanceof IdentifierNode)) {
                throw new SemanticException("Un parametro 'REF' deve essere un identificatore.");
            }

            String id = ((IdentifierNode) node.getExpr()).getName();
            Symbol symbol = symbolTableManager.lookup(id);
            if (symbol == null) {
                // throw new SemanticException("Variabile '" + id + "' non dichiarata.");
                symbolTableManager.addUnresolvedReference(id);
            }
            // Ulteriori controlli sul tipo possono essere aggiunti qui
        } else {
            // Visita l'espressione
            node.getExpr().accept(this);
        }
        return null;
    }

    @Override
    public Void visit(RealConstNode node) throws SemanticException {
        // Nessun controllo semantico necessario per una costante reale
        return null;
    }

    @Override
    public Void visit(IntConstNode node) throws SemanticException {
        // Nessun controllo semantico necessario per una costante intera
        return null;
    }

    @Override
    public Void visit(StringConstNode node) throws SemanticException {
        // Nessun controllo semantico necessario per una costante stringa
        return null;
    }

    @Override
    public Void visit(IdentifierNode node) throws SemanticException {
        String id = node.getName();
        Symbol symbol = symbolTableManager.lookup(id);
        if (symbol == null) {
            // throw new SemanticException("Variabile '" + id + "' non dichiarata.");
            symbolTableManager.addUnresolvedReference(id);
        }
        return null;
    }

    @Override
    public Void visit(BooleanConstNode node) throws SemanticException {
        // Nessun controllo semantico necessario per una costante booleana
        return null;
    }

    @Override
    public Void visit(BinaryExprNode node) throws SemanticException {
        // Visita il nodo sinistro
        node.getLeft().accept(this);

        // Visita il nodo destro
        node.getRight().accept(this);

        return null;
    }

    @Override
    public Void visit(UnaryExprNode node) throws SemanticException {
        // Visita l'espressione
        node.getExpr().accept(this);

        return null;
    }

}