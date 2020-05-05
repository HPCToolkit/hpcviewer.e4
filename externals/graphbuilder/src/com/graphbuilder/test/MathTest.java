/**
 * 
 */
package com.graphbuilder.test;

import com.graphbuilder.math.Expression;
import com.graphbuilder.math.ExpressionTree;
import com.graphbuilder.math.FuncMap;
import com.graphbuilder.math.VarMap;

public class MathTest {

	public static void main(String[] args) {
		String s = "&pi*r^2";
		Expression x = ExpressionTree.parse(s);

		VarMap vm = new VarMap(false /* case sensitive */);
		vm.setValue("&pi", Math.PI);
		vm.setValue("r", 5);

		FuncMap fm = null; // no functions in expression

		System.out.println(x); // (pi*(r^2))
		System.out.println(x.eval(vm, fm)); // 78.53981633974483

		vm.setValue("r", 10);
		System.out.println(x.eval(vm, fm)); // 314.1592653589793
		
		x = ExpressionTree.parse("4==1");
		final double r = x.eval(vm, fm);
		System.out.println("4 == 1 --> " + r);

		x = ExpressionTree.parse("4==4");
		System.out.println("4 == 4 --> " + x.eval(vm, fm));

		x = ExpressionTree.parse("r==5");
		System.out.println("r<-10; r == 5 --> " + x.eval(vm, fm));

		x = ExpressionTree.parse("r==10");
		System.out.println("r<-10; r == 10 --> " + x.eval(vm, fm));
		
		fm = new FuncMap();
		fm.loadDefaultFunctions();
		
		x = ExpressionTree.parse("if(r==10,10.0,-10.0)");
		System.out.println("if(r==10,10.0,-10.0) --> " + x.eval(vm, fm));

		x = ExpressionTree.parse("if(r==1,10.0,-10.0)");
		System.out.println("if(r==1,10.0,-10.0) --> " + x.eval(vm, fm));
	}
}