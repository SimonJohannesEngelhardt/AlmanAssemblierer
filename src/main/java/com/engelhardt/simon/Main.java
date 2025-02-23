package com.engelhardt.simon;

import com.engelhardt.simon.antlr.almanLexer;
import com.engelhardt.simon.antlr.almanParser;
import com.engelhardt.simon.ast.BuildTree;
import com.engelhardt.simon.visitor.GenAssembly;
import com.engelhardt.simon.visitor.MarkTailCall;
import com.engelhardt.simon.visitor.PrettyPrinter;
import com.engelhardt.simon.visitor.TypeCheckVisitor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.swing.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;

import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;

public class Main {
    static boolean tree = false;
    static boolean pretty = false;
    static String plattform = "mac";

    public static void compile(String className, Reader in) throws IOException {
        var lexer = new almanLexer(CharStreams.fromReader(in));
        var parser = new almanParser(new CommonTokenStream(lexer));
        var antlrTree = parser.program();

        // Nodes.AST bauen
        ParseTreeWalker.DEFAULT.walk(new BuildTree(), antlrTree);
        var ast = antlrTree.result;

        // Pretty Printer
        if (pretty) {
            var out = new StringWriter();
            ast.welcome(new PrettyPrinter(out));
            System.out.println(out);
        }

        // Typcheck starten
        ast.welcome(new TypeCheckVisitor());

        // Tail Call Optimization
        ast.functionDefinitions.forEach(fd -> fd.welcome(new MarkTailCall()));

        // JBC oder Assembly generieren
        ast.welcome(new GenAssembly(className, plattform));

        // Treeansicht generieren
        if (tree) {
            displayTree(parser, antlrTree);
        }
    }

    public static void displayTree(almanParser parser, almanParser.ProgramContext antlrTree) {
        JFrame frame = new JFrame("Antlr Nodes.AST");
        JPanel panel = new JPanel();
        TreeViewer viewer = new TreeViewer(Arrays.asList(
                parser.getRuleNames()), antlrTree);
        viewer.setScale(1.5); // Scale a little
        viewer.setAutoscrolls(true);
        panel.add(viewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("Main").build()
                .description("Kompiliere eine .almn Datei zu x86 Assembly");
        parser.addArgument("file")
                .metavar("FILE")
                .help("Die zu kompilierende Datei");
        parser.addArgument("--tree-view")
                .dest("tree")
                .action(storeTrue())
                .setDefault(false)
                .help("Zeigt eine grafische Baumansicht der Sprache an");
        parser.addArgument("--pretty-printer")
                .dest("pretty")
                .action(storeTrue())
                .setDefault(false)
                .help("Gibt den Code in einer schönen Form aus");
        parser.addArgument("--linux")
                .dest("plattform")
                .action(storeTrue())
                .setDefault("mac")
                .help("Kompiliert den Code für Linux");
        try {
            Namespace res = parser.parseArgs(args);
            tree = res.getBoolean("tree");
            pretty = res.getBoolean("pretty");
            plattform = res.getString("plattform");
            String file = res.getString("file");
            System.out.println(file);
            compile(file.substring(0, file.lastIndexOf('.')), new FileReader(file));
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
