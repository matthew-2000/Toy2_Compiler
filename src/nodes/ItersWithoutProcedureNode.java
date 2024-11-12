package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ItersWithoutProcedureNode implements Visitable {
    private List<IterWithoutProcedureNode> iterList;

    public ItersWithoutProcedureNode(List<IterWithoutProcedureNode> iterList) {
        this.iterList = iterList;
    }

    public void addIter(IterWithoutProcedureNode iter) {
        iterList.add(iter);
    }

    public List<IterWithoutProcedureNode> getIterList() {
        return iterList;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
