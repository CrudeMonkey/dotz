package com.enno.dotz.client;

import java.util.HashSet;
import java.util.Set;

import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.WordDisplayPanel.FindWordList;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.shared.WordFinder.ResultList;

public class Context
{
    public boolean isEditing;
    public boolean isPreview;

    public Config cfg;
    public GridState state;
    public Score score = new Score();
    
    public Generator generator;

    public FastLayer backgroundLayer;
    public FastLayer iceLayer;
    public FastLayer dotLayer;
    public FastLayer doorLayer;
    public FastLayer nukeLayer;
    public FastLayer laserLayer;
    public Layer connectLayer;
    public PlaybackLayer playbackLayer;
    
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
    
    public Recorder recorder;
    public PlaybackDialog playbackDialog;
    
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
    
    public boolean isRecording()
    {
        return recorder != null;
    }
    
    public boolean isWild(Integer color)
    {
        return cfg.isWild(color);
    }

    public void init()
    {
        state = cfg.grid.copy();
        generator = cfg.generator.copy();
        
        if (playbackDialog != null)
        {
            generator.setUsedSeed(playbackDialog.getSeed());
        }
        
        if (playbackDialog == null && !isEditing && !isPreview)
        {
            recorder = new Recorder(this);
            recorder.level(cfg.id, generator.getUsedSeed(), cfg.name);
        }
    }
    
    public void prepareWords()
    {
        if (generator.findWords)
        {
            findWordList = new FindWordList(this);
        }
    }

    public void userClicked(Cell cell)
    {
        lastMove = cell.pt();
    }
}
