package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class IOArgNode implements Visitable {
    private String identifier;
    private String literal;

    public IOArgNode(String identifier, String literal) {
        this.identifier = identifier;
        this.literal = literal;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
