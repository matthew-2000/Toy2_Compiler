package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

public class ProgramNode implements Visitable {
    private ItersWithoutProcedureNode itersWithoutProcedure;
    private ProcedureNode procedure;
    private ItersNode iters;

    public ProgramNode(ItersWithoutProcedureNode itersWithoutProcedure, ProcedureNode procedure, ItersNode iters) {
        this.itersWithoutProcedure = itersWithoutProcedure;
        this.procedure = procedure;
        this.iters = iters;
    }

    public ItersWithoutProcedureNode getItersWithoutProcedure() {
        return itersWithoutProcedure;
    }

    public ProcedureNode getProcedure() {
        return procedure;
    }

    public ItersNode getIters() {
        return iters;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
