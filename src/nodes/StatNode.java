package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class StatNode implements Visitable {
    // Placeholder per il tipo di istruzione (ad es. assegnazione, chiamata a procedura)
    private String statementType;
    private IdsNode ids;
    private ExprsNode expressions;

    public StatNode(String statementType, IdsNode ids, ExprsNode expressions) {
        this.statementType = statementType;
        this.ids = ids;
        this.expressions = expressions;
    }

    public String getStatementType() {
        return statementType;
    }

    public IdsNode getIds() {
        return ids;
    }

    public ExprsNode getExpressions() {
        return expressions;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
