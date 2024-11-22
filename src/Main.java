import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import nodes.ProgramNode;
import unisa.compilatori.parser;
import visitor.CodeGeneratorVisitor;
import visitor.ScopeCheckingVisitor;
import visitor.TypeCheckingVisitor;
import visitor.exception.SemanticException;
import visitor.symbolTable.SymbolTableManager;

public class Main {

    public static void main(String[] args) {
        // Controlla che ci sia un unico parametro di input
        if (args.length != 1) {
            System.err.println("Errore: il programma richiede un unico parametro di input (<nome>.txt).");
            System.exit(1);
        }

        String inputFileName = args[0];
        if (!inputFileName.endsWith(".txt")) {
            System.err.println("Errore: il file di input deve avere estensione .txt.");
            System.exit(1);
        }

        File inputFile = new File(inputFileName);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("Errore: il file di input specificato non esiste o non è un file.");
            System.exit(1);
        }

        // Estrai il nome del file senza estensione
        String baseName = inputFile.getName().substring(0, inputFile.getName().lastIndexOf('.'));
        // Cartella di output
        File outputDir = new File("test_files" + File.separator + "c_out");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Errore: impossibile creare la cartella di output.");
            System.exit(1);
        }

        // Nome del file di output .c
        File outputFile = new File(outputDir, baseName + ".c");

        try {
            // Parsing del file di input
            FileReader fileReader = new FileReader(inputFile);
            Toy2Lexer lexer = new Toy2Lexer(fileReader);
            parser parser = new parser(lexer);

            ProgramNode programNode = (ProgramNode) parser.parse().value;

            // Scope Checking
            SymbolTableManager symbolTableManager = new SymbolTableManager();
            ScopeCheckingVisitor scopeCheckingVisitor = new ScopeCheckingVisitor(symbolTableManager);
            programNode.accept(scopeCheckingVisitor);

            // Type Checking
            TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(symbolTableManager);
            programNode.accept(typeCheckingVisitor);

            // Code Generation
            CodeGeneratorVisitor codeGeneratorVisitor = new CodeGeneratorVisitor();
            programNode.accept(codeGeneratorVisitor);
            String generatedCode = codeGeneratorVisitor.getCode();

            // Salva il codice generato nel file di output
            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(generatedCode);
            }

            System.out.println("Codice generato salvato in: " + outputFile.getAbsolutePath());

        } catch (SemanticException e) {
            System.err.println("Errore semantico: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Errore durante la lettura/scrittura dei file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Errore durante l'elaborazione: " + e.getMessage());
            System.exit(1);
        }
    }
}
