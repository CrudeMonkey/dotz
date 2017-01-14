package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Polygon;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.util.Debug;

public class BorderFinder
{
    static final int RIGHT = 0;
    static final int LEFT = 1;
    static final int UP = 2;
    static final int DOWN = 3;
    
    static final int REGULAR = 0;
    static final int HOLE = 1;
    static final int ROCK = 2;
    
    GridState state;
    
    List<Border> m_borders = new ArrayList<Border>();
    private boolean[][] m_marks;
    
    public BorderFinder(GridState state)
    {
        this.state = state;
        
        m_marks = new boolean[state.numColumns][state.numRows];
    }
    
    public void find(Layer layer)
    {
        // Exclude holes on the outside
        for (int col = 0; col < state.numColumns; col++)
        {
            addCells(col, 0, HOLE);
            addCells(col, state.numRows - 1, HOLE);
        }
        
        for (int row = 0; row < state.numRows; row++)
        {
            addCells(0, row, HOLE);
            addCells(state.numColumns - 1, row, HOLE);
        }
        
        //dumpMarked();
        
        for (int col = 0; col < state.numColumns; col++)
        {
            for (int row = 0; row < state.numRows; row++)
            {
                if (markedCell(col, row))
                    continue;
                
                if (isType(col, row, REGULAR))
                {
                    trace(col, row, REGULAR);
                }
                else if (isType(col, row, ROCK))
                {
                    trace(col, row, ROCK);
                }
                else
                    trace(col, row, HOLE);
            }
        }
        
        layer.removeAll();
        for (Border b : m_borders)
        {
            Polygon poly = b.getPolygon();            
            layer.add(poly);
        }
    }

    private void trace(int col, int row, int type)
    {
        addCells(col, row, type);

        Border b = new Border(type);
        
        m_borders.add(b);
        
        b.addPoint(col, row + 1);
        b.addPoint(col, row);
        b.addPoint(col + 1, row);
        
        int start_x = col;
        int start_y = row + 1;
        
        // going right
        int x = col + 1;
        int y = row;
        
        int dir = RIGHT;
        
        while (x != start_x || y != start_y)
        {
            switch (dir)
            {
                case RIGHT:
                    if (isType(x, y, type))
                    {
                        if (isType(x, y - 1, type))
                        {
                            dir = UP;              
                            y--;
                        }
                        else    // keep right
                        {
                            x++;
                        }
                    }
                    else
                    {
                        // go down
                        dir = DOWN;
                        y++;
                    }
                    break;
                case UP:
                    if (isType(x, y - 1, type))
                    {
                        if (isType(x - 1, y - 1, type))
                        {
                            dir = LEFT;
                            x--;
                        }
                        else    // keep up
                        {
                            y--;
                        }
                    }
                    else
                    {
                        dir = RIGHT;
                        x++;
                    }
                    break;
                case LEFT:
                    if (isType(x - 1, y - 1, type))
                    {
                        if (isType(x - 1, y, type))
                        {
                            dir = DOWN;
                            y++;
                        }
                        else    // keep left
                        {
                            x--;
                        }
                    }
                    else
                    {
                        dir = UP;
                        y--;
                    }
                    break;
                case DOWN:
                    if (isType(x - 1, y, type))
                    {
                        if (isType(x, y, type))
                        {
                            dir = RIGHT;
                        }
                        else    // keep down
                        {
                            y++;
                        }
                    }
                    else
                    {
                        dir = LEFT;
                        x--;
                    }
                    break;
            }
            b.addPoint(x, y);
        }
    }
    
    private boolean isType(int col, int row, int type)
    {
        if (!state.isValidCell(col, row))
            return false;
        
        Cell cell = state.cell(col, row);
        if (type == HOLE)
            return cell instanceof Hole;
        if (type == ROCK)
            return cell instanceof Rock;
        
        return !(cell instanceof Hole || cell instanceof Rock);
    }
    
    private void addCells(int col, int row, int type)
    {
        if (!state.isValidCell(col, row) || markedCell(col, row) || !isType(col, row, type))
            return;
        
        markCell(col, row);
        
        addCells(col - 1, row, type);
        addCells(col + 1, row, type);
        addCells(col, row + 1, type);
        addCells(col, row - 1, type);
    }

    protected boolean markedCell(int col, int row)
    {
        return m_marks[col][row];
    }
    
    protected void markCell(int col, int row)
    {
        m_marks[col][row] = true;
    }
    
    protected void dumpMarked()
    {
        StringBuilder b = new StringBuilder();
        
        for (int col = 0; col < state.numColumns; col++)
        {
            for (int row = 0; row < state.numRows; row++)
            {
                if (markedCell(col, row))
                    b.append("X");
                else
                    b.append(".");
            }
            b.append("\n");
        }
        Debug.p("\n" + b);
    }
    
    public class Border
    {
        private int m_type;
        private Point2DArray m_points = new Point2DArray();
        
        public Border(int type)
        {
            m_type = type;
        }
        
        public void addPoint(int col, int row)
        {
            m_points.push(state.x(col), state.y(row));
        }

        public Polygon getPolygon()
        {
            Polygon poly = new Polygon(m_points);
            poly.setStrokeColor(ColorName.DEEPSKYBLUE); //FIREBRICK); //BLUE);
            poly.setCornerRadius(5);
            poly.setStrokeWidth(3);
            poly.setX(-state.size() / 2);
            poly.setY(-state.size() / 2);
            
            return poly;
        }
    }
}
