package visitor;

import nodes.*;

public interface Visitor<T> {
    T visit(ProgramNode node) throws Exception;
//    T visit(FunctionNode node) throws Exception;
//    T visit(ProcedureNode node) throws Exception;
//    T visit(VarDeclNode node) throws Exception;
//    T visit(AssignNode node) throws Exception;
    // Aggiungi altri metodi per i nodi che vuoi visitare, come IfNode, WhileNode, ecc.
}