package nodes.stat;

import nodes.IOArgNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ReadStatNode extends StatNode {
    private List<IOArgNode> args;

    public ReadStatNode(List<IOArgNode> args) {
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
