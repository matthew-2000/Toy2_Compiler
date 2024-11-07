package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class WhileStatNode implements Visitable {
    private ExprNode condition;
    private BodyNode body;

    public WhileStatNode(ExprNode condition, BodyNode body) {
        this.condition = condition;
        this.body = body;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public BodyNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
