package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class OtherProcParamsNode implements Visitable {
    private List<ProcParamsNode> additionalParams;

    public OtherProcParamsNode(List<ProcParamsNode> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public List<ProcParamsNode> getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
