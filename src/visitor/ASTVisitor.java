package visitor;

import nodes.ASTNode;

public interface ASTVisitor {
    void visit(ASTNode node);
}