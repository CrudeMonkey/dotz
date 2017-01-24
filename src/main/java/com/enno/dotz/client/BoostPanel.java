package com.enno.dotz.client;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.editor.Palette;
import com.enno.dotz.client.editor.Palette.PaletteButton;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Key;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.YinYang;
import com.enno.dotz.client.ui.MXHBox;
import com.smartgwt.client.types.Alignment;

public class BoostPanel extends MXHBox
{
    //protected static final int SIZE = 50;
    
    private BoostPalette m_palette;

    public BoostPanel(Context ctx)
    {
        setWidth(ctx.gridWidth);
        
        m_palette = new BoostPalette(ctx, ctx.cfg, ctx.cfg.size);
        addMember(m_palette);
        setAlign(Alignment.CENTER);
    }
    
    public void setActive(boolean active)
    {
        m_palette.setActive(active);
    }
    
    public static class BoostPalette extends Palette<Object>
    {
        private PaletteButton<Object> m_selected;
        private ConnectMode m_connectMode;
        private double m_size;
        
        public BoostPalette(Context ctx, Config level, int size)
        {
            super((int) (0.8 * size), (int) size, (ctx.gridWidth / size), 1);
            m_size = size;
            
            m_active = false;
            
            setBackgroundColor(ColorName.WHITE);
            
            ctx.backgroundLayer = new FastLayer();
            add(ctx.backgroundLayer);

            Boosts boosts = level.boosts;
            
            int col = 0;
            if (boosts.turners > 0)
                addButton(boosts.turners, new Turner(1, false), col++, 0);
            if (boosts.drops > 0)
                addButton(boosts.drops, new Drop(), col++, 0);
            if (boosts.colorBombs > 0)
                addButton(boosts.colorBombs, new ColorBomb(false), col++, 0);
            if (boosts.explodies > 0)
                addButton(boosts.explodies, new Explody(), col++, 0);
            if (boosts.wildCards > 0)
                addButton(boosts.wildCards, new Wild(false), col++, 0);
            if (boosts.reshuffles > 0)
                addButton(boosts.reshuffles, new YinYang(false), col++, 0);
            if (boosts.keys > 0)
                addButton(boosts.keys, new Key(false), col++, 0);
            if (boosts.picks > 0)
                addButton(boosts.picks, new IcePick(), col++, 0);
        }
        
        public void setActive(boolean active)
        {
            m_active = active;
        }

        protected void selected(PaletteButton<Object> selectedButton)
        {
            if (m_selected == selectedButton && selectedButton != null)
            {
                if (selectedButton.getItem() instanceof Turner)
                {
                    Turner turner = (Turner) selectedButton.getItem();
                    int n = turner.n;
                    n = (n % 3) + 1;
                    ((BoostButton) selectedButton).swapItem(new Turner(n, turner.isStuck()));
                    if (n == 1)
                    {
                        selectedButton = null;
                    }
//                    else
//                    {
//                        n = (n % 3) + 1;
//                        ((BoostButton) selectedButton).swapItem(new Turner(n));
//                    }
                }
                else
                {
                    selectedButton = null;
                }
                if (selectedButton == null)
                {
                    deselectAll();
                }
            }
            m_selected = selectedButton;
            
            ctx.backgroundLayer.redraw();
            
            m_connectMode.cancelBoostMode();
            if (m_selected != null)
            {
                final BoostButton selected = (BoostButton) selectedButton;                
                m_connectMode.startBoostMode((Item) selected.getItem(), new Runnable() {
                    @Override
                    public void run()
                    {
                        int n = selected.getCount();
                        if (n == 1)
                        {
                            removeBoostButton(selected);
                            //m_connectMode.cancelBoostMode();
                        }
                        else
                        {
                            selected.setCount(n - 1);
                        }
                        
                        m_selected = null;
                        deselectAll();
                        
                        ctx.backgroundLayer.redraw();                        
                    }
                });
            }
        }
        
        protected void removeBoostButton(BoostButton b)
        {
            int i = m_list.indexOf(b);
            m_list.remove(i);
            ctx.backgroundLayer.remove(b);
            
            // Move other buttons to the left
            while (i < m_list.size())
            {
                BoostButton m = (BoostButton) m_list.get(i);
                m.setX((i + 0.5) * m_size);
                m.decrementColumn();
                i++;
            }
        }
        
        private void addButton(int count, Object cell, int col, int row)
        {
            double W = m_size;
            double x = (col + 0.5) * W;
            double y = (row + 0.5) * W;
            
            int numCols = 1;
            int numRows = 1;
            
            IPrimitive<?> shape = null;
            if (cell instanceof Item)
            {
                Item item = (Item) cell;
                item.setContext(ctx);
                shape = item.createShape(ctx.cfg.size);                
            }
            
            BoostButton b = new BoostButton(col, row, numCols, numRows, cell, shape, count, ctx, m_size);
            b.setX(x);
            b.setY(y);
            m_list.add(b);
        }
        
        public void setConnectMode(ConnectMode connectMode)
        {
            m_connectMode = connectMode;
            m_active = connectMode != null;
        }
    }
    
    public void setConnectMode(ConnectMode connectMode)
    {
        m_palette.setConnectMode(connectMode);
    }
    
    public static class BoostButton extends PaletteButton<Object>
    {
        public static final int SIZE = 40 + 10;
        private IPrimitive<?> m_shape;
        private int m_count;                

        private Text m_text;
        private Circle m_countCircle;
        private double m_size;
        
        public BoostButton(int col, int row, int numCols, int numRows, Object cell, IPrimitive<?> shape, int count, Context ctx, double size)
        {
            super(col, row, numCols, numRows, (int) size, cell, ctx, true);
            
            m_count = count;
            m_shape = shape;
            m_size = size;
            
            add(shape);
            
            shape.setX(0);
            shape.setY(0);
            
            addCountCircle();
        }
        
        protected void addCountCircle()
        {
            double r = m_size * 0.16;
            double p = 3 + m_size * 0.25 - r / 3;
            m_countCircle = new Circle(r);
            m_countCircle.setFillColor(ColorName.WHITE);
            m_countCircle.setStrokeColor(ColorName.BLACK);
            m_countCircle.setX(p);
            m_countCircle.setY(p);
            add(m_countCircle);
            
            m_text = new Text("" + m_count);
            m_text.setFontSize(9);
            m_text.setFontStyle("bold");
            m_text.setTextAlign(TextAlign.CENTER);
            m_text.setTextBaseLine(TextBaseLine.MIDDLE);
            m_text.setFillColor(ColorName.BLACK);
            m_text.setX(p);
            m_text.setY(p);
            add(m_text);
        }
        
        public void setCount(int count)
        {
            m_count = count;
            if (m_text != null)
                m_text.setText("" + count);
        }
        
        public int getCount()
        {
            return m_count;
        }
        
        public void swapItem(Item item)
        {
            remove(m_countCircle);
            remove(m_text);
            
            remove(m_shape);
            
            m_cell = item;
            item.setContext(ctx);
            m_shape = item.createShape(ctx.cfg.size);
            add(m_shape);
            
            addCountCircle();
        }
    }
}
