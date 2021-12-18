package java2021.screen;

import java2021.asciiPanel.AsciiPanel;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;

public class LoadScreen implements Screen {

    private List<String> lines = new ArrayList<>();
    private int upBound = 9;
    private int leftBound = 11;
    private StartScreen ss;

    public LoadScreen(StartScreen ss) {
        this.ss = ss;
        try {
            FileInputStream f = new FileInputStream("./texts/load.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(f));
            String lineText = null;
            while ((lineText = br.readLine()) != null)
                lines.add(lineText);
            br.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        terminal.write('|', 35, upBound - 2, Color.red);
        terminal.write('|', 45, upBound - 2, Color.red);
        terminal.write("Load Game", 36, upBound - 2, Color.GREEN);
        for (int i = 0; i < lines.size(); ++i)
            terminal.write(lines.get(i), leftBound, upBound + i);
    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            return this.ss;
        default:
            return this;
        }
    }
}