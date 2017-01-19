package com.enno.dotz.client.editor;

import java.util.LinkedHashMap;

import com.enno.dotz.client.Boosts;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.Goal;
import com.enno.dotz.client.ui.MXSelectItem;

public enum Preset
{
    DOTS("Dots"),
    CANDY_CRUSH("Candy Crush"),
    DOMINOES("Dominoes"),
    WORDS("Words"),
    JELLY_SPLASH("Jelly Splash"),
    TOY_BLAST("Toy Blast"),
    ROME("Rome"),
    FIND_WORDS("Find Specific Words"),
    FIXED_WORDS("Fixed Words");
    
    private String m_name;
    
    Preset(String name)
    {
        m_name = name;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public Config getPresetConfig()
    {
        Config c = new Config();
        c.folder = m_name;
        
        Generator g = new Generator();
        c.generator = g;
        
        c.goals = new Goal();
        c.boosts = new Boosts();
        
        switch(this)
        {
            case CANDY_CRUSH:
                g.swapMode = true;
                c.numColumns = 9;
                c.numRows = 9;
                break;
            case DOMINOES:
                g.dominoMode = true;
                break;
            case WORDS:
                g.generateLetters = true;
                break;
            case FIND_WORDS:
                g.generateLetters = true;
                g.findWords = true;
                g.removeLetters = false;
                break;
            case FIXED_WORDS:
                g.generateLetters = true;
                g.removeLetters = false;
                break;
            case TOY_BLAST:
                g.clickMode = true;
                c.numColumns = 9;
                c.numRows= 9;
                g.rewardStrategies = "b<5,e<7,c<9";
                break;
            case JELLY_SPLASH:
                g.diagonalMode = true;
                c.numColumns = 7;
                c.numRows = 9;
                g.minChainLength = 3;
                g.slipperyAnchors = true;
            default:
                break;
        }
        return c;
    }
    
    public static Preset find(String s)
    {
        for (Preset p : values())
        {
            if (p.getName().equals(s))
                return p;
        }
        
        return null;
    }
    
    public static class PresetSelector extends MXSelectItem
    {
        public PresetSelector()
        {
            setTitle("Preset");
            
            LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
            for (Preset p : Preset.values())
            {
                map.put(p.getName(), p.getName());
            }
            setValueMap(map);
            setDefaultToFirstOption(true);
        }
        
        public Config getPresetConfig()
        {
            String s = getValueAsString();
            if (s == null || s.length() == 0)
                return null;
            
            Preset preset = Preset.find(s);
            return preset.getPresetConfig();
        }
    }
}
