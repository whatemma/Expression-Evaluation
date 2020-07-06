package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";
			
    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	/** DO NOT create new vars and arrays - they are already created before being sent in
    	 ** to this method - you just need to fill them in.
    	 **/
    	//(a + A[a*2-b])
    	//a - (b+A[B[2]])*d + 3
    	
    	expr = expr.replaceAll("\\s+","");
    	
    	String temp = "";
    	
    	for(int i = 0; i < expr.length(); i++){
    		
    		if(Character.isLetter(expr.charAt(i))){
    			temp = temp + expr.charAt(i);
    		}
    		if(expr.charAt(i) == '['){
    			if(!temp.isEmpty()){
	    			Array array = new Array(temp);
	    			if(!arrays.contains(array)){
	    				arrays.add(array);
	    			}
	    			temp = "";
    			}
    		}
    		if(!Character.isLetter(expr.charAt(i)) && expr.charAt(i) != '['){
    			if(!temp.isEmpty()){
    				Variable variable = new Variable(temp);
    				if(!vars.contains(variable)){
    					vars.add(variable);
    				}
    				temp = "";
    			}
    		}
    		if(i == expr.length()-1 && Character.isLetter(expr.charAt(i))){
    			Variable variable = new Variable(temp);
    			if(!vars.contains(variable)){
    				vars.add(variable);
    			}
    		}
    	}

    	System.out.println("array: " + arrays.toString());
    	System.out.println("variables: " + vars.toString());
    	
    	
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
    	/** COMPLETE THIS METHOD **/
    	// following line just a placeholder for compilation
    	
    	return parse(expr, vars, arrays);
    	
    }
    
    private static float parse(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays){
    	
    	expr = expr.replaceAll("\\s+","");
    	
    	Stack<Float> numbers = new Stack<Float>();
    	Stack<Character> operators = new Stack<Character>();
    	String temp = "";
    	
    	for(int i = 0; i < expr.length(); i++){
    		
    		if(Character.isLetterOrDigit(expr.charAt(i))){
        		temp += expr.charAt(i);
    		}
    		else if(expr.charAt(i) == '+' || expr.charAt(i) == '-' || expr.charAt(i) == '*' || expr.charAt(i) == '/'){
    			operators.push(expr.charAt(i));
    			if(!temp.isEmpty()){
    				if(isNum(temp)){
    					numbers.push(Float.parseFloat(temp));
    					temp = "";
    				}else if(isVar(temp, vars)){
    					numbers.push(getVar(temp, vars));
    					temp = "";
    				}
    			}
    		}
    		else if(expr.charAt(i) == '('){
    			int endb = findBrackets(expr, i);
    			float recurse = parse(expr.substring(i+1, endb), vars, arrays);
    			numbers.push(recurse);
    			i = endb;
    		}
    		else if(expr.charAt(i) == '['){
    			int endb = findBrackets(expr, i);
    			int index = (int) parse(expr.substring(i+1, endb), vars, arrays);
    			float num = getArr(temp, index, arrays);
    			numbers.push(num);
    			temp = "";
    			i = endb;

    		}
    		
    	}
    	if(!temp.isEmpty()){
    		if(isNum(temp)){
				numbers.push(Float.parseFloat(temp));
			}else if(isVar(temp, vars)){
				numbers.push(getVar(temp, vars));
			}
    	}
    	
    	Float result = calc(expr, numbers, operators);
    	
    	
    	return result;
    }
    
    private static int findBrackets(String expr, int open){
    	Stack<Character> sub = new Stack<Character>();
    	for(int i = open; i < expr.length(); i++){
    		if(expr.charAt(i) == '(' || expr.charAt(i) == '['){
        		sub.push(expr.charAt(i));
        	}
    		else if(expr.charAt(i) == ')' || expr.charAt(i) == ']'){
    			sub.pop();
    			if(sub.isEmpty()){
    				return i;
    			}
    		}
    	}
    	
    	return 0;
    }
    
    private static float calc(String expr, Stack<Float> numbers, Stack<Character> operators){
    	Stack<Float> renum = new Stack<Float>();
    	Stack<Character> reoper = new Stack<Character>();
    	while(!numbers.isEmpty()){
    		renum.push(numbers.pop());
    	}
    	while(!operators.isEmpty()){
    		reoper.push(operators.pop());
    	}
    	
    	while(renum.size() > 1 && reoper.size() != 0){
    		if(reoper.peek() == '*'){
    			float temp = renum.pop();
    			float temp2 = renum.pop();
    			renum.push(temp * temp2);
    			reoper.pop();
    		}
    		else if(reoper.peek() == '/'){
    			float temp = renum.pop();
    			float temp2 = renum.pop();
    			renum.push(temp / temp2);
    			reoper.pop();
    		}
    		else if(reoper.peek() == '+'){
    			float temp = renum.pop();
    			char oper = reoper.pop();
    			if(reoper.isEmpty() || reoper.peek() == '+' || reoper.peek() == '-'){
    				float temp2 = renum.pop();
    				renum.push(temp + temp2);
    			}
    			else if(reoper.peek() == '/'){
    				float first = renum.pop();
    				float second = renum.pop();
    				renum.push(first/second);
    				renum.push(temp);
    				reoper.pop();
    				reoper.push(oper);
    			}
    			else if(reoper.peek() == '*'){
    				float first = renum.pop();
    				float second = renum.pop();
    				renum.push(first*second);
    				renum.push(temp);
    				reoper.pop();
    				reoper.push(oper);
    			}
    		}
    		else if(reoper.peek() == '-'){
    			float temp = renum.pop();
    			char oper = reoper.pop();
    			
    			if(reoper.isEmpty() || reoper.peek() == '+' || reoper.peek() == '-'){
    				float temp2 = renum.pop();
    				renum.push(temp - temp2);
    			}
    			else if(reoper.peek() == '/'){
    				float first = renum.pop();
    				float second = renum.pop();
    				renum.push(first/second);
    				renum.push(temp);
    				reoper.pop();
    				reoper.push(oper);
    			}
    			else if(reoper.peek() == '*'){
    				float first = renum.pop();
    				float second = renum.pop();
    				renum.push(first*second);
    				renum.push(temp);
    				reoper.pop();
    				reoper.push(oper);
    			}
    		}
    	}
    	return renum.pop();
    }
    
    private static Float getVar(String variable, ArrayList<Variable> vars){
    	for(int i = 0; i < vars.size(); i++){
    		if(vars.get(i).name.equals(variable)){
    			return (float) vars.get(i).value;
    		}
    	}
    	return (float) 0;
    }
    
    private static Boolean isVar(String variable, ArrayList<Variable> vars){
    	for(int i = 0; i< vars.size(); i++){
    		if(vars.get(i).name.equals(variable)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private static Float getArr(String variable, int index, ArrayList<Array> arrays){
    	
    	for(int i = 0; i < arrays.size(); i++){
    		if(arrays.get(i).name.equals(variable)){
    			return (float) arrays.get(i).values[index];
    		}
    	}
    	return (float) 0; 
    }
    
    
    
    private static Boolean isNum(String variable){
    	try{  
			Float.parseFloat(variable);  
			return true;  
		}  
		catch( Exception e ){  
			return false;  
		}  
    }
}