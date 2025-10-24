package translator.ast;

import java.util.*;

public class Statement {
    public String type; // Request methods: "GET","POST","PUT","DELETE"; or assertion kinds
    public String url;
    public String body;
    public String extra;
    public Map<String,String> headers = new LinkedHashMap<>(); // per-request headers
    public int line = -1; // source line for the URL token (if available)
    public int urlCol = -1; // column for URL
    public int bodyLine = -1; // source line for the body string
    public int bodyCol = -1; // column for body
    public Map<String,Integer> headerValueLines = new LinkedHashMap<>(); // per-request header value lines
    public Map<String,Integer> headerValueCols = new LinkedHashMap<>(); // per-request header value columns

    public Statement(String type, String url, String body, String extra) {
        this.type = type;
        this.url = url;
        this.body = body;
        this.extra = extra;
    }

    public Statement(String type, String url, String body, Map<String,String> headers) {
        this.type = type;
        this.url = url;
        this.body = body;
        if (headers != null) this.headers.putAll(headers);
    }
}
