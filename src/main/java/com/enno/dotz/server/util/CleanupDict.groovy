package com.enno.dotz.server.util

import com.enno.dotz.shared.BinSearchDictionary;

class CleanupDict
{
    private Dictionary m_dict;
    
    public CleanupDict(String dictName = 'enable1.txt')
    {
        m_dict = Dictionary.load(getClass().getResource('/' + dictName))
    }
    
    public void subtract(String dictName)
    {
        m_dict.subtract(Dictionary.load(getClass().getResource('/' + dictName)))
    }
    
    public void findShortWords(int n, String fileName)
    {
        List list = m_dict.findWordsWithLength(n)
        new Dictionary(list).save(fileName)
    }
    
    public static void main(String[] args)
    {
        String dictName = 'enable1.txt'
        def d = new CleanupDict(dictName)
        //d.findShortWords(2, "src/main/resources/two_letter.txt")
        //d.findShortWords(3, "src/main/resources/three_letter.txt")
        
        d.subtract('bad_two_letter.txt')
        d.subtract('bad_three_letter.txt')
        d.m_dict.save('src/main/resources/clean_' + dictName)
    }
}
