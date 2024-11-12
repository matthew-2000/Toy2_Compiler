package nodes.expr;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class IdentifierNode implements ExprNode {
    private String name;

    public IdentifierNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
