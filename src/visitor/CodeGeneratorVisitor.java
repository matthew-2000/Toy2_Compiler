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

    public CodeGeneratorVisitor() {
        this.code = new StringBuilder();
        this.indentLevel = 0;
    }

    // Utility methods for indentation
    private void indent() {
        for (int i = 0; i < indentLevel; i++) {
            code.append("    "); // 4 spaces per indent level
        }
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

        code.append("\n");

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

    private void collectGlobalDeclarations(ItersWithoutProcedureNode node) {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode) {
                VarDeclNode varDeclNode = (VarDeclNode) iter.getDeclaration();
                globalDeclarations.addAll(varDeclNode.getDecls());
            }
        }
    }

    private void collectGlobalDeclarations(ItersNode node) {
        for (IterNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof VarDeclNode) {
                VarDeclNode varDeclNode = (VarDeclNode) iter.getDeclaration();
                globalDeclarations.addAll(varDeclNode.getDecls());
            }
        }
    }


    private void collectFunctionPrototypes(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof FunctionNode) {
                FunctionNode funcNode = (FunctionNode) iter.getDeclaration();
                String prototype = generateFunctionPrototype(funcNode);
                functionPrototypes.add(prototype);
            } else if (iter.getDeclaration() instanceof ProcedureNode) {
                ProcedureNode procNode = (ProcedureNode) iter.getDeclaration();
                String prototype = generateProcedurePrototype(procNode);
                functionPrototypes.add(prototype);
            }
        }
    }

    private void collectFunctionPrototypes(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            if (iter.getDeclaration() instanceof FunctionNode) {
                FunctionNode funcNode = (FunctionNode) iter.getDeclaration();
                String prototype = generateFunctionPrototype(funcNode);
                functionPrototypes.add(prototype);
            } else if (iter.getDeclaration() instanceof ProcedureNode) {
                ProcedureNode procNode = (ProcedureNode) iter.getDeclaration();
                if (procNode.getName().equals("main")){
                    continue;
                }
                String prototype = generateProcedurePrototype(procNode);
                functionPrototypes.add(prototype);
            }
        }
    }

    private String generateFunctionPrototype(FunctionNode node) throws SemanticException {
        // Function signature
        String returnType = mapType(node.getReturnTypes().get(0)); // Assuming single return type
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = getParamsCode(node.getParams());
        }
        String prototype = returnType + " " + node.getName() + "(" + paramsCode + ")";
        return prototype;
    }

    private String generateProcedurePrototype(ProcedureNode node) throws SemanticException {

        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = getProcParamsCode(node.getParams());
        }
        String prototype = "void " + node.getName() + "(" + paramsCode + ")";
        return prototype;
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
                    // For strings, allocate memory dynamically
                    indent();
                    code.append("char* ").append(id).append(" = malloc(256 * sizeof(char));\n");
                    indent();
                    code.append("if (").append(id).append(" == NULL) {\n");
                    increaseIndent();
                    indent();
                    code.append("fprintf(stderr, \"Memory allocation failed for ").append(id).append("\\n\");\n");
                    indent();
                    code.append("exit(1);\n");
                    decreaseIndent();
                    indent();
                    code.append("}\n");
                } else {
                    indent();
                    code.append(cType).append(" ").append(id).append(";\n");
                }
            }
        } else if (consts != null) {
            // Case: declaration with constant assignments
            if (ids.size() != consts.size()) {
                throw new SemanticException("Number of identifiers and constants does not match.");
            }
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                ConstNode constant = consts.get(i);
                String constantValue = (String) constant.accept(this); // Generate the constant code
                String cType = mapType(constant.getType()); // Get the constant's type

                if (constant.getType() == Type.STRING) {
                    // For string constants, allocate and initialize dynamically
                    indent();
                    code.append("char* ").append(id).append(" = malloc(strlen(").append(constantValue).append(") + 1);\n");
                    indent();
                    code.append("if (").append(id).append(" == NULL) {\n");
                    increaseIndent();
                    indent();
                    code.append("fprintf(stderr, \"Memory allocation failed for ").append(id).append("\\n\");\n");
                    indent();
                    code.append("exit(1);\n");
                    decreaseIndent();
                    indent();
                    code.append("}\n");
                    indent();
                    code.append("strcpy(").append(id).append(", ").append(constantValue).append(");\n");
                } else {
                    indent();
                    code.append(cType).append(" ").append(id).append(" = ").append(constantValue).append(";\n");
                }
            }
        } else {
            throw new SemanticException("Invalid declaration: missing type or assignment.");
        }

        return null; // DeclNode does not return a value
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
        String returnType = mapType(node.getReturnTypes().get(0)); // Assuming single return type
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = getParamsCode(node.getParams());
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

        // Procedure body
        if (node.getBody() != null) {
            node.getBody().accept(this);
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
            if (statement instanceof VarDeclNode) {
                VarDeclNode varDeclNode = (VarDeclNode) statement;
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

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            String exprCode = (String) exprs.get(i).accept(this);
            indent();
            code.append(isOutIds.get(i) ? "*" + id : id)
                    .append(" = ")
                    .append(exprCode)
                    .append(";\n");
        }
        return null; // AssignStatNode does not return a value
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
            StringJoiner returnJoiner = new StringJoiner(", ");
            for (ExprNode expr : node.getExprs()) {
                String exprCode = (String) expr.accept(this);
                returnJoiner.add(exprCode);
            }
            indent();
            code.append("return ").append(returnJoiner.toString()).append(";\n");
        }
        return null; // ReturnStatNode does not return a value
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
        StringBuilder argsBuilder = new StringBuilder();
        indent();
        code.append("printf(").append("\"");
        List<String> argCodes = new ArrayList<>();
        for (IOArgNode arg : args) {
            if (arg instanceof IOArgStringLiteralNode) {
                String str = ((IOArgStringLiteralNode) arg).getValue();
                code.append(str.replace("\"", "\\\""));
            } else {
                // Determina lo specificatore di formato in base al tipo
                Type argType = getTypeOfIOArgNode(arg);
                String formatSpecifier = getFormatSpecifier(argType);
                code.append(formatSpecifier);
                // Genera il codice per l'argomento
                String argCode = (String) arg.accept(this);
                argCodes.add(argCode);
            }
        }
        if (appendNewline) {
            code.append("\\n"); // Aggiunge il carattere di nuova linea
        }
        code.append("\"");
        if (!argCodes.isEmpty()) {
            code.append(", ");
            code.append(String.join(", ", argCodes));
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
            } else {
                // Altrimenti, leggi l'input nella variabile
                indent();
                String argName = (String) arg.accept(this);
                Type argType = getTypeOfIOArgNode(arg);
                String formatSpecifier = getInputFormatSpecifier(argType);

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
            // Supponendo che IOArgBinaryNode abbia un metodo getType()
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
        switch (type) {
            case INTEGER:
                return "%d";
            case REAL:
                return "%f";
            case STRING:
                return "%s";
            case BOOLEAN:
                return "%d"; // I booleani in C sono stampati come interi
            default:
                return "%d"; // Predefinito a %d
        }
    }

    private String getInputFormatSpecifier(Type type) {
        switch (type) {
            case INTEGER:
                return "%d";
            case REAL:
                return "%lf"; // Per leggere valori double in scanf
            case STRING:
                return "%s";
            case BOOLEAN:
                return "%d"; // Legge come intero
            default:
                return "%d"; // Predefinito a %d
        }
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
        for (int i = 0; i < args.size(); i++) {
            String argCode = (String) args.get(i).accept(this);
            argsBuilder.append(argCode);
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        return functionName + "(" + argsBuilder.toString() + ")";
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
        return node.getExpr().accept(this);
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
        return node.getValue() ? "1" : "0"; // C uses 1 for true and 0 for false
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
                String resultVar = generateConcatenationCode(leftCode, leftType, rightCode, rightType);
                return resultVar;
            } else {
                // Somma normale
                return "(" + leftCode + " + " + rightCode + ")";
            }
        } else if (isComparisonOperator(operator)) {
            if (leftType == Type.STRING && rightType == Type.STRING) {
                // Confronto tra stringhe
                String comparisonCode = generateStringComparisonCode(leftCode, rightCode, operator);
                return comparisonCode;
            } else {
                // Confronto tra altri tipi
                String cOperator = mapOperatorToC(operator);
                return "(" + leftCode + " " + cOperator + " " + rightCode + ")";
            }
        } else {
            // Altri operatori
            String cOperator = mapOperatorToC(operator);
            return "(" + leftCode + " " + cOperator + " " + rightCode + ")";
        }
    }

    private boolean isComparisonOperator(String operator) {
        return operator.equals("==") || operator.equals("!=")
                || operator.equals("<") || operator.equals(">")
                || operator.equals("<=") || operator.equals(">=");
    }

    private String generateStringComparisonCode(String leftCode, String rightCode, String operator) throws SemanticException {
        String condition;
        switch (operator) {
            case "==":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") == 0)";
                break;
            case "!=":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") != 0)";
                break;
            case "<":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") < 0)";
                break;
            case ">":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") > 0)";
                break;
            case "<=":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") <= 0)";
                break;
            case ">=":
                condition = "(strcmp(" + leftCode + ", " + rightCode + ") >= 0)";
                break;
            default:
                throw new SemanticException("Operatore di confronto non supportato per le stringhe: " + operator);
        }
        return condition;
    }

    private String generateConcatenationCode(String leftCode, Type leftType, String rightCode, Type rightType) throws SemanticException {
        String resultVar = getNextTempVar();
        indent();
        code.append("char ").append(resultVar).append("[1024];\n");

        // Convert left operand to string
        String leftStrVar = getNextTempVar();
        indent();
        code.append("char ").append(leftStrVar).append("[512];\n");
        indent();
        if (leftType == Type.STRING) {
            code.append("strcpy(").append(leftStrVar).append(", ").append(leftCode).append(");\n");
        } else if (leftType == Type.INTEGER) {
            code.append("sprintf(").append(leftStrVar).append(", \"%d\", ").append(leftCode).append(");\n");
        } else if (leftType == Type.REAL) {
            code.append("sprintf(").append(leftStrVar).append(", \"%f\", ").append(leftCode).append(");\n");
        } else {
            throw new SemanticException("Unsupported type for concatenation: " + leftType);
        }

        // Convert right operand to string
        String rightStrVar = getNextTempVar();
        indent();
        code.append("char ").append(rightStrVar).append("[512];\n");
        indent();
        if (rightType == Type.STRING) {
            code.append("strcpy(").append(rightStrVar).append(", ").append(rightCode).append(");\n");
        } else if (rightType == Type.INTEGER) {
            code.append("sprintf(").append(rightStrVar).append(", \"%d\", ").append(rightCode).append(");\n");
        } else if (rightType == Type.REAL) {
            code.append("sprintf(").append(rightStrVar).append(", \"%f\", ").append(rightCode).append(");\n");
        } else {
            throw new SemanticException("Unsupported type for concatenation: " + rightType);
        }

        // Concatenate the strings into resultVar
        indent();
        code.append("snprintf(").append(resultVar).append(", sizeof(").append(resultVar).append("), \"%s%s\", ").append(leftStrVar).append(", ").append(rightStrVar).append(");\n");

        // Return resultVar
        return resultVar;
    }

    @Override
    public Object visit(UnaryExprNode node) throws SemanticException {
        String operator = node.getOperator();
        String expr = (String) node.getExpr().accept(this);
        switch (operator) {
            case "uminus":
                return "(-" + expr + ")";
            case "not":
                return "(!" + expr + ")";
            default:
                throw new SemanticException("Unrecognized unary operator: " + operator);
        }
    }

    // Helper method to map custom types to C types
    private String mapType(Type type) throws SemanticException {
        switch (type) {
            case INTEGER:
                return "int";
            case REAL:
                return "double";
            case STRING:
                return "char*";
            case BOOLEAN:
                return "bool"; // Ensure to include <stdbool.h> in the generated code
            default:
                throw new SemanticException("Unsupported type: " + type);
        }
    }

    private String mapOperatorToC(String operator) {
        switch (operator) {
            case "and": return "&&";
            case "or": return "||";
            case "not": return "!";
            case "==": return "==";
            case "!=": return "!=";
            case ">": return ">";
            case ">=": return ">=";
            case "<": return "<";
            case "<=": return "<=";
            case "+": return "+";
            case "-": return "-";
            case "*": return "*";
            case "/": return "/";
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}
