package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class FunctionNode implements Visitable {
    private String functionName;
    private FuncParamsNode params;
    private TypesNode returnType;
    private BodyNode body;

    public FunctionNode(String functionName, FuncParamsNode params, TypesNode returnType, BodyNode body) {
        this.functionName = functionName;
        this.params = params;
        this.returnType = returnType;
        this.body = body;
    }

    public String getFunctionName() {
        return functionName;
    }

    public FuncParamsNode getParams() {
        return params;
    }

    public TypesNode getReturnType() {
        return returnType;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
