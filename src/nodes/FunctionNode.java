package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class FunctionNode implements Visitable {
    private String name;
    private FuncParamsNode params;
    private List<String> returnTypes;
    private BodyNode body;

    public FunctionNode(String name, FuncParamsNode params, List<String> returnTypes, BodyNode body) {
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

    public List<String> getReturnTypes() {
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
