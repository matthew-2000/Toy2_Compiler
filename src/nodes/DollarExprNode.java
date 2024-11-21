package nodes;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class DollarExprNode implements IOArgNode {
    private ExprNode expr;
    private Type type;

    public DollarExprNode(ExprNode expr) {
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
