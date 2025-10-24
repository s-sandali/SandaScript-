package translator.ast;
import java.util.*;

public class Program {
    public String baseUrl; // nullable; if null, paths must be absolute
    public Map<String, String> defaultHeaders = new LinkedHashMap<>();
    public Map<String, String> variables = new LinkedHashMap<>();
    public List<TestBlock> tests;

    public Program(List<TestBlock> tests) { this.tests = tests; }

    public Program(String baseUrl, Map<String,String> defaultHeaders, Map<String,String> variables, List<TestBlock> tests) {
        this.baseUrl = baseUrl;
        if (defaultHeaders != null) this.defaultHeaders.putAll(defaultHeaders);
        if (variables != null) this.variables.putAll(variables);
        this.tests = tests != null ? tests : new ArrayList<>();
    }
}
