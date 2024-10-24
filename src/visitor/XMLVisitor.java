package visitor;

import nodes.ASTNode;

public class XMLVisitor implements ASTVisitor {
    private StringBuilder xmlOutput;

    public XMLVisitor() {
        xmlOutput = new StringBuilder();
    }

    public String getXML() {
        return xmlOutput.toString();
    }

    @Override
    public void visit(ASTNode node) {
        xmlOutput.append("<").append(node.getType()).append(" value=\"").append(node.getValue()).append("\">\n");

        for (ASTNode child : node.getChildren()) {
            visit(child);
        }

        xmlOutput.append("</").append(node.getType()).append(">\n");
    }
}
