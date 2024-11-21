package nodes;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class IOArgIdentifierNode implements IOArgNode {
    private String identifier;
    private Type type;

    public IOArgIdentifierNode(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}

