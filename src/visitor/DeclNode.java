package visitor;

import javax.swing.tree.DefaultMutableTreeNode;

public class DeclNode extends DefaultMutableTreeNode implements Visitable {

    @Override
    public <T> T accept(Visitor<T> visitor) throws Exception {
        return null;
    }
}
