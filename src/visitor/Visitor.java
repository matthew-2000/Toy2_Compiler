package visitor;

import nodes.ProgramNode;

public interface Visitor<T> {
    public Object visit(ProgramNode node) throws Exception;
}
