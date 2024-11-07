package visitor;

import visitor.exception.SemanticException;

public interface Visitable {
    <T> T accept(Visitor<T> visitor) throws SemanticException;
}
