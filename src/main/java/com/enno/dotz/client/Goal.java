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
    
    protected int m_score;
    protected int m_time;
  
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

    public void setClocks(Integer goal)
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
}
