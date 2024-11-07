package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcParamIdNode implements Visitable {
    private String identifier;   // Nome del parametro
    private boolean isOutParam;  // Indica se il parametro è `OUT`

    public ProcParamIdNode(String identifier, boolean isOutParam) {
        this.identifier = identifier;
        this.isOutParam = isOutParam;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isOutParam() {
        return isOutParam;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
