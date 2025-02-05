grammar alman;
@header {
package com.engelhardt.simon.antlr;
import com.engelhardt.simon.ast.*;
}

program returns [AST result]:
  (functionDefinition | statement)*;

functionDefinition returns [FunctionDefinition result]:
  FUNCTION_HEAD ID LPAR formalParameters? RPAR COLON ID // fun name (arg1: type1, arg2: type2, ...) : returnType
  block
;

varDecl returns [VariableDecl result]:
    (LET | CONST) ID COLON ID (EQUAL expr)? SEMICOLON
;

varAssignment returns [VarAssignment result]:
    ID EQUAL expr SEMICOLON
;

formalParameters returns [AST result]:
    formalParameter (COMMA formalParameter)*
;

formalParameter returns [AST result]:
    ID COLON ID
;

block returns [Block result]:
    LBRC statement* RBRC
;

statement returns [AST result]
    : varDecl
    | varAssignment
    | ifElseStatement
    | whileStatement
    | (BREAK | CONTINUE) SEMICOLON
    | returnStatement
    | functionCall SEMICOLON
    | expr SEMICOLON
    ;

returnStatement returns [ReturnStatement result]:
    RETURN expr SEMICOLON
    ;

ifElseStatement returns [IfElseStatement result]:
    IF LPAR expr RPAR block
    (ELSE_IF LPAR expr RPAR block)*
    (ELSE block)?
    ;

whileStatement returns [WhileStatement result]:
    WHILE LPAR expr RPAR block
    ;

expr returns [AST result]:
    expr MOD expr
    | expr (MULT | DIV) expr
    | expr (PLUS | MINUS) expr
    | expr (IS_EQUAL | NOT_EQUAL | LESS_THAN | GREATER_THAN | GREATER_THAN_EQUAL | LESS_THAN_EQUAL) expr
    | functionCall
    | zahl
    | ID
    | LPAR expr RPAR
    ;

functionCall returns [AST result]:
    ID LPAR exprList RPAR
    ;

exprList: expr (COMMA expr)*; // arg list

zahl returns [ IntLiteral result]: NUMBER;

NUMBER : [0-9]+;
PLUS : '+';
MINUS : '-';
NOT: '!';
MULT : '*';
DIV : '/';
IS_EQUAL: '==';
NOT_EQUAL: '!=';
LESS_THAN: '<';
GREATER_THAN: '>';
LESS_THAN_EQUAL: '<=';
GREATER_THAN_EQUAL: '>=';
MOD: '%';
LPAR : '(';
RPAR : ')';
LBRC: '{';
RBRC: '}';
COLON: ':';
COMMA: ',';
SEMICOLON: ';';
EQUAL: '=';

IF: 'wenn';
ELSE_IF: 'ansonsten wenn';
ELSE: 'ansonsten';
FUNCTION_HEAD: 'definiere';
RETURN: 'gib zurueck';
WHILE: 'waehrend'; // oder solange
BREAK: 'break';
CONTINUE: 'continue';
LET: 'lasse';
CONST: 'konstante';

ID:
    VALID_ID_START ID_CHAR*;
TYPE:
    VALID_ID_START ID_CHAR*;

fragment VALID_ID_START:
 [a-zA-Z];

fragment ID_CHAR:
  VALID_ID_START|('0'..'9');


SINGLE_LINE_COMMENT : '//' ~[\r\n]* -> skip;  // Skips text after '//' until the end of the line
MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;  // Skips everything between '/*' and '*/', allowing multi-line comments
WS: [ \t\r\n]+ -> skip;
