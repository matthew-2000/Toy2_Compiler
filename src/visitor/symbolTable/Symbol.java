package visitor.symbolTable;

import java.util.List;

public class Symbol {
    private String name;
    private String type;  // Tipo del simbolo (es. integer, real, function, ecc.)
    private SymbolKind kind;  // Variabile, Funzione o Procedura
    private List<String> paramTypes;  // Tipi dei parametri (per funzioni e procedure)
    private List<Boolean> isOutParams;  // Flag per i parametri OUT (per procedure)
    private List<String> returnTypes;   // Tipi di ritorno (per funzioni)

    // Costruttore per variabili
    public Symbol(String name, String type, SymbolKind kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }

    // Costruttore per funzioni
    public Symbol(String name, List<String> paramTypes, List<String> returnTypes, SymbolKind kind) {
        this.name = name;
        this.kind = kind;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    // Costruttore per procedure
    public Symbol(String name, SymbolKind kind, List<String> paramTypes, List<Boolean> isOutParams) {
        this.name = name;
        this.kind = kind;
        this.paramTypes = paramTypes;
        this.isOutParams = isOutParams;
    }

    // Getter e Setter
    public String getName() { return name; }
    public String getType() { return type; }
    public SymbolKind getKind() { return kind; }
    public List<String> getParamTypes() { return paramTypes; }
    public void setParamTypes(List<String> paramTypes) { this.paramTypes = paramTypes; }
    public List<Boolean> getIsOutParams() { return isOutParams; }
    public void setIsOutParams(List<Boolean> isOutParams) { this.isOutParams = isOutParams; }
    public List<String> getReturnTypes() { return returnTypes; }
    public void setReturnTypes(List<String> returnTypes) { this.returnTypes = returnTypes; }

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", kind=" + kind +
                ", paramTypes=" + paramTypes +
                ", isOutParams=" + isOutParams +
                ", returnTypes=" + returnTypes +
                "}" + "\n" ;
    }
}
