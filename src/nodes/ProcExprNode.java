package nodes;

import nodes.expr.ExprNode;
import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcExprNode implements Visitable {
    private ExprNode expr;
    private boolean isRef;

    public ProcExprNode(ExprNode expr, boolean isRef) {
        this.expr = expr;
        this.isRef = isRef;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public boolean isRef() {
        return isRef;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
