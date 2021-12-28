package java2021;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.*;

import java2021.screen.OnlinePlayScreen;

import org.junit.*;

import static org.junit.Assert.*;

public class TestOnlineScreen {
    @Test
    public void testTwoBytes2List() throws Exception {
        ConcurrentLinkedDeque<int[]> res = new ConcurrentLinkedDeque<>();
        res.add(new int[]{1, 2});
        res.add(new int[]{3, 4});
        res.add(new int[]{5, 6});
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 1; i <= 6; ++i)
            baos.write(i);
        byte[] inputArray = baos.toByteArray();
        ConcurrentLinkedDeque<int[]> output = OnlinePlayScreen.twoBytes2List(inputArray);
        assertEquals(res.size(), output.size());
        Iterator<int[]> iter1 = res.iterator();
        Iterator<int[]> iter2 = output.iterator();
        while (iter1.hasNext()) {
            assertArrayEquals(iter1.next(), iter2.next());
        }
    }

    @Test
    public void testThreeBytes2List() throws Exception {
        ConcurrentLinkedDeque<int[]> res = new ConcurrentLinkedDeque<>();
        res.add(new int[]{1, 2, 3});
        res.add(new int[]{4, 5, 6});
        res.add(new int[]{7, 8, 9});
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 1; i <= 9; ++i)
            baos.write(i);
        byte[] inputArray = baos.toByteArray();
        ConcurrentLinkedDeque<int[]> output = OnlinePlayScreen.threeBytes2List(inputArray);
        assertEquals(res.size(), output.size());
        Iterator<int[]> iter1 = res.iterator();
        Iterator<int[]> iter2 = output.iterator();
        while (iter1.hasNext()) {
            assertArrayEquals(iter1.next(), iter2.next());
        }
    }

    @Test
    public void testGetScrollX() throws Exception {

    }

    @Test
    public void testGetScrollY() throws Exception {

    }

    @Test
    public void testCanSee() throws Exception {
        OnlinePlayScreen screen = new OnlinePlayScreen("", 0);
        screen.xx = 0;
        screen.yy = 0;
        assertFalse(screen.canSee(66, 66));
        assertTrue(screen.canSee(2, 2));
        screen.myCurAI = 2;
        assertTrue(screen.canSee(66, 66));
        assertTrue(screen.canSee(2, 2));
    }


}
