package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class IterNode implements Visitable {
    private List<Visitable> items;  // Lista di dichiarazioni, funzioni e procedure

    public IterNode(List<Visitable> items) {
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
