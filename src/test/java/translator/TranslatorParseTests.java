package translator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import translator.ast.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TranslatorParseTests {

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
    void parsesConfigVariablesAndTests() {
        String src = String.join("\n",
            "config {",
            "  base_url = \"http://localhost:8080\";",
            "  header \"Content-Type\" = \"application/json\";",
            "}",
            "let user = \"admin\";",
            "let id = 42;",
            "",
            "test Login {",
            "  POST \"/api/login\" { body = \"{ \\\"username\\\": \\\"$user\\\", \\\"password\\\": \\\"1234\\\" }\"; };",
            "  expect status = 200;",
            "  expect header \"Content-Type\" contains \"json\";",
            "  expect body contains \"\\\"token\\\":\";",
            "}");
        Program p = parse(src);
        assertEquals("http://localhost:8080", p.baseUrl);
        assertEquals("application/json", p.defaultHeaders.get("Content-Type"));
        assertEquals("admin", p.variables.get("user"));
        assertEquals("42", p.variables.get("id"));
        assertEquals(1, p.tests.size());
        TestBlock t = p.tests.get(0);
        assertEquals("Login", t.name);
        // Expect at least 3 statements (POST + 3 assertions)
        assertTrue(t.statements.size() >= 4);
    }

    @Test
    void reportsInvalidIdentifierAfterLet() {
        String src = "let 2a = \"x\"; test T { GET \"/x\"; expect status = 200; expect body contains \"x\"; }";
        ParseException ex = assertThrows(ParseException.class, () -> parse(src));
        assertTrue(ex.getMessage().contains("expected IDENT after 'let'"));
    }

    @Test
    void reportsBodyMustBeString() {
        String src = "test T { POST \"/x\" { body = 123; }; expect status = 200; expect body contains \"x\"; }";
        ParseException ex = assertThrows(ParseException.class, () -> parse(src));
        assertTrue(ex.getMessage().contains("expected STRING after 'body ='"));
    }

    @Test
    void reportsStatusMustBeNumber() {
        String src = "test T { GET \"/x\"; expect status = \"200\"; expect body contains \"x\"; }";
        ParseException ex = assertThrows(ParseException.class, () -> parse(src));
        assertTrue(ex.getMessage().contains("expected NUMBER for status"));
    }

    @Test
    void reportsMissingSemicolonAfterRequest() {
        String src = "test T { GET \"/x\" expect status = 200; expect body contains \"x\"; }";
        ParseException ex = assertThrows(ParseException.class, () -> parse(src));
        assertTrue(ex.getMessage().contains("expected ';' after request"));
    }
}
