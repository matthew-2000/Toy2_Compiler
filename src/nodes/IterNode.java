package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class IterNode implements Visitable {
    private Visitable declaration; // Può essere VarDeclNode, FunctionNode o ProcedureNode

    public IterNode(Visitable declaration) {
        this.declaration = declaration;
    }

    public Visitable getDeclaration() {
        return declaration;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
