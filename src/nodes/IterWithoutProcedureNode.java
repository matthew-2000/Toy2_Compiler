package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class IterWithoutProcedureNode implements Visitable {
    private List<Visitable> items;  // Lista di dichiarazioni e funzioni

    public IterWithoutProcedureNode(List<Visitable> items) {
        this.items = items;
    }

    public List<Visitable> getItems() {
        return items;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
