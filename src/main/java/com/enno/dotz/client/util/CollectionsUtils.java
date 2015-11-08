
package com.enno.dotz.client.util;

import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.RandomAccess;

public class CollectionsUtils
{
    public static final int SHUFFLE_THRESHOLD = 5;

    private static Random r;

    public static void shuffle(List<?> list)
    {
        if (r == null)
        {
            r = new Random();
        }
        shuffle(list, r);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void shuffle(List<?> list, Random rnd)
    {
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess)
        {
            for (int i = size; i > 1; i--)
                swap(list, i - 1, rnd.nextInt(i));
        }
        else
        {
            Object arr[] = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--)
                swap(arr, i - 1, rnd.nextInt(i));

            // Dump array back into list
            ListIterator it = list.listIterator();
            for (int i = 0; i < arr.length; i++)
            {
                it.next();
                it.set(arr[i]);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void swap(List<?> list, int i, int j)
    {
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    private static void swap(Object[] arr, int i, int j)
    {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
