package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcParamNode implements Visitable {
    private String name;
    private String type;
    private boolean isOut;

    public ProcParamNode(String name, String type, boolean isOut) {
        this.name = name;
        this.type = type;
        this.isOut = isOut;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isOut() {
        return isOut;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}