package com.engelhardt.simon.ast;

import com.engelhardt.simon.visitor.Visitor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {
    @Test
    public void testConstructor() {
        Block block = new Block(1, 1, new ArrayList<>());
        assertEquals(1, block.line);
        assertEquals(1, block.column);
        assertEquals(0, block.statements.size());
    }

    @Test
    public void testConstructorWithStatements() {
        ArrayList<AST> statements = new ArrayList<>();
        statements.add(new IntLiteral(1, 1, 42));
        statements.add(new IntLiteral(1, 1, 42));
        Block block = new Block(1, 1, statements);
        assertEquals(1, block.line);
        assertEquals(1, block.column);
        assertEquals(2, block.statements.size());
        assertEquals(42, ((IntLiteral) block.statements.getFirst()).n);
        assertEquals(42, ((IntLiteral) block.statements.getLast()).n);
    }

    @Test
    public void testWelcome() {
        Visitor mockedVisitor = mock(Visitor.class);
        Block block = new Block(1, 1, new ArrayList<>());
        block.welcome(mockedVisitor);
        verify(mockedVisitor).visit(block);
    }
}
