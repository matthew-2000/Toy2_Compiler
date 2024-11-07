package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class BodyNode implements Visitable {
    private List<StatNode> statements;

    public BodyNode(List<StatNode> statements) {
        this.statements = statements;
    }

    public List<StatNode> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
