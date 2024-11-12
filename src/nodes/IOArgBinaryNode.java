package nodes;

import visitor.Visitor;
import visitor.exception.SemanticException;

public class IOArgBinaryNode implements IOArgNode {
    private IOArgNode left;
    private IOArgNode right;
    private String operator;  // In questo caso, sarà sempre "+"

    public IOArgBinaryNode(IOArgNode left, IOArgNode right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public IOArgNode getLeft() {
        return left;
    }

    public IOArgNode getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
