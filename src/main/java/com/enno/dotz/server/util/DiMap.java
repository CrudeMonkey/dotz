package com.enno.dotz.server.util;

public class DiMap
{
    private boolean[][] m_follows = new boolean[26][26];
    
    public void process(String word)
    {
        char[] ch = word.toCharArray();
        int n = ch.length - 1;
        for (int i = 0; i < n; i++)
        {
            m_follows[index(ch[i])][index(ch[i+1])] = true;
        }
    }
    
    public void follows(char a, char b)
    {
        m_follows[index(a)][index(b)] = true;
    }
    
    int index(char c)
    {
        return (int) (c - 'a');
    }
    
    char letter (int i)
    {
        return (char) ('a' + i);
    }
    
    void dump()
    {
        for (int i = 0; i < 26; i++)
        {
            System.out.print(letter(i) + ": ");
            
            for (int j = 0; j < 26; j++)
            {
                if (m_follows[i][j])
                    System.out.print(letter(j));
            }
            System.out.println();
        }
    }
    
    void dumpNeg()
    {
        for (int i = 0; i < 26; i++)
        {
            System.out.print(letter(i) + ": ");
            
            for (int j = 0; j < 26; j++)
            {
                if (!m_follows[i][j])
                    System.out.print(letter(j));
            }
            System.out.println();
        }
    }
    
}
