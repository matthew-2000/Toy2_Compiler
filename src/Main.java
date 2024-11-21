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
        String filePath = "test/test7.txt";
        String outputCFile = "output.c";
        String outputExecutable = "output";

        try {
            // Creazione del lexer
            FileReader fileReader = new FileReader(filePath);
            Toy2Lexer lexer = new Toy2Lexer(fileReader);

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
            System.out.println("Il codice generato è stato salvato in 'output.c'");
            // Compile and Run the C code
            compileAndRunCCode(outputCFile, outputExecutable);

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

    private static void compileAndRunCCode(String sourceFile, String outputExecutable) {
        try {
            // Step 1: Compile the C code
            Process compileProcess = new ProcessBuilder("gcc", sourceFile, "-o", outputExecutable)
                    .inheritIO() // Redirects the output and error streams to the console
                    .start();

            // Wait for the compilation to complete
            int compileExitCode = compileProcess.waitFor();
            if (compileExitCode != 0) {
                System.err.println("Errore durante la compilazione del codice C.");
                return;
            }
            System.out.println("Compilazione completata con successo.");
        } catch (IOException | InterruptedException e) {
            System.err.println("Errore durante la compilazione o l'esecuzione del codice C: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
