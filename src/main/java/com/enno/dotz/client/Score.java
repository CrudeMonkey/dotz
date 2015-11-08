package com.enno.dotz.client;

import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;

public class Score
{
    private int[] m_explodedDots = new int[Config.MAX_COLORS];
    private int m_explodedWildcards; // e.g. by nuke (when not counting as a color)    
    
    private int m_fireInGrid;
    private int m_explodedFire;
    
    private int m_anchorsInGrid;
    private int m_droppedAnchors;
    private int m_explodedAnchors;
    
    private int m_clocksInGrid;
    private int m_droppedClocks;
    private int m_explodedClocks;

    private int m_animalsInGrid;
    private int m_explodedAnimals;
    
    private int m_knightsInGrid;
    private int m_explodedKnights;
    
    private int m_initialDoorCount;
    private int m_explodedDoors;

    private int m_initialIceCount;
    private int m_explodedIce;

    private int m_initialCircuitCount;
    private int m_explodedCircuits;
    
    private int m_lasersInGrid;
    private int m_explodedLasers;
    private int m_shortCircuitedLasers;
    
    private int m_mirrorsInGrid;
    private int m_explodedMirrors;
    
    private int m_rocketsInGrid;
    private int m_explodedRockets;
    
    private int m_birds;

    private int m_score;
    private int m_moves;    
    
    public void explodedIce()
    {
        m_explodedIce++;
        addPoints(10);
    }

    public void explodeCircuits(int n)
    {
        m_explodedCircuits += n;
        addPoints(10 * n);
    }
    
    public void explodedDoor()
    {
        m_explodedDoors += 1;     // NOTE: 0 points
    }
    
    public void explodedFire()
    {
        m_explodedFire++;
        m_fireInGrid--;
        addPoints(2);
    }

    public void droppedAnchor()
    {
        m_droppedAnchors++;
        m_anchorsInGrid--;
        addPoints(10);
    }

    public void droppedClock(int strengthLeft)
    {
        m_droppedClocks++;
        m_clocksInGrid--;
        addPoints(50 * strengthLeft);
    }

    public void explodedClock()
    {
        m_explodedClocks++;
        m_clocksInGrid--;
        addPoints(50);
    }
    
    public void explodedAnimal(int color)
    {
        m_explodedAnimals++;
        m_animalsInGrid--;
        addPoints(150);
    }

    public void explodedKnight()
    {
        m_explodedKnights++;
        m_knightsInGrid--;
        addPoints(20);
    }

    public void explodedDot(int color)
    {
        m_explodedDots[color]++;
        addPoints(1);
    }

    public void explodedWildcard()
    {
        m_explodedWildcards++;
        addPoints(1);
    }

    public void shortCircuitedLasers(int n)
    {
        m_shortCircuitedLasers += n;
        m_lasersInGrid -= n;
        addPoints(n * 25);
    }

    public void explodedLaser()
    {
        m_explodedLasers++;
        m_lasersInGrid--;
    }
    
    public void explodedMirror()
    {
        m_explodedMirrors++;
        m_mirrorsInGrid--;
    }
    
    public void explodedRocket()
    {
        m_explodedRockets++;
        m_rocketsInGrid--;
    }
    
    public void explodedAnchor()
    {
        m_explodedAnchors++;
        m_anchorsInGrid--;
    }

    public void addBird()
    {
        m_birds++;
    }
    
    public int getBirds()
    {
        return m_birds;
    }
    
    public int getScore()
    {
        return m_score;
    }
    
    protected void addPoints(int pts)
    {
        m_score += pts;
    }

    public int getMoves()
    {
        return m_moves;
    }
    
    public void addMove()
    {
        m_moves++;
    }

    public int[] getExplodedDots()
    {
        return m_explodedDots;
    }

    public int getExplodedAnimals()
    {
        return m_explodedAnimals;
    }

    public int getExplodedFire()
    {
        return m_explodedFire;
    }
    
    public int getExplodedIce()
    {
        return m_explodedIce;
    }
    
    public int getExplodedDoors()
    {
        return m_explodedDoors;
    }
    
    public int getExplodedCircuits()
    {
        return m_explodedCircuits;
    }

    public int getExplodedKnights()
    {
        return m_explodedKnights;
    }
    
    public int getExplodedLasers()
    {
        return m_explodedLasers;
    }
    
    public int getShortCircuitedLasers()
    {
        return m_shortCircuitedLasers;
    }
    
    public int getExplodedMirrors()
    {
        return m_explodedMirrors;
    }
    
    public int getExplodedRockets()
    {
        return m_explodedRockets;
    }
    
    public int getDroppedAnchors()
    {
        return m_droppedAnchors;
    }

    public int getDroppedClocks()
    {
        return m_droppedClocks;
    }
    
    public void generatedAnchor()
    {
        m_anchorsInGrid++;
    }
    
    public void generatedAnimal()
    {
        m_animalsInGrid++;
    }
    
    public void generatedFire()
    {
        m_fireInGrid++;
    }

    public void generatedKnight()
    {
        m_knightsInGrid++;
    }

    public void generatedClock()
    {
        m_clocksInGrid++;
    }

    public void generatedMirror()
    {
        m_mirrorsInGrid++;
    }

    public void generatedLaser()
    {
        m_lasersInGrid++;
    }

    public void generatedRocket()
    {
        m_rocketsInGrid++;
    }

    public int getAnchorsInGrid()
    {
        return m_anchorsInGrid;
    }
    
    public int getClocksInGrid()
    {
        return m_clocksInGrid;
    }
    
    public int getAnimalsInGrid()
    {
        return m_animalsInGrid;
    }

    public int getKnightsInGrid()
    {
        return m_knightsInGrid;
    }

    public int getDoorsInGrid()
    {
        return m_initialDoorCount - m_explodedDoors;
    }

    public int getIceInGrid()
    {
        return m_initialIceCount - m_explodedIce;
    }

    public int getFireInGrid()
    {
        return m_fireInGrid;
    }

    public int getMirrorsInGrid()
    {
        return m_mirrorsInGrid;
    }

    public int getRocketsInGrid()
    {
        return m_rocketsInGrid;
    }

    public int getInitialIce()
    {
        return m_initialIceCount;
    }

    public int getInitialDoors()
    {
        return m_initialDoorCount;
    }

    public int getInitialCircuits()
    {
        return m_initialCircuitCount;
    }

    public void initGrid(GridState state)
    {
        for (int row = 0; row < state.numRows; row++)
        {
            for (int col = 0; col < state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.item instanceof Anchor)
                {
                    m_anchorsInGrid++;
                }
                else if (cell.item instanceof Animal)
                {
                    m_animalsInGrid++;
                }
                else if (cell.item instanceof Knight)
                {
                    m_knightsInGrid++;
                }
                else if (cell.item instanceof Fire)
                {
                    m_fireInGrid++;
                }
                else if (cell.item instanceof Clock)
                {
                    m_clocksInGrid++;
                }
                else if (cell.item instanceof Laser)
                {
                    m_lasersInGrid++;
                }
                else if (cell.item instanceof Mirror)
                {
                    m_mirrorsInGrid++;
                }
                else if (cell.item instanceof Rocket)
                {
                    m_rocketsInGrid++;
                }
                
                if (cell instanceof Door)
                {
                    m_initialDoorCount++;
                }
                
                if (cell.ice > 0)
                {
                    m_initialIceCount++;
                }
            }
        }
        m_initialCircuitCount = state.getCircuitCount();
    }

    /** Fire or Animal ate something */
    public void ate(Item item)
    {
        if (item instanceof Fire)
        {
            m_fireInGrid--;
        }
        else if (item instanceof Anchor)
        {
            m_anchorsInGrid--;
        }
        else if (item instanceof Knight)
        {
            m_knightsInGrid--;
        }
        else if (item instanceof Clock)
        {
            m_clocksInGrid--;
        }
    }
}
