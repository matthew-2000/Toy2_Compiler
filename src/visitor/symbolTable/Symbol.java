package visitor.symbolTable;

import java.util.List;

public class Symbol {
    private String name;
    private String type;  // Tipo del simbolo (es. int, float, funzione, ecc.)
    private boolean isFunction;  // Per distinguere tra variabili e funzioni
    private List<String> paramTypes;  // Tipi dei parametri (solo per funzioni e procedure)
    private List<Boolean> isOutParams;  // Specifica se i parametri sono OUT (solo per procedure)

    // Costruttore per variabili semplici
    public Symbol(String name, String type, boolean isFunction) {
        this.name = name;
        this.type = type;
        this.isFunction = isFunction;
    }

    // Costruttore per funzioni e procedure
    public Symbol(String name, String type, boolean isFunction, List<String> paramTypes, List<Boolean> isOutParams) {
        this.name = name;
        this.type = type;
        this.isFunction = isFunction;
        this.paramTypes = paramTypes;
        this.isOutParams = isOutParams;
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

    public List<String> getParamTypes() {
        return paramTypes;
    }

    public List<Boolean> getIsOutParams() {
        return isOutParams;
    }
}
