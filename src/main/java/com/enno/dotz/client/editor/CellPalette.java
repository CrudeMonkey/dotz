package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.FastLayer;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.ChangeColorCell;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.ConveyorCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.ItemCell;
import com.enno.dotz.client.Cell.Slot;
import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Controller;
import com.enno.dotz.client.Direction;

public class CellPalette extends Palette<Cell>
{
    public CellPalette()
    {
        super(30, CellButton.SIZE, 8, 3);
        
        ctx.doorLayer = new FastLayer();
        add(ctx.doorLayer);
        
        addButton(new ItemCell(), 0, 0);
        addButton(new Hole(), 1, 0);
        addButton(new Door(1, Direction.NORTH, 0), 2, 0);
        addButton(new Door(1, Direction.EAST, 0), 3, 0);
        addButton(new Door(1, Direction.SOUTH, 0), 4, 0);
        addButton(new Door(1, Direction.WEST, 0), 5, 0);
        addButton(new Door(1, Direction.NONE, 0), 6, 0);
        addButton(new Door(Controller.ON_OFF), 7, 0);
        
        addButton(new Slide(false), 0, 1);
        addButton(new Slide(true), 1, 1);
        addButton(new Cage(1, false), 2, 1);
        addButton(new Cage(1, true), 3, 1);
        addButton(new Cage(Controller.ON_OFF), 4, 1);
        addButton(new Teleport(false), 5, 1);
        addButton(new Teleport(true), 6, 1);
        addButton(new ChangeColorCell(), 7, 1);        

        addButton(new CircuitCell(), 0, 2);
        addButton(new ConveyorCell(Direction.NORTH, -1), 1, 2);
        addButton(new ConveyorCell(Direction.NORTH, 0), 2, 2);
        addButton(new ConveyorCell(Direction.NORTH, 1), 3, 2);
        addButton(new Bubble(), 4, 2);        
        addButton(new Rock(), 5, 2);
        addButton(new Machine(), 6, 2);
        addButton(new Slot(), 7, 2);
    }
    
    private void addButton(Cell cell, int col, int row)
    {
        cell.init(ctx);
        
        int W = CellButton.SIZE;
        double x = (col + 0.5) * W;
        double y = (row + 0.5) * W;
        
        if (cell instanceof Hole)
            ((Hole) cell).initGraphics(0, 0, x, y, true);
        else if (cell instanceof Rock)
            ((Rock) cell).initGraphics(0, 0, x, y, true);
        else 
            cell.initGraphics(0, 0, x, y);
        
        if (cell instanceof ItemCell || cell instanceof Teleport || cell instanceof ChangeColorCell || cell instanceof ConveyorCell)
        {
            Rectangle r = new Rectangle(ctx.cfg.size, ctx.cfg.size);
            r.setStrokeWidth(1);
            r.setStrokeColor(ColorName.BLACK);
            r.setX(x - ctx.cfg.size / 2);
            r.setY(y - ctx.cfg.size / 2);
            ctx.backgroundLayer.add(r);
        }
        
        CellButton b = new CellButton(col, row, 1, 1, cell, ctx);
        b.setX((col + 0.5) * W);
        b.setY((row + 0.5) * W);
        m_list.add(b);
    }
    
    public static class CellButton extends PaletteButton<Cell>
    {
        public static final int SIZE = 30 + 10;                

        public CellButton(int col, int row, int numCols, int numRows, Cell cell, Context ctx)
        {
            super(col, row, numCols, numRows, SIZE, cell, ctx, false);
        }
    }
}
