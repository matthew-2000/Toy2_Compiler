import nodes.ASTNode;
import unisa.compilatori.parser;
import visitor.XMLVisitor;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class VisitorMain {
    public static void main(String[] args) throws FileNotFoundException {
        // Esegui il parsing del programma Toy2
        parser parser = new parser(new Toy2Lexer(new FileReader("test/test4.txt")));

        try {
            ASTNode root = (ASTNode) parser.debug_parse().value; // Root dell'albero sintattico
            XMLVisitor visitor = new XMLVisitor();
            visitor.visit(root); // Visita l'albero e genera XML

            // Stampa o salva l'output XML
            String xmlOutput = visitor.getXML();
            System.out.println(xmlOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
