package visitor;

import nodes.*;
import nodes.stat.*;
import nodes.expr.*;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.List;
import java.util.StringJoiner;

public class CodeGeneratorVisitor implements Visitor<String> {
    private StringBuilder code;
    private int indentLevel;

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

    @Override
    public String visit(ProgramNode node) throws SemanticException {
        // Inizia includendo le librerie standard necessarie
        code.append("#include <stdio.h>\n");
        code.append("#include <stdlib.h>\n");
        code.append("#include <stdbool.h>\n");
        code.append("\n");

        // Visita le dichiarazioni globali e le funzioni/procedure
        if (node.getItersWithoutProcedure() != null) {
            node.getItersWithoutProcedure().accept(this);
        }

        // Visita la procedura principale (main)
        if (node.getProcedure() != null) {
            node.getProcedure().accept(this);
        }

        // Visita eventuali altre dichiarazioni o funzioni
        if (node.getIters() != null) {
            node.getIters().accept(this);
        }

        return code.toString();
    }

    @Override
    public String visit(ItersWithoutProcedureNode node) throws SemanticException {
        for (IterWithoutProcedureNode iter : node.getIterList()) {
            code.append(iter.accept(this));
        }
        return ""; // ItersWithoutProcedureNode does not return a string directly
    }

    @Override
    public String visit(IterWithoutProcedureNode node) throws SemanticException {
        return node.getDeclaration().accept(this);
    }

    @Override
    public String visit(ItersNode node) throws SemanticException {
        for (IterNode iter : node.getIterList()) {
            code.append(iter.accept(this));
        }
        return ""; // ItersNode does not return a string directly
    }

    @Override
    public String visit(IterNode node) throws SemanticException {
        return node.getDeclaration().accept(this);
    }

    @Override
    public String visit(VarDeclNode node) throws SemanticException {
        for (DeclNode decl : node.getDecls()) {
            code.append(decl.accept(this));
        }
        return ""; // VarDeclNode does not return a string directly
    }

    @Override
    public String visit(DeclNode node) throws SemanticException {
        StringJoiner declarations = new StringJoiner(", ");
        List<String> ids = node.getIds();
        List<ConstNode> consts = node.getConsts();

        if (node.getType() != null) {
            // Caso: dichiarazione con un tipo specificato
            String cType = mapType(node.getType());
            for (String id : ids) {
                declarations.add(id);
            }
            indent();
            code.append(cType).append(" ").append(declarations.toString()).append(";\n");
        } else if (consts != null) {
            // Caso: dichiarazione con assegnazione di costanti
            if (ids.size() != consts.size()) {
                throw new SemanticException("Numero di identificatori e costanti non corrisponde.");
            }
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                ConstNode constant = consts.get(i);
                String constantValue = constant.accept(this); // Genera il codice della costante
                String cType = mapType(constant.getType()); // Ottiene il tipo della costante

                indent();
                code.append(cType).append(" ").append(id).append(" = ").append(constantValue).append(";\n");
            }
        } else {
            throw new SemanticException("Dichiarazione non valida: manca il tipo o l'assegnazione.");
        }

        return ""; // DeclNode does not return a string directly
    }

    @Override
    public String visit(ConstNode node) throws SemanticException {
        Object value = node.getValue();

        if (value instanceof Integer) {
            // Costante intera
            return Integer.toString((Integer) value);
        } else if (value instanceof Double) {
            // Costante reale
            return Double.toString((Double) value);
        } else if (value instanceof String) {
            // Costante stringa (escapando i caratteri speciali per C)
            return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
        } else if (value instanceof Boolean) {
            // Costante booleana
            return (Boolean) value ? "1" : "0"; // In C, 1 è true e 0 è false
        } else {
            throw new SemanticException("Tipo di costante non riconosciuto: " + value.getClass().getSimpleName());
        }
    }


    @Override
    public String visit(FunctionNode node) throws SemanticException {
        // Function signature
        String returnType = mapType(node.getReturnTypes().get(0)); // Assuming single return type
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = node.getParams().accept(this);
        }
        indent();
        code.append(returnType).append(" ").append(node.getName()).append("(")
                .append(paramsCode).append(") {\n");
        increaseIndent();

        // Function body
        if (node.getBody() != null) {
            code.append(node.getBody().accept(this));
        }

        decreaseIndent();
        indent();
        code.append("}\n\n");
        return ""; // FunctionNode does not return a string directly
    }

    @Override
    public String visit(FuncParamsNode node) throws SemanticException {
        StringJoiner paramsJoiner = new StringJoiner(", ");
        for (ParamNode param : node.getParams()) {
            paramsJoiner.add(param.accept(this));
        }
        return paramsJoiner.toString();
    }

    @Override
    public String visit(ParamNode node) throws SemanticException {
        String paramType = mapType(node.getType());
        return paramType + " " + node.getName();
    }

    @Override
    public String visit(ProcedureNode node) throws SemanticException {
        // Procedure signature (void return type)
        String paramsCode = "";
        if (node.getParams() != null) {
            paramsCode = node.getParams().accept(this);
        }
        indent();
        code.append("void ").append(node.getName()).append("(")
                .append(paramsCode).append(") {\n");
        increaseIndent();

        // Procedure body
        if (node.getBody() != null) {
            code.append(node.getBody().accept(this));
        }

        decreaseIndent();
        indent();
        code.append("}\n\n");
        return ""; // ProcedureNode does not return a string directly
    }

    @Override
    public String visit(ProcParamsNode node) throws SemanticException {
        StringJoiner paramsJoiner = new StringJoiner(", ");
        for (ProcParamNode param : node.getParams()) {
            paramsJoiner.add(param.accept(this));
        }
        return paramsJoiner.toString();
    }

    @Override
    public String visit(ProcParamNode node) throws SemanticException {
        String paramType = mapType(node.getType());
        if (node.isOut()) {
            paramType = paramType + "*"; // Assuming 'out' parameters are integers for simplicity
        }
        return paramType + " " + node.getName();
    }

    @Override
    public String visit(BodyNode node) throws SemanticException {
        StringBuilder bodyCode = new StringBuilder();
        for (Visitable statement : node.getStatements()) {
            String stmtCode = statement.accept(this);
            if (stmtCode != null) {
                bodyCode.append(stmtCode);
            }
        }
        return bodyCode.toString();
    }

    @Override
    public String visit(AssignStatNode node) throws SemanticException {
        List<String> ids = node.getIds();
        List<ExprNode> exprs = node.getExprs();
        List<Boolean> isOutIds = node.getIsOutIds();

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            String exprCode = exprs.get(i).accept(this);
            indent();
            code.append(isOutIds.get(i) ? "*"+id : id).append(" = ").append(exprCode).append(";\n");
        }
        return ""; // AssignStatNode does not return a string directly
    }

    @Override
    public String visit(ProcCallStatNode node) throws SemanticException {
        String procName = node.getProcCall().getProcedureName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ProcExprNode> args = node.getProcCall().getArguments();
        for (int i = 0; i < args.size(); i++) {
            ProcExprNode arg = args.get(i);
            String argCode = arg.accept(this);
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
        return ""; // ProcCallStatNode does not return a string directly
    }

    @Override
    public String visit(ReturnStatNode node) throws SemanticException {
        if (node.getExprs().isEmpty()) {
            indent();
            code.append("return;\n");
        } else {
            StringJoiner returnJoiner = new StringJoiner(", ");
            for (ExprNode expr : node.getExprs()) {
                returnJoiner.add(expr.accept(this));
            }
            indent();
            code.append("return ").append(returnJoiner.toString()).append(";\n");
        }
        return ""; // ReturnStatNode does not return a string directly
    }

    @Override
    public String visit(WriteStatNode node) throws SemanticException {
        StringBuilder argsBuilder = new StringBuilder();
        List<IOArgNode> args = node.getArgs();
        for (int i = 0; i < args.size(); i++) {
            IOArgNode arg = args.get(i);
            argsBuilder.append(arg.accept(this));
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        indent();
        code.append("printf(").append("\"");
        for (IOArgNode arg : args) {
            if (arg instanceof IOArgStringLiteralNode) {
                String str = ((IOArgStringLiteralNode) arg).getValue();
                code.append(str.replace("\"", "\\\""));
            } else {
                code.append("%d "); // Simplistic: assuming integer for other types
            }
        }
        code.append("\"");
        if (!args.isEmpty()) {
            code.append(", ").append(argsBuilder.toString());
        }
        code.append(");\n");
        return ""; // WriteStatNode does not return a string directly
    }

    @Override
    public String visit(WriteReturnStatNode node) throws SemanticException {
        generateWriteStat(node.getArgs());
        return "";
    }

    private void generateWriteStat(List<IOArgNode> args) throws SemanticException {
        StringBuilder argsBuilder = new StringBuilder();
        indent();
        code.append("printf(").append("\"");
        for (IOArgNode arg : args) {
            if (arg instanceof IOArgStringLiteralNode) {
                String str = ((IOArgStringLiteralNode) arg).getValue();
                code.append(str.replace("\"", "\\\""));
            } else {
                // Determine the format specifier based on the type
                code.append("%d"); // Simplistic: assuming integer
            }
        }
        code.append("\"");
        if (!args.isEmpty()) {
            code.append(", ");
            for (int i = 0; i < args.size(); i++) {
                IOArgNode arg = args.get(i);
                if (!(arg instanceof IOArgStringLiteralNode)) {
                    String argCode = arg.accept(this);
                    argsBuilder.append(argCode);
                    if (i < args.size() - 1) {
                        argsBuilder.append(", ");
                    }
                }
            }
            code.append(argsBuilder.toString());
        }
        code.append(");\n");
    }

    @Override
    public String visit(ReadStatNode node) throws SemanticException {
        for (IOArgNode arg : node.getArgs()) {
            indent();
            code.append("scanf(\"%d\", ").append(arg.accept(this)).append(");\n"); // Simplistic: assuming integer
        }
        return ""; // ReadStatNode does not return a string directly
    }

    @Override
    public String visit(IfStatNode node) throws SemanticException {
        // Generate C 'if' statement
        String condition = node.getCondition().accept(this);
        indent();
        code.append("if (").append(condition).append(") {\n");
        increaseIndent();
        code.append(node.getThenBody().accept(this));
        decreaseIndent();
        indent();
        code.append("}\n");

        // Handle elif blocks
        for (ElifNode elif : node.getElifBlocks()) {
            String elifCondition = elif.getCondition().accept(this);
            indent();
            code.append("else if (").append(elifCondition).append(") {\n");
            increaseIndent();
            code.append(elif.getBody().accept(this));
            decreaseIndent();
            indent();
            code.append("}\n");
        }

        // Handle else block
        if (node.getElseBlock() != null) {
            indent();
            code.append("else {\n");
            increaseIndent();
            code.append(node.getElseBlock().accept(this));
            decreaseIndent();
            indent();
            code.append("}\n");
        }

        return ""; // IfStatNode does not return a string directly
    }

    @Override
    public String visit(WhileStatNode node) throws SemanticException {
        // Generate C 'while' loop
        String condition = node.getCondition().accept(this);
        indent();
        code.append("while (").append(condition).append(") {\n");
        increaseIndent();
        code.append(node.getBody().accept(this));
        decreaseIndent();
        indent();
        code.append("}\n");
        return ""; // WhileStatNode does not return a string directly
    }

    @Override
    public String visit(FunCallNode node) throws SemanticException {
        String functionName = node.getFunctionName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ExprNode> args = node.getArguments();
        for (int i = 0; i < args.size(); i++) {
            argsBuilder.append(args.get(i).accept(this));
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        return functionName + "(" + argsBuilder.toString() + ")";
    }

    @Override
    public String visit(ProcCallNode node) throws SemanticException {
        String procedureName = node.getProcedureName();
        StringBuilder argsBuilder = new StringBuilder();
        List<ProcExprNode> args = node.getArguments();
        for (int i = 0; i < args.size(); i++) {
            ProcExprNode arg = args.get(i);
            if (arg.isRef()) {
                argsBuilder.append("&").append(arg.getExpr().accept(this));
            } else {
                argsBuilder.append(arg.getExpr().accept(this));
            }
            if (i < args.size() - 1) {
                argsBuilder.append(", ");
            }
        }
        return procedureName + "(" + argsBuilder.toString() + ");\n";
    }

    @Override
    public String visit(ElifNode node) throws SemanticException {
        // Handled within IfStatNode
        return node.getBody().accept(this);
    }

    @Override
    public String visit(ElseNode node) throws SemanticException {
        // Handled within IfStatNode
        return node.getBody().accept(this);
    }

    @Override
    public String visit(IOArgIdentifierNode node) throws SemanticException {
        return node.getIdentifier();
    }

    @Override
    public String visit(IOArgStringLiteralNode node) throws SemanticException {
        return "\"" + node.getValue().replace("\"", "\\\"") + "\"";
    }

    @Override
    public String visit(IOArgBinaryNode node) throws SemanticException {
        String left = node.getLeft().accept(this);
        String right = node.getRight().accept(this);
        String operator = node.getOperator();
        return left + " " + operator + " " + right;
    }

    @Override
    public String visit(DollarExprNode node) throws SemanticException {
        return node.getExpr().accept(this);
    }

    @Override
    public String visit(ProcExprNode node) throws SemanticException {
        return node.getExpr().accept(this);
    }

    @Override
    public String visit(RealConstNode node) throws SemanticException {
        return Double.toString(node.getValue());
    }

    @Override
    public String visit(IntConstNode node) throws SemanticException {
        return Integer.toString(node.getValue());
    }

    @Override
    public String visit(StringConstNode node) throws SemanticException {
        return "\"" + node.getValue().replace("\"", "\\\"") + "\"";
    }

    @Override
    public String visit(IdentifierNode node) throws SemanticException {
        return node.getIsOutInProcedure() ? "*"+node.getName() : node.getName();
    }

    @Override
    public String visit(BooleanConstNode node) throws SemanticException {
        return node.getValue() ? "1" : "0"; // C uses 1 for true and 0 for false
    }

    @Override
    public String visit(BinaryExprNode node) throws SemanticException {
        // Ottieni il codice generato per i nodi sinistro e destro
        String leftCode = (String) node.getLeft().accept(this);
        String rightCode = (String) node.getRight().accept(this);

        // Mappa l'operatore al corrispondente in C
        String operator = mapOperatorToC(node.getOperator());

        // Genera il codice per l'espressione binaria
        return "(" + leftCode + " " + operator + " " + rightCode + ")";
    }

    /**
     * Mappa l'operatore del linguaggio sorgente all'operatore equivalente in C.
     */
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
                throw new IllegalArgumentException("Operatore non supportato: " + operator);
        }
    }

    @Override
    public String visit(UnaryExprNode node) throws SemanticException {
        String operator = node.getOperator();
        String expr = node.getExpr().accept(this);
        switch (operator) {
            case "uminus":
                return "(-" + expr + ")";
            case "not":
                return "(!" + expr + ")";
            default:
                throw new SemanticException("Operatore unario non riconosciuto: " + operator);
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
//            case VOID:
//                return "void";
            default:
                throw new SemanticException("Tipo non supportato: " + type);
        }
    }

    // Optional: Determine type from operator if needed
    private Type determineTypeFromOperator(String operator, String operandType) throws SemanticException {
        switch (operator) {
            case "+":
            case "-":
            case "*":
            case "/":
                if (operandType.equals("int")) {
                    return Type.INTEGER;
                } else if (operandType.equals("double")) {
                    return Type.REAL;
                }
                break;
            case "and":
            case "or":
                return Type.BOOLEAN;
            case ">":
            case ">=":
            case "<":
            case "<=":
            case "==":
            case "!=":
                return Type.BOOLEAN;
            default:
                throw new SemanticException("Operatore non riconosciuto: " + operator);
        }
        throw new SemanticException("Tipo dell'operando non compatibile per l'operatore: " + operator);
    }
}
