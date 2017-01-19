package com.enno.dotz.client.editor;

import java.util.Random;

import com.ait.tooling.nativetools.client.NArray;
import com.ait.tooling.nativetools.client.NObject;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Controller;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Animal.Action;
import com.enno.dotz.client.item.Animal.Type;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.Debug;
import com.enno.dotz.client.util.FrequencyGenerator;
import com.smartgwt.client.util.SC;

public class RandomGridGenerator
{
    public interface Properties
    {
        boolean x_symmetry();
        boolean y_symmetry();
        boolean isReplace();
        int cageStrength();
        int doorStrength();
        int doorRotation();
        int animalStrength();
        Animal.Type animalType();
        Animal.Action animalAction();
        int clockStrength();
        int knightStrength();
        int iceStrength();
        int blockerStrength();
        boolean isInteractive();
    }
    
    public static abstract class ExtendedProperties implements Properties
    {
        private EditorPropertiesPanel m_panel;

        protected ExtendedProperties(EditorPropertiesPanel panel)
        {
            m_panel = panel;
        }        

        @Override
        public int doorStrength()
        {
            return m_panel.getDoorStrength();
        }

        @Override
        public int animalStrength()
        {
            return m_panel.getAnimalStrength();
        }

        @Override
        public Type animalType()
        {
            return m_panel.getAnimalType();
        }

        @Override
        public Action animalAction()
        {
            return m_panel.getAnimalAction();
        }

        @Override
        public int clockStrength()
        {
            return m_panel.getClockStrength();
        }

        @Override
        public int knightStrength()
        {
            return m_panel.getKnightStrength();
        }

        @Override
        public int iceStrength()
        {
            return m_panel.getIceStrength();
        }
        
        @Override
        public int blockerStrength()
        {
            return doorStrength();
        }
        
        @Override
        public int cageStrength()
        {
            return doorStrength();
        }
        
        @Override
        public int doorRotation()
        {
            return m_panel.getDoorRotation();
        }
    }
    
    private static final int HOLE_ID = 1;
    private static final int TELEPORT_ID = 2;
    private static final int SLIDE_ID = 3;
    private static final int SUSAN_ID = 4;
    private static final int FIX_ID = 5;
    private static final int DOT_ID = 6;
    private static final int ANCHOR_ID = 7;
    private static final int WILD_ID = 8;
    private static final int ANIMAL_ID = 9;
    private static final int CLOCK_ID = 10;
    private static final int KNIGHT_ID = 11;
    private static final int FIRE_ID = 12;
    private static final int ICE_ID = 13;
    private static final int CIRCUIT_ID = 14;
    private static final int DOOR_ID = 15;
    private static final int DIRECTED_DOOR_ID = 16;
    private static final int CHANGE_COLOR_ID = 17;
    private static final int FILL_RANDOM_DOTS_ID = 18;
    private static final int LASER_ID = 19;
    private static final int MIRROR_ID = 20;
    private static final int ROCKET_ID = 21;
    private static final int CAGE_ID = 22;
    private static final int BLINKING_CAGE_ID = 23;
    private static final int BLINKING_DOOR_ID = 24;
    private static final int BLOCKER_ID = 25;
    private static final int BUBBLE_ID = 26;
    private static final int DIAMOND_ID = 27;
    private static final int ZAP_BLOCKER_ID = 28;
    private static final int CHEST_ID = 29;
    
    private Context ctx;
    private GridState state;

    private static Random rnd = new Random();
    private int nc;
    private int nr;
    
    public RandomGridGenerator(Context ctx)
    {
        this.ctx = ctx;        
    }
    
    public void useGrid(GridState g)
    {
        state = g;
        nc = g.numColumns;
        nr = g.numRows;
    }
    
    public void createGrid(int numColumns, int numRows)
    {
        state = new GridState(numColumns, numRows);
        nc = numColumns;
        nr = numRows;
        
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                state.setCell(col,  row, new ItemCell());
            }
        }
    }
    
    protected boolean rb()
    {
        return rnd.nextBoolean();
    }
    
    protected int ri(int from, int to)
    {
        try
        {
            return from + rnd.nextInt(to - from + 1);
        }
        catch (RuntimeException e)
        {
            Debug.p("from=" + from + " to=" + to, e);
            throw e;
        }
    }
    
    protected int rh(int from, int to)
    {
        if (from == to)
            return from;
        
        if (rb())
            return from;        
        
        return rh(from + 1, to);
    }
    
    public void randomize(int fid, Properties props)
    {
        switch (fid)
        {
            case HOLE_ID: 
            {
                new HoleFeature().gen(props);
                break;
            }
            case TELEPORT_ID: 
            {
                new TeleportFeature().gen(props);
                break;                
            }
            case CIRCUIT_ID: 
            {
                new CircuitFeature().gen(props);
                break;                
            }
            case DOOR_ID: 
            {
                new DoorFeature(false).gen(props);
                break;                
            }
            case BUBBLE_ID: 
            {
                new BubbleFeature().gen(props);
                break;                
            }
            case DIRECTED_DOOR_ID: 
            {
                new DoorFeature(true).gen(props);
                break;                
            }
            case CAGE_ID: 
            {
                new CageFeature().gen(props);
                break;                
            }
            case BLINKING_CAGE_ID: 
            {
                new BlinkingCageFeature().gen(props);
                break;                
            }
            case BLINKING_DOOR_ID: 
            {
                new BlinkingDoorFeature().gen(props);
                break;                
            }
            case SLIDE_ID: 
            {
                new SlideFeature().gen(props);
                break;                
            }
            case SUSAN_ID: 
            {
                new SusanFeature().gen(props);
                break;                
            }
            case FIX_ID: 
            {
                new FixFeature().gen(props);
                break;                
            }
            case DOT_ID: 
            {
                new DotFeature().gen(props);
                break;                
            }
            case ANCHOR_ID: 
            {
                new AnchorFeature().gen(props);
                break;                
            }
            case DIAMOND_ID: 
            {
                new DiamondFeature().gen(props);
                break;                
            }
            case WILD_ID: 
            {
                new WildCardFeature().gen(props);
                break;                
            }
            case FIRE_ID: 
            {
                new FireFeature().gen(props);
                break;                
            }
            case BLOCKER_ID: 
            {
                new BlockerFeature(false).gen(props);
                break;                
            }
            case ZAP_BLOCKER_ID: 
            {
                new BlockerFeature(true).gen(props);
                break;                
            }
            case ICE_ID: 
            {
                new IceFeature().gen(props);
                break;                
            }
            case ANIMAL_ID: 
            {
                new AnimalFeature().gen(props);
                break;                
            }
            case CLOCK_ID: 
            {
                new ClockFeature().gen(props);
                break;                
            }
            case KNIGHT_ID: 
            {
                new KnightFeature().gen(props);
                break;                
            }
            case LASER_ID: 
            {
                new LaserFeature().gen(props);
                break;                
            }
            case MIRROR_ID: 
            {
                new MirrorFeature().gen(props);
                break;                
            }
            case ROCKET_ID: 
            {
                new RocketFeature().gen(props);
                break;                
            }
            case CHEST_ID: 
            {
                new ChestFeature().gen(props);
                break;                
            }
            case CHANGE_COLOR_ID: 
            {
                new ChangeColorFeature().gen(props);
                break;                
            }
            case FILL_RANDOM_DOTS_ID: 
            {
                new FillRandomDotsFeature().gen(props);
                break;                
            }
        }        
    }
    
    FrequencyGenerator<Integer> m_freqGen = createFrequencyGenerator();

    private FrequencyGenerator<Integer> createFrequencyGenerator()
    {
        FrequencyGenerator<Integer> f = new FrequencyGenerator<Integer>();
        f.add(50, HOLE_ID);
        f.add(10, TELEPORT_ID);
        f.add(10, SLIDE_ID);
        f.add(10, SUSAN_ID);
        f.add(10, ANIMAL_ID);
        f.add(10, CLOCK_ID);
        f.add(10, KNIGHT_ID);
        f.add(10, FIRE_ID);
        f.add(10, ICE_ID);
        f.add(30, CIRCUIT_ID);
        f.add(30, DOOR_ID);
        f.add(20, DIRECTED_DOOR_ID);
        f.add(20, CHANGE_COLOR_ID);
        f.add(10, LASER_ID);
        f.add(10, MIRROR_ID);
        f.add(10, ROCKET_ID);        
        f.add(20, CHEST_ID);        
        f.add(30, BLOCKER_ID);
        f.add(10, ZAP_BLOCKER_ID);
        f.add(30, CAGE_ID);
        f.add(10, BLINKING_CAGE_ID);
        f.add(10, BLINKING_DOOR_ID);
        return f;
    }
    
    public GridState getNextState(int nc, int nr, EditorPropertiesPanel editorProps)
    {
        createGrid(nc, nr);
        
        final boolean replace = rb();
        try
        {
            final boolean x = true;
            
            for (int i = 0, n = ri(2, 5); i < n; i++)
            {
                final boolean y = rb();

                randomize(m_freqGen.next(), new ExtendedProperties(editorProps) {
    
                    @Override
                    public boolean x_symmetry()
                    {
                        return x;
                    }
    
                    @Override
                    public boolean y_symmetry()
                    {
                        return y;
                    }
    
                    @Override
                    public boolean isReplace()
                    {
                        return replace;
                    }
    
                    @Override
                    public boolean isInteractive()
                    {
                        return false;
                    }
               });
            }
            
            Properties props = new ExtendedProperties(editorProps) {
    
                @Override
                public boolean x_symmetry()
                {
                    return false;
                }
    
                @Override
                public boolean y_symmetry()
                {
                    return false;
                }
    
                @Override
                public boolean isReplace()
                {
                    return false;
                }
    
                @Override
                public boolean isInteractive()
                {
                    return false;
                }
            };
            
            randomize(FIX_ID, props);
            randomize(FILL_RANDOM_DOTS_ID, props);
        }
        catch (Exception e)
        {
            Debug.p("RandomGridGenerator error", e);
        }
        return getState();
    }
    

    public GridState getState()
    {       
        return state;
    }
    
    public class Feature
    {
        public static final int MAX_LOOP = 200;
        
        protected void replaceCell(int col, int row, Cell c)
        {
            Item item = state.cell(col, row).item;
            c.item = item;
            state.setCell(col, row, c);
        }
        
        protected boolean isSusan(int col, int row)
        {
            for (LazySusan su : state.getLazySusans())
            {
                int c = su.getCol();
                int r = su.getRow();
                if ((c == col || c + 1 == col) && (r == row || r + 1 == row))
                    return true;
            }
            return false;
        }

        protected boolean valid(int col, int row)
        {
            return col >= 0 && col < nc && row >= 0 && row < nr;
        }

        protected int normalizeCol(int col)
        {
            col = col % nc;
            return col < 0 ? col + nc : col;
        }

        protected int normalizeRow(int row)
        {
            row = row % nr;
            return row < 0 ? row + nr : row;
        }
        
        protected boolean isTeleportTarget(int col, int row)
        {
            if (!valid(col, row))
                return false;
            
            return state.cell(col, row).isTeleportTarget();
        }

        protected boolean isTeleportSource(int col, int row)
        {
            if (!valid(col, row))
                return false;
            
            return state.cell(col, row).isTeleportSource();
        }
        
        protected void tooManyTries(Properties props)
        {
            if (props.isInteractive())
                SC.say("Can't find a spot to place this feature.");
        }
        
        protected int randomDirection()
        {
            return Direction.randomDirection(rnd);
        }
        
        protected int mirrorDirection(int dir, boolean x_sym, boolean y_sym)
        {
            if (x_sym)
            {
                if (dir == Direction.EAST)
                    dir = Direction.WEST;
                else if (dir == Direction.WEST)
                    dir = Direction.EAST;
            }
            if (y_sym)
            {
                if (dir == Direction.NORTH)
                    dir = Direction.SOUTH;
                else if (dir == Direction.SOUTH)
                    dir = Direction.NORTH;
            }
            return dir;
        }
    }

    public class FixFeature extends Feature
    {
        public void gen(Properties props)
        {
            for (int row = 0; row < nr; row++)
            {
                for (int col = 0; col < nc; col++)
                {
                    Cell c = state.cell(col, row);
                    if (row > 0 && c.isTeleportTarget())
                        checkAboveTarget(col, row - 1);
                    
                    if (row < nr - 1 && (c.isTeleportSource() || c instanceof Slide))
                        checkBelowSlide(col, row + 1);
                }
            }
        }
        
        protected void checkBelowSlide(int col, int row)
        {
            if (!valid(col, row))
                return;
            
            Cell c = state.cell(col, row);
            if (c.isTeleportTarget())
                return; // ok
            
            if (isSusan(col, row))
                return; // ok
            
            if (valid(col - 1, row))
            {
                Cell left = state.cell(col - 1, row);
                if (left instanceof Slide && !((Slide) left).isToLeft())
                    return;
            }
            if (valid(col + 1, row))
            {
                Cell right = state.cell(col + 1, row);
                if (right instanceof Slide && ((Slide) right).isToLeft())
                    return;
            }
            
            state.setCell(col, row, new Hole());
            checkBelowSlide(col, row + 1);
        }
        
        protected void checkAboveTarget(int col, int row)
        {
            if (!valid(col, row))
                return;
            
            Cell c = state.cell(col, row);
            if (c.isTeleportSource() || c instanceof Slide)
                return; // ok
            
            if (isSusan(col, row))
                return; // ok
            
            state.setCell(col, row, new Hole());
            
            checkAboveTarget(col, row - 1);
        }
    }

    public class FillRandomDotsFeature extends Feature
    {
        public void gen(Properties props)
        {
            for (int row = 0; row < nc; row++)
            {
                for (int col = 0; col < nc; col++)
                {
                    Cell c = state.cell(col, row);
                    if (c.item == null && c.canContainItems())
                        c.item = new RandomItem();
                }
            }
        }
    }
    
    public class TeleportFeature extends Feature
    {
        public void gen(Properties props)
        {
            boolean x_sym = props.x_symmetry();
            boolean y_sym = props.y_symmetry();
            boolean replace = props.isReplace();
            
            for (int loop = 0; loop < MAX_LOOP; loop++)
            {
                int x_src = ri(0, nc - 1);
                int y_src = ri(0, nc - 1);
                int x_target = ri(0, nc - 1);
                int y_target = ri(0, nc - 1);
                if (x_src == x_target)
                    continue;
                
                if (!checkTeleport(x_src, y_src, x_target, y_target, replace, x_sym, y_sym))
                    continue;
                
                if (teleport(x_src, y_src, x_target, y_target, replace, x_sym, y_sym))
                    return;
            }
            tooManyTries(props);
        }

        private boolean teleport(int x_src, int y_src, int x_target, int y_target, boolean replace, boolean x_sym, boolean y_sym)
        {
            // Never replace other teleporters
            Cell src = state.cell(x_src, y_src);
            
            if (!replace)
            {
                if (!(src instanceof ItemCell || src instanceof Hole))
                    return false;
            }
            if (src instanceof Teleport)
                return false;
            
            Cell target = state.cell(x_target, y_target);
            if (!replace)
            {
                if (!(target instanceof ItemCell || target instanceof Hole))
                    return false;
            }
            if (target instanceof Teleport)
                return false;
            
            Teleport s = new Teleport(false);
            s.setOther(x_target, y_target);
            replaceCell(x_src, y_src, s);
            
            Teleport t = new Teleport(true);
            t.setOther(x_src, y_src);
            replaceCell(x_target, y_target, t);
            
            if (x_sym)
            {
                teleport(nc - 1 - x_src, y_src, nc - 1 - x_target, y_target, replace, false, false);
            }
            if (y_sym)
            {
                teleport(x_target, nr - 1 - y_target, x_src, nr - 1 - y_src, replace, false, false);
            }
            if (x_sym && y_sym)
            {
                teleport(nc - 1 - x_target, nr - 1 - y_target, nc - 1 - x_src, nr - 1 - y_src, replace, false, false);
            }
            
            return true;
        }

        private boolean checkTeleport(int x_src, int y_src, int x_target, int y_target, boolean replace, boolean x_sym, boolean y_sym)
        {
            // Never replace other teleporters
            Cell src = state.cell(x_src, y_src);
            
            if (!replace)
            {
                if (!(src instanceof ItemCell || src instanceof Hole) || isSusan(x_src, y_src))
                    return false;
            }
            if (src instanceof Teleport)
                return false;
            
            Cell target = state.cell(x_target, y_target);
            if (!replace)
            {
                if (!(target instanceof ItemCell || target instanceof Hole) || isSusan(x_target, y_target))
                    return false;
            }
            if (target instanceof Teleport)
                return false;
            
            // Can't have 2 sources (or 2 targets) above each other
            if (isTeleportSource(x_src, y_src - 1) || isTeleportSource(x_src, y_src + 1)
             || isTeleportTarget(x_target, y_target - 1) || isTeleportTarget(x_target, y_target + 1))
                return false;                
            
            if (x_sym)
            {
                if (!checkTeleport(nc - 1 - x_src, y_src, nc - 1 - x_target, y_target, replace, false, false))
                    return false;
            }
            if (y_sym)
            {
                if (!checkTeleport(x_target, nr - 1 - y_target, x_src, nr - 1 - y_src, replace, false, false))
                    return false;
            }
            if (x_sym && y_sym)
            {
                if (!checkTeleport(nc - 1 - x_target, nr - 1 - y_target, nc - 1 - x_src, nr - 1 - y_src, replace, false, false))
                    return false;
            }
            
            return true;
        }

    }
    
    public class SlideFeature extends Feature
    {
        public void gen(Properties props)
        {
            boolean x_sym = props.x_symmetry();
            boolean replace = props.isReplace();

            int w2 = nc / 2;
            boolean even = (nc % 2) == 0;
            
            LOOP: for (int loop = 0; loop < MAX_LOOP; loop++)
            {
                int x, y, repeat;
                boolean left = rb();

                if (x_sym)
                {
                    if (even)
                    {
                        repeat = ri(1, Math.min(3, w2 - 1));
                        x = (rb() ? 0 : w2) + (left ? ri(repeat, w2 - 1) : ri(0, w2 - repeat - 1));
                    }
                    else
                    {
                        repeat = ri(1, Math.min(3, w2));
                        x = (rb() ? 0 : w2) + (left ? ri(repeat, w2) : ri(0, w2 - repeat));
                    }
                }
                else
                {
                    repeat = ri(1, Math.min(3, nc - 1));
                    if (left)
                        x = ri(repeat, nc - 1);
                    else
                        x = ri(0, nc - repeat - 1);
                }
                y = ri(0, nr - repeat);
                
                for (int i = 0; i < repeat; i++)
                {
                    if (!replace && !checkSlide(x + (left ? -i : i), y + i, left, x_sym))
                        continue LOOP;
                }
                
                for (int i = 0; i < repeat; i++)
                {
                    slide(x + (left ? -i : i), y + i, left, x_sym);
                }
                return;
            }
            tooManyTries(props);
        }

        private void slide(int col, int row, boolean left, boolean symmetry)
        {
            state.setCell(col, row, new Slide(left));
            if (row > 0)
            {
                // above
                ensureCell(col, row - 1, true);
            }
            if (left)
            {
                ensureCell(col - 1, row, false);
            }
            else
            {
                ensureCell(col + 1, row, false);
            }
            
            if (symmetry)
            {
                slide(nc - 1 - col, row, !left, false);
            }
        }

        private void ensureCell(int col, int row, boolean above)
        {
            if (!valid(col, row))
            {
                // should never happen
                Debug.p("slide bad coord " + col + "," + row);
                return;
            }
            
            Cell c = state.cell(col, row);
            if (!(c instanceof Hole || c instanceof Slide || (above && c.isTeleportSource())))
                return;
            
            replaceCell(col, row, new ItemCell());
        }
        
        private boolean checkSlide(int col, int row, boolean left, boolean symmetry)
        {
            Cell c = state.cell(col, row);
            if (!(c instanceof ItemCell || c instanceof Hole) || isSusan(col, row))
                return false;
            
            if (row > 0)
            {
                // above
                if (!checkCell(col, row - 1, true))
                    return false;
            }
            if (row < nr - 1)
            {
                // can't have slide below
                if (state.cell(col, row + 1) instanceof Slide)
                    return false;
            }
            
            if (left)
            {
                if (!checkCell(col - 1, row, false))
                    return false;
                
                // can't have left slide to the right
                if (col < nc - 1)
                {
                    Cell c2 = state.cell(col + 1, row);
                    if (c2 instanceof Slide && ((Slide) c2).isToLeft())
                        return false;
                }
            }
            else
            {
                if (!checkCell(col + 1, row, false))
                    return false;
                
                // can't have right slide to the left
                if (col > 0)
                {
                    Cell c2 = state.cell(col - 1, row);
                    if (c2 instanceof Slide && !((Slide) c2).isToLeft())
                        return false;
                }
            }
            
            if (symmetry)
            {
                if (!checkSlide(nc - 1 - col, row, !left, false))
                    return false;
            }
            return true;
        }

        private boolean checkCell(int col, int row, boolean above)
        {
            if (!valid(col, row))
            {
                // should never happen
                Debug.p("slide bad coord " + col + "," + row);
                return false;
            }
            
            Cell c = state.cell(col, row);
            if (c instanceof Hole || c instanceof Slide || (above && c.isTeleportSource()))
                return false;
            
            return true;
        }
    }

    public abstract class PatternFeature extends Feature
    {
        protected int m_min;
        protected int m_max;
        protected boolean m_repeat;
        protected boolean m_checker;
        protected int m_maxWide;
        protected int m_maxHigh;
        
        protected PatternFeature(int min, int max, boolean repeat, boolean checker, int maxWide, int maxHigh)
        {
            m_min = min;
            m_max = max;
            m_repeat = repeat;
            m_checker = checker;
            m_maxWide = maxWide;
            m_maxHigh = maxHigh;
        }
        
        public void gen(Properties props)
        {
            for (int i = 0, n = rh(m_min, m_max); i < n; i++)
            {
                gen2(props);
            }
        }
        
        protected void gen2(Properties props)
        {
            boolean x_sym = props.x_symmetry();
            boolean y_sym = props.x_symmetry();
            boolean replace = props.isReplace();
            
            boolean wide = rb();
            
            int repeat_n = 1, repeat_dist = 0;
            boolean checker = false;
            boolean repeat = false;
            
            if (m_checker && m_repeat)
            {
                int feat = ri(1, 3);
                checker = feat == 2;
                repeat = feat == 3;
            }
            else if (m_checker)
            {
                int feat = ri(1, 2);
                checker = feat == 1;
            }
            else if (m_repeat)
            {
                int feat = ri(1, 2);
                repeat = feat == 1;
            }
            
            if (repeat)
            {
                repeat_n = ri(2, 4);
                repeat_dist = ri(2, 4);
            }
                
            if (wide)
            {
                int w = rh(1, m_maxWide);
                int x = ri(0, nc - w);
                int h = rh(1, Math.min(m_maxHigh, checker ? nr / 2 : nr / 4));
                int y = ri(0, checker ? nr/2 - h : nr - h);
                
                for (int j = 0, row = y; j < h; row++, j++)
                {
                    for (int i = 0, col = x; i < w; col++, i++)
                    {
                        place(col, row, checker, replace, x_sym, y_sym, repeat_n, repeat_dist);
                    }
                }
            }
            else
            {
                int w = rh(1, Math.min(m_maxHigh, checker ? nc / 2 : nc / 4));
                int x = ri(0, checker ? nc/2 - w : nc - w);
                int h = rh(1, m_maxWide);
                int y = ri(0, nr - h);
                
                for (int j = 0, row = y; j < h; row++, j++)
                {
                    for (int i = 0, col = x; i < w; col++, i++)
                    {
                        place(col, row, checker, replace, x_sym, y_sym, repeat_n, repeat_dist);
                    }
                }
            }
        }
        
        protected void place(int col, int row, boolean checker, boolean replace, boolean x_sym, boolean y_sym, int repeat_n, int repeat_dist)
        {
            col = normalizeCol(col);
            row = normalizeRow(row);
            
            placeFeature(col, row, checker, replace, false, false);
            
            if (x_sym)
            {
                placeFeature(nc - col - 1, row, checker, replace, true, false);
            }
            if (y_sym)
            {
                placeFeature(col, nr - row - 1, checker, replace, false, true);
            }
            if (x_sym && y_sym)
            {
                placeFeature(nc - col - 1, nr - row - 1, checker, replace, true, true);
            }
            
            if (repeat_n > 1)
                place(col, row + repeat_dist, replace, checker, x_sym, y_sym, repeat_n - 1, repeat_dist);
        }

        protected abstract void placeFeature(int col, int row, boolean checker, boolean replace, boolean x_xym, boolean y_sym);
    }
    
    public class HoleFeature extends PatternFeature
    {        
        public HoleFeature()
        {
            super(3, 8, true, true, 8, 4);
        }
        
        protected void placeFeature(int col, int row, boolean checker, boolean replace, boolean x_sym, boolean y_sym)
        {
            if (!checker || (col % 2) == (row % 2))
            {
                if (!replace)
                {
                    if (!(state.cell(col, row) instanceof ItemCell) || isSusan(col, row))
                        return;
                }
                state.setCell(col, row, new Hole());
            }
        }
    }
    
    public class IceFeature extends PatternFeature
    {
        private int m_iceStrength;

        public IceFeature()
        {
            super(3, 6, true, true, 6, 3);
        }
        
        public void gen(Properties props)
        {
            m_iceStrength = props.iceStrength();
            super.gen(props);
        }
        
        @Override
        protected void placeFeature(int col, int row, boolean checker, boolean replace, boolean x_sym, boolean y_sym)
        {
            if (!checker || (col % 2) == (row % 2))
            {
                Cell c = state.cell(col, row);
                if (!c.canHaveIce())
                    return;
                
                c.ice = m_iceStrength;
            }
        }
    }

    public abstract class CellFeature extends PatternFeature
    {
        protected CellFeature(int min, int max, boolean repeat, boolean checker, int maxWide, int maxHigh)
        {
            super(min, max, repeat, checker, maxWide, maxHigh);
        }
        
        @Override
        protected void placeFeature(int col, int row, boolean checker, boolean replace, boolean x_sym, boolean y_sym)
        {
            if (!checker || (col % 2) == (row % 2))
            {
                if (!replace)
                {
                    Cell c = state.cell(col, row);
                    if (!(c instanceof ItemCell || c instanceof Hole))
                        return;
                }                
                replaceCell(col, row, createCell(x_sym, y_sym));
            }
        }
        
        protected abstract Cell createCell(boolean x_sym, boolean y_sym);
    }

    public class CircuitFeature extends CellFeature
    {
        public CircuitFeature()
        {
            super(1, 3, false, false, 6, 2);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new CircuitCell();
        }
    }

    public class ChangeColorFeature extends CellFeature
    {
        public ChangeColorFeature()
        {
            super(2, 4, false, false, 3, 1);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new ChangeColorCell();
        }
    }

    public class DoorFeature extends CellFeature
    {
        private boolean m_directed;
        private int m_direction;
        private int m_doorStrength;
        private int m_doorRotation;
        
        public DoorFeature(boolean directed)
        {
            super(1, 3, false, false, 4, 2);
            m_directed = directed;
        }
        
        public void gen(Properties props)
        {
            m_doorStrength = props.doorStrength();
            m_doorRotation = props.doorRotation();
            
            for (int i = 0, n = ri(m_min, m_max); i < n; i++)
            {
                m_direction = m_directed ? randomDirection() : Direction.NONE;
                gen2(props);
            }
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            int dir = mirrorDirection(m_direction, x_sym, y_sym);
            return new Door(m_doorStrength, dir, x_sym == y_sym ? m_doorRotation : -m_doorRotation);
        }
    }

    public class BubbleFeature extends CellFeature
    {
        public BubbleFeature()
        {
            super(1, 3, false, false, 3, 1);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new Bubble();
        }
    }
    
    public class BlinkingDoorFeature extends CellFeature
    {
        public BlinkingDoorFeature()
        {
            super(1, 3, false, false, 3, 1);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new Door(Controller.ON_OFF);
        }
    }
    
    public class CageFeature extends CellFeature
    {
        private int m_cageStrength;
        
        public CageFeature()
        {
            super(1, 3, false, false, 4, 2);
        }
        
        public void gen(Properties props)
        {
            m_cageStrength = props.cageStrength();
            super.gen(props);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new Cage(m_cageStrength, false);
        }
    }
    
    public class BlinkingCageFeature extends CellFeature
    {
        public BlinkingCageFeature()
        {
            super(1, 3, false, false, 3, 1);
        }
        
        @Override
        protected Cell createCell(boolean x_sym, boolean y_sym)
        {
            return new Cage(Controller.ON_OFF);
        }
    }
    
    public abstract class ItemFeature extends PatternFeature
    {
        protected int m_color;
        protected Properties m_props;
        protected boolean m_stuck;
        
        protected ItemFeature(int min, int max, int maxWide, int maxHigh)
        {
            super(min, max, true, true, maxWide, maxHigh);
        }
        
        @Override
        public void gen(Properties props)
        {
            m_color = ri(0, Config.MAX_COLORS - 1);
            m_props = props;
            
            super.gen(props);
        }
        
        @Override
        protected void placeFeature(int col, int row, boolean checker, boolean replace, boolean x_sym, boolean y_sym)
        {
            if (!checker || (col % 2) == (row % 2))
            {
                Cell cell = state.cell(col, row);
                if (!cell.canContainItems())
                    return;
                
                if (!replace)
                {
                    if (cell.item != null && !(cell.item instanceof RandomItem))
                        return;
                }                
                cell.item = createItem(x_sym, y_sym);
            }
        }
        
        protected abstract Item createItem(boolean x_sym, boolean y_sym);
    }

    public class DotFeature extends ItemFeature
    {
        public DotFeature()
        {
            super(3, 6, 4, 2);
        }
        
        @Override
        public void gen2(Properties props)
        {
            m_color = ri(0, Config.MAX_COLORS - 1);
            
            super.gen2(props);
        }
        
        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Dot(m_color, null, m_stuck, false); //TODO support radioActive
        }
    }
    
    public class WildCardFeature extends ItemFeature
    {
        public WildCardFeature()
        {
            super(1, 3, 3, 2);
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Wild(m_stuck);
        }
    }
    
    public class AnchorFeature extends ItemFeature
    {
        public AnchorFeature()
        {
            super(1, 3, 3, 2);
            m_repeat = false;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Anchor(m_stuck);
        }
    }
    
    public class DiamondFeature extends ItemFeature
    {
        public DiamondFeature()
        {
            super(1, 3, 3, 2);
            m_repeat = false;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Diamond();
        }
    }
    
    public class FireFeature extends ItemFeature
    {
        public FireFeature()
        {
            super(1, 3, 1, 1);
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Fire(m_stuck);
        }
    }
    
    public class BlockerFeature extends ItemFeature
    {
        private boolean m_zapOnly;

        public BlockerFeature(boolean zapOnly)
        {
            super(1, 3, zapOnly ? 1 : 4, zapOnly ? 1 : 2);
            m_zapOnly = zapOnly;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Blocker(m_props.blockerStrength(), m_stuck, m_zapOnly);
        }
    }
    
    public class ClockFeature extends ItemFeature
    {
        public ClockFeature()
        {
            super(1, 3, 1, 1);
            m_repeat = false;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Clock(m_props.clockStrength(), m_stuck);
        }
    }
    
    public class KnightFeature extends ItemFeature
    {
        public KnightFeature()
        {
            super(2, 4, 1, 1);
            m_repeat = false;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Knight(m_props.knightStrength(), m_stuck);
        }
    }
    
    public class AnimalFeature extends ItemFeature
    {
        public AnimalFeature()
        {
            super(1, 2, 1, 1);
            m_repeat = false;
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Animal(m_color, m_props.animalStrength(), m_props.animalType(), m_props.animalAction(), m_stuck);
        }
    }
    
    public class LaserFeature extends ItemFeature
    {
        protected int m_direction;
        
        public LaserFeature()
        {
            super(1, 2, 1, 1);
            m_repeat = false;
            m_direction = randomDirection();
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            int dir = mirrorDirection(m_direction, x_sym, y_sym);
            return new Laser(dir, m_stuck);
        }
    }
    
    public class MirrorFeature extends ItemFeature
    {
        private boolean m_flipped;

        public MirrorFeature()
        {
            super(1, 2, 1, 1);
            m_repeat = false;
            m_flipped = rb();
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            boolean flipped = x_sym == y_sym ? m_flipped : !m_flipped;
            return new Mirror(flipped, m_stuck);
        }
    }
    
    public class RocketFeature extends ItemFeature
    {
        private int m_direction;

        public RocketFeature()
        {
            super(1, 2, 1, 1);
            m_repeat = false;
            m_direction = randomDirection();
        }

        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            int dir = mirrorDirection(m_direction, x_sym, y_sym);
            return new Rocket(dir, m_stuck);
        }
    }
    
    public class ChestFeature extends ItemFeature
    {
        private int m_doorStrength;

        public ChestFeature()
        {
            super(1, 3, 2, 4);
            m_repeat = false;
        }

        @Override
        public void gen(Properties props)
        {
            m_doorStrength = props.doorStrength();
            super.gen(props);
        }
        
        @Override
        protected Item createItem(boolean x_sym, boolean y_sym)
        {
            return new Chest(null, m_doorStrength); //TODO wrap existing item
        }
    }
    
    public class SusanFeature extends Feature
    {
        public void gen(Properties props)
        {
            boolean x_sym = props.x_symmetry();
            boolean y_sym = props.y_symmetry();
            boolean replace = props.isReplace();
            boolean left = rb();
            
            for (int loop = 0; loop < MAX_LOOP; loop++)
            {
                int x, y;
                if (x_sym)
                    x = ri(0, nc / 2 - 2);
                else
                    x = ri(0, nc - 2);
                if (y_sym)
                    y = ri(0, nr / 2 - 2);
                else 
                    y = ri(0, nr - 2);
                
                if (!checkSusan(x, y, replace, x_sym, y_sym))
                    continue;
                
                susan(left, x, y, replace, x_sym, y_sym);
                return;
            }
        }
        
        private void susan(boolean left, int col, int row, boolean replace, boolean x_sym, boolean y_sym)
        {
            LazySusan su = new LazySusan(col, row, left);
            state.add(su);
            
            if (state.cell(col, row) instanceof Hole)
                state.setCell(col, row, new ItemCell());
            if (state.cell(col + 1, row) instanceof Hole)
                state.setCell(col + 1, row, new ItemCell());
            if (state.cell(col, row + 1) instanceof Hole)
                state.setCell(col, row + 1, new ItemCell());
            if (state.cell(col + 1, row + 1) instanceof Hole)
                state.setCell(col + 1, row + 1, new ItemCell());
            
            if (x_sym)
            {
                susan(!left, nc - 2 - col, row, replace, false, false);
            }
            if (y_sym)
            {
                susan(!left, col, nr - 2 - row, replace, false, false);
            }
            if (x_sym && y_sym)
            {
                susan(left, nc - 2 - col, nr - 2 - row, replace, false, false);
            }
        }

        private boolean checkSusan(int col, int row, boolean replace, boolean x_sym, boolean y_sym)
        {
            if (replace)
                return true;
            
            if (bad(col, row) || bad(col + 1, row) || bad(col, row + 1) || bad(col + 1, row + 1))
                return false;
            
            if (x_sym)
            {
                if (!checkSusan(nc - 2 - col, row, replace, false, false))
                    return false;
            }
            if (y_sym)
            {
                if (!checkSusan(col, nr - 2 - row, replace, false, false))
                    return false;
            }
            if (x_sym && y_sym)
            {
                if (!checkSusan(nc - 2 - col, nr - 2 - row, replace, false, false))
                    return false;
            }
            return true;
        }
        
        private boolean bad(int col, int row)
        {
            Cell c = state.cell(col, row);
            if (!(c instanceof ItemCell || c instanceof Hole) || isSusan(col, row))
                return true;
            
            return false;
        }
    }
    
    public static NArray getLeftFeatures()
    {
        NArray a = new NArray();
        a.push(new NObject("id", HOLE_ID).put("name", "Holes"));
        a.push(new NObject("id", SLIDE_ID).put("name", "Slides"));
        a.push(new NObject("id", BUBBLE_ID).put("name", "Bubbles"));
        a.push(new NObject("id", DOOR_ID).put("name", "Simple Doors"));
        a.push(new NObject("id", DIRECTED_DOOR_ID).put("name", "Directed Doors"));
        a.push(new NObject("id", BLINKING_DOOR_ID).put("name", "Blinking Doors"));
        a.push(new NObject("id", CAGE_ID).put("name", "Cages"));
        a.push(new NObject("id", BLINKING_CAGE_ID).put("name", "Blinking Cages"));
        a.push(new NObject("id", TELEPORT_ID).put("name", "Teleporters"));
        a.push(new NObject("id", CIRCUIT_ID).put("name", "Circuits"));
        a.push(new NObject("id", CHANGE_COLOR_ID).put("name", "Color Changers"));
        a.push(new NObject("id", SUSAN_ID).put("name", "Lazy Susans"));
        a.push(new NObject("id", ICE_ID).put("name", "Ice"));
        a.push(new NObject("id", FIX_ID).put("name", "Fix"));
        a.push(new NObject("id", FILL_RANDOM_DOTS_ID).put("name", "Fill Random Dots"));
        return a;
    }
    
    public static NArray getRightFeatures()
    {
        NArray a = new NArray();
        
        a.push(new NObject("id", DOT_ID).put("name", "Dots"));
        a.push(new NObject("id", WILD_ID).put("name", "Wild Cards"));
        a.push(new NObject("id", ANCHOR_ID).put("name", "Anchors"));
        a.push(new NObject("id", DIAMOND_ID).put("name", "Diamonds"));
        a.push(new NObject("id", FIRE_ID).put("name", "Fire"));
        a.push(new NObject("id", ANIMAL_ID).put("name", "Animals"));
        a.push(new NObject("id", CLOCK_ID).put("name", "Clocks"));
        a.push(new NObject("id", KNIGHT_ID).put("name", "Knights"));
        a.push(new NObject("id", LASER_ID).put("name", "Lasers"));
        a.push(new NObject("id", MIRROR_ID).put("name", "Mirrors"));
        a.push(new NObject("id", ROCKET_ID).put("name", "Rockets"));
        a.push(new NObject("id", CHEST_ID).put("name", "Chests"));
        a.push(new NObject("id", BLOCKER_ID).put("name", "Blockers (Green)"));
        a.push(new NObject("id", ZAP_BLOCKER_ID).put("name", "Zap Blockers (Red)"));
        return a;
    }
}
