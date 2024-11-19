package nodes;

import nodes.expr.ExprNode;
import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.List;

public class FunCallNode implements ExprNode {
    private String functionName;
    private List<ExprNode> arguments;
    private Type type;
    private List<Type> returnTypes;

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
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    public List<Type> getReturnTypes() {
        return returnTypes;
    }

    public void setReturnTypes(List<Type> returnTypes) {
        this.returnTypes = returnTypes;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
