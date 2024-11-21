package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class BooleanConstNode implements ExprNode {
    private boolean value;
    private Type type;

    public BooleanConstNode(boolean value) {
        this.value = value;
    }

    public BooleanConstNode(boolean value, Type type) {
        this.value = value;
        this.type = type;
    }

    public boolean getValue() {
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
