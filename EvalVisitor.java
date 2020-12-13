import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.Collection;
import java.util.LinkedList;

public class EvalVisitor extends calBaseVisitor<Object>{
    HashMap<String, HashMap<String,String>> table = new HashMap<String,HashMap<String,String>>();
    Stack<HashMap<String,HashMap<String,String>>> scope = new Stack<>();
    

    public HashMap<String,String> init_attributes(String type,String decl_type){
        HashMap<String,String> id_attributes = new HashMap<String,String>();
        String loctype = type;
        String locdecl_type = decl_type;
        String accessed = "f";
        id_attributes.put("type",loctype);
        id_attributes.put("decl_type",locdecl_type);
        id_attributes.put("accessed",accessed);
        return id_attributes;
    }

    public Boolean is_declared(String id){
        Boolean ret = scope.peek().containsKey(id);
        return ret;
    }

    //get the declate type of an identifier ie constant or variable
    public String check_decl_type(String id){
        try{
            if (is_declared(id)){
                return scope.peek().get(id).get("decl_type");
            }
        }catch(Exception e){
            System.out.println(e);
        }
        //make not declared error object
        return "Not Declared";
    }

    public Boolean assign_type_check(String given_type, String type){
        if(type.equals(given_type)){
            return true;
        }else{
            //raise an error
            return false;
        }        
    }

    //program
    public Boolean visitProgram(calParser.ProgramContext ctx){
        scope.push(table);
        visit(ctx.decl_list());
        visit(ctx.function_list());
        visit(ctx.main());
        scope.pop();
        //program terminated
        return true;
    }
    //main
    public Boolean visitMain(calParser.MainContext ctx){
        visit(ctx.decl_list());
        visit(ctx.statement_block());
        return true;
    }
    //function
    @Override
    public String visitFunction(calParser.FunctionContext ctx){
        /*set the id -> key
        funct_attr -> value
        declare type -> function
        set param list length for easy access for comparison with arg_list
        get the paramlist as a string,for comparison with arg_list types
        */
        
        String func_id = ctx.ID().getText();
        String ret_type = visit(ctx.func_return_type()).toString();
        String decl_type = "func";
        String param_li_str = ctx.parameter_list().getText(); 
        String param_size = String.valueOf(ctx.parameter_list().toString().split(",").length);
        
        HashMap<String,String> func_attr = new HashMap<String,String>();
        func_attr.put("id", func_id);
        func_attr.put("type", ret_type);
        func_attr.put("decl_type",decl_type);
        func_attr.put("param_list",param_li_str);
        func_attr.put("param_size",param_size);
        func_attr.put("accessed","f");
        
        scope.peek().put(func_id, func_attr);
        
        for(Object V:scope.peek().values().toArray()){
            System.out.println(V.toString());
        }
        HashMap<String, HashMap<String,String>> local_table = new HashMap<String,HashMap<String,String>>();
        
        scope.push(local_table);
        visit(ctx.decl_list());
        visit(ctx.statement_block());
        visit(ctx.expression());

        scope.pop();
        //terminate
        return ret_type;

    }

    //track the declare type of ID's,not the type of value but whether its a constant or variable
    @Override
    public String visitVar_decl(calParser.Var_declContext ctx){
        String id = ctx.ID().getText();
        //check for already existing id
        if (is_declared(id)){
            //make error object
            return "ID already declared";
        }
        String type = visit(ctx.type()).toString();
        HashMap<String,String> attr = init_attributes(type, "var");
        scope.peek().put(id,attr);
        return type;
    }


    @Override
    public String visitConst_decl(calParser.Const_declContext ctx){
        String id = ctx.ID().getText();
        //check for already existing id
        if (is_declared(id)){
            //make error object
            return "ID already declared";
        }
        //returns the expression type 
        String given_type = visit(ctx.expression()).toString();
        //gets the type given in context
        String type = visit(ctx.type()).toString();
        
        HashMap<String,String> attr = init_attributes(type, "const");
        if (assign_type_check(given_type,type)){
            scope.peek().put(id, attr);
        }else{
            //err
            System.out.println("Unmatched type");
        }
        
        
        return type;
    }
    
    
    // statement branches
    @Override 
    public String visitAssignstm(calParser.AssignstmContext ctx){
        String id = ctx.ID().getText();
        System.out.println(ctx.expression().getText());
        String expr_type = visit(ctx.expression()).toString();
        System.out.println(expr_type);
        String key = "decl_type";
        //check if id is declared
        if(is_declared(id)){
           //check the delcare type is var
           HashMap<String,String> map = scope.peek().get(id);
           String dtype = map.get(key);
           if (dtype.equals("var")){
               //check id and expr types
               key = "type";
               String type = map.get(key);
               if(assign_type_check(expr_type, type)){
                   //set id as accessed
                   key = "accessed";
                   map.replace(key, "t");
               }else{
                   //err unmatched
                   System.out.println("cant assign xtype to ytype");
               }
           }else{
               //error,cant change constant
               System.out.println("can't change constant");
           }
        }else{
            //error undeclared id
            System.out.println("undeclared");
        }
        return "";
    }

    @Override
    public String visitArgstm(calParser.ArgstmContext ctx){
        String id = ctx.ID().getText();
        //check if given id is declared
        String[] arg_li = ctx.arg_list().getText().split(",");
        if(function_call(arg_li, id)){
            return scope.peek().get(id).get("type");
        }
        return "null";
    }

    @Override
    public String visitBEstm(calParser.BEstmContext ctx){
        visit(ctx.statement_block());
        return "state block";
    }

    @Override
    public String visitIfstm(calParser.IfstmContext ctx){
        visit(ctx.condition());
        visit(ctx.statement_block(0));
        visit(ctx.statement_block(1));
        return "if statement";
    }

    @Override
    public String visitLoopstm(calParser.LoopstmContext ctx){
        visit(ctx.condition());
        visit(ctx.statement_block());
        return "loop";
    }


    //deals with return the type/function type 
    @Override
    public String visitFunc_ret_void(calParser.Func_ret_voidContext ctx){
        return "void";
    }
    
    @Override 
    public String visitFunc_ret_other_type(calParser.Func_ret_other_typeContext ctx){
        String type = visit(ctx.type()).toString();
        return type;
    }
    
    @Override 
    public String visitType_bool(calParser.Type_boolContext ctx){
        return "boolean";
    }

    @Override 
    public String visitType_int(calParser.Type_intContext ctx){
        return "integer";
    }



    @Override
    public String visitNempArgListID(calParser.NempArgListIDContext ctx){
        String id = ctx.ID().getText();
        if(!is_declared(id)){
            //declare undeclared error
            return "";
        }
        return id;
    }

    @Override 
    public String[] visitNempArgListIDs(calParser.NempArgListIDsContext ctx){
        //get list of identifiers
        ArrayList<Object> id_li = new ArrayList<Object>();   
        //get the firts ID
        id_li.add(ctx.ID().getText());
        String str = ctx.nemp_arg_list().getText();
        //split rest of the ids
        String[] strli = str.split(",");
        for(String id:strli){
            id_li.add(id);
        }
        
        String[] ret_li = id_li.toArray(new String[id_li.size()]);
        //check if each id is deaclared
        for(String x:ret_li){
            if(!(is_declared(x))){
                System.out.println(x);
                //error undeclared type
            }
        }
        return ret_li;
    }

    
    //expression block
    @Override
    public String visitBinopExpr(calParser.BinopExprContext ctx){
        //check if fragment types are int since its a binop
        String lfrag = visit(ctx.frag(0)).toString();
        String rfrag = visit(ctx.frag(1)).toString();
        if (!(lfrag.equals("integer")) || !(rfrag.equals("integer"))){
            //error
            System.out.println("Binop");
        }
        return "integer";
    }

    @Override
    public String visitBracketExpr(calParser.BracketExprContext ctx){
        /*accessing the expression context will return
        the type of the terminal node*/
        String type = visit(ctx.expression()).toString();
        return type;
    }

    @Override
    public String visitInvokdedid_expr(calParser.Invokdedid_exprContext ctx){
        String id = ctx.ID().getText();
        //check if given id is declared
        String[] arg_li = ctx.arg_list().getText().split(",");
        if(function_call(arg_li, id)){
            return scope.peek().get(id).get("type");
        }
        
        return "null";
        
    }
    //used by visitInvoked_id & visitArgstm
    public Boolean function_call(String[] arg_li,String id){
        if(is_declared(id)){
            //check if arg length is the same as param length
            
            
            String[] func_param = scope.peek().get(id).get("param_list").split(",");
            if(arg_li.length != func_param.length){
                //err
                System.out.println("not same length param,given arg");
                return false;
            }
            
            /*each item in func param is of form id : type
            each item in arg_li is of form id
            access each id from arg_li and get the type
            check if each type corresponds to the given parameter
            */
            
            for(int i=0;i<func_param.length;i++){
                String param_type = func_param[i].split(":")[1];
                String id_key = arg_li[i];
                String arg_type = scope.peek().get(id_key).get("type");
                if(!param_type.equals(arg_type)){
                    System.out.println(param_type+arg_type);
                    System.out.println("unmatched type");
                    return false;
                    //raise err
                }
            }
        }else{
            //err
            System.out.println("no function called id");
            return false;
        }
        return true;

    }
    
    @Override
    public String visitFragBranch(calParser.FragBranchContext ctx){
        /*frag branches will return terminal types
        as a String */
        String type = visit(ctx.frag()).toString();
        return type;
    }
    //frag block
    @Override
    public String visitFrag_id(calParser.Frag_idContext ctx){
        String id = ctx.ID().getText();
        //check if id is declared
        if (is_declared(id)){
            return scope.peek().get(id).get("type");
        }else{
            //create error type, addErrorNode
            return id;
        }
    }
    @Override
    public String visitFrag_negid(calParser.Frag_negidContext ctx){
        String id = ctx.ID().getText();
        if (is_declared(id)){
            return scope.peek().get(id).get("type");
        }else{
            //declare error
            return id;
        }
    }

    @Override
    public String visitFrag_num(calParser.Frag_numContext ctx){
        String num = ctx.NUMBER().getText();
        return "integer";
    }

    @Override
    public String visitFrag_BV(calParser.Frag_BVContext ctx){
        String bv = ctx.BV().getText();
        return "boolean";
    }

    @Override
    public String visitFrag_expr_recur(calParser.Frag_expr_recurContext ctx){
        //should always return an int type since its calling fragprime unless fragprime returns null
        //visit(ctx.expression());
        String type = visit(ctx.frag_prime()).toString();
        if (type.equals("integer")){
            return "integer";
        }else{
            return type;
        }
    }

    @Override
    public String visitFrag_invokedid_recur(calParser.Frag_invokedid_recurContext ctx){
        /*check id
        id should be of function declare type
        should have a return type of int
        */
        
        String id = ctx.ID().getText();
        if(!(is_declared(id))){
            //not declared error
            return "";
        }
        visit(ctx.arg_list());
        visit(ctx.frag_prime());
        return "integer";
    }

    @Override
    public String visitFrag_id_recur(calParser.Frag_id_recurContext ctx){
        String id = ctx.ID().getText();
        if(!(is_declared(id))){
            //declare error
            return id;
        }
        String type = visit(ctx.frag_prime()).toString();

        return type;
    } 

    @Override
    public String visitFrag_neg_id_recur(calParser.Frag_neg_id_recurContext ctx){
        String id = ctx.ID().getText();
        if(!(is_declared(id))){
            //declare error
            return id;
        }
        visit(ctx.frag_prime());
        String type = scope.peek().get(id).get("type");

        return type;
    }

    @Override
    public String visitFrag_num_recur(calParser.Frag_num_recurContext ctx){
        String num = ctx.NUMBER().getText();
        String type = visit(ctx.frag_prime()).toString();
        if (!(type.equals("integer"))){
            return "";
            //error
        }
        return "integer";
    }

    @Override
    public String visitFrag_BV_recur(calParser.Frag_BV_recurContext ctx){
        String bv = ctx.BV().getText();
        String type = visit(ctx.frag_prime()).toString();
        if (!type.equals("null")){
            System.out.println("frag_bv_recur_error");
            //set an error
        }
        return "boolean";

    }

    //frag_prime block
    @Override
    public String visitFragPrime_branch1(calParser.FragPrime_branch1Context ctx){
        String fragctx = visit(ctx.frag()).toString();
        if (!fragctx.equals("integer")){
            //error
            System.out.println("fprime_branch1");
        }
        visit(ctx.frag_prime());
        return "integer";
    } 

    @Override
    public String visitFragPrime_branch2(calParser.FragPrime_branch2Context ctx){
        String fragctx = visit(ctx.frag()).toString();
        return "integer";
    }

    @Override
    public String visitFragPrime_empty(calParser.FragPrime_emptyContext ctx){
        return "null";
    } 

    //condition block
    //all conditions should return type bool
    @Override
    public String visitNeg_cond(calParser.Neg_condContext ctx){
        /*condition always results to a boolean
        ergo Neg condition is always the opposite value
        of the current condition value
        String cond_value = visit(ctx.condition()).toString();
        char c = cond_value.charAt(0);
        String neg_cond_value = (c =='t' || c == 'T') ? "false" : "true";
        return neg_cond_value;*/
        return "boolean";
    }

    @Override
    public String visitBracket_cond(calParser.Bracket_condContext ctx){
        return "boolean";
    }

    @Override
    public String visitComp_op_cond(calParser.Comp_op_condContext ctx){
        String type0 = visit(ctx.expression(0)).toString();
        String type1 = visit(ctx.expression(1)).toString();
        if (ctx.comp_op().equals("integer")){
            if (!type0.equals("integer") || !type1.equals("integer")){
                //err
                System.out.println("comp_op err");
            }
        }        
        /*check if left type = right type
        if matching type, return the context 
        otherwise raise an error*/
        return "boolean";
    }

    @Override
    public String visitAndOr_cond(calParser.AndOr_condContext ctx){
        ctx.condition(0);
        ctx.condition(1);
        return "boolean";
    }
    
    //comp_op
    @Override
    public String visitLess_op(calParser.Less_opContext ctx){
        return "integer";
    }
    
    @Override
    public String visitLessEq_op(calParser.LessEq_opContext ctx){
        return "integer";
    }

    @Override
    public String visitGreat_op(calParser.Great_opContext ctx){
        return "integer";
    }

    @Override
    public String visitGreatEq_op(calParser.GreatEq_opContext ctx){
        return "integer";
    }

    

    
    

}