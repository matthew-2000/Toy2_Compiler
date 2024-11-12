package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class BodyNode implements Visitable {
    private List<Visitable> statements;

    public BodyNode(List<Visitable> statements) {
        this.statements = statements;
    }

    public List<Visitable> getStatements() {
        return statements;
    }

    public void addStatement(Visitable statement) {
        statements.add(statement);
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
