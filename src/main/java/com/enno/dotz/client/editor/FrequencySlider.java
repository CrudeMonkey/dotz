package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Slider;
import com.smartgwt.client.widgets.events.ValueChangedEvent;
import com.smartgwt.client.widgets.events.ValueChangedHandler;
import com.smartgwt.client.widgets.form.fields.CanvasItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class FrequencySlider extends HLayout
{
    public static int ICON_SIZE = 40;
    
    private Item m_item;
    private FrequencySliderGroup m_parent;
    private int m_index;
    
    private MXCheckBox m_check;
    private Slider m_slider;
    private boolean m_localChange;
    
    public FrequencySlider(int index, Item item, FrequencySliderGroup parent)
    {
        m_item = item;
        IPrimitive<?> shape = item.createShape(ICON_SIZE);
        
        m_parent = parent;
        m_index = index;
        
        setHeight(50);
        
        MXForm form = new MXForm();
        form.setNumCols(4);
        form.setColWidths(15, 0, ICON_SIZE, 320);
        form.setHeight(50);
        m_check = new MXCheckBox();
        m_check.setTitle(null);
        m_check.setShowLabel(false);
        m_check.setVAlign(VerticalAlignment.CENTER);
        m_check.addChangedHandler(new ChangedHandler()
        {            
            @Override
            public void onChanged(ChangedEvent event)
            {
                if (!m_localChange)
                {
                    m_slider.setDisabled(!m_check.isChecked());
                    m_parent.setSelected(m_index, m_check.isChecked());
                }
            }
        });
        
        CanvasItem ci = new CanvasItem();
        ci.setTitle(null);
        ci.setCanvas(createShapeCanvas(shape));
        ci.setShowTitle(false);
        ci.setVAlign(VerticalAlignment.CENTER);
        ci.setHeight(50);
        
        m_slider = new Slider();
        m_slider.setTitle(null);
        m_slider.setShowTitle(false);
        m_slider.setVertical(false);
        m_slider.setValue(0.0);
        m_slider.setMinValue(0.0);
        m_slider.setMaxValue(100.0);
        m_slider.setNumValues(1000);
        m_slider.setRoundPrecision(1);
        m_slider.setRoundValues(false);
        m_slider.setDisabled(true);
        m_slider.setHeight(50);
        
        CanvasItem ci2 = new CanvasItem();
        ci2.setTitle(null);
        ci2.setHeight(50);
        ci2.setCanvas(m_slider);
        ci2.setShowTitle(false);
        ci2.setVAlign(VerticalAlignment.CENTER);
        
        form.setFields(m_check, ci, ci2);
        addMember(form);
                
        m_slider.setDisabled(true); // NOTE: not sure why it doesn't work above...
        
        m_slider.addValueChangedHandler(new ValueChangedHandler()
        {            
            @Override
            public void onValueChanged(ValueChangedEvent event)
            {
                if (!m_localChange)
                    m_parent.setSliderValue(m_index, m_slider.getValueAsDouble());
            }
        });
    }
    
    public Item getItem()
    {
        return m_item;
    }
    
    public void setSelected(boolean selected)
    {
        m_check.setValue(selected);
        m_slider.setDisabled(!selected);
    }
    
    public boolean isSelected()
    {
        return m_check.isChecked();
    }
    
    public double getFrequency()
    {
        return m_slider.getValueAsDouble();
    }
    
    public int getIndex()
    {
        return m_index;
    }

    public void initFrequency(double freq)
    {
        setFrequency(freq);
        
        m_localChange = true;
        m_check.setValue(true);
        m_slider.setDisabled(false);
        m_localChange = false;
    }
    
    public void setFrequency(double freq)
    {
        m_localChange = true;
        m_slider.setValue(freq);
        m_localChange = false;
    }
    
    private Canvas createShapeCanvas(IPrimitive<?> shape)
    {
        int sz = ICON_SIZE;
        LienzoPanel panel = new LienzoPanel(sz, sz);
        Layer layer = new Layer();
        panel.add(layer);
        shape.setX(sz/2);
        shape.setY(sz/2);
        layer.add(shape);
        
        Canvas canvas = new Canvas();
        canvas.setWidth(sz);
        canvas.setHeight(sz);
        canvas.addChild(panel);
        return canvas;
    }
}
