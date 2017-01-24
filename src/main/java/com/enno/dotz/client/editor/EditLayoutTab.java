package com.enno.dotz.client.editor;

import java.util.Random;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Arrow;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ArrowType;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Cell;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.ConveyorCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.Cell.Hole;
import com.enno.dotz.client.Cell.Machine;
import com.enno.dotz.client.Cell.Machine.MachineType;
import com.enno.dotz.client.Cell.Rock;
import com.enno.dotz.client.Cell.Slide;
import com.enno.dotz.client.Cell.Teleport;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Controller.Controllable;
import com.enno.dotz.client.Conveyors;
import com.enno.dotz.client.Conveyors.ConveyorException;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.Generator;
import com.enno.dotz.client.GridState;
import com.enno.dotz.client.anim.Pt;
import com.enno.dotz.client.editor.EditLevelDialog.ChangeListener;
import com.enno.dotz.client.editor.LoopDetector.LoopException;
import com.enno.dotz.client.editor.ModePalette.Bombify;
import com.enno.dotz.client.editor.ModePalette.ChangeIce;
import com.enno.dotz.client.editor.ModePalette.Chestify;
import com.enno.dotz.client.editor.ModePalette.DeleteItem;
import com.enno.dotz.client.editor.ModePalette.DeleteSusan;
import com.enno.dotz.client.editor.ModePalette.ModeBase;
import com.enno.dotz.client.editor.ModePalette.RadioActive;
import com.enno.dotz.client.editor.ModePalette.RotateItem;
import com.enno.dotz.client.editor.ModePalette.SetController;
import com.enno.dotz.client.editor.ModePalette.Stick;
import com.enno.dotz.client.editor.TeleportConnections.Link;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.DotBomb;
import com.enno.dotz.client.item.Drop;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.IcePick;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.LazySusan;
import com.enno.dotz.client.item.RandomItem;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.Turner;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.ui.MXAccordion;
import com.enno.dotz.client.ui.UTabSet;
import com.enno.dotz.client.util.Debug;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public abstract class EditLayoutTab extends VLayout
{
    private Context ctx;
    
    private LevelPropPanel m_levelPropPanel;
    private CellPalette m_cellPalette;
    private ItemPalette m_itemPalette;
    private EditGrid m_grid;
    
    private LayoutMode m_mode;
    private ClickDragMode m_clickDragMode;
    private ConnectTeleportMode m_connectTeleportMode;

    private ModePalette m_modePalette;

    private EditorPropertiesPanel m_editorProps;

    private VLayout m_gridContainer;
    private VLayout m_gridContainer2;

    private FeatureEditor m_featureEditor;
    
    private Random m_rnd = new Random();

    private ChangeListener m_changeListener;

    public EditLayoutTab(boolean isNew, Config level, ChangeListener changeListener)
    {
        m_changeListener = changeListener;
        
        setMargin(10);
        setMembersMargin(10);
        
        m_clickDragMode = new ClickDragMode();
        m_connectTeleportMode = new ConnectTeleportMode();
        
        m_levelPropPanel = new LevelPropPanel();
        addMember(m_levelPropPanel);
        
        m_levelPropPanel.setLevel(level);
        
        VLayout right = new VLayout();
        right.setMembersMargin(10);
        
        m_cellPalette = new CellPalette() {
            @Override
            protected void selected(Cell cell)
            {
                m_itemPalette.deselectAll();
                m_modePalette.deselectAll();
                setMode(cell);
            }
        };
                
        MXAccordion acc = MXAccordion.createAccordion("Cells", wrap(m_cellPalette));
        acc.setWidth(350);
        acc.setHeight(110);
        right.addMember(acc);

        m_itemPalette = new ItemPalette() {
            @Override
            protected void selected(Object item)
            {
                m_cellPalette.deselectAll();
                m_modePalette.deselectAll();
                setMode(item);
            }
        };
        
        acc = MXAccordion.createAccordion("Items", wrap(m_itemPalette));
        acc.setWidth(350);
        acc.setHeight(200);
        right.addMember(acc);
        
        m_modePalette = new ModePalette(m_connectTeleportMode) {
            @Override
            protected void selected(Object item)
            {
                m_cellPalette.deselectAll();
                m_itemPalette.deselectAll();                
                setMode(item);
            }
        };
        
        acc = MXAccordion.createAccordion("Mode", wrap(m_modePalette));
        acc.setWidth(350);
        acc.setHeight(100);
        right.addMember(acc);        
        
        HLayout top = new HLayout();
        top.setMembersMargin(10);
        
        ctx = new Context(true, level);
        
        m_grid = new EditGrid(isNew, ctx);
        
        m_gridContainer = new VLayout();
        m_gridContainer.addMember(m_grid);

        m_gridContainer2 = new VLayout();
        m_gridContainer2.addMember(m_gridContainer);
        
        m_editorProps = new EditorPropertiesPanel();
        acc = MXAccordion.createAccordion("Settings", wrap(m_editorProps));
        acc.setWidth(585);
        acc.setHeight(100);
        
        VLayout left = new VLayout();
        left.setMembersMargin(10);
        left.addMember(m_gridContainer2);
        left.addMember(acc);
        
        top.addMember(left);
        
        m_featureEditor = new FeatureEditor(ctx, this);
        
        UTabSet tabs = new UTabSet();
        tabs.setWidth(400);
        tabs.addTab("Mode", right);
        tabs.addTab("Gen", m_featureEditor);
        
        top.addMember(tabs);
        
        addMember(top);        
        
        m_cellPalette.selectFirstOption();
    }
    
    protected abstract boolean isLetterMode();
    protected abstract boolean isDominoMode();

    protected void changed()
    {
        m_changeListener.changed();
    }
    
    public void replaceGrid(GridState state)
    {
        if (m_mode != null)
            m_mode.stop();
        
        m_gridContainer2.removeMember(m_gridContainer);
        
        ctx.cfg.grid = state;
        
        m_grid = new EditGrid(false, ctx);
        
        m_gridContainer = new VLayout();
        m_gridContainer.addMember(m_grid);
        m_gridContainer2.addMember(m_gridContainer);
        
        if (m_mode != null)
            m_mode.start();
        
        changed();
    }
    
    public GridState copyGrid()
    {
        return ctx.state.copy();
    }
    
    public void randomLevel()
    {
        RandomGridGenerator gen = new RandomGridGenerator(ctx);
        GridState state = gen.getNextState(ctx.cfg.numColumns, ctx.cfg.numRows, m_editorProps);
        replaceGrid(state);
    }
    
    public GridState getGridState()
    {
        return ctx.state;
    }
    
    public static Canvas wrap(Widget w)
    {
        VLayout v = new VLayout();
        v.addMember(w);
        return v;
    }
    
    public void setMode(Object mode)
    {
        if (!(mode instanceof LayoutMode))
        {
            m_clickDragMode.setOperation(mode);
            mode = m_clickDragMode;
        }
            
        if (m_mode == mode)
            return;
        
        if (m_mode != null)
            m_mode.stop();
        
        m_mode = (LayoutMode) mode;
        if (m_mode != null)
            m_mode.start();        
    }
    
    public boolean validate(boolean save)
    {
        if (!m_levelPropPanel.validate(save))
            return false;
        
        if (!new TeleportConnections(ctx.state).isAllConnected())
        {
            SC.warn("The teleport cells are not properly connected. Connect them first.");
            return false;
        }
        
        try
        {
            new Conveyors(ctx.state);
        }
        catch (ConveyorException e)
        {
            SC.warn(e.getMessage());
            return false;
        }
        
        try
        {
            new LoopDetector(ctx.state).validate();
        }
        catch (LoopException e)
        {
            SC.warn("Detected a loop: " + e.loopToString());
            return false;
        }
        
        return true;
    }

    public void prepareSave(Config level)
    {
        m_levelPropPanel.prepareSave(level);
        m_grid.prepareSave(level);
    }

    public void setLevelID(int id)
    {
        m_levelPropPanel.setLevelID(id);
    }
    
    public void keyPressed(int x, int y, char c, KeyPressEvent event)
    {
        GridState state = ctx.state;
        int col = state.col(x);
        int row = state.row(y);            
        
        if (!state.isValidCell(col, row))
            return;
            
        Cell cell = state.cell(col, row);
        
        if (isDominoMode())
        {
            if (cell.item instanceof Domino)
            {
                Domino domino = (Domino) cell.item;
                int[] num = { domino.num[0], domino.num[1] };
                if (c >= '0' && c <= '9')
                {
                    num[0] = (int) (c - '0');
                }
                else
                {
                    switch (c)
                    {
                        case '!': num[1] = 1; break;
                        case '@': num[1] = 2; break;
                        case '#': num[1] = 3; break;
                        case '$': num[1] = 4; break;
                        case '%': num[1] = 5; break;
                        case '^': num[1] = 6; break;
                        case '&': num[1] = 7; break;
                        case '*': num[1] = 8; break;
                        case '(': num[1] = 9; break;
                        case ')': num[1] = 0; break;
                        default: return;
                    }
                }
                Domino newDomino = new Domino(num[0], num[1], domino.vertical, domino.isStuck());
                removeItem(cell);
                addItem(cell, newDomino);
            }
            return;
        }
        
        if (c == ',' || c == '<' || c == '.' || c == '>')
        {
            int ds = c == ',' || c == '<' ? -1 : 1;
            if (cell.canIncrementStrength())
            {
                cell.incrementStrength(ds);
                changed();
                return;
            }
        }
        
        // +/- increase strength of Animal/Blocker/Clock/Knight etc.
        if (c == '+' || c == '-' || c == '=')   // = is same as + (it's on the same keyboard key!)
        {
            // Try Item first...
            if (cell.item != null && cell.item.canIncrementStrength())
            {
                int ds = c == '-' ? -1 : 1;
                cell.item.incrementStrength(ds);
                ctx.dotLayer.redraw();
                changed();
                return;
            }
            
            // ... then Cell
            if (cell.canIncrementStrength())
            {
                cell.incrementStrength(c == '-' ? -1 : 1);
                changed();
                return;
            }
        }
        
        if (!isLetterMode())
            return;
        
        if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'))
            return;
        
        String letter = Character.toString(c).toUpperCase();
        if (letter.equals("Q"))
            letter = "Qu";
                
        if (cell.item instanceof Dot)
        {
            Dot dot = (Dot) cell.item;
            removeItem(cell);
            
            Dot newDot = dot.copy(letter);
            addItem(cell, newDot);
        }
        else if (cell.item instanceof DotBomb)
        {
            DotBomb bomb = (DotBomb) cell.item;
            Dot dot = bomb.getDot();
            removeItem(cell);
            
            Dot newDot = dot.copy(letter);
            bomb.setDot(newDot);
            addItem(cell, bomb);
        }
    }

    protected void removeItem(Cell cell)
    {
        if (cell.item == null)
            return;
        
        cell.item.removeShapeFromLayer(ctx.dotLayer);
        cell.item = null;
        ctx.dotLayer.redraw();
        changed();
    }
    
    protected void addItem(Cell cell, Item item)
    {
        tweakItem(item);
        
        GridState state = ctx.state;
        item.init(ctx);
        item.moveShape(state.x(cell.col), state.y(cell.row));
        item.addShapeToLayer(ctx.dotLayer);
        cell.item = item;
        ctx.dotLayer.redraw();
        changed();
    }

    protected void tweakItem(Item item)
    {
        if (item instanceof Animal)
        {
            Animal animal = (Animal) item;
            animal.setStrength(m_editorProps.getAnimalStrength());
            animal.setType(m_editorProps.getAnimalType());
            animal.setAction(m_editorProps.getAnimalAction());
        }
        else if (item instanceof Blocker)
        {
            Blocker sg = (Blocker) item;
            sg.setStrength(m_editorProps.getDoorStrength()); //TODO use different variable?
        }
        else if (item instanceof Knight)
        {
            Knight knight = (Knight) item;
            knight.setStrength(m_editorProps.getKnightStrength());
        }
        else if (item instanceof Clock)
        {
            Clock clock = (Clock) item;
            clock.setStrength(m_editorProps.getClockStrength());
        }
        else if (item instanceof IcePick)
        {
            IcePick pick = (IcePick) item;
            pick.setRadius(m_editorProps.getRadius());
        }
        else if (item instanceof Drop)
        {
            Drop drop = (Drop) item;
            drop.setRadius(m_editorProps.getRadius());
        }
        else if (isLetterMode() && item instanceof Dot)
        {
            Dot dot = (Dot) item;
            if (!dot.isLetter())
                dot.setLetter(Generator.nextLetter(m_rnd));
        }
    }
    
    public interface LayoutMode
    {
        void start();
        void stop();
    }
    
    public class ConnectTeleportMode implements LayoutMode, ModeBase
    {
        private HandlerRegistration m_mouseDownHandler;
        private HandlerRegistration m_mouseUpHandler;
        private HandlerRegistration m_mouseMoveHandler;
        private HandlerRegistration m_mouseOutHandler;
        private HandlerRegistration m_mouseOverHandler;
        private HandlerRegistration m_keyPressHandler;
        
        private int m_x, m_y;
        private boolean m_mouseDown;
        
        private Teleport m_from;
        private Pt m_pt;
        
        private Layer m_layer = new Layer();
        private Line m_dragLine;
        private TeleportConnections m_connections;
        
        public void start()
        {
            m_layer = new Layer();
            m_grid.add(m_layer);
            
            initInfo();            
            
            m_mouseDownHandler = m_grid.addMouseDownHandler(new MouseDownHandler()
            {
                @Override
                public void onMouseDown(MouseDownEvent event)
                {
                    if (selectTeleportSource(event.getX(), event.getY()))
                    {
                        m_mouseDown = true;                            
                    }
                }
            });
            
            m_mouseUpHandler = m_grid.addMouseUpHandler(new MouseUpHandler()
            {                       
                @Override
                public void onMouseUp(MouseUpEvent event)
                {
                    connectTo(event.getX(), event.getY());
                    changed();
                    cancel();
                }
            });
            
            m_mouseMoveHandler = m_grid.addMouseMoveHandler(new MouseMoveHandler()
            {                       
                @Override
                public void onMouseMove(MouseMoveEvent event)
                {
                    m_x = event.getX();
                    m_y = event.getY();
                    
                    if (m_mouseDown)
                        updateDrag(event.getX(), event.getY());
                }
            });
            
            m_mouseOutHandler = m_grid.addMouseOutHandler(new MouseOutHandler()
            {
                @Override
                public void onMouseOut(MouseOutEvent event)
                {
                    // cancel drag when moving the mouse out of the grid
                    cancel();
                }
            });
            
            m_mouseOverHandler = m_grid.addMouseOverHandler(new MouseOverHandler() 
            {
                @Override
                public void onMouseOver(MouseOverEvent event)
                {
                    m_grid.setFocus(true);
                }
            });
            
            m_keyPressHandler = m_grid.addKeyPressHandler(new KeyPressHandler() 
            {
                @Override
                public void onKeyPress(KeyPressEvent event)
                {
                    char c = event.getCharCode();
                    keyPressed(m_x, m_y, c, event);
                }
            });
        }

        public void stop()
        {
            m_grid.remove(m_layer);
            m_layer = null;
            
            m_mouseDownHandler.removeHandler();
            m_mouseUpHandler.removeHandler();
            m_mouseMoveHandler.removeHandler();
            m_mouseOutHandler.removeHandler();
            m_mouseOverHandler.removeHandler();
            m_keyPressHandler.removeHandler();
        }
        
        protected void cancel()
        {
            if (m_mouseDown)
            {
                m_mouseDown = false;
                m_layer.remove(m_dragLine);
            }
            LayerRedrawManager.get().schedule(m_layer);
        }
        
        protected void initInfo()
        {
            GridState state = ctx.state;
            m_connections = new TeleportConnections(state);
            
            for (Link link : m_connections.getLinks())
            {
                createLinkLine(state, link);
            }
        }
        
        protected void createLinkLine(GridState state, Link link)
        {
            Arrow line = new Arrow(new Point2D(state.x(link.src.col), state.y(link.src.row)), 
                    new Point2D(state.x(link.target.col), state.y(link.target.row)),
                    3, 15, 30, 60, ArrowType.AT_END);
            line.setFillColor(ColorName.BLUE);
            
            m_layer.add(line);
            
            link.line = line;
        }
        
        protected boolean selectTeleportSource(int x, int y)
        {
            GridState state = ctx.state;
            int col = state.col(x);
            int row = state.row(y);
            if (col < 0 || col >= state.numColumns || row < 0 || row >= state.numRows)
                return false;
                   
            Cell cell = state.cell(col, row);
            if (!cell.isTeleportSource() && !cell.isTeleportTarget())
                return false;
            
            m_from = (Teleport) cell;
            m_pt = new Pt(row, col);
            
            m_dragLine = new Line(state.x(col), state.y(row), state.x(col), state.y(row));
            m_dragLine.setStrokeColor(ColorName.RED);
            m_dragLine.setStrokeWidth(3);
            m_layer.add(m_dragLine);
            
            return true;
        }
        
        protected void connectTo(int x, int y)
        {
            GridState state = ctx.state;
            int col = state.col(x);
            int row = state.row(y);
            if (!state.isValidCell(col, row))
                return;
            
            Cell cell = state.cell(col, row);
            if (!(m_from.isTeleportTarget() && cell.isTeleportSource()
               || m_from.isTeleportSource() && cell.isTeleportTarget()))
                return;
            
            boolean fromIsSource = m_from.isTeleportSource();
            Teleport to = (Teleport) cell;
            Link fromLink = fromIsSource ? m_connections.getSourceLink(m_from) : m_connections.getTargetLink(m_from);
            if (fromLink != null)
            {
                if ((fromIsSource && fromLink.target == to) || (!fromIsSource && fromLink.src == to))
                    return; // already connected
                
                m_layer.remove(fromLink.line);
            }
            
            Link toLink = fromIsSource ? m_connections.getTargetLink(to) : m_connections.getSourceLink(to);
            if (toLink != null)
            {
                m_layer.remove(toLink.line);
            }
            
            Link newLink = fromIsSource ? m_connections.link(m_from, to) :  m_connections.link(to, m_from);
            createLinkLine(state, newLink);
        }
        
        protected void updateDrag(int x, int y)
        {
            GridState state = ctx.state;
            int col = state.col(x);
            int row = state.row(y);
            if (col < 0 || col >= state.numColumns || row < 0 || row >= state.numRows)
                return;
            
            Pt newPt = new Pt(col, row);
            if (m_pt != null && m_pt.equals(newPt))
                return;

            m_pt = newPt;        
            // Cell cell = state.cell(col, row);
            
            Point2DArray pts = m_dragLine.getPoints();
            Point2D end = pts.get(1);
            end.setX(state.x(col));
            end.setY(state.y(row));
            m_dragLine.setPoints(pts);
            
            LayerRedrawManager.get().schedule(m_layer);
        }

        public IPrimitive<?> createShape(int size)
        {
            double sz = size;
            
            Group g = new Group();
            
            double s3 = sz / 3;
            double b = 4;
            double h = 3;
            double z = 0.5 * sz; // 1.5 * s3
            
            // bottom left
            Rectangle r = new Rectangle(s3, s3);
            r.setStrokeColor(ColorName.BLACK);
            r.setStrokeWidth(1);
            r.setX(b - z);
            r.setY(0.5 * s3 - b);
            g.add(r);
            
            r = new Rectangle(s3, h);
            r.setFillColor(Teleport.COLOR);
            r.setX(b - z);
            r.setY(z - b - h);
            g.add(r);
            
            // top right
            r = new Rectangle(s3, s3);
            r.setStrokeColor(ColorName.BLACK);
            r.setStrokeWidth(1);
            r.setX(0.5 * s3 - b);
            r.setY(b - z);
            g.add(r);
            
            r = new Rectangle(s3, h);
            r.setFillColor(Teleport.COLOR);
            r.setX(0.5 * s3 - b);
            r.setY(b - z);
            g.add(r);
            
            Line line = new Line(s3 - b, b - s3, b - s3, s3 - b);
            line.setStrokeColor(ColorName.BLUE);
            line.setStrokeWidth(2);
            g.add(line);
            
            return g;
        }
    }
    
    public class ClickDragMode implements LayoutMode
    {
        private HandlerRegistration m_mouseDownHandler;
        private HandlerRegistration m_mouseUpHandler;
        private HandlerRegistration m_mouseMoveHandler;
        private HandlerRegistration m_mouseOutHandler;
        private HandlerRegistration m_keyPressHandler;
        
        private Object m_operation;
        
        private boolean m_mouseDown;
        private int m_x, m_y;
        private ConveyorCell m_lastConveyorCell;

        private Pt m_pt;
        private HandlerRegistration m_mouseOverHandler;

        @Override
        public void start()
        {
            m_mouseDown = false;
            m_pt = null;
            
            m_mouseDownHandler = m_grid.addMouseDownHandler(new MouseDownHandler()
            {
                @Override
                public void onMouseDown(MouseDownEvent event)
                {
                    m_mouseDown = true;
                    m_pt = null;
                    m_lastConveyorCell = null;
                    selectCell(event.getX(), event.getY(), event.isShiftKeyDown());
                }
            });
            
            m_mouseUpHandler = m_grid.addMouseUpHandler(new MouseUpHandler()
            {                       
                @Override
                public void onMouseUp(MouseUpEvent event)
                {
                    m_mouseDown = false;
                }
            });
            
            m_mouseMoveHandler = m_grid.addMouseMoveHandler(new MouseMoveHandler()
            {                       
                @Override
                public void onMouseMove(MouseMoveEvent event)
                {
                    m_x = event.getX();
                    m_y = event.getY();
                    
                    if (m_mouseDown)
                        selectCell(event.getX(), event.getY(), event.isShiftKeyDown());
                }
            });
            
            m_mouseOutHandler = m_grid.addMouseOutHandler(new MouseOutHandler()
            {
                @Override
                public void onMouseOut(MouseOutEvent event)
                {
                    // cancel drag when moving the mouse out of the grid
                    m_mouseDown = false;
                }
            });

            m_mouseOverHandler = m_grid.addMouseOverHandler(new MouseOverHandler() 
            {
                @Override
                public void onMouseOver(MouseOverEvent event)
                {
                    m_grid.setFocus(true);
                }
            });
            
            m_keyPressHandler = m_grid.addKeyPressHandler(new KeyPressHandler() 
            {
                @Override
                public void onKeyPress(KeyPressEvent event)
                {
                    char c = event.getCharCode();
                    keyPressed(m_x, m_y, c, event);
                }
            });
        }
        
        public void setOperation(Object operation)
        {
            m_operation = operation;
        }

        public void stop()
        {
            m_mouseDownHandler.removeHandler();
            m_mouseUpHandler.removeHandler();
            m_mouseMoveHandler.removeHandler();
            m_mouseOutHandler.removeHandler();
            m_mouseOverHandler.removeHandler();
            m_keyPressHandler.removeHandler();
        }

        protected void selectCell(int x, int y, boolean shift)
        {
            GridState state = ctx.state;
            
            if (m_operation instanceof LazySusan)
            {
                x -= ctx.cfg.size / 2;
                y -= ctx.cfg.size / 2;
            }
            
            int col = state.col(x);
            int row = state.row(y);            
            
            if (!state.isValidCell(col, row))
                return;
            
            Pt newPt = new Pt(col, row);
            if (m_pt != null && m_pt.equals(newPt))
                return;

            m_pt = newPt;        
            Cell cell = state.cell(col, row);
            
            if (m_operation instanceof DeleteItem)
            {
                removeItem(cell);
            }
            else if (m_operation instanceof RotateItem)
            {
                rotateItem(cell, shift);
            }
            else if (m_operation instanceof Item)
            {
                if (cell instanceof Machine)
                {
                    Item item = (Item) m_operation;
                    if (item.canBeLaunched())
                    {
                        Item launchItem = item.copy();
                        tweakItem(launchItem);
                        
                        Machine machine = createMachine(MachineType.ITEM.name, launchItem);
                        
                        if (canHaveCell(col, row, machine))
                            changeCell(col, row, cell, machine);
                    }
                    return;
                }
                
                if (cell.canContainItems())
                {
                    boolean sameItem = cell.item != null && cell.item.getClass() == m_operation.getClass();
                    if (sameItem && cell.item instanceof Animal)
                    {
                        sameItem = false;
                    }
                    
                    if (sameItem && cell.item.canRotate())
                    {
                        // E.g. clicking on a Mirror with another Mirror will rotate it.
                        rotateItem(cell, shift);
                    }
                    else if (sameItem && cell.item instanceof Turner)
                    {
                        rotateTurner(cell);
                    }
                    else if (sameItem && cell.item.canIncrementStrength())
                    {
                        cell.item.incrementStrength(1);
                        ctx.dotLayer.redraw();
                        changed();
                    }
                    else
                    {
                        removeItem(cell);                    
                        addItem(cell, ((Item) m_operation).copy());
                    }
                }
            }
            else if (m_operation instanceof ConveyorCell)
            {
                if (canHaveCell(col, row, (Cell) m_operation))
                    changeConveyorCell(col, row, cell, (ConveyorCell) ((Cell) m_operation).copy());
            }
            else if (m_operation instanceof Machine)
            {
                if (canHaveCell(col, row, (Machine) m_operation))
                {
                    MachineType type = MachineType.find(m_editorProps.getMachineType());
                    Item launchItem = null;
                    
                    switch (type)
                    {
                        case ITEM: 
                            launchItem = new Fire(false); 
                            break;
                        case COIN: 
                            launchItem = new Coin(1, false); 
                            break;
                        case BOMBIFY:
                            Dot dot = isLetterMode() ? new Dot(0, "A", false, false) : new Dot(0);
                            int strength = m_editorProps.getBombStrength();                            
                            launchItem = new DotBomb(dot, strength, false);
                            break;
                        default:
                    }
                    
                    Machine machine = createMachine(type.name, launchItem);
                    changeCell(col, row, cell, machine); 
                }
            }
            else if (m_operation instanceof Cell)
            {
                boolean sameCell = cell.getClass() == m_operation.getClass();
                
                if (sameCell && cell instanceof Slide)
                {
                    boolean left = ((Slide) cell).isToLeft();
                    if (left == ((Slide) m_operation).isToLeft())
                    {
                        Slide newSlide = new Slide(!left); // toggle direction
                        if (canHaveCell(col, row, newSlide))
                        {
                            changeCell(col, row, cell, newSlide);
                        }
                        return;
                    }
                }
                
                if (canHaveCell(col, row, (Cell) m_operation))
                {
                    if (sameCell && cell instanceof Door && !((Door) cell).isBlinking())
                    {
                        if (((Door) cell).hasDirection())
                            ((Door) cell).rotate();
                        else
                            cell.incrementStrength(1);
                        changed();
                    }
                    else if (sameCell && cell instanceof Cage && !((Cage) cell).isBlinking() && 
                            ((Cage) cell).isBlocking() == ((Cage) m_operation).isBlocking())
                    {
                        cell.incrementStrength(1);
                        changed();
                    }
                    else
                    {
                        changeCell(col, row, cell, ((Cell) m_operation).copy());
                    }
                }
            }
            else if (m_operation instanceof LazySusan)
            {
                if (canHaveSusan(col, row))
                    placeSusan(col, row, (LazySusan) m_operation);
            }
            else if (m_operation instanceof DeleteSusan)
            {
                deleteSusan(col, row);
            }
            else if (m_operation instanceof SetController)
            {
                setController(col, row);
            }
            else if (m_operation instanceof Stick)
            {
                stick(col, row);
            }
            else if (m_operation instanceof RadioActive)
            {
                radioActify(col, row);
            }
            else if (m_operation instanceof ChangeIce)
            {
                changeIce(col, row);
            }
            else if (m_operation instanceof Bombify)
            {
                bombify(col, row);
            }
            else if (m_operation instanceof Chestify)
            {
                chestify(col, row);
            }
        }
        
        private boolean canHaveCell(int col, int row, Cell cell)
        {
            GridState state = ctx.state;
            
            // Slide, Conveyor, Rock or Hole can't overlap LazySusan
            if (cell instanceof Slide || cell instanceof Hole || cell instanceof ConveyorCell || cell instanceof Rock || cell instanceof Machine)
            {
                for (LazySusan su : state.getLazySusans())
                {
                    int c = su.getCol();
                    int r = su.getRow();
                    if (!(c < col - 1 || c > col || r < row - 1 || r > row))
                        return false; // overlap
                }
            }
            
            if (cell instanceof Slide)
            {
                boolean left = ((Slide) cell).isToLeft();
                if (left && col == 0 || !left && col == state.numColumns - 1)
                    return false; // too close to the side
                
                // Check above
                if (row > 0)
                {
                    // Can't be below other Slide, Rock or Teleport source
                    Cell c = state.cell(col, row - 1);
                    if (c instanceof Slide || c.isTeleportSource() || c instanceof Rock)
                        return false;
                }
                
                // Check below
                if (row < state.numRows - 1)
                {
                    // Can't be below other Slide
                    Cell c = state.cell(col,  row + 1);
                    if (c instanceof Slide)
                        return false;
                }
                
                if (left)
                {
                    Cell neighbor = state.cell(col - 1, row);
                    if (neighbor instanceof Slide || neighbor instanceof Rock)
                        return false;
                }
                else
                {
                    Cell neighbor = state.cell(col + 1, row);
                    if (neighbor instanceof Slide || neighbor instanceof Rock)
                        return false;
                }
            }
            
            return true;
        }
        
        private boolean canHaveSusan(int col, int row)
        {
            GridState state = ctx.state;
            if (col > state.numColumns - 2 || row > state.numRows - 2)
                return false; // too close to bottom or right side
            
            // Check overlap with other LazySusans
            for (LazySusan su : state.getLazySusans())
            {
                int c = su.getCol();
                int r = su.getRow();
                if (!(c < col - 1 || c > col + 1 || r < row - 1 || r > row + 1))
                    return false; // overlap
            }
            
            return canPlaceSusan(state.cell(col, row)) && canPlaceSusan(state.cell(col, row + 1)) 
                && canPlaceSusan(state.cell(col + 1, row)) && canPlaceSusan(state.cell(col + 1, row + 1));
        }
        
        private void placeSusan(int col, int row, LazySusan susan)
        {
            susan = susan.copy();
            susan.setCol(col);
            susan.setRow(row);
            
            ctx.state.add(susan);
            susan.initGraphics(ctx);
            ctx.backgroundLayer.redraw();
            changed();
        }

        private void deleteSusan(int col, int row)
        {
            GridState state = ctx.state;            
            for (LazySusan su : state.getLazySusans())
            {
                if ((su.getCol() == col || su.getCol() + 1 == col) && (su.getRow() == row || su.getRow() + 1 == row))
                {
                    su.removeGraphics();
                    state.getLazySusans().remove(su);
                    ctx.backgroundLayer.redraw();
                    changed();
                    return;
                }
            }
        }

        private void setController(int col, int row)
        {
            GridState state = ctx.state;
            Cell cell = state.cell(col, row);
            
            if (cell == null || !cell.hasController())
                SetControllerDialog.closeDialog();
            else
                SetControllerDialog.setControllable((Controllable) cell, m_changeListener);
        }

        private void changeIce(int col, int row)
        {
            GridState state = ctx.state;  
            Cell cell = state.cell(col,  row);
            if (!cell.canHaveIce())
                return;
            
            cell.ice = m_editorProps.getIceStrength();
            cell.updateIce();
            
            ctx.iceLayer.redraw();
            
            changed();
        }

        private void stick(int col, int row)
        {
            GridState state = ctx.state;  
            Cell cell = state.cell(col, row);
            
            if (cell instanceof Machine)
            {
                Machine machine = (Machine) cell;
                if (machine.canToggleStuck())
                {
                    Machine newMachine = machine.toggleStuck();
                    changeCell(col, row, cell, newMachine);
                }
                return;
            }
            
            if (cell.item == null || !cell.item.canBeStuck())
                return;
            
            Item newItem = cell.item.copy();
            newItem.setStuck(!cell.item.isStuck());
            removeItem(cell);
            addItem(cell, newItem);
        }

        private void radioActify(int col, int row)
        {
            GridState state = ctx.state;  
            Cell cell = state.cell(col, row);
            if (cell.item == null || !(cell.item instanceof Dot || cell.item instanceof DotBomb || cell.item instanceof RandomItem))
                return;
            
            Item newItem = cell.item.copy();
            
            if (cell.item instanceof RandomItem)
            {
                RandomItem rnd = (RandomItem) newItem;
                rnd.setRadioActive(!rnd.isRadioActive());
            }
            else
            {
                Dot dot = cell.item instanceof Dot ? (Dot) newItem : ((DotBomb) newItem).getDot();
                dot.setRadioActive(!dot.isRadioActive());
            }
            
            removeItem(cell);
            addItem(cell, newItem);
        }
        
        private void bombify(int col, int row)
        {
            GridState state = ctx.state;  
            Cell cell = state.cell(col, row);
            if (!(cell.item instanceof Dot || cell.item instanceof DotBomb))
                return;
            
            try
            {
                Debug.p("bombify");
                
                int strength = m_editorProps.getBombStrength();
                if (strength == 0)
                {
                    if (cell.item instanceof Dot)
                        return;
                    
                    DotBomb bomb = (DotBomb) cell.item;
                    Item dot = bomb.getDot().copy();
                    removeItem(cell);
                    addItem(cell, dot);                
                }
                else
                {
                    if (cell.item instanceof DotBomb)
                    {
                        DotBomb bomb = (DotBomb) cell.item;
                        bomb.setStrength(strength);
                        bomb.updateStrength();
                    }
                    else
                    {
                        DotBomb bomb = new DotBomb((Dot) cell.item.copy(), strength, cell.item.isStuck());
                        removeItem(cell);
                        addItem(cell, bomb); 
                    }
                }
                
                ctx.dotLayer.redraw();
                Debug.p("bombify done");
                
                changed();
            }
            catch (Exception e)
            {
                Debug.p("bombify failed", e);
            }
        }
        
        private void chestify(int col, int row)
        {
            GridState state = ctx.state;  
            Cell cell = state.cell(col,  row);
            if (cell.item instanceof Chest)
                return;
            
            try
            {
                Debug.p("chestify");
                
                int strength = m_editorProps.getDoorStrength();

                Chest chest = new Chest(cell.item == null ? null : cell.item.copy(), strength);
                removeItem(cell);
                addItem(cell, chest); 
                
                ctx.dotLayer.redraw();
                Debug.p("chestify done");
            }
            catch (Exception e)
            {
                Debug.p("chestify failed", e);
            }
        }
        
        private boolean canPlaceSusan(Cell c)
        {
            return !(c instanceof Hole || c instanceof Slide || c instanceof ConveyorCell || c instanceof Rock);
        }

        protected void changeConveyorCell(int col, int row, Cell oldCell, ConveyorCell newCell)
        {
            if (m_lastConveyorCell != null)
            {
                int dir = getDirection(m_lastConveyorCell, col, row);
                if (dir != Direction.NONE && dir != Direction.opposite(m_lastConveyorCell.getDirection()))
                {
                    if (m_lastConveyorCell.getExitDirection() != dir)
                        m_lastConveyorCell.turnTo(dir);
                    
                    newCell = new ConveyorCell(dir, newCell.getTurn());
                }
            }
            m_lastConveyorCell = newCell;
            changeCell(col, row, oldCell, newCell);
        }
        
        protected int getDirection(Cell from, int toCol, int toRow)
        {
            if (from.col == toCol)
            {
                if (from.row + 1 == toRow)
                    return Direction.SOUTH;
                else if (from.row - 1 == toRow)
                    return Direction.NORTH;                
            }
            else if (from.row == toRow)
            {
                if (from.col + 1 == toCol)
                    return Direction.EAST;
                else if (from.col - 1 == toCol)
                    return Direction.WEST;                
            }
            return Direction.NONE;
        }
        
        protected void changeCell(int col, int row, Cell oldCell, Cell newCell)
        {
            boolean keepItem = oldCell.item != null;
            if (keepItem && !newCell.canContainItems())
                keepItem = false;
            
            Item item = oldCell.item;
            if (item != null)
            {
                removeItem(oldCell);
            }
            
            // Preserve ice
            if (newCell.canHaveIce())
                newCell.ice = oldCell.ice;
            
            oldCell.removeGraphics();
            
            if (newCell instanceof Door)
            {
                Door door = (Door) newCell;
                if (!door.isBlinking())
                {
                    door.setStrength(m_editorProps.getDoorStrength());
                    door.setRotationDirection(m_editorProps.getDoorRotation());
                }
            }
            else if (newCell instanceof Cage)
            {
                Cage cage = (Cage) newCell;
                if (!cage.isBlinking())
                {
                    cage.setStrength(m_editorProps.getDoorStrength());
                }
            }
            
            newCell.init(ctx);
            newCell.initGraphics(col, row, ctx.state.x(col), ctx.state.y(row));
            ctx.state.setCell(col, row, newCell);
            
            if (keepItem)
                addItem(newCell, item);
            
            m_grid.setBorders();
            
            m_grid.draw();
            
            changed();
        }        
        
        protected void rotateItem(Cell cell, boolean shift)
        {
            if (!shift)
            {
                if (cell.item instanceof Turner)
                {
                    rotateTurner(cell);
                    return;
                }
                
                if (cell.item != null && cell.item.canRotate())
                {
                    cell.item.rotate(1);
                    ctx.dotLayer.redraw();
                    changed();
                    return;
                }
                
//                if (cell.item instanceof Striped)
//                {
//                    Striped old = (Striped) cell.item;
//                    Striped newItem = new Striped(old.color, !old.vertical);
//                    removeItem(cell);
//                    addItem(cell, newItem);
//                    return;
//                }
            }
            
            
            
            if (cell instanceof Door)
            {
                ((Door) cell).rotate();
            }
            else if (cell instanceof ConveyorCell)
            {
                ((ConveyorCell) cell).rotate();
            }
            changed();
        }

        protected void rotateTurner(Cell cell)
        {
            int n = ((Turner) cell.item).n;
            Turner newItem = new Turner((n % 3) + 1, cell.item.isStuck());
            removeItem(cell);
            addItem(cell, newItem);
        }        
    }

    public EditorPropertiesPanel getEditorPropertiesPanel()
    {
        return m_editorProps;
    }

    public Machine createMachine(String type, Item launchItem)
    {
        int every = m_editorProps.getMachineEvery();
        int howMany = m_editorProps.getMachineHowMany();
        String trigger = m_editorProps.getMachineTrigger();
        return new Machine(type, launchItem, every, howMany, trigger);
    }

    public void setLetterMode(boolean isLetterMode)
    {
        GridState state = ctx.state;
        for (int row = 0; row < state.numRows; row++)
        {
            for (int col = 0; col < state.numColumns; col++)
            {
                Cell cell = state.cell(col, row);
                if (cell.item instanceof Dot)
                {
                    Dot dot = (Dot) cell.item;
                    removeItem(cell);
                    
                    Dot newDot = new Dot(dot.color, isLetterMode ? Generator.nextLetter(m_rnd) : null, dot.isStuck(), dot.isRadioActive());
                    addItem(cell, newDot);
                }
                else if (cell.item instanceof DotBomb)
                {
                    DotBomb bomb = (DotBomb) cell.item;
                    removeItem(cell);
                    
                    Dot dot = bomb.getDot();
                    Dot newDot = new Dot(dot.color, isLetterMode ? Generator.nextLetter(m_rnd) : null, dot.isStuck(), dot.isRadioActive());
                    addItem(cell, new DotBomb(newDot, bomb.getStrength(), bomb.isStuck()));
                }
            }
        }
    }
}
