grammar XPath1;
options {
	 language = Java;
 	 backtrack=true;
  	 output    = AST;
  	ASTLabelType=CommonTree;
	} 

tokens {
    XPATH; SYNAPSE_SPECIFIC; QNAME; NameSpacedQNAME; STRING; NUMBER; VARREF; 
    OREXPR; ANDEXPR; UNIONEXPR; EQUEXPRESSION; 
    PREDICATE; FUNCALL;
    NAMED_AXIS_STEP; SIMPLE_AXIS_STEP; ABBREVIATED_AXIS_STEP;
    NAME_AXIS; ATTRIBUTE_AXIS;
    EXPRLIST;
    DOLLAR;
    ANY_NODE; ANY_NAMESPACED_NODE;
    LOCATION_PATH;
    QNAME_PREDICATE;
    NAME;
}

@header {
	package org.apache.synapse.util.streaming_xpath.compiler;
}

@lexer::header { 
	package org.apache.synapse.util.streaming_xpath.compiler;
}

// ---------------- Parser Rules ---------------- //
xpath
    :  locationPath			
    ;
	
locationPath 
    : SynapseSpecificBody relativeLocationPath	->^(LOCATION_PATH SynapseSpecificBody relativeLocationPath)
    | SynapseSpecific relativeLocationPath	->^(LOCATION_PATH SynapseSpecific relativeLocationPath)
    | SynapseSpecificBody absoluteLocationPath	->^(LOCATION_PATH SynapseSpecificBody absoluteLocationPath)
    | SynapseSpecific absoluteLocationPath	->^(LOCATION_PATH SynapseSpecific absoluteLocationPath)
    | relativeLocationPath			->^(LOCATION_PATH relativeLocationPath)
    | absoluteLocationPath			->^(LOCATION_PATH absoluteLocationPath)
    | SynapseSpecific ':' Name			->^(LOCATION_PATH Name)
    | SynapseSpecificBody                       ->^(LOCATION_PATH SynapseSpecificBody)
    | SynapseSpecific                           ->^(LOCATION_PATH SynapseSpecific)
    ;
	    
absoluteLocationPath 
    :SingleAxisStep relativeLocationPath?		-> ^(SingleAxisStep relativeLocationPath?)
    |RecursiveAxisStep relativeLocationPath	-> ^(RecursiveAxisStep relativeLocationPath)
;

relativeLocationPath 
    : step 
    ( (SingleAxisStep  relativeLocationPath)		-> ^(SingleAxisStep step relativeLocationPath)
    |(RecursiveAxisStep  relativeLocationPath)	-> ^(RecursiveAxisStep step relativeLocationPath)
    )
    |step
    ;

step
    : namedAxisStep (predicate*)?		->^(QNAME_PREDICATE namedAxisStep (predicate*)?)
    | abbreviatedAxisStep  (predicate*)?		->^(QNAME_PREDICATE abbreviatedAxisStep (predicate*)?)
    ;


abbreviatedAxisStep
    : '.' 				-> ^(ABBREVIATED_AXIS_STEP '.')
    | '..' 				-> ^(ABBREVIATED_AXIS_STEP '..')
    ;

namedAxisStep
    : AbbreviatedAxisSpecifier nodeTest		-> ^(ATTRIBUTE_AXIS AbbreviatedAxisSpecifier nodeTest)
    |(axisSpecifier? nodeTest) 		-> ^(NAMED_AXIS_STEP axisSpecifier? nodeTest);

axisSpecifier
    : AxisName AxisNameSeparator		-> ^(NAME_AXIS AxisName)
    ;

nodeTest
    : nameTest 
    | NodeType LeftParenthesis RightParenthesis 	-> ^(ANY_NODE NodeType)
    | ProcessingInstruction LeftParenthesis StringLiteral RightParenthesis
    ; 

nameTest 
    : Star 				-> ^(ANY_NODE)
    | Name Colon Star 			-> ^(ANY_NAMESPACED_NODE Name)
    | qName
    ;

predicate
    : LeftSquareBracket predicateExpr RightSquareBracket -> ^(PREDICATE predicateExpr)
    |LeftSquareBracket '1' RightSquareBracket -> ^(PREDICATE '1')
    ;

predicateExpr : expr;
    
functionCall
    : qName LeftParenthesis
    expressionList? 
    RightParenthesis 			-> ^(FUNCALL qName expressionList?)
	;
	
expressionList
    : expr ( Comma expr )* 			-> ^(EXPRLIST expr+)
    ;	

pathExpr
    : locationPath
    | filterExpr (simpleAxisStep  relativeLocationPath)?
    ;
    
simpleAxisStep
    : (SingleAxisStep			-> ^(SIMPLE_AXIS_STEP SingleAxisStep)
    | RecursiveAxisStep 			-> ^(SIMPLE_AXIS_STEP RecursiveAxisStep)
    )
    ;
    
filterExpr: primaryExpr predicate?;

primaryExpr
    : LeftParenthesis expr RightParenthesis
    | literal
    | functionCall
    ;

expr: orExpr;

orExpr: andExpr (Or andExpr)* 		-> ^(OREXPR andExpr+);

andExpr: equalityExpr (And equalityExpr)? 	-> ^(ANDEXPR equalityExpr+);

equalityExpr
    : relationalExpr (EqualtyOp relationalExpr)? 	-> ^(EQUEXPRESSION relationalExpr (EqualtyOp relationalExpr)?);
    
relationalExpr: unionExpr;

unionExpr: pathExpr (Pipe pathExpr)? 		-> ^(UNIONEXPR pathExpr+);

literal 
    : StringLiteral 			-> ^(STRING StringLiteral)
    | numericLiteral 			-> ^(NUMBER numericLiteral)
    | VariableReference 			-> ^(VARREF VariableReference)
    ;
    
numericLiteral: IntegerLiteral | DecimalLiteral | DoubleLiteral;

qName
   :Name Colon  Name 			-> ^(NameSpacedQNAME Name Name)
   |Name 				-> ^(QNAME Name)
   ;

   	
// ---------------- Lexer Rules ---------------- //
SingleAxisStep: '/';   
RecursiveAxisStep: '//';

AxisNameSeparator: '::';
AbbreviatedAxisSpecifier: '@';

Star:      '*';
Colon:     ':';
Comma:     ',';
Pipe:      '|';

SynapseSpecificBody
     : '$body'
     ;	
SynapseSpecific
     : '$header'|'$func'|'$axis2'|'$ctx'|'$trp'|'$url'
     ;

LeftParenthesis: '(';    	
RightParenthesis: ')';

LeftSquareBracket: '[';
RightSquareBracket: ']';

And	: 'and';
Or	: 'or';

AxisName
    :  'ancestor'  | 'ancestor-or-self'  | 'attribute' |
       'child'     | 'descendant'        | 'descendant-or-self' |
       'following' | 'following-sibling' | 'namespace' |
       'parent'    | 'preceding'         | 'preceding-sibling' |
       'self'
    ;

ProcessingInstruction:  'processing-instruction';

NodeType
	:  'comment' | 'text' | ProcessingInstruction | 'node';

IntegerLiteral :  ('0'..'9')+;
DecimalLiteral :  ('.' ('0'..'9')+)  | (('0'..'9')+ '.' '0'..'9'*);
DoubleLiteral  :  (('.' ('0'..'9')+) | (('0'..'9')+ ('.' '0'..'9'*)?)) ('e' | 'E') ('+' | '-')? ('0'..'9')+;

StringLiteral : '"' ~('"')* '"' | '\'' ~('\'')* '\'';

EqualtyOp: '=' | '!='|'&lt;'|'&lt;='|'&gt;'|'&gt;=';

VariableReference: '$' Name;

Name	
    :
    ( '\u0024' | '\u005f'|'\u0041'..'\u005a' | '\u0061'..'\u007a' |'\u00c0'..'\u00d6' | '\u00d8'..'\u00f6' |'\u00f8'..'\u00ff' | '\u0100'..'\u1fff' |'\u3040'..'\u318f' | '\u3300'..'\u337f' | 
      '\u3400'..'\u3d2d' | '\u4e00'..'\u9fff' | '\uf900'..'\ufaff') ( '\u0024' | '\u005f'|'\u0041'..'\u005a' | '\u0061'..'\u007a' |'\u00c0'..'\u00d6' | '\u00d8'..'\u00f6' |'\u00f8'..'\u00ff' |
      '\u0100'..'\u1fff' |'\u3040'..'\u318f' | '\u3300'..'\u337f' | '\u3400'..'\u3d2d' | '\u4e00'..'\u9fff' | '\uf900'..'\ufaff' | '0'..'9' | '.' | '-')*
    ;   

WS: (' '|'\t'|'\u000C') {skip();};