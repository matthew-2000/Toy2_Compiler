package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcParamsNode implements Visitable {
    private String identifier;        // Nome del parametro
    private TypeNode type;            // Tipo del parametro
    private boolean isOutParam;       // Se il parametro è un parametro "OUT"
    private OtherProcParamsNode otherParams; // Parametri aggiuntivi

    public ProcParamsNode(String identifier, TypeNode type, boolean isOutParam, OtherProcParamsNode otherParams) {
        this.identifier = identifier;
        this.type = type;
        this.isOutParam = isOutParam;
        this.otherParams = otherParams;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TypeNode getType() {
        return type;
    }

    public boolean isOutParam() {
        return isOutParam;
    }

    public OtherProcParamsNode getOtherParams() {
        return otherParams;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
