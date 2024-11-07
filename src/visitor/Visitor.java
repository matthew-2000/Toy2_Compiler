package visitor;

import nodes.*;
import visitor.exception.SemanticException;

public interface Visitor<T> {
    // Nodo principale del programma
    T visit(ProgramNode node) throws SemanticException;

    // Dichiarazioni di variabili, funzioni e procedure
    T visit(VarDeclNode node) throws SemanticException;
    T visit(FunctionNode node) throws SemanticException;
    T visit(ProcedureNode node) throws SemanticException;
    T visit(FuncParamsNode node) throws SemanticException;
    T visit(ProcParamsNode node) throws SemanticException;
    T visit(ProcParamIdNode node) throws SemanticException;
    T visit(OtherFuncParamsNode node) throws SemanticException; // Nuovo metodo
    T visit(OtherProcParamsNode node) throws SemanticException; // Nuovo metodo

    // Nodi per tipi e costanti
    T visit(ConstNode node) throws SemanticException;
    T visit(TypeNode node) throws SemanticException;
    T visit(IdsNode node) throws SemanticException;
    T visit(TypesNode node) throws SemanticException; // Nuovo metodo

    // Nodi per le espressioni
    T visit(ExprNode node) throws SemanticException;
    T visit(FunCallNode node) throws SemanticException;
    T visit(ProcCallNode node) throws SemanticException;

    // Nodi per le istruzioni di controllo
    T visit(IfStatNode node) throws SemanticException;
    T visit(ElifNode node) throws SemanticException;
    T visit(ElseNode node) throws SemanticException;
    T visit(ElifsNode node) throws SemanticException; // Nuovo metodo
    T visit(WhileStatNode node) throws SemanticException;

    // Corpo di funzioni e procedure
    T visit(BodyNode node) throws SemanticException;

    // Dichiarazione di istruzioni
    T visit(StatNode node) throws SemanticException;
    T visit(IOArgsListNode node) throws SemanticException;
    T visit(IOArgNode node) throws SemanticException;

    // Liste e altre strutture di supporto
    T visit(IterWithoutProcedureNode node) throws SemanticException;
    T visit(IterNode node) throws SemanticException;
    T visit(DeclsNode node) throws SemanticException;
    T visit(ExprsNode node) throws SemanticException;
    T visit(ConstsNode node) throws SemanticException;
    T visit(ProcExprsNode node) throws SemanticException;
}
