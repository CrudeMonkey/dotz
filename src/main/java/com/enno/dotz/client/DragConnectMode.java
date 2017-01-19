package com.enno.dotz.client;

import java.util.HashSet;
import java.util.Set;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.event.NodeMouseDownEvent;
import com.ait.lienzo.client.core.event.NodeMouseDownHandler;
import com.ait.lienzo.client.core.event.NodeMouseMoveEvent;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutEvent;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.event.NodeMouseUpEvent;
import com.ait.lienzo.client.core.event.NodeMouseUpHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell.Unlockable;
import com.enno.dotz.client.SoundManager.Sound;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Egg;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.Wild;
import com.enno.dotz.client.item.WrappedDot;
import com.google.gwt.event.shared.HandlerRegistration;

public class DragConnectMode extends ConnectMode
{
    protected Integer m_color;
    protected DragLine m_dragLine = new DragLine();
    
    protected CellList m_cells = new CellList();
    protected int m_lastCellCol, m_lastCellRow;
    protected boolean m_isSquare = false;
    protected boolean m_isKnightMode;
    protected boolean m_isEggMode;
    protected boolean m_isWordMode;
    protected boolean m_isDiagonalMode;
    protected boolean m_isDominoMode;
    protected int m_minChainLength;
    
    private boolean m_dragging;
    private double m_margin;
    
    public DragConnectMode(Context ctx, Layer layer)
    {
        super(ctx, layer);
        
        m_margin = ctx.cfg.size * 0.35; // for letter mode
        
        m_isWordMode = ctx.generator.generateLetters;
        m_isDiagonalMode = ctx.generator.diagonalMode;
        m_minChainLength = ctx.generator.minChainLength;
        
        //TODO replace with GWT handlers! - don't need Node detection here!
        m_lineMoveHandler = new NodeMouseMoveHandler() {

            @Override
            public void onNodeMouseMove(NodeMouseMoveEvent event)
            {
                int col = m_state.col(event.getX());
                int row = m_state.row(event.getY());
                                
                if (m_isWordMode || m_isDiagonalMode)
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

                Cell cell = m_state.cell(col, row);

                if (m_specialMode != null)
                {
                    m_specialMode.click(cell);
                    return;
                }
                
                // clear variables
                //Debug.p("square=false");
                m_isSquare = false;
                m_color = null;
                m_cells.clear();
                m_dragLine.clear();
                
                //Debug.p("mouse down in cell " + col + "," + row);
                
                if (isTriggeredBySingleClick(cell))
                    return;
                
                if (startSpecialMode(cell, event.getX(), event.getY()))
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
                if (m_specialMode != null)
                {
                    m_specialMode.mouseOut();
                    return;
                }
                
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
        int wordPoints = 0;
        if (m_isWordMode)
        {
            if (m_cells.size() < m_minChainLength)
                return;
            
            if (m_isKnightMode && m_cells.size() < 3)
                return;
            
            // When first item is an Animal, there must be at least 5
            if (m_cells.hasAnimals() && !(m_cells.size() >= 5 || m_cells.isOneColor() && m_cells.size() >= 4))
            {
                Sound.MISS.play();
                return;
            }
            
            int wordMultiplier = 1;
            StringBuilder b = new StringBuilder();
            for (Cell c : m_cells)
            {
                Dot dot = null;
                if (c.item instanceof Dot)
                    dot = (Dot) c.item;
                else if (c.item instanceof DotBomb)
                    dot = ((DotBomb) c.item).getDot();
                
                if (dot == null)               
                {
                    b.append('?'); // wild card or animal
                }
                else
                {
                    LetterMultiplier mult = dot.getLetterMultiplier();
                    if (mult == null)
                    {
                        wordPoints += dot.getLetterPoints();
                    }
                    else
                    {
                        if (mult.isWordMultiplier())
                        {
                            wordPoints += dot.getLetterPoints();
                            wordMultiplier *= mult.getMultiplier();
                        }
                        else // letter multiplier
                        {
                            wordPoints += dot.getLetterPoints() * mult.getMultiplier();                            
                        }
                    }
                    b.append(dot.getLetter());
                }
            }
            wordPoints *= wordMultiplier;
            word = WordList.getWord(b.toString(), ctx.guessedWords);
            
//            if (ctx.generator.findWords)
//            {
//                ctx.findWordList.foundWord(word);
////                if (!ctx.findWordList.foundWord(word))
////                    word = null;
//            }
            
            if (word == null)
            {
                Sound.MISS.play();
                return;
            }
            
            if (!ctx.generator.removeLetters)   // don't allow duplicate words
                ctx.guessedWords.add(word);
            
            ctx.bestWords.addWord(word, wordPoints);
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
                if (m_cells.size() < m_minChainLength)
                    return;
            }
            else
            {
                if (m_cells.size() < m_minChainLength || ctx.isWild(m_color))
                    return;
            
                if (m_isKnightMode && m_cells.size() < 3)
                    return;
            }
        }
        
        stop(); // stop listening to user input
        
        Cell lastCell = m_cells.get(m_cells.size() - 1);
        ctx.lastMove = new Pt(lastCell.col, lastCell.row);
        
        m_state.processChain(new UserAction(m_cells, m_isSquare, m_isKnightMode, m_isWordMode, m_isEggMode, word, wordPoints, m_color), new Runnable() {
            public void run()
            {
                start(); // next move
            }
        });
    }

    protected void updateDragLine(int col, int row)
    {
        if (col < 0 || col >= cfg.numColumns || row < 0 || row >= cfg.numRows)
            return;
                            
        if (col == m_lastCellCol && row == m_lastCellRow)
            return; // still same cell
        
        // p("drag " + col + "," + row);
        
        if (m_cells.previousCell(col, row))
        {
            //Debug.p("square=false");
            m_isSquare = false;
            m_dragLine.pop();
            popCell();
            
            if (m_isDominoMode)
            {
                if (m_cells.isOnlyWildCards())
                {
                    m_isDominoMode = false;
                    m_dragLine.clear();
                    for (Cell cell : m_cells)
                    {
                        m_dragLine.add(m_state.x(cell.col), m_state.y(cell.row));
                    }
                }
                else
                {
                    Domino.Chain ch = new Domino.Chain(m_cells);            
                    ch.updateDragLine(m_dragLine, m_state);
                }
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
                Cell neighbor = m_state.cell(col, row);
                if (!neighbor.canConnect(m_color, m_isWordMode))
                    return;
                
                if (m_isWordMode && invalidAnimalChain(neighbor))
                    return;                    
                
                if (!m_cells.didCell(col, row))
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
                
                if (m_cells.didCell(col, row))
                {
                    if (m_cells.hasDiagonal() || m_cells.isDiagonal(m_lastCellCol, m_lastCellRow, col, row))
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
             || (row == m_lastCellRow && (col == m_lastCellCol + 1 || col == m_lastCellCol - 1)) 
             || m_isDiagonalMode && isDiagonal(row, col))
            {
                // neighbor cell (horizontal/vertical)
                Cell neighbor = m_state.cell(col, row);
                
                if (!m_isEggMode && !m_isDominoMode && !m_isKnightMode && neighbor.item instanceof Domino && m_cells.isOnlyWildCards())
                    m_isDominoMode = true;
                
                if (m_isDominoMode)
                {
                    if (m_cells.didCell(col, row) || neighbor.isLocked() || 
                            !(neighbor.item instanceof Domino || neighbor.item instanceof Animal || neighbor.item instanceof Wild))
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
                    if (m_cells.didCell(col, row) || neighbor.isLocked() || !(neighbor.item instanceof Egg || neighbor.item instanceof Wild))
                        return;
                    
                    Boolean cracked = m_cells.isCracked();
                    if (neighbor.item instanceof Egg)
                    {
                        Egg egg = (Egg) neighbor.item;
                        if (cracked != null && cracked != egg.isCracked())
                            return;
                    }
                }
                else
                {
                    // Can't make squares in diagonal mode
                    if (m_isDiagonalMode && m_cells.didCell(col, row))
                        return;
                    
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
                        
                        if (neighbor.isLockedCage() && m_state.cell(m_lastCellCol, m_lastCellRow).isLockedCage())
                            return; // make sure we're not connecting 2 locked cages
                        
                        if (m_cells.didCell(col, row))
                        {
                            if (ctx.isWild(m_color)) // can't make a square of all wildcards
                                return;
                            
                            // made a square
                            m_isSquare = true;
                            //Debug.p("square=true");
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
    
    protected boolean isDiagonal(int row, int col)
    {
        int dcol = Math.abs(col - m_lastCellCol);
        int drow = Math.abs(row - m_lastCellRow);
        return (dcol < 2 && drow < 2);
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

    public static abstract class SpecialMode
    {
        protected Cell m_sourceCell;
        protected Item m_item;
        protected Cell m_targetCell;
        private Rectangle m_box;
        protected DragLine m_dragLine;
        private Runnable m_removeItem;
        
        protected Context ctx;
        protected GridState m_state;
        protected Layer m_layer;
        protected int m_lastCellCol = -1;
        protected int m_lastCellRow = -1;
        protected ConnectMode m_connectMode;
        
        protected HandlerRegistration m_lineMoveReg;
        protected HandlerRegistration m_mouseUpReg;
        
        protected SpecialMode(Cell cell, Context ctx, ConnectMode connectMode)
        {
            this.ctx = ctx;
            m_state = ctx.state;
            m_sourceCell = cell;
            m_item = m_sourceCell.item;
            m_layer = ctx.connectLayer;
            m_connectMode = connectMode;
        }
        
        protected SpecialMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            this.ctx = ctx;
            m_state = ctx.state;
            m_item = item;
            m_layer = ctx.connectLayer;
            m_connectMode = connectMode;
            m_removeItem = removeItem;
        }
        
        public void update(int row, int col)
        {
            if (!m_state.isValidCell(col, row))
            {
                m_targetCell = null;
                return;
            }
            
            if (col == m_lastCellCol && row == m_lastCellRow)
                return; // still same cell
            
            m_lastCellCol = col;
            m_lastCellRow = row;
            
            m_targetCell = m_state.cell(col, row);
            update(m_targetCell);
        }

        protected abstract void update(Cell cell);        
        
        public void cancel()
        {
            if (m_lineMoveReg != null)
                m_lineMoveReg.removeHandler();
            
            if (m_mouseUpReg != null)
                m_mouseUpReg.removeHandler();
            
            if (m_dragLine != null)
                m_layer.remove(m_dragLine);
            
            removeDragBox();
            redraw();
            
            m_connectMode.cancelSpecialMode();
        }

        public void done()
        {
            if (m_targetCell == null)
                Sound.MISS.play();
            else
                done(m_targetCell, true);
        }
        
        public abstract void done(Cell cell, boolean alwaysCancel);
        
        protected void startMoveListener()
        {
            m_lineMoveReg = m_layer.addNodeMouseMoveHandler(new NodeMouseMoveHandler() {

                @Override
                public void onNodeMouseMove(NodeMouseMoveEvent event)
                {
                    int col = m_state.col(event.getX());
                    int row = m_state.row(event.getY());
                    
                    update(row, col);
                }
            });
        }
        
        protected void startMouseUpListener()
        {
            m_mouseUpReg = m_layer.addNodeMouseUpHandler(new NodeMouseUpHandler()
            {                
                @Override
                public void onNodeMouseUp(NodeMouseUpEvent event)
                {
                    cancel();
                    done();
                }
            });
        }
        
        public void mouseOut()
        {
            if (m_mouseUpReg != null)
            {
                cancel();
                Sound.MISS.play();
            }
        }
        
        protected void startDragLine(Cell cell, int x, int y)
        {
            m_dragLine = new DragLine();
            m_dragLine.clear();            
            m_dragLine.setStrokeColor(ColorName.BLACK);
            m_dragLine.add(m_state.x(cell.col), m_state.y(cell.row));
            m_dragLine.add(x, y);
            
            m_layer.add(m_dragLine);
            redraw();
        }

        protected void createDragBox(Cell cell, int n)
        {
            double w = n * ctx.cfg.size;
            double sw = 3;
            m_box = new Rectangle(w - sw, w - sw);
            if (cell != null)
            {
                m_box.setX(m_state.x(cell.col) - w / 2 - sw / 2);
                m_box.setY(m_state.y(cell.row) - w / 2 - sw / 2);
            }
            else
            {
                m_box.setVisible(false);
            }
            m_box.setStrokeColor(ColorName.BLACK);
            m_box.setStrokeWidth(sw);
            m_box.setDashArray(4, 4);
            
            ctx.nukeLayer.setVisible(true);
            ctx.nukeLayer.add(m_box);
        }

        protected void updateDragLine(Cell cell)
        {
            if (m_dragLine != null)
            {
                m_dragLine.adjust(m_state.x(cell.col), m_state.y(cell.row));
                redraw();
            }
        }
        
        protected void removeDragBox()
        {
            if (m_box != null)
            {
                ctx.nukeLayer.remove(m_box);
                LayerRedrawManager.get().schedule(ctx.nukeLayer);
                ctx.nukeLayer.setVisible(false);
                m_box = null;
            }
        }
        
        protected void updateDragBox(Cell cell)
        {
            double w = m_box.getWidth();
            m_box.setX(m_state.x(cell.col) - w / 2);
            m_box.setY(m_state.y(cell.row) - w / 2);
            m_box.setVisible(true);
            LayerRedrawManager.get().schedule(ctx.nukeLayer);
        }
        
        protected void removeStartItem()
        {
            if (m_sourceCell != null)
            {
                m_sourceCell.item.removeShapeFromLayer(ctx.dotLayer);
                m_sourceCell.item = null;
            }
            if (m_removeItem != null)
                m_removeItem.run();
        }
        
        protected void process()
        {
            m_connectMode.stop();
            
            m_state.processChain(new Runnable() {
                public void run()
                {
                    m_connectMode.start(); // next move
                }
            });
        }
        
        protected void redraw()
        {
            LayerRedrawManager.get().schedule(m_layer);
        }
        
        public void click(Cell cell)
        {
            done(cell, false);
        }
    }
    
    public static class TurnMode extends SpecialMode
    {
        public TurnMode(Cell cell, int x, int y, Context ctx, ConnectMode connectMode)
        {
            super(cell, ctx, connectMode);
            
            startMoveListener();
            startMouseUpListener();
            
            startDragLine(cell, x, y);            
            createDragBox(cell, 1);
            update(cell);
        }        

        public TurnMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            if (cell.isLocked() || cell.item == null || !cell.item.canRotate())
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            cancel();
            
            Sound.FLIP_MIRROR.play();
            int n = ((Turner) m_item).n;
            cell.item.rotate(n);
            
            removeStartItem();
            process();
        }
    }
    
    public static class ColorBombMode extends SpecialMode
    {
        public ColorBombMode(Cell cell, int x, int y, Context ctx, ConnectMode connectMode)
        {
            super(cell, ctx, connectMode);
            
            startMoveListener();
            startMouseUpListener();
            
            startDragLine(cell, x, y);            
            createDragBox(cell, 1);
            update(cell);
        }        

        public ColorBombMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            if (cell.isLocked() || ctx.generator.dominoMode && !isDoubleDomino(cell) || !ctx.generator.dominoMode && !(cell.item instanceof Dot))
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            cancel();
            
            ctx.score.usedColorBomb();
            
            Sound.DROP.play();
            if (ctx.generator.dominoMode)
                explodeDominoes((Domino) cell.item);
            else
                explode(cell.item.getColor());
            
            removeStartItem();
            process();
        }
        
        private boolean isDoubleDomino(Cell cell)
        {
            if (!(cell.item instanceof Domino))
                return false;
            
            return ((Domino) cell.item).isDouble();
        }
        
        private void explodeDominoes(Domino doubleDomino)
        {
            int pip = doubleDomino.num[0];
            
            Set<Cell> exploded = new HashSet<Cell>();            
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell cell = m_state.cell(col, row);
                    Item item = cell.item;
                    if (item != null && !cell.isLocked() && !exploded.contains(cell) && cell.item instanceof Domino)
                    {
                        Domino d = (Domino) cell.item;
                        if (d.num[0] == pip || d.num[1] == pip)
                        {
                            explodeDomino(cell, exploded);
                        }
                    }
                }
                m_state.explodeNeighbors(exploded);                
            }
        }
        
        private void explode(Integer color)
        {
            Set<Cell> exploded = new HashSet<Cell>();
            Set<Cell> explodies = new HashSet<Cell>();
            for (int row = 0; row < m_state.numRows; row++)
            {
                for (int col = 0; col < m_state.numColumns; col++)
                {
                    Cell cell = m_state.cell(col, row);
                    Item item = cell.item;
                    if (item != null && !exploded.contains(cell) && !cell.isLocked() && cell.canExplode(color))
                    {
                        explode(cell, color, exploded, explodies);
                    }
                }
            }
            m_state.explodeNeighbors(exploded);
            
            for (Cell explody : explodies)
            {
                if (explody.item == null)
                    m_state.addExplody(explody);
            }
        }
        
        private void explodeDomino(Cell cell, Set<Cell> exploded)
        {
            exploded.add(cell);
            cell.explode(null, 1);
        }

        private void explode(Cell cell, Integer color, Set<Cell> exploded, Set<Cell> explodies)
        {
            if (cell.item instanceof WrappedDot)
                explodies.add(cell);
            
            exploded.add(cell);
            cell.explode(color, 1);
        }
    }
    
    public static class ReshuffleMode extends SpecialMode
    {        
        public ReshuffleMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }
        
        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {           
            cancel();
                      
            ctx.state.reshuffle(new Runnable() {
                @Override
                public void run()
                {

                    removeStartItem();
                    process();
                }
            });
        }
    }
    
    public static class WildCardMode extends SpecialMode
    {        
        public WildCardMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        public static boolean canReplace(Cell cell)
        {
            return ExplodyMode.canReplace(cell);
        }
        
        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            if (!canReplace(cell))
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            cancel();
            
            Sound.DROP.play();
            
            cell.explode(null, 1);
            if (cell.item == null)
                m_state.addItem(cell, new Wild(false));
            
            removeStartItem();
            process();
        }
    }
    
    public static class KeyMode extends SpecialMode
    {        
        public KeyMode(Cell cell, int x, int y, Context ctx, ConnectMode connectMode)
        {
            super(cell, ctx, connectMode);
            
            startMoveListener();
            startMouseUpListener();
            
            startDragLine(cell, x, y);            
            createDragBox(cell, 1);
            update(cell);
        }        

        public KeyMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        public static boolean canReplace(Cell cell)
        {
            return ExplodyMode.canReplace(cell);
        }
        
        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            boolean canUnlock = cell.isLocked() && ((Unlockable) cell).canUnlock();
            boolean isChest = !canUnlock && cell.item instanceof Chest;
            
            if (!canUnlock && !isChest)
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            cancel();
            
            Sound.DROP.play();
            
            if (canUnlock)
                ((Unlockable) cell).unlock();
            else
                cell.openChest();
            
            removeStartItem();
            process();
        }
    }
    
    public static class ExplodyMode extends SpecialMode
    {        
        public ExplodyMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
                        
            createDragBox(null, 1);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        public static boolean canReplace(Cell cell)
        {
            return GetSwapMatches.isSwapStart(cell) || cell.item instanceof Domino;
        }
        
        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            if (!canReplace(cell))
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            cancel();
            
            Sound.DROP.play();
            
            cell.explode(null, 1);
            if (cell.item == null)
                m_state.addItem(cell, new Explody());
            
            removeStartItem();
            process();
        }
    }
    
    public static class DropMode extends SpecialMode
    {
        public DropMode(Cell cell, int x, int y, Context ctx, ConnectMode connectMode)
        {
            super(cell, ctx, connectMode);
            
            startMoveListener();
            startMouseUpListener();
            
            startDragLine(cell, x, y);
            
            int n = ((Drop) m_item).getRadius();
            createDragBox(cell, n);
            update(cell);
        }        

        public DropMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
            
            int n = ((Drop) m_item).getRadius();
            createDragBox(null, n);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            int n = ((Drop) m_item).getRadius();
            
            boolean doused = false;
            for (int col = cell.col - n / 2, ci = 0; ci < n; ci++, col++)
            {
                for (int row = cell.row - n / 2, ri = 0; ri < n; ri++, row++)
                {
                    if (!m_state.isValidCell(col, row))
                        continue;
                    
                    Cell c = m_state.cell(col, row);
                    if (!c.isLocked() && c.item instanceof Fire)
                    {
                        c.explode(null, 0);
                        doused = true;
                    }
                }
            }
            
            if (!doused)
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            Sound.DRIP.play();
            cancel();
            
            removeStartItem();
            process();
        }        
    }
    
    public static class IcePickMode extends SpecialMode
    {
        public IcePickMode(Cell cell, int x, int y, Context ctx, ConnectMode connectMode)
        {
            super(cell, ctx, connectMode);
            
            startMoveListener();
            startMouseUpListener();
            
            startDragLine(cell, x, y);
            
            int n = ((IcePick) m_item).getRadius();
            createDragBox(cell, n);
            update(cell);
        }        

        public IcePickMode(Item item, Context ctx, ConnectMode connectMode, Runnable removeItem)
        {
            super(item, ctx, connectMode, removeItem);
            
            startMoveListener();
            
            int n = ((IcePick) m_item).getRadius();
            createDragBox(null, n);
        }

        @Override
        protected void update(Cell cell)
        {
            updateDragLine(cell);
            updateDragBox(cell);
        }

        @Override
        public void done(Cell cell, boolean alwaysCancel)
        {
            int n = ((IcePick) m_item).getRadius();
            int strength = ((IcePick) m_item).getStrength();
            
            boolean melted = false;
            for (int col = cell.col - n / 2, ci = 0; ci < n; ci++, col++)
            {
                for (int row = cell.row - n / 2, ri = 0; ri < n; ri++, row++)
                {
                    if (!m_state.isValidCell(col, row))
                        continue;
                    
                    Cell c = m_state.cell(col, row);
                    if (c.reduceIce(strength))
                    {
                        melted = true;
                    }
                    else if (!c.isLocked())
                    {
                        if (c.item instanceof Mirror)
                        {
                            c.explode(null, 0);
                            melted = true;
                        }
                        else if (c.item instanceof Egg)
                        {
                            Egg egg = (Egg) c.item;
                            if (!egg.isCracked())
                            {
                                egg.crack();
                                melted = true;
                            }
                        }
                    }
                }
            }
            
            if (!melted)
            {
                if (alwaysCancel)
                    cancel();
                
                Sound.MISS.play();
                return;
            }
            
            Sound.AXE.play();   
            
            cancel();
            
            removeStartItem();
            process();
        }        
    }
}