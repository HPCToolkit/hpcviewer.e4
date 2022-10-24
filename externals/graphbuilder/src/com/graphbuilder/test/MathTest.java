/*
* Copyright (c) 2005, Graph Builder
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* * Redistributions of source code must retain the above copyright notice,
* this list of conditions and the following disclaimer.
*
* * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.
*
* * Neither the name of Graph Builder nor the names of its contributors may be
* used to endorse or promote products derived from this software without
* specific prior written permission.
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

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
