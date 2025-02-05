package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Type;


import java.util.HashMap;
import java.util.Map;

public class TypeCheckVisitor implements Visitor {
    Map<String, Type> env;
    Map<String, FunctionDefinition> functions = new HashMap<>();
    FunctionDefinition currentFunction = null;

    @Override
    public void visit(Prog prog) {
        prog.functionDefinitions.forEach(fd -> functions.put(fd.name, fd));
        prog.functionDefinitions.forEach(fd -> fd.welcome(this));
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        functionDefinition.type = Type.of(functionDefinition.resultType, "");
        currentFunction = functionDefinition;
        env = new HashMap<>();
        functionDefinition.parameters.forEach(param -> {
            Type type = Type.of(param.type, "");
            env.put(param.name, type);
        });
        functionDefinition.block.welcome(this);
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        intLiteral.type = Type.LONG_TYPE;
    }

    @Override
    public void visit(OpExpr opExpr) {
        opExpr.left.welcome(this);
        opExpr.right.welcome(this);
        if (opExpr.operator.isLogical()) {
            opExpr.type = Type.BOOLEAN_TYPE;
            if (!opExpr.left.type.equals(Type.BOOLEAN_TYPE) || !opExpr.right.type.equals(Type.BOOLEAN_TYPE)) {
                reportError(opExpr.line, opExpr.column, STR."Wrong type in boolean operand. \{opExpr.left.type.name()} != \{opExpr.right.type.name()}");
            }
        } else if (opExpr.operator.isArithmetic()) {
            opExpr.type = Type.LONG_TYPE;
            if (!opExpr.left.type.equals(Type.LONG_TYPE) || !opExpr.right.type.equals(Type.LONG_TYPE)) {
                reportError(opExpr.line, opExpr.column, STR."Wrong type in arithmetic operand. \{opExpr.left.type.name()} != \{opExpr.right.type.name()}");
            }
        } else if (opExpr.operator.isComparison()) {
            opExpr.type = Type.BOOLEAN_TYPE;
            if (!opExpr.left.type.equals(Type.LONG_TYPE) || !opExpr.right.type.equals(Type.LONG_TYPE)) {
                reportError(opExpr.line, opExpr.column, STR."Wrong type in comparison operand. \{opExpr.left.type.name()} != \{opExpr.right.type.name()}");
            }
        }
    }

    @Override
    public void visit(Variable var) {
        Type type = env.get(var.name);
        if (type == null) {
            reportError(var.line, var.column, STR."Unknown variable \{var.name}");
        } else {
            var.type = type;
        }
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        returnStatement.expr.welcome(this);
        if (!returnStatement.expr.type.equals(currentFunction.type)) {
            reportError(
                    returnStatement.line,
                    returnStatement.column,
                    STR."""
                    Return statement mismatch:
                        Function Type: \{currentFunction.type.name()}
                        Return Type: \{returnStatement.expr.type.name()}
                    """
            );
        }
        returnStatement.type = currentFunction.type;
    }


    @Override
    public void visit(VariableDecl variableDecl) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void visit(Block block) {
        block.statements.forEach(stmt -> stmt.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        functionCall.args.forEach(arg -> arg.welcome(this));
        FunctionDefinition function = functions.get(functionCall.functionName);
        if (function == null) {
            reportError(functionCall.line, functionCall.column, STR."Unknown function \{functionCall.functionName}");
        } else if (functionCall.args.size() != function.parameters.size()) {
            reportError(functionCall.line, functionCall.column, "Function call mismatch. Wrong number of arguments.");
            functionCall.type = Type.of(function.resultType, "");
        } else {
            var argsIterator = functionCall.args.iterator();
            for (var param : function.parameters) {
                var arg = argsIterator.next();
                if (!arg.type.equals(Type.of(param.type, ""))) {
                    reportError(arg.line, arg.column, "Function call mismatch. Wrong type of argument.");
                }
            }
        }
    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {

    }

    @Override
    public void visit(ContinueStatement continueStatement) {

    }

    @Override
    public void visit(BreakStatement breakStatement) {

    }

    @Override
    public void visit(VarAssignment varAssignment) {

    }

    @Override
    public void reportError(int line, int column, String message) {
        Visitor.super.reportError(line, column, message);
    }

}
