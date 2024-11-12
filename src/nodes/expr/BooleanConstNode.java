package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class BooleanConstNode implements ExprNode {
    private boolean value;

    public BooleanConstNode(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
