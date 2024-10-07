package edu.neu.coe.info6205.sort.elementary;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class InsertionSortBasicTest {

    @Test
    public void testSortFull1() {
        String[] words = new String[]{"Dog", "Cat", "ferret", "Aardvark", "Fox", "Bat"};
        String[] expectedNormal = new String[]{"Aardvark", "Bat", "Cat", "Dog", "Fox", "ferret"};
        InsertionSortBasic<String> sorter = InsertionSortBasic.create();
        sorter.sort(words);
        assertArrayEquals(expectedNormal, words);
    }

    @Test
    public void testSortFull2() {
        String[] words = new String[]{"Dog", "Cat", "ferret", "Aardvark", "Fox", "Bat"};
        String[] expectedIgnoreCase = new String[]{"Aardvark", "Bat", "Cat", "Dog", "ferret", "Fox"};
        InsertionSortBasic<String> sorter = new InsertionSortBasic<>(String.CASE_INSENSITIVE_ORDER);
        sorter.sort(words);
        assertArrayEquals(expectedIgnoreCase, words);
    }

    @Test
    public void testSortFull3() {
        String[] words = new String[]{"Dog", "Cat", "ferret", "Aardvark", "Fox", "Bat"};
        String[] expectedIgnoreCase = new String[]{"Fox", "ferret", "Dog", "Cat", "Bat", "Aardvark"};
        InsertionSortBasic<String> sorter = new InsertionSortBasic<>(String.CASE_INSENSITIVE_ORDER.reversed());
        sorter.sort(words);
        assertArrayEquals(expectedIgnoreCase, words);
    }

    @Test
    public void testSortPartition() {
        String[] words = new String[]{"Dog", "Cat", "ferret", "Aardvark", "Fox", "Bat"};
        String[] expectedNormal = new String[]{"Dog", "Cat", "Aardvark", "ferret", "Fox", "Bat"};
        InsertionSortBasic<String> sorter = InsertionSortBasic.create();
        sorter.sort(words, 2, 4);
        System.out.println(words);
        assertArrayEquals(expectedNormal, words);
    }
}