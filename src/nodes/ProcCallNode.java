package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ProcCallNode implements Visitable {
    private String procedureName;       // Nome della procedura
    private List<ExprNode> arguments;   // Argomenti della procedura

    public ProcCallNode(String procedureName, List<ExprNode> arguments) {
        this.procedureName = procedureName;
        this.arguments = arguments;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
