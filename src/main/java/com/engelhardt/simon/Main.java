package com.engelhardt.simon;

import com.engelhardt.simon.antlr.almanLexer;
import com.engelhardt.simon.antlr.almanParser;
import com.engelhardt.simon.ast.BuildTree;
import com.engelhardt.simon.visitor.GenAssembly;
import com.engelhardt.simon.visitor.GenJBC;
import com.engelhardt.simon.visitor.PrettyPrinter;
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

public class Main {
    static boolean tree = false;
    static boolean pretty = true;
    static String outputType = "asm";

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
        //ast.welcome(new TypeCheckVisitor());

        // JBC oder Assembly generieren
        if (outputType.equals("jbc")) {
            ast.welcome(new GenJBC(className));
        } else if (outputType.equals("asm")) {
            ast.welcome(new GenAssembly(className));
        }

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
        panel.add(viewer);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("""
                            Usage: java -jar L1.jar <file>
                            Options:
                            \t--enable-tree-view : Show the tree view of the parsed file
                            \t--output-type <type> : Specify the output type of the compiler#
                            \t--no-pretty-printer
                    """);
            System.exit(1);
        }
        for (var arg : args) {
            if (arg.equals("--enable-tree-view")) {
                tree = true;
                args = Arrays.stream(args).filter(s -> !s.equals("--enable-tree-view")).toArray(String[]::new);
            }
            if (arg.equals("--output-type")) {
                outputType = args[Arrays.asList(args).indexOf("--output-type") + 1];
                args = Arrays.stream(args).filter(s -> !s.equals("--output-type")).toArray(String[]::new);
            }
            if (arg.equals("--no-pretty-printer")) {
                pretty = false;
            }
        }
        for (var arg : args) {
            compile(arg.substring(0, arg.lastIndexOf('.')), new FileReader(arg));
            break; // TODO später hier mehrere Dateien angeben
        }
    }
}
