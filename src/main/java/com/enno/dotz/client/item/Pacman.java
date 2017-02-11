package com.enno.dotz.client.item;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.util.ColorUtil;
import com.enno.dotz.client.util.Console;

public class Pacman extends Item
{
    private static final double MAX_ANGLE = Math.PI * 0.25;
    
    private int m_direction = Direction.EAST;

    private Slice m_slice;
    
    public Pacman()
    {
        this(Direction.EAST, false);
    }
    
    public Pacman(int direction, boolean stuck)
    {
        m_direction = direction;
        m_stuck = stuck;
    }

    @Override
    public String getType()
    {
        return "pacman";
    }
    
    public int getDirection()
    {
        return m_direction;
    }

    public void setDirection(int direction)
    {
        m_direction = direction;
        
        if (shape != null)
            updateRotation(shape);
    }
    
    @Override
    public boolean canRotate()
    {
        return true;
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
    public boolean stopsLaser()
    {
        return true;
    }
        
    @Override
    public void rotate(int n)
    {
        for (int i = 0; i < n; i++)
            m_direction = Direction.rotate(m_direction, true);
        updateRotation(shape);
    }
    
    protected void updateRotation(IPrimitive<?> shape)
    {
        switch (m_direction)
        {
            case Direction.EAST: shape.setRotation(0); break;
            case Direction.SOUTH: shape.setRotation(Math.PI / 2); break;
            case Direction.WEST: shape.setRotation(Math.PI); break;
            case Direction.NORTH: shape.setRotation(-Math.PI / 2); break;
        }
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        double r = size * 0.25;
        
        Group g = new Group();
        
        // facing right
        m_slice = new Slice(r, MAX_ANGLE, -MAX_ANGLE, false);
        m_slice.setFillColor(ColorName.YELLOW);
        m_slice.setStrokeColor(ColorUtil.darker(ColorName.YELLOW, 0.5));
        g.add(m_slice);
        
        updateRotation(g);
        
        return g;
    }

    private static final double WALK_TIME = 1.0 / 8;
    
    public void animateWalk(double pct)
    {
        int n = (int) (pct / WALK_TIME);
        double rest = pct / WALK_TIME - n;
        
        double angle;
        if (n % 2 == 0)
        {
            angle = MAX_ANGLE * (1 - rest);
        }
        else
            angle = MAX_ANGLE * rest;

        m_slice.setStartAngle(angle);
        m_slice.setEndAngle(-angle);
    }
    
    @Override
    protected Item doCopy()
    {
        return new Pacman(m_direction, m_stuck);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        // TODO Auto-generated method stub
        return ExplodeAction.NONE;
    }
}
