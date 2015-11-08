package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Context;

/**
 * TeleportClipBox clips the shape that goes into (or comes out of) a Teleport cell.
 * 
 * @author Enno
 */
public class TeleportClipBox extends Group
{
    private Context ctx;
    private IPrimitive<?> m_shape;

    public TeleportClipBox(IPrimitive<?> shape, Cell cell, Context ctx)
    {
        this.ctx = ctx;
        m_shape = shape;
        
        double x = ctx.state.x(cell.col);
        double y = ctx.state.y(cell.row);
        double sz2 = ctx.cfg.size / 2;
                
        setPathClipper(new BoundingBox(x - sz2, y - sz2, x + sz2, y + sz2));
    }
    
    public void init()
    {
        ctx.dotLayer.remove(m_shape);
        add(m_shape);
        ctx.dotLayer.add(this);
    }

    public void done()
    {
        ctx.dotLayer.remove(this);
        ctx.dotLayer.add(m_shape);
    }
}
