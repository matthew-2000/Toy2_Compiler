package visitor.symbolTable;

public class Symbol {
    private String name;
    private String type;  // Tipo del simbolo (es. int, float, funzione, ecc.)
    private boolean isFunction;  // Per distinguere tra variabili e funzioni

    public Symbol(String name, String type, boolean isFunction) {
        this.name = name;
        this.type = type;
        this.isFunction = isFunction;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFunction() {
        return isFunction;
    }
}
