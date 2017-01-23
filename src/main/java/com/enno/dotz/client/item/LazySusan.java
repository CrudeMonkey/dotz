package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Transition.RotateTransition;
import com.enno.dotz.client.anim.TransitionList;

public class LazySusan
{
    private int m_col, m_row;
    private boolean m_clockwise;
    
    private Group m_shape;
    private Context ctx;
    private int m_animRotation; // [0,3]
    
    public LazySusan(int col, int row, boolean clockwise)
    {
        m_col = col;
        m_row = row;
        m_clockwise = clockwise;
    }
    
    public int getCol()
    {
        return m_col;
    }
    
    public int getRow()
    {
        return m_row;
    }
    
    public void setCol(int col)
    {
        m_col = col;
    }
    
    public void setRow(int row)
    {
        m_row = row;
    }
    
    public boolean isClockwise()
    {
        return m_clockwise;
    }

    public LazySusan copy()
    {
        return new LazySusan(m_col, m_row, m_clockwise);
    }
    
    public void initGraphics(Context ctx)
    {
        this.ctx = ctx;
        
        GridState state = ctx.state;
        double sz = ctx.cfg.size;
        
        m_shape = createShape(sz);
        m_shape.setX(state.x(m_col) + sz / 2); // center of 4 cells
        m_shape.setY(state.y(m_row) + sz / 2);        
        
        ctx.backgroundLayer.add(m_shape);
    }
    
    public void removeGraphics()
    {
        ctx.backgroundLayer.remove(m_shape);
    }
    
    public Group createShape(double sz)
    {
        Group group = new Group();
        
        IColor color = ColorName.DODGERBLUE;
        double r = sz / 4;
        Circle c = new Circle(r);
        c.setStrokeColor(color);
        c.setStrokeWidth(3);
        group.add(c);
        
        double d = r / 4;
        double h = r / 2;
        double f = m_clockwise ? 1 : -1;
        for (int i = 0; i < 4; i++)
        {
            Triangle t = new Triangle(new Point2D(-d * f, r), new Point2D(d * f, r + h), new Point2D(d * f, r - h));
            t.setFillColor(color);
            t.setRotation(Math.PI / 4 + i * Math.PI/2);
            group.add(t);
        }
        return group;
    }
    
    public void addTransitions(TransitionList list)
    {
        GridState state = ctx.state;
        
        // 0 1 when clockwise        0 3 when CCW
        // 3 2                       1 2
        final Cell[] cells = new Cell[4];
        cells[0] = state.cell(m_col, m_row);
        cells[m_clockwise ? 1 : 3] = state.cell(m_col + 1, m_row);
        cells[2] = state.cell(m_col + 1, m_row + 1);
        cells[m_clockwise ? 3 : 1] = state.cell(m_col, m_row + 1);               
        final Item[] items = { cells[0].item, cells[1].item, cells[2].item, cells[3].item };
        
        // Change color?
        final Item[] newItems = new Item[4];
        for (int i = 0; i < 4; i++)
        {
            int to = (i + 1) % 4;
            if (cells[to] instanceof ChangeColorCell && cells[i].item.canChangeColor())
            {
                newItems[to] = ctx.generator.changeColor(ctx, items[i]);
            }
        }
        
        boolean first = true;
        for (int i = 0; i < 4; i++)
        {
            if (items[i] == null)
                continue;
            
            Cell src = cells[i];
            int to = (i + 1) % 4;
            final Cell target = cells[to];
            
            final boolean moveItems = first;
            first = false;
            
            list.add(new RotateTransition(state.x(src.col), state.y(src.row), state.x(target.col), state.y(target.row), items[i]) {
                @Override
                public void afterEnd()
                {
                    // Move all items in the first transition
                    // We can't move them each in their own transition, because some source cells may be empty and won't have a transition,
                    // and it would leave extra shapes behind.
                    if (moveItems)
                    {
                        for (int j = 0; j < 4; j++)
                        {
                            int to = (j + 1) % 4;
                            
                            if (newItems[to] != null)
                            {
                                // Change color
                                items[j].removeShapeFromLayer(ctx.dotLayer);
                                
                                GridState state = ctx.state;
                                Cell newTarget = cells[to];
                                newItems[to].addShapeToLayer(ctx.dotLayer);
                                newItems[to].moveShape(state.x(newTarget.col), state.y(newTarget.row));
                                newTarget.item = newItems[to];
                            }
                            else
                            {
                                cells[to].item = items[j];
                            }
                        }
                    }
                }
            });
        }
        
        final double fromRot = m_animRotation * Math.PI / 2;
        final double angle = m_clockwise ? Math.PI / 2 : -Math.PI / 2;
        
        list.add(new IAnimationCallback()
        {            
            @Override
            public void onStart(IAnimation animation, IAnimationHandle handle)
            {
                rot(fromRot);
            }
            
            @Override
            public void onFrame(IAnimation animation, IAnimationHandle handle)
            {
                rot(fromRot + animation.getPercent() * angle);
            }
            
            @Override
            public void onClose(IAnimation animation, IAnimationHandle handle)
            {
                m_animRotation = (m_clockwise ? m_animRotation + 1 : m_animRotation + 3) % 4;                
                rot(m_animRotation * Math.PI / 2);
            }
            
            protected void rot(double angle)
            {
                m_shape.setRotation(angle);
//                LayerRedrawManager.get().schedule(ctx.backgroundLayer);
            }
        });
    }
}
