package nodes.statNodes;

import nodes.IOArgNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class WriteReturnStatNode extends StatNode {
    private List<IOArgNode> args;

    public WriteReturnStatNode(List<IOArgNode> args) {
        this.args = args;
    }

    public List<IOArgNode> getArgs() {
        return args;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
