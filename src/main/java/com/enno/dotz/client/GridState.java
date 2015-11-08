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
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Circuits.Circuit;
import com.enno.dotz.client.Conveyors.Conveyor;
import com.enno.dotz.client.Conveyors.ConveyorException;
import com.enno.dotz.client.DotzGridPanel.EndOfLevel;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.AnimList;
import com.enno.dotz.client.anim.Explosions;
import com.enno.dotz.client.anim.FireRocket;
import com.enno.dotz.client.anim.GetTransitions;
import com.enno.dotz.client.anim.GrowFire;
import com.enno.dotz.client.anim.MoveAnimals;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.ShowDescription;
import com.enno.dotz.client.anim.ShowWordCallback;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.item.YinYang;
import com.enno.dotz.client.util.CallbackChain;
import com.enno.dotz.client.util.CallbackChain.Callback;
import com.enno.dotz.client.util.CollectionsUtils;
import com.enno.dotz.client.util.Debug;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;

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
    
    public EndOfLevel endOfLevel;
    
    public GridState(int numColumns, int numRows)
    {
        this.numRows = numRows;
        this.numColumns = numColumns;
        m_grid = new Cell[numColumns * (numRows + 1)]; //TODO do we need the extra row?
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
                        c.item = ctx.generator.getNextItem(ctx, true);
                    else
                    {
                        if (c.item instanceof Dot)
                        {
                            Dot dot = (Dot) c.item;
                            if (ctx.generator.generateLetters)
                            {
                                if (dot.letter == null)
                                    dot.letter = ctx.generator.nextLetter();                                
                            }
                            else
                            {
                                if (dot.letter != null)
                                    dot.letter = null;
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
            m_conveyors = new Conveyors(this);
        }
        catch (ConveyorException e)
        {
            Debug.p("unexpected conveyor problem", e);
        }
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

    public void processChain(final Runnable nextMoveCallback)
    {
        if (ctx.generator.swapMode)
            processSwapChain(null, nextMoveCallback);
        else
            processChain(new ArrayList<Cell>(), false, false, false, null, null, nextMoveCallback);
    }
    
    public void processChain(final List<Cell> cells, final boolean is_square, final boolean isKnightMode, final boolean isWordMode, final String word, final Integer color, final Runnable nextMoveCallback)
    {
        if (isWordMode)
        {
            new ShowWordCallback(ctx, word, color)
            {
                @Override
                protected void done()
                {
                    processChain2(cells, is_square, isKnightMode, isWordMode, word, color, nextMoveCallback);
                }
            };
        }
        else
            processChain2(cells, is_square, isKnightMode, isWordMode, word, color, nextMoveCallback);
    }
    
    private Callback explodeLoop(boolean checkClocks, boolean isEndOfLevel)
    {
        return new ExplodeLoop(checkClocks, isEndOfLevel);
    }
    
    private void processChain2(List<Cell> cells, boolean is_square, boolean isKnightMode, boolean isWordMode, String word, Integer color, final Runnable nextMoveCallback)
    {
        ctx.score.addMove();
        
        m_lastExplodedFire = ctx.score.getExplodedFire();
        clearStunnedAnimals();
        activateLasers(false);
        if (is_square)
            ctx.generator.excludeDotColor(color);
        
        final Collection<Cell> area = is_square ?  getArea(cells) : null;
        //TransitionList nukes = area == null ? null : animateNuke(area, ctx.nukeLayer);
        
        explodeCells(cells, area, is_square, isKnightMode, color);        
        
        if (cells.size() > 0)
            Sound.DROP.play(); // could be a mirror flip
        
        ctx.dotLayer.draw();
        updateScore();
        
        AnimList list = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);        
        
        list.add(explodeLoop(false, false)); // don't check clocks
        list.add(moveBeasts());
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
                ctx.dotLayer.draw();
                
                System.gc();
                
                checkEndOfLevel(nextMoveCallback);
            }
        });
        list.run();
    }
    
    public void processSwapChain(GetSwapMatches matches, final Runnable nextMoveCallback)
    {
        ctx.score.addMove();
        
        m_lastExplodedFire = ctx.score.getExplodedFire();
        clearStunnedAnimals();
        activateLasers(false);
        
        if (matches != null)
        {
            matches.animate(new Runnable() {
                @Override
                public void run()
                {
                    processSwapChain2(nextMoveCallback);
                }
            });
        }
        else
        {
            processSwapChain2(nextMoveCallback);
        }
    }
    
    protected void processSwapChain2(final Runnable nextMoveCallback)
    {
        AnimList list = new GetTransitions(ctx).get(ctx.dotLayer, cfg.dropDuration);        
        
        list.add(explodeLoop(false, false)); // don't check clocks
        list.add(moveBeasts());
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
                ctx.dotLayer.draw();
                
                System.gc();
                
                checkEndOfLevel(nextMoveCallback);
            }
        });
        list.run();
    }
    
    public void fireRocket(Cell cell, Runnable nextMoveCallback)
    {
        new FireRocket(cell, nextMoveCallback, ctx);
    }
    
    private void checkEndOfLevel(Runnable nextMoveCallback)
    {
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
        else if (mustReshuffle())
        {
            reshuffle(nextMoveCallback);
        }
        else
            nextMoveCallback.run();
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
                        Cell c = getRandomCell();
                        c.explode(null, 0);
                        addExplody(c);
                        
                        ctx.score.addMove();
                        updateScore();
                    }
                    doNext();
                }
            });
            list.add(explodeLoop(false, true));       // don't check clocks, endOfLevel=true
            list.add(transitions());
        }
        list.animate(done);
    }
    
    protected Cell getRandomCell()
    {
        Random r = new Random();
        while (true)
        {
            int col = r.nextInt(numColumns);
            int row = r.nextInt(numRows);
            Cell c = cell(col, row);
            if (!c.isLocked() && (c.item instanceof Dot || c.item instanceof Wild))
                return c;
        }
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
        ctx.laserLayer.draw();
    }

    protected void rotateDoors()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell c = cell(col, row);
                if (c instanceof Door)
                    ((Door) c).tick();
            }
        }
        ctx.doorLayer.draw();
    }
    
    protected void reshuffle(final Runnable nextMoveCallback)
    {
        SC.ask("You're stuck. Reshuffle?", new BooleanCallback()
        {            
            @Override
            public void execute(Boolean ok)
            {
                if (Boolean.TRUE.equals(ok))
                {
                    if (!doReshuffle(50))
                    {
                        reshuffle(nextMoveCallback);
                    }
                    else
                    {
                        if (ctx.generator.swapMode)
                        {
                            // Do explodeLoop after reshuffle                
                            CallbackChain chain = new CallbackChain();
                            chain.add(transitions());
                            chain.add(explodeLoop(false, false)); // don't check clocks
                            chain.add(new Callback() {
                                @Override
                                public void run()
                                {
                                    if (mustReshuffle())
                                        reshuffle(nextMoveCallback);
                                    else
                                        nextMoveCallback.run();
                                }
                            });
                            chain.run();
                        }
                        else
                        {
                            nextMoveCallback.run();
                        }
                    }
                }
                else
                {
                    nextMoveCallback.run(); //TODO cancel
                }
            }
        });
    }
    
    // When YinYang is clicked
    public void reshuffle()
    {
        doReshuffle(50); //TODO if false, then can't do it
    }
    
    /**
     * 
     * @param loop
     * 
     * @return Whether it succeeded
     */
    protected boolean doReshuffle(int loop)
    {
        if (loop < 0)
            return false;
        
        List<Pt> pts = new ArrayList<Pt>();
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = cell(col, row);
                if (cell.isLocked())            // Don't reshuffle items stuck in Doors
                    continue;
                
                Item item = cell.item;
                if (item instanceof Dot || item instanceof Wild)
                    pts.add(new Pt(col, row));
            }
        }
        
        List<Pt> pts2 = new ArrayList<Pt>();
        pts2.addAll(pts);
        CollectionsUtils.shuffle(pts2);
        
        for (int i = 0, n = pts.size(); i < n; i++)
        {
            Pt a = pts.get(i);
            Pt b = pts2.get(i);
            if (a != b)
            {
                Item ia = cell(a.col, a.row).item;
                Item ib = cell(b.col, b.row).item;
                cell(a.col, a.row).item = ib;
                cell(b.col, b.row).item = ia;
                ia.moveShape(x(b.col), y(b.row));
                ib.moveShape(x(a.col), y(a.row));
            }
        }
        
        if (mustReshuffle())
            return doReshuffle(loop - 1); // detect infinite loop
        
        return true; // success
    }
    
    protected boolean mustReshuffle()
    {
        if (ctx.generator.generateLetters)
            return false;
        
        if (ctx.generator.swapMode)
            return mustReshuffleSwapMode();
        
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = cell(col, row);
                if (cell.isLocked())
                    continue;
                
                Item item = cell.item;
                if (item == null)
                    continue;
                
                if (item instanceof Rocket || item instanceof YinYang)
                    return false;
                
                if (col < numColumns - 1)
                {
                    Cell cell2 = cell(col + 1, row);
                    if (cell2.isLocked())
                        continue;
                    
                    Item item2 = cell2.item;
                    if (item2 != null)
                    {
                        if (canConnect(item, item2))
                            return false;
                    }
                }
                
                if (row < numRows - 1)
                {
                    Cell cell2 = cell(col, row + 1);
                    if (cell2.isLocked())
                        continue;
                    
                    Item item2 = cell2.item;
                    if (item2 != null)
                    {
                        if (canConnect(item, item2))
                            return false;
                    }
                }
            }
        }
        //TODO check knights
        
        return true;
    }
    
    protected boolean mustReshuffleSwapMode()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = cell(col, row);
                if (cell.isLocked())
                    continue;
                
                Item item = cell.item;
                if (item == null)
                    continue;
                
                if (item instanceof Rocket || item instanceof ColorBomb || item instanceof YinYang)
                    return false;
            }
        }
        
        // Try vertical swaps
        for (int row = 0; row < numRows - 1; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                if (trySwap(col, row, col, row + 1))
                    return false;
            }
        }
        
        // Try horizontal swaps
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns - 1; col++)
            {
                if (trySwap(col, row, col + 1, row))
                    return false;
            }
        }
        
        return true;
    }
    
    protected boolean trySwap(int col, int row, int col2, int row2)
    {
        Cell cell = cell(col, row);
        if (cell.isLocked())
            return false;
        
        Item item = cell.item;
        if (item == null || cantSwap(item))
            return false;

        Cell cell2 = cell(col2, row2);
        if (cell2.isLocked())
            return false;
        
        Item item2 = cell2.item;
        if (item2 == null || cantSwap(item))
            return false;
        
        cell.item = item2;
        cell2.item = item;
        boolean canSwap = trySwap2(col, row, col2, row2);
        cell.item = item;
        cell2.item = item2;
        return canSwap;
    }
    
    protected boolean cantSwap(Item item)
    {
        return item instanceof Animal || item instanceof Fire || item instanceof Laser || item instanceof Knight;
    }
    
    protected boolean trySwap2(int col, int row, int col2, int row2)
    {
        boolean hor = row == row2;
        if (hor)
        {
            if (findCombo(col - 2, row, 1, 0, 6)
             || findCombo(col, row - 2, 0, 1, 5)
             || findCombo(col2, row - 2, 0, 1, 5))
                return true;
        }
        else
        {
            if (findCombo(col, row - 2, 0, 1, 6)
             || findCombo(col - 2, row, 1, 0, 5)
             || findCombo(col - 2, row2, 1, 0, 5))
                return true;
        }
        return false;
    }
    
    protected boolean findCombo(int col, int row, int dcol, int drow, int length)
    {
        int c = col, r = row;
        int n = 0;
        Integer comboColor = null;
        for (int i = 0; i < length; i++, c += dcol, r += drow)
        {
            Integer cellColor = cellColor(c, r);
            if (cellColor == null)
            {
                n = 0;
                comboColor = null;
            }
            else if (comboColor == null)
            {
                comboColor = cellColor;
                n++;
            }
            else if (cellColor == Config.WILD_ID)
            {
                n++;
            }
            else if (comboColor == Config.WILD_ID)
            {
                comboColor = cellColor;
                n++;
            }
            else if (comboColor == cellColor)
            {
                n++;
            }
            else
            {
                n = 0;
                comboColor = null;
            }
            if (n == 3)
                return true;
            
            if (!isValidCell(c, r))
            {
                n = 0;
                continue;
            }
        }
        return false;
    }
    
    protected Integer cellColor(int col, int row)
    {
        if (!isValidCell(col, row))
            return null;
        
        Cell cell = cell(col, row);
        if (cell.isLocked())
            return null;
        
        Item item = cell.item;
        if (item == null)
            return null;
        
        if (GetSwapMatches.isColorDot(item))
            return item.getColor();
        
        return null;
    }
    
    protected boolean canConnect(Item a, Item b)
    {
        boolean aDot = (a instanceof Dot || a instanceof Animal);
        if (aDot && b instanceof Wild)
            return true;
        
        boolean bDot = (b instanceof Dot || b instanceof Animal);
        if (bDot && a instanceof Wild)
            return true;
        
        if (aDot && bDot && a.getColor() == b.getColor())
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
                    ctx.backgroundLayer.draw();
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
                    if (a.item instanceof Explody)
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

        private int m_type;
        private Cell m_special;
        private int m_points;
        private int m_color;
        private boolean m_vertical;
        
        public void setType(int type)
        {
            m_type = type;
            
            addPoints(type);
            
            if (m_type != KNIGHT)
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
    }
    
    public static class GetSwapMatches
    {
        private Context ctx;
        private GridState m_state;
        
        private List<Cell> m_explosions = new ArrayList<Cell>();
        private List<SwapCombo> m_hcombos = new ArrayList<SwapCombo>();
        private List<SwapCombo> m_vcombos = new ArrayList<SwapCombo>();
        private List<SwapCombo> m_combos = new ArrayList<SwapCombo>();
        
        private boolean m_swapped;
        private Cell[] m_swaps = new Cell[2];
        private Random rnd = new Random();
        
        public GetSwapMatches(Context ctx)
        {
            this.ctx = ctx;
            m_state = ctx.state;
        }
        
        public void animate(final Runnable whenDone)
        {
            if (!m_swapped)
            {
                animate2(whenDone);
            }
            else
            {
                TransitionList list = new TransitionList("swap", ctx.dotLayer, 250) {
                    @Override
                    public void done()
                    {
                        animate2(whenDone);
                    }
                };
                
                double x1 = m_state.x(m_swaps[0].col);
                double y1 = m_state.y(m_swaps[0].row);
                double x2 = m_state.x(m_swaps[1].col);
                double y2 = m_state.y(m_swaps[1].row);
                list.add(new DropTransition(x1, y1, x2, y2, m_swaps[1].item));
                list.add(new DropTransition(x2, y2, x1, y1, m_swaps[0].item));
                
                list.run();
            }
        }
        
        protected void animate2(Runnable whenDone)
        {
            switch (m_combos.get(0).getType())
            {
                case SwapCombo.COLOR_BOMB:
                    animateColorBomb(whenDone);
                    return;
                case SwapCombo.TOTAL_BOMB:
                    animateTotalBomb(whenDone);
                    return;
                case SwapCombo.WRAP_BOMB:
                    animateWrapBomb(whenDone);
                    return;
            }
            
            Set<Cell> exploded = new HashSet<Cell>();           
            Set<Cell> explodies = new HashSet<Cell>();
            
            Sound sound = Sound.DROP;
            for (SwapCombo combo : m_combos)
            {
                for (Cell cell : combo)
                {
                    if (!exploded.contains(cell))
                    {
                        explode(cell, null, exploded, explodies); // or pass color?
                    }
                }
            }
            
            for (SwapCombo combo : m_combos)
            {
                switch (combo.getType())
                {
                    case SwapCombo.TEE:
                    {
                        Cell special = combo.getSpecial();
                        if (special.item == null)
                        {
                            WrappedDot dot = new WrappedDot(combo.getColor());
                            addItem(special, dot);
                        }
                        break;
                    }
                    case SwapCombo.FIVE:
                    {
                        Cell special = combo.getSpecial();
                        if (special.item == null)
                        {
                            addItem(special, new ColorBomb());
                        }
                        break;
                    }
                    case SwapCombo.FOUR:
                    {
                        Cell special = combo.getSpecial();
                        if (special.item == null)
                        {
                            addItem(special, new Wild());
                        }
                        break;
                    }
                    case SwapCombo.EXPLODY_5:
                    {
                        for (Cell special : combo)
                        {
                            if (special.item == null)
                            {
                                addItem(special, new Explody(2));
                            }
                        }
                        break;
                    }
                }
            }
            sound.play();
            
            m_state.explodeNeighbors(exploded);
            
            for (Cell explody : explodies)
            {
                if (explody.item == null)
                    m_state.addExplody(explody);
            }
            
            whenDone.run();
        }

        protected void animateColorBomb(Runnable whenDone)
        {
            SwapCombo combo = m_combos.get(0);
            int color = combo.getColor();
            
            Sound.DROP.play();
            
            Set<Cell> exploded = new HashSet<Cell>();
            Set<Cell> explodies = new HashSet<Cell>();
            
            exploded.add(m_swaps[0]);
            m_swaps[0].explode(color, 1);
            exploded.add(m_swaps[1]);
            m_swaps[1].explode(color, 1);
            
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell cell = m_state.cell(col, row);
                    Item item = cell.item;
                    if (item != null && !exploded.contains(cell) && cell.canExplode(color))
                    {
                        explode(cell, color, exploded, explodies);
                    }
                }
            }
            
            m_state.explodeNeighbors(exploded);
            
            for (Cell explody : explodies)
            {
                if (explody.item == null)
                    m_state.addExplody(explody);
            }
            
            whenDone.run();
        }
        
        protected void animateWrapBomb(Runnable whenDone)
        {
            SwapCombo combo = m_combos.get(0);
            int color = combo.getColor();
            
            Sound.DROP.play();
            
            Set<Cell> exploded = new HashSet<Cell>();
            Set<Cell> explodies = new HashSet<Cell>();
            
            exploded.add(m_swaps[0]);
            m_swaps[0].explode(color, 1);
            explodies.add(m_swaps[0]);

            exploded.add(m_swaps[1]);
            m_swaps[1].explode(color, 1);
            explodies.add(m_swaps[1]);
            
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell cell = m_state.cell(col, row);
                    Item item = cell.item;
                    if (item != null && !exploded.contains(cell) && cell.canExplode(color))
                    {
                        cell.explode(color, 1);
                        exploded.add(cell);
                        explodies.add(cell);
                    }
                }
            }
            
            m_state.explodeNeighbors(exploded);
            
            for (Cell explody : explodies)
            {
                if (explody.item == null)
                    m_state.addExplody(explody);
            }
            
            whenDone.run();
        }
        
        protected void animateTotalBomb(Runnable whenDone)
        {
            Sound.DROP.play();
            
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell cell = m_state.cell(col, row);
                    Item item = cell.item;
                    if (item != null && cell.canBeNuked())
                    {
                        cell.explode(null, 1);
                    }
                }
            }            
            
            whenDone.run();
        }
        
        private void explode(Cell cell, Integer color, Set<Cell> exploded, Set<Cell> explodies)
        {
            if (cell.item instanceof WrappedDot)
                explodies.add(cell);
            
            exploded.add(cell);
            cell.explode(color, 1);
        }
        
        private void addItem(Cell cell, Item item)
        {
            cell.item = item;
            item.init(ctx);
            item.moveShape(m_state.x(cell.col), m_state.y(cell.row));
            item.addShapeToLayer(ctx.dotLayer);
        }

        public static boolean isSwapStart(Cell a)
        {
            if (a.item == null || a.isLocked())
                return false;
            
            return isColorDot(a.item) || a.item instanceof ColorBomb || a.item instanceof Anchor;
        }
                
        public boolean canSwap(Cell a, Cell b)
        {
            // Assumes 'a' can be swapped
            if (b.item == null || b.isLocked())
                return false;
            
            if (!(isColorDot(b.item) || b.item instanceof ColorBomb || b.item instanceof Anchor || b.item instanceof Mirror))
                return false;
            
            if (!validSwap(a.item, b.item) && !validSwap(b.item, a.item))
                return false;
            
            Item x = a.item;
            a.item = b.item;
            b.item = x;
            
            m_swapped = true;
            m_swaps[0] = a;
            m_swaps[1] = b;
            
            findCombos();
            if (m_combos.size() == 0)
            {
                x = a.item;
                a.item = b.item;
                b.item = x;
                
                return false;
            }
            
            if (b.item instanceof Knight)
            {
                SwapCombo combo = new SwapCombo();
                combo.add(b);
                combo.setType(SwapCombo.KNIGHT);
                m_combos.add(combo);
            }
            
            return true;
        }
        
        private boolean validSwap(Item a, Item b)
        {
            if (a instanceof Wild && b instanceof ColorBomb)
                return false;
            
            return true;
        }

        protected static boolean isColorDot(Item item)
        {
            return item instanceof Dot || item instanceof DotBomb || item instanceof WrappedDot || item instanceof Wild;
        }
        
        public boolean getTransitions()
        {
            findCombos();
            return m_combos.size() > 0;
        }
        
        protected void findCombos()
        {
            try
            {
            if (m_swapped)
            {
                if (specialSwap(m_swaps[0], m_swaps[1]))
                    return;
                if (specialSwap(m_swaps[1], m_swaps[0]))
                    return;
            }
            
            // Horizontal combo            
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell c = m_state.cell(col, row);
                    if (!canStart(c))
                        continue;

                    SwapCombo combo = new SwapCombo();
                    combo.add(c);
                    
                    for (int col2 = col + 1; col2 < m_state.numColumns; col2++)                        
                    {
                        Cell c2 = m_state.cell(col2, row);
                        if (!canConnect(c2, combo))
                            break;
                        
                        combo.add(c2);
                    }
                    
                    if (combo.size() >= 3 && !combo.isAllWild())
                    {
                        m_hcombos.add(combo);
                        col += combo.size() - 1;
                    }
                }
            }
            
            // Vertical combos
            for (int col = 0; col < m_state.numColumns; col++)
            {
                for (int row = 0; row < m_state.numRows; row++)
                {
                    Cell c = m_state.cell(col, row);
                    if (!canStart(c))
                        continue;
                    
                    SwapCombo combo = new SwapCombo();
                    combo.add(c);
                    
                    for (int row2 = row + 1; row2 < m_state.numRows; row2++)                        
                    {
                        Cell c2 = m_state.cell(col, row2);
                        if (!canConnect(c2, combo))
                            break;
                        
                        combo.add(c2);
                    }
                    
                    if (combo.size() >= 3&& !combo.isAllWild())
                    {
                        m_vcombos.add(combo);
                        row += combo.size() - 1;
                    }                    
                }
            }
            findLength(5, m_hcombos);
            findLength(5, m_vcombos);
            findLength(4, m_hcombos);
            findLength(4, m_vcombos);
            mergeTriples(m_hcombos);
            mergeTriples(m_vcombos);
            addLT();
            addTriples(m_hcombos);
            addTriples(m_vcombos);
            }
            catch (Exception e)
            {
                Debug.p("findCombos", e);
            }
        }
        
        protected boolean specialSwap(Cell a, Cell b)
        {
            if (a.item instanceof ColorBomb)
            {
                if (b.item instanceof ColorBomb)
                {
                    return addCombo(SwapCombo.TOTAL_BOMB);
                }
                else if (b.item instanceof WrappedDot)
                {
                    return addCombo(SwapCombo.WRAP_BOMB);
                }
                else if (b.item instanceof Dot || b.item instanceof DotBomb)
                {
                    return addCombo(SwapCombo.COLOR_BOMB);
                }
            }
            else if (a.item instanceof WrappedDot)
            {
                if (b.item instanceof WrappedDot)
                {
                    return addCombo(SwapCombo.EXPLODY_5);
                }
            }
            return false;
        }
        
        protected boolean addCombo(int type)
        {
            SwapCombo combo = new SwapCombo();
            combo.add(m_swaps[0]);
            combo.add(m_swaps[1]);
            combo.setType(type);
            m_combos.add(combo);
            return true;
        }
        
        protected void findLength(int length, List<SwapCombo> combos)
        {
            for (int i = combos.size() - 1; i >= 0; i--)
            {
                SwapCombo c = combos.get(i);
                if (c.size() >= length)
                {
                    combos.remove(i);
                    c.setType(length > 5 ? 5 : length);
                    identify(c);
                    m_combos.add(c);
                }
            }
        }
        
        protected void mergeTriples(List<SwapCombo> combos)
        {
            COMBO: for (int i = combos.size() - 1; i >= 0; i--)
            {
                SwapCombo c = combos.get(i);
                for (SwapCombo c2 : m_combos)
                {
                    if (c2.overlaps(c))
                    {
                        combos.remove(i);
                        c2.merge(c);
                        continue COMBO; // beware: it could overlap 2 combos
                    }
                }
            }
        }
        
        protected void addLT()
        {
            COMBI: for (int i = m_hcombos.size() - 1; i >= 0; i--)
            {
                SwapCombo c = m_hcombos.get(i);
                
                for (int j = m_vcombos.size() - 1; j >= 0; j--)
                {
                    SwapCombo c2 = m_vcombos.get(j);
                    if (c.mergeTL(c2))
                    {
                        m_hcombos.remove(i);
                        m_vcombos.remove(j);
                        m_combos.add(c);
                        continue COMBI;
                    }
                }
            }
        }
        
        protected void addTriples(List<SwapCombo> combos)
        {
            for (SwapCombo c : combos)
            {
                c.setType(3);
                m_combos.add(c);
            }
        }

        private void identify(SwapCombo combo)
        {
            for (Cell c : combo)
            {
                if (c == m_swaps[0] || c == m_swaps[1])
                {
                    combo.setSpecial(c);
                    return;
                }                
            }
            combo.setSpecial(combo.get(rnd.nextInt(combo.size())));
            
            if (combo.size() == 4)
            {
                if (m_swaps[0] == null)
                    combo.setVertical(rnd.nextBoolean());
                else
                    combo.setVertical(m_swaps[0].col == m_swaps[1].col);
            }
        }

        private boolean canStart(Cell c)
        {
            if (c.item == null || c.isLocked() || m_explosions.contains(c))
                return false;
            
            //TODO specials
            return isColorDot(c.item) || c.item instanceof Animal;
        }

        private boolean canConnect(Cell c, SwapCombo combo)
        {
            if (c.item == null || c.isLocked() || m_explosions.contains(c))
                return false;
            
            if (isColorDot(c.item) || c.item instanceof Animal)
            {
                Integer color = c.item.getColor();
                if (combo.matchesColor(color))
                    return true;
            }
            //TODO specials
            
            
            return false;
        }
    }
    
    public abstract class ExplodeBombs extends TransitionList
    {
        public ExplodeBombs(List<Cell> bombsWentOff)
        {
            super("explodeBombs", ctx.nukeLayer, 1000);
            
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
                ctx.nukeLayer.setVisible(true);
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
                redraw();
            }
            
            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle)
            {
                for (Ring r : m_rings)
                {
                    ctx.nukeLayer.remove(r);
                }
                ctx.nukeLayer.setVisible(false);
            }
            
            private void redraw()
            {
                LayerRedrawManager.get().schedule(ctx.nukeLayer);
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
    
    public abstract class ExplodeLasers extends TransitionList
    {
        private List<Pt> m_list;
        private ArrayList<Laser> m_lasers;

        public ExplodeLasers()
        {
            super("explodeLasers", ctx.laserLayer, 2400);
            
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
    
    public void explodeCells(List<Cell> chain, Collection<Cell> area, boolean is_square, boolean isKnightMode, Integer color)
    {
        Set<Cell> exploded = new HashSet<Cell>();

        // How many to subtract from Animals
        int chainSize = chain.size();
        if (is_square)
            chainSize--;
        
        for (Cell cell : chain)
        {
            if (!exploded.contains(cell))
            {
                exploded.add(cell);
                cell.explode(color, chainSize);
            }
        }

        if (isKnightMode)
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
        else if (is_square)
        {            
            // Explode other cells with the same color
            for (int row = 0; row < numRows; row++)
            {
                for (int col = 0; col < numColumns; col++)
                {
                    Cell cell = cell(col, row);
                    Item item = cell.item;
                    if (item != null && !exploded.contains(cell) && cell.canExplode(color))
                    {
                        exploded.add(cell);
                        cell.explode(color, chainSize);
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
                        cell.explode(color, chainSize); // count wildcards as selected color
                    }
                    
                    if (cell.canBeFilled())
                    {
                        addExplody(cell);
                    }
                }
            }
        }
        
        explodeNeighbors(exploded);
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
                ctx.iceLayer.draw();
                ctx.doorLayer.draw();
                
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
    
    public final double x(int col)
    {
        return col * cfg.size + cfg.size / 2.0;
    }
    
    public final double y(int row)
    {
        return (row + 1) * cfg.size + cfg.size / 2.0;
    }
    
    public int col(int x)
    {
        return x / cfg.size;
    }
    
    public int row(int y)
    {
        return y / cfg.size - 1;
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
        Set<Cell> neighbors = new Neighbors(cells);
        for (Cell c : neighbors)
        {
            if (c.canExplodeNextTo(cells))
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
            
            if (c instanceof Hole || c instanceof Slide || c instanceof Rock)
                return;
            
            //Debug.p("add neighbor " + c);
            add(c);
        }
    }
    
    public TransitionList moveBeasts()
    {
        return new TransitionList("fire/animal", ctx.nukeLayer, cfg.growFireDuration)
        {
            public void init()
            {
                //Debug.p("movebeasts");
                Set<Cell> verboten = new HashSet<Cell>();
                
                new MoveAnimals(this, verboten, ctx);
                        
                if (m_lastExplodedFire == ctx.score.getExplodedFire()) // don't grow fire if any fire exploded
                {
                    new GrowFire(this, verboten, ctx);
                }
            }
        };
    }
    
    public TransitionList explosions()
    {
        final List<Cell> cells = new ArrayList<Cell>();
        
        return new TransitionList("explosions", ctx.nukeLayer, cfg.explosionDuration)
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
                ctx.nukeLayer.setVisible(true);
                super.run();
            }
            
            public void done()
            {
                //Debug.p("explosions done");
                ctx.nukeLayer.draw();
                ctx.nukeLayer.setVisible(false);
                
                for (Cell cell : cells)
                {
                    cell.item.removeShapeFromLayer(ctx.dotLayer);
                    cell.item = null;
                }
                
                ctx.dotLayer.draw();
                updateScore();
            }
            
            public void init()
            {                
                for (int row = 0; row < numRows; row++)
                {
                    for (int col = 0; col < numColumns; col++)
                    {
                        Cell a = cell(col, row);
                        if (a.item instanceof Explody)
                        {
                            cells.add(a);
                        }
                    }
                }
                new Explosions(this, cells, ctx);
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
        };
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
        Explody ex = new Explody();
        ex.init(ctx);
        ex.moveShape(x(cell.col), y(cell.row));
        ex.addShapeToLayer(ctx.dotLayer);
        cell.item = ex;
    }
}
