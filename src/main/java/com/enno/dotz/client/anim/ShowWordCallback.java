package com.enno.dotz.client.anim;

import com.ait.lienzo.client.core.animation.AbstractAnimation;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.WordDisplayPanel;

public abstract class ShowWordCallback implements IAnimationCallback 
{
    private Text m_text;
    private WordDisplayPanel m_display;
    
    public ShowWordCallback(Context ctx, String word, int wordPoints, int color)
    {
        m_display = ctx.wordDisplayPanel;
        
        m_text = m_display.createText(word + " (" + wordPoints + ")", 
                color == Config.WILD_ID ? ColorName.BLACK : Config.COLORS[color]);
        m_text.setAlpha(0);
        
        AbstractAnimation anim = new TimedAnimation(1000, this);
        anim.run();

        // doing this in parallel with the rest
        done();
    }

    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        m_display.addFoundWord(m_text);
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {            
        double pct = animation.getPercent();
        
        if (pct < 0.25)
        {
            pct = pct / 0.25;
            m_text.setAlpha(pct);
        }
        else if (pct > 0.75)
        {
            pct = 1 - (pct - 0.75) / 0.25;
            m_text.setAlpha(pct);
        }
        m_display.redraw();
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        m_display.removeFoundWord(m_text);
    }

    protected abstract void done();
}