/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.info6205.heapsort;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
/**
 *
 * @author xinyuan
 */

public class HeapSortTest {
    @Test
    public void testSort() throws Exception {
        Integer[] xs = new Integer[4];
        xs[0] = 3;
        xs[1] = 4;
        xs[2] = 2;
        xs[3] = 1;
        Sort<Integer> s = new HeapSort<Integer>();
        Integer[] ys = s.sort(xs);
        assertEquals(Integer.valueOf(1), ys[0]);
        assertEquals(Integer.valueOf(2), ys[1]);
        assertEquals(Integer.valueOf(3), ys[2]);
        assertEquals(Integer.valueOf(4), ys[3]);
    }
    @Test
    public void testSort1() throws Exception {
        Integer[] xs = new Integer[6];
        xs[0] = 3;
        xs[1] = 4;
        xs[2] = 2;
        xs[3] = 1;
        xs[5] = 6;
        xs[4] = 5;
        Sort<Integer> s = new HeapSort<Integer>();
        Integer[] ys = s.sort(xs);
        assertEquals(Integer.valueOf(1), ys[0]);
        assertEquals(Integer.valueOf(2), ys[1]);
        assertEquals(Integer.valueOf(3), ys[2]);
        assertEquals(Integer.valueOf(4), ys[3]);
        assertEquals(Integer.valueOf(5), ys[4]);
        assertEquals(Integer.valueOf(6), ys[5]);
    }
}
