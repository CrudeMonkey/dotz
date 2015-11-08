package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.enno.dotz.client.util.StringTokenizer;

public class WordList
{
    private static Set<String> s_words = new HashSet<String>();
    
    public static void init(String list)
    {
        StringTokenizer tok = new StringTokenizer(list);
        while (tok.hasMoreTokens()) 
        {
            s_words.add(tok.nextToken());
        }
    }
    
    public static String getWord(String word)
    {
        word = word.toLowerCase();
        
        if (word.contains("?"))
        {
            char[] ch = word.toCharArray();
            int[] wc = findWildCards(ch);
            if (wc.length > 5)
                return null; // can't have more than 5 wild cards or it takes to long to find words
            
            String s = findWord(ch, wc, 0);
            return s;
        }
        else
        {
            if (s_words.contains(word))
            {
                return word;
            }
        }
        return null;
    }
    
    private static int[] findWildCards(char[] ch)
    {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < ch.length; i++)
        {
            if (ch[i] == '?')
                list.add(i);
        }
        int n = list.size();
        int[] wc = new int[n];
        for (int i = 0; i < n; i++)
        {
            wc[i] = list.get(i);
        }
        return wc;
    }
    
    private static String findWord(char[] ch, int[] wc, int i)
    {
        int p = wc[i];
        boolean last = i == wc.length - 1;
        for (char c = 'a'; c <= 'z'; c++)
        {
            ch[p] = c;
            if (last)
            {
                String s = new String(ch);
                if (s_words.contains(s))
                    return s;
            }
            else
            {
                String s = findWord(ch, wc, i + 1);
                if (s != null)
                    return s;
            }
        }
        return null;
    }
    
//    public static void main(String[] args)
//    {
//        try
//        {
//            //Reader r = new InputStreamReader(WordList.class.getResource("/enable1.txt").openStream());
//            StringWriter writer = new StringWriter();
//            IOUtils.copy(WordList.class.getResource("/enable1.txt").openStream(), writer, "UTF-8");
//            String s = writer.toString();
//            init(s);
//            
//            String[] words = {
//                    "aa",
//                    "abacus",
//                    "zyzzyvas",
//                    "abac?",
//                    "?baci",
//                    "?bac?",
//                    "?????"
//            };
//            for (String str : words)
//            {
//                String found = getWord(str);
//                if (found != null)
//                {
//                    System.out.println(str + " => " + found);
//                }
//                else
//                {
//                    System.out.println(str + " NOT FOUND");
//                }
//            }
//        }
//        catch (IOException e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
