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

    // Costruttore che accetta la SymbolTableManager esistente
    public TypeCheckingVisitor(SymbolTableManager symbolTableManager) {
        this.symbolTableManager = symbolTableManager;
    }

    @Override
    public Type visit(ProgramNode node) throws SemanticException {
        // Effettua il controllo dei tipi dell'intero programma

        currentScope = symbolTableManager.getScope(node);

        for (Visitable decl : node.getItersWithoutProcedure().getIterList()) {
            decl.accept(this);
        }

        node.getProcedure().accept(this);

        for (Visitable decl : node.getIters().getIterList()) {
            decl.accept(this);
        }

        return null;
    }

    @Override
    public Type visit(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            iter.accept(this); // Visita ciascun IterWithoutProcedureNode per il controllo dei tipi
        }
        return null; // ItersWithoutProcedure non ha un tipo di ritorno
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
        return null; // Iters non ha un tipo di ritorno
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
        return null; // VarDeclNode non restituisce un tipo
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
        return null; // DeclNode non restituisce un tipo
    }

    @Override
    public Type visit(ConstNode node) throws SemanticException {
        Object value = node.getValue();
        if (value instanceof Integer) {
            return Type.INTEGER;
        } else if (value instanceof Double) {
            return Type.REAL;
        } else if (value instanceof Boolean) {
            return Type.BOOLEAN;
        } else if (value instanceof String) {
            return Type.STRING;
        }
        throw new SemanticException("Tipo di costante non riconosciuto per valore: " + value);
    }

    @Override
    public Type visit(FunctionNode node) throws SemanticException {
        // Ottiene i tipi di ritorno e i tipi dei parametri
        List<Type> returnTypes = node.getReturnTypes();
        List<Type> paramTypes = new ArrayList<>();

        currentScope = symbolTableManager.getScope(node);

        if (node.getParams() != null) {
            node.getParams().accept(this);
        }

        // Visita il corpo della funzione, che deve restituire un tipo compatibile con il tipo di ritorno
        List<Type> bodyType = (List<Type>) node.getBody().accept(this);
        if (!returnTypes.equals(bodyType)) {
            throw new SemanticException("Tipo di ritorno non compatibile nella funzione '" + node.getName() + "'. Atteso: " + returnTypes + ", trovato: " + bodyType);
        }

        return null;
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
            // Controlla che la procedura main abbia determinati parametri o nessuno
            if (node.getParams() != null && !node.getParams().getParams().isEmpty()) {
                throw new SemanticException("La procedura 'main' non deve avere parametri.");
            }
        }

        // Ottieni i tipi dei parametri e i flag isOut per la procedura
        List<Type> paramTypes = new ArrayList<>();
        List<Boolean> isOutParams = new ArrayList<>();

        currentScope = symbolTableManager.getScope(node);

        // Visita i parametri e raccoglie i loro tipi e flag isOut
        if (node.getParams() != null) {
            node.getParams().accept(this);
            for (ProcParamNode param : node.getParams().getParams()) {
                paramTypes.add(param.getType());
                isOutParams.add(param.isOut());
            }
        }

        // Visita il corpo della procedura (non è previsto un tipo di ritorno)
        node.getBody().accept(this);

        return null; // Le procedure non restituiscono un tipo direttamente
    }

    @Override
    public Type visit(ProcParamsNode node) throws SemanticException {
        // Visita ogni parametro della procedura
        for (ProcParamNode param : node.getParams()) {
            param.accept(this);
        }
        return null; // Non c'è un tipo di ritorno per un gruppo di parametri
    }

    @Override
    public Type visit(ProcParamNode node) throws SemanticException {
        return node.getType();
    }

    @Override
    public List<Type> visit(BodyNode node) throws SemanticException {
        // Itera attraverso ogni dichiarazione o istruzione nel corpo
        for (Visitable statement : node.getStatements()) {

            if (statement instanceof ReturnStatNode) {
                return (List<Type>) statement.accept(this);
            }

            Type type = (Type) statement.accept(this); // Visita ogni dichiarazione/istruzione
            // Controlla che ogni istruzione sia valida secondo le regole del tipo
            if (type != null && type != Type.UNKNOWN) {
                throw new SemanticException("Errore di tipo nel corpo della procedura/funzione.");
            }
        }
        return null; // Il corpo non ha un tipo di ritorno specifico
    }

    @Override
    public Type visit(AssignStatNode node) throws SemanticException {
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
                    Type idType = currentScope.lookup(id).getType();

                    if (idType != returnType) {
                        throw new SemanticException("Tipo non compatibile per '" + id + "': atteso " + idType +
                                ", trovato " + returnType + ".");
                    }

                    // Controllo dell'immutabilità se l'identificatore è un parametro
                    Symbol symbol = currentScope.lookup(id);
                    if (symbol.getKind() == SymbolKind.VARIABLE && symbol.isParameter()) {
                        throw new SemanticException("Parametro '" + id + "' è immutabile e non può essere assegnato.");
                    }

                    idIndex++;
                }
            } else {
                // Caso standard: espressione singola restituisce un tipo
                String id = ids.get(idIndex);
                Type exprType = (Type) expr.accept(this);
                Type idType = currentScope.lookup(id).getType();

                if (idType != exprType) {
                    throw new SemanticException("Assegnazione a '" + id + "' non compatibile: " + idType + " := " + exprType);
                }

                // Controllo dell'immutabilità se l'identificatore è un parametro
                Symbol symbol = currentScope.lookup(id);
                if (symbol.getKind() == SymbolKind.VARIABLE && symbol.isParameter()) {
                    throw new SemanticException("Parametro '" + id + "' è immutabile e non può essere assegnato.");
                }

                idIndex++;
            }
        }

        return Type.UNKNOWN; // L'assegnazione non ha un tipo di ritorno
    }

    @Override
    public Type visit(ProcCallStatNode node) throws SemanticException {
        String procName = node.getProcCall().getProcedureName();
        Symbol symbol = currentScope.lookup(procName);

        if (symbol.getKind() != SymbolKind.PROCEDURE) {
            throw new SemanticException("Procedura '" + procName + "' errore.");
        }

        // Verifica i parametri
        List<Type> paramTypes = symbol.getParamTypes();
        List<ProcExprNode> args = node.getProcCall().getArguments();
        if (paramTypes.size() != args.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla procedura '" + procName + "'.");
        }

        for (int i = 0; i < args.size(); i++) {
            Type argType = (Type) args.get(i).accept(this);
            if (argType != paramTypes.get(i)) {
                throw new SemanticException("Tipo dell'argomento " + (i + 1) + " non compatibile nella chiamata a '" + procName + "'.");
            }
        }
        return null;
    }

    @Override
    public List<Type> visit(ReturnStatNode node) throws SemanticException {
        List<ExprNode> exprs = node.getExprs();
        List<Type> returnTypes = new ArrayList<>();
        for (ExprNode expr : exprs) {
            Type exprType = (Type) expr.accept(this);
            if (exprType == Type.ERROR) {
                throw new SemanticException("Tipo errato nell'espressione di ritorno.");
            }
            returnTypes.add(exprType);
        }
        return returnTypes; // Non ha un tipo specifico
    }

    @Override
    public Type visit(WriteStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            Type argType = (Type) arg.accept(this);
            if (argType == Type.ERROR) {
                throw new SemanticException("Errore di tipo in 'write'.");
            }
        }
        return null;
    }

    @Override
    public Type visit(WriteReturnStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            Type argType = (Type) arg.accept(this);
            if (argType == Type.ERROR) {
                throw new SemanticException("Errore di tipo in 'writereturn'.");
            }
        }
        return null;
    }

    @Override
    public Type visit(ReadStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            Type argType = (Type) arg.accept(this);
            if (argType != Type.STRING && argType != Type.INTEGER && argType != Type.REAL) {
                throw new SemanticException("Errore di tipo in 'read': tipo non valido.");
            }
        }
        return null;
    }

    @Override
    public Type visit(IfStatNode node) throws SemanticException {

        currentScope = symbolTableManager.getScope(node);

        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione dell'if deve essere di tipo boolean.");
        }

        node.getThenBody().accept(this);

        for (ElifNode elifNode : node.getElifBlocks()) {
            elifNode.accept(this);
        }

        if (node.getElseBlock() != null) {
            node.getElseBlock().accept(this);
        }
        return null;
    }

    @Override
    public Type visit(WhileStatNode node) throws SemanticException {

        currentScope = symbolTableManager.getScope(node);

        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione del while deve essere di tipo boolean.");
        }

        node.getBody().accept(this);
        return null;
    }

    @Override
    public List<Type> visit(FunCallNode node) throws SemanticException {
        String functionName = node.getFunctionName();
        Symbol functionSymbol = currentScope.lookup(functionName);

        if (functionSymbol.getKind() != SymbolKind.FUNCTION) {
            throw new SemanticException("Funzione '" + functionName + "' errore.");
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
            throw new SemanticException("Procedura '" + procedureName + "' errore.");
        }

        List<Type> expectedParamTypes = procedureSymbol.getParamTypes();
        List<Boolean> isOutParams = procedureSymbol.getIsOutParams();
        List<ProcExprNode> arguments = node.getArguments();

        if (expectedParamTypes.size() != arguments.size()) {
            throw new SemanticException("Numero di argomenti errato nella chiamata alla procedura '" + procedureName + "'.");
        }

        for (int i = 0; i < arguments.size(); i++) {
            Type argType = (Type) arguments.get(i).getExpr().accept(this);
            boolean isRef = arguments.get(i).isRef();

            if (argType != expectedParamTypes.get(i)) {
                throw new SemanticException("Tipo dell'argomento " + (i + 1) + " non compatibile nella chiamata a '" + procedureName + "'. Atteso: " + expectedParamTypes.get(i) + ", trovato: " + argType);
            }

            if (isRef != isOutParams.get(i)) {
                throw new SemanticException("L'argomento " + (i + 1) + " deve essere " + (isOutParams.get(i) ? "passato per riferimento (REF)" : "passato per valore") + " nella chiamata alla procedura '" + procedureName + "'.");
            }
        }

        return null;
    }

    @Override
    public Type visit(ElifNode node) throws SemanticException {

        currentScope = symbolTableManager.getScope(node);

        // Controllo del tipo della condizione
        Type conditionType = (Type) node.getCondition().accept(this);
        if (conditionType != Type.BOOLEAN) {
            throw new SemanticException("La condizione nell'istruzione ELIF deve essere di tipo BOOLEAN, trovato: " + conditionType);
        }

        // Visita il corpo dell'ELIF
        node.getBody().accept(this);

        return null;
    }

    @Override
    public Type visit(ElseNode node) throws SemanticException {
        currentScope = symbolTableManager.getScope(node);

        // Visita il corpo dell'ELSE
        node.getBody().accept(this);
        return null;
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
            List<Type> returnTypes = funCallNode.getReturnTypes();
            if (returnTypes == null || returnTypes.isEmpty()) {
                Symbol symbol = currentScope.lookup(funCallNode.getFunctionName());
                if (symbol.getReturnTypes() == null || symbol.getReturnTypes().isEmpty()) {
                    throw new SemanticException("La funzione " + funCallNode.getFunctionName() + " non ha tipi di ritorno. ");
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
                throw new SemanticException("Operator '+' non applicabile ai tipi " + leftType + " e " + rightType);

            case "-":
            case "*":
            case "/":
                if (leftType == Type.INTEGER && rightType == Type.INTEGER) {
                    if (operator.equals("/")){
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
                throw new SemanticException("Operator '" + operator + "' non applicabile ai tipi " + leftType + " e " + rightType);

            case "and":
            case "or":
                if (leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                }
                throw new SemanticException("Operator '" + operator + "' richiede tipi BOOLEAN.");

            case ">":
            case ">=":
            case "<":
            case "<=":
            case "==":
            case "!=":
                if ((leftType == Type.INTEGER && rightType == Type.INTEGER) ||
                        (leftType == Type.REAL && rightType == Type.REAL) ||
                        (leftType == Type.INTEGER && rightType == Type.REAL) ||
                        (leftType == Type.REAL && rightType == Type.INTEGER)) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN;
                } else if (leftType == Type.STRING && rightType == Type.STRING && operator.equals("==")) {
                    node.setType(Type.BOOLEAN);
                    return Type.BOOLEAN; // Comparazione tra stringhe valida solo con "=="
                }
                throw new SemanticException("Operator '" + operator + "' non applicabile ai tipi " + leftType + " e " + rightType);

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
                throw new SemanticException("Operator 'uminus' applicabile solo a INTEGER o REAL.");

            case "not":
                if (exprType == Type.BOOLEAN) {
                    node.setType(exprType);
                    return Type.BOOLEAN;
                }
                throw new SemanticException("Operator 'not' applicabile solo a BOOLEAN.");

            default:
                throw new SemanticException("Operatore unario non riconosciuto: " + operator);
        }
    }



}
