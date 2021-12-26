package java2021.screen;

import java2021.asciiPanel.*;

public class LostConnectionScreen extends RestartScreen {
    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("You or other player", 9, 6);
        terminal.write("just lost connection.", 9, 7);
    }

}
