// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.data;

import edu.rice.cs.hpcdata.util.Util;

public class UtilTest {
	static public void main(String argv[]) {
		
		System.out.println("Display: " +  System.getenv("DISPLAY"));
		System.out.println("Display correct: " + Util.isCorrectDisplay());
	}

}
