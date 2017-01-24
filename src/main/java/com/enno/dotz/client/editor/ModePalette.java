package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.editor.EditLayoutTab.ConnectTeleportMode;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Cog;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.Wild;

public class ModePalette extends Palette<Object>
{
    public ModePalette(ConnectTeleportMode connectTeleportMode)
    {
        super(40, ItemButton.SIZE, 6, 2);
        
        ctx.dotLayer = new FastLayer();
        add(ctx.dotLayer);                
        
        addButton(new ChangeIce(), 0, 0);
        addButton(new DeleteItem(), 1, 0);
        addButton(new DeleteSusan(), 2, 0);
        addButton(connectTeleportMode, 3, 0);
        addButton(new RotateItem(), 4, 0);
        addButton(new Bombify(), 5, 0);
        addButton(new SetController(), 0, 1);
        addButton(new Chestify(), 1, 1);
        addButton(new Stick(), 2, 1);
        addButton(new RadioActive(), 3, 1);
    }
    
    private void addButton(Object cell, int col, int row)
    {
        int W = ItemButton.SIZE;
        double x = (col + 0.5) * W;
        double y = (row + 0.5) * W;
        
        int numCols = 1;
        int numRows = 1;
        
        if (cell instanceof ModeBase)
        {
            IPrimitive<?> shape = ((ModeBase) cell).createShape(ctx.cfg.size);
            shape.setX(x);
            shape.setY(y);
            ctx.dotLayer.add(shape);
        }

        ItemButton b = new ItemButton(col, row, numCols, numRows, cell, ctx);
        b.setX((col + 0.5) * W);
        b.setY((row + 0.5) * W);
        m_list.add(b);
    }
    
    public static interface ModeBase
    {
        public IPrimitive<?> createShape(int size);
    }
    
    public static class DeleteItem implements ModeBase
    {
        public IPrimitive<?> createShape(int size)
        {
            Group g = new Group();
            
            g.add(new Wild(false).createShape(size));
            
            double d = size * 0.65;
            double d2 = d / 2;
            
            Line line = new Line(-d2, -d2, d2, d2);
            line.setStrokeColor(ColorName.RED);
            line.setStrokeWidth(3);
            g.add(line);
            
            line = new Line(-d2, d2, d2, -d2);
            line.setStrokeColor(ColorName.RED);
            line.setStrokeWidth(3);
            g.add(line);
            
            return g;
        }
    }
    
    public static class RotateItem implements ModeBase
    {
        public IPrimitive<?> createShape(int sz)
        {
            Group g = new Group();
            
            IColor color = ColorName.DARKORANGE;
            double r = sz / 4;
            Circle c = new Circle(r);
            c.setStrokeColor(color);
            c.setStrokeWidth(3);
            g.add(c);
            
            double d = r / 4;
            double h = r / 2;
            double f = 1;
            
            Triangle t = new Triangle(new Point2D(-d * f, r), new Point2D(d * f, r + h), new Point2D(d * f, r - h));
            t.setFillColor(color);
            t.setRotation(Math.PI);
            g.add(t);
            
            return g;
        }
    }
    
    public static class Stick implements ModeBase
    {
        public IPrimitive<?> createShape(int size)
        {
            return Item.createStuckShape(size);
        }
    }
    
    public static class RadioActive implements ModeBase
    {
        public IPrimitive<?> createShape(int sz)
        {
            return createRadioActiveShape(sz);
        }
        
        public static IPrimitive<?> createRadioActiveShape(double sz)
        {
            Group g = new Group();
            
            double r = sz * 0.4;
            Circle c = new Circle(r);
            c.setFillColor(ColorName.YELLOW);
            c.setStrokeColor(ColorName.BLACK);
            //c.setStrokeWidth(3);
            g.add(c);
            
            double r2 = r * 0.8;
            double a = Math.PI / 3;
            
            Slice s = new Slice(r2, 0, a, false);
            s.setFillColor(ColorName.BLACK);
            g.add(s);
            
            s = new Slice(r2, a*2, a*3, false);
            s.setFillColor(ColorName.BLACK);
            g.add(s);
            
            s = new Slice(r2, a*4, a*5, false);
            s.setFillColor(ColorName.BLACK);
            g.add(s);
            
            double r3 = r * 0.25;
            c = new Circle(r3);
            c.setFillColor(ColorName.BLACK);
            c.setStrokeColor(ColorName.YELLOW);
            //c.setStrokeWidth(2);
            g.add(c);
            
            return g;
        }
    }

    public static class Bombify implements ModeBase
    {
        public IPrimitive<?> createShape(int sz)
        {
            return new DotBomb(new Dot(0), 9, false).createShape(sz);
        }
    }

    public static class Chestify implements ModeBase
    {
        public IPrimitive<?> createShape(int sz)
        {
            return new Chest(new Dot(0), 9).createShape(sz);
        }
    }
    
    public static class SetController implements ModeBase
    {
        public IPrimitive<?> createShape(int sz)
        {
            return new Cog().createShape(sz);
        }
    }
    
    public static class DeleteSusan implements ModeBase
    {
        public IPrimitive<?> createShape(int size)
        {
            Group g = new Group();
            
            Group shape = new LazySusan(0, 0, true).createShape(size);
            shape.setRotation(Math.PI / 4);
            
            g.add(shape);

            double d = size * 0.65;
            double d2 = d / 2;
            
            Line line = new Line(-d2, -d2, d2, d2);
            line.setStrokeColor(ColorName.RED);
            line.setStrokeWidth(3);
            g.add(line);
            
            line = new Line(-d2, d2, d2, -d2);
            line.setStrokeColor(ColorName.RED);
            line.setStrokeWidth(3);
            g.add(line);
            
            return g;
        }
    }
    
    public static class ChangeIce implements ModeBase
    {
        public IPrimitive<?> createShape(int size)
        {
            Group g = new Group();
            
            double sz = size * 0.65;
            Rectangle ice = new Rectangle(sz, sz);
            ice.setX(- sz / 2);
            ice.setY(- sz / 2); 
            
            int r = 255 - 3 * 20;
            if (r < 0) r = 0;
            ice.setFillColor(new Color(r, r, r));
            g.add(ice);
            
            Text txt = new Text("ICE");
            txt.setFontSize(7);
            txt.setFontStyle("bold");
            txt.setFillColor(ColorName.BLACK);
            txt.setTextBaseLine(TextBaseLine.MIDDLE);
            txt.setTextAlign(TextAlign.CENTER);
            
            g.add(txt);
            
            return g;
        }
    }
    
    public static class ItemButton extends PaletteButton<Object>
    {
        public static final int SIZE = 40 + 10;                

        public ItemButton(int col, int row, int numCols, int numRows, Object cell, Context ctx)
        {
            super(col, row, numCols, numRows, SIZE, cell, ctx, true);
        }
    }
}
