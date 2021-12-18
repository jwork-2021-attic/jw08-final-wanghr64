/*
 * Copyright (C) 2015 Aeranythe Echosong
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package java2021.screen;

import java2021.asciiPanel.AsciiPanel;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aeranythe Echosong
 */
public class StartScreen extends RestartScreen {

    private int selected = 0;

    private List<String> lines = new ArrayList<>();
    private int upBound = 3;

    private HelpScreen hs = new HelpScreen(this);

    public StartScreen() {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(StartScreen.class.getClassLoader().getResourceAsStream("texts/start.txt")));
            String lineText = null;
            while ((lineText = br.readLine()) != null)
                lines.add(lineText);
            br.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void displayOutput(AsciiPanel terminal) {
        try {
            for (int i = 0; i < lines.size(); ++i)
                terminal.write(lines.get(i), 2, upBound + i, AsciiPanel.white, AsciiPanel.black);
            terminal.write((char) 33, 11, 12 + selected, Color.MAGENTA);
        } catch (Exception e) {
            terminal.write("File not found.", 2, 10);
        }

    }

    @Override
    public Screen respondToUserInput(KeyEvent key) {
        switch (key.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                switch (this.selected) {
                    case 0:
                        return new PlayScreen();
                    case 1:
                        return new LoadScreen(this);
                    case 2:
                        return hs;
                }
            case KeyEvent.VK_DOWN:
                selected = (selected + 1) % 3;
                return this;
            case KeyEvent.VK_UP:
                selected = (selected - 1) % 3;
                return this;
            default:
                return this;
        }
    }

}
