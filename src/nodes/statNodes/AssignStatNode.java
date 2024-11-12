package nodes.statNodes;

import nodes.expr.ExprNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class AssignStatNode extends StatNode {
    private List<String> ids;
    private List<ExprNode> exprs;

    public AssignStatNode(List<String> ids, List<ExprNode> exprs) {
        this.ids = ids;
        this.exprs = exprs;
    }

    public List<String> getIds() {
        return ids;
    }

    public List<ExprNode> getExprs() {
        return exprs;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
