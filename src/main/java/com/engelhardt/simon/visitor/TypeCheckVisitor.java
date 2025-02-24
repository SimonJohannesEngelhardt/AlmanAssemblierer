package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Type;

import java.util.HashMap;
import java.util.Map;

public class TypeCheckVisitor implements Visitor {
    Map<String, Type> env;
    Map<String, Type> globalVars;
    Map<String, FunctionDefinition> functions;
    FunctionDefinition currentFunction = null;
    String[] libraryFunctions = {"drucke"};

    @Override
    public void visit(Prog prog) {
        functions = new HashMap<>();
        globalVars = new HashMap<>();
        // Global Vars
        prog.variableDecls.forEach(variableDecl -> {
            globalVars.put(variableDecl.varName, Type.of(variableDecl.type));
            variableDecl.welcome(this);
        });
        // Functions
        prog.functionDefinitions.forEach(fd -> functions.put(fd.name, fd));
        prog.functionDefinitions.forEach(fd -> fd.welcome(this));
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        functionDefinition.theType = Type.of(functionDefinition.resultType);
        currentFunction = functionDefinition;
        env = new HashMap<>();
        functionDefinition.parameters = functionDefinition.parameters.stream().peek(param -> {
            Type type = Type.of(param.type);
            env.put(param.name, type);
            param.theType = type;
        }).toList();

        functionDefinition.block.welcome(this);
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        intLiteral.theType = Type.LONG_TYPE;
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        stringLiteral.theType = Type.STRING_TYPE;
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        booleanLiteral.theType = Type.BOOLEAN_TYPE;
    }

    @Override
    public void visit(OpExpr opExpr) {
        opExpr.left.welcome(this);
        if (opExpr.left.theType.equals(Type.STRING_TYPE)) {
            reportError(opExpr.left.line, opExpr.left.column, "Stringoperationen werden nicht unterstützt.");
            return;
        }

        opExpr.right.welcome(this);
        if (opExpr.right.theType.equals(Type.STRING_TYPE)) {
            reportError(opExpr.right.line, opExpr.right.column, "Stringoperationen werden nicht unterstützt.");
            return;
        }

        if (opExpr.operator.isLogical()) {
            opExpr.theType = Type.BOOLEAN_TYPE;
            if (!opExpr.left.theType.equals(Type.BOOLEAN_TYPE) || !opExpr.right.theType.equals(Type.BOOLEAN_TYPE)) {
                reportError(opExpr.line, opExpr.column, "Falscher Typ " + opExpr.left.theType.name() + " != " + Type.BOOLEAN_TYPE + "oder" + opExpr.right.theType.name() + "!=" + Type.BOOLEAN_TYPE);
            }
        } else if (opExpr.operator.isArithmetic()) {
            opExpr.theType = Type.LONG_TYPE;
            if (!opExpr.left.theType.equals(Type.LONG_TYPE) || !opExpr.right.theType.equals(Type.LONG_TYPE)) {
                reportError(opExpr.line, opExpr.column, "Falscher Typ " + opExpr.left.theType.name() + " != " + Type.LONG_TYPE + "oder" + opExpr.right.theType.name() + "!=" + Type.LONG_TYPE);
            }
        } else if (opExpr.operator.isComparison()) {
            opExpr.theType = Type.BOOLEAN_TYPE;
            if (!opExpr.left.theType.equals(Type.LONG_TYPE) || !opExpr.right.theType.equals(Type.LONG_TYPE)) {
                reportError(opExpr.line, opExpr.column, "Falscher Typ " + opExpr.left.theType.name() + " != " + Type.LONG_TYPE + "oder" + opExpr.right.theType.name() + "!=" + Type.LONG_TYPE);
            }
        }
    }

    @Override
    public void visit(Variable var) {
        Type type = env.getOrDefault(var.name, globalVars.getOrDefault(var.name, null));
        if (type == null) {
            reportError(var.line, var.column, "Unbekannte Variable: " + var.name);
        } else {
            var.theType = type;
        }
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        returnStatement.expr.welcome(this);
        if (!returnStatement.expr.theType.equals(currentFunction.theType)) {
            reportError(
                    returnStatement.line,
                    returnStatement.column,
                    "Der falsche Typ wird zurückgeben\n Erwartet:" + currentFunction.theType.name() + "\nTatsächlich: " + returnStatement.expr.theType.name()

            );
        }
        returnStatement.theType = currentFunction.theType;
    }

    @Override
    public void visit(VariableDecl variableDecl) {
        variableDecl.expr.welcome(this);
        variableDecl.theType = variableDecl.expr.theType;
        if (variableDecl.global) {
            globalVars.put(variableDecl.varName, variableDecl.theType);
        } else {
            env.put(variableDecl.varName, variableDecl.theType);
        }
    }


    @Override
    public void visit(Block block) {
        block.statements.forEach(stmt -> stmt.welcome(this));
    }

    @Override
    public void visit(FunctionCall functionCall) {
        // Verify library call
        if (functionCall.functionName.equals("drucke")) {
            functionCall.theType = Type.VOID_TYPE;
            functionCall.args.forEach(arg -> arg.welcome(this));
            if (!(functionCall.args.getFirst() instanceof StringLiteral)) {
                reportError(functionCall.line, functionCall.column, "Der erste Parameter von drucken muss ein String sein.");
            }
            return;
        }

        functionCall.args.forEach(arg -> arg.welcome(this));
        FunctionDefinition function = functions.get(functionCall.functionName);
        if (functionCall.functionName.equals("drucke")) {
            functionCall.theType = Type.VOID_TYPE;
        } else if (function == null) {
            reportError(functionCall.line, functionCall.column, "Unbekannte Funktion " + functionCall.functionName);
        } else if (functionCall.args.size() != function.parameters.size()) {
            reportError(functionCall.line, functionCall.column, "Funktionsaufruffehler: Falsche Anzahl an Argumenten.");
            functionCall.theType = Type.of(function.resultType);
        } else {
            var argsIterator = functionCall.args.iterator();
            for (var param : function.parameters) {
                var arg = argsIterator.next();
                if (!arg.theType.equals(Type.of(param.type))) {
                    reportError(arg.line, arg.column, "Funktionsaufruffehler: Falscher Typ " + arg.theType.name() + " != " + Type.of(param.type).name());
                }
            }
            functionCall.theType = Type.of(function.resultType);
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
        varAssignment.expr.welcome(this);
        Type varType = env.getOrDefault(varAssignment.varName, globalVars.getOrDefault(varAssignment.varName, null));
        if (varType == null) {
            reportError(varAssignment.line, varAssignment.column, "Unbekannte variable " + varAssignment.varName);
            return;
        }
        if (!varAssignment.expr.theType.equals(varType)) {
            reportError(varAssignment.line, varAssignment.column, "Zuweisungsfehler: " + varAssignment.expr.theType.name() + " != " + varType.name());
        }

    }
}
