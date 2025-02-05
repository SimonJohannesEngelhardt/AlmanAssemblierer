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
        write(STR."\{intLiteral.n}");
    }

    @Override
    public void visit(OpExpr opExpr) {
        if (opExpr.attribute.parenthesis) write("(");
        opExpr.left.welcome(this);
        write(STR." \{opExpr.operator.image} ");
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
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        write(indent);
        write(variableDecl.varName);
        write(": ");
        write(variableDecl.type);
        if (variableDecl.statement != null) {
            write(" = ");
            variableDecl.statement.welcome(this);
        }
    }

    @Override
    public void visit(Prog prog) {
        for (var vd : prog.variableDecls) vd.welcome(this);
        for (var fd : prog.functionDefinitions) fd.welcome(this);
        for (var s : prog.statements) s.welcome(this);
    }

    @Override
    public void visit(Block block) {
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
                throw new IllegalStateException("Mismatch between number of conditions and blocks");
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
}
