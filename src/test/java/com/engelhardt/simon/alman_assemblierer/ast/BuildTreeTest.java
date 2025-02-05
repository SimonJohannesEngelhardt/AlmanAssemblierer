package com.engelhardt.simon.alman_assemblierer.ast;

import com.engelhardt.simon.antlr.almanParser;
import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.utils.Operator;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BuildTreeTest {
    BuildTree buildTree;
    Token startToken;

    @BeforeEach
    public void setUp() {
        buildTree = new BuildTree();

        // Start Token
        startToken = mock(Token.class);
        when(startToken.getLine()).thenReturn(1);
        when(startToken.getCharPositionInLine()).thenReturn(0);
    }

    @Nested
    class ExitZahl {
        @Test
        public void shouldCreateAnIntLiteral() {
            // Mock everything
            var ctx = mock(almanParser.ZahlContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            when(ctx.NUMBER()).thenReturn(mock(TerminalNode.class));
            when(ctx.NUMBER().getText()).thenReturn("42");

            // Call the method to test
            buildTree.exitZahl(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            IntLiteral result = (IntLiteral) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(42L, result.n);
        }
    }

    @Nested
    class exitExpr {
        void testOperator(Operator operator) {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var exprCtx1 = mock(almanParser.ExprContext.class);
            var exprCtx2 = mock(almanParser.ExprContext.class);
            when(ctx.expr()).thenReturn(List.of(exprCtx1, exprCtx2));
            switch (operator) {
                case add ->
                        when(ctx.PLUS()).thenReturn(mock(TerminalNode.class));
                case sub ->
                        when(ctx.MINUS()).thenReturn(mock(TerminalNode.class));
                case mult ->
                        when(ctx.MULT()).thenReturn(mock(TerminalNode.class));
                case div ->
                        when(ctx.DIV()).thenReturn(mock(TerminalNode.class));
                case eq ->
                        when(ctx.IS_EQUAL()).thenReturn(mock(TerminalNode.class));
            }
            var left = new IntLiteral(1, 0, 42);
            exprCtx1.result = left;
            var right = new IntLiteral(1, 0, 84);
            exprCtx2.result = right;

            // Call the method to test
            buildTree.exitExpr(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            OpExpr result = (OpExpr) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(operator, result.operator);
            assertEquals(left, result.left);
            assertEquals(right, result.right);
        }

        @Test
        void shouldCreateAnIntLiteral() {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var zahlCtx = mock(almanParser.ZahlContext.class);
            when(ctx.zahl()).thenReturn(zahlCtx);

            zahlCtx.result = new IntLiteral(1, 0, 42);

            // Call the method to test
            buildTree.exitExpr(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            IntLiteral result = (IntLiteral) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(42L, result.n);
        }

        @Test
        void shouldCreateAPlusExpr() {
            testOperator(Operator.add);
        }

        @Test
        void shouldCreateAMinusExpr() {
            testOperator(Operator.sub);
        }

        @Test
        void shouldCreateADivExpr() {
            testOperator(Operator.div);
        }

        @Test
        void shouldCreateAMultExpr() {
            testOperator(Operator.mult);
        }

        @Test
        void shouldCreateAnEqExpr() {
            testOperator(Operator.eq);
        }

        @Test
        void shouldCreateAVariable() {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            when(ctx.ID()).thenReturn(mock(TerminalNode.class));
            when(ctx.ID().getText()).thenReturn("my_var");

            // Call the method to test
            buildTree.exitExpr(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            Variable result = (Variable) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals("my_var", result.name);
        }

        @Test
        void shouldSetAttributesForParanthesis() {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var exprCtx1 = mock(almanParser.ExprContext.class);
            exprCtx1.result = new OpExpr(
                    1,
                    0,
                    new IntLiteral(1, 0, 42),
                    new IntLiteral(1, 0, 84),
                    Operator.add
            );
            when(ctx.expr()).thenReturn(List.of(exprCtx1));
            when(ctx.LPAR()).thenReturn(mock(TerminalNode.class));
            when(ctx.RPAR()).thenReturn(mock(TerminalNode.class));

            // Call the method to test
            buildTree.exitExpr(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            AST result = ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertTrue(result.attribute.parenthesis);
        }

        @Test
        void shouldThrowAnExceptionForUnknownOperator() {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            // Call the method to test
            assertThrows(RuntimeException.class, () -> buildTree.exitExpr(ctx));
        }
    }


    @Nested
    class ExitBlock {
        @Test
        void shouldCreateABlockWithTwoStatements() {
            // Mock everything
            var ctx = mock(almanParser.BlockContext.class);
            when(ctx.getStart()).thenReturn(startToken);
            var statement1 = mock(almanParser.StatementContext.class);
            statement1.result = new IntLiteral(1, 0, 42);
            var statement2 = mock(almanParser.StatementContext.class);
            statement2.result = new IntLiteral(1, 0, 84);
            when(ctx.statement()).thenReturn(List.of(
                    statement1,
                    statement2
            ));

            // Call the method to test
            buildTree.exitBlock(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            Block result = (Block) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(2, result.statements.size());
            assertEquals(result.statements.get(0), statement1.result);
            assertEquals(result.statements.get(1), statement2.result);

        }

        @Test
        void shouldCreateABlockWithNoStatements() {
            // Mock everything
            var ctx = mock(almanParser.BlockContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            when(ctx.statement()).thenReturn(List.of());

            // Call the method to test
            buildTree.exitBlock(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            Block result = (Block) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(0, result.statements.size());
        }
    }

}
