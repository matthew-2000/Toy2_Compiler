package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class TypeNode implements Visitable {
    private String type;

    public TypeNode(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
