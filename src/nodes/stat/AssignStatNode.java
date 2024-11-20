package nodes.stat;

import nodes.expr.ExprNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;

import java.util.ArrayList;
import java.util.List;

public class AssignStatNode extends StatNode {
    private List<String> ids;
    private List<Boolean> isOutIds;
    private List<ExprNode> exprs;

    public AssignStatNode(List<String> ids, List<ExprNode> exprs) {
        this.ids = ids;
        this.exprs = exprs;
        this.isOutIds = new ArrayList<>();
    }

    public List<String> getIds() {
        return ids;
    }

    public List<ExprNode> getExprs() {
        return exprs;
    }

    public List<Boolean> getIsOutIds() {return isOutIds;}

    public void setIsOutId(boolean isOutId) {
        isOutIds.add(isOutId);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
