package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class UnaryExprNode implements ExprNode {
    private ExprNode expr;
    private String operator;
    private Type type;

    public UnaryExprNode(ExprNode expr, String operator) {
        this.expr = expr;
        this.operator = operator;
    }

    public ExprNode getExpr() {
        return expr;
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
