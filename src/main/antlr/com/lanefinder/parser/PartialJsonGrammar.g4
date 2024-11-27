grammar PartialJsonGrammar;

// Tokens
L_CURL            : '{';
R_CURL            : '}';
L_BRACKET         : '[';
R_BRACKET         : ']';
COLON             : ':';
COMMA             : ',';

STRING            : '"' (~["\\] | '\\' .)* '"' ;
PARTIAL_STRING    : '"' (~["\\] | '\\' . | '\\')* [ \t\r\n]* EOF ;

NUMBER            : '-'? [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)? ;
PARTIAL_NUMBER    : '-' [ \t\r\n]* EOF
                  | '-'? [0-9]+ '.' [ \t\r\n]* EOF
                  | '-'? [0-9]+ ('.' [0-9]+)? [eE] [+-]? [ \t\r\n]* EOF ;

TRUE              : 'true';
PARTIAL_TRUE      : 't' [ \t\r\n]* EOF
                  | 'tr' [ \t\r\n]* EOF
                  | 'tru' [ \t\r\n]* EOF ;

FALSE             : 'false';
PARTIAL_FALSE     : 'f'[ \t\r\n]* EOF
                  | 'fa' [ \t\r\n]* EOF
                  | 'fal' [ \t\r\n]* EOF
                  | 'fals' [ \t\r\n]* EOF ;

NULL              : 'null';
PARTIAL_NULL      : 'n' [ \t\r\n]* EOF
                  | 'nu' [ \t\r\n]* EOF
                  | 'nul' [ \t\r\n]* EOF ;

WS                : [ \t\r\n]+ -> skip ;

// Production rules
json              : value ;
value             : STRING            # string
                  | PARTIAL_STRING    # partialString
                  | NUMBER            # number
                  | PARTIAL_NUMBER    # partialNumber
                  | TRUE              # true
                  | PARTIAL_TRUE      # partialTrue
                  | FALSE             # false
                  | PARTIAL_FALSE     # partialFalse
                  | NULL              # null
                  | PARTIAL_NULL      # partialNull
                  | jsonObject        # object
                  | partialJsonObject # partialObject
                  | jsonArray         # array
                  | partialJsonArray  # partialArray ;

jsonObject        : L_CURL keyValuePair (COMMA keyValuePair)* R_CURL
                  | L_CURL R_CURL ;

partialJsonObject : L_CURL (keyValuePair COMMA)* keyValuePair COMMA? EOF
                  | L_CURL EOF ;

keyValuePair      : STRING COLON value # pair
                  | STRING COLON       # partialPair
                  | STRING             # partialPair
                  | PARTIAL_STRING     # partialPair ;

jsonArray         : L_BRACKET value (COMMA value)* R_BRACKET
                  | L_BRACKET R_BRACKET ;

partialJsonArray  : L_BRACKET (value COMMA)* value COMMA? EOF
                  | L_BRACKET EOF ;
