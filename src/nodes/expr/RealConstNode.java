package nodes.expr;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class RealConstNode implements ExprNode {
    private double value;

    public RealConstNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
