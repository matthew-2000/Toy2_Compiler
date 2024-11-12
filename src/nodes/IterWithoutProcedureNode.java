package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class IterWithoutProcedureNode implements Visitable {
    private Visitable declaration; // Può essere VarDeclNode o FunctionNode

    public IterWithoutProcedureNode(Visitable declaration) {
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
