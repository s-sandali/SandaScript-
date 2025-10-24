package translator;

import org.junit.jupiter.api.Test;
import translator.ast.Program;

import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class SemanticTests {

    private Program parse(String src) {
        try {
            Reader r = new StringReader(src);
            Lexer lexer = new Lexer(r);
            Parser parser = new Parser(lexer, new java_cup.runtime.DefaultSymbolFactory());
            return (Program) parser.parse().value;
        } catch (Exception e) {
            if (e instanceof ParseException) throw (ParseException)e;
            throw new RuntimeException(e);
        }
    }

    @Test
    void undefinedVariableInPath() {
        String src = String.join("\n",
                "config { base_url = \"http://localhost:8080\"; };",
                "test T {",
                "  GET \"/api/users/$id\";",
                "  expect status = 200;",
                "  expect body contains \"x\";",
                "}");
        Program p = parse(src);
        ParseException ex = assertThrows(ParseException.class, () -> Validator.validate(p));
        assertTrue(ex.getMessage().contains("undefined variable 'id'"));
    }

    @Test
    void relativePathWithoutBaseUrl() {
        String src = String.join("\n",
                "test T {",
                "  GET \"/x\";",
                "  expect status = 200;",
                "  expect body contains \"x\";",
                "}");
        Program p = parse(src);
        ParseException ex = assertThrows(ParseException.class, () -> Validator.validate(p));
        assertTrue(ex.getMessage().contains("No base_url configured; path must be absolute"));
    }

    @Test
    void duplicateTestNames() {
        String src = String.join("\n",
                "test T { GET \"http://example.com\"; expect status = 200; expect body contains \"a\"; }",
                "test T { GET \"http://example.com\"; expect status = 200; expect body contains \"b\"; }");
        ParseException ex = assertThrows(ParseException.class, () -> {
            parse(src);
        });
        assertTrue(ex.getMessage().contains("Duplicate test 'T'"));
    }

    @Test
    void testMustHaveMinRequestsAndAssertions() {
        String src = String.join("\n",
                "test OnlyOneAssertion {",
                "  GET \"http://example.com\";",
                "  expect status = 200;",
                "}");
        ParseException ex = assertThrows(ParseException.class, () -> {
            parse(src);
        });
        assertTrue(ex.getMessage().contains("must contain at least 1 request and at least 2 assertions"));
    }
}
