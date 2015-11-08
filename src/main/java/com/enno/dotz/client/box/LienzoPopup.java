package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.event.NodeMouseClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseClickHandler;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.DotzGridPanel;
import com.enno.dotz.client.util.TransformGroup;
import com.smartgwt.client.util.BooleanCallback;

public class LienzoPopup extends Group
{
    public static void showDescription(String[] description, DotzGridPanel panel, Runnable whenDone)
    {
        say(null, panel, whenDone, description);
    }
    
    public static void say(String title, LienzoPanel panel, Runnable whenDone, String... msg)
    {
        new LienzoPopup(title, msg, panel, whenDone);
    }
    
    public static void ask(String title, final LienzoPanel panel, final BooleanCallback cb, String... msg)
    {
        new LienzoPopup(title, msg, panel, cb);
    }

    private Layer m_layer;
    private Rectangle m_background;
    private Extent m_extent;
    private TransformGroup m_transformGroup;
    
    public LienzoPopup(String title, String msg[], final LienzoPanel panel, final BooleanCallback cb)
    {
        BooleanCallbackWrapper cbWrapper = new BooleanCallbackWrapper();
        BoxDialog inner = BoxDialog.ask(title, cbWrapper, msg);
        
        prepare(panel, inner);
        
        BoxTransition transIn = new BoxTransition.SlideIn();
        final BoxTransition transOut = new BoxTransition.SlideOut();
        
        cbWrapper.setCallback(new BooleanCallback() {
            @Override
            public void execute(final Boolean value)
            {
                transOut.transition(m_transformGroup, m_extent, panel, m_layer, new Runnable() 
                {
                    @Override
                    public void run()
                    {
                        panel.remove(m_layer);
                        cb.execute(value);
                    }
                });
            }
        });
        
        transIn.transition(m_transformGroup, m_extent, panel, m_layer, new Runnable() 
        {
            @Override
            public void run()
            {                
            }
        });   
    }
    
    public LienzoPopup(String title, String msg[], final LienzoPanel panel, final Runnable whenDone)
    {
        BoxDialog inner = BoxDialog.say(title, msg);
        
        prepare(panel, inner);
        m_background.setListening(true);
        
        BoxTransition transIn = new BoxTransition.SlideIn();
        final BoxTransition transOut = new BoxTransition.SlideOut();
        
        transIn.transition(m_transformGroup, m_extent, panel, m_layer, new Runnable() 
        {
            @Override
            public void run()
            {
                m_layer.addNodeMouseClickHandler(new NodeMouseClickHandler()
                {
                    @Override
                    public void onNodeMouseClick(NodeMouseClickEvent event)
                    {
                        transOut.transition(m_transformGroup, m_extent, panel, m_layer, new Runnable() 
                        {
                            @Override
                            public void run()
                            {
                                panel.remove(m_layer);
                                whenDone.run();
                            }
                        });
                    }
                });
            }
        });   
    }

    private void prepare(LienzoPanel panel, BoxDialog boxDialog)
    {
        m_layer = new Layer();
        panel.add(m_layer);
        
        m_background = new Rectangle(panel.getWidth(), panel.getHeight());
        m_background.setFillColor(ColorName.DARKGRAY);
        m_background.setAlpha(0.7);
        m_layer.add(m_background);
        
        LayoutContext c = new LayoutContext();
        c.layer = m_layer;
        
        m_extent = boxDialog.getPreferred(c);
        m_extent.x = 0d;
        m_extent.y = 0d;
        boxDialog.setRect(m_extent, c);
        
        m_extent.x = (panel.getWidth() - m_extent.w) / 2;
        m_extent.y = (panel.getHeight() - m_extent.h) / 2;
        
        m_transformGroup = new TransformGroup(boxDialog.getShape());
        add(m_transformGroup);
        m_layer.add(this);
    }
    
    public static class BooleanCallbackWrapper implements BooleanCallback
    {
        private BooleanCallback m_cb;
        
        public void setCallback(BooleanCallback cb)
        {
            m_cb = cb;
        }
        
        @Override
        public void execute(Boolean value)
        {
            m_cb.execute(value);
        }        
    }
    
}
