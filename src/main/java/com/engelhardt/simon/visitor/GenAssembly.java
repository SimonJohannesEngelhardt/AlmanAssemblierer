package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class GenAssembly implements Visitor {
    Writer out;
    String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
    Map<String, FunctionDefinition> functions;
    Map<String, VariableDecl> globalVars;
    Map<String, Integer> env;
    int argsStackSize = 0;
    // Counter for unique labels
    int next = 0;
    private final Stack<String> loopConditionStack = new Stack<>();
    private final Stack<String> loopEndStack = new Stack<>();

    public GenAssembly(Writer out) {
        this.out = out;
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
        // Ergebnis in %rbx speichern
        nl();
        write("movq\t%rax, %rbx");
        // Code für rechte Seite generieren
        opExpr.right.welcome(this);

        // Ergebnis in %rax speichern
        switch (opExpr.operator) {
            case add -> {
                nl();
                write("addq\t%rbx, %rax");
            }
            case sub -> {
                nl();
                write("subq\t%rbx, %rax");
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
                write("cmpq\t%rbx, %rax");
                nl();
                write("sete\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case neq -> {
                nl();
                write("cmpq\t%rbx, %rax");
                nl();
                write("setne\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case lteq -> {
                nl();
                write("cmpq\t%rbx, %rax");
                nl();
                write("setle\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case lt -> {
                nl();
                write("cmpq\t%rbx, %rax");
                nl();
                write("setl\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case gteq -> {
                nl();
                write("cmpq\t%rbx, %rax");
                nl();
                write("setge\t%al");
                nl();
                write("movzbq\t%al, %rax");
            }
            case gt -> {
                nl();
                write("cmpq\t%rbx, %rax");
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
        //.globl  <functionName>
        write(".globl\t");
        write("_" + functionDefinition.name);
        // .type <functionName>, @function
        //write(".type\t");
        //write("_" + functionDefinition.name);
        //write(", @function");
        // <functionName>:
        write(STR."\n_\{functionDefinition.name}:");
        nl();
        write("endbr64");
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

        /*TODO Tailcall
        if (funDef.attribute.hasTailCall){
            write("\n.LStart:");
        }
        */

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

        // TODO lokale Variblen initialieren

        // Funktionsparameter in die env schreiben
        sp = 16;
        for (int i = registers.length; i < functionDefinition.parameters.size(); i++) {
            env.put(functionDefinition.parameters.get(i).name, sp);
            sp += 8;
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
        if (!(variableDecl.statement instanceof IntLiteral))
            throw new RuntimeException("kann keine VarDecl schreiben");
        write(STR.".globl _\{variableDecl.varName}");
        write("\n");
        write(STR."_\{variableDecl.varName}: ");
        switch (variableDecl.type) {
            case "long" -> {

                nl();
                write(STR.".long \{((IntLiteral) variableDecl.statement).n}");

            }
            default -> {
                throw new UnsupportedOperationException("Keine weiteren Typen bisher unterstützt.");
            }
        }
        write("\n");

    }

    @Override
    public void visit(Prog prog) {
        globalVars = new HashMap<>();
        prog.variableDecls.forEach(variableDecl -> {
            globalVars.put(variableDecl.varName, variableDecl);
            variableDecl.welcome(this);
        });
        write("\n");
        functions = new HashMap<>();
        prog.functionDefinitions.forEach(fd -> {
            functions.put(fd.name, fd);
            fd.welcome(this);
        });

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
        if (functions.get(functionCall.functionName) == null) {
            reportError(functionCall.line, functionCall.column, "This function call has no function defined.");
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
}
