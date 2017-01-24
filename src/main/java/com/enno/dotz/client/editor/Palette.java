package com.enno.dotz.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;

public abstract class Palette<T> extends LienzoPanel
{
    protected Context ctx;
    protected List<PaletteButton<T>> m_list = new ArrayList<PaletteButton<T>>();
    private int m_buttonSize;
    protected boolean m_active = true;
    
    protected Palette(int size, int buttonSize, int numColumns, int numRows)
    {
        super(buttonSize * numColumns, buttonSize * numRows);
        
        m_buttonSize = buttonSize;
        
        Config cfg = new Config();
        cfg.size = size;
        
        ctx = new Context(true, cfg);

        ctx.backgroundLayer = new FastLayer();
        add(ctx.backgroundLayer);    
        
        addMouseDownHandler(new MouseDownHandler()
        {            
            @Override
            public void onMouseDown(MouseDownEvent event)
            {
                if (!m_active)
                    return;
                
                int col = event.getX() / m_buttonSize;
                int row = event.getY() / m_buttonSize;
                
                selectOption(col, row);
            }
        });            
    }
    
    protected void selectOption(int col, int row)
    {
        PaletteButton<T> selected = null;
        for (PaletteButton<T> b : m_list)
        {
            if (b.selected(col, row))
                selected = b;
        }
        ctx.backgroundLayer.redraw();   // redraw button borders
        
        selected(selected);
    }

    public void selectFirstOption()
    {
        selectOption(0, 0);
    }
    
    protected void selected(PaletteButton<T> selected)
    {
        if (selected == null)
            selected((T) null);
        else
            selected(selected.getItem());
    }
    
    protected void selected(T selected)
    { 
    }

    public void deselectAll()
    {
        for (PaletteButton<T> b : m_list)
        {
            b.setSelected(false);
        }
        draw();
    }

    public abstract static class PaletteButton<T> extends Group
    {
        private int m_col, m_row, m_numColumns, m_numRows;
        protected T m_cell;

        private Rectangle m_border;
        protected Context ctx;

        public PaletteButton(int col, int row, int numCols, int numRows, int size, T cell, Context ctx, boolean addBackground)
        {
            m_col = col;
            m_row = row;
            m_numColumns = numCols;
            m_numRows = numRows;
            this.ctx = ctx;
            
            m_cell = cell;
            
            if (addBackground)
            {
                Rectangle bg = new Rectangle(numCols * size - 8, numRows * size - 8);
                int r = 240;
                bg.setFillColor(new Color(r, r, r));
                bg.setX(-size / 2 + 4);
                bg.setY(-size / 2 + 4);
                add(bg);
            }
            
            m_border = new Rectangle(numCols * size - 4, numRows * size - 4);
            m_border.setX(-size / 2 + 2);
            m_border.setY(-size / 2 + 2);
            m_border.setStrokeColor(ColorName.RED);
            m_border.setStrokeWidth(3);
            m_border.setVisible(false);
            add(m_border);
            
            ctx.backgroundLayer.add(this);
        }
        
        public T getItem()
        {
            return m_cell;
        }

        public void setSelected(boolean selected)
        {
            m_border.setVisible(selected);
        }
        
        public boolean selected(int col, int row)
        {
            boolean selected = (col >= m_col && col < m_col + m_numColumns && row >= m_row && row < m_row + m_numRows);
            setSelected(selected);
            return selected;
        }
        
        public void decrementColumn()
        {
            m_col--;
        }
    }
}
