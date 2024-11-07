package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class TypesNode implements Visitable {
    private List<TypeNode> types;

    public TypesNode(List<TypeNode> types) {
        this.types = types;
    }

    public List<TypeNode> getTypes() {
        return types;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
