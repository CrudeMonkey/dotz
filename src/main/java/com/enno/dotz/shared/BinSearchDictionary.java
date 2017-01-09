package com.enno.dotz.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.enno.dotz.client.util.StringTokenizer;
import com.enno.dotz.shared.emulation.Arrays;


public class BinSearchDictionary
{
    protected String[] m_words;
    protected int m_size;
    
    private int m_maxWildcards = 5;
    
    public BinSearchDictionary(String list)
    {
        List<String> words = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(list);
        while (tok.hasMoreTokens())
        {
            words.add(tok.nextToken());
        }
        m_size = words.size();
        m_words = words.toArray(new String[m_size]);
    }
    
    /**
     * Assumes list is sorted
     * 
     * @param list  Sorted word list
     */
    public BinSearchDictionary(Collection<String> list)
    {
        m_size = list.size();
        m_words = list.toArray(new String[m_size]);
    }
    
    public boolean contains(String word)
    {
        int i = Arrays.binarySearch(m_words, 0, m_size, word);
        return i >= 0;
    }
    
    /**
     * Find a word that matches the pattern. "?" is a wild card.
     * 
     * @param pattern       Letters must be lower case. May contain "?" wild cards.
     * @param invalidWords  When expanding wild cards, don't return these words.
     * 
     * @return the matching word
     */
    public String find(String pattern, Set<String> invalidWords)
    {
        if (pattern.contains("?"))
        {
            char[] ch = pattern.toCharArray();
            int[] wc = findWildCards(ch);
            if (wc.length > m_maxWildcards)
                return null; // can't have more than 5 wild cards or it takes to long to find words
            
            String s = findWord(ch, wc, 0, invalidWords);
            return s;
        }
        else
        {
            if (contains(pattern) && !invalidWords.contains(pattern))
                return pattern;
        }
        return null;
    }
    
    /** 
     * BEWARE: use size() to constrain the list when words have been removed!
     * 
     * @return
     */
    public String[] getWords()
    {
        return m_words;
    }
    
    public int size()
    {
        return m_size;
    }
    
    public boolean canStartWith(String word)
    {
        int i = Arrays.binarySearch(m_words, 0, m_size, word);
        if (i >= 0)
            return true; // found it
        
        i = -i - 1;
        if (i >= m_size)
            return false;
        
        return m_words[i].startsWith(word);
    }

    private int[] findWildCards(char[] ch)
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
    
    private String findWord(char[] ch, int[] wc, int i, Set<String> invalidWords)
    {
        int p = wc[i];
        boolean last = i == wc.length - 1;
        for (char c = 'a'; c <= 'z'; c++)
        {
            ch[p] = c;
            if (last)
            {
                String s = new String(ch);
                if (!invalidWords.contains(s) && contains(s))
                    return s;
            }
            else
            {
                String s = findWord(ch, wc, i + 1, invalidWords);
                if (s != null)
                    return s;
            }
        }
        return null;
    }
    
    public boolean removeWord(String word)
    {
        int index = Arrays.binarySearch(m_words, 0, m_size, word);
        if (index >= 0)
        {
            for (int i = index, j = index + 1; j < m_size; i++, j++)
            {
                m_words[i] = m_words[j];
            }
            m_size--;
            return true;
        }
        return false;
    }
    
    /**
     * Remove all words found in dictionary d
     * 
     * @param d
     */
    public void subtract(BinSearchDictionary d)
    {
        for (int i = 0; i < d.m_size; i++)
            removeWord(d.m_words[i]);
    }
    
    public List<String> findWordsWithLength(int n)
    {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < m_size; i++)
        {
            if (m_words[i].length() == n)
                list.add(m_words[i]);
        }
        return list;
    }
    
}
