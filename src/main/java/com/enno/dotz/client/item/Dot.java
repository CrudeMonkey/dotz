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
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.LetterMultiplier;

public class Dot extends Item
{
    public int color;
    private String m_letter;
    private int m_letterPoints;
    private LetterMultiplier m_multiplier; // = new LetterMultiplier(10, LetterMultiplier.WORD_MULTIPLIER);
    private Group m_multiplierGroup;
    private double m_tileSize;
    private Text m_pointsText;
    
    public Dot(int color)
    {
        this.color = color;
    }
    
    public Dot(int color, String letter, boolean stuck)
    {
        this.color = color;
        setLetter(letter);
        m_stuck = stuck;
    }
    
    public void setLetter(String letter)
    {
        m_letter = letter;
        if (letter != null)
        {
            m_letterPoints = Generator.getLetterPoints(letter);
            if (m_pointsText != null)
                m_pointsText.setText("" + m_letterPoints);
        }
    }
    
    public boolean isLetter()
    {
        return m_letter != null;
    }
    
    public String getLetter()
    {
        return m_letter;
    }
    
    public int getLetterPoints()
    {
        return m_letterPoints;
    }
    
    public LetterMultiplier getLetterMultiplier()
    {
        return m_multiplier;
    }
    
    public void setLetterMultiplier(LetterMultiplier m)
    {
        m_multiplier = m;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        if (m_letter == null)
        {
            Circle c = new Circle(size / 4);
            c.setFillColor(cfg == null ? Config.COLORS[0] : cfg.drawColor(color));       // cfg is null in ModePalette  
            g.add(c);
            
            addMark(color, g, size);
        }
        else
        {
            double w = size * 0.6; //0.8;
            m_tileSize = w;
            Rectangle r = new Rectangle(w, w, w * 0.1);
            r.setStrokeColor(ColorName.BLACK);
            r.setFillColor(cfg == null ? Config.COLORS[0] : cfg.drawColor(color));       // cfg is null in ModePalette  
            r.setX(-w / 2);
            r.setY(-w / 2);
            g.add(r);
            
            IColor textColor = ColorName.BLACK;
            
            if ("Qu".equals(m_letter))
            {
                Text text = new Text("Q");
                text.setFillColor(textColor);
                text.setFontSize(13);
                text.setFontStyle("bold");
                text.setTextBaseLine(TextBaseLine.MIDDLE);
                text.setTextAlign(TextAlign.CENTER);
                text.setX(-5);
                g.add(text);
 
                text = new Text("u");
                text.setFillColor(textColor);
                text.setFontSize(13);
                text.setFontStyle("bold");
                text.setTextBaseLine(TextBaseLine.MIDDLE);
                text.setTextAlign(TextAlign.CENTER);
                text.setX(6.5);
                text.setY(-2);
                g.add(text);
            }
            else
            {
                Text text = new Text(m_letter);
                text.setFillColor(textColor);
                text.setFontSize(13);
                text.setFontStyle("bold");
                text.setTextBaseLine(TextBaseLine.MIDDLE);
                text.setTextAlign(TextAlign.CENTER);
                g.add(text);
                
            }
            
            m_pointsText = new Text("" + m_letterPoints);
            m_pointsText.setFillColor(textColor);
            m_pointsText.setFontSize(7);
            m_pointsText.setFontStyle("bold");
            m_pointsText.setTextBaseLine(TextBaseLine.MIDDLE);
            m_pointsText.setTextAlign(TextAlign.CENTER);
            m_pointsText.setX(w * 0.3);
            m_pointsText.setY(w * 0.3);
            g.add(m_pointsText);
            
            m_multiplierGroup = new Group();
            m_multiplierGroup.setX(-w * 0.70);
            m_multiplierGroup.setY(-w * 0.70);
            g.add(m_multiplierGroup);
            
            updateMultiplier();
        }
        return g;
    }
    
    private void updateMultiplier()
    {
        m_multiplierGroup.removeAll();
        
        if (m_multiplier != null)
        {
            double w = m_tileSize * 0.7;
            double h = m_tileSize * 0.4;
            
            Rectangle r = new Rectangle(w, h);
            r.setStrokeColor(ColorName.BLACK);
            r.setFillColor(m_multiplier.isWordMultiplier() ? ColorName.YELLOW : new Color(230, 240, 255));
            m_multiplierGroup.add(r);
            
            Text text = new Text("x" + m_multiplier.getMultiplier());
            text.setFillColor(ColorName.BLACK);
            text.setFontSize(8);
            text.setFontStyle("bold");
            text.setTextBaseLine(TextBaseLine.MIDDLE);
            text.setTextAlign(TextAlign.CENTER);
            text.setX(w * 0.5);
            text.setY(h * 0.5);
            m_multiplierGroup.add(text);
        }
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
    
    @Override
    public Integer getColor()
    {
        return color;
    }
    
    @Override
    public boolean canConnect()
    {
        return true;
    }

    @Override
    public boolean canGrowFire()
    {
        return true;
    }

    @Override
    public boolean canChangeColor()
    {
        return true;
    }

    @Override
    public boolean canReshuffle()
    {
        return true;
    }

    @Override
    protected Item doCopy()
    {
        Dot dot = new Dot(color, m_letter, m_stuck);
        
        if (m_multiplier != null)
        {
            dot.setLetterMultiplier(m_multiplier.copy());
        }
        
        return dot;
    }

    public Dot copy()
    {
        return (Dot) doCopy();
    }
    
    /**
     * @param letter
     * @return Copy, but with a new letter
     */
    public Dot copy(String letter)
    {
        Dot dot = copy();
        dot.setLetter(letter);
        return dot;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // ignore passed in color
        ctx.score.explodedDot(this.color);
        
        return ExplodeAction.REMOVE; // remove dot
    }
}
