package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

import java.util.List;

public class IdsNode implements Visitable {
    private List<String> identifiers;

    public IdsNode(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
