package java2021.screen;

import java2021.asciiPanel.*;

public class ConnectionFailureScreen extends RestartScreen {
    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("Connection failed.", 10, 7);
    }
}
