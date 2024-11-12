package nodes;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class IOArgStringLiteralNode implements IOArgNode {
    private String value;

    public IOArgStringLiteralNode(String value) {
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
