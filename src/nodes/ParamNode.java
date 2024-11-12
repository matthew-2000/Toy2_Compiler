package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ParamNode implements Visitable {
    private String name;
    private String type;

    public ParamNode(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
