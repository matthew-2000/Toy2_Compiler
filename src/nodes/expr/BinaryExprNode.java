package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class BinaryExprNode implements ExprNode {
    private ExprNode left;
    private ExprNode right;
    private String operator;

    public BinaryExprNode(ExprNode left, ExprNode right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ExprNode getLeft() {
        return left;
    }

    public ExprNode getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
