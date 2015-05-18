/**
 * Copyright (C) 2014 Politecnico di Milano (marco.miglierina@polimi.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar Condition;

options
{
  // antlr will generate java lexer and parser
  language = Java;
}
	
// ***************** lexer rules:
//the grammar must contain at least one lexer rule
LPAR : '(' ;
RPAR : ')' ;
AND : '&&';
OR : '||';
NOT : '!';
COMMA : ',';
//MAXTERMINAL : 'maxOccurrence';
//MINTERMINAL : 'minOccurrence';
//PARENTTERMINAL : 'parentCondition';
METRICTERMINAL : 'METRIC';
GTEQ : '>=';
LTEQ : '<=';
EQ : '=';
GT : '>';
LT : '<';
NOTEQ : '<>';
METRICNAME : ('a'..'z' | 'A'..'Z' | '_' | '-') ('a'..'z' | 'A'..'Z' | '_' | '-' | '0'..'9')*;
INT : ('1'..'9')('0'..'9')* | '0';
DECIMAL : ('0' | ('1'..'9')('0'..'9')*) '.' ('0'..'9')+;
WS :   (' ' | '\t' | '\r'| '\n') -> skip;

// ***************** parser rules:
//our grammar
expression : condition;

condition : term | term OR condition;

term : factor | factor AND term;

factor : atom | NOT factor | LPAR condition RPAR;

atom : var operator var; //| MAXTERMINAL LPAR METRICNAME COMMA INT RPAR | MINTERMINAL LPAR METRICNAME COMMA INT RPAR; // | PARENTTERMINAL;

var : METRICTERMINAL | DECIMAL | INT ;

operator : GTEQ | LTEQ | EQ | GT | LT | NOTEQ;