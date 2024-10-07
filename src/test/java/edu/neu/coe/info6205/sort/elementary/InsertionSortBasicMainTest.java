package edu.neu.coe.info6205.sort.elementary;

import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

public class InsertionSortBasicMainTest {

    @Test
    public void testRandomArray(){
        InsertionSortBasic<Integer> sorter = InsertionSortBasic.create();
        Integer[] arr = generateRandomArray(100);
        Integer [] sorted = Arrays.copyOf(arr,arr.length);
        Arrays.sort(sorted);
        sorter.sort(arr);
        assertArrayEquals(sorted,arr);
        for(int i =1; i<=15;i++){
            Integer[] finalArr = generateRandomArray(100*i);
            Benchmark<Boolean> bm = new Benchmark_Timer<>(
                    "testRandomArray",null,
                    b -> sorter.sort(finalArr),
                    null);
            double x = bm.run(true, 10);

            System.out.println("Time taken to sort random array of size " + (100*i)+ ":" +x + " ms");
        }
    }

    @Test
    public void testPartiallyOrderedArray(){
        InsertionSortBasic<Integer> sorter = InsertionSortBasic.create();
        Integer[] arr = generatePartiallyOrderedArray(100);
        Integer [] sorted = Arrays.copyOf(arr,arr.length);
        Arrays.sort(sorted);

        sorter.sort(arr);
        assertArrayEquals(sorted,arr);
        for(int i =1; i<=15;i++){
            Integer[] finalArr = generatePartiallyOrderedArray(100*i);
            Benchmark<Boolean> bm = new Benchmark_Timer<>(
                    "testPartiallyOrderedArray",null,
                    b -> sorter.sort(finalArr),
                    null);
            double x = bm.run(true, 10);
            System.out.println(x);
            System.out.println("Time taken to sort partially ordered array of size " + (100*i)+ ":" +x + " ms");
        }

    }
    @Test
    public void testOrderedArray(){
        InsertionSortBasic<Integer> sorter = InsertionSortBasic.create();
        Integer[] arr = generateOrderedArray(100);
        Integer [] sorted = Arrays.copyOf(arr,arr.length);
        Arrays.sort(sorted);
        sorter.sort(arr);
        assertArrayEquals(sorted,arr);
        for(int i =1; i<=15;i++){
            Integer[] finalArr = generateOrderedArray(100*i);
            Benchmark<Boolean> bm = new Benchmark_Timer<>(
                    "testOrderedArray",null,
                    b -> sorter.sort(finalArr),
                    null);
            double x = bm.run(true, 10);
            System.out.println("Time taken to sort ordered array of size " + (100*i)+ ":" +x + " ms");
        }

    }
    @Test
    public void testReverseOrderedArray(){
        InsertionSortBasic<Integer> sorter = InsertionSortBasic.create();
        Integer[] arr = generateReverseOrderedArray(100);
        Integer [] sorted = Arrays.copyOf(arr,arr.length);
        Arrays.sort(sorted);
        sorter.sort(arr);
        assertArrayEquals(sorted,arr);

        for (int i = 1; i <= 15; i++) {
            Integer[] finalArr = generateReverseOrderedArray(100 * i);
            Benchmark<Boolean> bm = new Benchmark_Timer<>(
                    "testReverseOrderedArray", null,
                    b -> sorter.sort(finalArr),
                    null);
            double x = bm.run(true, 10);
            System.out.println("Time taken to sort reverse ordered array of size " + (100 * i) + ":" + x + " ms");
        }

    }

    private Integer[] generateRandomArray(int size){
        Integer[] arr = new Integer[size];
        Random random = new Random();
        for(int i = 0; i < size; i++){
            arr[i] = random.nextInt(size);
        }
        return arr;
    }

    private  Integer[] generatePartiallyOrderedArray(int size){
        Random random = new Random();
        Integer[] arr = new Integer[size];
        for(int i=0;i<size/2;i++){
            arr[i] = i;
        }
        for(int i =size/2;i<size;i++){
            arr[i] = random.nextInt(size/2,size);
        }
        return arr;
    }

    private Integer[] generateOrderedArray(int size){
        Integer[] arr = new Integer[size];
        for(int i = 0; i < size; i++){
            arr[i] = i;
        }
        return arr;
    }

    private Integer[] generateReverseOrderedArray(int size){
        Integer[] arr = new Integer[size];
        for(int i = 0; i < size; i++){
            arr[i] = size - i;
        }
        return arr;
    }
}
