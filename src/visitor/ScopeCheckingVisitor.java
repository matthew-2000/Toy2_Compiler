package visitor;

import nodes.*;
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
    public Void visit(ProgramNode node) throws SemanticException {
        symbolTableManager.enterScope();  // Scope globale

        // Visita ciascun nodo figlio
        if (node.getIterWithoutProcedure() != null) {
            node.getIterWithoutProcedure().accept(this);
        }
        if (node.getProcedure() != null) {
            node.getProcedure().accept(this);
        }
        if (node.getIter() != null) {
            node.getIter().accept(this);
        }

        symbolTableManager.exitScope();  // Esce dallo scope globale
        return null;
    }

    @Override
    public Void visit(VarDeclNode node) throws SemanticException {
        String declaredType = node.getType() != null ? (String) node.getType().accept(this) : null;

        // Ottieni gli identificatori dichiarati
        for (String identifier : node.getIds().getIdentifiers()) {
            // Controlla se l'identificatore esiste già nello scope corrente
            if (!symbolTableManager.addSymbol(identifier, declaredType, false)) {
                throw new SemanticException("Dichiarazione multipla di '" + identifier + "'");
            }
        }

        // Gestisci eventuali costanti per l'inferenza del tipo
        if (node.getConsts() != null) {
            node.getConsts().accept(this);  // Assicurati che i tipi delle costanti siano compatibili
        }

        return null;
    }

    @Override
    public Void visit(FunctionNode node) throws SemanticException {
        symbolTableManager.enterScope();

        // Aggiungi i parametri della funzione alla tabella dei simboli
        node.getParams().accept(this);

        // Visita il corpo della funzione
        node.getBody().accept(this);

        // Controllo del tipo di ritorno
        node.getReturnType().accept(this);

        symbolTableManager.exitScope();
        return null;
    }

    @Override
    public Void visit(ProcedureNode node) throws SemanticException {
        symbolTableManager.enterScope();

        // Aggiungi i parametri della procedura alla tabella dei simboli, specificando `OUT` se necessario
        if (node.getParams() != null) {
            node.getParams().accept(this);
        }

        // Visita il corpo della procedura
        node.getBody().accept(this);

        symbolTableManager.exitScope();
        return null;
    }

    @Override
    public Void visit(FuncParamsNode node) throws SemanticException {
        String paramName = node.getIdentifier();
        String paramType = (String) node.getType().accept(this);

        // Aggiungi il parametro alla tabella dei simboli come variabile
        if (!symbolTableManager.addSymbol(paramName, paramType, false)) {
            throw new SemanticException("Dichiarazione multipla del parametro '" + paramName + "'");
        }

        // Visita i parametri aggiuntivi
        if (node.getOtherParams() != null) {
            node.getOtherParams().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ProcParamsNode node) throws SemanticException {
        String paramName = node.getIdentifier();
        String paramType = (String) node.getType().accept(this);

        // Aggiungi il parametro alla tabella dei simboli e indicane l'uso come parametro OUT se necessario
        if (!symbolTableManager.addSymbol(paramName, paramType, node.isOutParam())) {
            throw new SemanticException("Dichiarazione multipla del parametro '" + paramName + "'");
        }

        // Visita i parametri aggiuntivi
        if (node.getOtherParams() != null) {
            node.getOtherParams().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ProcParamIdNode node) throws SemanticException {
        String paramName = node.getIdentifier();
        boolean isOut = node.isOutParam();

        // Aggiungi il parametro alla tabella dei simboli e specifica se è `OUT`
        if (!symbolTableManager.addSymbol(paramName, "UNKNOWN", isOut)) {
            throw new SemanticException("Dichiarazione multipla del parametro '" + paramName + "'");
        }

        return null;
    }

    @Override
    public Void visit(OtherFuncParamsNode node) throws SemanticException {
        for (FuncParamsNode param : node.getAdditionalParams()) {
            param.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(OtherProcParamsNode node) throws SemanticException {
        for (ProcParamsNode param : node.getAdditionalParams()) {
            param.accept(this);
        }
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
    public Void visit(TypesNode node) throws SemanticException {
        for (TypeNode typeNode : node.getTypes()) {
            typeNode.accept(this);  // Visita ogni tipo per confermarne la validità
        }
        return null;
    }

    @Override
    public String visit(ExprNode node) throws SemanticException {
        if (node.getLiteralType() != null) {
            // Ritorna direttamente il tipo del literal
            return node.getLiteralType();
        } else if (node.getIdentifier() != null) {
            // Verifica l'identificatore
            Symbol symbol = symbolTableManager.lookup(node.getIdentifier());
            if (symbol == null) {
                throw new SemanticException("Identificatore non dichiarato: " + node.getIdentifier());
            }
            return symbol.getType();
        } else if (node.getOperator() != null) {
            String operator = node.getOperator();

            if (node.getLeft() != null && node.getRight() != null) {
                // Operatore binario
                String leftType = (String) node.getLeft().accept(this);
                String rightType = (String) node.getRight().accept(this);

                switch (operator) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        if (leftType.equals("INTEGER") && rightType.equals("INTEGER")) {
                            return "INTEGER";
                        } else {
                            throw new SemanticException("Operatori aritmetici richiedono tipi INTEGER, trovato: " + leftType + " e " + rightType);
                        }
                    case "AND":
                    case "OR":
                        if (leftType.equals("BOOLEAN") && rightType.equals("BOOLEAN")) {
                            return "BOOLEAN";
                        } else {
                            throw new SemanticException("Operatori logici richiedono tipi BOOLEAN, trovato: " + leftType + " e " + rightType);
                        }
                    case "==":
                    case "!=":
                    case "<":
                    case ">":
                    case "<=":
                    case ">=":
                        if (leftType.equals(rightType)) {
                            return "BOOLEAN";
                        } else {
                            throw new SemanticException("Operatori di confronto richiedono tipi compatibili, trovato: " + leftType + " e " + rightType);
                        }
                    default:
                        throw new SemanticException("Operatore non riconosciuto: " + operator);
                }
            } else if (node.getSingleOperand() != null) {
                // Operatore unario
                String operandType = (String) node.getSingleOperand().accept(this);

                switch (operator) {
                    case "MINUS":
                        if (operandType.equals("INTEGER")) {
                            return "INTEGER";
                        } else {
                            throw new SemanticException("L'operatore '-' unario richiede un tipo INTEGER, trovato: " + operandType);
                        }
                    case "NOT":
                        if (operandType.equals("BOOLEAN")) {
                            return "BOOLEAN";
                        } else {
                            throw new SemanticException("L'operatore 'NOT' richiede un tipo BOOLEAN, trovato: " + operandType);
                        }
                    default:
                        throw new SemanticException("Operatore unario non riconosciuto: " + operator);
                }
            }
        }
        throw new SemanticException("Espressione non valida");
    }

    @Override
    public Void visit(FunCallNode node) throws SemanticException {
        String functionName = node.getFunctionName();
        Symbol functionSymbol = symbolTableManager.lookup(functionName);

        if (functionSymbol == null || !functionSymbol.isFunction()) {
            throw new SemanticException("Funzione non dichiarata: " + functionName);
        }

        // Verifica compatibilità dei parametri
        List<String> paramTypes = functionSymbol.getParamTypes();
        List<ExprNode> arguments = node.getArguments();

        if (paramTypes.size() != arguments.size()) {
            throw new SemanticException("Numero di argomenti non corrispondente nella chiamata alla funzione '" + functionName + "'");
        }

        for (int i = 0; i < arguments.size(); i++) {
            String expectedType = paramTypes.get(i);
            String actualType = (String) arguments.get(i).accept(this);

            if (!expectedType.equals(actualType)) {
                throw new SemanticException("Tipo di argomento non corrispondente per la funzione '" + functionName + "'. Atteso: " + expectedType + ", Trovato: " + actualType);
            }
        }

        return null;
    }

    @Override
    public Void visit(ProcCallNode node) throws SemanticException {
        String procedureName = node.getProcedureName();
        Symbol procedureSymbol = symbolTableManager.lookup(procedureName);

        if (procedureSymbol == null || procedureSymbol.isFunction()) {
            throw new SemanticException("Procedura non dichiarata: " + procedureName);
        }

        // Verifica compatibilità dei parametri
        List<String> paramTypes = procedureSymbol.getParamTypes();
        List<Boolean> isOutParams = procedureSymbol.getIsOutParams();
        List<ExprNode> arguments = node.getArguments();

        if (paramTypes.size() != arguments.size()) {
            throw new SemanticException("Numero di argomenti non corrispondente nella chiamata alla procedura '" + procedureName + "'");
        }

        for (int i = 0; i < arguments.size(); i++) {
            String expectedType = paramTypes.get(i);
            boolean isOutParam = isOutParams.get(i);
            ExprNode argument = arguments.get(i);

            // Verifica tipo
            String actualType = (String) argument.accept(this);
            if (!expectedType.equals(actualType)) {
                throw new SemanticException("Tipo di argomento non corrispondente per la procedura '" + procedureName + "'. Atteso: " + expectedType + ", Trovato: " + actualType);
            }

//            // Verifica parametro OUT
//            if (isOutParam && !(argument instanceof IdsNode)) {
//                throw new SemanticException("Parametro OUT deve essere passato per riferimento nella procedura '" + procedureName + "'");
//            }
        }

        return null;
    }

    @Override
    public Void visit(IfStatNode node) throws SemanticException {
        // Verifica che la condizione sia di tipo BOOLEAN
        String conditionType = (String) node.getCondition().accept(this);
        if (!conditionType.equals("BOOLEAN")) {
            throw new SemanticException("La condizione dell'istruzione IF deve essere di tipo BOOLEAN");
        }

        // Entra nello scope per il corpo 'then'
        symbolTableManager.enterScope();
        node.getThenBody().accept(this);
        symbolTableManager.exitScope();

        // Visita ciascun blocco elif, con nuovo scope
        for (ElifNode elifNode : node.getElifNodes()) {
            elifNode.accept(this);
        }

        // Visita il blocco else, se presente
        if (node.getElseNode() != null) {
            node.getElseNode().accept(this);
        }

        return null;
    }

    @Override
    public Void visit(ElifNode node) throws SemanticException {
        // Verifica che la condizione sia di tipo BOOLEAN
        String conditionType = (String) node.getCondition().accept(this);
        if (!conditionType.equals("BOOLEAN")) {
            throw new SemanticException("La condizione dell'istruzione ELIF deve essere di tipo BOOLEAN");
        }

        // Entra nello scope per il corpo 'elif'
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(ElseNode node) throws SemanticException {
        // Entra nello scope per il corpo 'else'
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(ElifsNode node) throws SemanticException {
        for (ElifNode elif : node.getElifBlocks()) {
            elif.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileStatNode node) throws SemanticException {
        // Verifica che la condizione sia di tipo BOOLEAN
        String conditionType = (String) node.getCondition().accept(this);
        if (!conditionType.equals("BOOLEAN")) {
            throw new SemanticException("La condizione del ciclo WHILE deve essere di tipo BOOLEAN");
        }

        // Entra nello scope per il corpo 'while'
        symbolTableManager.enterScope();
        node.getBody().accept(this);
        symbolTableManager.exitScope();

        return null;
    }

    @Override
    public Void visit(BodyNode node) throws SemanticException {
        symbolTableManager.enterScope();

        for (StatNode stat : node.getStatements()) {
            stat.accept(this);
        }

        symbolTableManager.exitScope();
        return null;
    }

    @Override
    public Void visit(StatNode node) throws SemanticException {
        switch (node.getStatementType()) {
            case "ASSIGN":
                // Verifica le assegnazioni
                node.getIds().accept(this);  // Verifica che gli identificatori siano dichiarati
                node.getExpressions().accept(this);  // Verifica e controlla i tipi delle espressioni
                break;

            case "PROC_CALL":
                // Gestisce la chiamata a una procedura
                node.getExpressions().accept(this);
                break;

            // Altri tipi di istruzioni, come RETURN, WRITE, READ, ecc.
        }
        return null;
    }

    @Override
    public Void visit(IOArgsListNode node) throws SemanticException {
        for (IOArgNode ioArg : node.getIoArgs()) {
            ioArg.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IOArgNode node) throws SemanticException {
        // Se l'argomento è un identificatore, verifica che sia stato dichiarato
        if (node.getIdentifier() != null) {
            Symbol symbol = symbolTableManager.lookup(node.getIdentifier());
            if (symbol == null) {
                throw new SemanticException("Identificatore non dichiarato per l'I/O: " + node.getIdentifier());
            }
        }

        // Se è un literal, può essere direttamente accettato
        return null;
    }

    @Override
    public Void visit(IterWithoutProcedureNode node) throws SemanticException {
        for (Visitable item : node.getItems()) {
            item.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IterNode node) throws SemanticException {
        for (Visitable item : node.getItems()) {
            item.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(DeclsNode node) throws SemanticException {
        for (VarDeclNode declNode : node.getDeclarations()) {
            declNode.accept(this);  // Visita ciascuna dichiarazione per aggiungerla alla tabella dei simboli
        }
        return null;
    }

    @Override
    public Void visit(ExprsNode node) throws SemanticException {
        for (ExprNode expr : node.getExpressions()) {
            expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ConstsNode node) throws SemanticException {
        for (ConstNode constant : node.getConstants()) {
            constant.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ProcExprsNode node) throws SemanticException {
        List<ExprNode> arguments = node.getArguments();
        List<Boolean> isRefFlags = node.getIsRef();

        for (int i = 0; i < arguments.size(); i++) {
            ExprNode argument = arguments.get(i);
            boolean isRef = isRefFlags.get(i);

            if (isRef) {
//                // Se è un parametro REF, deve essere un identificatore
//                if (!(argument instanceof IdsNode)) {
//                    throw new SemanticException("Parametro REF deve essere un identificatore");
//                }
//                // Verifica che l'identificatore sia dichiarato
//                String identifier = ((IdsNode) argument).getIdentifiers().get(0);
//                Symbol symbol = symbolTableManager.lookup(identifier);
//                if (symbol == null) {
//                    throw new SemanticException("Parametro REF non dichiarato: " + identifier);
//                }
            } else {
                // Se non è REF, può essere qualsiasi espressione, controlla il tipo
                argument.accept(this);
            }
        }

        return null;
    }

}