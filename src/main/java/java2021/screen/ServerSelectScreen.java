package java2021.screen;

import java.awt.event.KeyEvent;
import java.util.*;

import java2021.asciiPanel.AsciiPanel;

public class ServerSelectScreen implements Screen {

    private StringBuilder host;
    private StringBuilder port;
    private boolean isLegal;
    private boolean inputMode;// false for host, true for port

    public ServerSelectScreen() {
        host = new StringBuilder();
        port = new StringBuilder();
        inputMode = false;
        isLegal = false;
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("Shift to switch, Enter to Start", 5, 5);
        terminal.write("Host", 7, 7);
        terminal.write("Port", 25, 7);
        if (!inputMode)
            terminal.write("_", 7 + host.length(), 8);
        else
            terminal.write("_", 25 + port.length(), 8);
        isLegal = host.length() > 0 && port.length() > 0 && Integer.parseInt(port.toString()) > 0
                && Integer.parseInt(port.toString()) < 65535;
        if (isLegal) {
            terminal.write(host.toString(), 7, 8);
            terminal.write(port.toString(), 25, 8);
        } else {
            terminal.write(host.toString(), 7, 8, AsciiPanel.brightRed);
            terminal.write(port.toString(), 25, 8, AsciiPanel.brightRed);
        }
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_SHIFT) {
            inputMode = !inputMode;
        } else if (key.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!inputMode)
                host.deleteCharAt(host.length() - 1);
            else
                port.deleteCharAt(port.length() - 1);
        } else {
            char keyChar = key.getKeyChar();
            if (!inputMode)
                host.append(keyChar);
            else {
                if (port.length() <= 5 && Character.isDigit(keyChar))
                    port.append(keyChar);
            }
        }
        return this;
    }

}
