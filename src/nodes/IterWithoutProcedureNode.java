package nodes;

import visitor.Visitable;
import visitor.Visitor;
import visitor.exception.SemanticException;

import java.util.ArrayList;
import java.util.List;

public class IterWithoutProcedureNode implements Visitable {
    private List<Visitable> items;  // Lista di dichiarazioni e funzioni

    public IterWithoutProcedureNode(List<Visitable> items) {
        this.items = items;
    }

    // Costruttore per inizializzare una lista vuota
    public IterWithoutProcedureNode() {
        this.items = new ArrayList<>();
    }

    // Metodo per aggiungere singoli elementi, adatto alle regole CUP
    public void addItem(Visitable item) {
        items.add(item);
    }

    public List<Visitable> getItems() {
        return items;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) throws SemanticException {
        return visitor.visit(this);
    }
}
