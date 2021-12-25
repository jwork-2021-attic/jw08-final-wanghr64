package java2021.screen;

import java2021.asciiPanel.AsciiPanel;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HelpScreen implements Screen {

    private List<String> lines;
    private int leftBound = 6;
    private int upBound = 6;
    private int downBound = 40;
    private int scroll = 0;
    private boolean isEnd = false;
    private StartScreen ss;

    public HelpScreen(StartScreen ss) {
        this.ss = ss;
        try {
            FileInputStream f = new FileInputStream("./texts/help.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(f));
            lines = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null)
                lines.add(line);
            br.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write("Use UP/DOWN to scroll.", 29, upBound - 2, Color.GREEN);
        if (isEnd)
            terminal.write("(END)", 66, downBound + 1, Color.GREEN);

        for (int i = 0; i < downBound - upBound && i + scroll < lines.size(); ++i) {
            terminal.write(lines.get(i + scroll), leftBound, upBound + i);
        }
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_UP:
                isEnd = false;
                if (scroll > 0)
                    --scroll;
                return this;
            case KeyEvent.VK_DOWN:
                if (scroll < lines.size() - (downBound - upBound))
                    ++scroll;
                else
                    isEnd = true;
                return this;
            case KeyEvent.VK_ESCAPE:
                scroll = 0;
                return this.ss;
            default:
                return this;
        }
    }
}
