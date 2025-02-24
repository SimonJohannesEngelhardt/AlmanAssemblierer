package com.engelhardt.simon.visitor;


import com.engelhardt.simon.ast.*;

import java.io.IOException;
import java.io.Writer;

public class PrettyPrinter implements Visitor {
    Writer out;
    String indent = "\n";

    public PrettyPrinter(Writer out) {
        this.out = out;
    }

    void write(String s) {
        try {
            out.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        write("" + intLiteral.n);
    }

    @Override
    public void visit(OpExpr opExpr) {
        if (opExpr.attribute.parenthesis) write("(");
        opExpr.left.welcome(this);
        write(" " + opExpr.operator.image + " ");
        opExpr.right.welcome(this);
        if (opExpr.attribute.parenthesis) write(")");

    }

    @Override
    public void visit(Variable variable) {
        write(variable.name);
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        write("return ");
        returnStatement.expr.welcome(this);
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        write(indent);
        write("fun ");
        write(functionDefinition.name);
        write("(");
        var first = true;
        for (var p : functionDefinition.parameters) {
            if (first) first = false;
            else write(", ");
            write(p.name);
            write(": ");
            write(p.type);
        }
        write("): ");
        write(functionDefinition.resultType);
        write(" {");
        indent += "  ";
        functionDefinition.block.welcome(this);
        indent = indent.substring(0, indent.length() - 2);
        write(indent);
        write("}");
        write("\n");
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        write(indent);
        write(variableDecl.varName);
        write(": ");
        write(variableDecl.type);
        if (variableDecl.expr != null) {
            write(" = ");
            variableDecl.expr.welcome(this);
        }
        write(";");
    }

    @Override
    public void visit(Prog prog) {
        for (var vd : prog.variableDecls) vd.welcome(this);
        write("\n");
        for (var fd : prog.functionDefinitions) fd.welcome(this);
        for (var s : prog.statements.stream().filter(stat -> !(stat instanceof VariableDecl)).toList()) {
            s.welcome(this);
        }
    }

    @Override
    public void visit(Block block) {
        if (block == null) return;
        indent += "  ";
        for (var statement : block.statements) {
            write(indent);
            statement.welcome(this);
            write(";");
        }
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    public void visit(FunctionCall functionCall) {
        write(functionCall.functionName);
        write("(");
        var first = true;
        for (var arg : functionCall.args) {
            if (first) first = false;
            else write(", ");
            arg.welcome(this);
        }
        write(")");
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        write("waehrend (");
        whileStatement.condition.welcome(this);
        write(") {");
        whileStatement.block.welcome(this);
        write(indent);
        write("}");
    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        write("wenn (");
        ifElseStatement.ifCondition.welcome(this);
        write(") {");
        indent += "  ";
        ifElseStatement.ifBlock.welcome(this);
        indent = indent.substring(0, indent.length() - 2);
        write(indent);
        write("}");
        if (ifElseStatement.elseifBlocks != null && ifElseStatement.elseifConditions != null) {
            if (ifElseStatement.elseifBlocks.size() != ifElseStatement.elseifConditions.size()) {
                throw new IllegalStateException("Nicht die gleich Anzahl an Bedingungen und Blöcken");
            }

            for (int i = 0; i < ifElseStatement.elseifBlocks.size(); i++) {
                write(" ansonsten wenn (");
                ifElseStatement.elseifConditions.get(i).welcome(this);
                write(") {");
                ifElseStatement.elseifBlocks.get(i).welcome(this);
                write(indent);
                write("}");
            }
        }
        if (ifElseStatement.elseBlock != null) {
            write(" wenn {");
            indent += "  ";
            ifElseStatement.elseBlock.welcome(this);
            indent = indent.substring(0, indent.length() - 2);
            write(indent);
            write("}");
        }
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        write("fortfahren");
    }

    @Override
    public void visit(BreakStatement breakStatement) {
        write("breche");
    }

    @Override
    public void visit(VarAssignment varAssignment) {
        write(varAssignment.varName + " = ");
        varAssignment.expr.welcome(this);
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        write(stringLiteral.s);
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        write(booleanLiteral.b ? "wahr" : "falsch");
    }

    @Override
    public void reportError(int line, int column, String message) {
        Visitor.super.reportError(line, column, message);
    }
}
