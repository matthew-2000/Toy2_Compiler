package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ItersNode implements Visitable {
    private List<IterNode> iterList;

    public ItersNode(List<IterNode> iterList) {
        this.iterList = iterList;
    }

    public void addIter(IterNode iter) {
        iterList.add(iter);
    }

    public List<IterNode> getIterList() {
        return iterList;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
