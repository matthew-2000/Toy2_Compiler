package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class BinaryExprNode implements ExprNode {
    private ExprNode left;
    private ExprNode right;
    private String operator;
    private Type type;

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
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }

}
