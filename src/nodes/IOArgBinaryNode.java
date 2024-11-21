package nodes;

import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public class IOArgBinaryNode implements IOArgNode {
    private IOArgNode left;
    private IOArgNode right;
    private String operator;  // In questo caso, sarà sempre "+"
    private Type type;

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

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
