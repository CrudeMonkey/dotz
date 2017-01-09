package com.enno.dotz.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.enno.dotz.shared.emulation.Arrays;

public class WordFinder
{
    private final static int MAX_LEN = 30;

    public boolean addMultipliers = true;  // if true, word multipliers are additive
    public int     minWordLength  = 3;
    public int     maxWordLength  = MAX_LEN;
    public boolean canGoDiagonal  = true;
    public int     maxWildCards   = 5;
    public boolean q_is_qu        = true; // if true, then a "Q" is "Qu"
    
    private int         w, h;
    private char[][]    grid;
    private boolean[][] used;
    private int[][]     points;
    private int[][]     multiplier;

    private char[] word = new char[MAX_LEN];
    private int[]  col  = new int[MAX_LEN];
    private int[]  row  = new int[MAX_LEN];

    private BinSearchDictionary m_dict;

    private ResultList resultList;
    
    public WordFinder(int w, int h, int keep, BinSearchDictionary dict)
    {
        this.w = w;
        this.h = h;
        m_dict = dict;
        
        grid = new char[w][h];
        used = new boolean[w][h];
        points = new int[w][h];
        multiplier = new int[w][h];
        
        resultList = new ResultList(keep);
    }
    
    public void set(int x, int y, char c, int pts, int wordMultiplier)
    {
        grid[x][y] = Character.toLowerCase(c);
        points[x][y] = pts;
        multiplier[x][y] = wordMultiplier;
    }
    
    public void setUnusable(int x, int y)
    {
        used[x][y] = true;
    }
    
    public void rnd()
    {
        Random r = new Random(123);
        for (int x = 0; x < w; x++)
        {
            for (int y = 0; y < h; y++)
            {
                int c = r.nextInt(32);
                grid[x][y] = c >= 26 ? '?' : (char) ('a' + c);
                points[x][y] = c >= 26 ? 0 : 1;
            }
        }
    }
    
    public void dump()
    {
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                System.out.print(grid[x][y]);
            }
            System.out.println();
        }
    }
    
    public void findWords()
    {
        findWords(null);
    }
    
    public void findWords(Collection<String> excludeWords)
    {
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                track(x, y, word, 0, 0, 0, addMultipliers ? 0 : 1, excludeWords);
            }
        }
        
//        resultList.dump();
    }
    
    private boolean track(int x, int y, char[] word, int n, int numWild, int pts, int mult, Collection<String> excludeWords)
    {
        if (grid[x][y] == '?')
        {
            if (numWild > maxWildCards)
                return false;
            
            boolean found = false;
            for (char c = 'a'; c <= 'z'; c++)
            {
                grid[x][y] = c;
                if (track(x, y, word, n, numWild + 1, pts, mult, excludeWords))
                    found = true;
            }
            grid[x][y] = '?';
            return found;
        }
        
        col[n] = x;
        row[n] = y;
        pts += points[x][y];
        
        if (multiplier[x][y] > 0)
        {
            if (addMultipliers)
                mult += multiplier[x][y];
            else
                mult *= multiplier[x][y];
        }
        
        word[n++] = grid[x][y];
        if (q_is_qu && grid[x][y] == 'q')
            word[n++] = 'u';
        
        used[x][y] = true;

        String s = new String(word, 0, n);
        boolean found = false;
        if (m_dict.canStartWith(s))
        {
            if (n < maxWordLength)
            {
                for (int i = -1; i <= 1; i++)
                {
                    for (int j = -1; j <= 1; j++)
                    {
                        if (i == 0 && j == 0)
                            continue;
                        
                        if (!canGoDiagonal && i != 0 && j != 0)
                                continue;
                        
                        int nx = x + i;
                        int ny = y + j;
                        if (nx < 0 || nx >= w || ny < 0 || ny >= h || used[nx][ny])
                            continue;
                        
                        if (track(nx, ny, word, n, numWild, pts, mult, excludeWords))
                            found = true;
                    }
                }
            }
            
            if (!found)
            {
                if (n >= minWordLength && m_dict.contains(s) && (excludeWords == null || !excludeWords.contains(word)))
                {
                    addWord(s, numWild, pts, mult);
                    found = true;
                }
            }
        }
        used[x][y] = false;
        return found;
    }
    
    private int f = 0;
    public void addWord(String word, int numWild, int pts, int mult)
    {
//        System.out.println(f++ + " Found " + word + (numWild > 0 ? "*" : "") + " " + pts * mult);
        
        resultList.addWord(word, pts);
    }
    
    public List<String> getWordList()
    {
        List<String> list = new ArrayList<String>();
        list.addAll(resultList.m_words);
        //Collections.reverse(list);
        return list;
    }

    public static class ResultList
    {
        public static class Result implements Comparable<Result>
        {
            public String word;
            public int points;
            
            Result(String word, int points)
            {
                this.word = word;
                this.points = points;
            }

            @Override
            public int compareTo(Result r)
            {
                return points - r.points;
            }
            
            @Override
            public String toString()
            {
                return word + points;
            }
        }
        
        private Set<String> m_words = new HashSet<String>();
        
        private Result[] m_list;    // sorted by points, ascending (i.e. highest points last)
        private int m_keep;
        private int m_n;
        
        private int m_minPoints = 0;
        
        public ResultList(int keep)
        {
            m_keep = keep;
            m_list = new Result[m_keep];
        }
        
        public void addWord(String word, int points)
        {
            if (m_n == m_keep && points <= m_minPoints)
                return;
            
            if (m_words.contains(word))
            {
                int i = findWord(word);
                if (m_list[i].points >= points)
                    return; // same word, same/fewer points
                
                // Found same word but with more points
                remove(i);
            }
            
            Result r = new Result(word, points);
            int p = Arrays.binarySearch(m_list, 0, m_n, r);
            if (p < 0)
                p = -p - 1;
            
            if (m_n == m_keep)
            {
                // Insert result before [p]
                m_words.remove(m_list[0].word);
                
                // Move everything before [p] back
                for (int i = 0, j = 1; j < p; i++, j++)
                {
                    m_list[i] = m_list[j];
                }
                m_list[p - 1] = r;
                m_words.add(word);
                m_minPoints = m_list[0].points;
            }
            else
            {
                // Move everything from [p] on forward
                for (int i = m_n, j = i - 1; j >= p; i--, j--)
                {
                    m_list[i] = m_list[j];
                }
                m_list[p] = r;
                m_words.add(word);
                m_minPoints = m_list[0].points;
                m_n++;
            }
        }
        
        protected void remove(int index)
        {
            for (int i = index, j = index + 1; j < m_n; i++, j++)
            {
                m_list[i] = m_list[j];
            }
            m_n--;
        }
        
        protected int findWord(String word)
        {
            for (int i = 0; i < m_n; i++)
            {
                if (m_list[i].word.equals(word))
                    return i;
            }
            return -1;
        }
        
        public void dump()
        {
            System.out.println("--------------");
            for (int i = 0; i < m_n; i++)
            {
                System.out.println(m_list[i].word + " " + m_list[i].points);
            }
        }
    }
}
