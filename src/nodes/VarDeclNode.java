package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import java.util.List;

public class VarDeclNode implements Visitable {
    private List<DeclNode> decls;

    public VarDeclNode(List<DeclNode> decls) {
        this.decls = decls;
    }

    public List<DeclNode> getDecls() {
        return decls;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
