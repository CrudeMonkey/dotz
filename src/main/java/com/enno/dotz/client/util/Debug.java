package com.enno.dotz.client.util;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.enno.dotz.client.Cell;

public class Debug
{
    private static Logger s_log = Logger.getLogger(Debug.class.getName());
    
    public static void p(String s)
    {
        Console.log(s);
    }
    
    public static void p(String s, Exception e)
    {
        s_log.log(Level.SEVERE, s, e);
    }
    
    public static String str(List<Cell> cells)
    {
        StringBuilder b = new StringBuilder();
        for (Cell c : cells)
        {
            b.append(c.col).append(",").append(c.row).append(" ");
        }
        return b.toString();
    }
}
