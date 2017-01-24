package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseEnterEvent;
import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.event.NodeMouseExitEvent;
import com.ait.lienzo.client.core.event.NodeMouseExitHandler;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class BoxButton extends BoxVLayout
{
    public BoxButton(String text)
    {
        final BoxDefaults b = BoxDefaults.INSTANCE;
        
        final Rectangle bg = new Rectangle(0,0);
        bg.setStrokeColor(b.borderColor);
        bg.setStrokeWidth(1);
        bg.setCornerRadius(b.cornerRadius);
        bg.setFillColor(b.buttonColor);
        bg.setListening(true);
        setBackground(bg);
        
        Text t = new Text(text);
        t.setFillColor(b.textColor);
        t.setFontSize(b.fontSize);
        t.setFontStyle("bold");
        t.setTextAlign(TextAlign.CENTER);
        t.setTextBaseLine(TextBaseLine.MIDDLE);
        
        TextBox box = new TextBox(t);
        box.setWidth(Extent.FULL);
        addMember(box);
        
        setAlign(TextAlign.CENTER);
        setPadding(5);
        setListening(true);
        
        bg.addNodeMouseEnterHandler(new NodeMouseEnterHandler()
        {            
            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event)
            {
                bg.setFillColor(b.buttonActiveColor);
                redrawLayer();
            }
        });
        
        bg.addNodeMouseExitHandler(new NodeMouseExitHandler()
        {            
            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event)
            {
                bg.setFillColor(b.buttonColor);
                redrawLayer();
            }
        });
    }
    
    protected void redrawLayer()
    {
        LayerRedrawManager.get().schedule(getLayer());
    }
}
