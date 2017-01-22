package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.item.Animal.Action;
import com.enno.dotz.client.item.Animal.Type;
import com.google.gwt.dom.client.Style.FontWeight;

public class Spider extends Item
{
    private int m_strength = 19;
    private Integer m_color;
    private Action m_action;
    private Text m_text;
    private double m_pupilRadius;
    private double m_eyeHeight;
    private Type m_type;
    private double[] m_x = new double[2];
    private double[] m_y = new double[2];
    private Ellipse[] m_eyes = new Ellipse[2];
    private Circle[] m_pupils = new Circle[2];
    
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
            
            p = new PolyLine(s * 0.2, dy3, 
                    s * 0.34, dy3 -size * 0.24, 
                    s * 0.4, size * 0.18);
            p.setStrokeColor(ColorName.BLACK);
            //p.setStrokeWidth(2);
            g.add(p);

            p = new PolyLine(s * 0.18, dy3, 
                    s * 0.34, dy3 -size * 0.4, 
                    s * 0.42, -size * 0.1);
            p.setStrokeColor(ColorName.BLACK);
//            p.setStrokeWidth(2);
            g.add(p);
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
        m_eyeHeight = eyeWidth;
        m_pupilRadius = size * 0.08;
        
        for (int i = 0; i < 2; i++)
        {
            Ellipse eye = new Ellipse(eyeWidth, m_eyeHeight);
            eye.setFillColor(ColorName.WHITE);
            
            double x = size * 0.08 * (i == 0 ? -1 : 1);
            double y = dy2;
            m_x[i] = x;
            m_y[i] = y;
            
            eye.setX(x);
            eye.setY(y);
            
            g.add(eye);            
            m_eyes[i] = eye;
            
            Circle pupil = new Circle(1.5);
            pupil.setFillColor(ColorName.BLACK);
            
            pupil.setX(x);
            pupil.setY(y);
            
            g.add(pupil);
            m_pupils[i] = pupil;
        }
        return g;
    }

    @Override
    protected Item doCopy()
    {
        return new Spider();
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // TODO Auto-generated method stub
        return ExplodeAction.REMOVE;
    }
}
