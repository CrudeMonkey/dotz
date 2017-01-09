package com.enno.dotz.client.anim;

import com.ait.lienzo.client.core.animation.AbstractAnimation;
import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Context.FindWordList;
import com.google.gwt.dom.client.Style.FontWeight;

public abstract class ShowWordCallback implements IAnimationCallback 
{
    private Text m_text;
    private Layer m_layer;
    private FindWordList m_findWordList;
    
    public ShowWordCallback(Context ctx, String word, int wordPoints, int color)
    {
        m_findWordList = ctx.findWordList;
        
        double width = ctx.cfg.numColumns * ctx.cfg.size;
        m_layer = ctx.nukeLayer;
    
        m_text = new Text(word + " (" + wordPoints + ")");
        //m_text.setFillColor(ColorName.BLACK);
        m_text.setFillColor(color == Config.WILD_ID ? ColorName.BLACK : Config.COLORS[color]);
        m_text.setFontSize(30);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE); // y position is position of top of the text
        m_text.setY(ctx.cfg.size / 2);
        m_text.setX(width / 2);
        m_text.setAlpha(0);
        
        AbstractAnimation anim = new TimedAnimation(1000, this);
        anim.run();

        // doing this in parallel with the rest
        done();
    }

    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        if (m_findWordList != null)
            m_findWordList.setVisible(false);
        
        m_layer.add(m_text);
        m_layer.setVisible(true);
        redraw();
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {            
        double pct = animation.getPercent();
        
        if (pct < 0.25)
        {
            pct = pct / 0.25;
            m_text.setAlpha(pct);
            redraw();
        }
        else if (pct > 0.75)
        {
            pct = 1 - (pct - 0.75) / 0.25;
            m_text.setAlpha(pct);
            redraw();
        }
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        m_layer.setVisible(false);
        m_layer.remove(m_text);
        
        if (m_findWordList != null)
            m_findWordList.setVisible(true);
    }

    public void redraw()
    {
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    protected abstract void done();
}