package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Parameter;
import com.engelhardt.simon.utils.Type;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class GenAssembly implements Visitor {
    private final Stack<String> loopConditionStack = new Stack<>();
    private final Stack<String> loopEndStack = new Stack<>();
    String filename;
    String programName;
    String[] libraryFunctions = {"drucke"};
    Writer out;
    String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
    Map<String, FunctionDefinition> functions;
    Map<String, VariableDecl> globalVars;
    Map<String, Integer> env;
    int sp;
    int argsStackSize = 0;
    // Counter for unique labels
    int next = 0;
    Map<String, String> stringsToWrite = new HashMap<>();
    boolean compileForMac = true;
    private int tailCallOptimizationID;
    StringBuilder output;

    public GenAssembly(String programName, String plattform) {
        this.filename = programName;
        this.programName = programName.substring(programName.lastIndexOf(File.separator) + 1);
        if (plattform.equals("linux")) {
            compileForMac = false;
        }
        this.output = new StringBuilder();
    }

    int next() {
        return next++;
    }

    void write(Object o) {
        output.append(o);
    }

    void writeFile(Object o) {
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
        write("movq\t$" + intLiteral.n + ", %rax");
    }

    @Override
    public void visit(OpExpr opExpr) {
        // Code für linke Seite generieren
        opExpr.left.welcome(this);
        // Ergebnis auf dem Stack speichern
        nl();
        write("pushq\t%rax");
        // Code für rechte Seite generieren
        opExpr.right.welcome(this);
        nl();
        // Ergbenis wieder vom Stack nehmen
        write("popq\t%rbx");

        // Ergebnis in %rax speichern
        switch (opExpr.operator) {
            case add -> {
                nl();
                write("addq\t%rbx, %rax");
            }
            case sub -> {
                nl();
                write("subq\t%rax, %rbx");
                nl();
                write("movq\t%rbx, %rax");
            }
            case mult -> {
                nl();
                write("imulq\t%rbx, %rax");
            }
            case div -> {
                nl();
                write("cqo");
                nl();
                write("idivq\t%rbx");
            }
            case mod -> {
                nl();
                write("cqo");
                nl();
                write("idivq\t%rbx");
                nl();
                write("movq\t%rdx, %rax");
            }
            case eq -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("sete\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case neq -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("setne\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case lteq -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("setle\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case lt -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("setl\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case gteq -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("setge\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case gt -> {
                nl();
                write("cmpq\t%rax, %rbx");
                nl();
                write("setg\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case or -> {
                nl();
                write("orq\t%rbx, %rax");
            }
            case and -> {
                nl();
                write("andq\t%rbx, %rax");
            }
            default -> throw new UnsupportedOperationException("Operator not supported (yet)");
        }
    }

    @Override
    public void visit(Variable variable) {
        // TODO mit containsKey vorher checken ob die Variable existiert
        // Wert einer Variablen in %rax speichern
        var local = env.get(variable.name);
        var global = globalVars.get(variable.name);
        if (local != null) {
            nl();
            write("movq\t" + local + "(%rbp), %rax");
        } else if (global != null) {
            nl();
            write("movq\t_" + global.varName + "(%rip), %rax");
        } else {
            throw new RuntimeException("Konnte die angegebene Variable nicht finden.");
        }
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
        //.globl  <functionName>
        write("\t.globl\t");
        write((compileForMac ? "_" : "") + functionDefinition.name);
        // .type <functionName>, @function
        //write(".type\t");
        //write("_" + functionDefinition.name);
        //write(", @function");
        // <functionName>:
        write("\n" + (compileForMac ? "_" : "") + functionDefinition.name + ":");
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

        if (functionDefinition.attribute.hasTailCall) {
            tailCallOptimizationID = next();
            write("\n.LStart" + tailCallOptimizationID + ":");
        }


        // Den Stackpointer um die Größe des Argument-Stacks reduzieren
        nl();
        write("subq\t$" + argsStackSize + ", %rsp");


        // Parameter, die nicht in Register passen, liegen schon auf dem Stack
        // und werden nur noch in die env aufgenommen
        sp = 16;
        for (int i = registers.length; i < functionDefinition.parameters.size(); i++) {
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp += 8;
        }

        // Argumente von den Registern auf den Stack schreiben und in env merken
        sp = -8;
        for (int i = 0; i < registerArgs; i++) {
            nl();
            write("movq\t" + registers[i] + ", " + sp + "(%rbp)");
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp -= 8;
        }

        // Block generieren
        functionDefinition.block.welcome(this);

        // Falls noch nicht returned wurde, hier returnen
        if (functionDefinition.block.statements.stream().noneMatch(s -> s instanceof ReturnStatement)) {
            nl();
            write("movq\t$0, %rax");
            nl();
            write("movq\t%rbp, %rsp");
            nl();
            write("popq\t%rbp");
            nl();
            write("ret");
        }

        // Umgebung zurücksetzen
        env = oldEnv;

        write("\n\n");
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        if (!(variableDecl.expr instanceof IntLiteral
                || variableDecl.expr instanceof OpExpr
                || variableDecl.expr instanceof VarAssignment
                || variableDecl.expr instanceof StringLiteral
        )) throw new RuntimeException("kann keine VarDecl schreiben");

        if (variableDecl.global) {
            generateGlobalVarDecl(variableDecl);
        } else {
            generateLocalVarDecl(variableDecl);
        }
    }

    private void generateGlobalVarDecl(VariableDecl variableDecl) {
        write("\t.globl _" + variableDecl.varName);
        nl();
        write(".p2align\t3, 0x0"); // Für long müsste es 2 statt 3 sein
        write("\n");
        write("_" + variableDecl.varName + ": ");
        var expr = variableDecl.expr;
        // TODO hier kann wahrscheinlich beides zusammengefasst werden, wenn man jedes mal den EvalVisitor drüberlaufen lässt.
        if (expr instanceof OpExpr opExpr) {
            EvalVisitor eval = new EvalVisitor();
            opExpr.welcome(eval);
            nl();
            write(".quad " + eval.result);
            globalVars.put(variableDecl.varName, variableDecl);
        } else if (expr instanceof IntLiteral intLiteral) {
            switch (variableDecl.theType.name()) {
                case Type.long_type -> {
                    nl();
                    write(".quad " + intLiteral.n);
                    globalVars.put(variableDecl.varName, variableDecl);
                }
                case Type.double_type ->
                        throw new UnsupportedOperationException("Type Double not supported");
                default ->
                        throw new UnsupportedOperationException("Keine weiteren Typen bisher unterstützt.");
            }
        } else if (expr instanceof StringLiteral stringLiteral) {

        } else if (expr instanceof VarAssignment varAssignment) {
            throw new UnsupportedOperationException(varAssignment.varName + "Varassignment bisher noch nicht unterstützt");
        }
        write("\n\n");
    }

    private void generateLocalVarDecl(VariableDecl variableDecl) {
        variableDecl.expr.welcome(this);
        nl();
        write("subq\t$8, %rsp");
        nl();
        write("movq\t%rax, " + sp + "(%rbp)");
        env.put(variableDecl.varName, sp);
        sp -= 8;
    }

    @Override
    public void visit(Prog prog) {
        for (FunctionDefinition fd : prog.functionDefinitions) {
            if (fd.name.equals("haupt")) {
                fd.name = "main";
            }

        }


        try {
            // Generate .h file
            out = new FileWriter(filename + ".h");
            prog.functionDefinitions.stream().filter(fd -> !fd.name.equals("main")).forEach(fd -> {
                writeFile(fd.theType.ctype() + " " + fd.name);
                writeParameters(fd.parameters);
                writeFile(";\n");
            });
            out.close();

            File cFile = new File(filename + ".c");
            if (!cFile.isFile()) {
                out = new FileWriter(cFile);
                writeFile("#include <stdlib.h>\n");
                writeFile("#include <stdio.h>\n");
                writeFile("#include <string.h>\n");
                writeFile("#include <strings.h>\n");
                writeFile("#include \"" + programName + ".h");
                out.close();
            }

            // Prepare to write to the .s file
            out = new FileWriter(filename + ".s");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Global Vars
        globalVars = new HashMap<>();
        if (compileForMac) {
            write(".section __DATA,__data\n");
        } else {
            write(".section .data\n");
        }

        prog.variableDecls.forEach(variableDecl -> {
            globalVars.put(variableDecl.varName, variableDecl);
            variableDecl.welcome(this);
        });
        write("\n");

        // Funktionen
        functions = new HashMap<>();
        if (compileForMac) {
            write(".section __TEXT,__text\n");
        } else {
            write(".section .text\n");
        }
        // Zuerst alle Funktionen registrieren
        prog.functionDefinitions.forEach(fd -> functions.put(fd.name, fd));
        // Dann erst den Körper aufrufen, sonst funktioniert ein Aufruf in vorheriger Funktion nicht
        prog.functionDefinitions.forEach(fd -> fd.welcome(this));

        prog.statements.forEach(s -> s.welcome(this));

        if (compileForMac) {
            write(".section __TEXT,__cstring\n");
        } else {
            write(".section .rodata\n");
        }
        stringsToWrite.forEach((id, value) -> {
            write(id + ":");
            nl();
            write(".asciz\t\"" + value + "\"\n");
        });

        write("\n"); // Last line
        try {
            out.write(output.toString());
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeParameters(List<Parameter> ps) {
        writeFile("(");
        var first = true;
        for (Parameter p : ps) {
            if (first) {
                first = false;
            } else {
                writeFile(", ");
            }
            writeFile(p.theType.ctype());
            writeFile(" ");
            writeFile(p.name);

        }
        writeFile(")");
    }

    @Override
    public void visit(Block block) {
        block.statements.forEach(s -> s.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        if (Arrays.stream(libraryFunctions).anyMatch(s -> s.equals(functionCall.functionName))) {
            callLibraryFunction(functionCall);
        } else {
            var functionDefinition = functions.get(functionCall.functionName);
            if (functionDefinition == null) {
                reportError(functionCall.line, functionCall.column, functionCall.functionName + "() has no function defined.");
            }
        }
        int numArgs = functionCall.args.size();
        int numRegArgs = Math.min(numArgs, registers.length);

        List<String> tempStorage = new ArrayList<>();

        // Evaluate and store arguments that will go in registers (right to left)
        for (int i = numRegArgs - 1; i >= 0; i--) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("movq\t%rax, " + registers[i]); // Move result to argument register
        }

        // Evaluate and push arguments that go on the stack (right to left)
        for (int i = numArgs - 1; i >= registers.length; i--) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("pushq\t%rax"); // Push argument onto stack
            tempStorage.add("%rax"); // Track pushed arguments
        }

        /*for (int i = 0; i < Math.min(functionCall.args.size(), registers.length); i++) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("movq\t%rax, " + registers[i]);
        }

        for (int i = functionCall.args.size() - 1; i >= registers.length; i--) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("pushq\t%rax");
        }*/


        if (functionCall.attribute.isTailCall) {
            nl();
            write("jmp .LStart" + tailCallOptimizationID);
        }
        nl();
        write("call\t" + (compileForMac ? "_" : "") + functionCall.functionName);

        // Stack cleanup if arguments were pushed
        if (!tempStorage.isEmpty()) {
            nl();
            write("addq\t$" + (tempStorage.size() * 8) + ", %rsp"); // Clean up stack
        }
    }

    private void callLibraryFunction(FunctionCall functionCall) {
        switch (functionCall.functionName) {
            case "drucke" -> {
                functionCall.functionName = "printf";
                var firstArg = functionCall.args.getFirst();
                if (firstArg instanceof StringLiteral stringLiteral) {
                    stringLiteral.s = stringLiteral.s.replace("%d", "%ld");
                } else {
                    throw new UnsupportedOperationException("Nur inline Strings können gedruckt werden");
                }
            }
            case "eingabe" -> System.out.println("Eingabe"); //TODO
            default ->
                    throw new UnsupportedOperationException("Kenne diese Bibliotheksfunktion nicht");
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var condition = ".L" + next();
        var loopEnd = ".L" + next();

        loopConditionStack.push(condition);
        loopEndStack.push(loopEnd);

        write("\n" + condition + ": ");
        whileStatement.condition.welcome(this);
        nl();
        write("cmpq\t$0, %rax");
        nl();
        write("je " + loopEnd);
        whileStatement.block.welcome(this);
        nl();
        write("jmp " + condition);
        write("\n" + loopEnd + ": ");
        nl();
        write("nop\n");


        loopConditionStack.pop();
        loopEndStack.pop();
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        var ifEnd = ".L" + next();
        var elseEnd = ".L" + next();
        var elifEnd = ".L" + next();
        ifElseStatement.ifCondition.welcome(this);
        // In rax steht jetzt das Ergebnis
        // Prüfen, ob null
        nl();
        write("cmpq\t$0, %rax");
        nl();
        write("je " + ifEnd);
        ifElseStatement.ifBlock.welcome(this);

        nl();
        if (ifElseStatement.elseifConditions.isEmpty()) {
            write("jmp " + elseEnd);
        } else {
            write("jmp " + elifEnd);
        }
        write("\n" + ifEnd + ":");

        if (ifElseStatement.elseifConditions.size() != ifElseStatement.elseifBlocks.size()) {
            throw new RuntimeException("Nicht gleich Anzahl an Conditions und Blöcken");
        }
        for (int i = 0; i < ifElseStatement.elseifConditions.size(); i++) {
            ifElseStatement.elseifConditions.get(i).welcome(this);
            nl();
            write("testq\t%rax, %rax");
            nl();
            write("je " + elifEnd);
            ifElseStatement.elseifBlocks.get(i).welcome(this);
            nl();
            write("jmp " + elseEnd);
            write("\n" + elifEnd + ":");
            elifEnd = ".L" + next();
        }
        if (ifElseStatement.elseBlock != null) {
            ifElseStatement.elseBlock.welcome(this);
        }

        write("\n" + elseEnd + ":");
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        nl();
        write("jmp " + loopConditionStack.peek());
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        nl();
        write("jmp " + loopEndStack.peek());
    }

    @Override
    public void visit(VarAssignment varAssignment) {
        varAssignment.expr.welcome(this);
        if (env.containsKey(varAssignment.varName)) {
            var locationOnStack = env.get(varAssignment.varName);
            if (locationOnStack == null) {
                throw new RuntimeException("Konnte " + varAssignment.varName + " nicht finden");
            }
            nl();
            write("movq\t%rax, " + locationOnStack + "(%rbp)");
        } else if (globalVars.containsKey(varAssignment.varName)) {
            var labelName = globalVars.get(varAssignment.varName).varName;
            nl();
            write("movq\t%rax, _" + labelName + "(%rip)");
        }
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        var stringID = "L_str." + next();
        stringsToWrite.put(stringID, stringLiteral.s);
        nl();
        write("leaq\t" + stringID + "(%rip), %rax");
    }
}
