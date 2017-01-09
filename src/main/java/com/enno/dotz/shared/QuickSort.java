package com.enno.dotz.shared;

public class QuickSort
{
    public static void sort(int[] arr)
    {
        sort(arr, 0, arr.length - 1);
    }
    
    public static void sort(int[] arr, int low, int high) 
    {
        if (arr == null || arr.length == 0)
            return;
 
        if (low >= high)
            return;
 
        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];
 
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }
 
            while (arr[j] > pivot) {
                j--;
            }
 
            if (i <= j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
 
        // recursively sort two sub parts
        if (low < j)
            sort(arr, low, j);
 
        if (high > i)
            sort(arr, i, high);
    }
    
    public static void sort(char[] arr)
    {
        sort(arr, 0, arr.length - 1);
    }
    
    public static void sort(char[] arr, int low, int high) 
    {
        if (arr == null || arr.length == 0)
            return;
 
        if (low >= high)
            return;
 
        // pick the pivot
        int middle = low + (high - low) / 2;
        int pivot = arr[middle];
 
        // make left < pivot and right > pivot
        int i = low, j = high;
        while (i <= j) {
            while (arr[i] < pivot) {
                i++;
            }
 
            while (arr[j] > pivot) {
                j--;
            }
 
            if (i <= j) {
                char temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
 
        // recursively sort two sub parts
        if (low < j)
            sort(arr, low, j);
 
        if (high > i)
            sort(arr, i, high);
    }
}
