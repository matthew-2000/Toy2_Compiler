package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class FuncParamsNode implements Visitable {
    private String identifier;      // Nome del parametro
    private TypeNode type;          // Tipo del parametro
    private OtherFuncParamsNode otherParams; // Parametri aggiuntivi

    public FuncParamsNode(String identifier, TypeNode type, OtherFuncParamsNode otherParams) {
        this.identifier = identifier;
        this.type = type;
        this.otherParams = otherParams;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TypeNode getType() {
        return type;
    }

    public OtherFuncParamsNode getOtherParams() {
        return otherParams;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
