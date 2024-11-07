package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class IfStatNode implements Visitable {
    private ExprNode condition;       // Condizione dell'if
    private BodyNode thenBody;        // Corpo dell'if
    private List<ElifNode> elifNodes; // Lista delle condizioni elif
    private ElseNode elseNode;        // Blocco else (opzionale)

    public IfStatNode(ExprNode condition, BodyNode thenBody, List<ElifNode> elifNodes, ElseNode elseNode) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elifNodes = elifNodes;
        this.elseNode = elseNode;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public BodyNode getThenBody() {
        return thenBody;
    }

    public List<ElifNode> getElifNodes() {
        return elifNodes;
    }

    public ElseNode getElseNode() {
        return elseNode;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
