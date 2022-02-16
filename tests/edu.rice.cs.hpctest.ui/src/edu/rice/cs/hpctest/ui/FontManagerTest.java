package edu.rice.cs.hpctest.ui;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.rice.cs.hpcsetting.fonts.FontManager;


class FontManagerTest 
{
	private static Display display;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		display = Display.getDefault();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}


	@Test
	void testGetFontGeneric() {
		assertNotNull(FontManager.getFontGeneric() );
	}

	@Test
	void testGetMetricFont() {
		assertNotNull(FontManager.getMetricFont() );
	}

	@Test
	void testGetTextEditorFont() {
		assertNotNull(FontManager.getTextEditorFont() );
	}

	@Test
	void testGetCallsiteGlyphDefaultFont() {
		assertNotNull(FontManager.getCallsiteGlyphDefaultFont() );
	}

	@Test
	void testGetCallsiteGlyphDefaultFontFontData() {
		Font font = FontManager.getFontGeneric() ;
		Font fontCS = FontManager.getCallsiteGlyphDefaultFont(font.getFontData()[0]);
		assertNotNull(fontCS);
		
		FontData fdBase = font.getFontData()[0];
		FontData fdCS   = fontCS.getFontData()[0];
		assertTrue(fdBase.getHeight() + 2 == fdCS.getHeight());
	}

	@Test
	void testGetCallsiteGlyphFont() {
		display.syncExec(()-> {
			Font cs = FontManager.getCallsiteGlyphFont() ;
			assertNotNull( cs );
		});
	}

	@Test
	void testGetFontDataPreference() {
		assertThrows( Exception.class, () -> {
			FontManager.getFontDataPreference(null) ;
		});
	}

	@Test
	void testSetFontPreference() {
		assertThrows(Exception.class, () -> {
			FontManager.setFontPreference(null, null);
		});
	}

	@Test
	void testChangeFontHeightInt() {
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
