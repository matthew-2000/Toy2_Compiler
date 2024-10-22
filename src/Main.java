import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java_cup.runtime.Symbol;

public class Main {
    public static void main(String[] args) {
        // Percorso del file di test
        String filePath = "test/TestProgram.txt";

        // Tentiamo di aprire il file di input
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Creiamo un'istanza del lexer generato
            Toy2Lexer lexer = new Toy2Lexer(bufferedReader);

            // Leggiamo i token e li stampiamo
            Symbol token;
            while ((token = lexer.next_token()).sym != sym.EOF) {
                System.out.println("Token: " + getTokenName(token.sym) + " | Valore: " + token.value);
            }

            // Chiudiamo il reader dopo l'elaborazione
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Errore: il file " + filePath + " non è stato trovato.");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore durante l'analisi del file: " + e.getMessage());
        }
    }

    // Metodo che restituisce il nome del token in base al suo valore sym
    private static String getTokenName(int simbol) {
        switch (simbol) {
            case sym.VAR: return "VAR";
            case sym.PROC: return "PROC";
            case sym.ENDPROC: return "ENDPROC";
            case sym.FUNC: return "FUNC";
            case sym.ENDFUNC: return "ENDFUNC";
            case sym.IF: return "IF";
            case sym.THEN: return "THEN";
            case sym.ELSE: return "ELSE";
            case sym.ELIF: return "ELIF";
            case sym.ENDIF: return "ENDIF";
            case sym.WHILE: return "WHILE";
            case sym.DO: return "DO";
            case sym.ENDWHILE: return "ENDWHILE";
            case sym.TRUE: return "TRUE";
            case sym.FALSE: return "FALSE";
            case sym.RETURN: return "RETURN";
            case sym.WRITE: return "WRITE";
            case sym.WRITERETURN: return "WRITERETURN";
            case sym.READ: return "READ";
            case sym.REAL: return "REAL";
            case sym.INTEGER: return "INTEGER";
            case sym.STRING: return "STRING";
            case sym.PLUS: return "PLUS";
            case sym.MINUS: return "MINUS";
            case sym.TIMES: return "TIMES";
            case sym.DIV: return "DIV";
            case sym.EQ: return "EQ";
            case sym.NE: return "NE";
            case sym.LT: return "LT";
            case sym.LE: return "LE";
            case sym.GT: return "GT";
            case sym.GE: return "GE";
            case sym.AND: return "AND";
            case sym.OR: return "OR";
            case sym.NOT: return "NOT";
            case sym.ASSIGN: return "ASSIGN";
            case sym.COLON: return "COLON";
            case sym.SEMI: return "SEMI";
            case sym.COMMA: return "COMMA";
            case sym.LPAR: return "LPAR";
            case sym.RPAR: return "RPAR";
            case sym.LBRACE: return "LBRACE";
            case sym.RBRACE: return "RBRACE";
            case sym.REF: return "REF";
            case sym.IDENTIFIER: return "IDENTIFIER";
            case sym.NUMBER_LITERAL: return "NUMBER_LITERAL";
            case sym.EOF: return "EOF";
            default: return "UNKNOWN";
        }
    }
}
