package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ProcExprsNode implements Visitable {
    private List<ExprNode> arguments;
    private List<Boolean> isRef; // Lista che indica se ciascun argomento è passato per riferimento

    public ProcExprsNode(List<ExprNode> arguments, List<Boolean> isRef) {
        this.arguments = arguments;
        this.isRef = isRef;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    public List<Boolean> getIsRef() {
        return isRef;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
