package nodes;

import nodes.expr.ExprNode;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class IfStatNode extends StatNode {
    private ExprNode condition;
    private BodyNode thenBody;
    private List<ElifNode> elifBlocks;
    private ElseNode elseBlock;

    public IfStatNode(ExprNode condition, BodyNode thenBody, List<ElifNode> elifBlocks, ElseNode elseBlock) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elifBlocks = elifBlocks;
        this.elseBlock = elseBlock;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public BodyNode getThenBody() {
        return thenBody;
    }

    public List<ElifNode> getElifBlocks() {
        return elifBlocks;
    }

    public ElseNode getElseBlock() {
        return elseBlock;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
