package translator.ast;
import java.util.*;

public class TestBlock {
    public String name;
    public List<Statement> statements;

    public TestBlock(String name, List<Statement> statements) {
        this.name = name;
        this.statements = statements;
    }
}
