package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.shared.core.types.TextAlign;

public interface Box
{
    Extent getPreferred(LayoutContext ctx);        
    void setRect(Extent r, LayoutContext ctx);
    TextAlign getAlign();
    HorAlign getHalign();
    
    IPrimitive<?> getShape();
}