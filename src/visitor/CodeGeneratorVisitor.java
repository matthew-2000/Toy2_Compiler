package visitor;

import nodes.*;
import nodes.stat.*;
import nodes.expr.*;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.*;

public class CodeGeneratorVisitor implements Visitor<Object> {
    private StringBuilder code;
    private int indentLevel;
    private int tempVarCounter = 0;
    private List<String> functionPrototypes = new ArrayList<>();
    private List<DeclNode> globalDeclarations = new ArrayList<>();
    private Deque<FunctionNode> functionStack = new ArrayDeque<>();
    private List<String> globalInitializations = new ArrayList<>();
    private List<String> globalDeallocations = new ArrayList<>();
    private boolean isGlobalScope = true;

    public CodeGeneratorVisitor() {
        this.code = new StringBuilder();
        this.indentLevel = 0;
    }

    // Utility methods for indentation
    private void indent() {
        code.append("    ".repeat(Math.max(0, indentLevel))); // 4 spaces per indent level
    }

    private void increaseIndent() {
        indentLevel++;
    }

    private void decreaseIndent() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }

    // Getter for the generated code
    public String getCode() {
        return code.toString();
    }

    private String getNextTempVar() {
        return "tmp_" + (tempVarCounter++);
    }

    private void addGlobalInitialization(String id, String constantValue, boolean hasValue) {
        StringBuilder initCode = new StringBuilder();

        if (hasValue) {
            // Inizializzazione con valore
            initCode.append(id).append(" = malloc(strlen(").append(constantValue).append(") + 1);\n");
            initCode.append("    if (!").append(id).append(") {\n");
            initCode.append("        fprintf(stderr, \"Errore: allocazione fallita\\n\");\n");
            initCode.append("        exit(1);\n");
            initCode.append("    }\n");
            initCode.append("    strcpy(").append(id).append(", ").append(constantValue).append(");");
        } else {
            // Inizializzazione senza valore (allocazione standard)
            initCode.append(id).append(" = malloc(sizeof(char) * 256);\n");
            initCode.append("    if (!").append(id).append(") {\n");
            initCode.append("        fprintf(stderr, \"Errore: allocazione fallita\\n\");\n");
            initCode.append("        exit(1);\n");
            initCode.append("    }\n");
            initCode.append("    strcpy(").append(id).append(", \"\");");
        }

        globalInitializations.add(initCode.toString());

        // Aggiungi il codice per la deallocazione
        String deallocCode = "if (" + id + ") {\n";
        deallocCode += "        free(" + id + ");\n";
        deallocCode += "        " + id + " = NULL;\n";
        deallocCode += "    }";
        globalDeallocations.add(deallocCode);
    }

    @Override
    public Object visit(ProgramNode node) throws SemanticException {
        // Include necessary standard libraries
        code.append("#include <stdio.h>\n");
        code.append("#include <stdlib.h>\n");
        code.append("#include <stdbool.h>\n");
        code.append("#include <string.h>\n");
        code.append("\n");

        // Rileva e raccogli tutte le dichiarazioni globali
        if (node.getItersWithoutProcedure() != null) {
            collectGlobalDeclarations(node.getItersWithoutProcedure());
        }

        if (node.getIters() != null) {
            collectGlobalDeclarations(node.getIters());
        }

        // Genera il codice per le dichiarazioni globali
        for (DeclNode globalDecl : globalDeclarations) {
            globalDecl.accept(this); // Genera il codice per ogni dichiarazione globale
        }
        isGlobalScope = false;

        code.append("\n");

        // Genera le funzioni di inizializzazione e deallocazione
        generateGlobalInitializationFunction();
        generateGlobalDeallocationFunction();

        // Rileva e genera le dichiarazioni per le funzioni/procedure
        if (node.getItersWithoutProcedure() != null) {
            collectFunctionPrototypes(node.getItersWithoutProcedure());
        }
        if (node.getIters() != null) {
            collectFunctionPrototypes(node.getIters());
        }

        // Output function prototypes
        for (String prototype : functionPrototypes) {
            code.append(prototype).append(";\n");
        }
        code.append("\n");

        // Genera il codice per il resto del programma
        if (node.getItersWithoutProcedure() != null) {
            node.getItersWithoutProcedure().accept(this);
        }
        if (node.getProcedure() != null) {
            node.getProcedure().accept(this);
        }
        if (node.getIters() != null) {
            node.getIters().accept(this);
        }

        return code.toString();
    }

    private void generateGlobalInitializationFunction() {
        if (!globalInitializations.isEmpty()) {
            code.append("\nvoid initialize_globals() {\n");
            for (String init : globalInitializations) {
                code.append("    ").append(init).append("\n");
            }
            code.append("}\n");
        }
    }

    private void generateGlobalDeallocationFunction() {
        if (!globalDeallocations.isEmpty()) {
            code.append("\nvoid free_globals() {\n");
            for (String dealloc : globalDeallocations) {
                code.append("    ").append(dealloc).append("\n");
            }
            code.append("}\n");
        }
    }

    private void collectGlobalDeclarations(ItersWithoutProcedureNode node) {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode varDeclNode) {
                globalDeclarations.addAll(varDeclNode.getDecls());
            }
        }
    }

    private void collectGlobalDeclarations(ItersNode node) {
        for (IterNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode varDeclNode) {
                globalDeclarations.addAll(varDeclNode.getDecls());
            }
        }
    }

    private void collectFunctionPrototypes(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof FunctionNode funcNode) {
                String prototype = generateFunctionPrototype(funcNode);
                functionPrototypes.add(prototype);
            } else if (iter.getDeclaration() instanceof ProcedureNode procNode) {
                String prototype = generateProcedurePrototype(procNode);
                functionPrototypes.add(prototype);
            }
        }
    }

    private void collectFunctionPrototypes(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof FunctionNode funcNode) {
                String prototype = generateFunctionPrototype(funcNode);
                functionPrototypes.add(prototype);
            } else if (iter.getDeclaration() instanceof ProcedureNode procNode) {
                if (procNode.getName().equals("main")) {
                    continue;
                }
                String prototype = generateProcedurePrototype(procNode);
                functionPrototypes.add(prototype);
            }
        }
    }

    private String generateFunctionPrototype(FunctionNode node) throws SemanticException {
        String returnType;
        StringBuilder paramsCode = new StringBuilder();
        if (node.getParams() != null) {
            paramsCode = new StringBuilder(getParamsCode(node.getParams()));
        }

        if (node.getReturnTypes().size() == 1) {
            // Funzione che restituisce un solo valore
            returnType = mapType(node.getReturnTypes().get(0));
        } else if (node.getReturnTypes().isEmpty()) {
            // Nessun valore di ritorno (funzione di tipo void)
            returnType = "void";
        } else {
            // Funzione che restituisce più valori, usiamo void e parametri out
            returnType = "void";
            // Aggiungiamo i parametri out per i valori di ritorno
            for (int i = 0; i < node.getReturnTypes().size(); i++) {
                Type retType = node.getReturnTypes().get(i);
                paramsCode.append(", ").append(mapType(retType)).append("* out_param").append(i);
            }
        }

        return returnType + " " + node.getName() + "(" + paramsCode + ")";
    }

    private String generateProcedurePrototype(ProcedureNode node) throws SemanticException {
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = getProcParamsCode(node.getParams());
        }
        return "void " + node.getName() + "(" + paramsCode + ")";
    }

    private String getParamsCode(FuncParamsNode params) throws SemanticException {
        StringJoiner paramsJoiner = new StringJoiner(", ");
        for (ParamNode param : params.getParams()) {
            String paramType = mapType(param.getType());
            paramsJoiner.add(paramType + " " + param.getName());
        }
        return paramsJoiner.toString();
    }

    private String getProcParamsCode(ProcParamsNode params) throws SemanticException {
        StringJoiner paramsJoiner = new StringJoiner(", ");
        for (ProcParamNode param : params.getParams()) {
            String paramType = mapType(param.getType());
            if (param.isOut()) {
                paramType = paramType + "*"; // 'out' parameters are pointers
            }
            paramsJoiner.add(paramType + " " + param.getName());
        }
        return paramsJoiner.toString();
    }

    @Override
    public Object visit(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode) {
                continue;
            }
            iter.accept(this);
        }
        return null; // ItersWithoutProcedureNode does not return a value
    }

    @Override
    public Object visit(IterWithoutProcedureNode node) throws SemanticException {
        node.getDeclaration().accept(this);
        return null;
    }

    @Override
    public Object visit(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode) {
                continue;
            }
            iter.accept(this);
        }
        return null; // ItersNode does not return a value
    }

    @Override
    public Object visit(IterNode node) throws SemanticException {
        node.getDeclaration().accept(this);
        return null;
    }

    @Override
    public Object visit(VarDeclNode node) throws SemanticException {
        // Variable declarations are handled in BodyNode
        // But if there are global variable declarations, we need to handle them here
        for (DeclNode decl : node.getDecls()) {
            decl.accept(this);
        }
        return null; // VarDeclNode does not return a value
    }

    @Override
    public Object visit(DeclNode node) throws SemanticException {
        List<String> ids = node.getIds();
        List<ConstNode> consts = node.getConsts();

        if (node.getType() != null) {
            // Case: declaration with a specified type
            String cType = mapType(node.getType());
            for (String id : ids) {
                if (node.getType() == Type.STRING) {
                    if (isGlobalScope) {
                        // Dichiarazione globale: inizializzata a NULL
                        indent();
                        code.append("char* ").append(id).append(" = NULL;\n");
                        // Aggiungi codice per l'inizializzazione globale
                        addGlobalInitialization(id, null, false);
                    } else {
                        // Dichiarazione locale: alloca immediatamente
                        indent();
                        code.append("char* ").append(id).append(" = malloc(256 * sizeof(char));\n");
                        checkAllocation(id);
                    }
                } else {
                    if (isGlobalScope) {
                        // Dichiarazione globale per altri tipi
                        indent();
                        code.append(cType).append(" ").append(id).append(";\n");
                    } else {
                        // Dichiarazione locale
                        indent();
                        code.append(cType).append(" ").append(id).append(";\n");
                    }
                }
            }
        } else if (consts != null) {
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                ConstNode constant = consts.get(i);
                String constantValue = (String) constant.accept(this); // Generate the constant code
                String cType = mapType(constant.getType()); // Get the constant's type

                if (constant.getType() == Type.STRING) {
                    if (isGlobalScope) {
                        // Dichiarazione globale con inizializzazione
                        indent();
                        code.append("char* ").append(id).append(" = NULL;\n");
                        // Aggiungi codice per l'inizializzazione globale con valore
                        addGlobalInitialization(id, constantValue, true);
                    } else {
                        // Dichiarazione locale con inizializzazione
                        indent();
                        code.append("char* ").append(id).append(" = malloc(strlen(").append(constantValue).append(") + 1);\n");
                        checkAllocation(id);
                        indent();
                        code.append("strcpy(").append(id).append(", ").append(constantValue).append(");\n");
                    }
                } else {
                    if (isGlobalScope) {
                        // Dichiarazione globale per altri tipi con inizializzazione
                        indent();
                        code.append(cType).append(" ").append(id).append(" = ").append(constantValue).append(";\n");
                    } else {
                        // Dichiarazione locale con inizializzazione
                        indent();
                        code.append(cType).append(" ").append(id).append(" = ").append(constantValue).append(";\n");
                    }
                }
            }
        } else {
            throw new SemanticException("Invalid declaration: missing type or assignment.");
        }

        return null;
    }

    private void checkAllocation(String id) {
        indent();
        code.append("if (!").append(id).append(") {\n");
        increaseIndent();
        indent();
        code.append("fprintf(stderr, \"Errore: allocazione fallita\\n\");\n");
        indent();
        code.append("exit(1);\n");
        decreaseIndent();
        indent();
        code.append("}\n");
    }

    @Override
    public Object visit(ConstNode node) throws SemanticException {
        Object value = node.getValue();

        if (value instanceof Integer) {
            // Integer constant
            return Integer.toString((Integer) value);
        } else if (value instanceof Double) {
            // Real constant
            return Double.toString((Double) value);
        } else if (value instanceof String) {
            // String constant (escaping special characters for C)
            return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
        } else if (value instanceof Boolean) {
            // Boolean constant
            return (Boolean) value ? "1" : "0"; // In C, 1 is true and 0 is false
        } else {
            throw new SemanticException("Unrecognized constant type: " + value.getClass().getSimpleName());
        }
    }

    @Override
    public Object visit(FunctionNode node) throws SemanticException {
        // Function signature
        functionStack.push(node);
        String returnType = node.getReturnTypes().size() == 1 ? mapType(node.getReturnTypes().get(0)) : "void";
        StringBuilder paramsCode = new StringBuilder();
        if (node.getParams() != null) {
            paramsCode = new StringBuilder(getParamsCode(node.getParams()));
        }
        // Add output parameters for multiple return values
        if (node.getReturnTypes().size() > 1) {
            for (int i = 0; i < node.getReturnTypes().size(); i++) {
                Type retType = node.getReturnTypes().get(i);
                paramsCode.append(", ").append(mapType(retType)).append("* out_param").append(i);
            }
        }

        indent();
        code.append(returnType).append(" ").append(node.getName()).append("(")
                .append(paramsCode).append(") {\n");
        increaseIndent();

        // Function body
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        decreaseIndent();
        indent();
        code.append("}\n\n");
        functionStack.pop();
        return null; // FunctionNode does not return a value
    }

    @Override
    public Object visit(FuncParamsNode node) throws SemanticException {
        // Already handled in getParamsCode
        return null;
    }

    @Override
    public Object visit(ParamNode node) throws SemanticException {
        // Already handled in getParamsCode
        return null;
    }

    @Override
    public Object visit(ProcedureNode node) throws SemanticException {
        // Procedure signature (void return type)
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = getProcParamsCode(node.getParams());
        }
        indent();

        if (node.getName().equals("main")) {
            code.append("int main() {\n");
        } else {
            code.append("void ").append(node.getName()).append("(").append(paramsCode).append(") {\n");
        }
        increaseIndent();

        if (node.getName().equals("main") && !globalInitializations.isEmpty()) {
            // Chiama initialize_globals
            indent();
            code.append("initialize_globals();\n\n");
        }

        // Procedure body
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        if (node.getName().equals("main") && !globalInitializations.isEmpty()) {
            // Chiama free_globals prima di uscire
            indent();
            code.append("free_globals();\n");

            // Aggiungi return 0;
            indent();
            code.append("return 0;\n");
        }

        decreaseIndent();
        indent();
        code.append("}\n\n");
        return null; // ProcedureNode does not return a value
    }

    @Override
    public Object visit(ProcParamsNode node) throws SemanticException {
        // Already handled in getProcParamsCode
        return null;
    }

    @Override
    public Object visit(ProcParamNode node) throws SemanticException {
        // Already handled in getProcParamsCode
        return null;
    }

    @Override
    public Object visit(BodyNode node) throws SemanticException {
        List<Visitable> statements = node.getStatements();

        // Collect variable declarations
        List<DeclNode> varDeclarations = new ArrayList<>();
        List<Visitable> otherStatements = new ArrayList<>();

        for (Visitable statement : statements) {
            if (statement instanceof VarDeclNode varDeclNode) {
                varDeclarations.addAll(varDeclNode.getDecls());
            } else {
                otherStatements.add(statement);
            }
        }

        // Output variable declarations first
        for (DeclNode decl : varDeclarations) {
            decl.accept(this);
        }

        // Now output other statements
        for (Visitable statement : otherStatements) {
            statement.accept(this);
        }

        return null; // BodyNode does not return a value
    }

    @Override
    public Object visit(AssignStatNode node) throws SemanticException {
        List<String> ids = node.getIds();
        List<ExprNode> exprs = node.getExprs();
        List<Boolean> isOutIds = node.getIsOutIds();

        int idIndex = 0; // Indice per scorrere gli identificatori

        for (ExprNode expr : exprs) {
            if (expr instanceof FunCallNode funcCall) {

                String functionName = funcCall.getFunctionName();
                StringBuilder argsBuilder = new StringBuilder();
                List<ExprNode> args = funcCall.getArguments();

                // Genera il codice per gli argomenti della funzione
                for (int i = 0; i < args.size(); i++) {
                    String argCode = (String) args.get(i).accept(this);
                    argsBuilder.append(argCode);
                    if (i < args.size() - 1) {
                        argsBuilder.append(", ");
                    }
                }

                List<Type> returnTypes = funcCall.getReturnTypes(); // Assume che questo metodo esista
                int numReturns = returnTypes.size();

                if (numReturns == 1) {
                    // Funzione con un solo valore di ritorno
                    String id = ids.get(idIndex);
                    indent();
                    code.append(id).append(" = ").append(functionName).append("(")
                            .append(argsBuilder.toString()).append(");\n");
                    idIndex++;
                } else {
                    // Funzione con più valori di ritorno
                    // Aggiungiamo i parametri out
                    StringBuilder funcCallCode = new StringBuilder();
                    funcCallCode.append(functionName).append("(").append(argsBuilder.toString());
                    for (int i = 0; i < numReturns; i++) {
                        String id = ids.get(idIndex + i);
                        funcCallCode.append(", &").append(id);
                    }
                    funcCallCode.append(");\n");
                    indent();
                    code.append(funcCallCode.toString());
                    idIndex += numReturns;
                }
            } else {
                // Assegnamento normale
                String id = ids.get(idIndex);
                String exprCode = (String) expr.accept(this);
                indent();

                boolean isOutId = false;
                if ((isOutIds.size() > idIndex)) {
                    if (isOutIds.get(idIndex)) {
                        isOutId = true;
                    }
                }

                code.append(isOutId ? "*" + id : id)
                        .append(" = ")
                        .append(exprCode)
                        .append(";\n");
                idIndex++;
            }
        }
        return null;
    }

    @Override
    public Object visit(ProcCallStatNode node) throws SemanticException {
        String procName = node.getProcCall().getProcedureName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ProcExprNode> args = node.getProcCall().getArguments();
        for (int i = 0; i < args.size(); i++) {
            ProcExprNode arg = args.get(i);
            String argCode = (String) arg.getExpr().accept(this);
            if (arg.isRef()) {
                argsBuilder.append("&").append(argCode);
            } else {
                argsBuilder.append(argCode);
            }
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        indent();
        code.append(procName).append("(").append(argsBuilder.toString()).append(");\n");
        return null; // ProcCallStatNode does not return a value
    }

    @Override
    public Object visit(ReturnStatNode node) throws SemanticException {
        if (node.getExprs().isEmpty()) {
            indent();
            code.append("return;\n");
        } else {
            // Otteniamo la funzione corrente per sapere se siamo in una funzione con più valori di ritorno
            FunctionNode currentFunction = getCurrentFunction();
            if (currentFunction.getReturnTypes().size() > 1) {
                // Assegniamo le espressioni ai parametri out
                for (int i = 0; i < node.getExprs().size(); i++) {
                    String exprCode = (String) node.getExprs().get(i).accept(this);
                    indent();
                    code.append("*out_param").append(i).append(" = ").append(exprCode).append(";\n");
                }
                indent();
                code.append("return;\n");
            } else if (currentFunction.getReturnTypes().size() == 1) {
                // Funzione con un solo valore di ritorno
                String exprCode = (String) node.getExprs().get(0).accept(this);
                indent();
                code.append("return ").append(exprCode).append(";\n");
            }
        }
        return null;
    }

    private FunctionNode getCurrentFunction() {
        return functionStack.peek();
    }

    @Override
    public Object visit(WriteStatNode node) throws SemanticException {
        generateWriteStat(node.getArgs(), false); // Passiamo 'false' per NON aggiungere una nuova linea
        return null;
    }

    @Override
    public Object visit(WriteReturnStatNode node) throws SemanticException {
        generateWriteStat(node.getArgs(), true); // Passiamo 'true' per aggiungere una nuova linea
        return null;
    }

    private void generateWriteStat(List<IOArgNode> args, boolean appendNewline) throws SemanticException {
        StringBuilder formatBuilder = new StringBuilder();
        List<String> argCodes = new ArrayList<>();

        for (IOArgNode arg : args) {
            if (arg instanceof IOArgStringLiteralNode) {
                // Aggiungi la stringa letterale al formato
                String str = ((IOArgStringLiteralNode) arg).getValue();
                formatBuilder.append(str.replace("\"", "\\\"")); // Escape per le virgolette
            } else if (arg instanceof DollarExprNode dollarExpr) {
                // Gestione dell'espressione racchiusa in $(...)
                ExprNode expr = dollarExpr.getExpr();
                if (expr instanceof FunCallNode) {
                    Type exprType = ((FunCallNode) expr).getReturnTypes().get(0);
                    String formatSpecifier = getFormatSpecifier(exprType);
                    formatBuilder.append(formatSpecifier);
                    String argCode = (String) expr.accept(this);
                    argCodes.add(argCode);
                } else {
                    Type exprType = expr.getType();
                    String formatSpecifier = getFormatSpecifier(exprType);
                    formatBuilder.append(formatSpecifier);
                    String argCode = (String) expr.accept(this);
                    argCodes.add(argCode);
                }
            } else {
                // Altri tipi di argomenti
                Type argType = getTypeOfIOArgNode(arg);
                String formatSpecifier = getFormatSpecifier(argType);
                formatBuilder.append(formatSpecifier);
                String argCode = (String) arg.accept(this);
                argCodes.add(argCode);
            }
        }

        if (appendNewline) {
            formatBuilder.append("\\n");
        }

        // Genera la chiamata a printf
        indent();
        code.append("printf(\"").append(formatBuilder).append("\"");
        if (!argCodes.isEmpty()) {
            code.append(", ").append(String.join(", ", argCodes));
        }
        code.append(");\n");
    }

    @Override
    public Object visit(ReadStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            if (arg instanceof IOArgStringLiteralNode) {
                // Se è una stringa letterale, stampala come prompt
                indent();
                String prompt = ((IOArgStringLiteralNode) arg).getValue().replace("\"", "\\\"");
                code.append("printf(\"").append(prompt).append("\");\n");
            } else if (arg instanceof DollarExprNode) {
                // Leggi l'input nella variabile
                ExprNode exprNode = ((DollarExprNode) arg).getExpr();
                String argName = (String) exprNode.accept(this);
                Type argType = exprNode.getType();
                String formatSpecifier = getInputFormatSpecifier(argType);

                indent();
                if (argType == Type.STRING) {
                    // Per le stringhe, non serve &
                    code.append("scanf(\"").append(formatSpecifier).append("\", ").append(argName).append(");\n");
                } else {
                    // Per altri tipi, usa &
                    code.append("scanf(\"").append(formatSpecifier).append("\", &").append(argName).append(");\n");
                }
            }
        }
        return null;
    }

    private Type getTypeOfIOArgNode(IOArgNode arg) throws SemanticException {
        if (arg instanceof IOArgIdentifierNode) {
            // Recupera il tipo dall'identifier
            return ((IOArgIdentifierNode) arg).getType();
        } else if (arg instanceof IOArgBinaryNode) {
            return ((IOArgBinaryNode) arg).getType();
        } else if (arg instanceof DollarExprNode) {
            return ((DollarExprNode) arg).getType();
        } else if (arg instanceof IOArgStringLiteralNode) {
            return Type.STRING;
        } else {
            throw new SemanticException("Tipo di IOArgNode sconosciuto: " + arg.getClass().getSimpleName());
        }
    }

    private String getFormatSpecifier(Type type) {
        return switch (type) {
            case INTEGER -> "%d";
            case REAL -> "%f";
            case STRING -> "%s";
            case BOOLEAN -> "%d"; // I booleani in C sono stampati come interi
            default -> "%d"; // Predefinito a %d
        };
    }

    private String getInputFormatSpecifier(Type type) {
        return switch (type) {
            case INTEGER -> "%d";
            case REAL -> "%lf"; // Per leggere valori double in scanf
            case STRING -> "%s";
            case BOOLEAN -> "%d"; // Legge come intero
            default -> "%d"; // Predefinito a %d
        };
    }

    @Override
    public Object visit(IfStatNode node) throws SemanticException {
        // Generate C 'if' statement
        String condition = (String) node.getCondition().accept(this);
        indent();
        code.append("if (").append(condition).append(") {\n");
        increaseIndent();
        node.getThenBody().accept(this);
        decreaseIndent();
        indent();
        code.append("}\n");

        // Handle elif blocks
        for (ElifNode elif : node.getElifBlocks()) {
            String elifCondition = (String) elif.getCondition().accept(this);
            indent();
            code.append("else if (").append(elifCondition).append(") {\n");
            increaseIndent();
            elif.getBody().accept(this);
            decreaseIndent();
            indent();
            code.append("}\n");
        }

        // Handle else block
        if (node.getElseBlock() != null) {
            indent();
            code.append("else {\n");
            increaseIndent();
            node.getElseBlock().getBody().accept(this);
            decreaseIndent();
            indent();
            code.append("}\n");
        }

        return null; // IfStatNode does not return a value
    }

    @Override
    public Object visit(WhileStatNode node) throws SemanticException {
        // Generate C 'while' loop
        String condition = (String) node.getCondition().accept(this);
        indent();
        code.append("while (").append(condition).append(") {\n");
        increaseIndent();
        node.getBody().accept(this);
        decreaseIndent();
        indent();
        code.append("}\n");
        return null; // WhileStatNode does not return a value
    }

    @Override
    public Object visit(FunCallNode node) throws SemanticException {
        String functionName = node.getFunctionName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ExprNode> args = node.getArguments();

        // Genera il codice per gli argomenti
        for (int i = 0; i < args.size(); i++) {
            String argCode = (String) args.get(i).accept(this);
            argsBuilder.append(argCode);
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }

        List<Type> returnTypes = node.getReturnTypes();
        int numReturns = returnTypes.size();

        if (numReturns == 1) {
            // Funzione con un solo valore di ritorno
            // Restituiamo l'espressione della chiamata alla funzione
            return functionName + "(" + argsBuilder.toString() + ")";
        } else if (numReturns > 1) {
            // Funzione con più valori di ritorno non può essere usata in un'espressione
            throw new SemanticException("La funzione '" + functionName + "' con più valori di ritorno non può essere usata in un'espressione.");
        } else {
            // Funzione senza valori di ritorno
            // Generiamo il codice per la chiamata alla funzione
            indent();
            code.append(functionName).append("(").append(argsBuilder.toString()).append(");\n");
            return null;
        }
    }

    @Override
    public Object visit(ProcCallNode node) throws SemanticException {
        // For procedure calls used as expressions (if any)
        String procedureName = node.getProcedureName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ProcExprNode> args = node.getArguments();
        for (int i = 0; i < args.size(); i++) {
            ProcExprNode arg = args.get(i);
            String argCode = (String) arg.getExpr().accept(this);
            if (arg.isRef()) {
                argsBuilder.append("&").append(argCode);
            } else {
                argsBuilder.append(argCode);
            }
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        return procedureName + "(" + argsBuilder.toString() + ")";
    }

    @Override
    public Object visit(ElifNode node) throws SemanticException {
        // Handled within IfStatNode
        return null;
    }

    @Override
    public Object visit(ElseNode node) throws SemanticException {
        // Handled within IfStatNode
        return null;
    }

    @Override
    public Object visit(IOArgIdentifierNode node) throws SemanticException {
        return node.getIdentifier();
    }

    @Override
    public Object visit(IOArgStringLiteralNode node) throws SemanticException {
        return "\"" + node.getValue().replace("\"", "\\\"") + "\"";
    }

    @Override
    public Object visit(IOArgBinaryNode node) throws SemanticException {
        String left = (String) node.getLeft().accept(this);
        String right = (String) node.getRight().accept(this);
        String operator = mapOperatorToC(node.getOperator());
        return "(" + left + " " + operator + " " + right + ")";
    }

    @Override
    public Object visit(DollarExprNode node) throws SemanticException {
        ExprNode exprNode = node.getExpr();
        return (String) exprNode.accept(this);
    }

    @Override
    public Object visit(ProcExprNode node) throws SemanticException {
        return node.getExpr().accept(this);
    }

    @Override
    public Object visit(RealConstNode node) throws SemanticException {
        return Double.toString(node.getValue());
    }

    @Override
    public Object visit(IntConstNode node) throws SemanticException {
        return Integer.toString(node.getValue());
    }

    @Override
    public Object visit(StringConstNode node) throws SemanticException {
        return "\"" + node.getValue().replace("\"", "\\\"") + "\"";
    }

    @Override
    public Object visit(IdentifierNode node) throws SemanticException {
        return node.getIsOutInProcedure() ? "*" + node.getName() : node.getName();
    }

    @Override
    public Object visit(BooleanConstNode node) throws SemanticException {
        return node.getValue() ? "true" : "false"; // C uses 1 for true and 0 for false
    }

    @Override
    public Object visit(BinaryExprNode node) throws SemanticException {
        String operator = node.getOperator();
        String leftCode = (String) node.getLeft().accept(this);
        String rightCode = (String) node.getRight().accept(this);
        Type leftType = node.getLeft().getType();
        Type rightType = node.getRight().getType();

        if (operator.equals("+")) {
            if (leftType == Type.STRING || rightType == Type.STRING) {
                // Concatenazione di stringhe
                return generateConcatenationCode(leftCode, leftType, rightCode, rightType);
            } else {
                // Somma normale
                return leftCode + " + " + rightCode;
            }
        } else if (isComparisonOperator(operator)) {
            if (leftType == Type.STRING && rightType == Type.STRING) {
                // Confronto tra stringhe
                return generateStringComparisonCode(leftCode, rightCode, operator);
            } else {
                // Confronto tra altri tipi
                String cOperator = mapOperatorToC(operator);
                return leftCode + " " + cOperator + " " + rightCode;
            }
        } else {
            // Altri operatori
            String cOperator = mapOperatorToC(operator);
            return leftCode + " " + cOperator + " " + rightCode;
        }
    }

    private boolean isComparisonOperator(String operator) {
        return operator.equals("=") || operator.equals("!=")
                || operator.equals("<") || operator.equals(">")
                || operator.equals("<=") || operator.equals(">=");
    }

    private String generateStringComparisonCode(String leftCode, String rightCode, String operator) throws SemanticException {
        return switch (operator) {
            case "=" -> "(strcmp(" + leftCode + ", " + rightCode + ") == 0)";
            case "!=" -> "(strcmp(" + leftCode + ", " + rightCode + ") != 0)";
            case "<" -> "(strcmp(" + leftCode + ", " + rightCode + ") < 0)";
            case ">" -> "(strcmp(" + leftCode + ", " + rightCode + ") > 0)";
            case "<=" -> "(strcmp(" + leftCode + ", " + rightCode + ") <= 0)";
            case ">=" -> "(strcmp(" + leftCode + ", " + rightCode + ") >= 0)";
            default ->
                    throw new SemanticException("Operatore di confronto non supportato per le stringhe: " + operator);
        };
    }

    private String generateConcatenationCode(String leftCode, Type leftType, String rightCode, Type rightType) throws SemanticException {
        // Nome della variabile risultato
        String resultVar = getNextTempVar();

        // Stima dimensione buffer
        int bufferSize = estimateBufferSize(leftType) + estimateBufferSize(rightType) + 1;
        indent();
        code.append("char* ").append(resultVar).append(" = malloc(").append(bufferSize).append(");\n");
        checkAllocation(resultVar);

        // Conversione degli operandi in stringa, se necessario
        String leftStrVar = generateStringConversion(leftCode, leftType);
        String rightStrVar = generateStringConversion(rightCode, rightType);

        // Concatenazione delle stringhe
        generateStringConcatenation(resultVar, leftStrVar, rightStrVar);

        return resultVar;
    }

    private int estimateBufferSize(Type type) {
        return switch (type) {
            case STRING -> 512; // Presupponendo una lunghezza massima di 512 per le stringhe
            case INTEGER -> 11; // Interi rappresentabili in base 10 con segno (max 10 cifre + terminatore)
            case REAL -> 32; // Per numeri in formato scientifico o decimale
            default -> 0;
        };
    }

    private String generateStringConversion(String source, Type type) throws SemanticException {
        String tempVar = getNextTempVar();
        indent();
        code.append("char ").append(tempVar).append("[512];\n");
        indent();
        switch (type) {
            case STRING:
                code.append("strcpy(").append(tempVar).append(", ").append(source).append(");\n");
                break;
            case INTEGER:
                code.append("sprintf(").append(tempVar).append(", \"%d\", ").append(source).append(");\n");
                break;
            case REAL:
                code.append("sprintf(").append(tempVar).append(", \"%f\", ").append(source).append(");\n");
                break;
            default:
                throw new SemanticException("Unsupported type for conversion to string: " + type);
        }
        return tempVar;
    }

    private void generateStringConcatenation(String dest, String left, String right) {
        indent();
        code.append("strcpy(").append(dest).append(", ").append(left).append(");\n");
        indent();
        code.append("strcat(").append(dest).append(", ").append(right).append(");\n");
    }

    @Override
    public Object visit(UnaryExprNode node) throws SemanticException {
        String operator = node.getOperator();
        String expr = (String) node.getExpr().accept(this);
        return switch (operator) {
            case "uminus" -> "(-" + expr + ")";
            case "not" -> "(!" + expr + ")";
            default -> throw new SemanticException("Unrecognized unary operator: " + operator);
        };
    }

    // Helper method to map custom types to C types
    private String mapType(Type type) throws SemanticException {
        return switch (type) {
            case INTEGER -> "int";
            case REAL -> "double";
            case STRING -> "char*";
            case BOOLEAN -> "bool"; // Ensure to include <stdbool.h> in the generated code
            default -> throw new SemanticException("Unsupported type: " + type);
        };
    }

    private String mapOperatorToC(String operator) {
        return switch (operator) {
            case "and" -> "&&";
            case "or" -> "||";
            case "not" -> "!";
            case "=" -> "==";
            case "!=" -> "!=";
            case ">" -> ">";
            case ">=" -> ">=";
            case "<" -> "<";
            case "<=" -> "<=";
            case "+" -> "+";
            case "-" -> "-";
            case "*" -> "*";
            case "/" -> "/";
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }
}
