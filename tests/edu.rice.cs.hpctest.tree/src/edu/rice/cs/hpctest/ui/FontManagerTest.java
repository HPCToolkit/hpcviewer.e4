// SPDX-FileCopyrightText: 2024 Contributors to the HPCToolkit Project
//
// SPDX-License-Identifier: BSD-3-Clause

package edu.rice.cs.hpctest.ui;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.rice.cs.hpcsetting.fonts.FontManager;


public class FontManagerTest 
{
	private static Display display;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		display = Display.getDefault();
	}



	@Test
	public void testGetFontGeneric() {
		assertNotNull(FontManager.getFontGeneric() );
	}

	@Test
	public void testGetMetricFont() {
		assertNotNull(FontManager.getMetricFont() );
	}

	@Test
	public void testGetTextEditorFont() {
		assertNotNull(FontManager.getTextEditorFont() );
	}

	@Test
	public void testGetCallsiteGlyphDefaultFont() {
		assertNotNull(FontManager.getCallsiteGlyphDefaultFont() );
	}

	@Test
	public void testGetCallsiteGlyphDefaultFontFontData() {
		Font font = FontManager.getFontGeneric() ;
		Font fontCS = FontManager.getCallsiteGlyphDefaultFont(font.getFontData()[0]);
		assertNotNull(fontCS);
		
		FontData fdBase = font.getFontData()[0];
		FontData fdCS   = fontCS.getFontData()[0];
		assertTrue(fdBase.getHeight() + 2 == fdCS.getHeight());
	}

	@Test
	public void testGetCallsiteGlyphFont() {
		display.syncExec(()-> {
			Font cs = FontManager.getCallsiteGlyphFont() ;
			assertNotNull( cs );
		});
	}

	@Test
	public void testGetFontDataPreference() {
		assertThrows( Exception.class, () -> {
			FontManager.getFontDataPreference(null) ;
		});
	}

	@Test
	public void testSetFontPreference() {
		assertThrows(Exception.class, () -> {
			FontManager.setFontPreference(null, null);
		});
	}

	@Test
	public void testChangeFontHeightInt() {
		int heightGeneric = getHeight(FontManager.getFontGeneric());
		int heightMetric  = getHeight(FontManager.getMetricFont());
		int heightText    = getHeight(FontManager.getTextEditorFont());
		int heightCS      = getHeight(FontManager.getCallsiteGlyphFont());
		
		try {
			FontManager.changeFontHeight(2);
			
			// case of successful:
			int newheightGeneric = getHeight(FontManager.getFontGeneric());
			int newheightMetric  = getHeight(FontManager.getMetricFont());
			int newheightText    = getHeight(FontManager.getTextEditorFont());
			int newheightCS      = getHeight(FontManager.getCallsiteGlyphFont());
			
			assertTrue(newheightGeneric == heightGeneric + 2);
			assertTrue(newheightMetric == heightMetric + 2);
			assertTrue(newheightText == heightText + 2);
			assertTrue(newheightCS == heightCS + 2);
		} catch (Exception e) {
			// case of exception:
			int newheightGeneric = getHeight(FontManager.getFontGeneric());
			int newheightMetric  = getHeight(FontManager.getMetricFont());
			int newheightText    = getHeight(FontManager.getTextEditorFont());
			int newheightCS      = getHeight(FontManager.getCallsiteGlyphFont());
			
			assertTrue(newheightGeneric == heightGeneric);
			assertTrue(newheightMetric == heightMetric);
			assertTrue(newheightText == heightText);
			assertTrue(newheightCS == heightCS);
		}
	}
	
	private int getHeight(Font font) {
		FontData fd = font.getFontData()[0];
		return fd.getHeight();
	}
}
