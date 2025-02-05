grammar alman;
//@header {
//import nodes.*;
//}

program returns [AST result]:
  (functionDefinition | varDecl | statement)*;

varDecl returns [VariableDecl result]:
    ID COLON ID (EQUAL expr)? SEMICOLON // name: string = "100";
;

functionDefinition returns [FunctionDefinition result]:
  FUN ID LPAR formalParameters? RPAR COLON ID // fun name (arg1: type1, arg2: type2, ...) : returnType
  block
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
    | ifElseStatement
    | returnStatement
    | functionCall SEMICOLON
    | expr SEMICOLON
    ;

returnStatement returns [ReturnStatement result]:
    RETURN expr SEMICOLON
    ;

ifElseStatement returns [IfElseStatement result]:
    IF LPAR expr RPAR block (ELSE block)?
    ;

expr returns [AST result]:
    | expr (MULT | DIV) expr
    | expr (PLUS | MINUS) expr
    | expr (IS_EQUAL | NOT_EQUAL | LESS_THAN | GREATER_THAN | GREATER_THAN_EQUAL | LESS_THAN_EQUAL) expr
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
POWER: '^';
MOD: '%';
LPAR : '(';
RPAR : ')';
LBRC: '{';
RBRC: '}';
COLON: ':';
COMMA: ',';
SEMICOLON: ';';
EQUAL: '=';

IF: 'if';
ELSE: 'else';
FUN: 'fun';
RETURN: 'return';

ID:
    VALID_ID_START ID_CHAR*;

fragment VALID_ID_START:
 [a-zA-Z];

 fragment ID_CHAR:
   VALID_ID_START|('0'..'9');


SINGLE_LINE_COMMENT : '//' ~[\r\n]* -> skip;  // Skips text after '//' until the end of the line
MULTI_LINE_COMMENT  : '/*' .*? '*/' -> skip;  // Skips everything between '/*' and '*/', allowing multi-line comments
WS: [ \t\r\n]+ -> skip;
