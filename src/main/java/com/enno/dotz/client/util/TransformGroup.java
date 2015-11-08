package com.enno.dotz.client.util;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.types.Transform;

public class TransformGroup extends Group
{
    protected Transform m_transform;

    public TransformGroup()
    {        
    }
    
    public TransformGroup(IPrimitive<?> shape)
    {
        add(shape);
    }
    
    public void setTransform(Transform t)
    {
        m_transform = t;
    }
    
    @Override
    protected Transform getPossibleNodeTransform()
    {
        return m_transform;
    }
}
