package nodes;

import nodes.expr.ExprNode;
import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ProcCallNode implements Visitable {
    private String procedureName;
    private List<ProcExprNode> arguments;

    public ProcCallNode(String procedureName, List<ProcExprNode> arguments) {
        this.procedureName = procedureName;
        this.arguments = arguments;
    }

    public String getProcedureName() {
        return procedureName;
    }

    public List<ProcExprNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
