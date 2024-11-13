package visitor;

import nodes.*;
import nodes.ProgramNode;
import nodes.expr.*;
import nodes.stat.*;
import visitor.exception.SemanticException;
import visitor.symbolTable.Symbol;
import visitor.symbolTable.SymbolKind;
import visitor.symbolTable.SymbolTableManager;

import java.util.ArrayList;
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
            boolean success = symbolTableManager.addSymbol(id, type != null ? type : "inferred", SymbolKind.VARIABLE);
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
        symbolTableManager.enterScope();

        // Visita i parametri per aggiungerli allo scope corrente e raccogliere i tipi
        List<String> paramTypes = new ArrayList<>();
        if (node.getParams() != null) {
            paramTypes = (List<String>) node.getParams().accept(this);
        }

        // Aggiorna i tipi dei parametri nella funzione corrente
        symbolTableManager.updateCurrentProcedureOrFunctionParams(paramTypes, null);

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
        List<String> paramTypes = new ArrayList<>();
        for (ParamNode param : node.getParams()) {
            paramTypes.add((String) param.accept(this));
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
        return node.getType();
    }

    @Override
    public Void visit(ProcedureNode node) throws SemanticException {
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
        symbolTableManager.enterScope();

        // Visita i parametri per aggiungerli allo scope corrente e raccogliere i tipi e i flag isOut
        List<String> paramTypes = new ArrayList<>();
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
        List<String> paramTypes = new ArrayList<>();
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
                throw new SemanticException("Variabile '" + id + "' non dichiarata.");
            }
            // Ulteriori controlli sui tipi possono essere aggiunti qui
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

        List<String> returnTypes = currentFunction.getReturnTypes();
        List<ExprNode> exprs = node.getExprs();

        // Verifica che il numero di espressioni restituite corrisponda ai tipi di ritorno
        if (exprs.size() != returnTypes.size()) {
            throw new SemanticException("Il numero di valori restituiti non corrisponde al numero di tipi di ritorno dichiarati.");
        }

        // Visita le espressioni e verifica i tipi
        for (int i = 0; i < exprs.size(); i++) {
            ExprNode expr = exprs.get(i);
            expr.accept(this);

            // Qui potresti voler implementare un meccanismo per determinare il tipo dell'espressione
            // e confrontarlo con returnTypes.get(i). Questo richiede un type checking più avanzato.
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
            if (arg instanceof IOArgIdentifierNode) {
                IOArgIdentifierNode idNode = (IOArgIdentifierNode) arg;
                String id = idNode.getIdentifier();

                // Verifica che l'identificatore sia dichiarato
                Symbol symbol = symbolTableManager.lookup(id);
                if (symbol == null) {
                    throw new SemanticException("Variabile '" + id + "' non dichiarata.");
                }

                // Ulteriori controlli possono essere aggiunti qui, ad esempio verificare che la variabile sia assegnabile
            }
        }

        return null;
    }

    @Override
    public Void visit(ProcCallNode node) throws SemanticException {
        String procName = node.getProcedureName();
        Symbol symbol = symbolTableManager.lookup(procName);
        if (symbol == null || symbol.getKind() != SymbolKind.PROCEDURE) {
            throw new SemanticException("Procedura '" + procName + "' non dichiarata.");
        }

        List<ExprNode> args = node.getArguments();
        List<String> paramTypes = symbol.getParamTypes();

        // Verifica il numero di argomenti
        if (args.size() != paramTypes.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla procedura '" + procName + "'.");
        }

        // Visita gli argomenti e verifica i tipi
        for (int i = 0; i < args.size(); i++) {
            ExprNode arg = args.get(i);
            arg.accept(this);
            // Ulteriori controlli sui tipi possono essere aggiunti qui
        }

        return null;
    }

    @Override
    public Void visit(IfStatNode node) throws SemanticException {
        node.getCondition().accept(this);
        node.getThenBody().accept(this);

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
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(ElseNode node) throws SemanticException {
        // Entra in un nuovo scope per il corpo dell'Else
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(WhileStatNode node) throws SemanticException {
        node.getCondition().accept(this);

        // Entra in un nuovo scope per il corpo del loop
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(FunCallNode node) throws SemanticException {
        String funcName = node.getFunctionName();
        Symbol symbol = symbolTableManager.lookup(funcName);
        if (symbol == null || symbol.getKind() != SymbolKind.FUNCTION) {
            throw new SemanticException("Funzione '" + funcName + "' non dichiarata.");
        }

        List<ExprNode> args = node.getArguments();
        List<String> paramTypes = symbol.getParamTypes();

        // Verifica il numero di argomenti
        if (args.size() != paramTypes.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla funzione '" + funcName + "'.");
        }

        // Visita gli argomenti e verifica i tipi
        for (int i = 0; i < args.size(); i++) {
            ExprNode arg = args.get(i);
            arg.accept(this);
            // Ulteriori controlli sui tipi possono essere aggiunti qui
        }

        return null;
    }

    @Override
    public Void visit(IOArgIdentifierNode node) throws SemanticException {
        String id = node.getIdentifier();

        // Verifica che l'identificatore sia dichiarato
        Symbol symbol = symbolTableManager.lookup(id);
        if (symbol == null) {
            throw new SemanticException("Variabile '" + id + "' non dichiarata.");
        }

        // Ulteriori controlli possono essere aggiunti qui, ad esempio verificare il tipo della variabile

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

        // Ulteriori controlli possono essere aggiunti qui, ad esempio verificare che i tipi siano compatibili per l'operazione

        return null;
    }

    @Override
    public Void visit(DollarExprNode node) throws SemanticException {
        // Visita l'espressione contenuta
        node.getExpr().accept(this);

        // Ulteriori controlli possono essere aggiunti qui se necessario

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
                throw new SemanticException("Variabile '" + id + "' non dichiarata.");
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
            throw new SemanticException("Variabile '" + id + "' non dichiarata.");
        }
        // Ulteriori controlli sul tipo possono essere aggiunti qui
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

        // Ulteriori controlli sul tipo e sull'operatore possono essere aggiunti qui

        return null;
    }

    @Override
    public Void visit(UnaryExprNode node) throws SemanticException {
        // Visita l'espressione
        node.getExpr().accept(this);

        // Ulteriori controlli sull'operatore possono essere aggiunti qui

        return null;
    }

}