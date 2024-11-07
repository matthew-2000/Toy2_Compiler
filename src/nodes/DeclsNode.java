package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class DeclsNode implements Visitable {
    private List<VarDeclNode> declarations;

    public DeclsNode(List<VarDeclNode> declarations) {
        this.declarations = declarations;
    }

    public List<VarDeclNode> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
