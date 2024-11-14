package nodes;

import nodes.expr.ExprNode;
import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class ProcExprNode implements ExprNode {
    private ExprNode expr;
    private boolean isRef;
    private Type type;

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
