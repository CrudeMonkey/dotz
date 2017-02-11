package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.anim.TransitionList;
import com.enno.dotz.client.anim.Transition.DropTransition;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.YinYang;
import com.enno.dotz.client.util.CollectionsUtils;

public class Reshuffle
{
    protected static final int MAX_RESHUFFLES = 50;
    
    protected Context ctx;
    protected GridState state;
    protected int numRows;
    protected int numColumns;
    
    protected ArrayList<Pt> m_initialState;
    protected ArrayList<Pt> m_toState;
    private Random m_rnd;

    public Reshuffle(Context ctx)
    {
        m_rnd = ctx.generator.getRandom();
        
        this.ctx = ctx;
        this.state = ctx.state;
        this.numRows = state.numRows;
        this.numColumns = state.numColumns;
    }
    
    public boolean mustReshuffle()
    {
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.isLocked())
                    continue;
                
                Item item = cell.item;
                if (item == null)
                    continue;
                
                if (isActionItem(item))
                    return false;
                
                if (col < numColumns - 1)
                {
                    Cell cell2 = state.cell(col + 1, row);
                    if (cell2.isLocked())
                        continue;
                    
                    Item item2 = cell2.item;
                    if (item2 != null)
                    {
                        if (GridState.canConnect(item, item2, 1, 0))
                        {
                            //Debug.p("can connect " + col + "," + row + " - " + (col+1) + "," + row);
                            return false;
                        }
                    }
                }
                
                if (row < numRows - 1)
                {
                    Cell cell2 = state.cell(col, row + 1);
                    if (cell2.isLocked())
                        continue;
                    
                    Item item2 = cell2.item;
                    if (item2 != null)
                    {
                        if (GridState.canConnect(item, item2, 0, 1))
                        {
                            //Debug.p("can connect " + col + "," + row + " - " + col + "," + (row+1));
                            return false;
                        }
                    }
                }
            }
        }
        
        if (Domino.hasAnimalChain(state)) // Animal - optional wild cards - double Domino
            return false;
        
        //TODO check knights
        
//            Debug.p("mustReshuffle=true");
        
        if (m_initialState == null)
            m_initialState = getReshuffleCells();
        
        return true;
    }
    
    public void reshuffle(final Runnable nextMoveCallback, Runnable reshuffleFailedCallback)
    {
        boolean success = doReshuffle(MAX_RESHUFFLES);
        if (success)
        {
            animateReshuffle(nextMoveCallback);
        }
        else
        {
            reshuffleFailedCallback.run();   
        }
    }
    
    public void forceReshuffle(Runnable nextCallback)
    {
        m_initialState = getReshuffleCells();
        doReshuffle(MAX_RESHUFFLES);
        animateReshuffle(nextCallback);
    }
    
    @SuppressWarnings("unchecked")
    protected boolean doReshuffle(int loop)
    {
        if (loop < 0)
            return false;
        
//            Debug.p("doReshuffle loop=" + loop);
        
        if (m_toState != null)
        {
            replace(m_toState, m_initialState); // undo last shuffle
        }
        
        m_toState = (ArrayList<Pt>) m_initialState.clone();
        CollectionsUtils.shuffle(m_toState, m_rnd);
        
        replace(m_initialState, m_toState);
        
        if (mustReshuffle())
            return doReshuffle(loop - 1); // detect infinite loop
        
        return true; // success
    }
    
    protected void replace(List<Pt> from, List<Pt> to)
    {
        for (int i = 0, n = from.size(); i < n; i++)
        {
            Pt a = from.get(i);
            Pt b = to.get(i);
            if (a != b)
            {
                Item ia = state.cell(a.col, a.row).item;
                Item ib = state.cell(b.col, b.row).item;
                state.cell(a.col, a.row).item = ib;
                state.cell(b.col, b.row).item = ia;
            }
        }
    }
    
    protected boolean isActionItem(Item item)
    {
        return item instanceof Rocket || item instanceof ColorBomb || item instanceof YinYang || item instanceof Blaster;
    }
    
    protected ArrayList<Pt> getReshuffleCells()
    {
        ArrayList<Pt> pts = new ArrayList<Pt>();
        for (int row = 0; row < numRows; row++)
        {
            for (int col = 0; col < numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.isLocked())            // Don't reshuffle items stuck in Doors
                    continue;
                
                Item item = cell.item;
                if (item != null && item.canReshuffle())
                    pts.add(new Pt(col, row));
            }
        }
        return pts;
    }
    
    protected void animateReshuffle(final Runnable nextCallback)
    {
        Sound.RESHUFFLE.play();
        
        //TODO could move items to nukeLayer
        TransitionList list = new TransitionList("reshuffle", ctx.dotLayer, ctx.cfg.reshuffleDuration) {                
            @Override
            public void doNext()
            {
                nextCallback.run();
            }
        };
        for (int i = 0, n = m_initialState.size(); i < n; i++)
        {
            Pt fromPt = m_initialState.get(i);
            Pt toPt = m_toState.get(i);
            Cell src = state.cell(fromPt.col, fromPt.row);
            Cell target = state.cell(toPt.col, toPt.row);
            
            // NOTE: item was already moved to target!
            list.add(new DropTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), target.item));
        }
        
        list.run();
    }
}