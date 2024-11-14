package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.List;

public class DeclNode implements Visitable {
    private List<String> ids;
    private Type type; // Può essere null se non specificato
    private List<ConstNode> consts; // Può essere null se non specificato

    public DeclNode(List<String> ids, Type type, List<ConstNode> consts) {
        this.ids = ids;
        this.type = type;
        this.consts = consts;
    }

    public List<String> getIds() {
        return ids;
    }

    public Type getType() {
        return type;
    }

    public List<ConstNode> getConsts() {
        return consts;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
