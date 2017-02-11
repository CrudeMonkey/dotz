package com.enno.dotz.client;

import java.util.List;
import java.util.Random;

import com.enno.dotz.client.BoostPanel.BoostState;
import com.enno.dotz.client.Cell.CellState;
import com.enno.dotz.client.Score.ScoreState;
import com.enno.dotz.client.ScorePanel.GoalItemState;

public class UndoState
{
    // generator: random
    public Random rnd;
    
    // gridState: cells
    
    // Door: direction, strength, controller(tick)
    // Cage: strength, controller(tick)
    // Bubble: popped
    // Circuit: state (on,off,done)
    // Machine: stage
    // all: ice
    
    public CellState[] grid;
    
    // score
    public ScoreState scoreState;
    
    // scorePanel: GoalItem state
    public List<GoalItemState> goalItems;

    // statsPanel: score, time (timer), moves, bombWentOff?
    public int elapsedTime;
    public boolean bombWentOff;

    // boostPanel: boostButtons(item, n)
    public List<BoostState> boosts;

    // Word List in Word Mode
    public String word;    
    public String wordToGuess;

    public boolean[] circuitsDone;      // used by Circuits
}
