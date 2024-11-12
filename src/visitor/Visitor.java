package visitor;

import nodes.*;
import visitor.exception.SemanticException;

public interface Visitor<T> {
    // Nodo principale del programma
    T visit(ProgramNode node) throws SemanticException;

    // Nodi per le dichiarazioni top-level
    T visit(ItersWithoutProcedureNode node) throws SemanticException;
    T visit(IterWithoutProcedureNode node) throws SemanticException;
    T visit(ItersNode node) throws SemanticException;
    T visit(IterNode node) throws SemanticException;

    // Dichiarazioni di variabili, funzioni e procedure
    T visit(VarDeclNode node) throws SemanticException;
    T visit(FunctionNode node) throws SemanticException;
    T visit(ProcedureNode node) throws SemanticException;

    // Nodi per parametri e tipi
    T visit(FuncParamsNode node) throws SemanticException;
    T visit(OtherFuncParamsNode node) throws SemanticException;
    T visit(ProcParamsNode node) throws SemanticException;
    T visit(OtherProcParamsNode node) throws SemanticException;
    T visit(ProcParamNode node) throws SemanticException;
    T visit(TypeNode node) throws SemanticException;
    T visit(TypesNode node) throws SemanticException;
    T visit(ParamNode paramNode) throws SemanticException;

    // Nodi per dichiarazioni interne
    T visit(DeclNode node) throws SemanticException;
    T visit(IdsNode node) throws SemanticException;
    T visit(ConstNode node) throws SemanticException;

    // Nodi per il corpo e le istruzioni
    T visit(BodyNode node) throws SemanticException;
    T visit(StatNode node) throws SemanticException;

    // Nodi per le espressioni e chiamate
    T visit(ExprNode node) throws SemanticException;
    T visit(ExprsNode node) throws SemanticException;
    T visit(FunCallNode node) throws SemanticException;
    T visit(ProcCallNode node) throws SemanticException;
    T visit(ProcExprsNode node) throws SemanticException;

    // Nodi per strutture di controllo
    T visit(IfStatNode node) throws SemanticException;
    T visit(ElifNode node) throws SemanticException;
    T visit(ElifsNode node) throws SemanticException;
    T visit(ElseNode node) throws SemanticException;
    T visit(WhileStatNode node) throws SemanticException;

    // Nodi per Input/Output
    T visit(IOArgsListNode node) throws SemanticException;
    T visit(IOArgNode node) throws SemanticException;

    // Aggiungi altri metodi visit per eventuali altri nodi della grammatica...
}