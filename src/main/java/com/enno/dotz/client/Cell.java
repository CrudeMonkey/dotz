package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Polygon;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.LinearGradient;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.LineCap;
import com.ait.lienzo.shared.core.types.LineJoin;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Controller.Controllable;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Item.ExplodeAction;
import com.enno.dotz.client.item.RandomItem;
import com.google.gwt.dom.client.Style.FontWeight;

public abstract class Cell
{
    public static interface Unlockable
    {
        /**
         * @return Whether the cell can be unlocked with a Key (e.g. Door/Cage)
         */
        boolean canUnlock();
        
        /**
         * With a Key.
         */
        void unlock();
    }
    
    public Item item;
    public int col, row;
    
    public int ice;
    
    public DropDirection lastSlideDir;
    protected Rectangle m_iceShape;
    
    protected Context ctx;
    protected double m_x;
    protected double m_y;
    
    protected Cell()
    {        
        //NOTE: row, col is set by GridState.addCell()
    }
    
    public void init(Context ctx)
    {
        this.ctx = ctx;
    }

    public Pt pt()
    {
        return new Pt(col, row);
    }
    
    public abstract Cell copy();
    
    protected void copyValues(Cell from)
    {
        if (from.item != null)
            item = from.item.copy();

        ice = from.ice;
    }
    
    public boolean canDrop()
    {
        return item != null && !item.isArmed() && item.canDrop(); 
        // NOTE: armed striped candy should blast the line it is on
    }
    
    /**
     * Used by layout editor.
     * 
     * @return
     */
    public boolean canContainItems()
    {
        return true;
    }
    
    public boolean canBeFilled()
    {
        return item == null;
    }
    
    public boolean canConnect(Integer color, boolean isWordMode)
    {
        if (item == null || !item.canConnect())
            return false;
        
        if (color == null || ctx.isWild(color) || ctx.isWild(item.getColor()))
            return true;
        
        return isWordMode || item.getColor().equals(color);
    }

    /**
     * @param color
     * @return Whether this cell can explode after a square was dragged, 
     *          i.e. it must contain a dot with that color.
     */
    public boolean canExplode(Integer color)
    {
        if (item == null)
            return false;
        
        return color.equals(item.getColor());            
    }
    
    public boolean canBeNuked()
    {
        if (item == null)
            return false;
        
        return true;
    }

    public boolean canGrowFire()
    {
        if (item == null)
            return false;
        
        return item.canGrowFire();
    }

    public boolean canExplodeNextTo(Collection<Cell> cells, Integer color)
    {
        if (item == null)
            return false;
        
        return item.canExplodeNextTo();
    }
    
    /**
     * @return Whether it's a Door with strength. If so, fire and animals can't grow/move from it. 
     */
    public boolean isLocked()
    {
        return false;
    }
    
    public boolean isLockedCage()
    {
        return false;
    }
    
    /**
     * Whether it's a locked Door or a Rock.
     * 
     * @return
     */
    public boolean isLockedDoor()
    {
        return false;
    }

    public boolean isUnpoppedBubble()
    {
        return false;
    }
    
    public boolean isTeleportSource()
    {
        return false;
    }
    
    public boolean isTeleportTarget()
    {
        return false;
    }
    
    public boolean canHaveIce()
    {
        return true;
    }
    
    public boolean stopsLaser()
    {
        return true;
    }
    
    protected boolean itemStopsLaser()
    {
        if (item == null)
            return false;
        
        return item.stopsLaser();
    }

    /**
     * E.g. Blinking Cage
     * 
     * @return
     */
    public boolean hasController()
    {
        return false;
    }

    public String toString()
    {
        return col + "," + row;
    }

    public void initGraphics(int col, int row, double x, double y)
    {
        m_x = x;
        m_y = y;
        updateIce();
    }
    
    public void removeGraphics()
    {
        if (m_iceShape != null)
        {
            ctx.iceLayer.remove(m_iceShape);
        }
    }
    
    public void animate(long t, double cursorX, double cursorY)
    {
        if (item != null)
        {
            item.animate(t, cursorX, cursorY);
        }
    }
    
    public boolean reduceIce(int n)
    {
        if (ice > 0)
        {
            ice -= n;
            if (ice < 0)
                ice = 0;
            
            updateIce();
            
            if (ice == 0)
                ctx.score.explodedIce(); // only the last ice counts
            
            return true;
        }
        return false;
    }
    
    public void explode(Integer color, int chainSize)
    {
        reduceIce(1);
        
        if (item != null)
        {
            ExplodeAction action = item.explode(color, chainSize);
            if (action == ExplodeAction.EXPLODY)
            {
                item.removeShapeFromLayer(ctx.dotLayer);
                
                Explody explody = new Explody();
                explody.init(ctx);
                explody.addShapeToLayer(ctx.dotLayer);
                explody.moveShape(ctx.state.x(col), ctx.state.y(row));
                item = explody;
            }
            else if (action == ExplodeAction.REMOVE)                
            {
                item.removeShapeFromLayer(ctx.dotLayer);
                item = null;
            }
            else if (action == ExplodeAction.OPEN)                
            {
                openChest();
            }
        }
    }
    
    public void openChest()
    {
        Chest chest = (Chest) item;
        item.removeShapeFromLayer(ctx.dotLayer);
        item = chest.getItem();
        
        if (item != null)
        {
            if (item instanceof RandomItem)
            {
                item = ctx.generator.getNextItem(ctx, false);
                // NOTE: calls item.init(ctx);
            }
            else
            {
                item.init(ctx);
            }
            item.addShapeToLayer(ctx.dotLayer);
            item.moveShape(ctx.state.x(col), ctx.state.y(row));
        }
    }
    
    /**
     * Caused by Circuit or Explody
     */
    public void zap()
    {
        explode(null, 1);
    }
    
    public void updateIce()
    {
        if (ice == 0)
        {
            if (m_iceShape != null)
            {
                ctx.iceLayer.remove(m_iceShape);
                m_iceShape = null;
            }
        }
        else
        {
            if (m_iceShape == null)
            {
                double sz = ctx.generator.generateLetters ? ctx.cfg.size * 0.9 : ctx.cfg.size * 0.65;
                m_iceShape = new Rectangle(sz, sz);
                m_iceShape.setX(m_x - sz / 2);
                m_iceShape.setY(m_y - sz / 2); 
                ctx.iceLayer.add(m_iceShape);
            }
            int r = 255 - ice * 20;
            if (r < 0) r = 0;
            m_iceShape.setFillColor(new Color(r, r, r));
        }
    }

    /**
     * @return  Whether +/- keys can modify the strength in the Level Editor.
     */
    public boolean canIncrementStrength()
    {
        return false;
    }
    
    /**
     * Override for cells that can increment/decrement strength in the layout editor (with +/- or '<' / '>')
     * 
     * @param ds    {1, -1} 
     */
    public void incrementStrength(int ds)
    {
    }

    public static class ItemCell extends Cell
    {
        public ItemCell()
        {            
        }
        
        @Override
        public Cell copy()
        {
            ItemCell c = new ItemCell();
            c.copyValues(this);
            return c;
        }
        
        @Override
        public boolean stopsLaser()
        {
            return itemStopsLaser();
        }
    }
    
    
    public static class ChangeColorCell extends Cell
    {
        private Group m_shape;

        public ChangeColorCell()
        {
        }
        
        @Override
        public Cell copy()
        {
            ChangeColorCell c = new ChangeColorCell();
            c.copyValues(this);
            return c;
        }
        
        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            List<IColor> colors = ctx.generator == null ? null : ctx.generator.getColors(ctx);
            if (colors == null || colors.size() == 0)
                colors = getDefaultColors();
            
            m_shape = createGraphics(ctx.cfg.size, colors);
            
            ctx.backgroundLayer.add(m_shape);
        }
        
        private List<IColor> getDefaultColors()
        {
            List<IColor> list = new ArrayList<IColor>();
            for (IColor col : Config.COLORS)
            {
                list.add(col);
            }
            return list;
        }

        protected Group createGraphics(double sz, List<IColor> colors)
        {
            Group g = new Group();
            
            Text t = new Text("?");
            t.setFontFamily("Arial");
            t.setFontStyle("bold");
            t.setX(m_x);
            t.setY(m_y);
            t.setTextAlign(TextAlign.CENTER);
            t.setTextBaseLine(TextBaseLine.MIDDLE);
            t.setFontSize((int) (sz * 0.8));
            t.setStrokeColor(ColorName.BLACK);
            t.setStrokeWidth(2);
            
            double f = sz * 0.3;
            LinearGradient gr = new LinearGradient(m_x, -f, m_x, f);
            for (int i = 0, n = colors.size(); i < n; i++)
            {
                gr.addColorStop(i / (double)(n - 1), colors.get(i));
            }
            t.setFillGradient(gr);
                        
            g.add(t);
            
            return g;
        }
        
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }
    }
    
    public static class ConveyorCell extends Cell
    {
        private int m_direction = Direction.NORTH; // initial direction
        private int m_turn; // -1 is left turn, 0 is straight, 1 is right turn
        
        private Group m_shape;
        private PolyLine[] m_tracks = new PolyLine[2];
        
        public ConveyorCell(int direction, int turn)
        {
            m_direction = direction;
            m_turn = turn;
        }
        
        @Override
        public Cell copy()
        {
            return new ConveyorCell(m_direction, m_turn);
        }
        
        protected void setRotation()
        {
            double angle = 0;
            switch (m_direction)
            {
                case Direction.EAST: angle = Math.PI / 2; break; 
                case Direction.WEST: angle = -Math.PI / 2; break; 
                case Direction.SOUTH: angle = Math.PI; break; 
            }
            m_shape.setRotation(angle);
        }
        
        /** Used by Layout Editor */
        public void rotate()
        {
            m_direction = Direction.rotate(m_direction, true); // clockwise
            setRotation();
            ctx.backgroundLayer.draw();
        }        
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape();
            setRotation();
            
            ctx.backgroundLayer.add(m_shape);
        }
        
        protected Group createShape()
        {            
            double sz = ctx.cfg.size;
            double sz2 = sz / 2;
            double w = 8;
            double d = sz * 0.05;
            
            double r = w * 2; // corner radius
            
            Group shape = new Group();
            shape.setX(m_x);
            shape.setY(m_y);
            
            double z = w / 2;

            double x0 = d - sz2 + z;
            double x1 = sz2 - d - w + z;
            double y1 = sz2 - w - d + z;
            double y2 = -sz2 + d + z;

            IColor color = ColorName.GRAY;
            
            if (m_turn == 0)
            {
                PolyLine p = new PolyLine(x0, sz2, x0, -sz2);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                m_tracks[0] = p;
                shape.add(p);
                
                p = new PolyLine(x1, sz2, x1, -sz2);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                m_tracks[1] = p;
                shape.add(p);
            }
            else if (m_turn == -1) // left turn
            {
                PolyLine p = new PolyLine(x0, sz2, x0, y1, -sz2, y1);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                p.setCornerRadius(r);
                m_tracks[0] = p;
                shape.add(p);
                
                p = new PolyLine(x1, sz2, x1, y2, -sz2, y2);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                p.setCornerRadius(r);
                m_tracks[1] = p;
                shape.add(p);
            }
            else // right turn
            {
                PolyLine p = new PolyLine(x0, sz2, x0, y2, sz2, y2);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                p.setCornerRadius(r);
                m_tracks[0] = p;
                shape.add(p);
                
                p = new PolyLine(x1, sz2, x1, y1, sz2, y1);
                p.setStrokeColor(color);
                p.setStrokeWidth(w);
                p.setDashArray(w, 2);
                p.setCornerRadius(r);
                m_tracks[1] = p;
                shape.add(p);
            }
            
            double tw = 4;
            double th = 4;
            
            double ty = sz2 - th;
            Triangle tri = new Triangle(new Point2D(-tw, ty), new Point2D(tw, ty), new Point2D(0, ty - th));
            tri.setFillColor(color);            
            shape.add(tri);
            
            return shape;
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }        

        @Override
        public void animate(long t, double cursorX, double cursorY)
        {
            int n = (int) ((t / 50) % 10);
            m_tracks[0].setDashOffset(9 - n);
            m_tracks[1].setDashOffset(9 - n);
            
            super.animate(t, cursorX, cursorY);
        }
        
        @Override
        public boolean stopsLaser()
        {
            return false;
        }
        
        public int getDirection()
        {
            return m_direction;
        }
        
        public int getExitDirection()
        {
            if (m_turn == 0)
                return m_direction;
            
            return Direction.rotate(m_direction, m_turn == 1);
        }
        
        public int getTurn()
        {
            return m_turn;
        }

        public void turnTo(int dir)
        {
            ctx.backgroundLayer.remove(m_shape);
            m_turn = Direction.turn(m_direction, dir);
                        
            m_shape = createShape();
            setRotation();
            ctx.backgroundLayer.add(m_shape);
        }
    }
    
    public static class Teleport extends Cell
    {
        public static final IColor COLOR = ColorName.DARKGRAY;
        public static final int DISCONNECTED = -1;
        
        private boolean m_isTarget;
        private int m_otherCol = DISCONNECTED;
        private int m_otherRow = DISCONNECTED;
        
        private Rectangle m_shape;
        
        public Teleport(boolean isTarget)
        {
            m_isTarget = isTarget;
        }
        
        @Override
        public boolean isTeleportSource()
        {
            return !isTarget();
        }
        
        @Override
        public boolean isTeleportTarget()
        {
            return isTarget();
        }
        
        public void setDisconnected()
        {
            m_otherCol = DISCONNECTED;
            m_otherRow = DISCONNECTED;
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            int h = 5;
            double sz = ctx.cfg.size;
            
            m_shape = new Rectangle(sz, h);
            m_shape.setFillColor(COLOR);
            m_shape.setX(m_x - sz/2);
            m_shape.setY(m_y + (m_isTarget ? 0 : sz - h) - sz/2);
            ctx.backgroundLayer.add(m_shape);
        }

        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }
        
        public boolean isTarget()
        {
            return m_isTarget;
        }
        
        public void setOther(int col, int row)
        {
            m_otherCol = col;
            m_otherRow = row;
        }
        
        public int getOtherCol()
        {
            return m_otherCol;
        }

        public int getOtherRow()
        {
            return m_otherRow;
        }

        @Override
        public boolean stopsLaser()
        {
            return itemStopsLaser();
        }
        
        @Override
        public Cell copy()
        {
            Teleport c = new Teleport(m_isTarget);            
            c.copyValues(this);
            c.m_otherRow = m_otherRow;
            c.m_otherCol = m_otherCol;
            return c;
        }
    }
    
    public static class Hole extends Cell
    {
        private static IColor HOLE_COLOR = new Color(180, 180, 180);
        private static IColor DARK_BORDER = ColorName.BLACK;
        private static IColor LIGHT_BORDER = new Color(235, 235, 235);
        
        private Group m_shape;

        public Hole()
        {            
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            initGraphics(col, row, x, y, false);
        }
            
        public void initGraphics(int col, int row, double x, double y, boolean addBorder)
        {
            if (!ctx.isEditing)
                return;
            
            super.initGraphics(col, row, x, y);
            
            double sz = ctx.cfg.size;
            GridState state = ctx.state;
            
            Group g = new Group();
            g.setX(m_x - sz/2);
            g.setY(m_y - sz/2);            
            
            Rectangle r = new Rectangle(sz, sz);
            r.setFillColor(HOLE_COLOR);
            g.add(r);
            
            int w = 1;
            double sz1 = sz - 1;
            if (addBorder || !holeAt(col, row - 1, state))
            {
                Line line = new Line(0, 0, sz, 0); // above
                line.setStrokeColor(DARK_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!holeAt(col - 1, row, state))
            {
                Line line = new Line(0, 0, 0, sz); // left
                line.setStrokeColor(DARK_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!holeAt(col, row + 1, state))
            {
                Line line = new Line(0, sz1, sz, sz1); // below
                line.setStrokeColor(LIGHT_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!holeAt(col + 1, row, state))
            {
                Line line = new Line(sz1, 0, sz1, sz); // right
                line.setStrokeColor(LIGHT_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            
            m_shape = g;
            ctx.backgroundLayer.add(m_shape);
        }
        
        @Override
        public void removeGraphics()
        {
            if (!ctx.isEditing)
                return;
            
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }
                
        private boolean holeAt(int col, int row, GridState state)
        {
            if (col < 0 || col > state.numColumns - 1 || row < 0 || row > state.numRows - 1)
                return false;
            
            return state.cell(col,  row) instanceof Hole;
        }
        
        @Override
        public boolean canContainItems()
        {
            return false;
        }
        
        @Override
        public boolean canBeFilled()
        {
            return false;
        }        

        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public Cell copy()
        {
            Hole c = new Hole();
            c.copyValues(this);
            return c;
        }
    }
    
    public static class Rock extends Cell
    {
        private static IColor ROCK_COLOR = new Color(210, 210, 210);
        private static IColor LIGHT_BORDER = ColorName.BLACK;
        private static IColor DARK_BORDER = new Color(235, 235, 235);
        
        private Group m_shape;

        public Rock()
        {            
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            initGraphics(col, row, x, y, false);
        }
            
        public void initGraphics(int col, int row, double x, double y, boolean addBorder)
        {
            if (!ctx.isEditing)
                return;
            
            super.initGraphics(col, row, x, y);
            
            double sz = ctx.cfg.size;
            GridState state = ctx.state;
            
            Group g = new Group();
            g.setX(m_x - sz/2);
            g.setY(m_y - sz/2);            
            
            Rectangle r = new Rectangle(sz, sz);
            r.setFillColor(ROCK_COLOR);
            g.add(r);
            
            int w = 1;
            double sz1 = sz - 1;
            if (addBorder || !rockAt(col, row - 1, state))
            {
                Line line = new Line(0, 0, sz, 0); // above
                line.setStrokeColor(DARK_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!rockAt(col - 1, row, state))
            {
                Line line = new Line(0, 0, 0, sz); // left
                line.setStrokeColor(DARK_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!rockAt(col, row + 1, state))
            {
                Line line = new Line(0, sz1, sz, sz1); // below
                line.setStrokeColor(LIGHT_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            if (addBorder ||!rockAt(col + 1, row, state))
            {
                Line line = new Line(sz1, 0, sz1, sz); // right
                line.setStrokeColor(LIGHT_BORDER);
                line.setStrokeWidth(w);
                g.add(line);
            }
            
            m_shape = g;
            ctx.backgroundLayer.add(m_shape);
        }
        
        @Override
        public void removeGraphics()
        {
            if (!ctx.isEditing)
                return;
            
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }
                
        private boolean rockAt(int col, int row, GridState state)
        {
            if (col < 0 || col > state.numColumns - 1 || row < 0 || row > state.numRows - 1)
                return false;
            
            return state.cell(col,  row) instanceof Rock;
        }
        
        @Override
        public boolean canContainItems()
        {
            return false;
        }
        
        @Override
        public boolean canBeFilled()
        {
            return false;
        }        

        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public boolean isLocked()
        {
            return true;
        }

        @Override
        public boolean isLockedDoor()
        {
            return true;
        }
        
        @Override
        public Cell copy()
        {
            Rock c = new Rock();
            c.copyValues(this);
            return c;
        }
    }
    
    public static class Door extends Cell implements Controllable, Unlockable
    {
        private boolean m_blinking;
        private Controller m_controller;
        
        private int m_rotationDirection = 1; // 1 is clockwise, -1 is CCW, 0 is neither
        private int m_direction;
        private int m_strength;
        
        private Group m_shape;
        private Text m_text;

        private Pt m_neighbor;
        private Polygon m_b;
        private Polygon m_a;
        private Rectangle m_offDash;
        
        public Door(int strength, int direction, int rotationDirection)
        {
            m_direction = direction;
            m_strength = strength;
            m_rotationDirection = rotationDirection;
        }

        /** Blinking Door */
        public Door(String sequence)
        {
            m_blinking = true;
            m_strength = 1;
            m_rotationDirection = 0;
            m_direction = Direction.NONE;
            m_controller = new Controller(sequence);
        }
        
        public boolean isBlinking()
        {
            return m_blinking;
        }
        
        @Override
        public Cell copy()
        {
            Door c = m_blinking ? new Door(m_controller.getSequence()) :
                new Door(m_strength, m_direction, m_rotationDirection);
            c.copyValues(this);
            return c;
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape(ctx.cfg.size);
            ctx.doorLayer.add(m_shape);
        }
        
        public Group createShape(double sz)
        {
            Group shape = new Group();
            shape.setX(m_x - sz/2);
            shape.setY(m_y - sz/2);
                        
            IColor light = ColorName.LIGHTBLUE;
            IColor medium = darker(light, 0.8);
            IColor dark = darker(medium, 0.8);
            
            double alpha = 0.5;
            if (m_direction == Direction.NONE)
            {
                Triangle t = new Triangle(new Point2D(0, 0), new Point2D(0, sz), new Point2D(sz, sz));
                t.setAlpha(alpha);
                t.setFillColor(dark);
                shape.add(t);
                
                t = new Triangle(new Point2D(0, 0), new Point2D(sz, 0), new Point2D(sz/2, sz/2));
                t.setAlpha(alpha);
                t.setFillColor(light);
                shape.add(t);
                
                t = new Triangle(new Point2D(sz, 0), new Point2D(sz, sz), new Point2D(sz/2, sz/2));
                t.setAlpha(alpha);
                t.setFillColor(medium);
                shape.add(t);
            }
            else
            {
                // Determine which cell has to explode
                m_neighbor = determineNeighbor(col, row);
                
                // Create shape
                double sz1 = sz / 3;
                double sz2 = 2 * sz1;
                Rectangle r = new Rectangle(sz1, sz1);
                r.setFillColor(medium);
                r.setAlpha(alpha);
                r.setX(sz1);
                r.setY(sz1);
                shape.add(r);
                
                // m_a, m_b is based on WEST (then angle = 0)
                
                // lightest/smaller shape
                m_a = new Polygon(0,0, sz1,sz1, sz1,sz2, 0,sz);
                m_a.setOffset(sz/2, sz/2);
                m_a.setFillColor(light);
                m_a.setAlpha(alpha);
                shape.add(m_a);

                m_b = new Polygon(0,0, sz,0, sz,sz, 0,sz, sz1,sz2, sz2,sz2, sz2,sz1, sz1,sz1);
                m_b.setOffset(sz/2, sz/2);
                m_b.setFillColor(dark);
                m_b.setAlpha(alpha);                
                shape.add(m_b);
                
                setRotation();
            }
            
            m_text = new Text("" + m_strength);
            m_text.setFillColor(ColorName.BLACK);
            m_text.setFontSize(9);
            m_text.setFontStyle(FontWeight.BOLD.getCssName());
            m_text.setX(5);
            m_text.setY(5);
            m_text.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
            
            shape.add(m_text);
            
            if (m_strength < 2)
                m_text.setVisible(false);
            
            if (m_blinking)
            {
                Rectangle r = new Rectangle(sz, sz);
                r.setStrokeColor(ColorName.BLACK);
                r.setDashArray(3, 3);
                shape.add(r);
                m_offDash = r;
            }
            
            return shape;
        }
        
        protected void setRotation()
        {
            double angle = 0;
            switch (m_direction)
            {
                case Direction.NORTH: angle = Math.PI / 2; break; 
                case Direction.SOUTH: angle = -Math.PI / 2; break; 
                case Direction.EAST: angle = Math.PI; break; 
            }
            m_a.setRotation(angle);
            m_b.setRotation(angle);
        }
        
        protected void tickRotateDoor()
        {
            if (m_direction == Direction.NONE || m_rotationDirection == 0)
                return;
            
            m_direction = Direction.rotate(m_direction, m_rotationDirection == 1); // 1=clockwise
            setRotation();
            m_neighbor = determineNeighbor(col, row);
        }
        
        /** Used by Layout Editor */
        public void rotate()
        {
            if (m_direction == Direction.NONE)
                return;
            
            m_direction = Direction.rotate(m_direction, true); // clockwise
            setRotation();
            ctx.doorLayer.draw();
        }
        
        @Override
        public String getSequence()
        {
            if (m_controller == null)
                return null;
            
            return m_controller.getSequence();
        }
        
        @Override
        public void setSequence(String seq)
        {
            m_controller = new Controller(seq);
        }
        
        @Override
        public boolean hasController()
        {
            return m_blinking;
        }
        
        @Override
        public void tick()
        {
            if (!m_blinking)
                return;
            
            m_strength = m_controller.tick();
            updateStrength();
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.doorLayer.remove(m_shape);
        }
                
        protected static IColor darker(IColor col, double factor)
        {
            return new Color(
                    (int) Math.round(col.getR() * factor), 
                    (int) Math.round(col.getG() * factor), 
                    (int) Math.round(col.getB() * factor));
        }
        
        @Override
        public boolean canDrop()
        {
            return m_strength == 0 && super.canDrop();
        }
        
        @Override
        public boolean canBeFilled()
        {
            return m_strength == 0 && super.canBeFilled();
        }
        
        @Override
        public boolean canConnect(Integer color, boolean isWordMode)
        {
            return m_strength == 0 && super.canConnect(color, isWordMode);
        }
        
        @Override
        public boolean canBeNuked()
        {
            return m_strength > 0 || super.canBeNuked();
        }

        @Override
        public boolean canGrowFire()
        {
            return m_strength == 0 && super.canGrowFire();
        }
        
        @Override
        public boolean canExplodeNextTo(Collection<Cell> cells, Integer color)
        {
            if (m_blinking && m_strength > 0)
                return false;
            
            if (m_strength == 0)
            {
                return super.canExplodeNextTo(cells, color);
            }
            
            if (hasDirection())
            {
                if (m_neighbor == null)
                    return false;
                
                Cell cell = ctx.state.cell(m_neighbor.col, m_neighbor.row);
                if (!cells.contains(cell))
                    return false;
                
                return matchesColor(color);
            }
            else
            {
                return matchesColor(color);
            }
        }
        
        private boolean matchesColor(Integer color)
        {
            if (color == null)
                return true;
            
            if (item == null)
                return true;
            
            // If the Door has an item with a color, it has to match if color != null
            Integer c = item.getColor();
            if (c == null || c == Config.WILD_ID)
                return true;
            
            return c.equals(color);                
        }
        
        @Override
        public boolean stopsLaser()
        {
            return isLocked() || itemStopsLaser();
        }
        
        protected Pt determineNeighbor(int col, int row)
        {
            GridState state = ctx.state;
            if (state == null)
                return null; // when in level editor
            
            if (col > 0 && m_direction == Direction.WEST)
                return new Pt(col - 1, row);
            if (col < state.numColumns - 1 && m_direction == Direction.EAST)
                return new Pt(col + 1, row);
            if (row > 0 && m_direction == Direction.NORTH)
                return new Pt(col, row - 1);
            if (row < state.numRows - 1 && m_direction == Direction.SOUTH)
                return new Pt(col, row + 1);

            return null;
        }
        
        /**
         * @return Whether it's a Door with strength. If so, fire and animals can't grow/move from it. 
         */
        @Override
        public boolean isLocked()
        {
            return m_strength > 0;
        }
        
        @Override
        public boolean isLockedDoor()
        {
            return isLocked();
        }
        
        @Override
        public void explode(Integer color, int chainSize)
        {
            if (m_strength == 0 || m_blinking)
            {
                super.explode(color, chainSize);
            }
            else
            {
                m_strength--;
                
                if (m_strength == 0)
                {
                    ctx.score.explodedDoor();                    
                    ctx.doorLayer.remove(m_shape);
                    
                    super.explode(color, chainSize);
                }
                
                updateStrength();
            }
        }

        protected void updateStrength()
        {
            m_text.setText("" + m_strength);
            m_text.setVisible(m_strength > 1);
            
            m_shape.setVisible(m_strength > 0);
            
            ctx.doorLayer.draw();
        }
        
        @Override
        public boolean canUnlock()
        {
            if (m_blinking)
                return false; // can't unlock blinking door
            
            return isLocked();
        }
        
        /** Unlock with key */
        @Override
        public void unlock()
        {
            if (!canUnlock())
                return;
            
            m_strength = 0;
            ctx.score.explodedDoor();                    
            ctx.doorLayer.remove(m_shape);
            m_text.setVisible(false);
        }
        
        public void setStrength(int strength)
        {
            m_strength = strength;
        }
        
        public int getStrength()
        {
            return m_strength;
        }

        public int getDirection()
        {
            return m_direction;
        }

        public int getRotationDirection()
        {
            return m_rotationDirection;
        }

        public void setRotationDirection(int rotationDirection)
        {
            m_rotationDirection = rotationDirection;
        }
        
        @Override
        public boolean canIncrementStrength()
        {
            return !m_blinking;
        }
        
        @Override
        public void incrementStrength(int ds)
        {
            if (m_strength <= 1 && ds == -1)
                return;
            
            m_strength += ds;
            m_text.setText("" + m_strength);
            m_text.setVisible(m_strength > 1);
            
            ctx.doorLayer.draw();
        }

        @Override
        public void animate(long t, double cursorX, double cursorY)
        {
            if (m_blinking)
            {
                int n = (int) ((t / 50) % 6);
                m_offDash.setDashOffset(5 - n);
            }
            
            super.animate(t, cursorX, cursorY);
        }

        public boolean hasDirection()
        {
            return m_direction != Direction.NONE;
        }
    }
    
    public static class Cage extends Cell implements Controllable, Unlockable
    {
        protected int m_strength;
        
        private Group m_shape;
        private Text m_text;
        protected IColor m_color = ColorName.BLACK;
        
        private boolean m_blinking;
        private Controller m_controller;
        
        private Line[] m_offDash = new Line[3];
        
        public Cage(int strength)
        {
            m_strength = strength;
        }
        
        /** Blinking cage */
        public Cage(String sequence)
        {
            m_strength = 1;
            m_controller = new Controller(sequence);
            m_blinking = true;
        }
        
        public boolean isBlinking()
        {
            return m_blinking;
        }
        
        @Override
        public boolean hasController()
        {
            return m_blinking;
        }

        @Override
        public Cell copy()
        {
            Cage c = m_blinking ? new Cage(m_controller.getSequence()) : new Cage(m_strength);
            c.copyValues(this);
            return c;
        }
        
        @Override
        public boolean canConnect(Integer color, boolean isWordMode)
        {
            if (m_blinking)
                return m_strength == 0 && super.canConnect(color, isWordMode);
            else
                return super.canConnect(color, isWordMode);
        }
        
        @Override
        public boolean isLockedCage()
        {
            return isLocked();
        }
        
        @Override
        public boolean isLocked()
        {
            return m_strength > 0;
        }
        
        @Override
        public boolean canUnlock()
        {
            if (m_blinking)
                return false; // can't unlock blinking cage
            
            return isLocked();
        }
        
        /** Unlock with key */
        @Override
        public void unlock()
        {
            if (!canUnlock())
                return;
            
            m_strength = 0;
            ctx.score.explodedCage();                    
            ctx.doorLayer.remove(m_shape);
            m_text.setVisible(false);
        }

        @Override
        public boolean canDrop()
        {
            return m_strength == 0 && super.canDrop();
        }
        
        @Override
        public boolean canBeFilled()
        {
            return m_strength == 0 && super.canBeFilled();
        }
        
        @Override
        public boolean canExplode(Integer color)
        {
            return super.canExplode(color);
        }
        
        @Override
        public boolean canBeNuked()
        {
            return m_strength > 0 || super.canBeNuked();
        }

        @Override
        public boolean canGrowFire()
        {
            return m_strength == 0 && super.canGrowFire();
        }
        
        @Override
        public boolean stopsLaser()
        {
            return isLocked() || itemStopsLaser();
        }

      @Override
      public boolean canExplodeNextTo(Collection<Cell> cells, Integer color)
      {
          if (m_blinking && m_strength > 0)
              return false;
          
          if (m_strength == 0)
              return super.canExplodeNextTo(cells, color);
          
          return false;
      }
        
//        @Override
//        public boolean canExplodeNextTo(Collection<Cell> cells)
//        {
//            if (m_blinking)
//            {
//                if (m_strength > 0)
//                    return false;
//                else
//                    return super.canExplodeNextTo(cells);
//            }
//            
//            if (m_strength == 0 || item instanceof Chest)
//            {
//                return super.canExplodeNextTo(cells);
//            }
//            
//            return false;
//        }
        
        @Override
        public void explode(Integer color, int chainSize)
        {
            if (m_strength == 0 || m_blinking)
            {
                super.explode(color, chainSize);
            }
            else
            {
//                if (item instanceof Chest)
//                {
//                    super.explode(color, chainSize);
//                    return;
//                }
                
                m_strength--;
                
                if (m_strength == 0)
                {
                    ctx.score.explodedCage();                    
                    ctx.doorLayer.remove(m_shape);
                }
                
                updateStrength();
            }
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape(ctx.cfg.size);
            ctx.doorLayer.add(m_shape);
        }

        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.doorLayer.remove(m_shape);
        }
        
        public Group createShape(double sz)
        {
            Group shape = new Group();
            shape.setX(m_x - sz / 2);
            shape.setY(m_y - sz / 2);
            
            double b = 0.1 * sz;
            double s2 = sz - 2 * b;
            Rectangle r = new Rectangle(s2, s2);
            r.setX(b);
            r.setY(b);
            r.setStrokeColor(m_color);
            shape.add(r);
            
            double w = s2 / 4;
            for (int i = 1; i < 4; i++)
            {
                Line line = new Line(b + i * w, b, b + i * w, sz - b);
                line.setStrokeColor(m_color);
                
                if (m_blinking)
                {
                    line.setLineCap(LineCap.SQUARE);
                    line.setDashArray(3, 3);
                    m_offDash[i-1] = line;
                }
                
                shape.add(line);
            }
            
            m_text = new Text("" + m_strength);
            m_text.setFillColor(ColorName.BLACK);
            m_text.setFontSize(9);
            m_text.setFontStyle(FontWeight.BOLD.getCssName());
            m_text.setX(6);
            m_text.setY(5);
            m_text.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
            
            shape.add(m_text);
            
            if (m_strength < 2)
                m_text.setVisible(false);
            
            return shape;
        }

        public void setStrength(int strength)
        {
            m_strength = strength;
        }

        public int getStrength()
        {
            return m_strength;
        }
        
        @Override
        public boolean canIncrementStrength()
        {
            return !m_blinking;
        }
        
        @Override
        public void incrementStrength(int ds)
        {
            if (m_strength <= 1 && ds == -1)
                return;
            
            m_strength += ds;
            updateStrength();
        }
        
        protected void updateStrength()
        {
            m_text.setText("" + m_strength);
            m_text.setVisible(m_strength > 1);
            
            m_shape.setVisible(m_strength > 0);
            
            ctx.doorLayer.draw();
        }
        
        @Override
        public void tick()
        {
            if (!m_blinking)
                return;
            
            m_strength = m_controller.tick();
            updateStrength();
        }

        @Override
        public String getSequence()
        {
            if (m_controller == null)
                return null;
            
            return m_controller.getSequence();
        }
        
        @Override
        public void setSequence(String seq)
        {
            m_controller = new Controller(seq);
        }

        @Override
        public void animate(long t, double cursorX, double cursorY)
        {
            if (m_blinking)
            {
                int n = (int) ((t / 50) % 6);
                for (int i = 0; i < m_offDash.length; i++)
                    m_offDash[i].setDashOffset(5 - n);
            }
            
            super.animate(t, cursorX, cursorY);
        }
    }
    
    public static class Bubble extends Cell
    {
        private boolean m_popped;
        private Group m_shape;
        
        public boolean isPopped()
        {
            return m_popped;
        }
        
        @Override
        public boolean isUnpoppedBubble()
        {
            return !m_popped;
        }
        
        @Override
        public boolean canGrowFire()
        {
            if (!m_popped)
                return false;
            
            return super.canGrowFire();
        }
        
        @Override
        public boolean canBeNuked()
        {
            if (!m_popped)
                return true; // TODO not sure
            
            return super.canBeNuked();
        }
        
        @Override
        public boolean stopsLaser()
        {
            return !m_popped || super.stopsLaser();
        }
        
//        @Override
//        public boolean canExplodeNextTo(Collection<Cell> cells)
//        {
//            if (m_popped)
//                return super.canExplodeNextTo(cells);
//            
//            return item instanceof Chest;
//        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape(ctx.cfg.size);
            ctx.doorLayer.add(m_shape);
        }

        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.doorLayer.remove(m_shape);
        }
        
        public Group createShape(double sz)
        {
            Group shape = new Group();
            shape.setX(m_x - sz / 2);
            shape.setY(m_y - sz / 2);
        
            double r = sz * 0.45;
            double r2 = r * 0.9;
            double alpha = 0.6;
            
            double dr = Math.sqrt(2) * 0.5 * (r - r2);
            Circle c = new Circle(r2);
            c.setFillColor(Color.fromColorString("#93DEFF"));
            c.setFillAlpha(alpha);
            c.setX(sz / 2 - dr);
            c.setY(sz / 2 - dr);
            shape.add(c);
            
            MultiPath b = new MultiPath();
            b.M(0, r);
            b.A(r, r, 0, 0, 0, 0, -r);
            b.A(r, r, 0, 0, 0, 0, r);
            b.A(r2, r2, 0, 0, 1, 0, r - 2*r2);
            b.A(r2, r2, 0, 0, 1, 0, r);
            b.Z();
            //b.setStrokeColor(ColorName.BLACK);
            b.setFillAlpha(alpha);
            b.setFillColor(Color.fromColorString("#DDF9FE"));
//            b.setFillColor(ColorName.BLACK);
            b.setRotation(Math.PI * 0.75);
            b.setX(sz / 2);
            b.setY(sz / 2);
            shape.add(b);
            
            MultiPath p = new MultiPath();
            double r1 = r * 0.7;
            r2 = r * 0.85;
            double a = 0.5;
            double x = r1 * Math.cos(a);
            double y = r1 * Math.sin(a);
            p.M(x, y);
            x = r1 * Math.cos(-a);
            y = r1 * Math.sin(-a);
            p.A(r1, r1, 0, 0, 0, x, y);
            x = r2 * Math.cos(-a);
            y = r2 * Math.sin(-a);
            p.L(x, y);
            x = r2 * Math.cos(a);
            y = r2 * Math.sin(a);
            p.A(r2, r2, 0, 0, 1, x, y);
            p.Z();
            p.setFillColor(ColorName.WHITE);
            p.setFillAlpha(0.6);
            //p.setStrokeColor(ColorName.BLACK);
            p.setRotation(Math.PI * -0.75);
            p.setX(sz / 2);
            p.setY(sz / 2);
            shape.add(p);

            c = new Circle(r);
            c.setStrokeColor(ColorName.LIGHTBLUE);
//            c.setFillColor(Color.fromColorString("#93DEFF"));
//            c.setFillAlpha(0.4);
            c.setX(sz / 2);
            c.setY(sz / 2);
            shape.add(c);

            return shape;
        }
        
//        @Override
//        public void zap()
//        {
//            if (m_popped)
//                super.zap();
//        }
        
        @Override
        public void explode(Integer color, int chainSize)
        {
            if (m_popped || (item != null && item.canExplodeNextTo()))
            {
                super.explode(color, chainSize);
                return;
            }
            
            ctx.score.explodedBubble();                    
            ctx.doorLayer.remove(m_shape);
            m_popped = true;
            
            super.explode(color, chainSize);
        }
        
        @Override
        public Cell copy()
        {
            return new Bubble();
        }
    }
    
    public static class Slide extends Cell
    {
        public boolean m_toLeft;
        private Polygon m_shape;
        
        public Slide(boolean toLeft)
        {
            m_toLeft = toLeft;
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            double sz = ctx.cfg.size;
            double f = 0.8 * sz;
            double b = (1 - f) / 2;
            
            Polygon p = new Polygon(m_toLeft ? 1 - b : b, b, b, 1 - b, 1 - b, 1 - b);
            p.setX(m_x);
            p.setY(m_y);            
            p.setStrokeWidth(4);
            p.setLineJoin(LineJoin.ROUND);
            p.setStrokeColor(ColorName.DARKMAGENTA);
            ctx.backgroundLayer.add(p);
            
            m_shape = p;
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.backgroundLayer.remove(m_shape);
        }

        @Override
        public boolean canContainItems()
        {
            return false;
        }
        
        @Override
        public boolean canBeFilled()
        {
            return false;
        }
        
        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public Cell copy()
        {
            Slide c = new Slide(m_toLeft);
            c.copyValues(this);
            return c;
        }

        public boolean isToLeft()
        {
            return m_toLeft;
        }
    }
    
    public static class CircuitCell extends Cell
    {
        public enum State { ON, OFF, DONE };
        
        public State state = State.ON;
        
        private Group m_onShape;
        private Group m_offShape;

        private Rectangle m_offDash;
        
        @Override
        public Cell copy()
        {
            CircuitCell c = new CircuitCell();
            c.copyValues(this);
            return c;
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            double sz = ctx.cfg.size;
            
            m_onShape = createOnShape(sz);
            m_onShape.setX(m_x);
            m_onShape.setY(m_y);
            
            m_offShape = new Group();
            
            double w = sz * 0.2;            
            double h = sz * 0.08;
            double sz1 = sz - w + h/2 - 2;
            
            Rectangle r = new Rectangle(sz1, sz1);
            r.setStrokeWidth(h);
            r.setStrokeColor(ColorName.DARKBLUE);
            r.setX((w - sz) / 2);
            r.setY((w - sz) / 2);
            r.setLineCap(LineCap.SQUARE);
            r.setDashArray(5, 5);
            
            m_offDash = r;
            
            m_offShape.add(r);
            m_offShape.setX(m_x);
            m_offShape.setY(m_y);
                        
            m_offShape.setVisible(false);
            
            ctx.backgroundLayer.add(m_onShape);
            ctx.backgroundLayer.add(m_offShape);
        }
        
        @Override
        public void animate(long t, double cursorX, double cursorY)
        {
            if (state == State.OFF)
            {
                int n = (int) ((t / 50) % 10);
                m_offDash.setDashOffset(9 - n);
            }
            
            super.animate(t, cursorX, cursorY);
        }
        
        public Group createOnShape(double sz)
        {
            Group g = new Group();
            
            double w = sz * 0.2;
            double w2 = w * 2;
            Rectangle r = new Rectangle(sz - w2, sz - w2);
            r.setStrokeWidth(w2);
            r.setStrokeColor(ColorName.DARKBLUE);
            r.setX((w2 - sz) / 2);
            r.setY((w2 - sz) / 2);
            g.add(r);
            
            double h = sz * 0.08;
            double sz1 = sz - w + h/2 - 2;
            r = new Rectangle(sz1, sz1);
            r.setStrokeWidth(h);
            r.setStrokeColor(ColorName.WHITE);
            r.setX((w - sz) / 2);
            r.setY((w - sz) / 2);
            r.setLineCap(LineCap.SQUARE);
            r.setDashArray(5, 5);
            g.add(r);
            
            return g;
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            
            ctx.backgroundLayer.remove(m_onShape);
            ctx.backgroundLayer.remove(m_offShape);
        }
        
        public void explode(Integer color, int chainSize)
        {
            if (state != State.DONE)
            {
                state = state == State.ON ? State.OFF : State.ON;
                m_onShape.setVisible(state == State.ON);
                m_offShape.setVisible(state == State.OFF);
            }
            
            super.explode(color, chainSize);
        }

        public void setDone()
        {
            state = State.DONE;
            m_onShape.setVisible(false);
            m_offShape.setVisible(false);
        }
        
        @Override
        public boolean stopsLaser()
        {
            return state != State.DONE || itemStopsLaser();
        }
    }
}
