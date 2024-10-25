package nodes;

import visitor.Visitable;
import visitor.Visitor;

public class ProgramNode implements Visitable {
    private IterWithoutProcedureNode iterWithoutProcedure;
    private ProcedureNode procedure;
    private IterNode iter;

    public ProgramNode(IterWithoutProcedureNode iterWithoutProcedure, ProcedureNode procedure, IterNode iter) {
        this.iterWithoutProcedure = iterWithoutProcedure;
        this.procedure = procedure;
        this.iter = iter;
    }

    public IterWithoutProcedureNode getIterWithoutProcedure() {
        return iterWithoutProcedure;
    }

    public void setIterWithoutProcedure(IterWithoutProcedureNode iterWithoutProcedure) {
        this.iterWithoutProcedure = iterWithoutProcedure;
    }

    public ProcedureNode getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureNode procedure) {
        this.procedure = procedure;
    }

    public IterNode getIter() {
        return iter;
    }

    public void setIter(IterNode iter) {
        this.iter = iter;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws Exception {
        return visitor.visit(this);
    }
}
