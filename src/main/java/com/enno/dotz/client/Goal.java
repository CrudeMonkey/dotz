package com.enno.dotz.client;

public class Goal
{
    public static final int ALL = -1;

    protected int m_maxMoves;

    protected int[] m_dots = new int[Config.MAX_COLORS];
    protected int m_animals;
    protected int m_knights;
    protected int m_clocks;
    protected int m_anchors;
    protected int m_diamonds;
    protected int m_doors;
    protected int m_cages;
    protected int m_ice;
    protected int m_fire;
    protected int m_circuits;
    protected int m_lasers;
    protected int m_mirrors;
    protected int m_rockets;
    protected int m_birds;
    protected int m_dominoes;
    protected int m_blockers;
    protected int m_zapBlockers;
    protected int m_bubbles;
    protected int m_colorBombs;
    protected int m_bombs;
    protected int m_blasters;
    protected int m_coins;
    
    protected int m_score;
    protected int m_time;
    protected int m_words;      // must find N words

    protected ChainGoal m_chainGoal;
    
    public int getWords()
    {
        return m_words;
    }
    
    public void setWords(int words)
    {
        m_words = words;
    }
    
    public int getFire()
    {
        return m_fire;
    }

    public void setFire(int fire)
    {
        m_fire = fire;
    }

    public int[] getDots()
    {
        return m_dots;
    }
    
    public int getAnimals()
    {
        return m_animals;
    }
    
    public void setDots(int color, int goal)
    {
        m_dots[color] = goal;
    }
    
    public void setAnimals(int goal)
    {
        m_animals = goal;
    }

    public int getKnights()
    {
        return m_knights;
    }
    
    public void setKnights(int goal)
    {
        m_knights = goal;
    }
    
    public int getClocks()
    {
        return m_clocks;
    }

    public void setClocks(int goal)
    {
        m_clocks = goal;
    }

    public int getMaxMoves()
    {
        return m_maxMoves;
    }

    public void setMaxMoves(int maxMoves)
    {
        m_maxMoves = maxMoves;
    }

    public int getAnchors()
    {
        return m_anchors;
    }

    public void setAnchors(int anchors)
    {
        m_anchors = anchors;
    }

    public int getDiamonds()
    {
        return m_diamonds;
    }

    public void setDiamonds(int diamonds)
    {
        m_diamonds = diamonds;
    }

    public int getCoins()
    {
        return m_coins;
    }

    public void setCoins(int coins)
    {
        m_coins = coins;
    }

    public int getBubbles()
    {
        return m_bubbles;
    }

    public void setBubbles(int bubbles)
    {
        m_bubbles = bubbles;
    }

    public int getDoors()
    {
        return m_doors;
    }

    public void setDoors(int doors)
    {
        m_doors = doors;
    }
    
    public int getCages()
    {
        return m_cages;
    }

    public void setCages(int cages)
    {
        m_cages = cages;
    }

    public int getIce()
    {
        return m_ice;
    }

    public void setIce(int ice)
    {
        m_ice = ice;
    }

    public int getScore()
    {
        return m_score;
    }

    public void setScore(int score)
    {
        m_score = score;
    }

    public void setTime(int val)
    {
        m_time = val;
    }

    public int getTime()
    {
        return m_time;
    }

    public void setCircuits(int goal)
    {
        m_circuits = goal;
    }
    
    public int getCircuits()
    {
        return m_circuits;
    }

    public void setBlockers(int goal)
    {
        m_blockers = goal;
    }
    
    public int getBlockers()
    {
        return m_blockers;
    }

    public void setZapBlockers(int goal)
    {
        m_zapBlockers = goal;
    }
    
    public int getZapBlockers()
    {
        return m_zapBlockers;
    }

    public void setLasers(int goal)
    {
        m_lasers = goal;
    }
    
    public int getLasers()
    {
        return m_lasers;
    }

    public void setMirrors(int goal)
    {
        m_mirrors = goal;
    }
    
    public int getBirds()
    {
        return m_birds;
    }

    public void setBirds(int birds)
    {
        m_birds = birds;
    }

    public int getMirrors()
    {
        return m_mirrors;
    }

    public void setRockets(int goal)
    {
        m_rockets = goal;
    }
    
    public int getRockets()
    {
        return m_rockets;
    }
    
    public boolean isOutOfMoves(Score score)
    {
        return m_maxMoves != 0 && score.getMoves() >= m_maxMoves;
    }

    public int getDominoes()
    {
        return m_dominoes;
    }
    
    public void setDominoes(int goal)
    {
        m_dominoes = goal;
    }

    public ChainGoal getChainGoal()
    {
        return m_chainGoal;
    }

    public void setChainGoal(ChainGoal chainGoal)
    {
        m_chainGoal = chainGoal;
    }

    public int getColorBombs()
    {
        return m_colorBombs;
    }

    public void setColorBombs(int colorBombs)
    {
        m_colorBombs = colorBombs;
    }

    public int getBombs()
    {
        return m_bombs;
    }

    public void setBombs(int bombs)
    {
        m_bombs = bombs;
    }

    public int getBlasters()
    {
        return m_blasters;
    }

    public void setBlasters(int blasters)
    {
        m_blasters = blasters;
    }
}
