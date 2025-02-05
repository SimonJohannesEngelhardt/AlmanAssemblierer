package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class GenAssembly implements Visitor {
    Writer out;
    String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
    Map<String, Integer> env;
    int argsStackSize = 0;

    public GenAssembly(Writer out) {
        this.out = out;
    }

    void write(Object o) {
        try {
            out.write(o.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void nl() {
        write("\n\t");
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        // Zahl in %rax speichern
        nl();
        write(STR."movq\t$\{intLiteral.n}, %rax");
    }

    @Override
    public void visit(OpExpr opExpr) {
        // Code für linke Seite generieren
        opExpr.left.welcome(this);
        // Ergebnis in %rdx speichern
        nl();
        write("movq\t%rax, %rdx");
        // Code für rechte Seite generieren
        opExpr.right.welcome(this);

        // Ergebnis in %rax speichern
        switch (opExpr.operator) {
            case add -> {
                System.out.println("add");
                nl();
                write("addq\t%rdx, %rax");
            }
            case sub -> {
                nl();
                write("subq\t%rdx, %rax");
            }
            default -> throw new UnsupportedOperationException("Operator not supported (yet)");
        }
    }

    @Override
    public void visit(Variable variable) {
        // Wert einer Variablen in %rax speichern
        nl();
        write(STR."movq\t\{env.get(variable.name)}(%rbp), %rax");
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        // Code für den Rückgabewert generieren
        returnStatement.expr.welcome(this);
        // Rücksprung
        nl();
        write("movq\t%rbp, %rsp");
        nl();
        write("popq\t%rbp");
        nl();
        write("ret");

    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        nl();
        //.globl  <functionName>
        write(".globl\t_");
        write(functionDefinition.name);
        nl();
        /*
        .type <functionName>, @function
         */
        /*
        write(".type\t");
        write(functionDefinition.name);
        write(", @function");
        */

        // <functionName>:
        write(STR."\n_\{functionDefinition.name}:");

        // Basepointer auf den Stack pushen
        nl();
        write("pushq\t%rbp");
        // Stackpointer in Basepointer speichern
        nl();
        write("movq\t%rsp, %rbp");

        // Anzahl der Register-Argumente
        int registerArgs = Math.min(registers.length, functionDefinition.parameters.size());
        // Größe des Argument-Stacks
        argsStackSize = registerArgs * 8;
        // Umgebung für Variablen
        var oldEnv = env;
        env = new HashMap<>();

        // Den Stackpointer um die Größe des Argument-Stacks reduzieren
        nl();
        write(STR."subq\t$\{argsStackSize}, %rsp");

        // Argumente in Register schreiben
        int sp = -8;
        for (int i = 0; i < registerArgs; i++) {
            nl();
            write(STR."movq\t\{registers[i]}, \{sp}(%rbp)");
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp = sp - 8;
        }
        functionDefinition.block.welcome(this);

        env = oldEnv;
        write("\n");

    }

    @Override
    public void visit(VariableDecl variableDecl) {

    }

    @Override
    public void visit(Prog prog) {
        prog.functionDefinitions.forEach(fd -> fd.welcome(this));
        try {
            write("\n");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Block block) {
        block.statements.forEach(s -> s.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        for (int i = 0; i < Math.min(functionCall.args.size(), registers.length); i++) {
            functionCall.args.get(i).welcome(this);
            nl();
            write(STR."movq %rax, \{registers[i]}");
        }

        for (int i = functionCall.args.size() - 1; i >= registers.length; i--) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("pushq %rax");
        }
        nl();
        write(STR."call \{functionCall.functionName}");
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }
}
