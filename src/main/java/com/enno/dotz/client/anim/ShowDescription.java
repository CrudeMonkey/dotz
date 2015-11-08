package com.enno.dotz.client.anim;

import com.enno.dotz.client.Context;
import com.enno.dotz.client.box.LienzoPopup;
import com.enno.dotz.client.util.CallbackChain.Callback;

public class ShowDescription extends Callback
{
    private String m_description;
    private Context ctx;

    public ShowDescription(String description, Context ctx)
    {
        this.ctx = ctx;
        m_description = description;
    }
    
    @Override
    public void run()
    {
        String[] lines = m_description.split("\\<br\\>");
        ctx.gridPanel.pause();
        LienzoPopup.showDescription(lines, ctx.gridPanel, new Runnable() {
            @Override
            public void run()
            {
                ctx.gridPanel.unpause();
                doNext();
            }
        });
    }
}