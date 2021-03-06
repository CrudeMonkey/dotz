package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Polygon;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Slice;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.Triangle;
import com.ait.lienzo.client.core.types.BoundingBox;
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
import com.enno.dotz.client.Generator.ItemFrequency;
import com.enno.dotz.client.SlotMachines.SlotMachineInfo;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.editor.ModePalette.RadioActive;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Explody;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Item.ExplodeAction;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.util.Console;
import com.enno.dotz.client.util.FrequencyGenerator;
import com.google.gwt.dom.client.Style.FontWeight;

public abstract class Cell
{
    // some Cell type identifiers used when undoing Bubble Machine transformations
    public static final int UNKNOWN_TYPE = 0;
    public static final int ITEM_CELL = 1;
    public static final int BUBBLE = 2;
    
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
    
    public int getType()
    {
        return UNKNOWN_TYPE;
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
     * Used by layout editor and MoveAnimals.
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

    public boolean isLockedBlockingCage()
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

    /**
     * @return Whether dots fall thru this cell (e.g. Hole)
     */
    public boolean isFallThru()
    {
        return false;
    }
    
    public String toString()
    {
        return col + "," + row;
    }

    public void initGraphics(int col, int row, double x, double y)
    {
        this.col = col;
        this.row = row;
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
                RandomItem rnd = (RandomItem) item;
                item = ctx.generator.getNextItem(ctx, false, rnd.isRadioActive(), rnd.isStuck());
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

    public CellState copyState()
    {
        return new CellState(item, ice, getType());
    }
    
    public void restoreState(CellState state)
    {
        ice = state.ice;
        updateIce();
        
        if (item != null)
        {
            item.removeShapeFromLayer(ctx.dotLayer);
            item = null;
        }
            
        if (state.item != null)
        {
            item = state.item.copy();
            item.init(ctx);
            item.moveShape(ctx.state.x(col), ctx.state.y(row));
            item.addShapeToLayer(ctx.dotLayer);
        }
    }
    
    public static class CellState
    {
        Item item;
        int ice;
        int type;
        
        public CellState(Item item, int ice)
        {
            this(item, ice, UNKNOWN_TYPE);
        }
        
        public CellState(Item item, int ice, int type)
        {
            this.type = type;
            this.ice = ice;
            
            if (item != null)
                this.item = item.copy();
        }
   }
    
    public static class ItemCell extends Cell
    {
        public ItemCell()
        {            
        }
        
        @Override
        public int getType()
        {
            return ITEM_CELL;
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
            ctx.backgroundLayer.redraw();
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
        private static IColor HOLE_COLOR = new Color(80, 80, 80);
        
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
            
            Group g = new Group();
            g.setX(m_x - sz/2 + 1);
            g.setY(m_y - sz/2 + 1);            
            
            Rectangle r = new Rectangle(sz-2, sz-2);
            r.setFillColor(HOLE_COLOR);
            g.add(r);
            
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

        @Override
        public boolean isFallThru()
        {
            return true;
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
        private static IColor ROCK_COLOR = new Color(235, 235, 235);
        
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
            
            Group g = new Group();
            g.setX(m_x - sz/2 + 1);
            g.setY(m_y - sz/2 + 1);
            
            Rectangle r = new Rectangle(sz - 2, sz - 2);
            r.setFillColor(ROCK_COLOR);
            g.add(r);
            
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
        public static class State extends CellState
        {
            public int strength;
            public int direction;
            public int time;

            public State(Item item, int ice, int strength, int direction, Controller controller)
            {
                super(item, ice);
                this.strength = strength;
                this.direction = direction;
                
                if (controller != null)
                    time = controller.getTime();
            }
        }
        
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
        
        @Override
        public CellState copyState()
        {
            return new State(item, ice, m_strength, m_direction, m_controller);
        }
        
        @Override
        public void restoreState(CellState state)
        {
            super.restoreState(state);
            
            State s = (State) state;
            m_strength = s.strength;
            m_direction = s.direction;
            if (m_controller != null)
                m_controller.setTime(s.time);
            
            updateStrength();
            
            if (m_direction != Direction.NONE)
            {
                setRotation();
                m_neighbor = determineNeighbor(col, row);
            }
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
            ctx.doorLayer.redraw();
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
                    m_shape.setVisible(false);
                    
                    // don't explode item!
                }
                
                updateStrength();
            }
        }

        protected void updateStrength()
        {
            m_text.setText("" + m_strength);
            m_text.setVisible(m_strength > 1);
            
            m_shape.setVisible(m_strength > 0);
            
            ctx.doorLayer.redraw();
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
            m_shape.setVisible(false);
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
            
            ctx.doorLayer.redraw();
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
        public static class State extends CellState
        {
            public int strength;
            public int time;

            public State(Item item, int ice, int strength, Controller controller)
            {
                super(item, ice);
                this.strength = strength;
                
                if (controller != null)
                    time = controller.getTime();
            }
        }
        
        protected int m_strength;
        
        private Group m_shape;
        private Text m_text;
        protected IColor m_color = ColorName.BLACK;
        
        private boolean m_blocking;
        private boolean m_blinking;
        private Controller m_controller;
        
        private Line[] m_offDash = new Line[3];
        
        public Cage(int strength, boolean blocking)
        {
            m_strength = strength;
            m_blocking = blocking;
        }
        
        /** Blinking cage */
        public Cage(String sequence)
        {
            m_strength = 1;
            m_controller = new Controller(sequence);
            m_blinking = true;
        }
        
        @Override
        public CellState copyState()
        {
            return new State(item, ice, m_strength, m_controller);
        }
        
        @Override
        public void restoreState(CellState state)
        {
            super.restoreState(state);
            
            State s = (State) state;
            m_strength = s.strength;
            if (m_controller != null)
                m_controller.setTime(s.time);
            
            updateStrength();
            
        }
        
        public boolean isBlinking()
        {
            return m_blinking;
        }
        
        public boolean isBlocking()
        {
            return m_blocking;
        }
        
        @Override
        public boolean isFallThru()
        {
            return !isLockedBlockingCage();
        }
        
        @Override
        public boolean hasController()
        {
            return m_blinking;
        }

        @Override
        public Cell copy()
        {
            Cage c = m_blinking ? new Cage(m_controller.getSequence()) : new Cage(m_strength, m_blocking);
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
        
        public boolean isLockedBlockingCage()
        {
            return isLocked() && isBlocking();
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
            m_shape.setVisible(false);
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
                    m_shape.setVisible(false);
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
            
            if (m_blocking)
            {
                for (int i = 1; i < 4; i++)
                {
                    Line line = new Line(b, b + i * w, sz - b, b + i * w);
                    line.setStrokeColor(m_color);
                    
                    shape.add(line);
                }
            }
            else
            {
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
            
            ctx.doorLayer.redraw();
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
        public static class State extends CellState
        {
            public boolean popped;

            public State(Item item, int ice, boolean popped)
            {
                super(item, ice, BUBBLE);
                this.popped = popped;
            }
        }
        
        private boolean m_popped;
        private Group m_shape;
        
        @Override
        public int getType()
        {
            return BUBBLE;
        }
        
        @Override
        public CellState copyState()
        {
            return new State(item, ice, m_popped);
        }
        
        @Override
        public void restoreState(CellState state)
        {
            super.restoreState(state);
            
            State s = (State) state;
            m_popped = s.popped;
            
            m_shape.setVisible(!m_popped);
        }
        
        public boolean isPopped()
        {
            return m_popped;
        }
        
        @Override
        public boolean isUnpoppedBubble()
        {
            return !m_popped;
        }
        
//        @Override
//        public boolean canGrowFire()
//        {
//            if (!m_popped)
//                return false;
//            
//            return super.canGrowFire();
//        }
        
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
            m_shape.setVisible(false);
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
        public static class State extends CellState
        {
            public OnOff onOff;

            public State(Item item, int ice, OnOff onOff)
            {
                super(item, ice);
                this.onOff = onOff;
            }
        }
        
        public enum OnOff { ON, OFF, DONE };
        
        public OnOff state = OnOff.ON;
        
        private Group m_onShape;
        private Group m_offShape;

        private Rectangle m_offDash;
        
        public CellState copyState()
        {
            return new State(item, ice, state);
        }
        
        @Override
        public void restoreState(CellState cellState)
        {
            super.restoreState(cellState);
            
            State s = (State) cellState;
            state = s.onOff;
            
            m_onShape.setVisible(state == OnOff.ON);
            m_offShape.setVisible(state == OnOff.OFF);
        }
        
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
            if (state == OnOff.OFF)
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
            if (state != OnOff.DONE)
            {
                state = state == OnOff.ON ? OnOff.OFF : OnOff.ON;
                m_onShape.setVisible(state == OnOff.ON);
                m_offShape.setVisible(state == OnOff.OFF);
            }
            
            super.explode(color, chainSize);
        }

        public void setDone()
        {
            state = OnOff.DONE;
            m_onShape.setVisible(false);
            m_offShape.setVisible(false);
        }
        
        @Override
        public boolean stopsLaser()
        {
            return state != OnOff.DONE || itemStopsLaser();
        }
    }
    
    public static class Machine extends Cell
    {
        public static class State extends CellState
        {
            public int stage;

            public State(Item item, int ice, int stage)
            {
                super(item, ice);
                this.stage = stage;
            }
        }
        
        public static enum MachineType
        {
            ITEM("Item"),
            BOMBIFY("Bombify"),
            WRAP("Wrap"),
            STRIPE("Stripe"),
            MULTIPLIER("Multiplier"),
            RADIATE("Radiate"),
            BUBBLE("Bubble"),
            COIN("Coin");
            
            public String name;
            
            MachineType(String name)
            {
                this.name = name;
            }
            
            public static MachineType find(String name)
            {
                for (MachineType t : values())
                {
                    if (t.name.equals(name))
                        return t;
                }
                return null;
            }
            
            public static LinkedHashMap<String,String> getValueMap()
            {
                LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
                for (MachineType t : values())
                {
                    map.put(t.name, t.name);
                }
                return map;
            }
        }
        
        public static enum MachineTrigger
        {
            ZAP_AND_CHAIN("Zap/Chain"),
            ZAP("Zap"),
            CHAIN("Chain");
            
            public String name;
            
            MachineTrigger(String name)
            {
                this.name = name;
            }
            
            public static MachineTrigger find(String name)
            {
                for (MachineTrigger t : values())
                {
                    if (t.name.equals(name))
                        return t;
                }
                return null;
            }
            
            public static LinkedHashMap<String,String> getValueMap()
            {
                LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
                for (MachineTrigger t : values())
                {
                    map.put(t.name, t.name);
                }
                return map;
            }
        }
        
        private int m_howMany;
        private int m_every;
        private MachineTrigger m_trigger;
        private MachineType m_type;
        private Item m_launchItem;
         
        private Group m_shape;
        private Line[] m_lines;
        
        private int m_stage;
        private Text m_howManyText;
        
        private String m_coinFrequencies;
        private double[] m_coinFreqChance;
       
        public Machine()
        {
            m_every = 3;
            m_howMany = 1;
            m_stage = m_every;
            m_type = MachineType.ITEM;
            m_trigger = MachineTrigger.ZAP_AND_CHAIN;
            m_launchItem = new Fire(false);
        }
        
        public Machine(String type, Item item, int every, int howMany, String trigger, String coinFrequencies)
        {
           m_type = MachineType.find(type);
           m_launchItem = item;
           m_every = every;
           m_howMany = howMany;
           m_trigger = MachineTrigger.find(trigger);
           
           if (m_type == MachineType.COIN)
               setCoinFrequencies(coinFrequencies == null ? Coin.DEFAULT_COIN_FREQ : coinFrequencies);           
        }
        
        public String getCoinFrequencies()
        {
            return m_coinFrequencies;
        }
        
        public void setCoinFrequencies(String coinFrequencies)
        {
            m_coinFrequencies = coinFrequencies;
            m_coinFreqChance = parseCoinFrequencies(coinFrequencies);
        }
        
        public int nextCoinAmount(Random rnd)
        {
            double d = rnd.nextDouble();
            
            double totalChance = 0;
            for (int i = 0; i < Coin.NUM_COINS - 1; i++)
            {
                totalChance += m_coinFreqChance[i];
                if (d < totalChance)
                    return Coin.COIN_DENOMINATION[i];
            }
            return Coin.COIN_DENOMINATION[Coin.NUM_COINS - 1];
        }
        
        public static double[] parseCoinFrequencies(String coinFrequencies)
        {
            if (coinFrequencies == null || coinFrequencies.isEmpty())
                return null;
            
            double[] cf = new double[Coin.NUM_COINS];
            int i = 0;
            double total = 0;
            for (String s : coinFrequencies.split(","))
            {
                if (i >= Coin.NUM_COINS)
                    throw new RuntimeException("too many values");
                
                cf[i] = Double.parseDouble(s.trim());
                total += cf[i];
                i++;
            }
            
            if (i < Coin.NUM_COINS)
                throw new RuntimeException("must specify 3 values");
            
            if (total == 0)
                throw new RuntimeException("total can't be zero");
            
            // normalize
            for (i = 0; i < cf.length; i++)
            {
                cf[i] = cf[i] / total;
            }
            
            return cf;
        }

        public CellState copyState()
        {
            return new State(item, ice, m_stage);
        }
        
        @Override
        public void restoreState(CellState state)
        {
            super.restoreState(state);
            
            State s = (State) state;
            m_stage = s.stage;
            
            updateStage();
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape(ctx.cfg.size);
            
            if (ctx.isEditing || ctx.isPreview)
                m_stage = m_every;
            
            updateStage();
            ctx.doorLayer.add(m_shape);
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.doorLayer.remove(m_shape);
        }

        public MachineType getMachineType()
        {
            return m_type;
        }

        public Item getLaunchItem()
        {
            return m_launchItem;
        }
        
        public int getHowMany()
        {
            return m_howMany;
        }

        public void setHowMany(int howMany)
        {
            m_howMany = howMany;
        }

        public int getEvery()
        {
            return m_every;
        }

        public void setEvery(int every)
        {
            m_every = every;
        }

        public MachineTrigger getTrigger()
        {
            return m_trigger;
        }
        
        @Override
        public boolean isFallThru()
        {
            return true;
        }
        
        @Override
        public boolean canDrop()
        {
            return false;
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
        public boolean canGrowFire()
        {
            return false;
        }
        
        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public boolean canExplode(Integer color)
        {
            return false;
        }
        
        @Override
        public boolean canBeNuked()
        {
            return m_trigger == MachineTrigger.ZAP || m_trigger == MachineTrigger.ZAP_AND_CHAIN;
        }
        
        @Override
        public boolean canConnect(Integer color, boolean isWordMode)
        {
            return false;
        }
        
        @Override
        public boolean canExplodeNextTo(Collection<Cell> cells, Integer color)
        {
            return m_trigger == MachineTrigger.CHAIN || m_trigger == MachineTrigger.ZAP_AND_CHAIN;
        }
        
        @Override
        public void zap()
        {
            if (m_stage < m_every)
            {
                m_stage++;
                updateStage();
            }
        }
        
        @Override
        public void explode(Integer color, int chainSize)
        {
            zap();
        }
        
        public boolean isTriggered()
        {
            return m_stage == m_every;
        }
        
        public void clearStage()
        {
            m_stage = 0;
            updateStage();
        }
        
        protected void updateStage()
        {
            for (int i = 0; i < m_every; i++)
            {
                m_lines[i].setVisible(i < m_stage);
            }
        }
        
        public Group createShape(double sz)
        {            
            double strokeWidth = 4;
            double sw2 = strokeWidth / 2;
            double w = sz * 0.8;
            double h = sz * 0.75;
            
            Group g = new Group();
            g.setX(m_x - w / 2);
            g.setY(m_y - h / 2 - sw2);
            
            double x = w / 2;
            if (m_type == MachineType.ITEM || m_type == MachineType.BOMBIFY || m_type == MachineType.COIN)
            {
                IPrimitive<?> item = m_launchItem.createShape(sz * 0.6);
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            else if (m_type == MachineType.BUBBLE)
            {
                IPrimitive<?> item = new Bubble().createShape(sz * 0.6);
                x -= sz * 0.3;
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            else if (m_type == MachineType.WRAP)
            {
                IPrimitive<?> item = new WrappedDot(0).createShape(sz * 0.6);
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            else if (m_type == MachineType.STRIPE)
            {
                IPrimitive<?> item = new Striped(0, false).createShape(sz * 0.6);
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            else if (m_type == MachineType.MULTIPLIER)
            {
                IPrimitive<?> item = Dot.getMultiplierShapeForMachine();
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            else if (m_type == MachineType.RADIATE)
            {
                IPrimitive<?> item = RadioActive.createRadioActiveShape(sz * 0.5);
                item.setX(x);
                item.setY(x);
                g.add(item);
            }
            
            Rectangle r = new Rectangle(w, h);
            r.setStrokeColor(ColorName.GRAY);
            r.setStrokeWidth(strokeWidth);
            g.add(r);
            
            r = new Rectangle(w, sz * 0.05);
            r.setStrokeColor(ColorName.GRAY);
            r.setStrokeWidth(strokeWidth);
            g.add(r);
            
            for (int i = -1; i <= 1; i += 2) // legs
            {
                Slice s = new Slice(sz * 0.08, 0, Math.PI);
                s.setFillColor(new Color(100, 100, 100));
                s.setX(w / 2 + i * sz * 0.25);
                s.setY(h);
                g.add(s);
            }
            
            m_lines = new Line[m_every];
            
            double lw = w / m_every;
            double lwm = lw * 0.1;
            double ly = 1;
            for (int i = 0; i < m_every; i++)
            {
                Line l = new Line(i * lw + lwm, ly, i * lw + lw - lwm, ly);
                l.setStrokeWidth(strokeWidth * 0.5);
                l.setStrokeColor(ColorName.YELLOW);
                
                m_lines[i] = l;                
                g.add(l);
            }
            
            m_howManyText = new Text("" + m_howMany);
            m_howManyText.setFillColor(ColorName.BLACK);
            m_howManyText.setFontSize(7);
            m_howManyText.setFontStyle(FontWeight.BOLD.getCssName());
            m_howManyText.setTextAlign(TextAlign.CENTER);
            m_howManyText.setTextBaseLine(TextBaseLine.MIDDLE);
            
            m_howManyText.setX(sz * 0.65);
            m_howManyText.setY(sz * 0.62);
            g.add(m_howManyText);
            
            m_howManyText.setVisible(m_howMany > 1);
            
            return g;
        }
        
        @Override
        public Cell copy()
        {
            return new Machine(m_type.name, m_launchItem == null ? null : m_launchItem.copy(), m_every, m_howMany, m_trigger.name, getCoinFrequencies());
        }

        public Item createItem(Context ctx, Cell target)
        {
            if (m_type == MachineType.ITEM)
            {
                if (m_launchItem instanceof RandomItem)
                {
                    RandomItem rnd = (RandomItem) m_launchItem;
                    return ctx.generator.getNextItem(ctx, false, rnd.isRadioActive(), rnd.isStuck());
                }
                
                Item item = m_launchItem.copy();
                
                if (item instanceof Rocket)
                    ((Rocket) item).setDirection(Direction.randomDirection(ctx.generator.getRandom()));
                else if (item instanceof Blaster)
                    ((Blaster) item).setVertical(ctx.generator.getRandom().nextBoolean());
                else if (item instanceof Animal && !((Animal) item).isBlack())
                    ((Animal) item).setColor(ctx.generator.getNextDotColor());
                
                return item;
            }
            else if (m_type == MachineType.COIN)
            {
                Item item = m_launchItem.copy();                
                ((Coin) item).setAmount(nextCoinAmount(ctx.generator.getRandom()));
                return item;
            }
            else if (m_type == MachineType.BOMBIFY)
            {
                DotBomb b = (DotBomb) m_launchItem;
                Dot dot = (Dot) target.item;
                
                Dot newDot = dot.copy();
                return new DotBomb(newDot, b.getStrength(), dot.isStuck());
            }
            else if (m_type == MachineType.WRAP)
            {
                Dot dot = (Dot) target.item;
                return new WrappedDot(dot.color);
            }
            else if (m_type == MachineType.STRIPE)
            {
                Dot dot = (Dot) target.item;
                boolean vertical = ctx.generator.getRandom().nextBoolean();
                return new Striped(dot.color, vertical);
            }
            else if (m_type == MachineType.MULTIPLIER)
            {
                return Dot.upgradeMultiplier(target.item, ctx);
            }
            else if (m_type == MachineType.RADIATE)
            {
                Item newItem = target.item.copy();
                Dot dot = null;
                if (newItem instanceof Dot)
                    dot = (Dot) newItem;
                else //if (newItem instanceof DotBomb)
                    dot = ((DotBomb) newItem).getDot();
                
                dot.setRadioActive(true);
                return newItem;
            }
            
            throw new RuntimeException("unexpected MachineType " + m_type.name);
        }

        public boolean isCoinMachine()
        {
            return m_type == MachineType.COIN;
        }

        public boolean canTargetCell(Cell cell)
        {
            if (m_type == MachineType.ITEM)
            {
                if (cell.isLocked() || !cell.canContainItems())
                    return false;
                
                if (cell.item == null)
                    return true;
                
                if (!cell.item.canBeReplaced())
                    return false;
            
                return true;
            }
            else if (m_type == MachineType.BUBBLE)
            {
                return cell instanceof ItemCell;
            }
            else if (m_type == MachineType.BOMBIFY || m_type == MachineType.WRAP || m_type == MachineType.STRIPE)
            {
                if (cell.isLocked() || !cell.canContainItems())
                    return false;
                
                if (cell.item == null)
                    return false;       // or do we just generate a random one?
                
                return cell.item instanceof Dot;
            }
            else if (m_type == MachineType.MULTIPLIER || m_type == MachineType.RADIATE)
            {
                if (cell.isLocked() || !cell.canContainItems())
                    return false;
                
                if (cell.item == null)
                    return false;
                
                return cell.item instanceof Dot;
            }
            else if (m_type == MachineType.RADIATE)
            {
                if (cell.isLocked() || !cell.canContainItems())
                    return false;
                
                if (cell.item == null)
                    return false;
                
                Dot dot = null;
                if (cell.item instanceof Dot)
                {
                    dot = (Dot) cell.item;
                }
                else if (cell.item instanceof DotBomb)
                {
                    dot = ((DotBomb) cell.item).getDot();
                }
                else
                    return false;
                
                return !dot.isRadioActive();
            }
            return false;
        }

        public boolean canToggleStuck()
        {
            if (m_type != MachineType.ITEM)
                return false;
            
            return true;
        }

        public Machine toggleStuck()
        {
            Machine m = (Machine) copy();
            if (m.m_launchItem != null)
                m.m_launchItem.setStuck(!m.m_launchItem.isStuck());
            return m;
        }
    }
    
    public static class Slot extends Cell
    {
        public static class State extends CellState
        {
            public boolean hold;
            public boolean canHold;
            public int currShapeIndex;
            public int nextShapeIndex;

            public State(boolean hold, boolean canHold, int currShapeIndex, int nextShapeIndex)
            {
                super(null, 0);
                
                this.hold = hold;
                this.canHold = canHold;
                this.currShapeIndex = currShapeIndex;
                this.nextShapeIndex = nextShapeIndex;
            }
        }
        
        public static final int DT = 250;   // time to roll one slot in ms.
        public static final int SPINS = 8;

        private IPrimitive<?> m_shape;
        private Group m_clipBox;
        private List<IPrimitive<?>> m_shapes;

        private int m_currShapeIndex = -1;
        private int m_nextShapeIndex = -1;

        private IPrimitive<?> m_currShape;
        private IPrimitive<?> m_nextShape;

        private long m_startTime;

        private FrequencyGenerator<Integer> m_freqGen;
        private List<Item> m_possibleItems = new ArrayList<Item>();

        private Text m_holdText;
        private boolean m_hold;
        private boolean m_canHold;

        private int m_spins;

        private SlotMachineInfo m_info;
        private Group m_winShape;
        
        public SlotMachineInfo getSlotMachineInfo()
        {
            return m_info;
        }

        public void setSlotMachineInfo(SlotMachineInfo info)
        {
            m_info = info;
        }
        
        public boolean isHold()
        {
            return m_hold;
        }

        public void setHold(boolean hold)
        {
            m_hold = hold;
            m_holdText.setVisible(hold);
        }

        public void toggleHold()
        {
            setHold(!isHold());
        }
        
        public void setCanHold(boolean canHold)
        {
            m_canHold = canHold;
        }
        
        public boolean canHold()
        {
            return m_canHold;
        }
        
        @Override
        public boolean canDrop()
        {
            return false;
        }
        
        @Override
        public boolean isFallThru()
        {
            return true;
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
        public boolean canGrowFire()
        {
            return false;
        }
        
        @Override
        public boolean canHaveIce()
        {
            return false;
        }
        
        @Override
        public boolean canExplode(Integer color)
        {
            return false;
        }
        
        @Override
        public boolean canBeNuked()
        {
            return true;
        }
        
        @Override
        public boolean canConnect(Integer color, boolean isWordMode)
        {
            return false;
        }
        
        @Override
        public boolean canExplodeNextTo(Collection<Cell> cells, Integer color)
        {
            return true;
        }
        
        @Override
        public void zap()
        {
            ctx.state.getSlotMachines().triggeredSlot(this);
        }
        
        @Override
        public void explode(Integer color, int chainSize)
        {
            zap();
        }
           
        @Override
        public Cell copy()
        {
            Slot c = new Slot();
            c.copyValues(this);
            c.m_info = m_info; // no need to deep copy
            return c;
        }
        
        @Override
        public State copyState()
        {
            return new State(m_hold, m_canHold, m_currShapeIndex, m_nextShapeIndex);
        }
        
        @Override
        public void restoreState(CellState state)
        {
            super.restoreState(state);
            
            State s = (State) state;
            setHold(s.hold);
            setCanHold(s.canHold);
            
            if (m_currShape != null)
                m_clipBox.remove(m_currShape);
            if (m_nextShape != null)
                m_clipBox.remove(m_nextShape);
            
            m_currShapeIndex = s.currShapeIndex;
            if (m_currShapeIndex != -1)
            {
                m_currShape = m_shapes.get(m_currShapeIndex);
                m_currShape.setY(ctx.cfg.size / 2);
                m_clipBox.add(m_currShape);
            }
            
            m_nextShapeIndex = s.nextShapeIndex;
            if (m_nextShapeIndex != -1)
            {
                m_nextShape = m_shapes.get(m_nextShapeIndex);
                m_nextShape.setY(ctx.cfg.size * -0.5);
                m_clipBox.add(m_nextShape);
            }
        }
        
        @Override
        public void initGraphics(int col, int row, double x, double y)
        {
            super.initGraphics(col, row, x, y);
            
            m_shape = createShape(ctx.cfg.size);
            
//            if (!ctx.isEditing && !ctx.isPreview)
//                setup();
            
//            if (ctx.isEditing || ctx.isPreview)
//                m_stage = m_every;
//            
//            updateStage();
            ctx.doorLayer.add(m_shape);
        }
        
        @Override
        public void removeGraphics()
        {
            super.removeGraphics();
            ctx.doorLayer.remove(m_shape);
        }
        
        private IPrimitive<?> createShape(int sz)
        {
            Group g = new Group();
            g.setX(m_x - sz / 2);
            g.setY(m_y - sz / 2);
            
            Rectangle r = new Rectangle(sz, sz);
            r.setFillColor(ColorName.BLACK);
//            r.setX(1);
//            r.setY(1);
            g.add(r); 
            
            double bx = 2;
            double by = 4;
            double d = sz * 0.1;
            double r1 = sz * 0.9;
            double r2 = sz * 1.2;
            MultiPath p = new MultiPath();
            p.M(d + bx, by);
            p.L(sz - 1 - bx, by);
            p.A(r1, r2, 0, 0, 0, sz - 1 - bx, sz - 1 - by);
            p.L(d + bx, sz - 1 - by);
            p.A(r1, r2, 0, 0, 1, d + bx, by);
            p.Z();
            
            p.setStrokeColor(ColorName.DARKGRAY);
            p.setStrokeWidth(2);
            p.setFillColor(ColorName.ANTIQUEWHITE);
            g.add(p);
            
            m_clipBox = new Group();
            m_clipBox.setPathClipper(new BoundingBox(1, by + 1, sz - 2, sz - by - 2));
            g.add(m_clipBox);
            
//            r = new Rectangle(sz - 3, sz - 3);
//            r.setStrokeColor(ColorName.GREEN);
//            r.setStrokeWidth(3);
//            r.setCornerRadius(4);
//            r.setX(1);
//            r.setY(1);
            //g.add(r);            
            
            Line l = new Line(bx, sz * 0.5, sz - 1 - bx, sz * 0.5);
            l.setAlpha(0.5);
            l.setStrokeColor(ColorName.BLACK);
            g.add(l);
            
            m_holdText = new Text("HOLD");
            m_holdText.setFontSize(8);
            m_holdText.setFontStyle("bold");
            m_holdText.setTextAlign(TextAlign.CENTER);
            m_holdText.setTextBaseLine(TextBaseLine.MIDDLE);
            m_holdText.setFillColor(ColorName.RED);
            m_holdText.setX(sz * 0.5);
            m_holdText.setY(sz * 0.5);
            g.add(m_holdText);
            
            m_winShape = new Group();
            m_winShape.setX(sz / 2);
            m_winShape.setY(sz / 2);
            
            double ww = sz * 0.6;
            double wh = sz * 0.3;
            Rectangle wr = new Rectangle(ww, wh);
            wr.setX(-ww / 2);
            wr.setY(-wh / 2);
            wr.setFillColor(ColorName.RED);
            m_winShape.add(wr);
            
            Text winText = new Text("WIN");
            winText.setFontSize(9);
            winText.setFontStyle("bold");
            winText.setTextAlign(TextAlign.CENTER);
            winText.setTextBaseLine(TextBaseLine.MIDDLE);
            winText.setFillColor(ColorName.WHITE);
            m_winShape.add(winText);
            
            m_winShape.setVisible(false);
            g.add(m_winShape);
            return g;
        }
        
        public void setup()
        {
            SlotMachineInfo info = m_info == null ? SlotMachineInfo.createDefaultInfo() : m_info;
            
            m_freqGen = new FrequencyGenerator<Integer>();
            
            List<IPrimitive<?>> shapes = new ArrayList<IPrimitive<?>>();
            for (ItemFrequency f : info.frequencies)
            {
                addShape(shapes, f.frequency, f.item);
            }
            m_shapes = shapes;
            
            Random rnd = ctx.generator.getRandom();
            m_currShapeIndex = getNextShapeIndex(rnd);
            m_currShape = m_shapes.get(m_currShapeIndex);
            m_currShape.setY(ctx.cfg.size / 2);
            m_clipBox.add(m_currShape);
            
            m_nextShapeIndex = m_currShapeIndex;    // don't generate this index next            
            m_nextShapeIndex = getNextShapeIndex(rnd);
            m_nextShape = m_shapes.get(m_nextShapeIndex);
            m_nextShape.setY(ctx.cfg.size * -0.5);
            m_clipBox.add(m_nextShape);
            
            m_startTime = System.currentTimeMillis();
        }
        
        public void spin(int spins)
        {
            m_spins = spins;
            m_startTime = System.currentTimeMillis();
        }
        
        public void updateSpin(long t)
        {
            long dt = t - m_startTime;
            boolean past = dt >= DT;
            if (!past || m_spins > 0)
            {
                tick(t);
            }
        }
        
        public void endSpin()
        {
            pullNextItem();
            m_currShape.setY(ctx.cfg.size * 0.5);
            m_nextShape.setY(ctx.cfg.size * -0.5);
        }
        
        @Override
        public void animate(long t, double cursorX, double cursorY)
        {
            //tick(t);
        }
        
        private void tick(long t)
        {
            long dt = t - m_startTime;
            boolean past = dt >= DT;
            double f = (dt % DT) / (double) DT;
            
            if (past)
            {
                m_spins--;
                
//                Console.log("past=" + past + " dt=" + dt + " f=" + f);
                m_startTime += (dt / DT) * DT;
                
//                dt = t - m_startTime;
//                Console.log("newdt=" + dt);
                
                
                pullNextItem();
            }
            
            m_currShape.setY(ctx.cfg.size * (f + 0.5));
            m_nextShape.setY(ctx.cfg.size * (f - 0.5));
        }

        protected void pullNextItem()
        {
            m_clipBox.remove(m_currShape);
            m_currShape = m_nextShape;
            m_currShapeIndex = m_nextShapeIndex;
            
            int i = getNextShapeIndex(ctx.generator.getRandom());
            m_nextShape = m_shapes.get(i);
            m_nextShapeIndex = i;
            
            m_clipBox.add(m_nextShape);
        }
        
        private int getNextShapeIndex(Random rnd)
        {
            int i;
            do
            {
                i = m_freqGen.next(rnd);
            }
            while (i == m_nextShapeIndex);
//            Console.log("next slot " + i);
            return i;
        }
        
        private void addShape(List<IPrimitive<?>> shapes, double freq, Item item)
        {
            m_freqGen.add(freq, shapes.size());
            m_possibleItems.add(item);
            
            item.init(ctx);
            IPrimitive<?> shape = item.createShape(ctx.cfg.size);
            shape.setX(ctx.cfg.size / 2);
            shapes.add(shape);
        }

        public Item getCurrentItem()
        {
            return m_possibleItems.get(m_currShapeIndex);
        }

        public void setWinning(boolean winning)
        {
            m_startTime = System.currentTimeMillis();
            m_winShape.setVisible(winning);
        }
        
        private static final long BLINK_INTERVAL = 200;
        
        public void updateWinning()
        {
            long dt = System.currentTimeMillis() - m_startTime;
            m_winShape.setVisible((dt / BLINK_INTERVAL) % 2 == 0);
        }
    }
}
