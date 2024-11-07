package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ElifsNode implements Visitable {
    private List<ElifNode> elifBlocks;

    public ElifsNode(List<ElifNode> elifBlocks) {
        this.elifBlocks = elifBlocks;
    }

    public List<ElifNode> getElifBlocks() {
        return elifBlocks;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
