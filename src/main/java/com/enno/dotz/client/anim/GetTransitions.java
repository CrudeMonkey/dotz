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
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.DropDirection;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Pt.PtList;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.TeleportClipBox;
import com.enno.dotz.client.util.CallbackChain.Callback;

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
        
        @Override
        public void done()
        {
            for (Sound s : m_sounds)
                s.play();
        }
    }
    
    private Context ctx;
    private Config cfg;
    private GridState state;
    
    private DropTransitionList m_list;
    
    private boolean m_roll;
    private boolean m_slipperyAnchors;
    
    public GetTransitions(Context ctx)
    {
        this.ctx = ctx;
        this.cfg = ctx.cfg;
        this.state = ctx.state;
        
        m_roll = ctx.generator.rollMode;
        m_slipperyAnchors = ctx.generator.slipperyAnchors;
    }
    
    public Callback getCallback(final Layer layer, final double dropDuration)
    {
        return new Callback() {
            @Override
            public void run()
            {
                AnimList animList = get(layer, dropDuration);
                
                animList.setDoneCallback(new Runnable() {
                    @Override
                    public void run()
                    {
                        doNext();
                    }
                });
                animList.run();
            }
        };
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

            PtList ptList = new PtList();
            boolean found = false;
            
            if (m_slipperyAnchors)
            {
                found = checkDrops(true, ptList);   // check slippery anchors
                if (!found && m_roll)
                    found = checkRolls(true, ptList);
            }
            
            if (!found)
            {
                found = checkDrops(false, ptList);  // check regular drops
            
                if (!found && m_roll)
                    checkRolls(false, ptList);
            }
            
            if (m_list.getTransitions().size() > 0)
                animList.add(m_list);
            else
                break;
        }
        return animList;
    }
    
    private boolean checkRolls(boolean anchorsOnly, PtList droppedInto)
    {
        int before = droppedInto.size();
        
        boolean found = true;
        while (found)
        {
            found = false;
            
            for (int row = cfg.numRows - 1; row > 0; row--)
            {
                for (int col = 0; col < cfg.numColumns; col++)
                {
                    Pt p = new Pt(col, row);
                    if (droppedInto.contains(p))
                        continue;
                    
                    Cell c = state.cell(col, row);
                    if (!c.canBeFilled())
                        continue;
                    
                    Cell above = state.cell(col, row - 1);
                    if (above.item != null)
                        continue;
                    
                    boolean canRollFromLeft = false;
                    boolean canRollFromRight = false;
                    if (col > 0)
                    {
                        Cell left = state.cell(col - 1, row - 1);
                        if (left.canDrop() && checkAnchor(left, anchorsOnly) && !droppedInto.contains(col - 1, row - 1) && isSolidBelow(col - 1, row - 1))
                            canRollFromLeft = true;
                    }
                    if (col < state.numColumns - 1)
                    {
                        Cell right = state.cell(col + 1, row - 1);
                        if (right.canDrop() && checkAnchor(right, anchorsOnly) && !droppedInto.contains(col + 1, row - 1) && isSolidBelow(col + 1, row - 1))
                            canRollFromRight = true;
                    }
                    
                    Cell src = null;
                    if (canRollFromLeft)
                    {
                        if (canRollFromRight)
                        {
                            if (c.lastSlideDir == DropDirection.LEFT)
                            {
                                src = state.cell(col + 1, row - 1); // right
                                c.lastSlideDir = DropDirection.RIGHT;
                            }
                            else
                            {
                                src = state.cell(col - 1, row - 1); // left
                                c.lastSlideDir = DropDirection.LEFT;
                            }
                        }
                        else
                        {
                            src = state.cell(col - 1, row - 1); // left
                            c.lastSlideDir = DropDirection.LEFT;
                        }
                    }
                    else if (canRollFromRight)
                    {
                        src = state.cell(col + 1, row - 1); // right
                        c.lastSlideDir = DropDirection.RIGHT;
                    }
                    if (src != null)
                    {
                        addRollDown(src, c);
                        found = true;
                        droppedInto.add(p);
                    }
                }
            }
        }
        int added = droppedInto.size() - before;
        //Debug.p("total: " + added);
        return added > 0;
    }
    
    private boolean checkDrops(boolean anchorsOnly, PtList droppedInto)
    {
        int before = droppedInto.size();
        
        boolean found = true;
        while (found)
        {
            found = false;
            
            for (int row = cfg.numRows; row >= 0; row--)
            {
                for (int col = 0; col < cfg.numColumns; col++)
                {
                    Pt p = new Pt(col, row);
                    if (droppedInto.contains(p))
                        continue;
                    
                    if (row == cfg.numRows)     // near bottom
                    {
                        Pt above = getSourceAbove(p);
                        if (above != null)
                        {
                            if (above.row == -1)
                            {
                                // Entire column of holes or locked cages - nothing to drop
                            }
                            else 
                            {
                                Cell aboveCell = state.cell(above.col, above.row);
                                if (aboveCell.canDrop() && aboveCell.item.canDropFromBottom() && checkAnchor(aboveCell, anchorsOnly))
                                {
                                    // Drop out of bottom
                                    addDropOut(aboveCell);
                                    found = true;
                                    droppedInto.add(p);
                                }
                            }
                        }
                    }
                    else
                    {
                        Cell c = state.cell(col, row);
                        if (!c.canBeFilled())
                            continue;
                        
                        if (nextToSlide(p))
                        {
                            List<Pt> slideSources = getSlideSources(p);
                            DropDirection fillDir = NOT_ALLOWED;
                            
                            // who's gonna fill?
                            List<Pt> fillers = getSlideFillers(slideSources, anchorsOnly, droppedInto);
                            int n = fillers.size();
                            if (n > 0)
                            {
                                if (n == 1 || c.lastSlideDir == null)
                                    fillDir = fillers.get(0).dir;
                                else
                                    fillDir = getNextSlideDir(c.lastSlideDir.next(), fillers);

                                //Debug.p("n=" + n + " lastSlideDir=" + src.lastSlideDir + "fill=" + fillDir);
                                c.lastSlideDir = fillDir; // remember for next time
                                
                                if (fillDir != DropDirection.DOWN)
                                {
                                    int prevCol = fillDir == DropDirection.RIGHT ? col - 1 : col + 1;
                                    Cell aboveCell = state.cell(prevCol, row - 1);
                                    
                                    // Add slide transition                                    
                                    addDropDown(aboveCell, c);
                                    found = true;
                                    droppedInto.add(p);
                                    continue;
                                }
                                // else fall through and use source above - which could be a Transport target or Spawn etc.
                            }
                        }
                        
                        Pt above = getSourceAbove(p);
                        if (above != null)
                        {
                            if (above.row == -1)
                            {
                                if (!anchorsOnly)
                                {
                                    // Spawn new item at the top
                                    addSpawn(above, p);
                                    found = true;
                                    droppedInto.add(p);
                                }
                            }
                            else if (!droppedInto.contains(above))
                            {
                                Cell aboveCell = state.cell(above.col, above.row);
                                if (aboveCell.canDrop() && checkAnchor(aboveCell, anchorsOnly))
                                {
                                    // Add transition
                                    if (aboveCell.isTeleportSource())
                                        addTeleport(aboveCell, c);
                                    else
                                        addDropDown(aboveCell, c);
                                    
                                    found = true;
                                    droppedInto.add(p);
                                }
                            }
                        }
                    }
                }
            }
            //Debug.p("found " + droppedInto);
        }
        int added = droppedInto.size() - before;
        //Debug.p("total: " + added);
        return added > 0;
    }
    
    private static boolean checkAnchor(Cell c, boolean anchorsOnly)
    {
        return !anchorsOnly || c.item instanceof Anchor;
    }
    
    private boolean isSolidBelow(int col, int row)
    {
        Cell c  = state.cell(col, row);
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
        
        if (cell.item == null)
            return false;
        
        return isSolidBelow(col, row + 1);
    }

    private Pt getSourceAbove(Pt p)
    {
        if (p.row < state.numRows)
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

    private void addRollDown(Cell src, final Cell target)
    {
        addDropDown(src, target); // TODO animate roll
    }
    
    private void addDropDown(Cell src, final Cell target)
    {
        if (target instanceof ChangeColorCell && src.item.canChangeColor())
        {
            Item oldItem = src.item;
            Item newItem = ctx.generator.changeColor(ctx, src.item);
            
            addTeleport(oldItem, newItem, src.pt(), target.pt(), true, false);
        }
        else
        {
            if (src.row != target.row - 1)
            {
                addTeleport(src.item, null, src.pt(), target.pt(), true, false);
            }
            else
            {
                m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), src.item));
                target.item = src.item; 
                src.item = null;
            }
        }
    }
    
    private void addTeleport(Cell src, Cell target)
    {
        //Debug.p("teleport from " + src + " to " + target);
        
        addTeleport(src.item, null, src.pt(), target.pt(), true, false);
    }

    private void addSpawn(final Pt src, Pt target)
    {
        Item item = ctx.generator.getNextItem(ctx, false);
        
        if (target.row > 0)
        {
            // It's skipping some cells (e.g. Holes or locked cages)
            addTeleport(item, null, src, target, false, true);
            return;
        }
        
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), item) {
            @Override
            public void afterStart()
            {
                //Debug.p("spawn " + src);
                item.addShapeToLayer(ctx.dotLayer);
            }
        });
        state.cell(target.col, target.row).item = item;
    }
    
    /**
     * 
     * @param oldItem
     * @param newItem_      If null, oldItem will be cloned.
     * @param src
     * @param target
     * @param removeFromSrc If true, the oldItem must be removed from the src cell.
     * @param isBrandNew    If true, the oldItem's shape must be added to the layer first (e.g. when spawning a new item.)
     */
    private void addTeleport(final Item oldItem, final Item newItem_, Pt src, Pt target, boolean removeFromSrc, final boolean isBrandNew)
    {
        final Item newItem;
        if (newItem_ == null)
        {
            newItem = oldItem.copy();
            newItem.init(ctx);
        }
        else
        {
            newItem = newItem_;
        }
        
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(src.col), state.y(src.row + 1), oldItem));

        final TeleportClipBox fromBox = new TeleportClipBox(oldItem.shape, src, ctx);
        final TeleportClipBox toBox = new TeleportClipBox(newItem.shape, target, ctx);
        
        m_list.add(new DropTransition(state.x(target.col), state.y(target.row - 1), state.x(target.col), state.y(target.row), newItem) {
            @Override
            public void afterStart()
            {
                if (isBrandNew) // e.g. when spawning a new item
                    oldItem.addShapeToLayer(ctx.dotLayer);
                
                newItem.addShapeToLayer(ctx.dotLayer);
                fromBox.init();
                toBox.init();
            }
            
            @Override
            public void afterEnd()
            {
                fromBox.done();
                toBox.done();
                oldItem.removeShapeFromLayer(ctx.dotLayer);
            }
        });
        
        state.cell(target.col, target.row).item = newItem; 
        if (removeFromSrc)
            state.cell(src.col, src.row).item = null;
    }

    private void addDropOut(Cell src)
    {
        if (src.item instanceof Anchor)
            m_list.addSound(Sound.DROPPED_ANCHOR);
        else if (src.item instanceof Clock)
            m_list.addSound(Sound.DROPPED_CLOCK);
        
        m_list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(src.col), state.y(src.row + 1), src.item) {
            @Override
            public void afterEnd()
            {
                item.removeShapeFromLayer(ctx.dotLayer);
                item.dropFromBottom();
            }
        });
        src.item = null;
    }
    
    private DropDirection getNextSlideDir(DropDirection dir, List<Pt> fillers)
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
    
    private List<Pt> getSlideSources(Pt p)
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
    
    private List<Pt> getSlideFillers(List<Pt> sources, boolean anchorsOnly, PtList droppedInto)
    {
        List<Pt> list = new ArrayList<Pt>();
        for (Pt p : sources)
        {
            if (p.row == -1 && !anchorsOnly)
                list.add(p);
            else
            {
                Cell c = state.cell(p.col, p.row);
                if (c.canDrop() && checkAnchor(c, anchorsOnly) && !droppedInto.contains(p))
                    list.add(p);
            }
        }
        return list;
    }
    
    private static boolean endOfLine(Cell c)
    {
        return c instanceof Slide || c.isTeleportSource() || c instanceof Rock;
    }
    
    private boolean nextToSlide(Pt p)
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
}