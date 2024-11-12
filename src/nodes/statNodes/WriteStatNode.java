package nodes.statNodes;

import nodes.IOArgNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class WriteStatNode extends StatNode {
    private List<IOArgNode> args;

    public WriteStatNode(List<IOArgNode> args) {
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