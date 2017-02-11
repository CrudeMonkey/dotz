package com.enno.dotz.client;

import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Spider;

public class Score
{
    private int[] m_explodedDots = new int[Config.MAX_COLORS];
    private int m_explodedWildcards; // e.g. by nuke (when not counting as a color)    
    
    private int m_fireInGrid;
    private int m_explodedFire;
    
    private int m_spidersInGrid;
    private int m_explodedSpiders;
    
    private int m_anchorsInGrid;
    private int m_droppedAnchors;
    private int m_explodedAnchors;
    
    private int m_diamondsInGrid;
    private int m_droppedDiamonds;
    
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

    private int m_zapBlockersInGrid;
    private int m_explodedZapBlockers;

    private int m_chestsInGrid;
    private int m_openedChests;
    
    private int m_explodedDominoes;
    
    private int m_birds;
    
    private int m_usedColorBombs;
    private int m_usedBombs;
    private int m_usedBlasters;
    private int m_usedWrappedDots;
    private int m_usedStriped;
    
    private int m_explodedCoins;
    
    private int m_score;
    private int m_moves;    
    private int m_wordCount;
    
    public static class ScoreState
    {
        protected int[] m_explodedDots = new int[Config.MAX_COLORS];
        protected int m_explodedWildcards; // e.g. by nuke (when not counting as a color)    
        
        protected int m_fireInGrid;
        protected int m_explodedFire;
        
        protected int m_spidersInGrid;
        protected int m_explodedSpiders;
        
        protected int m_anchorsInGrid;
        protected int m_droppedAnchors;
        protected int m_explodedAnchors;
        
        protected int m_diamondsInGrid;
        protected int m_droppedDiamonds;
        
        protected int m_clocksInGrid;
        protected int m_droppedClocks;
        protected int m_explodedClocks;

        protected int m_animalsInGrid;
        protected int m_explodedAnimals;
        
        protected int m_knightsInGrid;
        protected int m_explodedKnights;
        
        protected int m_initialDoorCount;
        protected int m_explodedDoors;
        
        protected int m_initialBubbleCount;
        protected int m_explodedBubbles;
        
        protected int m_initialCageCount;
        protected int m_explodedCages;

        protected int m_initialIceCount;
        protected int m_explodedIce;

        protected int m_initialCircuitCount;
        protected int m_explodedCircuits;
        
        protected int m_lasersInGrid;
        protected int m_explodedLasers;
        protected int m_shortCircuitedLasers;
        
        protected int m_mirrorsInGrid;
        protected int m_explodedMirrors;
        
        protected int m_rocketsInGrid;
        protected int m_explodedRockets;
        
        protected int m_blockersInGrid;
        protected int m_explodedBlockers;

        protected int m_zapBlockersInGrid;
        protected int m_explodedZapBlockers;

        protected int m_chestsInGrid;
        protected int m_openedChests;
        
        protected int m_explodedDominoes;
        
        protected int m_birds;
        
        protected int m_usedColorBombs;
        protected int m_usedBombs;
        protected int m_usedBlasters;
        protected int m_usedWrappedDots;
        protected int m_usedStriped;
        
        protected int m_explodedCoins;
        
        protected int m_score;
        protected int m_moves;    
        protected int m_wordCount;

    }

    public void copyState(UndoState undoState)
    {
        ScoreState state = new ScoreState();
        undoState.scoreState = state;
        
        for (int i = 0; i < Config.MAX_COLORS; i++)
            state.m_explodedDots[i] = m_explodedDots[i];
        
        state.m_explodedWildcards = m_explodedWildcards;
        
        state.m_fireInGrid = m_fireInGrid;
        state.m_explodedFire = m_explodedFire;
        
        state.m_spidersInGrid = m_spidersInGrid;
        state.m_explodedSpiders = m_explodedSpiders;
        
        state.m_anchorsInGrid = m_anchorsInGrid;
        state.m_droppedAnchors = m_droppedAnchors;
        state.m_explodedAnchors = m_explodedAnchors;
        
        state.m_diamondsInGrid = m_diamondsInGrid;
        state.m_droppedDiamonds = m_droppedDiamonds;
        
        state.m_clocksInGrid = m_clocksInGrid;
        state.m_droppedClocks = m_droppedClocks;
        state.m_explodedClocks = m_explodedClocks;

        state.m_animalsInGrid = m_animalsInGrid;
        state.m_explodedAnimals = m_explodedAnimals;
        
        state.m_knightsInGrid = m_knightsInGrid;
        state.m_explodedKnights = m_explodedKnights;
        
        state.m_initialDoorCount = m_initialDoorCount;
        state.m_explodedDoors = m_explodedDoors;
        
        state.m_initialBubbleCount = m_initialBubbleCount;
        state.m_explodedBubbles = m_explodedBubbles;
        
        state.m_initialCageCount = m_initialCageCount;
        state.m_explodedCages = m_explodedCages;

        state.m_initialIceCount = m_initialIceCount;
        state.m_explodedIce = m_explodedIce;

        state.m_initialCircuitCount = m_initialCircuitCount;
        state.m_explodedCircuits = m_explodedCircuits;
        
        state.m_lasersInGrid = m_lasersInGrid;
        state.m_explodedLasers = m_explodedLasers;
        state.m_shortCircuitedLasers = m_shortCircuitedLasers;
        
        state.m_mirrorsInGrid = m_mirrorsInGrid;
        state.m_explodedMirrors = m_explodedMirrors;
        
        state.m_rocketsInGrid = m_rocketsInGrid;
        state.m_explodedRockets = m_explodedRockets;
        
        state.m_blockersInGrid = m_blockersInGrid;
        state.m_explodedBlockers = m_explodedBlockers;

        state.m_zapBlockersInGrid = m_zapBlockersInGrid;
        state.m_explodedZapBlockers = m_explodedZapBlockers;

        state.m_chestsInGrid = m_chestsInGrid;
        state.m_openedChests = m_openedChests;
        
        state.m_explodedDominoes = m_explodedDominoes;
        
        state.m_birds = m_birds;
        
        state.m_usedColorBombs = m_usedColorBombs;
        state.m_usedBombs = m_usedBombs;
        state.m_usedBlasters = m_usedBlasters;
        state.m_usedWrappedDots = m_usedWrappedDots;
        state.m_usedStriped = m_usedStriped;
        
        state.m_explodedCoins = m_explodedCoins;
        
        state.m_score = m_score;
        state.m_moves = m_moves;    
        state.m_wordCount = m_wordCount;
    }
    
    public void restoreState(UndoState undoState)
    {
        ScoreState state = undoState.scoreState;
        
        for (int i = 0; i < Config.MAX_COLORS; i++)
            m_explodedDots[i] = state.m_explodedDots[i];
        
        m_explodedWildcards = state.m_explodedWildcards;
        
        m_fireInGrid = state.m_fireInGrid;
        m_explodedFire = state.m_explodedFire;
        
        m_spidersInGrid = state.m_spidersInGrid;
        m_explodedSpiders = state.m_explodedSpiders;
        
        m_anchorsInGrid = state.m_anchorsInGrid;
        m_droppedAnchors = state.m_droppedAnchors;
        m_explodedAnchors = state.m_explodedAnchors;
        
        m_diamondsInGrid = state.m_diamondsInGrid;
        m_droppedDiamonds = state.m_droppedDiamonds;
        
        m_clocksInGrid = state.m_clocksInGrid;
        m_droppedClocks = state.m_droppedClocks;
        m_explodedClocks = state.m_explodedClocks;

        m_animalsInGrid = state.m_animalsInGrid;
        m_explodedAnimals = state.m_explodedAnimals;
        
        m_knightsInGrid = state.m_knightsInGrid;
        m_explodedKnights = state.m_explodedKnights;
        
        m_initialDoorCount = state.m_initialDoorCount;
        m_explodedDoors = state.m_explodedDoors;
        
        m_initialBubbleCount = state.m_initialBubbleCount;
        m_explodedBubbles = state.m_explodedBubbles;
        
        m_initialCageCount = state.m_initialCageCount;
        m_explodedCages = state.m_explodedCages;

        m_initialIceCount = state.m_initialIceCount;
        m_explodedIce = state.m_explodedIce;

        m_initialCircuitCount = state.m_initialCircuitCount;
        m_explodedCircuits = state.m_explodedCircuits;
        
        m_lasersInGrid = state.m_lasersInGrid;
        m_explodedLasers = state.m_explodedLasers;
        m_shortCircuitedLasers = state.m_shortCircuitedLasers;
        
        m_mirrorsInGrid = state.m_mirrorsInGrid;
        m_explodedMirrors = state.m_explodedMirrors;
        
        m_rocketsInGrid = state.m_rocketsInGrid;
        m_explodedRockets = state.m_explodedRockets;
        
        m_blockersInGrid = state.m_blockersInGrid;
        m_explodedBlockers = state.m_explodedBlockers;

        m_zapBlockersInGrid = state.m_zapBlockersInGrid;
        m_explodedZapBlockers = state.m_explodedZapBlockers;

        m_chestsInGrid = state.m_chestsInGrid;
        m_openedChests = state.m_openedChests;
        
        m_explodedDominoes = state.m_explodedDominoes;
        
        m_birds = state.m_birds;
        
        m_usedColorBombs = state.m_usedColorBombs;
        m_usedBombs = state.m_usedBombs;
        m_usedBlasters = state.m_usedBlasters;
        m_usedWrappedDots = state.m_usedWrappedDots;
        m_usedStriped = state.m_usedStriped;
        
        m_explodedCoins = state.m_explodedCoins;
        
        m_score = state.m_score;
        m_moves = state.m_moves;    
        m_wordCount = state.m_wordCount;        
    }
    
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

    public void explodedSpider()
    {
        m_explodedSpiders++;
        m_spidersInGrid--;
        addPoints(5);
    }

    public void explodedCoins(int n)
    {
        m_explodedCoins += n;
        addPoints(n * 10);
    }

    public void droppedAnchor()
    {
        m_droppedAnchors++;
        m_anchorsInGrid--;
        addPoints(10);
    }

    public void droppedDiamond()
    {
        m_droppedDiamonds++;
        m_diamondsInGrid--;
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

    public void explodedLetter(int color)
    {
        m_explodedDots[color]++;
        // don't add points, those are added for the word
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
    
    public void explodedZapBlocker()
    {
        m_explodedZapBlockers++;
        m_zapBlockersInGrid--;
        addPoints(20);
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
    
    public void usedColorBomb()
    {
        m_usedColorBombs++;
    }
    
    public void usedBomb()
    {
        m_usedBombs++;
    }
    
    public void usedBlaster()
    {
        m_usedBlasters++;
    }
    
    public void usedWrappedDot()
    {
        m_usedWrappedDots++;
    }
    
    public void usedStriped()
    {
        m_usedStriped++;
    }
    
    public void openedChest()
    {
        m_openedChests++;
        m_chestsInGrid--;
        addPoints(5);
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
    
    public int getChestsInGrid()
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

    public int getExplodedCoins()
    {
        return m_explodedCoins;
    }

    public int getExplodedFire()
    {
        return m_explodedFire;
    }
    
    public int getExplodedSpiders()
    {
        return m_explodedSpiders;
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
    
    public int getExplodedZapBlockers()
    {
        return m_explodedZapBlockers;
    }
    
    public int getDroppedAnchors()
    {
        return m_droppedAnchors;
    }
    
    public int getDroppedDiamonds()
    {
        return m_droppedDiamonds;
    }

    public int getDroppedClocks()
    {
        return m_droppedClocks;
    }
    
    public int getOpenedChests()
    {
        return m_openedChests;
    }
    
    public int getUsedColorBombs()
    {
        return m_usedColorBombs;
    }
    
    public int getUsedBombs()
    {
        return m_usedBombs;
    }
    
    public int getUsedBlasters()
    {
        return m_usedBlasters;
    }
    
    public int getUsedWrappedDots()
    {
        return m_usedWrappedDots;
    }
    
    public int getUsedStriped()
    {
        return m_usedStriped;
    }
    
    public void generatedAnchor()
    {
        m_anchorsInGrid++;
    }
    
    public void generatedDiamond()
    {
        m_diamondsInGrid++;
    }
    
    public void generatedAnimal()
    {
        m_animalsInGrid++;
    }
    
    public void generatedFire()
    {
        m_fireInGrid++;
    }

    public void generatedSpider()
    {
        m_spidersInGrid++;
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

    public void generatedZapBlocker()
    {
        m_zapBlockersInGrid++;
    }

    public void generatedChest()
    {
        m_chestsInGrid++;
    }

    public int getAnchorsInGrid()
    {
        return m_anchorsInGrid;
    }
    
    public int getDiamondsInGrid()
    {
        return m_diamondsInGrid;
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

    public int getSpidersInGrid()
    {
        return m_spidersInGrid;
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

    public int getZapBlockersInGrid()
    {
        return m_zapBlockersInGrid;
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
                countItem(cell.item, 1);
                
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

    protected void countItem(Item item, int n)
    {
        if (item instanceof Anchor)
        {
            m_anchorsInGrid += n;
        }
        else if (item instanceof Diamond)
        {
            m_diamondsInGrid += n;
        }
        else if (item instanceof Animal)
        {
            m_animalsInGrid += n;
        }
        else if (item instanceof Knight)
        {
            m_knightsInGrid += n;
        }
        else if (item instanceof Fire)
        {
            m_fireInGrid += n;
        }
        else if (item instanceof Spider)
        {
            m_spidersInGrid += n;
        }
        else if (item instanceof Clock)
        {
            m_clocksInGrid += n;
        }
        else if (item instanceof Laser)
        {
            m_lasersInGrid += n;
        }
        else if (item instanceof Mirror)
        {
            m_mirrorsInGrid += n;
        }
        else if (item instanceof Rocket)
        {
            m_rocketsInGrid += n;
        }
        else if (item instanceof Blocker)
        {
            if (((Blocker) item).isZapOnly())
                m_zapBlockersInGrid += n;
            else
                m_blockersInGrid += n;
        }
        else if (item instanceof Chest)
        {
            m_chestsInGrid += n;
            
            Chest chest = (Chest) item;
            if (!(chest.getItem() instanceof RandomItem))
                countItem(chest.getItem(), n);
        }
    }
    
    /** Fire or Animal ate something */
    public void ate(Item item)
    {
        countItem(item, -1);
    }
    
    public void replace(Item from, Item to)
    {
        if (from != null)
            countItem(from, -1);
        
        countItem(to, 1);
    }

    public void addBubble()
    {
        m_initialBubbleCount++;
    }

    public void reduce(int n, String type, int color)
    {
        for (int i = 0; i < n; i++)
        {
            if (type.equals("diamond"))
            {
                m_droppedDiamonds++;
                addPoints(10);
            }
            else if (type.equals("dot"))
            {
                explodedDot(color);
            }
            else if (type.equals("anchor"))
            {
                m_droppedAnchors++;
                addPoints(10);
            }
            else if (type.equals("fire"))
            {
                m_explodedFire++;
                addPoints(2);
            }
            else if (type.equals("spider"))
            {
                m_explodedSpiders++;
                addPoints(5);
            }
        }
    }
}
