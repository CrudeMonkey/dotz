package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.event.NodeMouseDownEvent;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveEvent;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutEvent;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpEvent;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.util.Debug;

public class DragConnectMode extends ConnectMode
{
    protected Integer m_color;
    protected DragLine m_dragLine = new DragLine();
    
    protected List<Cell> m_cells = new ArrayList<Cell>();
    protected int m_lastCellCol, m_lastCellRow;
    protected boolean m_isSquare = false;
    protected boolean m_isKnightMode;
    protected boolean m_isEggMode;
    protected boolean m_isWordMode;
    protected boolean m_isDominoMode;
    
    private boolean m_dragging;
    private double m_margin;
    
    public DragConnectMode(Context ctx, Layer layer)
    {
        super(ctx, layer);
        
        m_margin = ctx.cfg.size * 0.35; // for letter mode
        
        m_isWordMode = ctx.generator.generateLetters;
        
        //TODO replace with GWT handlers! - don't need Node detection here!
        m_lineMoveHandler = new NodeMouseMoveHandler() {

            @Override
            public void onNodeMouseMove(NodeMouseMoveEvent event)
            {
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                
                if (m_isWordMode)
                {
                    // Must be relatively near the center so you can drag diagonal lines
                    double dx = Math.abs(m_state.x(col) - event.getX());
                    double dy = Math.abs(m_state.y(row) - event.getY());
                    if (dx < m_margin && dy < m_margin)
                        updateDragLine(col, row);
                }
                else
                    updateDragLine(col, row);
                
                m_dragLine.adjust(event.getX(), event.getY());
                redraw();
            }
        };
        
        m_mouseUpHandler = new NodeMouseUpHandler()
        {                
            @Override
            public void onNodeMouseUp(NodeMouseUpEvent event)
            {                 
                endOfDrag();
                connectDone();
            }
        };
        
        m_mouseDownHandler = new NodeMouseDownHandler()
        {
            @Override
            public void onNodeMouseDown(NodeMouseDownEvent event)
            {
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                if (row < 0 || row >= cfg.numRows || col < 0 || col >= cfg.numColumns)
                    return;
                
                // clear variables
                Debug.p("square=false");
                m_isSquare = false;
                m_color = null;
                m_cells.clear();
                m_dragLine.clear();
                
                //Debug.p("mouse down in cell " + col + "," + row);
                
                Cell cell = m_state.cell(col, row);
                if (flipMirror(cell) || fireRocket(cell) || reshuffle(cell))
                    return;
                
                //TODO simplify
                if (!cell.canConnect(null, m_isWordMode) && !(!cell.isLocked() && (cell.item instanceof Knight || cell.item instanceof Egg)))
                    return;
                
                //Debug.p("dragging=true");
                m_dragging = true;
                
                m_isKnightMode = cell.item instanceof Knight;
                m_isEggMode = cell.item instanceof Egg;
                m_isDominoMode = cell.item instanceof Domino;
                
                m_layer.add(m_dragLine);
                
                pushCell(cell, col, row);
                
                double x = m_state.x(col);
                double y = m_state.y(row);
                m_dragLine.add(x, y);
                m_dragLine.add(event.getX(), event.getY());
                
                redraw();
                
                m_lineMoveReg = m_layer.addNodeMouseMoveHandler(m_lineMoveHandler);
                m_mouseUpReg = m_layer.addNodeMouseUpHandler(m_mouseUpHandler);
            }                
        };
        
        m_mouseOutHandler = new NodeMouseOutHandler() {
            @Override
            public void onNodeMouseOut(NodeMouseOutEvent event)
            {
                if (m_dragging)
                {
                    Sound.MISS.play();
                    endOfDrag();
                }
            }
        };
    }
    
    protected void endOfDrag()
    {
        //Debug.p("dragging=false");

        m_dragging = false;
        m_lineMoveReg.removeHandler();
        m_mouseUpReg.removeHandler();
        
        m_layer.remove(m_dragLine);
        
        redraw();            
    }
    
    protected void connectDone()
    {
        String word = null;
        if (m_isWordMode)
        {
            if (m_cells.size() < 2)
                return;
            
            if (m_isKnightMode && m_cells.size() < 3)
                return;
            
            // When first item is an Animal, only wild cards and dots of the same color can be connected
            // and there must be at least 5
            if (hasAnimals() && m_cells.size() < 5)
            {
                Sound.MISS.play();
                return;
            }
            
            StringBuilder b = new StringBuilder();
            for (Cell c : m_cells)
            {
                if (c.item instanceof Dot)
                    b.append(((Dot) c.item).letter);
                else if (c.item instanceof DotBomb)
                    b.append(((DotBomb) c.item).getDot().letter);
                else
                    b.append('?'); // wild card or animal
            }
            word = WordList.getWord(b.toString());
            if (word == null)
            {
                Sound.MISS.play();
                return;
            }
        }
        else
        {
            if (m_isEggMode)
            {
                if (m_cells.size() < 3)
                    return;
            }
            else if (m_isDominoMode)
            {
                if (m_cells.size() < 2)
                    return;
            }
            else
            {
                if (m_cells.size() < 2 || ctx.isWild(m_color))
                    return;
            
                if (m_isKnightMode && m_cells.size() < 3)
                    return;
            }
        }
        
        stop(); // stop listening to user input
        
        Cell lastCell = m_cells.get(m_cells.size() - 1);
        ctx.lastMove = new Pt(lastCell.col, lastCell.row);
        
        m_state.processChain(m_cells, m_isSquare, m_isKnightMode, m_isWordMode, m_isEggMode, word, m_color, new Runnable() {
            public void run()
            {
                start(); // next move
            }
        });
    }

    protected void updateDragLine(int col, int row)
    {
        if (col < 0 || col >= cfg.numColumns || row < 0 || row > cfg.numRows)
            return;
                            
        if (col == m_lastCellCol && row == m_lastCellRow)
            return; // still same cell
        
        // p("drag " + col + "," + row);
        
        if (previousCell(col, row))
        {
            Debug.p("square=false");
            m_isSquare = false;
            m_dragLine.pop();
            popCell();
            
            if (m_isDominoMode)
            {
                Domino.Chain ch = new Domino.Chain(m_cells);            
                ch.updateDragLine(m_dragLine, m_state);
            }
            
            m_dragLine.adjust(m_state.x(col), m_state.y(row));
            return;
        }
        
        if (m_isSquare)
            return; // can't connect anymore cells
        
        if (m_isKnightMode)
        {
            int dcol = Math.abs(col - m_lastCellCol);
            int drow = Math.abs(row - m_lastCellRow);
            if (dcol == 1 && drow == 2 || dcol == 2 && drow == 1)
            {
                Cell neighbor = m_state.cell(col,  row);
                if (!neighbor.canConnect(m_color, m_isWordMode))
                    return;
                
                if (m_isWordMode && invalidAnimalChain(neighbor))
                    return;                    
                
                if (!didCell(col, row))
                {
                    // push cell
                    m_dragLine.adjust(m_state.x(col), m_state.y(row));
                    pushCell(neighbor, col, row);
                    m_dragLine.add(0, 0);
                }
            }
        }
        else if (m_isWordMode)
        {
            int dcol = Math.abs(col - m_lastCellCol);
            int drow = Math.abs(row - m_lastCellRow);
            if (dcol < 2 && drow < 2)
            {
                Cell neighbor = m_state.cell(col, row);
                if (!neighbor.canConnect(m_color, m_isWordMode))
                    return;
                
                // When first item is an Animal, only wild cards and dots of the same color can be connected
                // (and there must be at least 5)
                if (invalidAnimalChain(neighbor))
                {
                    return;
                }
                
                if (didCell(col, row))
                {
                    if (hasDiagonal() || isDiagonal(m_lastCellCol, m_lastCellRow, col, row))
                        return;
                    
                    // made a square
                    m_isSquare = true;
                }
                
                // push cell
                m_dragLine.adjust(m_state.x(col), m_state.y(row));
                pushCell(neighbor, col, row);
                m_dragLine.add(0, 0);
            }
        }
        else
        {        
            if ((col == m_lastCellCol && (row == m_lastCellRow + 1 || row == m_lastCellRow - 1))
             || (row == m_lastCellRow && (col == m_lastCellCol + 1 || col == m_lastCellCol - 1)))
            {
                // neighbor cell (horizontal/vertical)
                Cell neighbor = m_state.cell(col,  row);
                
                if (m_isDominoMode)
                {
                    if (didCell(col, row) || neighbor.isLocked() || !(neighbor.item instanceof Domino))
                        return;
                    
                    Domino.Chain ch = new Domino.Chain(m_cells);
                    if (!ch.connect(neighbor))
                        return;
                    
                    ch.updateDragLine(m_dragLine, m_state);
                    pushCell(neighbor, col, row);
                    return;
                    //TODO didCell
                }
                else if (m_isEggMode)
                {
                    if (didCell(col, row) || neighbor.isLocked() || !(neighbor.item instanceof Egg || neighbor.item instanceof Wild))
                        return;
                    
                    Boolean cracked = isCracked();
                    if (neighbor.item instanceof Egg)
                    {
                        Egg egg = (Egg) neighbor.item;
                        if (cracked != null && cracked != egg.isCracked())
                            return;
                    }
                }
                else
                {
                    // If all cells are Wild and it's an Egg, switch to Egg mode
                    if (neighbor.item instanceof Egg)
                    {
                        for (Cell c : m_cells)
                        {
                            if (!(c.item instanceof Wild))
                                return;
                        }
                        m_isEggMode = true;
                    }
                    else
                    {
                        if (!neighbor.canConnect(m_color, m_isWordMode))
                            return;
                        
                        if (didCell(col, row))
                        {
                            if (ctx.isWild(m_color)) // can't make a square of all wildcards
                                return;
                            
                            // made a square
                            m_isSquare = true;
                            Debug.p("square=true");
                        }
                    }
                }
                
                // push cell
                m_dragLine.adjust(m_state.x(col), m_state.y(row));
                pushCell(neighbor, col, row);
                m_dragLine.add(0, 0);
            }
        }
    }
    
    protected Boolean isCracked()
    {
        for (Cell cell : m_cells)
        {
            if (cell.item instanceof Egg)
                return ((Egg) cell.item).isCracked();
        }
        return null;
    }
    
    protected boolean hasAnimals()
    {
        for (Cell cell : m_cells)
        {
            if (cell.item instanceof Animal)
                return true;
        }
        return false;
    }
    
    protected boolean invalidAnimalChain(Cell c)
    {
        // Animals can only be added if the other items are wild (Wild or Knight)
        // or dots of the same color.
        if (c.item instanceof Animal)
        {
            int animalColor = c.item.getColor();
            for (Cell cell : m_cells)
            {                
                int itemColor = cell.item.getColor();
                if (itemColor != Config.WILD_ID && itemColor != animalColor)
                    return true;
            }
        }
        return false;
    }
    
    protected boolean hasDiagonal()
    {
        int n = m_cells.size();
        for (int i = 1; i < n; i++)
        {
            Cell a = m_cells.get(i-1);
            Cell b = m_cells.get(i);
            if (isDiagonal(a.col, a.row, b.col, b.row))
                return true;
        }
        return false;
    }
    
    protected boolean isDiagonal(int cola, int rowa, int colb, int rowb)
    {
        return cola != colb && rowa != rowb;
    }

    protected void pushCell(Cell cell, int col, int row)
    {
        // p("push " + col + "," + row);
        
        if (m_isSquare)
            Sound.MADE_SQUARE.play();
        else
            Sound.CLICK.play();
        
        m_cells.add(cell);
        
        // dumpChain();
        
        m_lastCellCol = col;
        m_lastCellRow = row;
        
        if (m_isKnightMode)
        {
            Integer newColor = m_cells.size() == 1 ? Config.WILD_ID : cell.item.getColor(); //TODO KNIGHT_ID
            if (m_cells.size() <= 2 || !ctx.isWild(newColor) && ctx.isWild(m_color))
            {
                //TODO connect more knights (isWild)
                m_color = newColor;
                m_dragLine.setStrokeColor(cfg.connectColor(m_color));
            }
        }
        else if (m_isEggMode || m_isDominoMode)
        {
            m_color = Config.WILD_ID;
            m_dragLine.setStrokeColor(cfg.connectColor(m_color));
        }
        else
        {
            Integer newColor = cell.item.getColor();
            if (m_cells.size() == 1 || !ctx.isWild(newColor) && ctx.isWild(m_color))
            {
                m_color = newColor;
                m_dragLine.setStrokeColor(cfg.connectColor(m_color));
            }
        }
    }
    
    protected void dumpChain()
    {
        StringBuilder b = new StringBuilder();
        for (Cell c : m_cells)
        {
            b.append(c.col).append(",").append(c.row).append(" ");
        }
        Debug.p(b.toString());
    }
    
    protected void popCell()
    {
        Sound.CLICK.play();
        
        int n = m_cells.size();
        m_cells.remove(--n);
        
        Cell prevCell = m_cells.get(n - 1);
        m_lastCellCol = prevCell.col;
        m_lastCellRow = prevCell.row;
        
        if (m_isEggMode)
        {
            // If only wild cards left, then switch to regular mode
            boolean allWild = true;
            for (Cell cell : m_cells)
            {
                if (!(cell.item instanceof Wild))
                {
                    allWild = false;
                    break;
                }
            }
            if (allWild)
            {
                m_isEggMode = false;
                m_color = Config.WILD_ID;
            }
        }
        else if (m_isDominoMode)
        {
            m_color = Config.WILD_ID;
        }
        else
        {
            Integer newColor = Config.WILD_ID;
            for (int i = m_isKnightMode ? 1 : 0; i < n; i++)
            {
                newColor = m_cells.get(i).item.getColor();                
                if (!ctx.isWild(newColor))
                    break;
            }
            m_color = newColor;
        }
        
        m_dragLine.setStrokeColor(cfg.connectColor(m_color));
    }
    
    protected boolean previousCell(int col, int row)
    {
        int n = m_cells.size();
        if (n < 2)
            return false;
        
        Cell prevCell = m_cells.get(n - 2);
        return prevCell.col == col && prevCell.row == row;
    }
    
    protected boolean didCell(int col, int row)
    {
        for (Cell c : m_cells)
        {
            if (c.col == col && c.row == row)
                return true;
        }
        return false;
    }
}