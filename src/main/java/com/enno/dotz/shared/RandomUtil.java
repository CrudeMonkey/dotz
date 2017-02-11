package com.enno.dotz.shared;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class RandomUtil
{
    public static Random copy(Random rnd)
    {
        if (rnd instanceof Random2)
            return ((Random2) rnd).copy();
        
        throw new RuntimeException("invalid random class" + (rnd == null ? null : rnd.getClass()));
    }
//    
//    public static void main(String[] args)
//    {
//        Random rnd1 = new Random();
//        Random rnd2 = copy(rnd1);
//        for (int i = 0; i < 20; i++)
//        {
//            long a = rnd1.nextLong();
//            long b = rnd2.nextLong();
//            if (a != b)
//                System.out.println("diff " + a + " != " + b);
//        }
//        
//        rnd2 = copy(rnd1);
//        for (int i = 0; i < 20; i++)
//        {
//            long a = rnd1.nextLong();
//            long b = rnd2.nextLong();
//            if (a != b)
//                System.out.println("diff " + a + " != " + b);
//        }
//    }
}
