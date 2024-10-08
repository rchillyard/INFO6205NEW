package edu.neu.coe.info6205;

import scala.util.Random;

public class Assignment3_Part3 {
    //test bubble sort
    public static void bubbleSort(int[] arr){
        int n = arr.length;
        for (int i = 0; i < n-1; i++){
            for (int j = 0; j < n-i-1; j++){
                if (arr[j] > arr[j+1]){
                   // swap(arr,j,j+1);
                   int temp = arr[j];
                   arr[j] = arr[j+1];
                   arr[j+1] = temp;
                }
            }
        }
    }

    // four different array ordering situations
    // random array
    public static void main(String[] args) {
    int[] sizes = new int[]{100, 200, 400, 800, 1600};
    Random rand = new Random();
    for (int n : sizes){
        //random array
        int [] randomArray = new int[n];
        for (int i = 0; i < n; i++){
            randomArray[i] = rand.nextInt(n);
        }

    //ordered array
        int[] orderedArray = new int[n];
        for (int i = 0; i < n; i++){
            orderedArray[i] = i;
        }
       
    // partially-ordered array
        int[] partiallyOrderedArray = new int[n];
        for (int i = 0; i < n / 2 ; i++){
            partiallyOrderedArray[i] = i;
        }

        for(int i = n / 2; i < n; i++){
            partiallyOrderedArray[i] = rand.nextInt(n);
        }


    // reverse-ordered array
        int[] reverseOrderedArray = new int[n];
        for (int i = 0; i < n; i++){
            reverseOrderedArray[i] = n - 1- i;
        }

       long startTime = System.nanoTime();
       bubbleSort(randomArray);
       long endTime = System.nanoTime();
       System.out.println("Time for random array of size " + n + " is " + (endTime - startTime) + " nano"); 

       long startTime1 = System.nanoTime();
       bubbleSort(orderedArray);
       long endTime1 = System.nanoTime();
       System.out.println("Time for ordered array of size " + n + " is " + (endTime1 - startTime1) + " nano"); 


       long startTime2 = System.nanoTime();
       bubbleSort(partiallyOrderedArray);
       long endTime2 = System.nanoTime();
       System.out.println("Time for partially ordered array of size " + n + " is " + (endTime2 - startTime2) + " nano"); 
       

       long startTime3 = System.nanoTime();
       bubbleSort(reverseOrderedArray);
       long endTime3 = System.nanoTime();
       System.out.println("Time for reverse-ordered array of size " + n + " is " + (endTime3 - startTime3) + " nano"); 
       System.out.println("------------------------------------------------------------");

    }
   

// conclusion : the more ordered a list is, the less complexity it will take to sort the array. And the bigger size the array becomes, the longer it takes.





    }

}



   

