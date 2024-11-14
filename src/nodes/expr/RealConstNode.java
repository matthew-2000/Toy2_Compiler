package nodes.expr;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class RealConstNode implements ExprNode {
    private double value;
    private Type type;

    public RealConstNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
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
