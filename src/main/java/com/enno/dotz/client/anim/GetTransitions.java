package com.enno.dotz.client.anim;

import static com.enno.dotz.client.DropDirection.DOWN;
import static com.enno.dotz.client.DropDirection.LEFT;
import static com.enno.dotz.client.DropDirection.NOT_ALLOWED;
import static com.enno.dotz.client.DropDirection.RIGHT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.DropDirection;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.TeleportClipBox;

public class GetTransitions
{
    public static class DropTransitionList extends TransitionList
    {
        private Set<Sound> m_sounds = new HashSet<Sound>();
        
        public DropTransitionList(Layer layer, double duration)
        {
            super("drop", layer, duration);
        }
        
        public void addSound(Sound sound)
        {
            m_sounds.add(sound);
        }
        
        public void done()
        {
            for (Sound s : m_sounds)
                s.play();
        }
        
        public boolean containsItem(Item item)
        {
            for (IAnimationCallback cb : m_list)
            {
                if (cb instanceof Transition && ((Transition) cb).containsItem(item))
                    return true;
            }
            return false;
        }
    }
    
    private Context ctx;
    private Config cfg;
    private GridState state;
    
    private DropTransitionList m_list;
    
    private boolean m_roll = true;
    
    public GetTransitions(Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        
        m_roll = ctx.generator.rollMode;
    }
    
    public AnimList get(Layer layer, double dropDuration)
    {
        //Debug.p("GetTransitions");
        
        AnimList animList = new AnimList() {
            @Override
            public void run()
            {
                ctx.backgroundLayer.draw();
                ctx.iceLayer.draw();
                ctx.doorLayer.draw();
                
                super.run();
            }
        };
        
        while (true)
        {
            m_list = new DropTransitionList(layer, dropDuration);

            for (int col = 0; col < cfg.numColumns; col++)
            {
                //Debug.p("start col=" + col);
                
                Pt p = new Pt(col, cfg.numRows - 1);
                trail(p, DOWN, null);
            }
            
            if (m_roll)
            {
                checkRolls();
                
                for (int col = 0; col < cfg.numColumns; col++)
                {
                    //Debug.p("start col=" + col);
                    
                    Pt p = new Pt(col, cfg.numRows - 1);
                    trail(p, DOWN, null);
                }
            }

            if (m_list.getTransitions().size() > 0)
                animList.add(m_list);
            else
                break;
        }
        
        return animList;
    }
    
    private static int m_lastDx = 0;
    
    protected void checkRolls()
    {
        for (int row = cfg.numRows - 1; row >= 0; row--)
        {
            for (int col = 0; col < cfg.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.isLocked() || cell.item == null)
                    continue;
                
                if (m_list.containsItem(cell.item))
                    continue;
                
                if (isSolidBelow(col, row))  // bottom row or only holes/cages below
                {
                    int dx = m_lastDx == 0 ? 1 : -m_lastDx;                    
                    if (checkRoll(col, row, dx))
                    {
                        m_lastDx = dx;
                        continue;
                    }
                    else if (checkRoll(col, row, -dx))
                    {
                        m_lastDx = -dx;
                        continue;
                    }
                }
            }
        }
    }
    
    protected boolean didCell(Cell cell)
    {
        return m_roll && cell.item != null && m_list.containsItem(cell.item);
    }
    
    protected boolean didItem(Item item)
    {
        return m_roll && m_list.containsItem(item);
    }
    
    protected boolean checkRoll(int col, int row, int dx)
    {
        if (!state.isValidCell(col + dx, row + 1))
            return false; // can't roll there
        
        Cell under = state.cell(col, row + 1);
        if (under.canBeFilled())
            return false;
        
        Cell below = state.cell(col + dx, row + 1);
        if (!below.canBeFilled())
            return false;
        
        addDropDown(state.cell(col, row), below);
        
        return true;
    }
    
    protected boolean isSolidBelow(int col, int row)
    {
        Cell c  = state.cell(col,  row);
        if (c.isTeleportSource())
        {
            Teleport src = (Teleport) c;
            Cell target = state.cell(src.getOtherCol(), src.getOtherRow());
            if (target.item == null)
                return false;
            else
                return isSolidBelow(src.getOtherCol(), src.getOtherRow());
        }
        
        if (row == state.numRows - 1)
            return true;
        
        Cell cell = state.cell(col, row + 1);
        if (cell.isLockedDoor())
            return true;
        
        if (cell instanceof Hole || cell.isLockedCage())
            return isSolidBelow(col, row + 1);
        
//        if (cell.isLocked())
//            return true;
        
        if (cell.item == null)
            return false;
        
        return isSolidBelow(col, row + 1);
    }
    
    protected void trail(Pt p, DropDirection dir, Pt prev)
    {
        //Debug.p("trail " + p);
        if (p.row == -1)
        {
            // Reached the top. 
            // Do we need to spawn a new Item?
            if (dir != NOT_ALLOWED && prev != null)
            {
                Cell target = state.cell(prev.col, prev.row);
                if (target.canBeFilled())
                    addSpawn(p, prev);
            }
            return; // end of trail
        }
        
        Cell src = state.cell(p.col, p.row);
        if (!didCell(src) && src.canDrop() && dir != NOT_ALLOWED)
        {
            if (src.isTeleportSource())
            {
                Teleport tsrc = (Teleport) src;                                
                    
                Cell target = state.cell(tsrc.getOtherCol(), tsrc.getOtherRow());
                if (target.canBeFilled())
                    addTeleport(src, target);
            }
            else if (nearBottom(p.col, p.row))  // bottom row or only holes/cages below
            {
                // see if anchor can fall
                if (src.item.canDropFromBottom())
                    addDropOut(src);
            }
            else
            {
                Cell target = state.cell(prev.col, prev.row);
                if (target.canBeFilled())
                    addDropDown(src, target);
            }
        }
        
        if (nextToSlide(p))
        {
            List<Pt> slideSources = getSlideSources(p);
            DropDirection fillDir = NOT_ALLOWED;
            if (!didCell(src) && src.canBeFilled())
            {
                // who's gonna fill?
                List<Pt> fillers = getSlideFillers(slideSources);
                int n = fillers.size();
                if (n > 0)
                {
                    if (n == 1 || src.lastSlideDir == null)
                        fillDir = fillers.get(0).dir;
                    else
                        fillDir = getNextSlideDir(src.lastSlideDir.next(), fillers);

                    //Debug.p("n=" + n + " lastSlideDir=" + src.lastSlideDir + "fill=" + fillDir);
                    src.lastSlideDir = fillDir; // remember for next time
                }
            }
            for (Pt slideSrc : slideSources)
            {
                DropDirection d = fillDir == slideSrc.dir ? fillDir : NOT_ALLOWED;
                trail(slideSrc, d, p);
            }
        }
        else
        {
            // If it's a Teleport target, also check above it.
            if (p.row > 0 && state.cell(p.col, p.row).isTeleportTarget())
            {
                Cell aboveSrc = state.cell(p.col, p.row - 1);
                if (!(aboveSrc.isTeleportTarget() || aboveSrc instanceof Slide))
                {
                    trail(new Pt(p.col, p.row - 2), DOWN, new Pt(p.col, p.row - 1));
                }
            }
            
            // only possible source is up
            Pt above = getSourceAbove(p);
            if (above == null)
                return;
            
            trail(above, DOWN, p);
        }
    }

    protected Pt getSourceAbove(Pt p)
    {
        Cell target = state.cell(p.col, p.row);
        if (target.isTeleportTarget())
        {
            // return the Teleport Source
            Teleport tt = (Teleport) target;
            Pt trans = new Pt(tt.getOtherCol(), tt.getOtherRow());
            trans.dir = DOWN;
            return trans;
        }
        
        if (p.row == 0)
        {
            // top row
            Pt top = new Pt(p.col, -1);
            top.dir = DOWN;
            return top;
        }
        else
        {
            Cell c = state.cell(p.col, p.row - 1);
            if (endOfLine(c))
                return null;
            
            if (c instanceof Hole || c.isLockedCage())
            {
                return getSourceAbove(new Pt(p.col, p.row - 1));
            }
            else
            {
                Pt above = new Pt(p.col, p.row - 1);
                above.dir = DOWN;
                return above;
            }
        }            
    }
    
    protected void addDropDown(Cell src, final Cell target)
    {
        if (target instanceof ChangeColorCell && src.item.canChangeColor())
        {
            final Item oldItem = src.item;
            final Item newItem = ctx.generator.changeColor(ctx, src.item);
            m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), src.item) {
                @Override
                public void afterEnd()
                {
                    oldItem.removeShapeFromLayer(ctx.dotLayer);
                    
                    newItem.addShapeToLayer(ctx.dotLayer);
                    newItem.moveShape(state.x(target.col), state.y(target.row));
                }
            });
            target.item = newItem; 
            src.item = null;
        }
        else
        {
            m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), src.item));
            target.item = src.item; 
            src.item = null;
        }
    }
    
    protected void addSpawn(final Pt src, Pt target)
    {
        Item item = ctx.generator.getNextItem(ctx, false);
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), item) {
            public void afterStart()
            {
                //Debug.p("spawn " + src);
                item.addShapeToLayer(ctx.dotLayer);
            }
        });
        state.cell(target.col, target.row).item = item;
    }
    
    protected void addDropOut(Cell src)
    {
        if (src.item instanceof Anchor)
            m_list.addSound(Sound.DROPPED_ANCHOR);
        else if (src.item instanceof Clock)
            m_list.addSound(Sound.DROPPED_CLOCK);
        
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(src.col), state.y(src.row + 1), src.item) {
            public void afterEnd()
            {
                item.removeShapeFromLayer(ctx.dotLayer);
                item.dropFromBottom();
            }
        });
        src.item = null;
    }
    
    protected void addTeleport(Cell src, Cell target)
    {
        //Debug.p("teleport from " + src + " to " + target);
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(src.col), state.y(src.row + 1), src.item));
        
        final Item clone = src.item.copy();
        clone.init(ctx);
        final Item origItem = src.item;
        
        final TeleportClipBox fromBox = new TeleportClipBox(origItem.shape, src, ctx);
        final TeleportClipBox toBox = new TeleportClipBox(clone.shape, target, ctx);
        
        m_list.add(new DropTransition(state.x(target.col), state.y(target.row - 1), state.x(target.col), state.y(target.row), clone) {
            public void afterStart()
            {
                clone.addShapeToLayer(ctx.dotLayer);
                fromBox.init();
                toBox.init();
            }
            
            public void afterEnd()
            {
                fromBox.done();
                toBox.done();
                origItem.removeShapeFromLayer(ctx.dotLayer);
            }
        });
        
        target.item = clone; 
        src.item = null;
    }
    
    protected DropDirection getNextSlideDir(DropDirection dir, List<Pt> fillers)
    {
        // return dir if it's in the list
        for (Pt p : fillers)
        {
            if (p.dir == dir)
                return dir;
        }
        // else try the next one
        return getNextSlideDir(dir.next(), fillers);
    }
    
    protected List<Pt> getSlideSources(Pt p)
    {
        //TODO could cache these in the cell
        List<Pt> list = new ArrayList<Pt>();
        
        if (p.col > 0)
        {
            Cell c = state.cell(p.col - 1, p.row);
            if (c instanceof Slide && !((Slide) c).isToLeft())
            {
                // slide to the right
                Pt slide = new Pt(p.col - 1, p.row - 1);
                slide.dir = RIGHT;
                list.add(slide);
            }
        }
        if (p.col < cfg.numColumns - 1)
        {
            Cell c = state.cell(p.col + 1, p.row);
            if (c instanceof Slide && ((Slide) c).isToLeft())
            {
                // slide to the left
                Pt slide = new Pt(p.col + 1, p.row - 1);
                slide.dir = LEFT;
                list.add(slide);
            }
        }
        
        // Look above
        Pt above = getSourceAbove(p);
        if (above != null)
            list.add(above);            
        
        return list;
    }
    
    protected List<Pt> getSlideFillers(List<Pt> sources)
    {
        List<Pt> list = new ArrayList<Pt>();
        for (Pt p : sources)
        {
            if (p.row == -1)
                list.add(p);
            else
            {
                Cell c = state.cell(p.col, p.row);
                if (c.canDrop())
                    list.add(p);
            }
        }
        return list;
    }
    
    protected boolean endOfLine(Cell c)
    {
        return c instanceof Slide || c.isTeleportSource();
    }
    
    protected boolean nextToSlide(Pt p)
    {
        if (p.col > 0)
        {
            Cell c = state.cell(p.col - 1, p.row);
            if (c instanceof Slide && !((Slide) c).isToLeft())
                return true; // slide to the right                
        }
        if (p.col < cfg.numColumns - 1)
        {
            Cell c = state.cell(p.col + 1, p.row);
            if (c instanceof Slide && ((Slide) c).isToLeft())
                return true; // slide to the left 
        }
        return false;
    }
    
    protected boolean nearBottom(int col, int row)
    {
        while (row < cfg.numRows)
        {
            if (row == cfg.numRows - 1)
                return true; // bottom row
            
            Cell c = state.cell(col, row + 1);
            if (! (c instanceof Hole || c.isLockedCage()))
                return false;
            
            row++;
        }
        return false; // never gets here
    }

    protected Cell getTargetCell(int col, int row)
    {
        if (row >= cfg.numRows)
            return null;
        
        Cell c = state.cell(col, row + 1);
        if (c instanceof Hole || c.isLockedCage())
            return getTargetCell(col, row + 1);
        else if (c.isTeleportTarget()) 
            return null;
        
        return c;
    }
}