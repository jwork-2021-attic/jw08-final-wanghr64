package java2021.screen;

import java2021.asciiPanel.AsciiPanel;

public class OtherWinScreen extends RestartScreen {

    private int wonIndex;

    public OtherWinScreen(int index) {
        super();
        wonIndex = index;
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("Player" + wonIndex + " has won!", 11, 7);
    }

}
