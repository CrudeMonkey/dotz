package com.enno.dotz.client.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;

public class Laser extends Item
{
    public int m_direction;
    
    private PolyLine m_beam;
    
    public Laser(int direction, boolean stuck)
    {
        m_direction = direction;
        m_stuck = stuck;
    }

    public int getDirection()
    {
        return m_direction;
    }

    public void setDirection(int direction)
    {
        m_direction = direction;
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group group = new Group();
        
        double sz = size * 0.6;
        double sz2 = sz / 2;
        double r = size * 1; // greater r means flatter
        
        MultiPath p = new MultiPath();
        p.M(0, -sz2);
        p.A(sz2, sz2, 0, 0, 0, 0, sz2);
        p.Z();
        p.setFillColor(ColorName.MEDIUMPURPLE);
        p.setStrokeColor(ColorName.DARKBLUE);
        group.add(p);
        
        p = new MultiPath();
        p.M(0, sz2);
        p.A(r, r, 0, 0, 0, 0, -sz2);
        p.A(r, r, 0, 0, 0, 0, sz2);
        p.Z();
        
        p.setFillColor(ColorName.LIGHTBLUE);
        p.setStrokeColor(ColorName.DARKBLUE);
        p.setStrokeWidth(1);
        group.add(p);
        
        updateRotation(group);
        
        if (isStuck())
        {
            Group shape = new Group();
            shape.add(createStuckShape(size));
            shape.add(group);
            return shape;
        }
        
        return group;
    }
    
    public void activateBeam(boolean active, int col, int row, Context ctx)
    {
        if (m_beam != null)
        {
            ctx.laserLayer.remove(m_beam);
            m_beam = null;
        }
        
        if (active)
        {
            GridState state = ctx.state;            
            double sz = ctx.cfg.size;
            double sz2 = sz / 2;
            
            Point2DArray p = new Point2DArray();
            p.push(state.x(col), state.y(row));
            int dx = 0, dy = 0;
            switch (m_direction)
            {
                case Direction.NORTH: dy = -1; break; //TODO use vector
                case Direction.EAST: dx = 1; break;
                case Direction.SOUTH: dy = 1; break;
                case Direction.WEST: dx = -1; break;
            }
            
            while (true)
            {
                int col2 = col + dx;
                int row2 = row + dy;
                if (!state.isValidCell(col2, row2) || state.cell(col2, row2).stopsLaser())
                {
                    // Reached the grid border
                    if (dx == 0)
                    {
                        p.push(state.x(col), state.y(row) + dy * sz2);
                    }
                    else
                    {
                        p.push(state.x(col) + dx * sz2, state.y(row));
                    }
                    break;
                }
                Cell cell = state.cell(col2,  row2);
                if (cell.item instanceof Mirror)
                {
                    boolean flipped = ((Mirror) cell.item).isFlipped();
                    int ndx = flipped ? -dy : dy;
                    int ndy = flipped ? -dx : dx;
                    p.push(state.x(col2), state.y(row2));
                    dx = ndx;
                    dy = ndy;                    
                }
                col = col2;
                row = row2;
            }
            
            m_beam = new PolyLine(p);
            m_beam.setStrokeWidth(1);
            m_beam.setStrokeColor(ColorName.RED);
            //m_beam.setAlpha(0.8);
            m_beam.setDashArray(8,8);
            
            ctx.laserLayer.add(m_beam);
        }
    }
    
    public static List<Pt> getExplodingLasers(GridState state, List<Laser> lasers)
    {
        List<Pt> list = new ArrayList<Pt>();
        
        for (int rowi = 0, nr = state.numRows; rowi < nr; rowi++)
        {
            for (int coli = 0, nc = state.numColumns; coli < nc; coli++)
            {
                int col = coli;
                int row = rowi;
                Cell cell = state.cell(col, row);
                if (cell.item instanceof Laser)
                {
                    Laser laser = (Laser) cell.item;
                    Pt loc = new Pt(col, row);
                    if (list.contains(loc))
                        continue; // already did this laser
                    
                    int direction = laser.getDirection();
                    Pt d = Direction.vector(direction);
                    
                    while (true)
                    {
                        int col2 = col + d.col;
                        int row2 = row + d.row;
                        if (!state.isValidCell(col2, row2))
                            break;
                        
                        Cell cell2 = state.cell(col2, row2);
                        if (cell2.item instanceof Laser)
                        {
                            Laser laser2 = (Laser) cell2.item;
                            Pt d2 = Direction.vector(laser2.getDirection());
                            if (d.col == -d2.col && d.row == -d2.row) // opposite direction
                            {
                                list.add(loc);
                                list.add(new Pt(col2, row2));
                                lasers.add(laser);
                                lasers.add(laser2);
                            }
                            break;
                        }
                                
                        if (state.cell(col2, row2).stopsLaser())
                            break;
                        
                        if (cell2.item instanceof Mirror)
                        {
                            boolean flipped = ((Mirror) cell2.item).isFlipped();
                            int ndx = flipped ? -d.row : d.row;
                            int ndy = flipped ? -d.col : d.col;
                            d.col = ndx;
                            d.row = ndy;                    
                        }
                        col = col2;
                        row = row2;
                    }
                }
            }
        }        
        return list;
    }
    
    @Override
    public void animate(long t, double cursorX, double cursorY)
    {
        if (m_beam == null)
            return;
        
        int n = (int) ((t / 50) % 16);
        m_beam.setDashOffset(15 - n);
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
    public boolean canSwap()
    {
        return false;
    }

    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean canRotate()
    {
        return true;
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
    
    @Override
    public boolean canDropFromBottom()
    {
        return true;
    }
    
    protected Item doCopy()
    {
        return new Laser(m_direction, m_stuck);
    }

//    @Override
//    public void dropFromBottom()
//    {
//        ctx.score.droppedAnchor();
//    }
//
    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        ctx.score.explodedLaser();
        return ExplodeAction.REMOVE; // remove mirror
    }

    private static Random s_rnd = new Random();
    private static IColor[] COLORS = { ColorName.YELLOW, ColorName.AZURE, ColorName.RED, ColorName.DARKMAGENTA };
    
    public void overload(long t)
    {
        m_beam.setStrokeColor(COLORS[s_rnd.nextInt(COLORS.length)]);
        m_beam.setStrokeWidth(1 + s_rnd.nextInt(5));
    }
}
