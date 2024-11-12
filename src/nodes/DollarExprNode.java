package nodes;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class DollarExprNode implements IOArgNode {
    private ExprNode expr;

    public DollarExprNode(ExprNode expr) {
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
