package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;
import visitor.utils.Type;

import java.util.ArrayList;
import java.util.List;

public class DeclNode implements Visitable {
    private List<String> ids;
    private Type type;
    private List<Type> constsType;
    private List<ConstNode> consts;

    public DeclNode(List<String> ids, Type type, List<ConstNode> consts) {
        this.ids = ids;
        this.type = type;
        this.consts = consts;
        if (consts != null) {
            constsType = new ArrayList<Type>();
            for (ConstNode c : consts) {
                constsType.add(c.getType());
            }
        }
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

    public List<Type> getConstsType() {
        return constsType;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
