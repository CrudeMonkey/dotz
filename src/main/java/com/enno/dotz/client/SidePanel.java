package com.enno.dotz.client;

import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;

public class SidePanel extends LienzoPanel
{
    public SidePanel(Context ctx)
    {
        super(50, ctx.cfg.numRows * ctx.cfg.size);
        setBackgroundColor(ColorName.ALICEBLUE);
        draw();
    }
}
