package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ExprNode implements Visitable {
    private String operator;         // Operatore come "+", "AND", "NOT"
    private ExprNode left;           // Operando sinistro per operazioni binarie
    private ExprNode right;          // Operando destro per operazioni binarie
    private ExprNode singleOperand;  // Operando singolo per operazioni unarie
    private String literalType;      // Tipo del literal, se si tratta di un valore costante (ad es. "INTEGER", "REAL")
    private String identifier;       // Identificatore se si tratta di una variabile
    private boolean isIdentifier;

    // Costruttore per operatori binari
    public ExprNode(String operator, ExprNode left, ExprNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    // Costruttore per operatori unari
    public ExprNode(String operator, ExprNode singleOperand) {
        this.operator = operator;
        this.singleOperand = singleOperand;
    }

    // Costruttore per valori letterali
    public ExprNode(String literalType) {
        this.literalType = literalType;
    }

    // Costruttore per identificatori
    public ExprNode(String identifier, boolean isIdentifier) {
        this.identifier = identifier;
        this.isIdentifier = isIdentifier;
    }

    public String getOperator() {
        return operator;
    }

    public ExprNode getLeft() {
        return left;
    }

    public ExprNode getRight() {
        return right;
    }

    public ExprNode getSingleOperand() {
        return singleOperand;
    }

    public String getLiteralType() {
        return literalType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isIdentifier() {
        return isIdentifier;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
