grammar cal;

program:                decl_list function_list main;
decl_list:              decl SEMI decl_list
                    |
                    ;
decl:                   var_decl    
                    |   const_decl  
                    ;
var_decl:               VARIABLE ID COLON type; 

const_decl:             CONSTANT ID COLON type ASSIGN expression;

function_list:          (function function_list | );

function:               func_return_type ID LBR parameter_list RBR IS
                        decl_list
                        BEGIN
                        statement_block
                        RETURN LBR (expression | ) RBR SEMI
                        END
                    ;

type:                   BOOLEAN #Type_bool
                    |   INTEGER #Type_int
                    ;

func_return_type:       VOID    #Func_ret_void
                    |   type    #Func_ret_other_type
                    ;


parameter_list:         nemp_parameter_list
                    |
                    ;

nemp_parameter_list:    ID COLON type   #Param_list
                    |   ID COLON type COMMA nemp_parameter_list #Param_list_recur
                    ;

main:                   MAIN BEGIN decl_list statement_block END;

statement_block:        (statement statement_block | );

statement:              ID ASSIGN expression SEMI   #Assignstm
                    |   ID LBR arg_list RBR SEMI    #Argstm
                    |   BEGIN statement_block END   #BEstm
                    |   IF condition BEGIN statement_block END ELSE BEGIN statement_block END #Ifstm
                    |   WHILE condition BEGIN statement_block END   #Loopstm
                    |   PASS SEMI   #Passstm
                    ;
expression:             frag binary_arith_op frag   #BinopExpr
                    |   LBR expression RBR  #BracketExpr
                    |   ID LBR arg_list RBR #Invokdedid_expr
                    |   frag    #FragBranch
                    ;
binary_arith_op:        PLUS    #PlusBinop
                    |   MINUS   #MinusBinop
                    ;

frag:                   ID  #Frag_id
                    |   MINUS ID  #Frag_negid
                    |   NUMBER  #Frag_num
                    |   BV  #Frag_BV
                    |   LBR expression RBR frag_prime   #Frag_expr_recur
                    |   ID LBR arg_list RBR frag_prime  #Frag_invokedid_recur
                    |   ID frag_prime   #Frag_id_recur
                    |   NEG ID frag_prime   #Frag_neg_id_recur
                    |   NUMBER frag_prime   #Frag_num_recur
                    |   BV frag_prime   #Frag_BV_recur
                    ;
frag_prime:             binary_arith_op frag frag_prime #FragPrime_branch1
                    |   binary_arith_op frag    #FragPrime_branch2
                    |                           #FragPrime_empty
                    ;

condition:              NEG condition   #Neg_cond
                    |   LBR condition RBR   #Bracket_cond
                    |   expression comp_op expression   #Comp_op_cond
                    |   condition (OR | AND) condition  #AndOr_cond
                    ;
comp_op:                EQUAL   #Eq_op
                    |   NOT_EQUAL   #Neq_op
                    |   LESSER  #Less_op
                    |   LESSEQUAL   #LessEq_op
                    |   GREATER #Great_op
                    |   GREATEREQUAL    #GreatEq_op
                    ;
arg_list:               nemp_arg_list
                    |   
                    ;



nemp_arg_list:          ID      #NempArgListID
                    |   ID COMMA nemp_arg_list  #NempArgListIDs
                    ;

                


fragment A : [aA]; // match either an 'a' or 'A'
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];

fragment Letter:		[a-zA-Z];
fragment Digit:			[0-9];
fragment UnderScore:	'_';
fragment Zero:          [0];



COMMENT:          '//'.*?'\n'->skip;
NESTED_COMMENT:   '/''*' (NESTED_COMMENT | .)*?'*''/'->skip;
COMMA:            ',';
SEMI:             ';';
COLON:            ':';
ASSIGN:           ':=';
LBR:              '(';
RBR:              ')';
PLUS:             '+';
MINUS:            '-';
NEG:              '~';
OR:               '|';
AND:              '&';
EQUAL:            '=';
NOT_EQUAL:        '!=';
GREATEREQUAL:     '>=';
GREATER:          '>';
LESSEQUAL:        '<=';
LESSER:           '<';




BEGIN:             B E G I N;
END:               E N D;
IF:                I F;  
ELSE:              E L S E;  
WHILE:             W H I L E;
MAIN:              M A I N;
RETURN:            R E T U R N;
IS:                I S;
PASS:              S K I P;

CONSTANT:         C O N S T A N T;
VARIABLE:         V A R I A B L E;
VOID:             V O I D;
INTEGER:          I N T E G E R;
BOOLEAN:          B O O L E A N;

BV:				  'true' | 'false';

ID:               Letter (Letter | Digit | UnderScore)*;
NUMBER:           (Zero | MINUS? [1-9] + Digit*);

WS:			[ \t\n\r]+ -> skip;
