package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.SVGPath;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;

public class Bomb extends Item
{
    private int m_radius = 1;
    private boolean m_armed;
    
    public Bomb()
    {
    }
    
    public Bomb(int radius, boolean stuck)
    {
        m_radius = radius;
        m_stuck = stuck;
    }
    
    public int getRadius()
    {
        return m_radius;
    }
    
    public boolean isArmed()
    {
        return m_armed;
    }
    
    public void arm()
    {
        m_armed = true;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group shape = new Group();

        if (isStuck())
            shape.add(createStuckShape(size));
        
        double scale = 0.07;
        
        Group g = new Group();
        
        IColor col1 = Color.hex2RGB("#777777"); //#999999");
        IColor col2 = Color.hex2RGB("#666666"); // "#808080");
        
        // Fuse
        SVGPath p = new SVGPath("M96.911,120.104c-2.659,0-5.319-1.014-7.347-3.044c-9.494-9.496-11.729-17.514-13.523-23.956 c-1.488-5.344-2.564-9.205-8.199-14.839c-5.636-5.636-9.497-6.712-14.842-8.202c-6.444-1.795-14.462-4.03-23.958-13.526 s-11.73-17.514-13.526-23.958c-1.489-5.344-2.566-9.206-8.2-14.842c-4.058-4.058-4.058-10.637,0-14.694s10.637-4.059,14.694,0 c9.496,9.496,11.729,17.514,13.524,23.958c1.489,5.344,2.566,9.206,8.202,14.842s9.497,6.712,14.842,8.202 c6.444,1.795,14.462,4.03,23.958,13.526c9.494,9.494,11.729,17.513,13.523,23.956c1.489,5.344,2.564,9.205,8.199,14.839 c4.058,4.058,4.058,10.637,0,14.694C102.23,119.088,99.57,120.104,96.911,120.104z");
        p.setFillColor(Color.hex2RGB("#666666"));
        g.add(p);
        
        p = new SVGPath("M178.099,122.189l-49.39-49.39c-11.703,7.76-22.797,16.769-33.102,27.072 c-10.305,10.305-19.325,21.41-27.072,33.102l49.388,49.388");
        p.setFillColor(col1);
        g.add(p);

        p = new SVGPath("M89.223,106.558c-7.685,8.375-14.587,17.211-20.688,26.416l49.39,49.388l23.552-23.552 L89.223,106.558z");
        p.setFillColor(col2);
        g.add(p);

        Circle c = new Circle(212.48);
        c.setX(295.245);
        c.setY(299.511);
        c.setFillColor(col1);
        c.setStrokeColor(ColorName.BLACK);
        c.setStrokeWidth(1 / scale);
        g.add(c);
        
        p = new SVGPath("M208.46,386.296c-72.093-72.092-81.537-183.091-28.365-265.397 c-12.431,8.031-24.214,17.477-35.102,28.365c-82.98,82.98-82.98,217.519,0,300.499s217.519,82.98,300.499,0 c10.888-10.888,20.334-22.672,28.365-35.102C391.553,467.833,280.553,458.388,208.46,386.296z");
        p.setFillColor(col2);
        g.add(p);

        Ellipse e = new Ellipse(150, 75);
        e.setFillColor(Color.hex2RGB("#B3B3B3"));
        e.setRotation(Math.PI / 4);
        e.setX(400);
        e.setY(200);
        g.add(e);
                
        double sz = size / 50;
        g.setScale(scale * sz);
        g.setX(-20 * sz);
        g.setY(-20 * sz);
        
        shape.add(g);
        return shape;
    }

    @Override
    protected Item doCopy()
    {
        return new Bomb(m_radius, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        arm();
        return ExplodeAction.NONE;
    }
}
