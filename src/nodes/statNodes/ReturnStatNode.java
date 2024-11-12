package nodes.statNodes;

import nodes.expr.ExprNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ReturnStatNode extends StatNode {
    private List<ExprNode> exprs;

    public ReturnStatNode(List<ExprNode> exprs) {
        this.exprs = exprs;
    }

    public List<ExprNode> getExprs() {
        return exprs;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
