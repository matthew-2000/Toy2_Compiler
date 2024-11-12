package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class UnaryExprNode implements ExprNode {
    private ExprNode expr;
    private String operator;

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
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
