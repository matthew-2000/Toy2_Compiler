package nodes.expr;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

public interface ExprNode extends Visitable {
    Type getType();
    void setType(Type type);
}
