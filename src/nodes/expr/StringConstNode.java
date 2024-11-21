package nodes.expr;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class StringConstNode implements ExprNode {
    private String value;
    private Type type;

    public StringConstNode(String value) {
        this.value = value;
    }

    public StringConstNode(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
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
