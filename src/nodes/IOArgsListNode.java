package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class IOArgsListNode implements Visitable {
    private List<IOArgNode> ioArgs;

    public IOArgsListNode(List<IOArgNode> ioArgs) {
        this.ioArgs = ioArgs;
    }

    public List<IOArgNode> getIoArgs() {
        return ioArgs;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
