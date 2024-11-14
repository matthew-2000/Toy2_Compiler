package visitor.symbolTable;

import visitor.utils.Type;

import java.util.List;

public class Symbol {
    private String name;
    private Type type;  // Tipo del simbolo (es. integer, real, function, ecc.)
    private SymbolKind kind;  // Variabile, Funzione o Procedura
    private boolean isParameter = false;
    private List<Type> paramTypes;  // Tipi dei parametri (per funzioni e procedure)
    private List<Boolean> isOutParams;  // Flag per i parametri OUT (per procedure)
    private List<Type> returnTypes;   // Tipi di ritorno (per funzioni)

    // Costruttore per variabili
    public Symbol(String name, Type type, SymbolKind kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }

    // Costruttore per variabili parametri di funzione
    public Symbol(String name, Type type, SymbolKind kind, boolean isParameter) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.isParameter = isParameter;
    }

    // Costruttore per funzioni
    public Symbol(String name, List<Type> paramTypes, List<Type> returnTypes, SymbolKind kind) {
        this.name = name;
        this.kind = kind;
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    // Costruttore per procedure
    public Symbol(String name, SymbolKind kind, List<Type> paramTypes, List<Boolean> isOutParams) {
        this.name = name;
        this.kind = kind;
        this.paramTypes = paramTypes;
        this.isOutParams = isOutParams;
    }

    // Getter e Setter
    public String getName() { return name; }
    public Type getType() { return type; }
    public SymbolKind getKind() { return kind; }
    public List<Type> getParamTypes() { return paramTypes; }
    public void setParamTypes(List<Type> paramTypes) { this.paramTypes = paramTypes; }
    public List<Boolean> getIsOutParams() { return isOutParams; }
    public void setIsOutParams(List<Boolean> isOutParams) { this.isOutParams = isOutParams; }
    public List<Type> getReturnTypes() { return returnTypes; }
    public void setReturnTypes(List<Type> returnTypes) { this.returnTypes = returnTypes; }

    public boolean isParameter() {
        return isParameter;
    }
    public void setIsParameter(boolean parameter) {isParameter = parameter; }

    @Override
    public String toString() {
        return "Symbol{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", kind=" + kind +
                ", isParameter=" + isParameter +
                ", paramTypes=" + paramTypes +
                ", isOutParams=" + isOutParams +
                ", returnTypes=" + returnTypes +
                "}" + "\n" ;
    }

}
