package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class FuncParamsNode implements Visitable {
    private List<ParamNode> params;

    public FuncParamsNode(List<ParamNode> params) {
        this.params = params;
    }

    public List<ParamNode> getParams() {
        return params;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
