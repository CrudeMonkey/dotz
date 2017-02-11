package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.item.Animal.Action;
import com.enno.dotz.client.item.Animal.Eyes;
import com.enno.dotz.client.item.Animal.Type;
import com.google.gwt.dom.client.Style.FontWeight;

public class Spider extends Item
{
    private static final int BLINK_DURATION = Animal.BLINK_DURATION;

    private int     m_strength = 19;
    private Integer m_color;
    private Action  m_action;
    private Type    m_type;

    private Text m_text;

    private PolyLine[] m_legs   = new PolyLine[6];
    private Eyes m_eyes;
    
    public int lastDirection = Direction.NONE;
    
    
    public Spider()
    {
        this(1, false);
    }
    
    public Spider(int strength, boolean stuck)
    {
        m_strength = strength;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "spider";
    }
    
    public int getStrength()
    {
        return m_strength;
    }

    public void setStrength(int strength)
    {
        m_strength = strength;
        
        if (m_text != null)
        {
            m_text.setVisible(m_strength > 1);
            m_text.setText("" + m_strength);
        }
    }
    
    @Override
    public boolean canIncrementStrength()
    {
        return true;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (m_strength <= 1 && ds == -1)
            return;
        
        setStrength(m_strength + ds);
    }
    
    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean canSwap()
    {
        return false;
    }
    
    @Override
    public boolean canBeReplaced()
    {
        return false;
    }
    
    @Override
    public boolean canConnect()
    {
        return false;
    }
    
    @Override
    public boolean canExplodeNextTo()
    {
        return true;
    }
    
    @Override
    public boolean stopsLaser()
    {
        return true;
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
//        double w = size * 0.6;
//        double h = w * 0.6;
        
        
        double r1 = size * 0.5;
        double r2 = size * 0.35;
        
        double dy = -r1 * 0.01;
        
        Ellipse e = new Ellipse(r1, r1);
        e.setFillColor(ColorName.BLACK);
        e.setY(dy);
        g.add(e);
        
        double dy2 = dy + (r1 + r2) * 0.2;
        
        e = new Ellipse(r2, r2);
        e.setFillColor(ColorName.BLACK);
        e.setY(dy2);
        g.add(e);
        
        double dy3 = dy + size * 0.1;
        for (int side = -1; side <= 1; side += 2) // -1, 1
        {
            double s = side * size;
            
            PolyLine p = new PolyLine(s * 0.25, dy3, 
                                      s * 0.3, dy3 -size * 0.1, 
                                      s * 0.38, size * 0.4);
            p.setStrokeColor(ColorName.BLACK);
            //p.setStrokeWidth(2);            
            g.add(p);
            m_legs[side == -1 ? 2 : 3] = p;
            
            p = new PolyLine(s * 0.2, dy3, 
                    s * 0.34, dy3 -size * 0.24, 
                    s * 0.4, size * 0.18);
            p.setStrokeColor(ColorName.BLACK);
            //p.setStrokeWidth(2);
            g.add(p);
            m_legs[side == -1 ? 0 : 1] = p;

            p = new PolyLine(s * 0.18, dy3, 
                    s * 0.34, dy3 -size * 0.4, 
                    s * 0.42, -size * 0.1);
            p.setStrokeColor(ColorName.BLACK);
//            p.setStrokeWidth(2);
            g.add(p);
            m_legs[side == -1 ? 4 : 5] = p;
        }
        
        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.WHITE);
        m_text.setFontSize(7);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setY(dy - size * 0.085);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE);
        g.add(m_text);
//        
//        m_pupilRadius = r * 0.25;
//        m_eyeHeight = r * 0.7;
//        double eyeWidth = m_eyeHeight;
//        
//        if (m_type == Type.FROZEN)      // looks sleepy
//        {
//            m_eyeHeight *= 0.5;
//            m_pupilRadius *= 0.5;
//        }
//        
        double eyeWidth = r2 * 0.45;
        double eyeHeight = eyeWidth;
        double pupilRadius = size * 0.05;
        
        m_eyes = new Eyes(BLINK_DURATION, false, eyeWidth, eyeHeight, pupilRadius, 1.5, ColorName.WHITE, null, ColorName.BLACK);
        
        for (int i = 0; i < 2; i++)
        {
            double x = size * 0.08 * (i == 0 ? -1 : 1);
            double y = dy2;
            m_eyes.add(i, x, y, g);
       }
        
        m_text.setVisible(m_strength > 1);
        
        return g;
    }

    @Override
    protected Item doCopy()
    {
        Spider s = new Spider(m_strength, m_stuck);
        s.lastDirection = lastDirection;
        return s;
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        setStrength(m_strength - 1);
        
        if (m_strength <= 0)
        {
            Sound.SPIDER_DIED.play();
            ctx.score.explodedSpider();
            return ExplodeAction.REMOVE; // remove spider
        }
        
        return ExplodeAction.NONE; // don't remove spider
    }
    
    @Override
    public void animate(long t, double x, double y)
    {
        m_eyes.animate(t, x - shape.getX(), y - shape.getY());
        
//        long dt = t - m_startBlink;
//        if (dt > BLINK_DURATION)
//        {
//            m_startBlink = 2 * BLINK_DURATION + Animal.s_blinkRandom.nextInt(4 * BLINK_DURATION) + System.currentTimeMillis();
//        }
//        else if (dt > 0)
//        {
//            double half = BLINK_DURATION / 2.0;
//            double h;
//            if (dt < half)
//            {
//                h = 1 - dt / half;
//            }
//            else
//            {
//                h = (dt - half) / half;
//            }
//            m_eyes[0].setHeight(h * m_eyeHeight);
//            m_eyes[1].setHeight(h * m_eyeHeight);
//        }
//        
//        // pupils track the mouse pointer
//        for (int i = 0; i < 2; i++)
//        {
//            double dx = x - m_x[i] - shape.getX();
//            double dy = y - m_y[i] - shape.getY();
//            double angle = Math.atan2(dy, dx);
//            double nx = m_x[i] + Math.cos(angle) * m_pupilRadius;
//            double ny = m_y[i] + Math.sin(angle) * m_pupilRadius;
//            m_pupils[i].setX(nx);
//            m_pupils[i].setY(ny);
//        }
    }
    
    static double UP_DOWN = 1 / 6.0;
    static double DY = 4;
    
    public void animateLegs(double pct)
    {
        if (pct == 1)
        {
            for (int i = 0; i < 6; i++)
                m_legs[i].setY(0);
        }
        else
        {
            int n = (int) (pct / UP_DOWN);
            double t = (pct - n * UP_DOWN) / UP_DOWN;
            
            int dir = (n % 2) == 0 ? 1 : -1;
            
            m_legs[0].setY(DY * dir * t);
            m_legs[1].setY(DY * -dir * t);
            m_legs[2].setY(DY * -dir * t);
            m_legs[3].setY(DY * dir * t);
            m_legs[4].setY(DY * dir * t);
            m_legs[5].setY(DY * -dir * t);
        }
    }
}
