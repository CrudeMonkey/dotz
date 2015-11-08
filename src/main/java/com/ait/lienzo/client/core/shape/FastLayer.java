package com.ait.lienzo.client.core.shape;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Node;

/**
 * When a large number of shapes are added, the Layer's color map runs out of color keys.
 * But since we don't need to pick stuff on most layers, we avoid child.attachToLayerColorMap()
 * and we setListening(false).
 * 
 * @author Enno
 *
 */
public class FastLayer extends Layer
{
    public FastLayer()
    {
        setListening(false); // don't listen to events (for performance)
    }
    
    @Override
    public Layer add(IPrimitive<?> child)
    {
        // don't do: child.attachToLayerColorMap();
        return super_add(child);
    }
    
    private Layer super_add(IPrimitive<?> child)
    {
        final Node<?> node = child.asNode();

        node.setParent(this);

        getStorageEngine().add(child);

        return cast();
    }
}
