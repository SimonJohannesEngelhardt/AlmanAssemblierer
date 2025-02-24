package com.engelhardt.simon.ast;

import com.engelhardt.simon.antlr.almanBaseListener;
import com.engelhardt.simon.antlr.almanParser;
import com.engelhardt.simon.utils.Parameter;

import java.util.ArrayList;
import java.util.List;

public class BuildTree extends almanBaseListener {
    @Override
    public void exitZahl(almanParser.ZahlContext ctx) {
        ctx.result = new IntLiteral(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), Long.parseLong(ctx.NUMBER().getText()));
    }

    @Override
    public void exitString(almanParser.StringContext ctx) {
        ctx.result = new StringLiteral(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1));
    }

    @Override
    public void exitWahrheitswert(almanParser.WahrheitswertContext ctx) {
        ctx.result = new BooleanLiteral(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), ctx.WAHRHEITSWERT().getText().equals("wahr"));
    }

    @Override
    public void exitExpr(almanParser.ExprContext ctx) {
        int line = ctx.getStart().getLine();
        int column = ctx.getStart().getCharPositionInLine();

        if (ctx.zahl() != null) {
            ctx.result = ctx.zahl().result;
        } else if (ctx.string() != null) {
            ctx.result = ctx.string().result;
        } else if (ctx.wahrheitswert() != null) {
            ctx.result = ctx.wahrheitswert().result;
        } else if (ctx.AND() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.and
            );
        } else if (ctx.OR() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.or
            );
        } else if (ctx.XOR() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.xor
            );
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
        } else if (ctx.MOD() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.mod
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
        } else if (ctx.NOT_EQUAL() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.neq
            );
        } else if (ctx.LESS_THAN() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.lt
            );
        } else if (ctx.LESS_THAN_EQUAL() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.lteq
            );
        } else if (ctx.GREATER_THAN() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.gt
            );
        } else if (ctx.GREATER_THAN_EQUAL() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.gteq
            );
        } else if (ctx.LOR() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.lor
            );
        } else if (ctx.LAND() != null) {
            ctx.result = new OpExpr(
                    line,
                    column,
                    ctx.expr().get(0).result,
                    ctx.expr().get(1).result,
                    Operator.land
            );
        } else if (ctx.LPAR() != null && ctx.RPAR() != null) {
            ctx.result = ctx.expr().getFirst().result;
            ctx.result.attribute.parenthesis = true;
        } else if (ctx.functionCall() != null) {
            ctx.result = ctx.functionCall().result;
        } else {
            throw new RuntimeException("Unbekannte Expression");
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
        } else if (ctx.whileStatement() != null) {
            ctx.result = ctx.whileStatement().result;
        } else if (ctx.BREAK() != null) {
            ctx.result = new BreakStatement(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        } else if (ctx.CONTINUE() != null) {
            ctx.result = new ContinueStatement(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        } else if (ctx.varAssignment() != null) {
            ctx.result = ctx.varAssignment().result;
        } else {
            System.err.println("Unbekannter Statement-Typ");
        }
    }

    @Override
    public void exitFunctionCall(almanParser.FunctionCallContext ctx) {
        int line = ctx.getStart().getLine();
        int column = ctx.getStart().getCharPositionInLine();
        var args = new ArrayList<AST>();
        if (ctx.exprList() != null) {
            for (var expr : ctx.exprList().expr()) {
                args.add(expr.result);
            }
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
        if (ctx.FUNCTION_HEAD() != null) {
            var params = new ArrayList<Parameter>();
            if (ctx.formalParameters() != null) {
                for (var formalParameter : ctx.formalParameters().formalParameter()) {
                    String name = formalParameter.ID().getFirst().getText();
                    String type = formalParameter.ID().getLast().getText();
                    params.add(new Parameter(name, type));
                }
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
        var isGlobal = ctx.parent.parent.getRuleIndex() == almanParser.RULE_program;
        if (ctx.CONST() != null) {
            if (ctx.expr() == null) {
                throw new RuntimeException("Konstante Variablendeklarationen müssen initialisiert werden");
            }
            ctx.result = new VariableDecl(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    varName,
                    type,
                    ctx.expr().result,
                    false,
                    isGlobal

            );
        } else if (ctx.LET() != null) {
            ctx.result = new VariableDecl(
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine(),
                    varName,
                    type,
                    ctx.expr() == null ? null : ctx.expr().result,
                    true,
                    isGlobal);
        }
    }

    @Override
    public void exitVarAssignment(almanParser.VarAssignmentContext ctx) {
        ctx.result = new VarAssignment(ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(), ctx.ID().getText(), ctx.expr().result);
    }

    @Override
    public void exitIfElseStatement(almanParser.IfElseStatementContext ctx) {
        if (ctx.IF() == null) {
            throw new RuntimeException("If-Statement muss mit 'if' beginnen");
        }
        // Creates a copy of the list of expressions in a modifiable list
        List<almanParser.ExprContext> expressions = new ArrayList<>(ctx.expr());
        List<almanParser.BlockContext> blocks = new ArrayList<>(ctx.block());

        AST ifCondition = expressions.getFirst().result;
        expressions.removeFirst();
        Block ifBlock = blocks.getFirst().result;
        blocks.removeFirst();

        List<AST> elseIfConditions = null;
        List<Block> elseIfBlocks = null;
        Block elseBlock = null;

        if (ctx.ELSE() != null) {
            elseBlock = blocks.getLast().result;
            blocks.removeLast();
        }


        if (ctx.ELSE_IF() != null) {
            elseIfConditions = expressions.stream().map(expr -> expr.result).toList();
            elseIfBlocks = blocks.stream().map(block -> block.result).toList();
        }

        ctx.result = new IfElseStatement(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                ifCondition,
                ifBlock,
                elseIfConditions,
                elseIfBlocks,
                elseBlock
        );
    }

    @Override
    public void exitWhileStatement(almanParser.WhileStatementContext ctx) {
        ctx.result = new WhileStatement(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                ctx.expr().result,
                ctx.block().result
        );
    }

    @Override
    public void exitProgram(almanParser.ProgramContext ctx) {
        ctx.result = new Prog(
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine(),
                ctx.statement().stream().filter(statement -> statement.varDecl() != null).map(statementContext -> statementContext.varDecl().result).toList(),
                ctx.functionDefinition().stream().map(definition -> definition.result).toList(),
                ctx.statement().stream().filter(statement -> statement.varDecl() == null).map(statement -> statement.result).toList()
        );
    }
}
