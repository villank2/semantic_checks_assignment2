import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Collection;


import java.util.LinkedList;

public class EvalVisitor extends calBaseVisitor<Object>{
    Map<String, HashMap<String,String>> table = new HashMap<String,HashMap<String,String>>();
    Scanner scan = new Scanner(System.in);


    public HashMap<String,String> init_attributes(String type,String decl_type){
        HashMap<String,String> id_attributes = new HashMap<String,String>();
        String loctype = type;
        String locdecl_type = decl_type;
        String value = "Null";
        id_attributes.put("type",loctype);
        id_attributes.put("decl_type",locdecl_type);
        id_attributes.put("value",value);
        return id_attributes;
    }

    public Boolean is_declared(String id){
        Boolean ret = table.containsKey(id);
        return ret;
    }

    //get the declate type of an identifier ie constant or variable
    public String check_decl_type(String id){
        try{
            if (is_declared(id)){
                return table.get(id).get("decl_type");
            }
        }catch(Exception e){
            System.out.println(e);
        }
        //make not declared error object
        return "Not Declared";
    }

    public Boolean assign_type_check(String val, String type){
        //check if the value being assigned is of the same type
        //grab first char and checl its a digit
        if (type.equals("int")){
            char c = val.charAt(0);
            return Character.isDigit(c); 
        }else if(type.equals("bool")){
        //check if its a boolean value
            return (val.toLowerCase().equals("true") || val.toLowerCase().equals("false"));
        }
        //given value is of unknown type
        //IMPROVE LATER for functions
        return false;
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
        String type = ctx.type().getText();
        HashMap<String,String> attr = init_attributes(type, "var");
        table.put(id,attr);
        return id;
    }


    @Override
    public String visitConst_decl(calParser.Const_declContext ctx){
        String id = ctx.ID().getText();
        //check for already existing id
        if (is_declared(id)){
            //make error object
            return "ID already declared";
        }
        String value = ctx.expression().getText();//visit(ctx.expression()).toString();
        
        String type = visit(ctx.type()).toString();
        
        HashMap<String,String> attr = init_attributes(type, "const");
        if (assign_type_check(value, type)){
            attr.replace("value", value);
        }else{
            System.out.println("Unmatched type");
        }
        
        return id;
    }
    //statement_block
    /*@Override
    public String visitStatement_block(calParser.Statement_blockContext ctx){
        String stm = visit(ctx.statement());
        String blck = ctx.statement_block().toString();
        System.out.println(stm);
        System.out.println(blck);
        return "";
    }*/
    
    // statement branches
    @Override 
    public String visitAssignstm(calParser.AssignstmContext ctx){
        String id = ctx.ID().getText();
        String value = ctx.expression().toString();
        //check if ID is declared
        if (is_declared(id)){
            //check if declare type is changeable
            String decl_type = check_decl_type(id);
            if (decl_type.equals("var")){
                //access table -> attributes and change value
                //check if value to be assigned is correct type
                table.get(id).replace("value", value);
            }else{
                System.out.println("Can't change const type");
            }
        }else{
            System.out.println("variable not declared");
        }
        return id;
    }

    @Override
    public String visitArgstm(calParser.ArgstmContext ctx){
        String id = ctx.ID().getText();
        //check if given id is declared
        if(is_declared(id)){
            String id_li = visit(ctx.arg_list()).toString();
            //check if each ID exists
        }
        return "";
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
        return "bool";
    }

    @Override 
    public String visitType_int(calParser.Type_intContext ctx){
        return "int";
    }



    @Override
    public String visitNempArgListID(calParser.NempArgListIDContext ctx){
        String id = ctx.ID().getText();
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
        /*for(String x:ret_li){
            System.out.println(x);
        }*/
        return ret_li;
    }

    //comp_op block
    //condition block
   // @Override
    public String visitNeg_cond(calParser.Neg_condContext ctx){
        /*condition always results to a boolean
        ergo Neg condition is always the opposite value
        of the current condition value*/
        String cond_value = visit(ctx.condition()).toString();
        char c = cond_value.charAt(0);
        String neg_cond_value = (c =='t' || c == 'T') ? "false" : "true";
        return neg_cond_value;
    }

    @Override
    public calParser.ConditionContext visitBracket_cond(calParser.Bracket_condContext ctx){
        return ctx.condition();
    }

    @Override
    public calParser.ConditionContext visitComp_op_cond(calParser.Comp_op_condContext ctx){
        
    }
    
     
    

    
    

}