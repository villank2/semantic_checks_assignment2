public class SemanticError {
    

    public String undeclaredVar(String id){
        return "Undeclared variable : "+id;
    }

    public String already_declared(String id){
        return "Cannot redeclare variable : " + id;  
    }

    public String assigntypeErr(String typex, String typey){
        return "Cannot assign value of type " + typey + " to variable of type " + typex; 
    }

    public String optypeErr(String typex,String typey){
        return "Cannot perform operation on " + typex + " and " + typey;
    }

    public String optypeErr(String typex){
        return "Cannot perform operation on " + typex;
    }

    public String unAssigned(String[] vars){
        String str = "";
        for(String id:vars){
            str = str + id + " ";
        }
        return "Variable not assigned : " + str;
    }

    public String notused(String[] ids){
        String str  = "";
        for (String id : ids){
            str = str + id + " ";
        }
        return "Unused variables/functions: " + str;
    }

    public String changeConstant(String id){
        return "Cannot change constant : " + id;
    }
}
