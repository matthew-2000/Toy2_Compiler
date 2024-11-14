package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.List;

public class FunctionNode implements Visitable {
    private String name;
    private FuncParamsNode params;
    private List<Type> returnTypes;
    private BodyNode body;

    public FunctionNode(String name, FuncParamsNode params, List<Type> returnTypes, BodyNode body) {
        this.name = name;
        this.params = params;
        this.returnTypes = returnTypes;
        this.body = body;
    }

    // Getter
    public String getName() {
        return name;
    }

    public FuncParamsNode getParams() {
        return params;
    }

    public List<Type> getReturnTypes() {
        return returnTypes;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
