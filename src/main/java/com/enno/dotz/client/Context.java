package com.enno.dotz.client;

import java.util.Random;

import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.anim.Pt;

public class Context
{
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

    public Pt lastMove; // location of last connect
    
    public boolean killed;
    
    public boolean isWild(Integer color)
    {
        return cfg.isWild(color);
    }

    public void init()
    {
        generator = cfg.generator.copy();
        beastRandom = new Random(generator.getSeed());
    }
}
