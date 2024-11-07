package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ConstNode implements Visitable {
    private String value;
    private String type;  // Tipo del nodo, assegnato in fase di visita

    public ConstNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
