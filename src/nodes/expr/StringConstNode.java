package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class StringConstNode implements ExprNode {
    private String value;

    public StringConstNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
