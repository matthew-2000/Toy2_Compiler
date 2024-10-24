package nodes;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private String type;  // Tipo del nodo (es. "VarDecl", "Expr")
    private String value; // Valore del nodo (es. nome della variabile, valore costante)
    private List<ASTNode> children; // Figli di questo nodo

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        children.add(child);
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public List<ASTNode> getChildren() {
        return children;
    }
}
