package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class ParamNode implements Visitable {
    private String name;
    private Type type;

    public ParamNode(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    // Getter
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
