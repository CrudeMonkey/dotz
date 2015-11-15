package com.enno.dotz.client.item;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.DragLine;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.item.Domino.Chain.Link;

public class Domino extends Item
{
    public int[] PIPS = {
        0b000000000, // 0
        0b000010000, // 1
        0b100000001, // 2
        0b100010001, // 3
        0b101000101, // 4
        0b101010101, // 5
        0b101101101, // 6            
        0b101111101, // 7            
        0b111101111, // 8            
        0b111111111, // 9
    };
    
    private IColor[] COLORS = {
            null,
            ColorName.DARKGREEN, // 1
            ColorName.BLUE,      // 2
            Color.fromColorString("#968ADE"), // 3
            Color.fromColorString("#FE8E00"), // 4
            ColorName.DEEPSKYBLUE, // LIGHTBLUE, // 5
            ColorName.LIMEGREEN, // 6
            ColorName.ORANGE,      // 7 - yellow
            ColorName.BLACK,     // 8
            ColorName.RED,       // 9
    };
    
    public int[] num = new int[2];
    public boolean vertical;
    private boolean flip;
    
    public Domino(int top, int bottom, boolean vertical)
    {
        num[0] = top;
        num[1] = bottom;
        this.vertical = vertical;
    }
    
    public Domino(int top, int bottom, int direction)
    {
        if (direction == Direction.NORTH || direction == Direction.EAST)
        {
            num[0] = top;
            num[1] = bottom;
        }
        else
        {
            num[1] = top;
            num[0] = bottom;
        }
        vertical = direction == Direction.EAST || direction == Direction.WEST;
    }
    
    public Domino()
    {
        this(3, 6, false);
    }

    public boolean isDouble()
    {
        return num[0] == num[1];
    }
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        double sz = size * 0.4;
        Rectangle r = new Rectangle(sz, sz);
        r.setStrokeColor(ColorName.BLACK);
        r.setFillColor(ColorName.BEIGE);
        r.setX(-sz/2);
        r.setY(-sz);
        g.add(r);
        
        r = new Rectangle(sz, sz);
        r.setStrokeColor(ColorName.BLACK);
        r.setFillColor(ColorName.BEIGE);
        r.setX(-sz/2);
        g.add(r);
        
        addPips(-sz/2, -sz, sz, num[0], g);
        addPips(-sz/2, 0, sz, num[1], g);
        
        setRotation(g);
        
        return g;
    }
    
    private void setRotation(IPrimitive<?> g)
    {
        double angle = vertical ? 0 : -Math.PI / 2;
        if (flip)
            angle += Math.PI;
        
        g.setRotation(angle);
    }
    
    @Override
    public void rotate()
    {
        if (vertical)
        {
            int swap = num[0];
            num[0] = num[1];
            num[1] = swap;
            flip = !flip;
        }
        vertical = !vertical;
        setRotation(shape);
    }
    
    private void addPips(double x, double y, double sz, int n, Group g)
    {
        double d = sz / 4;
        int pips = PIPS[n];
        for (int row = 2; row >= 0; row--)
        {
            for (int col = 2; col >= 0; col--)
            {
                int bit = pips % 2;
                pips = pips / 2;
                
                if (bit == 1)
                {
                    Circle c = new Circle(2);
                    c.setFillColor(COLORS[n]);
                    c.setX(d + x + col * d);
                    c.setY(d + y + row * d);
                    g.add(c);
                }
            }
        }
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
        return false;
    }

    public boolean canRotate()
    {
        return true;
    }

    protected Item doCopy()
    {
        return new Domino(num[0], num[1], vertical);
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        ctx.score.explodedDomino();
        return ExplodeAction.REMOVE; // remove domino
    }
    
    public static class Chain extends ArrayList<Link>
    {
        public static class Link
        {
            Cell cell;
            Domino d;
            Link[] connect = { null, null };
            
            public Link(Cell cell)
            {
                this.cell = cell;
                d = (Domino) cell.item;
            }

            public boolean canConnect(int i, int pip)
            {
                if (d.num[i] != pip)
                    return false;
                
                if (connect[i] == null)
                    return true;
                
                if (sameDominoChain(i) || parallelDoubleChain(i))
                    return true;
                
                return false;         
            }            
            
            // Detect a chain of duplicate dominoes, so we can flip the connections:
            //
            //    1 - 1   1               1   1 - 1
            //    2   2   4         =>    2 - 2   4
            //
            protected boolean sameDominoChain(int i)
            {
                Link prev = connect[i];
                if (prev == null)
                    return true;
                
                Domino pd = prev.d;
                if (pd.num[0] != d.num[0] || pd.num[1] != d.num[1] || pd.vertical != d.vertical)
                    return false;
                
                if (d.vertical)
                {
                    if (prev.cell.row != cell.row)
                        return false;
                }
                else
                {
                    if (prev.cell.col != cell.col)
                        return false;
                }
                return prev.sameDominoChain(1 - i);
            }
            
            // Detect a chain of duplicate dominoes, so we can flip the connections:
            //
            //      1 - 1   1            21 -  1   1 - 1
            // 21 - 1   1   4         =>       1 - 1   4
            //
            protected boolean parallelDoubleChain(int i)
            {
                if (!d.isDouble())
                    return false;
                
                Link prev = connect[i];
                if (prev == null)
                    return true;
                                
                Domino pd = prev.d;
                if (pd.isDouble() && parallel(prev))
                {
                    return prev.parallelDoubleChain(1 - i);
                }

                boolean sameDirection = prev.d.vertical == d.vertical;
                return !sameDirection;    //  previous domino has  -| connection to double                
            }

            public void connect(int i, Link link)
            {
                if (connect[i] == null)
                {
                    connect[i] = link;
                }
                else if (sameDominoChain(i))
                {
                    swapChain(i);
                    connect[i] = link;
                }
                else                    
                {
                    swapDoubleChain(i);
                    connect[i] = link;
                }
            }
            
            protected void swapDoubleChain(int i)
            {
                Link prev = connect[i];
                if (prev != null)
                {
                    if (prev.d.isDouble() && parallel(prev))
                        prev.swapDoubleChain(1 - i);
                }
                connect[i] = connect[1 - i];
                connect[1 - i] = prev;
            }
            
            protected boolean parallel(Link prev)
            {
                if (prev.d.vertical != d.vertical)
                    return false;
                
                if (d.vertical)
                    return prev.cell.row == cell.row;
                else
                    return prev.cell.col == cell.col;
            }
            
            protected void swapChain(int i)
            {
                Link prev = connect[i];
                if (prev != null)
                    prev.swapChain(1 - i);
                
                connect[i] = connect[1 - i];
                connect[1 - i] = prev;
            }
            
            public boolean tryConnect(int pos, int linkPos, Link link)
            {
                if (!canConnect(pos, link.d.num[linkPos]))
                    return false;
                
                connect(pos, link);
                link.connect[linkPos] = this;
                return true;
            }

            static final double D = 10;
            
            public Point2D getPos(Link link, GridState state)
            {
                double dx = 0, dy = 0;
                if (connect[0] != null || connect[1] != null)
                {
                    if (d.vertical)
                    {
                        if (connect[0] == link)
                            dy = -D;
                        else
                            dy = D;
                    }
                    else
                    {
                        if (connect[0] == link)
                            dx = -D;
                        else
                            dx = D;
                    }
                }
                return new Point2D(state.x(cell.col) + dx, state.y(cell.row) + dy);
            }
        }
        
        public Chain(List<Cell> cells)
        {
            for (Cell c : cells)
                connect(c);
        }        
        
        public boolean connect(Cell cell)
        {
            if (!(cell.isLocked() || cell.item instanceof Domino))
                return false;
            
            if (containsLink(cell))
                return false;
            
            Link link = new Link(cell);
            if (size() == 0)
            {
                add(link);
                return true;
            }
            
            Link prev = get(size() - 1);
            int dir = Direction.fromVector(cell.col - prev.cell.col, cell.row - prev.cell.row);
            if (!canConnect(dir, prev, link))
                return false;
            
            add(link);
            return true;
        }
        
        protected boolean canConnect(int dir, Link prev, Link link)
        {
            int pref = prev.connect[0] == null ? 0 : 1;
            int other = 1 - pref;
            switch (dir)
            {
                case Direction.EAST:
                    if (link.d.vertical)
                    {
                        if (!prev.d.vertical)
                            return prev.tryConnect(1, 0, link) || prev.tryConnect(1, 1, link); // -|
                        else
                            return prev.tryConnect(pref, pref, link) || prev.tryConnect(other, other, link); // ||
                    }
                    else // hor
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(pref, 0, link) || prev.tryConnect(other, 0, link); // |-
                        else
                            return prev.tryConnect(1, 0, link); // --
                    }
                case Direction.WEST:
                    if (link.d.vertical)
                    {
                        if (!prev.d.vertical)
                            return prev.tryConnect(0, 0, link) || prev.tryConnect(0, 1, link); // |-
                        else
                            return prev.tryConnect(pref, pref, link) || prev.tryConnect(other, other, link); // ||
                    }
                    else // hor
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(pref, 1, link) || prev.tryConnect(other, 1, link); // -|
                        else
                            return prev.tryConnect(0, 1, link); // --
                    }
                case Direction.NORTH:
                    if (link.d.vertical)
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(0, 1, link);
                        else
                            return prev.tryConnect(pref, 1, link) || prev.tryConnect(other, 1, link); // L
                    }
                    else // hor
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(0, 0, link) || prev.tryConnect(0, 1, link); // T
                        else
                            return prev.tryConnect(pref, pref, link) || prev.tryConnect(other, other, link); // =
                    }
                case Direction.SOUTH:
                    if (link.d.vertical)
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(1, 0, link);
                        else
                            return prev.tryConnect(pref, 0, link) || prev.tryConnect(other, 0, link); // T
                    }
                    else // hor
                    {
                        if (prev.d.vertical)
                            return prev.tryConnect(1, 0, link) || prev.tryConnect(1, 1, link); // L
                        else
                            return prev.tryConnect(pref, pref, link) || prev.tryConnect(other, other, link); // =
                    }
            }
            return false;
        }
        
        protected boolean containsLink(Cell cell)
        {
            for (Link link : this)
            {
                if (link.cell == cell)
                    return true;
            }
            return false;
        }

        public void updateDragLine(DragLine d, GridState state)
        {
            Point2DArray pts = new Point2DArray();
            for (int i = 0; i < size() - 1; i++)
            {
                Link prev = get(i);
                Link curr = get(i + 1);
                pts.push(prev.getPos(curr, state));
                pts.push(curr.getPos(prev, state));
            }
            pts.push(get(size() - 1).getPos(null, state));
            pts.push(new Point2D(0, 0));
            d.setPoints(pts);
        }
    }
}
