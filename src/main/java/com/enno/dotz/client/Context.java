package com.enno.dotz.client;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.WordDisplayPanel.FindWordList;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.shared.WordFinder.ResultList;

public class Context
{
    public boolean isEditing;
    public Config cfg;
    public GridState state;
    public Score score = new Score();
    
    public Generator generator;
    public Random beastRandom;

    public Layer backgroundLayer;
    public Layer iceLayer;
    public Layer dotLayer;
    public Layer doorLayer;
    public Layer nukeLayer;
    public Layer laserLayer;
    public Layer connectLayer;
    
    public DotzGridPanel gridPanel;
    public BoostPanel boostPanel;
    public ScorePanel scorePanel;
    public StatsPanel statsPanel;
    public WordDisplayPanel wordDisplayPanel;

    public Pt lastMove; // location of last connect
    
    public boolean killed;
    
    public Set<String> guessedWords = new HashSet<String>();
    public ResultList bestWords = new ResultList(20);
    public FindWordList findWordList;
    
    public int gridWidth;
    public int gridHeight;
    public int gridDx;
    public int gridDy;
    
    public Context(boolean isEditing, Config level)
    {
       this.isEditing = isEditing;
       cfg = level;
       
       int cols = Math.max(cfg.numColumns, 8);
       int rows = Math.max(cfg.numRows, 8);
       gridWidth = cols * cfg.size + (isEditing ? 0 : cfg.size);
       gridHeight = rows * cfg.size + (isEditing ? 0 : cfg.size);
       gridDx = (gridWidth - (cfg.numColumns * cfg.size)) / 2;
       gridDy = (gridHeight - (cfg.numRows * cfg.size)) / 2;
    }
    
    public boolean isWild(Integer color)
    {
        return cfg.isWild(color);
    }

    public void init()
    {
        generator = cfg.generator.copy();
        beastRandom = new Random(generator.getSeed());
    }
    
    public void prepareWords()
    {
        if (generator.findWords)
        {
            findWordList = new FindWordList(this);
        }
    }
}
