package com.engelhardt.simon.visitor;

import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.AccessFlag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GenJBC implements Visitor {
    private final String className;
    private final String fullClassName;
    Map<String, FunctionDefinition> functions;
    ClassBuilder classBuilder = null;
    Map<String, Integer> variables = new HashMap<>();
    private CodeBuilder codeConstructor;

    public GenJBC(String fullClassName) {
        this.className = fullClassName.substring(fullClassName.lastIndexOf('/') + 1);
        this.fullClassName = fullClassName;
    }

    @Override
    public void visit(IntLiteral intLit) {
        codeConstructor.ldc(intLit.n);
    }

    @Override
    public void visit(OpExpr opExpr) {
        opExpr.left.welcome(this);
        opExpr.right.welcome(this);
        switch (opExpr.operator) {
            case add -> codeConstructor.ladd();
            case mult -> codeConstructor.lmul();
        }
    }

    @Override
    public void visit(Variable variable) {
        codeConstructor.lload(variables.get(variable.name));
    }

    @Override
    public void visit(ReturnStatement returnStatement) {
        returnStatement.expr.welcome(this);
        codeConstructor.lreturn();
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        variables.clear();
        int i = 0;
        for (var v : functionDefinition.parameters) {
            variables.put(v.name, i);
            i += 2;
        }

        var ps = new ArrayList<ClassDesc>();
        for (var p : functionDefinition.parameters) {
            ps.add(ClassDesc.ofDescriptor(Type.of(p.type, "").jvmName()));
        }
        var methodDescriptor = MethodTypeDesc.of(ClassDesc.ofDescriptor(Type.of(functionDefinition.resultType, "").jvmName()), ps);
        classBuilder.withMethod(
                functionDefinition.name,
                methodDescriptor,
                AccessFlag.STATIC.mask() | AccessFlag.PUBLIC.mask(),
                methodBuilder -> methodBuilder.withCode(cc -> {
                    this.codeConstructor = cc;
                    functionDefinition.block.welcome(this);
                })
        );

    }

    @Override
    public void visit(VariableDecl variableDecl) {

    }

    @Override
    public void visit(Prog prog) {
        functions = new HashMap<>();
        for (var funcDef : prog.functionDefinitions) {
            functions.put(funcDef.name, funcDef);
        }

        var classDescriptor = ClassDesc.of(className);
        var bytes = ClassFile.of().build(
                classDescriptor,
                classBuilder -> {
                    this.classBuilder = classBuilder;
                    classBuilder.withFlags(AccessFlag.FINAL);
                    prog.functionDefinitions.forEach(fd -> fd.welcome(this));
                });
        try {
            var out = new FileOutputStream(STR."\{fullClassName}.class");
            out.write(bytes);
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
        functionCall.args.forEach(arg -> arg.welcome(this));
        var as = functionCall.args.stream().map(a -> ClassDesc.ofDescriptor(a.type.jvmName())).toList();
        codeConstructor.invokestatic(
                ClassDesc.of(className),
                functionCall.functionName,
                MethodTypeDesc.of(ClassDesc.ofDescriptor(functionCall.type.jvmName()), as)
        );
    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(IfElseStatement ifElseStatement) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    @Override
    public void visit(ContinueStatement continueStatement) {
        
    }

    @Override
    public void visit(BreakStatement breakStatement) {

    }
}
