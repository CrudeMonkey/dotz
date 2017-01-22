package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.enno.dotz.client.GridState.SwapCombo;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Bird;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.util.Debug;

public class GetSwapMatches
{
    private Context ctx;
    private GridState m_state;
    
    private List<Cell> m_explosions = new ArrayList<Cell>();
    private List<SwapCombo> m_hcombos = new ArrayList<SwapCombo>();
    private List<SwapCombo> m_vcombos = new ArrayList<SwapCombo>();
    private List<SwapCombo> m_combos = new ArrayList<SwapCombo>();
    private List<SwapCombo> m_eggCombos = new ArrayList<SwapCombo>();
    
    private boolean m_swapped;
    private Cell[] m_swaps = new Cell[2];
    private Random rnd;
    
    public GetSwapMatches(Context ctx)
    {
        this.ctx = ctx;
        rnd = ctx.generator.getRandom();
        m_state = ctx.state;
    }
    
    public static void doEggs(Context ctx, List<Cell> cells, Runnable whenDone)
    {
        Boolean cracked = null;
        for (Cell c : cells)
        {
            if (c.item instanceof Egg)
            {
                Egg egg = (Egg) c.item;
                cracked = egg.isCracked();
                break;
            }
        }
        
        GetSwapMatches g = new GetSwapMatches(ctx);
        SwapCombo combo = new SwapCombo();
        combo.addAll(cells);
        combo.setSpecial(cells.get(cells.size() - 1));
        combo.setType(cracked ? SwapCombo.CRACKED_EGG : SwapCombo.EGG);
        g.m_eggCombos.add(combo);
        g.animate(whenDone);
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
    
    public boolean hasEggCombos()
    {
        return m_eggCombos.size() > 0;
    }
    
    protected void animate2(final Runnable whenDone)
    {
        if (hasEggCombos())
        {
            TransitionList list = new TransitionList("eggs", ctx.dotLayer, 300) {
                @Override
                public void done()
                {
                    whenDone.run();
                }
            };
            
            boolean playCrack = false;
            boolean playBird = false;
            for (final SwapCombo combo : m_eggCombos)
            {
                if (combo.getType() == SwapCombo.EGG)
                    playCrack = true;
                else
                    playBird = true;
                
                final Cell special = combo.getSpecial();
                double x = m_state.x(special.col);
                double y = m_state.y(special.row);
                boolean first = true;
                for (final Cell c : combo)
                {
                    if (c != special)
                    {
                        final boolean theFirst = first;
                        first = false;
                        
                        list.add(new DropTransition(m_state.x(c.col), m_state.y(c.row), x, y, c.item) {
                            @Override
                            public void afterEnd()
                            {
                                c.item.removeShapeFromLayer(ctx.dotLayer);
                                c.item = null;
                                
                                if (theFirst)
                                {
                                    if (combo.getType() == SwapCombo.EGG)
                                    {
                                        special.item.removeShapeFromLayer(ctx.dotLayer);
                                        addItem(special, new Egg(true, false));
                                    }
                                    else
                                    {
                                        special.item.removeShapeFromLayer(ctx.dotLayer);
                                        special.item = null;
                                        ctx.score.addBird();

                                        new Bird().animate(special.col, special.row, ctx, null);
                                    }
                                }
                            }
                        });
                    }
                }
            }
            
            if (playBird)
            {
                Sound.CHICKEN.play();
            }
            if (playCrack)
                Sound.EGG_CRACK.play();
            
            list.run();
            
            return;
        }
        else
            animate3(whenDone);
    }
    
    protected void animate3(Runnable whenDone)
    {
        switch (m_combos.get(0).getType())
        {
            case SwapCombo.COLOR_BOMB:
                animateColorBomb(whenDone);
                return;
            case SwapCombo.TOTAL_BOMB:
                animateTotalBomb(m_state, whenDone);
                return;
            case SwapCombo.WRAP_BOMB:
                animateWrapBomb(whenDone);
                return;
            case SwapCombo.STRIPED_BOMB:
                animateStripedBomb(whenDone);
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
                        addItem(special, new ColorBomb(false));
                    }
                    break;
                }
                case SwapCombo.FOUR:
                {
                    Cell special = combo.getSpecial();
                    if (special.item == null)
                    {
                        addItem(special, new Striped(combo.getColor(), combo.isVertical()));
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
                case SwapCombo.WIDE_STRIPED:
                {
                    // not sure why only Striped is left over
                    int stripedIndex = combo.get(0).item instanceof Striped ? 0 : 1;
                    Striped striped = (Striped) combo.get(stripedIndex).item;
                    striped.armed = true;
                    striped.setWide(true);
                    striped.setBothWays(true);
                    
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
    

    protected void animateStripedBomb(Runnable whenDone)
    {
        SwapCombo combo = m_combos.get(0);
        int color = combo.getColor();
        
        Sound.DROP.play();
        
        Set<Cell> exploded = new HashSet<Cell>();
        
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
                if (item != null && !exploded.contains(cell) && cell.canExplode(color) && !cell.isLocked())
                {
                    if (cell.item instanceof Dot)
                    {
                        Striped striped = new Striped(((Dot) cell.item).color, rnd.nextBoolean());
                        striped.armed = true;

                        cell.item.removeShapeFromLayer(ctx.dotLayer);
                        addItem(cell, striped);
                    }
                    else if (cell.item instanceof Striped)
                    {
                        ((Striped) cell.item).armed = true;
                    }
                }
            }
        }
        
        m_state.explodeNeighbors(exploded);
        
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
    
    public static void animateTotalBomb(GridState state, Runnable whenDone)
    {
        Sound.DROP.play();
        
        for (int row = 0; row < state.numRows; row++)
        {
            for (int col = 0; col < state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
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
//        if (cell.item instanceof WrappedDot)
//            explodies.add(cell);            
//        
        exploded.add(cell);
        cell.explode(color, 1);
    }
    
    private void addItem(Cell cell, Item item)
    {
        m_state.addItem(cell, item);
    }

    public static boolean isSwapStart(Cell a)
    {
        if (a.item == null || a.isLocked())
            return false;
        
        return a.item.canSwap();
    }
            
    public boolean canSwap(Cell a, Cell b)
    {
        // Assumes 'a' can be swapped
        if (b.item == null || b.isLocked())
            return false;
        
        if (!b.item.canSwap())
            return false;
        
        Item x = a.item;
        a.item = b.item;
        b.item = x;
        
        m_swapped = true;
        m_swaps[0] = a;
        m_swaps[1] = b;
        
        findCombos();
        findEggCombos();
        if (m_combos.size() == 0 && m_eggCombos.size() == 0)
        {
            x = a.item;
            a.item = b.item;
            b.item = x;
            
            return false;
        }
        
        if (b.item instanceof Knight)   // start (!) item is a Knight
        {
            SwapCombo combo = new SwapCombo();
            combo.add(b);
            combo.setType(SwapCombo.KNIGHT);
            m_combos.add(combo);
        }
        
        return true;
    }

    protected static boolean isColorDot(Item item)
    {
        return item instanceof Dot || item instanceof DotBomb || item instanceof WrappedDot || item instanceof Wild || item instanceof Striped;
    }
    
    public boolean getTransitions()
    {
        findCombos();
        findEggCombos();
        return m_combos.size() > 0 || m_eggCombos.size() > 0;
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
                
                if (combo.size() >= 3 && !combo.isAllWild())
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
    
    public void findEggCombos()
    {
        // Horizontal combo            
        for (int row = 0; row < m_state.numRows; row++)
        {
            for (int col = 0; col < m_state.numColumns; col++)
            {
                Cell c = m_state.cell(col, row);
                if (c.isLocked() || !(c.item instanceof Egg || c.item instanceof Wild)) //TODO cage
                    continue;
                
                Boolean cracked = null;
                if (c.item instanceof Egg)
                {
                    Egg egg = (Egg) c.item;
                    cracked = egg.isCracked();
                }

                SwapCombo combo = new SwapCombo();
                combo.add(c);
                
                for (int col2 = col + 1; col2 < m_state.numColumns; col2++)                        
                {
                    Cell c2 = m_state.cell(col2, row);
                    if (c2.isLocked() || !(c2.item instanceof Egg || c2.item instanceof Wild))
                        break;
                    
                    if (c2.item instanceof Egg)
                    {
                        Egg egg2 = (Egg) c2.item;
                        if (cracked == null)
                        {
                            cracked = egg2.isCracked();
                        }
                        else if (egg2.isCracked() != cracked)
                            break;
                    }
                    
                    combo.add(c2);
                }
                
                if (combo.size() >= 3)
                {
                    identify(combo);
                    combo.setType(cracked ? SwapCombo.CRACKED_EGG : SwapCombo.EGG);
                    m_eggCombos.add(combo);
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
                if (eggCombosContain(c) || c.isLocked() || !(c.item instanceof Egg || c.item instanceof Wild))
                    continue;
                
                Boolean cracked = null;
                if (c.item instanceof Egg)
                {
                    Egg egg = (Egg) c.item;
                    cracked = egg.isCracked();
                }

                SwapCombo combo = new SwapCombo();
                combo.add(c);
                
                for (int row2 = row + 1; row2 < m_state.numRows; row2++)                        
                {
                    Cell c2 = m_state.cell(col, row2);
                    if (c2.isLocked() || !(c2.item instanceof Egg || c2.item instanceof Wild))
                        break;
                    
                    if (c2.item instanceof Egg)
                    {
                        Egg egg2 = (Egg) c2.item;
                        if (cracked == null)
                        {
                            cracked = egg2.isCracked();
                        }
                        else if (egg2.isCracked() != cracked)
                            break;
                    }
                    
                    combo.add(c2);
                }
                
                if (combo.size() >= 3)
                {
                    identify(combo);
                    combo.setType(cracked ? SwapCombo.CRACKED_EGG : SwapCombo.EGG);
                    m_eggCombos.add(combo);
                    row += combo.size() - 1;
                }                    
            }
        }
    }
    
    protected boolean eggCombosContain(Cell c)
    {
        for (SwapCombo combo : m_eggCombos)
        {
            if (combo.contains(c))
                return true;
        }
        return false;
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
            else if (b.item instanceof Striped)
            {
                return addCombo(SwapCombo.STRIPED_BOMB);
            }
        }
        else if (a.item instanceof WrappedDot)
        {
            if (b.item instanceof WrappedDot)
            {
                return addCombo(SwapCombo.EXPLODY_5);
            }
            else if (b.item instanceof Striped)
            {
                return addCombo(SwapCombo.WIDE_STRIPED);
            }
            // in Candy Crush each direction is separate
        }
        //TODO 2 Striped
        
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
        
        Cell c;
        int i = 0;
        do
        {
            c = combo.get(rnd.nextInt(combo.size()));
            i++;
            if (i > 50)
            {
                c = combo.get(0);
                Debug.p("can't find uncaged special"); // should never happen
                break;
            }
        }
        while (c.isLockedCage());
        
        combo.setSpecial(c);
        
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
        if (c.item == null || c.isLockedDoor() || m_explosions.contains(c))
            return false;
        
        //TODO specials
        return isColorDot(c.item) || c.item instanceof Animal;
    }

    private boolean canConnect(Cell c, SwapCombo combo)
    {
        if (c.item == null || c.isLockedDoor() || m_explosions.contains(c))
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