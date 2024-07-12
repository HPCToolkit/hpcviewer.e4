// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctraceviewer.data.color;

import java.util.Random;

import org.eclipse.swt.graphics.RGB;

public class RandomColorGenerator implements IColorGenerator 
{
	static private final int COLOR_MIN = 16;
	static private final int COLOR_MAX = 210 - COLOR_MIN;
	static private final long RANDOM_SEED = 612543231L;
	final private Random random_generator;

	public RandomColorGenerator() {
		
		// rework the color assignment to use a single random number stream
		random_generator = new Random((long)RANDOM_SEED);
	}
	
	
	@Override
	public RGB createColor(String procedureName) {
		
		return new RGB(	COLOR_MIN + random_generator.nextInt(COLOR_MAX), 
						COLOR_MIN + random_generator.nextInt(COLOR_MAX), 
						COLOR_MIN + random_generator.nextInt(COLOR_MAX));
	}
}
