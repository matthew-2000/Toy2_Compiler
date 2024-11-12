package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class ProcParamsNode implements Visitable {
    private List<ProcParamNode> params;

    public ProcParamsNode(List<ProcParamNode> params) {
        this.params = params;
    }

    public List<ProcParamNode> getParams() {
        return params;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
