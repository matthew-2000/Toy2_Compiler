package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

import java.util.List;

public class ProcedureNode implements Visitable {
    private String procedureName;      // Nome della procedura
    private ProcParamsNode params;     // Parametri della procedura
    private BodyNode body;             // Corpo della procedura

    public ProcedureNode(String procedureName, ProcParamsNode params, BodyNode body) {
        this.procedureName = procedureName;
        this.params = params;
        this.body = body;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public ProcParamsNode getParams() {
        return params;
    }

    public BodyNode getBody() {
        return body;
    }

    // Metodo accept per il visitor
    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
