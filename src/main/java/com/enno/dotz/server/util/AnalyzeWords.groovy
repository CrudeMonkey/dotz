package com.enno.dotz.server.util

import com.enno.dotz.shared.BinSearchDictionary
import com.enno.dotz.shared.WordFinder

class AnalyzeWords
{
    private BinSearchDictionary m_dict;
    
    private boolean[][] m_follows = new boolean[26][26];
    private DiMap diMap = new DiMap();
    
    AnalyzeWords()
    {
        m_dict = Dictionary.load(getClass().getResource('/enable1.txt'))
        
    }
    
    void analyze()
    {
        for (String word : m_dict.getWords())
        {
            diMap.process(word)
        }
        diMap.dumpNeg()
    }
    
    void find(String s)
    {
        println "find " + s + " " + m_dict.contains(s)
    }
    
    void startsWith(String s)
    {
        println "startsWith " + s + " " + m_dict.canStartWith(s)
    }

    void findWords()
    {
        WordFinder g = new WordFinder(10, 10, 20, m_dict);
        g.rnd();
        g.dump();
        g.findWords();
    }
    
    static void main(String[] args)
    {
        AnalyzeWords w = new AnalyzeWords()
        //w.analyze()
//        List words = [ 'aa', 'aardvark', 'zyzzyvas', 'tx', 'zzzz' ]
//        for (String word : words)
//        {
//            w.find(word)
//            w.startsWith(word)
//        }
          w.findWords();  
    }
}
