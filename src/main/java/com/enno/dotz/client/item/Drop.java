package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.google.gwt.dom.client.Style.FontWeight;

public class Drop extends Item
{
    private int m_radius;
    
    public Drop()
    {
        this(3, false);
    }
    
    public Drop(int radius, boolean stuck)
    {
        m_radius = radius;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "drop";
    }
    
    public void setRadius(int radius)
    {
        m_radius = radius;
    }

    @Override
    public IPrimitive<?> createShape(double sz)
    {
        Group shape = new Group();
        
        if (isStuck())
            shape.add(createStuckShape(sz));
        
        double scale = 0.055; //sz * 0.08 / 50;
        
        Group g = new Group();
        g.setX(150);
        g.setY(-35);
        g.setScale(scale);
        
        SVGPath p = new SVGPath("m -2692.9779,255.80653 c -19.7513,90.86372 -49.6938,179.50822 -89.0857,263.73772 -19.8543,42.45337 -42.1919,84.02012 -56.1329,128.7653 -13.941,44.74517 -19.0203,93.85191 -3.8584,138.19825 12.9996,38.02187 40.9097,70.72513 76.421,89.5294 35.5113,18.80426 78.2514,23.50941 117,12.86986 38.7486,-10.63956 73.096,-36.514 94.0049,-70.82827 20.9089,-34.31428 28.1558,-76.70641 19.8222,-116.0155 -7.5883,-35.79365 -27.2342,-67.87162 -49.186,-97.14427 -21.9517,-29.27265 -46.5723,-56.61128 -66.2989,-87.42731 -48.996,-76.53924 -64.8193,-173.54334 -42.6862,-261.68518 l 0,0");
        //p.setStrokeColor(ColorName.BLACK);
        p.setFillColor(Color.fromColorString("#505F93"));
        //p.setStrokeWidth(1 / scale);
        g.add(p);
        
//                 p = new SVGPath("m -2692.9779,255.80653 c -19.7513,90.86372 -49.6938,179.50822 -89.0857,263.73772 -19.8543,42.45337 -42.1919,84.02012 -56.1329,128.7653 -13.941,44.74517 -19.0203,93.85191 -3.8584,138.19825 12.9996,38.02187 40.9097,70.72513 76.421,89.5294 35.5113,18.80426 78.2514,23.50941 117,12.86986 38.7486,-10.63956 73.096,-36.514 94.0049,-70.82827 20.9089,-34.31428 28.1558,-76.70641 19.8222,-116.0155 -7.5883,-35.79365 -27.2342,-67.87162 -49.186,-97.14427 -21.9517,-29.27265 -46.5723,-56.61128 -66.2989,-87.42731 -48.996,-76.53924 -64.8193,-173.54334 -42.6862,-261.68518 l 0,0");
//        p.setFillColor(Color.fromColorString("#505F93"));
//        //p.setStrokeWidth(1 / scale);
//        g.add(p);
        
        p = new SVGPath("m -2716.4548,394.92383 c -23.8374,73.035 -50.9538,144.99967 -81.2403,215.60463 -9.6097,22.40252 -19.5713,44.7594 -26.1612,68.22838 -6.5898,23.46898 -9.726,48.30968 -5.4187,72.30274 2.3222,12.93549 6.8192,25.53205 13.7259,36.71318 6.9066,11.18113 16.2569,20.92114 27.4644,27.78489 11.2076,6.86375 24.2868,10.78711 37.4284,10.64651 13.1415,-0.14059 26.2945,-4.43009 36.6514,-12.52049 10.5622,-8.25078 17.9459,-20.18318 21.8539,-33.0036 3.908,-12.82041 4.4771,-26.50199 2.9446,-39.81688 -3.0651,-26.6298 -14.2332,-51.52197 -22.9608,-76.86698 -29.6969,-86.23986 -31.2217,-181.93012 -4.2876,-269.07238");
        p.setFillColor(Color.fromColorString("#C4CEE4"));
        //p.setStrokeWidth(1 / scale);
        g.add(p);

        shape.add(g);
        return shape;
    }

    @Override
    public boolean canGrowFire()
    {
        return true;
    }
    
    @Override
    protected Item doCopy()
    {
        return new Drop(m_radius, m_stuck);
    }
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        return ExplodeAction.REMOVE;
    }

    public int getRadius()
    {
        return m_radius;
    }
}
