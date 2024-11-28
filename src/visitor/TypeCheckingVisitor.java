package visitor;

import nodes.*;
import nodes.expr.*;
import nodes.stat.*;
import visitor.exception.SemanticException;
import visitor.symbolTable.Symbol;
import visitor.symbolTable.SymbolKind;
import visitor.symbolTable.SymbolTable;
import visitor.symbolTable.SymbolTableManager;
import visitor.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class TypeCheckingVisitor implements Visitor {
    private final SymbolTableManager symbolTableManager;
    private SymbolTable currentScope;

    // Costruttore che accetta il SymbolTableManager esistente
    public TypeCheckingVisitor(SymbolTableManager symbolTableManager) {
        this.symbolTableManager = symbolTableManager;
    }

    @Override
    public Type visit(ProgramNode node) throws SemanticException {
        // Effettua il controllo dei tipi dell'intero programma

        currentScope = symbolTableManager.getScope(node);

        // Visita le dichiarazioni senza procedure
        for (Visitable decl : node.getItersWithoutProcedure().getIterList()) {
            decl.accept(this);
        }

        // Visita la procedura principale
        node.getProcedure().accept(this);

        // Visita le restanti dichiarazioni
        for (Visitable decl : node.getIters().getIterList()) {
            decl.accept(this);
        }

        return Type.NOTYPE;
    }

    @Override
    public Type visit(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            iter.accept(this); // Visita ciascun IterWithoutProcedureNode per il controllo dei tipi
        }
        return Type.NOTYPE; // ItersWithoutProcedure non ha un tipo di ritorno
    }

    @Override
    public Type visit(IterWithoutProcedureNode node) throws SemanticException {
        return (Type) node.getDeclaration().accept(this); // Visita la dichiarazione top-level senza procedure
    }

    @Override
    public Type visit(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            iter.accept(this); // Visita ciascun IterNode per il controllo dei tipi
        }
        return Type.NOTYPE; // Iters non ha un tipo di ritorno
    }

    @Override
    public Type visit(IterNode node) throws SemanticException {
        return (Type) node.getDeclaration().accept(this); // Visita la dichiarazione top-level con eventuali procedure
    }

    @Override
    public Type visit(VarDeclNode node) throws SemanticException {
        for (DeclNode decl : node.getDecls()) {
            decl.accept(this); // Visita ogni dichiarazione contenuta nel nodo VarDeclNode
        }
        return Type.NOTYPE;
    }

    @Override
    public Type visit(DeclNode node) throws SemanticException {
        if (node.getConsts() != null) {
            List<Type> declaredTypes = node.getConstsType(); // Ottiene il tipo dichiarato
            List<ConstNode> constNodes = node.getConsts();
            for (int i = 0; i < declaredTypes.size(); i++) {
                Type constType = (Type) constNodes.get(i).accept(this); // Verifica il tipo della costante
                if (!constType.equals(declaredTypes.get(i))) {
                    throw new SemanticException("Type mismatch per '" + constNodes.get(i).getValue() + "'. Atteso: " + declaredTypes.get(i) + ", trovato: " + constType);
                }
            }
        }
        return Type.NOTYPE; // DeclNode non restituisce un tipo
    }

    @Override
    public Type visit(ConstNode node) throws SemanticException {
        Object value = node.getValue();
        if (value instanceof Integer) {
            node.setType(Type.INTEGER);
            return node.getType();
        } else if (value instanceof Double) {
            node.setType(Type.REAL);
            return node.getType();
        } else if (value instanceof Boolean) {
            node.setType(Type.BOOLEAN);
            return node.getType();
        } else if (value instanceof String) {
            node.setType(Type.STRING);
            return node.getType();
        }
        throw new SemanticException("Tipo di costante non riconosciuto per valore: " + value);
    }

    @Override
    public Type visit(FunctionNode node) throws SemanticException {
        // Ottiene i tipi di ritorno e i tipi dei parametri
        List<Type> returnTypes = node.getReturnTypes();

        currentScope = symbolTableManager.getScope(node);

        if (node.getParams() != null) {
            node.getParams().accept(this);
        }

        // Visita il corpo della funzione e ottiene i tipi di ritorno
        List<Type> bodyReturnTypes = (List<Type>) node.getBody().accept(this);

        if (bodyReturnTypes.isEmpty()) {
            throw new SemanticException("Nessun 'return' nella funzione '" + node.getName() + "'.");
        }

        if (!returnTypes.equals(bodyReturnTypes)) {
            throw new SemanticException("Tipo di ritorno non compatibile nella funzione '" + node.getName() +
                    "'. Atteso: " + returnTypes + ", trovato: " + bodyReturnTypes);
        }

        return Type.NOTYPE;
    }

    @Override
    public List<Type> visit(FuncParamsNode node) throws SemanticException {
        List<Type> paramTypes = new ArrayList<>();

        for (ParamNode param : node.getParams()) {
            Type paramType = (Type) param.accept(this); // Ottiene il tipo di ogni parametro
            paramTypes.add(paramType);
        }

        return paramTypes; // Restituisce i tipi dei parametri
    }

    @Override
    public Type visit(ParamNode node) throws SemanticException {
        return node.getType(); // Restituisce il tipo del parametro
    }

    @Override
    public Type visit(ProcedureNode node) throws SemanticException {
        if (node.getName().equals("main")) {
            // Controlla che la procedura main non abbia parametri
            if (node.getParams() != null && !node.getParams().getParams().isEmpty()) {
                throw new SemanticException("La procedura 'main' non deve avere parametri.");
            }
        }

        currentScope = symbolTableManager.getScope(node);

        if (node.getParams() != null) {
            node.getParams().accept(this);
        }

        // Visita il corpo della procedura
        List<Type> bodyReturnTypes = (List<Type>) node.getBody().accept(this);

        if (!bodyReturnTypes.isEmpty()) {
            throw new SemanticException("La procedura '" + node.getName() + "' non deve contenere istruzioni 'return'.");
        }

        return Type.NOTYPE;
    }

    @Override
    public Type visit(ProcParamsNode node) throws SemanticException {
        // Visita ogni parametro della procedura
        for (ProcParamNode param : node.getParams()) {
            param.accept(this);
        }
        return Type.NOTYPE;
    }

    @Override
    public Type visit(ProcParamNode node) throws SemanticException {
        return node.getType();
    }

    @Override
    public List<Type> visit(BodyNode node) throws SemanticException {
        List<Type> returnTypes = new ArrayList<>();
        boolean hasReturn = false;

        for (Visitable statement : node.getStatements()) {
            Object result = statement.accept(this);

            // Se l'istruzione è un 'return' o contiene un 'return', elaboriamo i tipi di ritorno
            if (result instanceof List<?>) {
                List<Type> stmtReturnTypes = (List<Type>) result;

                if (!stmtReturnTypes.isEmpty()) {
                    if (!hasReturn) {
                        // Primo 'return' trovato
                        returnTypes = stmtReturnTypes;
                        hasReturn = true;
                    } else {
                        // Controlliamo la consistenza dei tipi di ritorno
                        if (!returnTypes.equals(stmtReturnTypes)) {
                            throw new SemanticException("Tipi di ritorno incoerenti nel corpo.");
                        }
                    }
                }
            }
        }
        return returnTypes; // Restituiamo i tipi di ritorno raccolti (potrebbe essere vuoto)
    }

    @Override
    public List<Type> visit(AssignStatNode node) throws SemanticException {
        // Visita gli identificatori e le espressioni
        List<String> ids = node.getIds();
        List<ExprNode> exprs = node.getExprs();

        // Variabile per tracciare il numero totale di espressioni e valori restituiti
        int totalExpressions = 0;

        // Itera su tutte le espressioni
        for (ExprNode expr : exprs) {
            if (expr instanceof FunCallNode) {
                FunCallNode funcCall = (FunCallNode) expr;

                // Ottieni i tipi restituiti dalla funzione
                List<Type> returnTypes = (List<Type>) funcCall.accept(this);

                // Incrementa il conteggio totale con i valori restituiti dalla funzione
                totalExpressions += returnTypes.size();
            } else {
                // Ogni espressione standard conta come un singolo valore
                totalExpressions++;
            }
        }

        // Confronta il numero totale di valori con il numero di identificatori
        if (totalExpressions != ids.size()) {
            throw new SemanticException("Numero totale di valori restituiti ed espressioni (" + totalExpressions +
                    ") non corrisponde al numero di identificatori (" + ids.size() + ").");
        }

        // Controlla la compatibilità dei tipi per ciascun identificatore
        int idIndex = 0;
        for (ExprNode expr : exprs) {
            if (expr instanceof FunCallNode) {
                FunCallNode funcCall = (FunCallNode) expr;

                // Ottieni i tipi restituiti dalla funzione
                List<Type> returnTypes = (List<Type>) funcCall.accept(this);

                for (Type returnType : returnTypes) {
                    String id = ids.get(idIndex);
                    Symbol symbol = currentScope.lookup(id);
                    Type idType = symbol.getType();

                    if (idType != returnType) {
                        throw new SemanticException("Tipo non compatibile per '" + id + "': atteso " + idType +
                                ", trovato " + returnType + ".");
                    }

                    // Controllo dell'immutabilità se l'identificatore è un parametro
                    if (symbol.getKind() == SymbolKind.VARIABLE && symbol.isParameter()) {
                        throw new SemanticException("Parametro '" + id + "' è immutabile e non può essere assegnato.");
                    }
                    node.setIsOutId(symbol.isOut());
                    idIndex++;
                }
            } else {
                // Caso standard: espressione singola restituisce un tipo
                String id = ids.get(idIndex);
                Symbol symbol = currentScope.lookup(id);
                Type exprType = (Type) expr.accept(this);
                Type idType = symbol.getType();

                if (idType != exprType) {
                    throw new SemanticException("Assegnazione a '" + id + "' non compatibile: " + idType + " := " + exprType);
                }

                // Controllo dell'immutabilità se l'identificatore è un parametro
                if (symbol.getKind() == SymbolKind.VARIABLE && symbol.isParameter()) {
                    throw new SemanticException("Parametro '" + id + "' è immutabile e non può essere assegnato.");
                }
                node.setIsOutId(symbol.isOut());
                idIndex++;
            }
        }

        return new ArrayList<>(); // Restituiamo una lista vuota di tipi di ritorno
    }

    @Override
    public List<Type> visit(ProcCallStatNode node) throws SemanticException {
        node.getProcCall().accept(this);
        return new ArrayList<>(); // Restituiamo una lista vuota di tipi di ritorno
    }

    @Override
    public List<Type> visit(ReturnStatNode node) throws SemanticException {
        List<ExprNode> exprs = node.getExprs();
        List<Type> returnTypes = new ArrayList<>();

        for (ExprNode expr : exprs) {
            if (expr instanceof FunCallNode) {
                List<Type> funReturnTypes = (List<Type>) expr.accept(this);
                returnTypes.addAll(funReturnTypes);
            } else {
                Type exprType = (Type) expr.accept(this);
                returnTypes.add(exprType);
            }
        }

        return returnTypes;
    }

    @Override
    public List<Type> visit(WriteStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            arg.accept(this);
        }
        return new ArrayList<>(); // Restituiamo una lista vuota di tipi di ritorno
    }

    @Override
    public List<Type> visit(WriteReturnStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            arg.accept(this);
        }
        return new ArrayList<>(); // Restituiamo una lista vuota di tipi di ritorno
    }

    @Override
    public List<Type> visit(ReadStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            Type argType = (Type) arg.accept(this);
            if (argType != Type.STRING && argType != Type.INTEGER && argType != Type.REAL) {
                throw new SemanticException("Errore di tipo in 'read': tipo non valido.");
            }
        }
        return new ArrayList<>(); // Restituiamo una lista vuota di tipi di ritorno
    }

    @Override
    public List<Type> visit(IfStatNode node) throws SemanticException {
        // Verifica della condizione
        currentScope = symbolTableManager.getScope(node);

        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione nell'istruzione IF deve essere di tipo BOOLEAN, trovato: " + conditionType);
        }

        // Blocchi 'then'
        List<Type> thenReturnTypes = (List<Type>) node.getThenBody().accept(this);

        // Blocchi 'elif'
        List<List<Type>> elifsReturnTypes = new ArrayList<>();
        for (ElifNode elifNode : node.getElifBlocks()) {
            List<Type> elifReturnTypes = (List<Type>) elifNode.accept(this);
            elifsReturnTypes.add(elifReturnTypes);
        }

        // Blocco 'else'
        List<Type> elseReturnTypes = new ArrayList<>();
        if (node.getElseBlock() != null) {
            elseReturnTypes = (List<Type>) node.getElseBlock().accept(this);
        }

        // Verifica se tutti i rami hanno un 'return' con tipi coerenti
        boolean allBranchesReturn = !thenReturnTypes.isEmpty();

        for (List<Type> elifReturnType : elifsReturnTypes) {
            if (elifReturnType.isEmpty()) {
                allBranchesReturn = false;
                break;
            }
        }

        if (node.getElseBlock() != null && elseReturnTypes.isEmpty()) {
            allBranchesReturn = false;
        }

        if (allBranchesReturn) {
            // Controlla la coerenza dei tipi di ritorno tra tutti i rami
            List<Type> firstReturnTypes = thenReturnTypes;
            for (List<Type> elifReturnType : elifsReturnTypes) {
                if (!firstReturnTypes.equals(elifReturnType)) {
                    throw new SemanticException("Tipi di ritorno incoerenti tra i rami nell'istruzione IF.");
                }
            }
            if (!firstReturnTypes.equals(elseReturnTypes)) {
                throw new SemanticException("Tipi di ritorno incoerenti tra i rami nell'istruzione IF.");
            }
            return firstReturnTypes;
        } else {
            // Non tutti i rami hanno un 'return'
            return new ArrayList<>(); // Lista vuota di tipi di ritorno
        }
    }

    @Override
    public List<Type> visit(WhileStatNode node) throws SemanticException {
        currentScope = symbolTableManager.getScope(node);

        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione nell'istruzione WHILE deve essere di tipo BOOLEAN, trovato: " + conditionType);
        }

        return (List<Type>) node.getBody().accept(this);
    }

    @Override
    public List<Type> visit(FunCallNode node) throws SemanticException {
        String functionName = node.getFunctionName();
        Symbol functionSymbol = currentScope.lookup(functionName);

        if (functionSymbol.getKind() != SymbolKind.FUNCTION) {
            throw new SemanticException("'" + functionName + "' non è una funzione.");
        }

        List<Type> expectedParamTypes = functionSymbol.getParamTypes();
        List<ExprNode> arguments = node.getArguments();

        if (expectedParamTypes.size() != arguments.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla funzione '" + functionName + "'.");
        }

        for (int i = 0; i < arguments.size(); i++) {
            Type argType = (Type) arguments.get(i).accept(this);
            if (argType != expectedParamTypes.get(i)) {
                throw new SemanticException("Tipo dell'argomento " + (i + 1) + " non compatibile nella chiamata a '" + functionName + "'. Atteso: " + expectedParamTypes.get(i) + ", trovato: " + argType);
            }
        }

        node.setReturnTypes(functionSymbol.getReturnTypes());
        return functionSymbol.getReturnTypes(); // Restituisce il tipo di ritorno della funzione
    }

    @Override
    public Type visit(ProcCallNode node) throws SemanticException {
        String procedureName = node.getProcedureName();
        Symbol procedureSymbol = currentScope.lookup(procedureName);
        if (procedureSymbol.getKind() != SymbolKind.PROCEDURE) {
            throw new SemanticException("'" + procedureName + "' non è una procedura.");
        }

        List<Type> expectedParamTypes = procedureSymbol.getParamTypes();
        List<Boolean> isOutParams = procedureSymbol.getIsOutParams();
        List<ProcExprNode> arguments = node.getArguments();

        if (expectedParamTypes.size() != arguments.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla procedura '" + procedureName + "'.");
        }

        for (int i = 0; i < arguments.size(); i++) {
            ProcExprNode procExprNode = arguments.get(i);
            Type argType = (Type) procExprNode.getExpr().accept(this);
            boolean isRef = procExprNode.isRef();

            if (argType != expectedParamTypes.get(i)) {
                throw new SemanticException("Tipo dell'argomento " + (i + 1) + " non compatibile nella chiamata a '" + procedureName + "'. Atteso: " + expectedParamTypes.get(i) + ", trovato: " + argType);
            }

            if (isRef != isOutParams.get(i)) {
                throw new SemanticException("L'argomento " + (i + 1) + " deve essere " + (isOutParams.get(i) ? "passato per riferimento (REF)" : "passato per valore") + " nella chiamata alla procedura '" + procedureName + "'.");
            }
        }

        return Type.NOTYPE;
    }

    @Override
    public List<Type> visit(ElifNode node) throws SemanticException {
        currentScope = symbolTableManager.getScope(node);

        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione nell'istruzione ELIF deve essere di tipo BOOLEAN, trovato: " + conditionType);
        }

        // Visita il corpo dell'ELIF
        return (List<Type>) node.getBody().accept(this);
    }

    @Override
    public List<Type> visit(ElseNode node) throws SemanticException {
        currentScope = symbolTableManager.getScope(node);
        return (List<Type>) node.getBody().accept(this);
    }

    @Override
    public Type visit(IOArgIdentifierNode node) throws SemanticException {
        // Verifica che l'identificatore esista nella SymbolTable e ottieni il suo tipo
        Symbol symbol = currentScope.lookup(node.getIdentifier());
        node.setType(symbol.getType());
        return symbol.getType(); // Ritorna il tipo dell'identificatore
    }

    @Override
    public Type visit(IOArgStringLiteralNode node) throws SemanticException {
        // Un literal di stringa è sempre di tipo STRING
        return Type.STRING;
    }

    @Override
    public Type visit(IOArgBinaryNode node) throws SemanticException {
        // Verifica il tipo del nodo sinistro e destro dell'operazione binaria
        Type leftType = (Type) node.getLeft().accept(this);
        Type rightType = (Type) node.getRight().accept(this);

        // L'operatore "+" è valido solo se entrambi gli argomenti sono STRING o entrambi INTEGER
        if ((leftType == Type.STRING && rightType == Type.STRING) || (leftType == Type.INTEGER && rightType == Type.INTEGER)) {
            node.setType(leftType);
            return leftType; // Il tipo di ritorno è coerente con il tipo degli argomenti
        } else {
            throw new SemanticException("Operatore '+' non applicabile ai tipi " + leftType + " e " + rightType);
        }
    }

    @Override
    public Type visit(DollarExprNode node) throws SemanticException {
        // Visita l'espressione contenuta e ritorna il tipo

        ExprNode exprNode = node.getExpr();
        if (exprNode instanceof FunCallNode) {
            FunCallNode funCallNode = (FunCallNode) exprNode;
            List<Type> returnTypes = (List<Type>) funCallNode.accept(this);
            if (returnTypes == null || returnTypes.isEmpty()) {
                Symbol symbol = currentScope.lookup(funCallNode.getFunctionName());
                if (symbol.getReturnTypes() == null || symbol.getReturnTypes().isEmpty()) {
                    throw new SemanticException("La funzione " + funCallNode.getFunctionName() + " non ha tipi di ritorno.");
                } else {
                    node.setType(symbol.getReturnTypes().get(0));
                    return symbol.getReturnTypes().get(0);
                }
            }
            if (returnTypes.size() != 1) {
                throw new SemanticException("La funzione " + funCallNode.getFunctionName() + " ha più tipi di ritorno.");
            }
            node.setType(returnTypes.get(0));
            return returnTypes.get(0);
        } else {
            node.setType((Type) node.getExpr().accept(this));
            return node.getType();
        }
    }

    @Override
    public Type visit(ProcExprNode node) throws SemanticException {
        // Verifica il tipo dell'espressione interna
        Type exprType = (Type) node.getExpr().accept(this);

        // Se è un parametro passato per riferimento (REF), deve essere un identificatore e non un'espressione complessa
        if (node.isRef() && !(node.getExpr() instanceof IdentifierNode)) {
            throw new SemanticException("Un parametro 'REF' deve essere un identificatore, trovato: " + node.getExpr());
        }

        return exprType;
    }

    @Override
    public Type visit(RealConstNode node) throws SemanticException {
        // Una costante reale ha sempre tipo REAL
        return Type.REAL;
    }

    @Override
    public Type visit(IntConstNode node) throws SemanticException {
        // Una costante intera ha sempre tipo INTEGER
        return Type.INTEGER;
    }

    @Override
    public Type visit(StringConstNode node) throws SemanticException {
        // Una costante stringa ha sempre tipo STRING
        return Type.STRING;
    }

    @Override
    public Type visit(IdentifierNode node) throws SemanticException {
        // Recupera il tipo dell'identificatore dalla SymbolTable
        Symbol symbol = currentScope.lookup(node.getName());
        if (symbol == null) {
            throw new SemanticException("Identificatore '" + node.getName() + "' non dichiarato.");
        }
        node.setIsOutInProcedure(symbol.isOut());
        node.setType(symbol.getType());
        return symbol.getType(); // Ritorna il tipo dell'identificatore
    }

    @Override
    public Type visit(BooleanConstNode node) throws SemanticException {
        // Una costante booleana ha sempre tipo BOOLEAN
        node.setType(Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    @Override
    public Type visit(BinaryExprNode node) throws SemanticException {
        // Ottieni i tipi degli operandi sinistro e destro
        Object leftResult = node.getLeft().accept(this);
        Object rightResult = node.getRight().accept(this);

        Type leftType;
        Type rightType;

        // Controlla se il risultato del nodo sinistro è una lista di tipi
        if (leftResult instanceof List<?>) {
            List<?> leftTypeList = (List<?>) leftResult;
            if (leftTypeList.size() != 1) {
                throw new SemanticException("Espressione sinistra restituisce più di un tipo.");
            }
            leftType = (Type) leftTypeList.get(0);
        } else {
            leftType = (Type) leftResult;
        }

        // Controlla se il risultato del nodo destro è una lista di tipi
        if (rightResult instanceof List<?>) {
            List<?> rightTypeList = (List<?>) rightResult;
            if (rightTypeList.size() != 1) {
                throw new SemanticException("Espressione destra restituisce più di un tipo.");
            }
            rightType = (Type) rightTypeList.get(0);
        } else {
            rightType = (Type) rightResult;
        }

        // Determina il tipo dell'operatore
        String operator = node.getOperator();

        // Regole di type-checking basate sull'operatore
        switch (operator) {
            case "+":
                if ((leftType == Type.STRING && (rightType == Type.STRING || rightType == Type.INTEGER || rightType == Type.REAL)) ||
                        ((leftType == Type.INTEGER || leftType == Type.REAL) && rightType == Type.STRING)) {
                    node.setType(Type.STRING);
                    return Type.STRING; // Concatenazione tra stringhe e numeri è valida
                } else if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                    node.setType(Type.INTEGER);
                    return Type.INTEGER;
                } else if ((leftType == Type.INTEGER && rightType == Type.REAL) ||
                        (leftType == Type.REAL && rightType == Type.INTEGER) ||
                        (leftType == Type.REAL && rightType == Type.REAL)) {
                    node.setType(Type.REAL);
                    return Type.REAL;
                }
                throw new SemanticException("Operatore '+' non applicabile ai tipi " + leftType + " e " + rightType);

            case "-":
            case "*":
            case "/":
                if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                    if (operator.equals("/")) {
                        node.setType(Type.REAL);
                        return Type.REAL;
                    } else {
                        node.setType(Type.INTEGER);
                        return Type.INTEGER;
                    }
                } else if ((leftType == Type.INTEGER && rightType == Type.REAL) ||
                        (leftType == Type.REAL && rightType == Type.INTEGER) ||
                        (leftType == Type.REAL && rightType == Type.REAL)) {
                    node.setType(Type.REAL);
                    return Type.REAL;
                }
                throw new SemanticException("Operatore '" + operator + "' non applicabile ai tipi " + leftType + " e " + rightType);

            case "and":
            case "or":
                if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                }
                throw new SemanticException("Operatore '" + operator + "' richiede tipi BOOLEAN.");

            case ">":
            case ">=":
            case "<":
            case "<=":
            case "=":
            case "!=":
                if ((leftType == Type.INTEGER && rightType == Type.INTEGER) ||
                        (leftType == Type.REAL && rightType == Type.REAL) ||
                        (leftType == Type.INTEGER && rightType == Type.REAL) ||
                        (leftType == Type.REAL && rightType == Type.INTEGER)) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                } else if (leftType == Type.STRING && rightType == Type.STRING && (operator.equals("=") || operator.equals("!="))) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN; // Comparazione tra stringhe valida solo con "=" e "!="
                } else if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN && (operator.equals("=") || operator.equals("!="))) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN; // Comparazione tra booleani con "=" o "!="
                }
                throw new SemanticException("Operatore '" + operator + "' non applicabile ai tipi " + leftType + " e " + rightType);

            default:
                throw new SemanticException("Operatore non riconosciuto: " + operator);
        }
    }

    @Override
    public Type visit(UnaryExprNode node) throws SemanticException {
        // Ottieni il tipo dell'espressione
        Type exprType = (Type) node.getExpr().accept(this);
        String operator = node.getOperator();

        // Regole di type-checking basate sull'operatore unario
        switch (operator) {
            case "uminus":
                if (exprType == Type.INTEGER || exprType == Type.REAL) {
                    node.setType(exprType);
                    return exprType; // Ritorna lo stesso tipo (INTEGER o REAL)
                }
                throw new SemanticException("Operatore 'uminus' applicabile solo a INTEGER o REAL.");

            case "not":
                if (exprType == Type.BOOLEAN) {
                    node.setType(exprType);
                    return Type.BOOLEAN;
                }
                throw new SemanticException("Operatore 'not' applicabile solo a BOOLEAN.");

            default:
                throw new SemanticException("Operatore unario non riconosciuto: " + operator);
        }
    }
}