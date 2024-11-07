package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class VarDeclNode implements Visitable {
    private IdsNode ids;      // Identificatori dichiarati
    private TypeNode type;    // Tipo della dichiarazione
    private ConstsNode consts; // Costanti per eventuale assegnazione

    public VarDeclNode(IdsNode ids, TypeNode type, ConstsNode consts) {
        this.ids = ids;
        this.type = type;
        this.consts = consts;
    }

    public IdsNode getIds() {
        return ids;
    }

    public TypeNode getType() {
        return type;
    }

    public ConstsNode getConsts() {
        return consts;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
