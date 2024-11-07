package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class OtherFuncParamsNode implements Visitable {
    private List<FuncParamsNode> additionalParams;

    public OtherFuncParamsNode(List<FuncParamsNode> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public List<FuncParamsNode> getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
