package translator;

import java.io.*;
import java.nio.file.*;
import java_cup.runtime.SymbolFactory;
import java_cup.runtime.DefaultSymbolFactory;
import translator.ast.*;

public class Translator {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: mvn -q exec:java -Dexec.mainClass=translator.Translator -Dexec.args=\"<file.test>\"");
            return;
        }

        String srcPath = args[0];
        try (Reader reader = new FileReader(srcPath)) {
            Lexer lexer = new Lexer(reader);
            SymbolFactory sf = new DefaultSymbolFactory();
            Parser parser = new Parser(lexer, sf);
            Program program = (Program) parser.parse().value;
            Validator.validate(program);
            CodeGen gen = new CodeGen(program, srcPath);
            // write to src/test/java/GeneratedTests.java by default
            Path outDir = Paths.get("src", "test", "java");
            Files.createDirectories(outDir);
            String outputPath = outDir.resolve("GeneratedTests.java").toString();
            gen.generate(outputPath);
            System.out.println("✅ Generated " + outputPath + " from " + srcPath + " successfully!");
        } catch (ParseException pe) {
            System.err.println(pe.getMessage());
            System.exit(2);
        }
    }
}
