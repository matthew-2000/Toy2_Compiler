package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class IntConstNode implements ExprNode {
    private int value;
    private Type type;

    public IntConstNode(int value) {
        this.value = value;
    }

    public int getValue() {
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
