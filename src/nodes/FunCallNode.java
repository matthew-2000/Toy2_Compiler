package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class FunCallNode implements Visitable {
    private String functionName;       // Nome della funzione
    private List<ExprNode> arguments;  // Argomenti della funzione

    public FunCallNode(String functionName, List<ExprNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
