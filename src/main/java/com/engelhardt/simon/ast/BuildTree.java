package com.engelhardt.simon.ast;

import com.engelhardt.simon.antlr.*;
import com.engelhardt.simon.utils.*;

import java.util.ArrayList;

public class BuildTree extends almanBaseListener {
    @Override
    public void exitZahl(almanParser.ZahlContext ctx) {
        ctx.result = new IntLiteral(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), Long.parseLong(ctx.NUMBER().getText()));
    }

    @Override
    public void exitExpr(almanParser.ExprContext ctx) {
        int line = ctx.getStart().getLine();
        int column = ctx.getStart().getCharPositionInLine();

        if (ctx.zahl() != null) {
            ctx.result = ctx.zahl().result;
        } else if (ctx.PLUS() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.add
            );
        } else if (ctx.MINUS() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.sub
            );
        } else if (ctx.MULT() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.mult
            );
        } else if (ctx.DIV() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.div
            );
        } else if (ctx.ID() != null) {
            ctx.result = new Variable(
                    line,
                    column,
                    ctx.ID().getText()
            );
        } else if (ctx.IS_EQUAL() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.eq
            );
        } else if (ctx.LPAR() != null && ctx.RPAR() != null) {
            ctx.result = ctx.expr().getFirst().result;
            ctx.result.attribute.parenthesis = true;
        } else {
            System.err.println("Unrecognized expression");
        }
    }

    public void exitBlock(almanParser.BlockContext ctx) {
        var statements = new ArrayList<AST>();
        for (var statement : ctx.statement()) {
            statements.add(statement.result);
        }
        ctx.result = new Block(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                statements
        );
    }

    public void exitStatement(almanParser.StatementContext ctx) {
        if (ctx.varDecl() != null) {
            ctx.result = ctx.varDecl().result;
        } else if (ctx.ifElseStatement() != null) {
            ctx.result = ctx.ifElseStatement().result;
        } else if (ctx.returnStatement() != null) {
            ctx.result = ctx.returnStatement().result;
        } else if (ctx.expr() != null) {
            ctx.result = ctx.expr().result;
        } else if (ctx.functionCall() != null) {
            ctx.result = ctx.functionCall().result;
        } else {
            System.err.println("Unrecognized statement");
        }
    }

    @Override
    public void exitFunctionCall(almanParser.FunctionCallContext ctx) {
        int line = ctx.getStart().getLine();
        int column = ctx.getStart().getCharPositionInLine();
        var args = new ArrayList<AST>();
        for (var expr : ctx.exprList().expr()) {
            args.add(expr.result);
        }
        ctx.result = new FunctionCall(line, column, ctx.ID().getText(), args);
    }

    @Override
    public void exitReturnStatement(almanParser.ReturnStatementContext ctx) {
        ctx.result = new ReturnStatement(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                ctx.expr().result
        );
    }

    @Override
    public void exitFunctionDefinition(almanParser.FunctionDefinitionContext ctx) {
        if (ctx.FUN() != null) {
            var params = new ArrayList<Parameter>();
            for (var formalParameter : ctx.formalParameters().formalParameter()) {
                String name = formalParameter.ID().getFirst().getText();
                String type = formalParameter.ID().getLast().getText();
                params.add(new Parameter(name, type));
            }
            ctx.result = new FunctionDefinition(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    ctx.ID().getFirst().getText(),
                    ctx.ID().getLast().getText(),
                    ctx.block().result,
                    params
            );

        }
    }

    @Override
    public void exitVarDecl(almanParser.VarDeclContext ctx) {
        String varName = ctx.ID().getFirst().getText();
        String type = ctx.ID().getLast().getText();
        if (ctx.expr() != null) {
            ctx.result = new VariableDecl(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), varName, type, ctx.expr().result);
        } else {
            ctx.result = new VariableDecl(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), varName, type);
        }
    }

    @Override
    public void exitIfElseStatement(almanParser.IfElseStatementContext ctx) {
        if (ctx.ELSE() == null) {
            ctx.result = new IfElseStatement(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    ctx.expr().result,
                    ctx.block().getFirst().result
            );
        } else {
            ctx.result = new IfElseStatement(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    ctx.expr().result,
                    ctx.block().getFirst().result,
                    ctx.block().getLast().result
            );
        }
    }

    @Override
    public void exitProgram(almanParser.ProgramContext ctx) {
        ctx.result = new Prog(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                ctx.varDecl().stream().map(definition -> definition.result).toList(),
                ctx.functionDefinition().stream().map(definition -> definition.result).toList(),
                ctx.statement().stream().map(statement -> statement.result).toList()
        );
    }
}
