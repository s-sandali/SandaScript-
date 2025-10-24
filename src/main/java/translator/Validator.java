package translator;

import translator.ast.*;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern VAR = Pattern.compile("\\$[A-Za-z_][A-Za-z0-9_]*");

    public static void validate(Program p) {
        for (TestBlock t : p.tests) {
            for (Statement s : t.statements) {
                if (isRequest(s)) {
                    // base_url vs relative path
                    if (p.baseUrl == null || p.baseUrl.isEmpty()) {
                        if (!isAbsoluteUrl(s.url)) {
                            int ln = s.line > 0 ? s.line : 1;
                            int col = s.urlCol > 0 ? s.urlCol : 1;
                            throw new ParseException("Line " + ln + ":Col " + col + ": No base_url configured; path must be absolute");
                        }
                    }
                    // undefined variables in URL
                    checkVars(p, s.url, s.line, s.urlCol);
                    // headers
                    for (Map.Entry<String,String> h : s.headers.entrySet()) {
                        int ln = s.headerValueLines.getOrDefault(h.getKey(), s.line);
                        int col = s.headerValueCols.getOrDefault(h.getKey(), 1);
                        checkVars(p, h.getValue(), ln, col);
                    }
                    // body
                    if (s.body != null) {
                        checkVars(p, s.body, s.bodyLine > 0 ? s.bodyLine : s.line, s.bodyCol > 0 ? s.bodyCol : 1);
                    }
                }
            }
        }
    }

    private static boolean isRequest(Statement s) {
        return s != null && ("GET".equals(s.type) || "POST".equals(s.type) || "PUT".equals(s.type) || "DELETE".equals(s.type));
    }

    private static boolean isAbsoluteUrl(String url) {
        if (url == null) return false;
        String u = url.trim();
        return u.startsWith("http://") || u.startsWith("https://");
    }

    private static void checkVars(Program p, String text, int line, int col) {
        if (text == null) return;
        Matcher m = VAR.matcher(text);
        while (m.find()) {
            String name = m.group().substring(1);
            if (!p.variables.containsKey(name)) {
                int ln = line > 0 ? line : 1;
                int c = col > 0 ? col : 1;
                throw new ParseException("Line " + ln + ":Col " + c + ": undefined variable '" + name + "'");
            }
        }
    }
}
