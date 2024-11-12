package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcedureNode implements Visitable {
    private String name;
    private ProcParamsNode params;
    private BodyNode body;

    public ProcedureNode(String name, ProcParamsNode params, BodyNode body) {
        this.name = name;
        this.params = params;
        this.body = body;
    }

    // Getter
    public String getName() {
        return name;
    }

    public ProcParamsNode getParams() {
        return params;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
