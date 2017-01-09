package com.enno.dotz.client;

import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.RandomItem;
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
    
    private int m_initialBubbleCount;
    private int m_explodedBubbles;
    
    private int m_initialCageCount;
    private int m_explodedCages;

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
    
    private int m_blockersInGrid;
    private int m_explodedBlockers;

    private int m_chestsInGrid;
    private int m_openedChests;
    
    private int m_explodedDominoes;
    
    private int m_birds;

    private int m_score;
    private int m_moves;    
    private int m_wordCount;
    
    public void foundWord()
    {
        m_wordCount++;
    }
    
    public int getWordCount()
    {
        return m_wordCount;
    }
    
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
        m_explodedDoors++;     // NOTE: 0 points
    }

    public void explodedBubble()
    {
        m_explodedBubbles++;
    }

    public void explodedCage()
    {
        m_explodedCages++;     // NOTE: 0 points
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
    
    public void explodedBlocker()
    {
        m_explodedBlockers++;
        m_blockersInGrid--;
        addPoints(10);
    }
    
    public void explodedAnchor()
    {
        m_explodedAnchors++;
        m_anchorsInGrid--;
    }
    
    public void explodedDomino()
    {
        m_explodedDominoes++;
        addPoints(1);
    }
    
    public void openedChest()
    {
        m_openedChests++;
        //TODO addPoints
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
    
    public int getChests()
    {
        return m_chestsInGrid;
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

    public int getExplodedCages()
    {
        return m_explodedCages;
    }
    
    public int getExplodedBubbles()
    {
        return m_explodedBubbles;
    }
    
    public int getExplodedCircuits()
    {
        return m_explodedCircuits;
    }

    public int getExplodedKnights()
    {
        return m_explodedKnights;
    }
    
    public int getExplodedDominoes()
    {
        return m_explodedDominoes;
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
     
    public int getExplodedBlockers()
    {
        return m_explodedBlockers;
    }
    
    public int getDroppedAnchors()
    {
        return m_droppedAnchors;
    }

    public int getDroppedClocks()
    {
        return m_droppedClocks;
    }
    
    public int getOpenedChests()
    {
        return m_openedChests;
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

    public void generatedBlocker()
    {
        m_blockersInGrid++;
    }

    public void generatedChest()
    {
        m_chestsInGrid++;
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

    public int getBubblesInGrid()
    {
        return m_initialBubbleCount - m_explodedBubbles;
    }
    
    public int getCagesInGrid()
    {
        return m_initialCageCount - m_explodedCages;
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

    public int getBlockersInGrid()
    {
        return m_blockersInGrid;
    }

    public int getInitialIce()
    {
        return m_initialIceCount;
    }

    public int getInitialDoors()
    {
        return m_initialDoorCount;
    }

    public int getInitialBubbles()
    {
        return m_initialBubbleCount;
    }

    public int getInitialCages()
    {
        return m_initialCageCount;
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
                countItem(cell.item);
                
                if (cell instanceof Door && !((Door) cell).isBlinking())
                {
                    m_initialDoorCount++;
                }
                else if (cell instanceof Cage && !((Cage) cell).isBlinking())
                {
                    m_initialCageCount++;
                }
                else if (cell instanceof Bubble)
                {
                    m_initialBubbleCount++;
                }
                
                if (cell.ice > 0)
                {
                    m_initialIceCount++;
                }
            }
        }
        m_initialCircuitCount = state.getCircuitCount();
    }

    protected void countItem(Item item)
    {
        if (item instanceof Anchor)
        {
            m_anchorsInGrid++;
        }
        else if (item instanceof Animal)
        {
            m_animalsInGrid++;
        }
        else if (item instanceof Knight)
        {
            m_knightsInGrid++;
        }
        else if (item instanceof Fire)
        {
            m_fireInGrid++;
        }
        else if (item instanceof Clock)
        {
            m_clocksInGrid++;
        }
        else if (item instanceof Laser)
        {
            m_lasersInGrid++;
        }
        else if (item instanceof Mirror)
        {
            m_mirrorsInGrid++;
        }
        else if (item instanceof Rocket)
        {
            m_rocketsInGrid++;
        }
        else if (item instanceof Blocker)
        {
            m_blockersInGrid++;
        }
        else if (item instanceof Chest)
        {
            m_chestsInGrid++;
            
            Chest chest = (Chest) item;
            if (!(chest.getItem() instanceof RandomItem))
                countItem(chest.getItem());
        }
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
