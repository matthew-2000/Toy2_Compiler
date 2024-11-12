package nodes;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class IOArgIdentifierNode implements IOArgNode {
    private String identifier;

    public IOArgIdentifierNode(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}

