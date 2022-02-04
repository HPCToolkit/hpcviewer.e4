package edu.rice.cs.hpcviewer.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.rice.cs.hpcdata.util.OSValidator;

public class ExitHandlerTest {

    private static SWTBot bot;

    @BeforeEach
    public void beforeClass() throws Exception {
        // don't use SWTWorkbenchBot here which relies on Platform 3.x
        bot = new SWTBot();
    }

    @Test
    public void executeExit() {
        SWTBotMenu fileMenu = bot.menu("File");
        assertNotNull(fileMenu);
        SWTBotMenu openMenu = fileMenu.menu("Open database");
        assertNotNull(openMenu);
        openMenu.click();

        if (OSValidator.isMac()) {
        	SWTBotMenu mainMenu = bot.menu("hpcviewer");
        	assertNotNull(mainMenu);
        	
        	SWTBotMenu quitMenu = bot.menu("Quit hpcviewer");
        	assertNotNull(quitMenu);
        	quitMenu.click();
            assertTrue(true);
        	return;
        }
        SWTBotMenu exitMenu = fileMenu.menu("Exit");
        assertNotNull(exitMenu);
        exitMenu.click();
        assertTrue(true);
    }



//  @AfterEach
//  public void sleep() {
//      bot.sleep(2000);
//  }
}