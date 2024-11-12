package visitor;

import nodes.*;
import nodes.expr.*;
import nodes.statNodes.*;
import visitor.exception.SemanticException;

public interface Visitor<T> {

    // Nodo del programma principale
    T visit(ProgramNode node) throws SemanticException;

    // Nodi per le dichiarazioni top-level senza Procedure
    T visit(ItersWithoutProcedureNode node) throws SemanticException;
    T visit(IterWithoutProcedureNode node) throws SemanticException;

    // Nodi per le dichiarazioni top-level
    T visit(ItersNode node) throws SemanticException;
    T visit(IterNode node) throws SemanticException;

    // Nodi per le dichiarazioni di variabili
    T visit(VarDeclNode node) throws SemanticException;
    T visit(DeclNode node) throws SemanticException;
    T visit(ConstNode node) throws SemanticException;

    // Nodi per le funzioni
    T visit(FunctionNode node) throws SemanticException;
    T visit(FuncParamsNode node) throws SemanticException;
    T visit(ParamNode node) throws SemanticException;

    // Nodi per le procedure
    T visit(ProcedureNode node) throws SemanticException;
    T visit(ProcParamsNode node) throws SemanticException;
    T visit(ProcParamNode node) throws SemanticException;
    // Non è necessario un metodo visit per ProcParamIdNode se non è Visitable

    // Nodo per il corpo delle funzioni/procedure
    T visit(BodyNode node) throws SemanticException;

    // Nodi per le istruzioni
    T visit(AssignStatNode node) throws SemanticException;
    T visit(ProcCallStatNode node) throws SemanticException;
    T visit(ReturnStatNode node) throws SemanticException;
    T visit(WriteStatNode node) throws SemanticException;
    T visit(WriteReturnStatNode node) throws SemanticException;
    T visit(ReadStatNode node) throws SemanticException;
    T visit(IfStatNode node) throws SemanticException;
    T visit(WhileStatNode node) throws SemanticException;

    // Nodi per le chiamate a funzione e procedura
    T visit(FunCallNode node) throws SemanticException;
    T visit(ProcCallNode node) throws SemanticException;

    // Nodi per le strutture condizionali
    T visit(ElifNode node) throws SemanticException;
    T visit(ElseNode node) throws SemanticException;

    // Nodi per gli argomenti di I/O
    T visit(IOArgIdentifierNode node) throws SemanticException;
    T visit(IOArgStringLiteralNode node) throws SemanticException;
    T visit(IOArgBinaryNode node) throws SemanticException;
    T visit(DollarExprNode node) throws SemanticException;

    // Nodi per i parametri delle procedure
    T visit(ProcExprNode node) throws SemanticException;

    // Nodi per le espressioni
    T visit(RealConstNode node) throws SemanticException;
    T visit(IntConstNode node) throws SemanticException;
    T visit(StringConstNode node) throws SemanticException;
    T visit(IdentifierNode node) throws SemanticException;
    T visit(BooleanConstNode node) throws SemanticException;
    T visit(BinaryExprNode node) throws SemanticException;
    T visit(UnaryExprNode node) throws SemanticException;

    // Altri nodi...

    // Se hai altri nodi che implementano Visitable, aggiungi qui i metodi visit corrispondenti.

}
