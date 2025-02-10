package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Parameter;
import com.engelhardt.simon.utils.Type;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class GenAssembly implements Visitor {
    String filename;
    String programName;
    private final Stack<String> loopConditionStack = new Stack<>();
    private final Stack<String> loopEndStack = new Stack<>();
    private int tailCallOptimizationID;
    Writer out;
    String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
    Map<String, FunctionDefinition> functions;
    Map<String, VariableDecl> globalVars;
    Map<String, Integer> env;
    int sp;
    int argsStackSize = 0;
    // Counter for unique labels
    int next = 0;

    public GenAssembly(String programName) {
        this.filename = programName;
        this.programName = programName.substring(programName.lastIndexOf(File.separator) + 1);
    }

    int next() {
        return next++;
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
            write(STR."movq\t\{local}(%rbp), %rax");
        } else if (global != null) {
            nl();
            write(STR."movq\t_\{global.varName}(%rip), %rax");
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
        write(STR."_\{functionDefinition.name}");
        // .type <functionName>, @function
        //write(".type\t");
        //write("_" + functionDefinition.name);
        //write(", @function");
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

        if (functionDefinition.attribute.hasTailCall) {
            tailCallOptimizationID = next();
            write(STR."\n.LStart\{tailCallOptimizationID}:");
        }


        // Den Stackpointer um die Größe des Argument-Stacks reduzieren
        nl();
        write(STR."subq\t$\{argsStackSize}, %rsp");


        // übrige Parameter auch in die env schreiben
        sp = 16;
        for (int i = registers.length; i < functionDefinition.parameters.size(); i++) {
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp += 8;
        }

        // Argumente von den Registern auf den Stack schreiben und in env merken
        sp = -8;
        for (int i = 0; i < registerArgs; i++) {
            nl();
            write(STR."movq\t\{registers[i]}, \{sp}(%rbp)");
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp = sp - 8;
        }

        // Block generieren
        functionDefinition.block.welcome(this);
        // Falls noch nicht returned wurde, hier returnen
        nl();
        write("movq\t%rbp, %rsp");
        nl();
        write("popq\t%rbp");
        nl();
        write("ret");

        // Umgebung zurücksetzen
        env = oldEnv;

        write("\n");
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        if (!(
                variableDecl.expr instanceof IntLiteral || variableDecl.expr instanceof OpExpr || variableDecl.expr instanceof VarAssignment
        )) throw new RuntimeException("kann keine VarDecl schreiben");

        if (variableDecl.global) {
            generateGlobalVarDecl(variableDecl);
        } else {
            generateLocalVarDecl(variableDecl);
        }
    }

    private void generateGlobalVarDecl(VariableDecl variableDecl) {
        write(STR."\t.globl _\{variableDecl.varName}");
        nl();
        write(".p2align\t3, 0x0"); // Für long müsste es 2 statt 3 sein
        write("\n");
        write(STR."_\{variableDecl.varName}: ");
        var expr = variableDecl.expr;
        // TODO hier kann wahrscheinlich beides zusammengefasst werden, wenn man jedes mal den EvalVisitor drüberlaufen lässt.
        if (expr instanceof OpExpr opExpr) {
            EvalVisitor eval = new EvalVisitor();
            opExpr.welcome(eval);
            nl();
            write(STR.".quad \{eval.result}");
            globalVars.put(variableDecl.varName, variableDecl);
        } else if (expr instanceof IntLiteral intLiteral) {
            switch (variableDecl.theType.name()) {
                case Type.long_type -> {
                    nl();
                    long value = intLiteral.n;
                    write(STR.".quad \{value}");
                    globalVars.put(variableDecl.varName, variableDecl);
                }
                case Type.double_type -> throw new UnsupportedOperationException("Type Double");
                default ->
                        throw new UnsupportedOperationException("Keine weiteren Typen bisher unterstützt.");
            }
        } else if (expr instanceof VarAssignment varAssignment) {
            throw new UnsupportedOperationException(STR."\{varAssignment.varName}Varassignment bisher noch nicht unterstützt");
        }
        write("\n\n");
    }

    private void generateLocalVarDecl(VariableDecl variableDecl) {
        variableDecl.expr.welcome(this);
        nl();
        write("subq\t$8, %rsp");
        nl();
        write(STR."movq\t%rax, \{sp}(%rbp)");
        env.put(variableDecl.varName, sp);
        sp -= 8;
    }

    @Override
    public void visit(Prog prog) {
        try {
            // Generate .h file
            out = new FileWriter(STR."\{filename}.h");
            prog.functionDefinitions.forEach(fd -> {
                write(STR."\{fd.theType.ctype()} \{fd.name}");
                writeParameters(fd.parameters);
                write(";\n");
            });
            out.close();

            File cFile = new File(STR."\{filename}.c");
            if (!cFile.isFile()) {
                out = new FileWriter(cFile);
                write("#include <stdlib.h>\n");
                write("#include <stdio.h>\n");
                write("#include <string.h>\n");
                write("#include <strings.h>\n");
                write(STR."#include \"\{programName}.h");
                out.close();
            }

            // Prepare to write to the .s file
            out = new FileWriter(STR."\{filename}.s");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Global Vars
        globalVars = new HashMap<>();
        prog.variableDecls.forEach(variableDecl -> {
            globalVars.put(variableDecl.varName, variableDecl);
            variableDecl.welcome(this);
        });
        write("\n");

        functions = new HashMap<>();
        // Zuerst alle Funktionen registrieren
        prog.functionDefinitions.forEach(fd -> functions.put(fd.name, fd));
        // Dann erst den Körper aufrufen, sonst funktioniert ein Aufruf in vorheriger Funktion nicht
        prog.functionDefinitions.forEach(fd -> fd.welcome(this));


        try {
            write("\n");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeParameters(List<Parameter> ps) {
        write("(");
        var first = true;
        for (Parameter p : ps) {
            if (first) {
                first = false;
            } else {
                write(", ");
            }
            write(p.theType.ctype());
            write(" ");
            write(p.name);

        }
        write(")");
    }

    @Override
    public void visit(Block block) {
        block.statements.forEach(s -> s.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        var functionDefinition = functions.get(functionCall.functionName);
        if (functionDefinition == null) {
            reportError(functionCall.line, functionCall.column, STR."\{functionCall.functionName}() has no function defined.");
        }

        for (int i = 0; i < Math.min(functionCall.args.size(), registers.length); i++) {
            functionCall.args.get(i).welcome(this);
            nl();
            write(STR."movq\t%rax, \{registers[i]}");
        }

        for (int i = functionCall.args.size() - 1; i >= registers.length; i--) {
            functionCall.args.get(i).welcome(this);
            nl();
            write("pushq\t%rax");
        }
        if (functionCall.attribute.isTailCall) {
            nl();
            write(STR."jmp .LStart\{tailCallOptimizationID}");
        }
        nl();
        write(STR."call\t_\{functionCall.functionName}");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        var condition = STR.".L\{next()}";
        var loopEnd = STR.".L\{next()}";

        loopConditionStack.push(condition);
        loopEndStack.push(loopEnd);

        write(STR."\n\{condition}: ");
        whileStatement.condition.welcome(this);
        nl();
        write("cmpq\t$0, %rax");
        nl();
        write(STR."je \{loopEnd}");
        whileStatement.block.welcome(this);
        nl();
        write(STR."jmp \{condition}");
        write(STR."\n\{loopEnd}: ");
        nl();
        write("nop");


        loopConditionStack.pop();
        loopEndStack.pop();
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        var ifEnd = STR.".L\{next()}";
        var elseEnd = STR.".L\{next()}";
        var elifEnd = STR.".L\{next()}";
        ifElseStatement.ifCondition.welcome(this);
        // In rax steht jetzt das Ergebnis
        // Prüfen, ob null
        nl();
        write("cmpq\t$0, %rax");
        nl();
        write(STR."je \{ifEnd}");
        ifElseStatement.ifBlock.welcome(this);

        nl();
        if (ifElseStatement.elseifConditions.isEmpty()) {
            write(STR."jmp \{elseEnd}");
        } else {
            write(STR."jmp \{elifEnd}");
        }
        write(STR."\n\{ifEnd}:");

        if (ifElseStatement.elseifConditions.size() != ifElseStatement.elseifBlocks.size()) {
            throw new RuntimeException("Nicht gleich Anzahl an Conditions und Blöcken");
        }
        for (int i = 0; i < ifElseStatement.elseifConditions.size(); i++) {
            ifElseStatement.elseifConditions.get(i).welcome(this);
            nl();
            write("testq\t%rax, %rax");
            nl();
            write(STR."je \{elifEnd}");
            ifElseStatement.elseifBlocks.get(i).welcome(this);
            nl();
            write(STR."jmp \{elseEnd}");
            write(STR."\n\{elifEnd}:");
            elifEnd = STR.".L\{next()}";
        }
        if (ifElseStatement.elseBlock != null) {
            ifElseStatement.elseBlock.welcome(this);
        }

        write(STR."\n\{elseEnd}:");
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        nl();
        write(STR."jmp \{loopConditionStack.peek()}");
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        nl();
        write(STR."jmp \{loopEndStack.peek()}");
    }

    @Override
    public void visit(VarAssignment varAssignment) {
        var locationOnStack = env.get(varAssignment.varName);
        if (locationOnStack == null) {
            throw new RuntimeException(STR."Konnte \{varAssignment.varName} nicht finden");
        }
        varAssignment.expr.welcome(this);
        nl();
        write(STR."movq\t%rax, \{locationOnStack}(%rbp)");
    }
}
