package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ConstsNode implements Visitable {
    private List<ConstNode> constants;

    public ConstsNode(List<ConstNode> constants) {
        this.constants = constants;
    }

    public List<ConstNode> getConstants() {
        return constants;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
