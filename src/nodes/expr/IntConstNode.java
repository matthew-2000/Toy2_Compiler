package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class IntConstNode implements ExprNode {
    private int value;

    public IntConstNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
