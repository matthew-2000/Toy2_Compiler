package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcParamNode implements Visitable {
    private String name;
    private boolean isOut;
    private String type;

    public ProcParamNode(String name, boolean isOut, String type) {
        this.name = name;
        this.isOut = isOut;
        this.type = type;
    }

    // Getter
    public String getName() {
        return name;
    }

    public boolean isOut() {
        return isOut;
    }

    public String getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}