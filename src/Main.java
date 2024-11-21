import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java_cup.runtime.Symbol;
import nodes.ProgramNode;
import unisa.compilatori.parser;
import unisa.compilatori.sym;
import visitor.CodeGeneratorVisitor;
import visitor.ScopeCheckingVisitor;
import visitor.TypeCheckingVisitor;
import visitor.exception.SemanticException;
import visitor.symbolTable.SymbolTableManager;

public class Main {

    public static void main(String[] args) {
        // Percorso del file di test
        String filePath = "test/test1.txt";

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
            ProgramNode programNode = (ProgramNode) parser.parse().value;
            System.out.println("=== Parsing completato con successo! ===");

            // Creazione del SymbolTableManager condiviso
            SymbolTableManager symbolTableManager = new SymbolTableManager();

            // Scope Checking
            System.out.println("\n=== Avvio dello scope checking ===");
            ScopeCheckingVisitor scopeCheckingVisitor = new ScopeCheckingVisitor(symbolTableManager);
            programNode.accept(scopeCheckingVisitor);
            System.out.println("=== Scope checking completato con successo! ===");

            // Type Checking
            System.out.println("\n=== Avvio del type checking ===");
            TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(symbolTableManager);
            programNode.accept(typeCheckingVisitor);
            System.out.println("=== Type checking completato con successo! ===");

            // Code Generation
            System.out.println("\n=== Avvio della generazione del codice ===");
            CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor();
            String generatedCode = (String) programNode.accept(codeGeneratorVisitor);
            System.out.println("=== Codice generato ===");
            System.out.println(generatedCode);

            // Salva il codice generato su un file
            saveGeneratedCodeToFile("output.c", generatedCode);
            System.out.println("\nIl codice generato è stato salvato in 'output.c'");

        } catch (FileNotFoundException e) {
            System.err.println("Errore: il file " + filePath + " non è stato trovato.");
        } catch (IOException e) {
            System.err.println("Errore durante la lettura del file: " + e.getMessage());
        } catch (SemanticException e) {
            System.err.println("Errore semantico: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Errore durante l'analisi del file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveGeneratedCodeToFile(String filePath, String code) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(code);
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio del codice generato: " + e.getMessage());
        }
    }

    private static String getTokenName(int tokenSym) {
        return switch (tokenSym) {
            case sym.VAR -> "VAR";
            case sym.PROC -> "PROC";
            case sym.ENDPROC -> "ENDPROC";
            case sym.FUNC -> "FUNC";
            case sym.ENDFUNC -> "ENDFUNC";
            case sym.IF -> "IF";
            case sym.THEN -> "THEN";
            case sym.ELSE -> "ELSE";
            case sym.ELIF -> "ELIF";
            case sym.ENDIF -> "ENDIF";
            case sym.WHILE -> "WHILE";
            case sym.DO -> "DO";
            case sym.ENDWHILE -> "ENDWHILE";
            case sym.RETURN -> "RETURN";
            case sym.NUMBER_LITERAL -> "NUMBER_LITERAL";
            case sym.REAL_CONST -> "REAL_CONST";
            case sym.STRING_LITERAL -> "STRING_LITERAL";
            case sym.TRUE -> "TRUE";
            case sym.FALSE -> "FALSE";
            case sym.IDENTIFIER -> "ID";
            case sym.ASSIGN -> "ASSIGN";
            case sym.PLUS -> "PLUS";
            case sym.MINUS -> "MINUS";
            case sym.TIMES -> "TIMES";
            case sym.DIV -> "DIV";
            case sym.EQ -> "EQ";
            case sym.NE -> "NE";
            case sym.LT -> "LT";
            case sym.LE -> "LE";
            case sym.GT -> "GT";
            case sym.GE -> "GE";
            case sym.AND -> "AND";
            case sym.OR -> "OR";
            case sym.NOT -> "NOT";
            case sym.LPAR -> "LPAR";
            case sym.RPAR -> "RPAR";
            case sym.SEMI -> "SEMI";
            case sym.COLON -> "COLON";
            case sym.COMMA -> "COMMA";
            case sym.DOLLAR -> "DOLLAR";
            case sym.WRITE -> "WRITE";
            case sym.WRITERETURN -> "WRITERETURN";
            case sym.READ -> "READ";
            case sym.REF -> "REF";
            case sym.ENDVAR -> "ENDVAR";
            case sym.REAL -> "REAL";
            case sym.INTEGER -> "INTEGER";
            case sym.STRING -> "STRING";
            case sym.BOOLEAN -> "BOOLEAN";
            case sym.UMINUS -> "UMINUS";
            case sym.TYPERETURN -> "TYPERETURN";
            case sym.OUT -> "OUT";
            case sym.EOF -> "EOF";
            default -> "UNKNOWN";
        };
    }
}
