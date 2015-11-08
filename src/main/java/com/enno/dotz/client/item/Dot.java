package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.RegularPolygon;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;

public class Dot extends Item
{
    public int color;
    public String letter;
    
    public Dot(int color)
    {
        this.color = color;
    }
    
    public Dot(int color, String letter)
    {
        this.color = color;
        this.letter = letter;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        Circle c = new Circle(size / 4);
        c.setFillColor(cfg == null ? Config.COLORS[0] : cfg.drawColor(color));       // cfg is null in ModePalette  
        g.add(c);
        
        if (letter == null)
            addMark(color, g, size);
        else
        {
            Text text = new Text(letter);
            text.setFillColor(ColorName.WHITE);
            text.setFontSize(13);
            text.setFontStyle("bold");
            text.setTextBaseLine(TextBaseLine.MIDDLE);
            text.setTextAlign(TextAlign.CENTER);
            g.add(text);
        }
        return g;
    }

    public static void addMark(int color, Group g, double size)
    {
        double r = size * 0.1;
        double w = 2;
        if (color == 1)
        {
            // triangle
            double c30 = Math.cos(Math.toRadians(30));
            double s30 = Math.sin(Math.toRadians(30));
            Triangle tri = new Triangle(
                    new Point2D(0, -r), 
                    new Point2D(r * c30, r * s30), 
                    new Point2D(-r * c30, r * s30));
            tri.setStrokeColor(ColorName.WHITE);
            tri.setStrokeWidth(w);
            g.add(tri);
        }
        else if (color == 4)
        {
            // square           
            Rectangle a = new Rectangle(2 * r, 2 * r);
            a.setX(-r);
            a.setY(-r);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
        }
        else if (color == 3)
        {
            // plus            
            Line a = new Line(0, -r, 0, r);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
            
            a = new Line(r, 0, -r, 0);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
        }
        else if (color == 2)
        {
            // minus            
            Line a = new Line(-r, 0, r, 0);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
        }
        else if (color == 0)
        {
            // equals
            double d = r * 0.6;
            Line a = new Line(-d, -r, -d, r);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
            
            a = new Line(d, -r, d, r);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);
        }
        else if (color == 5)
        {
            // equals
            RegularPolygon a = new RegularPolygon(6, r * 1.5);
            a.setStrokeColor(ColorName.WHITE);
            a.setStrokeWidth(w);
            g.add(a);            
        }
    }
    
    public Integer getColor()
    {
        return color;
    }
    
    public boolean canConnect()
    {
        return true;
    }

    public boolean canGrowFire()
    {
        return true;
    }

    public boolean canChangeColor()
    {
        return true;
    }

    protected Item doCopy()
    {
        return new Dot(color, letter);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // ignore passed in color
        ctx.score.explodedDot(this.color);
        
        return ExplodeAction.REMOVE; // remove dot
    }
}
