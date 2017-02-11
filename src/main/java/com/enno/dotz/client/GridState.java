package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.CellState;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Machine.MachineType;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Slot;
import com.enno.dotz.client.Circuits.Circuit;
import com.enno.dotz.client.Controller.Controllable;
import com.enno.dotz.client.Conveyors.Conveyor;
import com.enno.dotz.client.Conveyors.ConveyorException;
import com.enno.dotz.client.DotzGridPanel.EndOfLevel;
import com.enno.dotz.client.Rewards.Reward;
import com.enno.dotz.client.Rewards.RewardStrategy;
import com.enno.dotz.client.SlotMachines.RewardInfo;
import com.enno.dotz.client.SlotMachines.SlotException;
import com.enno.dotz.client.SlotMachines.SlotMachine;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.AnimList;
import com.enno.dotz.client.anim.Explosions;
import com.enno.dotz.client.anim.FireRocket;
import com.enno.dotz.client.anim.GetTransitions;
import com.enno.dotz.client.anim.GrowFire;
import com.enno.dotz.client.anim.MoveAnimals;
import com.enno.dotz.client.anim.MovePacmans;
import com.enno.dotz.client.anim.MoveSpiders;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.RadioActives;
import com.enno.dotz.client.anim.ShowDescription;
import com.enno.dotz.client.anim.ShowWordCallback;
import com.enno.dotz.client.anim.Transition.ArchTransition;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.anim.TransitionList.NukeTransitionList;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.CallbackChain;
import com.enno.dotz.client.util.CallbackChain.Callback;
import com.enno.dotz.client.util.Debug;
import com.enno.dotz.client.util.SerialAnimation;

public class GridState
{
    private static Logger s_log = Logger.getLogger(GridState.class.getName());
    
    private Context ctx;
    private Config cfg;
    
    public int numRows, numColumns;
    private Cell[] m_grid;

    private int m_lastExplodedFire;
    
    private List<LazySusan> m_lazySusans = new ArrayList<LazySusan>();
    private Circuits m_circuits;
    private Conveyors m_conveyors;
    private SlotMachines m_slotMachines;
    private List<RewardStrategy> m_rewardStrategies;
    
    public EndOfLevel endOfLevel;

    public GridState(int numColumns, int numRows)
    {
        this.numRows = numRows;
        this.numColumns = numColumns;
        
        m_grid = new Cell[numColumns * numRows];
    }
    
    public void init(Context ctx, boolean replaceRandom)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell c = cell(col, row);
                c.init(ctx);
                c.initGraphics(col, row, x(col), y(row));
                
                if (c.item != null)
                {
                    if (replaceRandom && (c.item instanceof RandomItem))
                    {
                        RandomItem rnd = (RandomItem) c.item;
                        c.item = ctx.generator.getNextItem(ctx, true, rnd.isRadioActive(), c.item.isStuck());
                    }
                    else
                    {
                        if (c.item instanceof Dot)
                        {
                            Dot dot = (Dot) c.item;
                            if (ctx.generator.generateLetters)
                            {
                                if (!dot.isLetter())
                                    dot.setLetter(ctx.generator.nextLetter());                                
                            }
                            else
                            {
                                if (dot.isLetter())
                                    dot.setLetter(null);
                            }
                        }
                        c.item.init(ctx);
                    }
                }
            }
        }
        
        for (LazySusan su : m_lazySusans)
        {
            su.initGraphics(ctx);
        }
        
        m_circuits = new Circuits();
        m_circuits.makeConnections(this);
        
        try
        {
            m_slotMachines = new SlotMachines();
            m_slotMachines.locateSlotMachines(this);
            
            if (!ctx.isEditing && !ctx.isPreview)
                m_slotMachines.initSlots();
        }
        catch (SlotException e)
        {
            Debug.p("unexpected SlotMachine problem", e);
        }
        
        try
        {
            m_conveyors = new Conveyors(this);
        }
        catch (ConveyorException e)
        {
            Debug.p("unexpected conveyor problem", e);
        }
        
        m_rewardStrategies = RewardStrategy.parseStrategies(ctx.generator.rewardStrategies);
    }

    public SlotMachines getSlotMachines()
    {
        return m_slotMachines;
    }
    
    public int getCircuitCount()
    {
        return m_circuits.size();
    }

    public boolean hasConveyors()
    {
        return m_conveyors.size() > 0;
    }
    
    public final boolean isValidColumn(int col)
    {
        return col >= 0 && col < numColumns;
    }
    
    public final boolean isValidRow(int row)
    {
        return row >= 0 && row < numRows;
    }
    
    public final boolean isValidCell(int col, int row)
    {
        return isValidColumn(col) && isValidRow(row);
    }
    
    public final Cell cell(int col, int row)
    {
        return m_grid[row * numColumns + col];
    }
    
    public final Cell cell(Pt p)
    {
        return cell(p.col, p.row);
    }
    
    public void setCell(int col, int row, Cell cell)
    {
        cell.col = col;
        cell.row = row;
        m_grid[row * numColumns + col] = cell;
    }

    public void doInitialTransitions(final Runnable startLevelCallback)
    {
        AnimList list = getInitialTransitions(ctx.dotLayer, cfg.dropDuration);
        list.add(new Callback() {
            public void run()
            {
                // There could be anchors near the bottom
                AnimList after = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);
                after.animate(new Runnable() {
                    public void run()
                    {
                        activateLasers(true);
                        updateScore();
                        doNext();
                    }
                });
            }
        });
        list.add(transitions()); // for swap mode
        list.add(explodeLoop(false, false)); // don't check clocks
        
        String desc = ctx.cfg.description;
        if (desc != null && desc.length() > 0)
        {
            list.add(new ShowDescription(desc, ctx));
        }
        list.add(new Callback() {
            @Override
            public void run()
            {
                activateLasers(true);
                checkEndOfLevel(startLevelCallback);
            }
        });
        list.run();
    }

    public void startOfMove()
    {
        m_slotMachines.startOfMove();
    }

//    public void processChain(final Runnable nextMoveCallback)
//    {
//        if (ctx.generator.swapMode)
//            processSwapChain(new UserAction(), null, nextMoveCallback);
//        else
//            processChain(new UserAction(), nextMoveCallback);
//    }
    
    public void processChain(final Runnable nextMoveCallback)
    {
        processChain(new UserAction(), nextMoveCallback);
    }
    
    public void processChain(final UserAction action, final Runnable nextMoveCallback)
    {
        if (ctx.generator.swapMode)
        {
            processSwapChain(action, action.swapMatches, nextMoveCallback);
            return;
        }
        
        if (action.isWordMode) // && !ctx.generator.findWords)
        {
            ctx.lastWord = action.word;
            new ShowWordCallback(ctx, action.word, action.wordPoints, action.color)
            {
                @Override
                protected void done()
                {
                    processChain2(action, nextMoveCallback);
                }
            };
        }
        else if (action.isEggMode)
        {
            GetSwapMatches.doEggs(ctx, action.cells, new Runnable() {
                @Override
                public void run()
                {
                    action.cells.clear(); // don't explode these cells - already merged eggs
                    processChain2(action, nextMoveCallback);
                }
            });
        }
        else
            processChain2(action, nextMoveCallback);
    }
    
    Callback explodeLoop(boolean checkClocks, boolean isEndOfLevel)
    {
        return new ExplodeLoop(checkClocks, isEndOfLevel);
    }
    
    private void processChain2(UserAction action, final Runnable nextMoveCallback)
    {
        ctx.score.addMove();
        
        m_lastExplodedFire = ctx.score.getExplodedFire();
        if (action.dousedFire)
            m_lastExplodedFire--;   // used Drop to douse fire
        
        clearStunnedAnimals();
        activateLasers(false);
        
        Collection<Cell> area = null;
        if (action.is_square)
        {
            ctx.generator.excludeDotColor(action.color);
            area = getArea(action.cells);
        }
        
        boolean hasBubbles = action.cells.containsBubble();
        
        Rewards rewards = new Rewards(m_rewardStrategies);
        explodeCells(action, area, rewards);
        
        if (action.cells.size() > 0)   // could be a mirror flip
        {
            if (hasBubbles)
                Sound.BUBBLE.play();
            else
                Sound.DROP.play();
            
            ctx.scorePanel.madeChain(action.cells.size(), action.color);
            if (action.isWordMode)
            {
                ctx.score.addPoints(action.wordPoints);
                
                if (ctx.generator.findWords)
                {
                    if (ctx.findWordList.foundWord(action.word))
                        ctx.score.foundWord();
                }
                else
                    ctx.score.foundWord();
            }
        }
        
        ctx.dotLayer.redraw();
        updateScore();
        
        AnimList list = new AnimList();
        if (rewards != null && rewards.hasRewards())
            rewards.addAnimations(list, this);
        
        list.add(new GetTransitions(ctx).getCallback(ctx.dotLayer, cfg.dropDuration));        
        list.add(explodeLoop(false, false)); // don't check clocks
        list.add(machines());
        list.add(slotMachines());
        list.add(radioActives());
        list.add(activateControllers());
        list.add(moveBeasts(action));
        list.add(moveSusans());
        list.add(transitions());             // susans can leave empty cells
        list.add(explodeLoop(true, false));  // check clocks also and lasers
        list.add(new Callback() {
            @Override
            public void run()
            {
                ctx.generator.dontExcludeDotColor();
                rotateDoors();
                activateLasers(true);

                //Debug.p("next move");
                ctx.dotLayer.redraw();
                
                System.gc();
                
                checkEndOfLevel(nextMoveCallback);
            }
        });
        list.run();
    }
    
    public void processSwapChain(final UserAction action, GetSwapMatches matches, final Runnable nextMoveCallback)
    {
        ctx.score.addMove();
        
        m_lastExplodedFire = ctx.score.getExplodedFire();
        if (action.dousedFire)
            m_lastExplodedFire--;   // used Drop to douse fire
        
        clearStunnedAnimals();
        activateLasers(false);
        
        if (matches != null)
        {
            matches.animate(new Runnable() {
                @Override
                public void run()
                {
                    processSwapChain2(action, nextMoveCallback);
                }
            });
        }
        else
        {
            processSwapChain2(action, nextMoveCallback);
        }
    }
    
    protected void processSwapChain2(UserAction action, final Runnable nextMoveCallback)
    {
        AnimList list = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);        
        
        list.add(explodeLoop(false, false)); // don't check clocks
        list.add(activateControllers());
        list.add(machines());
        list.add(slotMachines());
        list.add(radioActives());        
        list.add(moveBeasts(action));
        list.add(moveSusans());
        list.add(transitions());             // susans can leave empty cells
        list.add(explodeLoop(true, false));  // check clocks also and lasers
        list.add(new Callback() {
            @Override
            public void run()
            {
                ctx.generator.dontExcludeDotColor();
                rotateDoors();
                activateLasers(true);

                //Debug.p("next move");
                ctx.dotLayer.redraw();
                
                System.gc();
                
                checkEndOfLevel(nextMoveCallback);
            }
        });
        list.run();
    }
    
    public Callback activateControllers()
    {
        return new Callback() {
            @Override
            public void run()
            {
                for (int row = 0; row < numRows; row++)
                {
                    for (int col = 0; col < numColumns; col++)
                    {
                        Cell c = cell(col, row);
                        if (c instanceof Controllable)
                            ((Controllable) c).tick();
                    }
                }
                ctx.doorLayer.redraw();
                
                doNext();
            }
        };
    }
    
    public void fireRocket(Cell cell, Runnable nextMoveCallback)
    {
        new FireRocket(cell, nextMoveCallback, ctx);
    }
    
    private void checkEndOfLevel(Runnable nextMoveCallback)
    {
        Reshuffle reshuffle;
        if (ctx.scorePanel.isGoalReached())
        {
            final int time = ctx.statsPanel.elapsedTime();            
            final int moves = ctx.score.getMoves();

            Sound.WIN_LEVEL.play();

            Runnable done = new Runnable() {
                @Override
                public void run()
                {
                    int score = ctx.score.getScore();
                    endOfLevel.goalReached(time, score, moves, cfg.id);
                }
            };
            int movesLeft = ctx.cfg.goals.m_maxMoves > 0 ? ctx.cfg.goals.m_maxMoves - moves : 0;
            if (movesLeft > 0)
            {
                animateMovesLeft(movesLeft, done);
                return;
            }
            else
                done.run();
        }
        else if (ctx.statsPanel.isOutOfMoves())
        {
            Sound.OUT_OF_MOVES.play();
            endOfLevel.failed("You're out of moves.");
        }
        else if (ctx.statsPanel.isOutOfTime())
        {
            Sound.OUT_OF_TIME.play();
            endOfLevel.failed("You're out of time.");
        }
        else if (ctx.statsPanel.isBombWentOff())
        {
            Sound.OUT_OF_MOVES.play();
            endOfLevel.failed("A bomb went off.");
        }
        else if ((reshuffle = mustReshuffle()) != null)
        {
            reshuffle.reshuffle(nextMoveCallback, new Runnable() {
                @Override
                public void run()
                {
                    Sound.OUT_OF_MOVES.play();
                    endOfLevel.failed("No possible moves left. Reshuffle failed.");
                }
            });
        }
        else
            nextMoveCallback.run();
    }
    
    protected Reshuffle mustReshuffle()
    {
        if (ctx.generator.generateLetters)
            return null;
        
        Reshuffle r = ctx.generator.swapMode ? new ReshuffleSwap(ctx) : new Reshuffle(ctx);
        if (r.mustReshuffle())            
            return r;
        
        return null;
    }
    
    /** User clicked YinYang */
    public void reshuffle(Runnable nextCallback)
    {
        Reshuffle r = ctx.generator.swapMode ? new ReshuffleSwap(ctx) : new Reshuffle(ctx);
        r.forceReshuffle(nextCallback);
    }
    
    /**
     * Animates the final explosions if there are any moves left when the goals are reached.
     * @param movesLeft
     * @param done
     */
    protected void animateMovesLeft(int movesLeft, Runnable done)
    {
        AnimList list = new AnimList();
        for (int i = 0; i < movesLeft; i++)
        {
            list.add(new Callback() {
                @Override
                public void run()
                {
                    if (!ctx.killed)
                    {
                        Cell c = getRandomCellForFinalExplosion();
                        if (c != null)
                        {
                            c.explode(null, 0);
                            addExplody(c);
                            
                            ctx.score.addMove();
                            updateScore();
                        }
                    }
                    doNext();
                }
            });
            list.add(explodeLoop(false, true));       // don't check clocks, endOfLevel=true
            list.add(transitions());
        }
        list.animate(done);
    }
    
    protected Cell getRandomCellForFinalExplosion()
    {
        Random r = ctx.generator.getRandom();
        int i = 0;
        while (i < 200)
        {
            int col = r.nextInt(numColumns);
            int row = r.nextInt(numRows);
            Cell c = cell(col, row);
            if (!c.isLocked() && (c.item instanceof Dot || c.item instanceof Wild || c.item instanceof Domino))
                return c;
            
            i++;
        }
        return null;
    }

    public Cell getRandomDotCell()
    {
        int i = 0;
        Random rnd = ctx.generator.getRandom();
        while (i < 200)
        {
            int col = rnd.nextInt(numColumns);
            int row = rnd.nextInt(numRows);
            Cell c = cell(col, row);
            if (!c.isLocked() && c.item instanceof Dot) //TODO could use Item.canBeReplaced()
                return c;
            
            i++;
        }
        return null;
    }
    
    public void activateLasers(boolean active)
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell c = cell(col, row);
                if (c.item instanceof Laser)
                    ((Laser) c.item).activateBeam(active, col, row, ctx);
            }
        }
        ctx.laserLayer.redraw();
    }

    protected void rotateDoors()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell c = cell(col, row);
                if (c instanceof Door)
                    ((Door) c).tickRotateDoor();
            }
        }
        ctx.doorLayer.redraw();
    }
    
    protected static boolean canConnect(Item a, Item b, int dcol, int drow)
    {
        boolean aDot = (a instanceof Dot || a instanceof Animal);
        if (aDot && b instanceof Wild)
            return true;
        
        boolean bDot = (b instanceof Dot || b instanceof Animal);
        if (bDot && a instanceof Wild)
            return true;
        
        if (aDot && bDot)
            return a.getColor() == b.getColor();
        
        if (Domino.canConnect(a, b, dcol, drow))
            return true;
        
        return false;
    }
    
    public class ExplodeLoop extends Callback
    {
        private boolean m_checkClocks;
        private boolean m_endOfLevel;
        
        public ExplodeLoop(boolean checkClocks, boolean isEndOfLevel)
        {
            m_checkClocks = checkClocks;
            m_endOfLevel = isEndOfLevel;
        }
        
        @Override
        public void run()
        {
            if (m_checkClocks && !m_endOfLevel)
            {
                List<Cell> bombsWentOff = clockTick();
                if (bombsWentOff != null)
                {
                    // bomb went off
                    ctx.statsPanel.setBombWentOff();
                    new ExplodeBombs(bombsWentOff)
                    {
                        public void done()
                        {
                            ExplodeLoop.this.doNext();
                        }
                    };
                    return;
                }
            }
            
            if (!ctx.killed)
            {
                new ExplodeLasers()
                {
                    public void done()
                    {
                        run2();
                    }
                };
                return;
            }
            run2();
        }
        
        protected void run2()        
        {
            // Check if circuits are turned off
            final List<Circuit> explodedCircuits = new ArrayList<Circuit>();
            if (!ctx.killed)
            {
                m_circuits.checkConnections(explodedCircuits);
                if (explodedCircuits.size() > 0)
                {
                    ctx.score.explodeCircuits(explodedCircuits.size());
                    Sound.CIRCUIT_OFF.play();
                    
                    for (Circuit circuit : explodedCircuits)
                    {
                        for (CircuitCell c : circuit)
                        {
                            if (c.item != null)
                            {
                                c.zap();
                                if (c.item == null)
                                {
                                    addExplody(c);                            
                                }
                            }                            
                        }
                    }
                    ctx.backgroundLayer.redraw();
                }
            }
            
            if ((!ctx.killed || m_endOfLevel) && hasExplodies())
            {
                CallbackChain chain = new CallbackChain()
                {
                    @Override
                    public void done()
                    {
                        ExplodeLoop.this.run(); // loop
                    }
                };                
                chain.add(explosions());
                chain.add(machines());
                chain.add(slotMachines());
                chain.add(transitions());
                chain.run();
                return;
            }
            else
                doNext();
        }
        
        protected List<Cell> clockTick()
        {
            boolean clockWent = false;
            
            List<Cell> bombs = null;
            
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    Cell cell = cell(col, row);
                    if (cell.item instanceof Clock)
                    {
                        if (((Clock) cell.item).tick())
                        {
                            clockWent = true;
                            cell.item.removeShapeFromLayer(ctx.dotLayer);
                            addExplody(cell);
                        }
                    }
                    else if (cell.item instanceof DotBomb)
                    {
                        if (((DotBomb) cell.item).tick())
                        {
                            // bomb explodes
                            if (bombs == null)
                                bombs = new ArrayList<Cell>();
                            bombs.add(cell);
                        }
                    }
                }
            }
            
            if (clockWent)
                Sound.CLOCK_WENT.play();
            
            return bombs;
        }
        
        protected boolean hasExplodies()
        {
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    Cell a = cell(col, row);
                    if (a.item == null)
                        continue;
                    
                    if (a.item instanceof Explody || a.item.isArmed())
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class SwapCombo extends ArrayList<Cell>
    {
        public static final int TEE = 1; // T, L or +
        public static final int THREE = 3;
        public static final int FOUR = 4;
        public static final int FIVE = 5;
        public static final int COLOR_BOMB = 6;
        public static final int TOTAL_BOMB = 7;
        public static final int WRAP_BOMB = 8;
        public static final int EXPLODY_5 = 9;
        public static final int KNIGHT = 10;
        public static final int EGG = 11;
        public static final int CRACKED_EGG = 12;
        public static final int STRIPED_BOMB = 13;
        public static final int WIDE_STRIPED = 14;

        private int m_type;
        private Cell m_special;
        private int m_points;
        private int m_color;
        private boolean m_vertical;
        
        public void setType(int type)
        {
            m_type = type;
            
            addPoints(type);
            
            if (m_type != KNIGHT && m_type != EGG && m_type != CRACKED_EGG)
                m_color = determineColor();
        }
        
        protected void addPoints(int type)
        {
            switch (type)
            {
                case FIVE: m_points += 120; break;
                case FOUR: m_points += 80; break;
                case TEE: m_points += 50; break;
                default: m_points += 10; break;
            }
        }

        public boolean matchesColor(Integer color)
        {
            if (color == Config.WILD_ID)
                return true;
            
            for (Cell c : this)
            {
                if (color != c.item.getColor() && c.item.getColor() != Config.WILD_ID)
                    return false;
            }
            return true;
        }

        public void setSpecial(Cell c)
        {
            m_special = c;
        }

        public boolean overlaps(SwapCombo combo)
        {
            if (m_type == KNIGHT)
                return false;
            
            for (Cell c : combo)
            {
                if (contains(c))
                    return true;
            }
            return false;
        }
        
        public void merge(SwapCombo combo)
        {
            for (Cell c : combo)
            {
                if (!contains(c))
                    add(c);
            }
        }

        public boolean mergeTL(SwapCombo combo)
        {
            // Merging two length 3 combos
            Cell special = null;
            int type = 0;
            if (get(0) == combo.get(0) || get(0) == combo.get(2))
            {
                type = TEE;// L
                special = get(0);
            }
            else if (get(2) == combo.get(0) || get(2) == combo.get(2))
            {
                type = TEE;// L
                special = get(2);
            }
            else if (get(1) == combo.get(0) || get(1) == combo.get(2))
            {
                type = TEE;// T
                special = get(1);
            }
            else if (get(0) == combo.get(1) || get(2) == combo.get(1))
            {
                type = TEE; // T
                special = combo.get(1);
            }
            else if (get(1) == combo.get(1))
            {
                type = TEE; // +
                special = get(1);
            }
            if (type == 0)
                return false;
            
            setSpecial(special);
            setType(type);
            merge(combo);
            
            return true;
        }

        public void setVertical(boolean vertical)
        {
            m_vertical = vertical;
        }

        public int getType()
        {
            return m_type;
        }
        
        public Cell getSpecial()
        {
            return m_special;
        }

        public int getColor()
        {
            return m_color;
        }
        
        private int determineColor()
        {
            for (Cell c : this)
            {
                Integer color = c.item.getColor();
                if (color != Config.WILD_ID)
                    return color;
            }
            return Config.WILD_ID;
        }

        public boolean isAllWild()
        {
            return getColor() == Config.WILD_ID;
        }

        public boolean isVertical()
        {
            return m_vertical;
        }
    }
    
    public abstract class ExplodeBombs extends NukeTransitionList
    {
        public ExplodeBombs(List<Cell> bombsWentOff)
        {
            super("explodeBombs", ctx, 1000);
            
            for (final Cell c : bombsWentOff)
            {
                add(new ExplodeBomb(c));
            }
            
            run();
        }
        
        public class ExplodeBomb implements IAnimationCallback
        {
            private double m_dt = 0.02;
            private List<Ring> m_rings = new ArrayList<Ring>();
            private double m_x;
            private double m_y;
            
            public ExplodeBomb(Cell cell)
            {
                m_x = x(cell.col);
                m_y = y(cell.row);
            }
            
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle)
            {
                Sound.BOMB_WENT.play();
            }
            
            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                double pct = animation.getPercent();
                int n = (int) (pct / m_dt);
                for (int i = m_rings.size(); i < n; i++)
                {
                    m_rings.add(new Ring(m_dt * i));
                }
                for (Ring r : m_rings)
                {
                    r.update(pct);
                }
            }
            
            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle)
            {
                for (Ring r : m_rings)
                {
                    ctx.nukeLayer.remove(r);
                }
            }
            
            public class Ring extends Circle
            {
                private double m_start;

                public Ring(double start)
                {
                    super(1);
                    setStrokeColor(ColorName.BLACK);
                    setStrokeWidth(4);
                    setX(m_x);
                    setY(m_y);
                    ctx.nukeLayer.add(this);
                    
                    m_start = start;
                }
                
                public void update(double pct)
                {
                    double d = pct - m_start;
                    setRadius(1000 * d);
                }
            }
        }
    }
    
    public abstract class ExplodeLasers extends NukeTransitionList
    {
        private List<Pt> m_list;
        private ArrayList<Laser> m_lasers;

        public ExplodeLasers()
        {
            super("explodeLasers", ctx, 2400);
            
            m_lasers = new ArrayList<Laser>();            
            m_list = Laser.getExplodingLasers(GridState.this, m_lasers);
            if (m_list.size() == 0)
            {
                done();
                return;
            }
            
            final GridState state = GridState.this;            
            
            add(new IAnimationCallback()
            {                
                @Override
                public void onStart(IAnimation animation, IAnimationHandle handle)
                {
                    activateLasers(true);
                    Sound.LASER_BREAKING.play();
                }
                
                @Override
                public void onFrame(IAnimation animation, IAnimationHandle handle)
                {
                    long t = System.currentTimeMillis();
                    for (Laser a : m_lasers)
                    {
                        a.overload(t);
                    }
                }
                
                @Override
                public void onClose(IAnimation animation, IAnimationHandle handle)
                {
                    Sound.LASER_BROKE.play();
                    activateLasers(false);
                    
                    for (Pt p : m_list)
                    {
                        Cell cell = state.cell(p.col, p.row);
                        cell.item.removeShapeFromLayer(ctx.dotLayer);
                        addExplody(cell);
                    }
                    
                    ctx.score.shortCircuitedLasers(m_list.size());
                    updateScore();
                }
            });
            
            run();
        }
    }
    
    public void explodeCells(UserAction action, Collection<Cell> area, Rewards rewards)
    {
        CellList chain = action.cells;
        // How many to subtract from Animals
        int chainSize = chain.size();
        if (action.is_square)
            chainSize--;
        
        rewards.addChainSizeReward(chain, chainSize);
        
        if (action.isWordMode && !ctx.generator.removeLetters)
        {
            debombify(chain);
            explodeNeighbors(chain);
            return;
        }
        
        Set<Cell> exploded = new HashSet<Cell>();
        for (Cell cell : chain)
        {
            if (!exploded.contains(cell))
            {
                exploded.add(cell);
                cell.explode(action.color, chainSize);
            }
        }

        if (action.isKnightMode)
        {
            Cell knightCell = chain.get(0);
            if (knightCell.item != null)
            {
                // Knight is still alive
                Cell lastCell = chain.get(chain.size() - 1);
                if (lastCell.item == null)
                {
                    // Move knight to the last cell
                    lastCell.item = knightCell.item;
                    knightCell.item = null;
                    lastCell.item.moveShape(x(lastCell.col), y(lastCell.row));
                }
            }
        }        
        else if (action.is_square)
        {            
            // Explode other cells with the same color
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    Cell cell = cell(col, row);
                    Item item = cell.item;
                    if (item != null && !exploded.contains(cell) && cell.canExplode(action.color))
                    {
                        exploded.add(cell);
                        cell.explode(action.color, chainSize);
                    }
                }
            }
            
            if (area != null)
            {
                for (Cell cell : area)
                {
                    if (!exploded.contains(cell) && cell.canBeNuked())
                    {
                        exploded.add(cell);
                        cell.explode(action.color, chainSize); // count wildcards as selected color
                    }
                    
                    if (cell.canBeFilled())
                    {
                        addExplody(cell);
                    }
                }
            }
        }
        
        // Doors with colored item, must much the color
        explodeNeighbors(exploded, action.color);
    }
    
    private void debombify(CellList chain)
    {
        for (Cell cell : chain)
            debombify(cell);
    }

    private void debombify(Cell cell)
    {
        if (cell.item instanceof DotBomb)
        {
            DotBomb bomb = (DotBomb) cell.item;
            bomb.removeShapeFromLayer(ctx.dotLayer);
            
            Dot dot = bomb.getDot();
            dot.init(ctx);
            dot.moveShape(x(cell.col), y(cell.row));
            dot.addShapeToLayer(ctx.dotLayer);            
        }
    }

    private void clearStunnedAnimals()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Item item = cell(col, row).item;
                if (item instanceof Animal)
                    ((Animal) item).setStunned(false);
            }
        }
    }

    protected void updateScore()
    {
        ctx.scorePanel.update();
        ctx.statsPanel.update();
    }
    
    protected Callback transitions()
    {
        return transitions(null);
    }
    
    protected Callback transitions(final Runnable whenDone)
    {
        if (ctx.generator.swapMode)
        {
            return new Callback() {
                public void run()
                {
                    final Runnable doNext = whenDone != null ? whenDone : new Runnable() {
                        @Override
                        public void run()
                        {
                            doNext();
                        }
                    };
                    
                    AnimList after = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);
                    after.animate(new Runnable() {
                        public void run()
                        {
                            updateScore();
                            
                            GetSwapMatches matches = new GetSwapMatches(ctx);
                            if (!matches.getTransitions())
                            {
                                doNext.run();
                            }
                            else
                            {
                                matches.animate(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        transitions(doNext).run();
                                    }
                                });
                            }
                        }
                    });
                }
            };
        }
        else
        {
            return new Callback() {
                public void run()
                {
                    AnimList after = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);
                    after.animate(new Runnable() {
                        public void run()
                        {
                            updateScore();
                            doNext();
                        }
                    });
                }
            };
        }
    }
    
    protected AnimList getInitialTransitions(Layer layer, double dropDuration)
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                final Item item = cell(col, row).item;
                if (item == null)
                    continue;
                
                item.moveShape(x(col), y(row));
                item.addShapeToLayer(ctx.dotLayer);                
            }
        }
        
        AnimList animList = new AnimList() {
            @Override
            public void run()
            {
                ctx.iceLayer.redraw();
                ctx.doorLayer.redraw();
                
                super.run();
            }
        };
        
        return animList;
    }
    
    protected AnimList getInitialTransitionsOld(Layer layer, double dropDuration)
    {
        TransitionList[] transList = new TransitionList[numRows];
        for (int row = 0; row < numRows; row++)
        {
            transList[row] = new TransitionList("drop", layer, dropDuration);
        }
        
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                final Item item = cell(col, row).item;
                if (item == null)
                    continue;
                
                for (int i = 0; i <= row; i++)
                {
                    final boolean addShape = i == 0;
                    //Debug.p("add drop " + col + "," + (i-1));
                    
                    transList[numRows - 1 - row + i].add(new DropTransition(x(col), y(i - 1), x(col), y(i), item) {
                        public void afterStart()
                        {
                            if (addShape)
                                item.addShapeToLayer(ctx.dotLayer);
                        }
                    });
                }
            }
        }
        
        AnimList animList = new AnimList() {
            @Override
            public void run()
            {
                ctx.iceLayer.redraw();
                ctx.doorLayer.redraw();
                
                super.run();
            }
        };
        for (int row = 0; row < numRows; row++)
        {
            TransitionList list = transList[row];
            if (list.getTransitions().size() > 0)
                animList.add(list);
        }
        return animList;
    }
    
    public double size()
    {
        return cfg.size;
    }
    
    public final double x(int col)
    {
        return ctx.gridDx + col * cfg.size + cfg.size / 2.0;
    }
    
    public final double y(int row)
    {
        return ctx.gridDy + row * cfg.size + cfg.size / 2.0;
    }
    
    public int col(int x)
    {
        return (x - ctx.gridDx) / cfg.size;
    }
    
    public int row(int y)
    {
        return (y - ctx.gridDy) / cfg.size;
    }

    public static void p(String s)
    {
        s_log.info(s);
    }

    public Collection<Cell> getArea(List<Cell> cells)
    {
        //Debug.p("loop " + Debug.str(cells));

        Set<Cell> insiders = new HashSet<Cell>();
        int n = cells.size();
        if (n < 8)
            return null;
        
        // Find closed loop
        int p = -1;
        int loopSize = -1;
        Cell lastCell = cells.get(n - 1);
        for (int i = 0; i < n; i++)
        {
            if (cells.get(i) == lastCell)
            {
                p = ++i;
                loopSize = n - p;
                
                while (i < n)
                {
                    // Add loop cells
                    //insiders.add(cells.get(i));
                    i++;
                }
                break;
            }
        }
        if (loopSize < 8)
        {
            //Debug.p("loop " + Debug.str(cells));
            //Debug.p("no area loopSize=" + loopSize);
            return null; // need at least 8 to enclose something
        }
        
        // Scan each row ...
        for (int row = 0; row < numRows; row++)
        {
            // ... from left to right
            int state = 0; // outside
            
            for (int col = 0; col < numColumns; col++)
            {
                // Is cell part of the loop?
                int q = -1;
                Cell cell = null;
                for (int i = p; i < n; i++)
                {
                    cell = cells.get(i);
                    if (cell.col == col && cell.row == row)
                    {
                        q = i;
                        break; // found cell in loop
                    }
                }
                
                if (q == -1)
                {
                    // not in loop
                    if (state == 1) // inside
                    {
                        cell = cell(col, row);
                        p("add to area " + cell.col + "," + cell.row);
                        insiders.add(cell);
                    }
                }
                else // cell is part of loop
                {
                    // +-------+
                    // |   |   |     <- This is NORTH+WEST
                    // |---+   |
                    // |       |
                    // +-------+
                    //
                    Cell prev = cells.get(p + ((q - 1 - p + loopSize) % loopSize));
                    Cell next = cells.get(p + ((q + 1 - p) % loopSize));
                    int corner = direction(cell, prev) + direction(cell, next);
                    //s_log.info("dir " + cell.col + "," + cell.row + " = " + corner);
                    switch (state)
                    {
                        case 0: // out
                        {
                            switch (corner)
                            {
                                case Direction.NORTH+Direction.SOUTH: state = 1; break;
                                case Direction.EAST+Direction.SOUTH: state = 2; break;
                                case Direction.NORTH+Direction.EAST: state = 3; break;
                            }                            
                            break;
                        }
                        case 1: // in
                        {
                            switch (corner)
                            {
                                case Direction.NORTH+Direction.SOUTH: state = 0; break;
                                case Direction.EAST+Direction.SOUTH: state = 3; break;
                                case Direction.NORTH+Direction.EAST: state = 2; break;
                            }
                            break;
                        }
                        case 2: // in on path
                        {
                            switch (corner)
                            {
                                case Direction.SOUTH+Direction.WEST: state = 0; break;
                                case Direction.NORTH+Direction.WEST: state = 1; break;
                            }
                            break;
                        }
                        case 3: // out on path
                        {
                            switch (corner)
                            {
                                case Direction.SOUTH+Direction.WEST: state = 1; break;
                                case Direction.NORTH+Direction.WEST: state = 0; break;
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (insiders.size() == 0)
        {
            //Debug.p("no area");
            return null;
        }
        
//        // Enlarge the loop area by 1
//        for (int i = p; i < n; i++)
//        {
//            Cell c = cells.get(i);
//            addNeighbors(c, list);
//        }
        
        dumpArea(insiders);
        
        return insiders;
    }
    
    protected void addNeighbors(Cell c, Collection<Cell> list)
    {
        // All 8 directions
        int col = c.col;
        int row = c.row;
        if (col > 0)
        {
            list.add(cell(col - 1, row));
            if (row > 0)
                list.add(cell(col - 1, row - 1));
            if (row < numRows - 1)
                list.add(cell(col - 1, row + 1));
        }
        if (col < numColumns - 1)
        {
            list.add(cell(col + 1, row));
            if (row > 0)
                list.add(cell(col + 1, row - 1));
            if (row < numRows - 1)
                list.add(cell(col + 1, row + 1));
        }
        if (row > 0)
            list.add(cell(col, row - 1));
        if (row < numRows - 1)
            list.add(cell(col, row + 1));
    }
    
    public void dumpArea(Collection<Cell> cells)
    {
        if (cells == null)
        {
            s_log.info("no area");
            return;
        }
        
        StringBuilder b = new StringBuilder();
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = cell(col, row);
                boolean x = cells.contains(cell);
                b.append(x ? 'x' : '.');
            }
            b.append("\n");
        }
        s_log.info(b.toString());
    }
    
    protected int direction(Cell a, Cell b)
    {
        if (a.row == b.row)
            return a.col == b.col - 1 ? Direction.EAST : Direction.WEST;
        else
            return a.row == b.row - 1 ? Direction.SOUTH : Direction.NORTH;
    }

    protected void explodeNeighbors(Collection<Cell> cells)
    {
        explodeNeighbors(cells, null);
    }
    
    protected void explodeNeighbors(Collection<Cell> cells, Integer color)
    {
        Set<Cell> neighbors = new Neighbors(cells);
        for (Cell c : neighbors)
        {
            if (c.canExplodeNextTo(cells, color))
            {
                c.explode(null, 0);
            }
        }
    }
    
    public class Neighbors extends HashSet<Cell>
    {
        private Collection<Cell> m_cells;
        
        public Neighbors(Collection<Cell> cells)
        {
            m_cells = cells;
            
            for (Cell c : cells)
            {
                int col = c.col;
                int row = c.row;
                if (col > 0)
                    maybeAdd(cell(col - 1, row));
                if (col < numColumns - 1)
                    maybeAdd(cell(col + 1, row));
                if (row > 0)
                    maybeAdd(cell(col, row - 1));
                if (row < numRows - 1)
                    maybeAdd(cell(col, row + 1));
            }
        }
        
        protected void maybeAdd(Cell c)
        {
            if (contains(c) || m_cells.contains(c))
                return;
            
            if (c instanceof Hole || c instanceof Slide || c instanceof Rock)// || c.isLockedCage())
                return;
            
            //Debug.p("add neighbor " + c);
            add(c);
        }
    }
    
    public TransitionList moveBeasts(final UserAction action)
    {
        return new NukeTransitionList("fire/animal", ctx, cfg.growFireDuration)
        {
            public void init()
            {
                //Debug.p("movebeasts");
                Set<Cell> verboten = new HashSet<Cell>();
                
                new MovePacmans(this, verboten, ctx, action);
                new MoveAnimals(this, verboten, ctx);
                new MoveSpiders(this, verboten, ctx);
                        
                if (m_lastExplodedFire == ctx.score.getExplodedFire()) // don't grow fire if any fire exploded
                {
                    new GrowFire(this, verboten, ctx);
                }
            }
        };
    }
    
    public TransitionList radioActives()
    {
        return RadioActives.createTransitions(ctx);
    }
    
    static final Random s_dingRandom = new Random();
    
    public TransitionList machines()
    {
        return new NukeTransitionList("machines", ctx, 1000) //TODO machineDuration
        {
            public void init()
            {
                CellList targets = new CellList();
                Random rnd = ctx.generator.getRandom();
                double dy = -size() * 2;
                for (int row = 0; row < numRows; row++)
                {
                    for (int col = 0; col < numColumns; col++)
                    {
                        Cell a = cell(col, row);
                        if (a instanceof Machine)
                        {
                            Machine machine = (Machine) a;
                            int cx = machine.col;
                            int cy = machine.row;
                            
                            if (((Machine) a).isTriggered())
                            {
                                machine.clearStage();
                                
                                for (int i = 0; i < machine.getHowMany(); i++)
                                {
                                    if (machine.getMachineType() == MachineType.COIN)
                                    {
                                        Coin coin = (Coin) machine.createItem(ctx, null);
                                        add(getCoinTransition(coin, cx, cy, ctx));
                                    }
                                    else
                                    {
                                        final Cell target = getMachineTarget(machine, targets, rnd);
                                        if (target != null)
                                        {
                                            targets.add(target);

                                            final boolean playSound = isEmpty();
                                            //final boolean isBombify = machine.getMachineType() == MachineType.BOMBIFY;
                                            
                                            switch (machine.getMachineType())
                                            {
                                                case BUBBLE:
                                                    final Bubble bubble = new Bubble();                                            
                                                    final IPrimitive<?> shape = bubble.createShape(size());
                                                    double d = size() / 2;
                                                    
                                                    add(new ArchTransition(x(cx) - d, y(cy) - d, x(target.col) - d, y(target.row) - d, dy, shape) {
                                                        @Override
                                                        public void afterStart()
                                                        {
                                                            ctx.nukeLayer.add(shape);
                                                            
                                                            if (playSound)
                                                                Sound.WOOSH.play();
                                                        }
                                                        
                                                        @Override
                                                        public void afterEnd()
                                                        {
                                                            ctx.nukeLayer.remove(shape);
                                                            
                                                            bubble.item = target.item;
                                                            bubble.ice = target.ice;
                                                            setCell(target.col, target.row, bubble);
                                                            
                                                            bubble.init(ctx);
                                                            bubble.initGraphics(target.col, target.row, x(target.col), y(target.row));
                                                            
                                                            ctx.score.addBubble();
                                                        }
                                                    });
                                                    break;
                                                    
                                                default:
                                                    final Item newItem = machine.createItem(ctx, target);
                                                    add(getLaunchItemTransition(newItem, cx, cy, target, ctx, playSound));
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            private Cell getMachineTarget(Machine machine, CellList targets, Random rnd)
            {
                for (int i = 0; i < 200; i++)
                {
                    int col = rnd.nextInt(numColumns);
                    int row = rnd.nextInt(numRows);
                    
                    if (targets.containsCell(col, row))
                        continue;
                    
                    Cell cell = cell(col, row);
                    if (machine.canTargetCell(cell))
                        return cell;
                }
                return null;
            }
        };
    }
    
    public static ArchTransition getLaunchItemTransition(final Item newItem, int cx, int cy, final Cell target, final Context ctx, final boolean playSound)
    {
        GridState state = ctx.state;
        double dy = state.size() * -2;
        
        newItem.init(ctx);
        
        return new ArchTransition(state.x(cx), state.y(cy), state.x(target.col), state.y(target.row), dy, newItem) {
            @Override
            public void afterStart()
            {
                newItem.addShapeToLayer(ctx.nukeLayer);
                
                if (playSound)
                    Sound.WOOSH.play();
            }
            
            @Override
            public void afterEnd()
            {
                newItem.removeShapeFromLayer(ctx.nukeLayer);
                if (target.item != null)
                    target.item.removeShapeFromLayer(ctx.dotLayer);
                
//                if (isBombify)
//                    ((DotBomb) newItem).incrementStrength(1);   // TODO immediately decreases
                
                ctx.score.replace(target.item, newItem);
                
                newItem.addShapeToLayer(ctx.dotLayer);
                target.item = newItem;
            }
        };
    }
    
    public static IAnimationCallback getCoinTransition(final Coin coin, int cx, int cy, final Context ctx)
    {
        GridState state = ctx.state;
        Random rnd = ctx.generator.getRandom();
        double dy = state.size() * -2;
        
        int newCol = rnd.nextInt(state.numColumns);
        int newRow = rnd.nextInt(state.numRows);
        
        coin.init(ctx);

        return new ArchTransition(state.x(cx), state.y(cy), state.x(newCol), state.y(newRow), dy, coin) {
            boolean dinged; 
            
            @Override
            public void move2(double pct)
            {
                if (pct > 0 && !dinged)
                {
                    SoundManager.ding(s_dingRandom.nextInt(5));
                    dinged = true;
                }
                super.move2(pct);
            }
            
            @Override
            public void afterStart()
            {
                coin.addShapeToLayer(ctx.nukeLayer);
                
//                if (playSound)
//                    Sound.WOOSH.play();
            }

            @Override
            public void afterEnd()
            {
                coin.removeShapeFromLayer(ctx.nukeLayer);
                
                ctx.score.explodedCoins(coin.getAmount());
            }
        }.fadeOut();
    }
    
    public class SpinSlotMachines extends Callback
    {
        private static final long SLOT_PULL_DELAY = 400;
        
        @Override
        public void run()
        {
            boolean found = false;
            int max = 0;
            for (SlotMachine sm : m_slotMachines)
            {
                if (sm.isTriggered())
                {
                    int notHoldCount = sm.notOnHoldCount();
                    if (notHoldCount > max)
                        max = notHoldCount;
                    
                    found = true;
                }
            }
            if (!found)
            {
                doNext();
                return;
            }
            
            final int n = max - 1;
            final long spinDuration = (Slot.SPINS + n) * Slot.DT;
            TransitionList trans = new TransitionList("slotMachines", ctx.doorLayer, SLOT_PULL_DELAY + spinDuration) //TODO machineDuration
            {
                public void init()
                {
                    Sound.SLOT_PULL.play();
                    
                    for (SlotMachine sm : m_slotMachines)
                    {
                        if (sm.isTriggered())
                        {
                            int notOnHold = sm.notOnHoldCount();
                            
                            int pause = notOnHold - 1;
                            for (Slot slot : sm)
                            {
                                if (!slot.isHold())
                                {
                                    addTransition(slot, Slot.SPINS + n, pause);
                                    pause--;
                                }                            
                            }
                        }
                    }
                }

                private void addTransition(final Slot slot, int totalSpins, int pause)
                {
                    final int spins = totalSpins - pause;
                    IAnimationCallback cb = new IAnimationCallback() 
                    {
                        @Override
                        public void onStart(IAnimation animation, IAnimationHandle handle)
                        {
                            slot.spin(spins);
                            slot.updateSpin(System.currentTimeMillis());
                            ctx.doorLayer.redraw();
                        }

                        @Override
                        public void onFrame(IAnimation animation, IAnimationHandle handle)
                        {
                            slot.updateSpin(System.currentTimeMillis());
                            ctx.doorLayer.redraw();
                        }

                        @Override
                        public void onClose(IAnimation animation, IAnimationHandle handle)
                        {
                            slot.endSpin();
                            ctx.doorLayer.redraw();
                        }
                    };
                    
                    SerialAnimation sa = SerialAnimation.delay(SLOT_PULL_DELAY, spins * Slot.DT, cb, null);
                    if (pause > 0)
                        sa.addDelay(pause * Slot.DT);
                    
                    add(sa);
                }
                
                @Override
                public void doNext()
                {
                    // check winnings
                    List<RewardInfo> rewards = new ArrayList<RewardInfo>();
                    for (SlotMachine sm : ctx.state.getSlotMachines())
                    {
                        if (sm.isTriggered())
                        {
                            RewardInfo c = sm.getReward(ctx);
                            if (c != null)
                                rewards.add(c);
                            
                            ctx.state.getSlotMachines().afterSpin(sm, c != null);
                        }
                    }
                    
                    if (rewards.size() == 0)
                    {
                        Sound.MISS.play();
                        SpinSlotMachines.this.doNext();
                        return;
                    }
                    
                    awardCombos(rewards);
                }
            };
            trans.doRun();
        }

        protected void awardCombos(final List<RewardInfo> rewards)
        {
            CallbackChain chain = new CallbackChain();
            
            TransitionList blink = new TransitionList("slotMachine blink", ctx.doorLayer, 1000) {
                @Override
                public void init()
                {
                    Sound.WIN_SLOTS.play();
                    for (RewardInfo rewardInfo : rewards)
                    {
                        for (final Slot slot : rewardInfo.slotMachine)
                        {
                            add(new IAnimationCallback() {
                                @Override
                                public void onStart(IAnimation animation, IAnimationHandle handle)
                                {
                                    slot.setWinning(true);
                                }

                                @Override
                                public void onFrame(IAnimation animation, IAnimationHandle handle)
                                {
                                    slot.updateWinning();
                                }

                                @Override
                                public void onClose(IAnimation animation, IAnimationHandle handle)
                                {
                                    slot.setWinning(false);
                                }
                            });
                        }
                    }
                }
            };
            chain.add(blink);
            
            TransitionList trans = new NukeTransitionList("slotMachine rewards", ctx, 1000) //TODO duration
            {
                @Override
                public void init()
                {
                    Sound.WOOSH.play(); //TODO
                    
                    for (RewardInfo rewardInfo : rewards)
                    {
                        rewardInfo.reward.addTransitions(ctx, rewardInfo, this, new CellList());
                    }
                }
                
                @Override
                public void doNext()
                {
                    SpinSlotMachines.this.doNext();
                }
            };
            chain.add(trans);
            chain.run();
        }
    }
    
    public Callback slotMachines()
    {
        return new SpinSlotMachines();
    }
    
    public TransitionList explosions()
    {
        final List<Cell> explodies = new ArrayList<Cell>();
        final List<Cell> armed = new ArrayList<Cell>();
        
        return new NukeTransitionList("explosions", ctx, cfg.explosionDuration)
        {
            @Override
            public boolean condition()
            {
                return true; // also animate if there are no animations
                
                // BUG FIX: infinite loop when Explody was between holes and borders, and there were no resulting animations
            }
            
            public void run()
            {
                Sound.ZAP.play();
                
                //Debug.p("run explosions");
                super.run();
            }
            
            public void done()
            {
                //Debug.p("explosions done");
                
                for (Cell cell : explodies)
                {
                    cell.item.removeShapeFromLayer(ctx.dotLayer);
                    cell.item = null;
                }
                for (Cell cell : armed)
                {
                    cell.item.removeShapeFromLayer(ctx.dotLayer);
                    cell.item = null;
                }
                
                super.done();
                
                updateScore();
            }
            
            @Override
            public void init()
            {                
                for (int row = 0; row < numRows; row++)
                {
                    for (int col = 0; col < numColumns; col++)
                    {
                        Cell a = cell(col, row);
                        if (a.item == null)
                            continue;
                        
                        if (a.item instanceof Explody)
                        {
                            explodies.add(a);
                        }
                        else if (a.item.isArmed())
                        {
                            if (a.item instanceof Bomb)
                                explodies.add(a); //TODO .................. is this right?
                            else
                                armed.add(a);
                        }
                    }
                }
                new Explosions(this, explodies, armed, ctx);
            }
        };        
    }

    public TransitionList moveSusans()
    {
        return new TransitionList("susan", ctx.dotLayer, cfg.lazySusanTurnDuration)
        {
            public void init()
            {
                //Debug.p("movesusans");
                for (LazySusan su : m_lazySusans)
                {
                    su.addTransitions(this);
                }
                
                for (Conveyor c : m_conveyors)
                {
                    c.addTransitions(this, GridState.this, ctx);
                }
            }
        }.addOtherLayers(ctx.backgroundLayer);
    }
    
    public void addRewardAnimation(AnimList list, final Reward reward)
    {
        TransitionList t = new NukeTransitionList("reward", ctx, 300)
        {
            public void init()
            {
                //Debug.p("reward");
                final Cell c;
                final Item item;
                if (reward.isRandom())
                {
                    c = getRandomDotCell();
                    if (c == null)
                        return;
                }
                else
                {
                    c = reward.cell;
                }
                item = reward.upgrade(c.item, ctx);
                item.init(ctx);
                
                int cx = c.col < numColumns / 2 ? numColumns - 1 : 0; 
                int cy = c.row < numRows / 2 ? numRows - 1 : 0; 
                
                add(new DropTransition(x(cx), y(cy), x(c.col), y(c.row), item) {
                    @Override
                    public void afterStart()
                    {
                        //Debug.p("spawn " + src);
                        item.addShapeToLayer(ctx.nukeLayer);
                        
                        Sound.WOOSH.play();
                    }
                    
                    @Override
                    public void afterEnd()
                    {
                        item.removeShapeFromLayer(ctx.nukeLayer);
                        if (c.item != null)
                        {
                            c.item.removeShapeFromLayer(ctx.dotLayer);
                        }
                        
                        ctx.score.replace(c.item, item);
                        
                        item.addShapeToLayer(ctx.dotLayer);
                        c.item = item;
                        //Debug.p("reward added");
                    }
                });
            }
        };
        list.add(t);
    }
    
    public void add(LazySusan s)
    {
        m_lazySusans.add(s);
    }

    public List<LazySusan> getLazySusans()
    {
        return m_lazySusans;
    }
    
    // grid is not connected to Context yet
    public GridState copy()
    {
        GridState grid = new GridState(numColumns, numRows);
        
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell a = cell(col, row);
                Cell b = a.copy();
                if (a.item != null)
                    b.item = a.item.copy(); 
                
                grid.setCell(col, row, b);
            }
        }
        
        for (int i = 0; i < m_lazySusans.size(); i++)
        {
            grid.add(m_lazySusans.get(i).copy());
        }
        
        return grid;
    }
    
    public void addExplody(Cell cell)
    {
        addItem(cell, new Explody());
    }
    
    public void addItem(Cell cell, Item item)
    {
        item.init(ctx);
        item.moveShape(x(cell.col), y(cell.row));
        item.addShapeToLayer(ctx.dotLayer);
        cell.item = item;
    }

    public void copyState(UndoState undoState)
    {
        CellState[] grid = new CellState[m_grid.length];
        for (int i = 0; i < m_grid.length; i++)
            grid[i] = m_grid[i].copyState();
        undoState.grid = grid;
        m_circuits.copyState(undoState);
    }

    public void restoreState(UndoState undoState)
    {
        m_circuits.restoreState(undoState);
        for (int i = 0; i < m_grid.length; i++)
        {
            CellState state = undoState.grid[i];
            if (state.type != m_grid[i].getType())
            {
                // cell type changed due to Bubble Machine
                Cell cell;
                if (state.type == Cell.BUBBLE)
                {
                    cell = new Bubble();
                }
                else // ITEM_CELL
                {
                    cell = new ItemCell();
                }
                
                Cell prev = m_grid[i];
                cell.init(ctx);
                cell.initGraphics(prev.col, prev.row, x(prev.col), y(prev.row));
                
                if (prev.item != null)
                    prev.item.removeShapeFromLayer(ctx.dotLayer);
                
                prev.removeGraphics();
                
                m_grid[i] = cell;
            }
            m_grid[i].restoreState(state);
        }
    }
}
