package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.shared.core.types.ColorName;

/**
 * Inspired by eggs in Farm Club
 * 
 * @author Enno
 */
public class Egg extends Item
{
    private boolean m_cracked;
    private SVGPath m_crack;
    
    public Egg()
    {
    }
    
    public Egg(boolean cracked, boolean stuck)
    {
        m_cracked = cracked;
        m_stuck = stuck;
    }
    
    public boolean isCracked()
    {
        return m_cracked;
    }
    
    public void setCracked(boolean cracked)
    {
        m_cracked = cracked;
    }
    
    public void crack()
    {
        setCracked(true);
        m_crack.setVisible(true);
    }
    
    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group shape = new Group();
        if (isStuck())
            shape.add(createStuckShape(sz));
        
        double scale = sz * 0.08 / 50;
        
        Group g = new Group();
        g.setX(-sz / 2);
        g.setY(-sz / 2);
        g.setScale(scale);
        
        SVGPath p = new SVGPath("M 486.46256,349.71691 C 486.46256,442.76834 416.06256,518.28834 329.31971,518.28834 C 242.57686,518.28834 172.17685,442.76834 172.17685,349.71691 C 172.17685,256.66549 242.57686,89.716923 329.31971,89.716923 C 416.06256,89.716923 486.46256,256.66549 486.46256,349.71691 z");
        p.setStrokeColor(ColorName.BLACK);
        p.setFillColor(ColorName.BEIGE);
        p.setStrokeWidth(1 / scale);
        g.add(p);
        
        m_crack = new SVGPath("M 192.21155,246.11012 C 178.54,284.67148 170.6803,323.9634 170.6803,355.01637 C 170.6803,448.0678 241.0937,523.57889 327.83655,523.57887 C 414.5794,523.57887 484.99283,448.0678 484.9928,355.01637 C 484.9928,329.19951 480.98023,296.97695 471.29637,264.92708 L 470.57762,266.86458 L 455.21155,249.54762 L 451.1178,285.61012 L 427.3678,258.57887 C 427.3678,258.57887 419.18031,338.82155 419.1803,343.73512 C 419.1803,348.64867 386.43031,265.95386 386.4303,265.95387 L 373.33655,288.04762 L 364.33655,270.86012 L 347.9303,341.26637 L 334.83655,267.57887 L 308.64905,312.61012 L 286.52405,268.39137 L 260.3053,348.64137 L 248.02405,265.11012 L 231.64905,295.42262 L 216.08655,260.20387 L 204.1178,282.45387 L 193.2428,246.54762 L 192.21155,246.11012 z");
        m_crack.setStrokeColor(ColorName.BLACK);
        m_crack.setStrokeWidth(1 / scale);
        m_crack.setVisible(m_cracked);
        g.add(m_crack);
        
        shape.add(g);
        
        return shape;
    }

    @Override
    public boolean canRotate()
    {
        return true;
    }
    
    @Override
    public void rotate(int n)
    {
        m_cracked = !m_cracked;
        m_crack.setVisible(m_cracked);
    }
    
    @Override
    protected Item doCopy()
    {
        return new Egg(m_cracked, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }
}
