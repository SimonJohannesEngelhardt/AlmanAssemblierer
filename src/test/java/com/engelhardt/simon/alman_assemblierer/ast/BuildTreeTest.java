package com.engelhardt.simon.alman_assemblierer.ast;

import com.engelhardt.simon.antlr.almanParser;
import com.engelhardt.simon.ast.*;
import com.engelhardt.simon.ast.Operator;
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
            IntLiteral result = ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(42L, result.n);
        }
    }

    @Nested
    class ExitExpr {
        void testOperator(Operator operator) {
            // Mock everything
            var ctx = mock(almanParser.ExprContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var exprCtx1 = mock(almanParser.ExprContext.class);
            var exprCtx2 = mock(almanParser.ExprContext.class);
            when(ctx.expr()).thenReturn(List.of(exprCtx1, exprCtx2));
            switch (operator) {
                case add -> when(ctx.PLUS()).thenReturn(mock(TerminalNode.class));
                case sub -> when(ctx.MINUS()).thenReturn(mock(TerminalNode.class));
                case mult -> when(ctx.MULT()).thenReturn(mock(TerminalNode.class));
                case div -> when(ctx.DIV()).thenReturn(mock(TerminalNode.class));
                case eq -> when(ctx.IS_EQUAL()).thenReturn(mock(TerminalNode.class));
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
            Block result = ctx.result;
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
            Block result = ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(0, result.statements.size());
        }
    }

    @Nested
    class ExitStatement {
        @Test
        void shouldReturnAVarDecleration() {
            // Mock everything
            var ctx = mock(almanParser.StatementContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var varDecl = mock(almanParser.VarDeclContext.class);
            varDecl.result = new VariableDecl(1, 0, "my_var", "int", new IntLiteral(1, 0, 42));
            when(ctx.varDecl()).thenReturn(varDecl);

            // Call the method to test
            buildTree.exitStatement(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            VariableDecl result = (VariableDecl) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals("my_var", result.varName);
            assertEquals("int", result.type);
            assertEquals(42L, ((IntLiteral) result.statement).n);

        }

        @Test
        void shouldReturnAnIfElseStatement() {
            // Mock everything
            var ctx = mock(almanParser.StatementContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var ifElse = mock(almanParser.IfElseStatementContext.class);
            when(ctx.ifElseStatement()).thenReturn(ifElse);
            ifElse.result = new IfElseStatement(
                    1,
                    0,
                    new IntLiteral(1, 0, 42),
                    new Block(1, 0, List.of(new IntLiteral(1, 0, 84))),
                    List.of(new IntLiteral(1, 0, 42)),
                    List.of(new Block(1, 0, List.of(new IntLiteral(1, 0, 84)))),
                    new Block(1, 0, List.of(new IntLiteral(1, 0, 84)))
            );

            // Call the method to test
            buildTree.exitStatement(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            IfElseStatement result = (IfElseStatement) ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);
            assertEquals(42L, ((IntLiteral) result.ifCondition).n);
            assertEquals(42L, ((IntLiteral) result.elseifConditions.getFirst()).n);
            assertEquals(84, ((IntLiteral) result.elseBlock.statements.getFirst()).n);


        }
    }

    @Nested
    class ExitIfElseStatement {
        @Test
        void shouldCreateAnIfElseStatement() {
            // Mock everything
            var ctx = mock(almanParser.IfElseStatementContext.class);
            when(ctx.getStart()).thenReturn(startToken);

            var ifConditionMock = mock(almanParser.ExprContext.class);
            var ifBlockMock = mock(almanParser.BlockContext.class);
            var elseifCondition1Mock = mock(almanParser.ExprContext.class);
            var elseifBlock1Mock = mock(almanParser.BlockContext.class);
            var elseIfCondtion2Mock = mock(almanParser.ExprContext.class);
            var elseIfBlock2Mock = mock(almanParser.BlockContext.class);
            var elseBlockMock = mock(almanParser.BlockContext.class);

            IntLiteral ifCondition = new IntLiteral(0, 1, 0);
            ifConditionMock.result = ifCondition;

            Block ifBlock = new Block(
                    0,
                    1,
                    List.of(new IntLiteral(0, 0, 0), new IntLiteral(0, 0, 0))
            );
            ifBlockMock.result = ifBlock;


            IntLiteral elseIfCondition1 = new IntLiteral(0, 0, 100);
            elseifCondition1Mock.result = elseIfCondition1;
            IntLiteral elseIfCondition2 = new IntLiteral(0, 0, 200);
            elseIfCondtion2Mock.result = elseIfCondition2;

            Block elseIfBlock1 = new Block(
                    0, 0,
                    List.of(new IntLiteral(0, 0, -100), new IntLiteral(0, 0, -100))
            );
            elseifBlock1Mock.result = elseIfBlock1;
            Block elseIfBlock2 = new Block(
                    0, 0,
                    List.of(new IntLiteral(0, 0, -100), new IntLiteral(0, 0, -100))
            );
            elseIfBlock2Mock.result = elseIfBlock2;

            Block elseBlock = new Block(
                    0, 0,
                    List.of(new IntLiteral(0, 0, -1000), new IntLiteral(0, 0, -1000))
            );
            elseBlockMock.result = elseBlock;

            when(ctx.IF()).thenReturn(mock(TerminalNode.class));
            when(ctx.ELSE_IF()).thenReturn(List.of(mock(TerminalNode.class), mock(TerminalNode.class)));
            when(ctx.ELSE()).thenReturn(mock(TerminalNode.class));
            when(ctx.expr()).thenReturn(List.of(ifConditionMock, elseifCondition1Mock, elseIfCondtion2Mock));
            when(ctx.block()).thenReturn(List.of(ifBlockMock, elseifBlock1Mock, elseIfBlock2Mock, elseBlockMock));

            // Call the method to test
            buildTree.exitIfElseStatement(ctx);

            // Verify the result
            assertNotNull(ctx.result);
            IfElseStatement result = ctx.result;
            assertEquals(1, result.line);
            assertEquals(0, result.column);

            assertEquals(ifCondition, result.ifCondition);
            assertEquals(ifBlock, result.ifBlock);

            assertEquals(2, result.elseifConditions.size());
            assertEquals(elseIfCondition1, result.elseifConditions.getFirst());
            assertEquals(elseIfCondition2, result.elseifConditions.getLast());

            assertEquals(2, result.elseifBlocks.size());
            assertEquals(elseIfBlock1, result.elseifBlocks.getFirst());
            assertEquals(elseIfBlock2, result.elseifBlocks.getLast());

            assertEquals(elseBlock, result.elseBlock);
        }
    }

}
