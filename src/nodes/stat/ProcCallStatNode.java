package nodes.stat;

import nodes.ProcCallNode;
import nodes.StatNode;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProcCallStatNode extends StatNode {
    private ProcCallNode procCall;

    public ProcCallStatNode(ProcCallNode procCall) {
        this.procCall = procCall;
    }

    public ProcCallNode getProcCall() {
        return procCall;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
