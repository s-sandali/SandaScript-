package translator.ast;

public class Assertion extends Statement {
    // For header assertions
    public String headerKey;
    public String headerValue;
    public boolean headerContains;

    // For status range assertions
    public Integer statusLo;
    public Integer statusHi;

    // kind: "status_eq", "body_contains", "header_eq", "header_contains", "status_in"
    public Assertion(String kind, String arg) {
        // Store kind in type, and arg in extra field
        super(kind, null, null, arg);
    }

    public static Assertion headerEq(String key, String value) {
        Assertion a = new Assertion("header_eq", null);
        a.headerKey = key;
        a.headerValue = value;
        a.headerContains = false;
        return a;
    }

    public static Assertion headerContains(String key, String sub) {
        Assertion a = new Assertion("header_contains", null);
        a.headerKey = key;
        a.headerValue = sub;
        a.headerContains = true;
        return a;
    }

    public static Assertion statusIn(int lo, int hi) {
        Assertion a = new Assertion("status_in", null);
        a.statusLo = lo;
        a.statusHi = hi;
        return a;
    }
}
