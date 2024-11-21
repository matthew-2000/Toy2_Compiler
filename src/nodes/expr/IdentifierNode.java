package nodes.expr;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class IdentifierNode implements ExprNode {
    private String name;
    private Type type;
    private boolean isOutInProcedure = false;

    public IdentifierNode(String name) {
        this.name = name;
    }

    public IdentifierNode(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    public void setIsOutInProcedure(boolean isOutInProcedure) {
        this.isOutInProcedure = isOutInProcedure;
    }

    public boolean getIsOutInProcedure() {
        return isOutInProcedure;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
