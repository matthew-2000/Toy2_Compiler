import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java_cup.runtime.Symbol;
import unisa.compilatori.parser;
import unisa.compilatori.sym;

public class Main {
    public static void main(String[] args) {
        // Percorso del file di test
        String filePath = "test/test2.txt";

        // Tentiamo di aprire il file di input
        try {
            // Creazione del lexer
            FileReader fileReader = new FileReader(filePath);
            Toy2Lexer lexer = new Toy2Lexer(fileReader);

            // Visualizza tutti i token trovati dal lexer
            System.out.println("=== Token riconosciuti dal lexer ===");
            Symbol token;
            while ((token = lexer.next_token()).sym != sym.EOF) {
                System.out.println("Token: " + getTokenName(token.sym) + " | Valore: " + token.value);
            }
            System.out.println("=== Fine dei token ===");

            // Reinizializza il lexer per il parsing
            fileReader = new FileReader(filePath);
            lexer = new Toy2Lexer(fileReader);

            // Creazione del parser con il lexer
            parser parser = new parser(lexer);

            // Esecuzione del parsing
            System.out.println("\n=== Avvio del parsing ===");
            Symbol result = parser.parse();
            System.out.println("=== Parsing completato con successo! ===");

        } catch (FileNotFoundException e) {
            System.err.println("Errore: il file " + filePath + " non è stato trovato.");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore durante l'analisi del file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getTokenName(int tokenSym) {
        switch (tokenSym) {
            case sym.VAR:
                return "VAR";
            case sym.PROC:
                return "PROC";
            case sym.ENDPROC:
                return "ENDPROC";
            case sym.FUNC:
                return "FUNC";
            case sym.ENDFUNC:
                return "ENDFUNC";
            case sym.IF:
                return "IF";
            case sym.THEN:
                return "THEN";
            case sym.ELSE:
                return "ELSE";
            case sym.ELIF:
                return "ELIF";
            case sym.ENDIF:
                return "ENDIF";
            case sym.WHILE:
                return "WHILE";
            case sym.DO:
                return "DO";
            case sym.ENDWHILE:
                return "ENDWHILE";
            case sym.RETURN:
                return "RETURN";
            case sym.NUMBER_LITERAL:
                return "NUMBER_LITERAL";
            case sym.STRING_LITERAL:
                return "STRING_LITERAL";
            case sym.TRUE:
                return "TRUE";
            case sym.FALSE:
                return "FALSE";
            case sym.IDENTIFIER:
                return "ID";
            case sym.ASSIGN:
                return "ASSIGN";
            case sym.PLUS:
                return "PLUS";
            case sym.MINUS:
                return "MINUS";
            case sym.TIMES:
                return "TIMES";
            case sym.DIV:
                return "DIV";
            case sym.EQ:
                return "EQ";
            case sym.NE:
                return "NE";
            case sym.LT:
                return "LT";
            case sym.LE:
                return "LE";
            case sym.GT:
                return "GT";
            case sym.GE:
                return "GE";
            case sym.AND:
                return "AND";
            case sym.OR:
                return "OR";
            case sym.NOT:
                return "NOT";
            case sym.LPAR:
                return "LPAR";
            case sym.RPAR:
                return "RPAR";
            case sym.SEMI:
                return "SEMI";
            case sym.COLON:
                return "COLON";
            case sym.COMMA:
                return "COMMA";
            case sym.DOLLAR:
                return "DOLLAR";
            case sym.WRITE:
                return "WRITE";
            case sym.WRITERETURN:
                return "WRITERETURN";
            case sym.READ:
                return "READ";
            case sym.REF:
                return "REF";
            case sym.ENDVAR:
                return "ENDVAR";
            case sym.REAL:
                return "REAL";
            case sym.INTEGER:
                return "INTEGER";
            case sym.STRING:
                return "STRING";
            case sym.BOOLEAN:
                return "BOOLEAN";
            case sym.UMINUS:
                return "UMINUS";
            case sym.TYPERETURN:
                return "TYPERETURN";
            case sym.OUT:
                return "OUT";
            case sym.EOF:
                return "EOF";
            default:
                return "UNKNOWN";
        }
    }
}