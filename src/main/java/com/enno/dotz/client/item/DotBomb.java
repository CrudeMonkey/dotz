package com.enno.dotz.client.item;

import java.util.Random;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.anim.Ignition;

public class DotBomb extends Item
{
    private static final int N = 15;
    private static double R = 8;
    
    protected Dot m_dot;
    private int m_strength;
    private Text m_text;
    private Random rnd = new Random();
    private Line[] m_lines = new Line[N];
    
    public DotBomb(Dot dot, int strength, boolean stuck)
    {
        m_strength = strength;
        m_dot = dot;
        m_stuck = stuck;
    }
        
    @Override
    public boolean isRadioActive()
    {
        return m_dot.isRadioActive();
    }

    @Override
    public Integer getColor()
    {
        return m_dot.getColor();
    }
    
    @Override
    public boolean canConnect()
    {
        return true;
    }

    @Override
    public boolean canGrowFire()
    {
        return true; //TODO or not?
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
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        g.add(m_dot.createShape(size));
        
        double r = size * 0.16;
        double p = size * 0.25 - r / 3;
        Circle c = new Circle(r);
        c.setFillColor(ColorName.WHITE);
        c.setStrokeColor(ColorName.BLACK);
        c.setX(-p);
        c.setY(p);
        g.add(c);
        
        m_text = new Text("" + m_strength);
        m_text.setFontStyle("bold");
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE);
        m_text.setFillColor(ColorName.BLACK);
        
        if (size < Config.DEFAULT_CELL_SIZE)    // inside Machine
        {
            m_text.setFontSize(6);
            m_text.setX(-p);
            m_text.setY(p+1);
        }
        else
        {
            m_text.setFontSize(9);
            m_text.setX(-p);
            m_text.setY(p);
        }
        g.add(m_text);
        
        // Draw fuse        
        Group fuse = new Group();
        fuse.setRotation(Math.PI / 4);
        g.add(fuse);
        
        if (m_dot.isLetter())
        {
            double s = size * 0.13;
            fuse.setX(s);
            fuse.setY(-s);
        }
        
        double y = -size * 0.18;
        double y2 = -size * 0.30;
        double w = size * 0.16;
        Ellipse e = new Ellipse(w, size * 0.1);
        e.setFillColor(ColorName.BLACK);
        e.setY(y);
        fuse.add(e);
        
        Line line = new Line(0, y, 0, y2);
        line.setStrokeColor(ColorName.BLACK);
        line.setStrokeWidth(w);
        fuse.add(line);

        Ellipse e2 = new Ellipse(w, size * 0.1);
        e2.setFillColor(ColorName.BLACK);
        e2.setStrokeColor(ColorName.GRAY);
        e2.setY(y2);
        fuse.add(e2);

        for (int i = 0; i < N; i++)
        {
            Line a = new Line();
            a.setStrokeWidth(1);
            a.setVisible(false);
            a.setY(y2);
            a.setStrokeColor(Ignition.COLORS[rnd.nextInt(Ignition.COLORS.length)]);
            m_lines[i] = a;
            fuse.add(a);
        }
        
        return g;
    }
    
    @Override
    public void animate(long t, double cursorX, double cursorY)
    {
        for (int i = 0; i < N; i++)
        {
            Line a = m_lines[i];
            if (rnd.nextInt(3) < 2)
            {
                a.setVisible(true);
                double r1 = rnd.nextDouble() * R;
                double r2 = rnd.nextDouble() * R;
                double angle = rnd.nextDouble() * Math.PI * 2;
                                    
                Point2DArray points = new Point2DArray();
                points.push(r1 * Math.sin(angle), r1 * Math.cos(angle));
                points.push(r2 * Math.sin(angle), r2 * Math.cos(angle));
                a.setPoints(points);
            }
            else
                a.setVisible(false);
        }
        
        if (isRadioActive())
            m_dot.animate(t, cursorX, cursorY);
    }
    
    @Override
    public boolean canIncrementStrength()
    {
        return true;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (ds == -1 && m_strength <= 1)
            return;
        
        m_strength += ds;
        updateStrength();
    }
    
    public void updateStrength()
    {
        if (m_text != null)
            m_text.setText("" + m_strength);
    }
    
    @Override
    public void setContext(Context ctx)
    {
        super.setContext(ctx);
        m_dot.setContext(ctx);
    }

    @Override
    protected Item doCopy()
    {
        return new DotBomb((Dot) m_dot.copy(), m_strength, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return m_dot.explode(color, chainSize);
    }

    public boolean tick()
    {
        if (m_strength > 0)
            m_strength--;
        
        updateStrength();
        
        if (m_strength > 0)
        {
            return false;
        }
               
        return true; // explode - end of level
    }
    
    public Dot getDot()
    {
        return m_dot;
    }
    
    public void setDot (Dot dot)
    {
        m_dot = dot;
    }
    
    public int getStrength()
    {
        return m_strength;
    }

    public void setStrength(int strength)
    {
        m_strength = strength;
    }
}
