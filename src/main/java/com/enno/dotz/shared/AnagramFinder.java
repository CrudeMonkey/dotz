package com.enno.dotz.shared;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.enno.dotz.server.util.Dictionary;
import com.enno.dotz.shared.emulation.Arrays;

public class AnagramFinder
{
    private AnagramDictionary m_dict;

    public AnagramFinder(BinSearchDictionary dict)
    {
        m_dict = new AnagramDictionary(dict);
    }
    
    public WordList find(String word, int minLength)
    {
        WordList list = new WordList();
        find(word, list, minLength);
        return list;
    }
    
    public Collection<String> find(String word, Collection<String> list, int minLength)
    {
        word = word.toLowerCase();
        char[] letters = sortLetters(word);
        
        int n = word.length();
        char[] buf = new char[n];
        
        find(list, 0, n, letters, buf, 0, minLength);
        
        return list;
    }
    
    private void find(Collection<String> list, int p, int n, char[] letters, char[] buf, int buf_len, int minLength)
    {
        int curr_len = buf_len + 1;
        for (int i = p; i < n; i++)
        {
            buf[buf_len] = letters[i];
            
            boolean stop = false;
            if (curr_len >= minLength)
            {
                String word = new String(buf, 0, curr_len);
                if (!m_dict.addWords(word, list))
                    stop = true;
                
//                System.out.println(word);
            }
            if (!stop)
                find(list, i + 1, n, letters, buf, curr_len, minLength);
        }
    }

    public static char[] sortLetters(String s)
    {
        char[] c = s.toCharArray();
        Arrays.sort(c);
        return c;
    }
    
    public static String key(String word)
    {
        return new String(sortLetters(word));
    }
    
    public static class WordList extends ArrayList<String>
    {
        private static final Comparator<String> BY_LEN_THEN_ASC = new Comparator<String>() 
        {
            @Override
            public int compare(String a, String b)
            {
                int cmp = a.length() - b.length();
                if (cmp == 0)
                    cmp = a.compareTo(b);
                
                return cmp;
            }
        };

        @Override
        public boolean add(String s)
        {
            if (contains(s))
                return false;
            
            return super.add(s);
        }
        
        public WordList sort()
        {
            Collections.sort(this, BY_LEN_THEN_ASC);
            return this;
        }
    }
    
    public static class AnagramDictionary
    {
        public static class Entry implements Comparable<Entry>
        {
            String key;
            String word;
            
            public Entry(String key, String word)
            {
                this.key = key;
                this.word = word;
            }
            
            @Override
            public int compareTo(Entry e)
            {
                return key.compareTo(e.key);
            }
            
            @Override
            public String toString()
            {
                return key + ':' + word;
            }
        }

        private Entry[] m_list;
        private int m_size;
        
        public AnagramDictionary(BinSearchDictionary dict)
        {
            List<Entry> list = new ArrayList<Entry>();
            String[] words = dict.getWords();
            for (int i = 0, n = dict.size(); i < n; i++)
            {
                String word = words[i];
                list.add(new Entry(key(word), word));
            }
            Collections.sort(list);
            
            m_size = list.size();
            m_list = list.toArray(new Entry[m_size]);
            
        }
        
        private Entry m_entry = new Entry(null, null);
        
        public boolean addWords(String sortedKey, Collection<String> words)
        {
            m_entry.key = sortedKey;
            return addWords(m_entry, words);
        }
        
        public boolean addWords(Entry e, Collection<String> words)
        {
            int i = Arrays.binarySearch(m_list, e);
            String key = e.key;
            if (i >= 0)
            {
                words.add(m_list[i].word);
                int p = i;
                while (--p >= 0 && m_list[p].key.equals(key))
                {
                    words.add(m_list[p].word);
                }
                while (++i < m_size && m_list[i].key.equals(key))
                {
                    words.add(m_list[i].word);
                }
            }
            else
            {
                i = -i - 1;
            }
            
            // See if the next key in the dictionary starts with the key
            if (i < m_size && m_list[i].key.startsWith(key))
                return true;
            
            return false;
        }
    }
    
    public static void main(String[] args)
    {
        try
        {
            BinSearchDictionary dict = Dictionary.load(BinSearchDictionary.class.getResource("/clean_enable1.txt"));
            
            AnagramFinder a = new AnagramFinder(dict);
//            String word = "bush";
//            String word = "mellow";
//            String word = "uursre";
            
            String word = "crayon";
            int minLength = 3; // Text Twist
//            int minLength = 2; // Bully
            
            WordList found = a.find(word, minLength);
            found.sort();
            
            System.out.println("-----------");
            for (String s : found)
            {
                System.out.println(s);
            }
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
