package com.enno.dotz.client;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Rectangle;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Cell.Bubble;
import com.enno.dotz.client.Cell.Cage;
import com.enno.dotz.client.Cell.CircuitCell;
import com.enno.dotz.client.Cell.Door;
import com.enno.dotz.client.item.Anchor;
import com.enno.dotz.client.item.Animal;
import com.enno.dotz.client.item.Blaster;
import com.enno.dotz.client.item.Blocker;
import com.enno.dotz.client.item.Bomb;
import com.enno.dotz.client.item.Chest;
import com.enno.dotz.client.item.Clock;
import com.enno.dotz.client.item.Coin;
import com.enno.dotz.client.item.ColorBomb;
import com.enno.dotz.client.item.Diamond;
import com.enno.dotz.client.item.Domino;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.item.Fire;
import com.enno.dotz.client.item.Knight;
import com.enno.dotz.client.item.Laser;
import com.enno.dotz.client.item.Mirror;
import com.enno.dotz.client.item.Rocket;
import com.enno.dotz.client.item.Spider;
import com.enno.dotz.client.item.Striped;
import com.enno.dotz.client.item.WrappedDot;
import com.enno.dotz.client.util.Font;


public class ScorePanel extends LienzoPanel
{
    private static String FONT = "Helvetica";
    private static final int GOAL_SPACING = 10;
    private static final int ROW_HEIGHT = 70;
    
    protected Context ctx;
    
    private Layer m_layer;
    private Text m_levelName;
    private Text m_id;
    private Text m_creator;

    private double m_width;
    private List<GoalItem> m_list = new ArrayList<GoalItem>();

    public ScorePanel(Context ctx)
    {
        super(ctx.gridWidth, ROW_HEIGHT);
        
        this.ctx = ctx;
        m_width = ctx.gridWidth;

        setBackgroundColor(ColorName.ALICEBLUE);

        m_layer = new Layer();
        add(m_layer);
        
        double used = 0;
        double usedLeft = 0;
        if (ctx.cfg.id != Config.UNDEFINED_ID)
        {
            m_id = new Text("[" + ctx.cfg.id + "]", FONT, "bold", 10);
            m_id.setFillColor(ColorName.BLACK);
            m_id.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
            m_id.setX(5);
            m_id.setY(5);
            m_layer.add(m_id);
            
            usedLeft = 5 + m_id.measure(m_layer.getContext()).getWidth();
            used += usedLeft;
        }
        
        m_levelName = new Text(ctx.cfg.name, Font.LEVEL_NAME, "normal", 12);
        m_levelName.setFillColor(ColorName.BLACK);
        m_levelName.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
        m_levelName.setX(5);
        m_levelName.setY(5);
        m_levelName.setTextAlign(TextAlign.CENTER);
        m_layer.add(m_levelName);
        
        
        if (ctx.cfg.creator != null && ctx.cfg.creator.length() > 0 && !ctx.cfg.creator.equals("?"))
        {
            m_creator = new Text("(by " + ctx.cfg.creator + ")", FONT, "bold", 10);
            m_creator.setFillColor(ColorName.BLACK);
            m_creator.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
            m_creator.setX(m_width - 5);
            m_creator.setY(5);
            m_creator.setTextAlign(TextAlign.RIGHT);
            m_layer.add(m_creator);
            
            used += 5 + m_creator.measure(m_layer.getContext()).getWidth();
        }
         
        m_levelName.setX(usedLeft + (m_width - used) / 2);          
        
        Line line = new Line(0, 25, m_width, 25);
        line.setStrokeColor(ColorName.LIGHTBLUE);
        line.setStrokeWidth(1);
        m_layer.add(line);
    }
    
    public void copyState(UndoState undoState)
    {
        List<GoalItemState> list = new ArrayList<GoalItemState>();
        for (GoalItem g : m_list)
            list.add(g.copyState());
        undoState.goalItems = list;
    }
    
    public void restoreState(UndoState undoState)
    {
        List<GoalItemState> list = undoState.goalItems;
        for (int i = 0, n = m_list.size(); i < n; i++)
        {
            m_list.get(i).restoreState(list.get(i));
        }
        draw();
    }

    public void setGoals(Goal goal)
    {
        if (m_list.size() > 0)
        {
            for (GoalItem g : m_list)
                g.removeFromPanel(m_layer);
            
            m_list.clear();
        }
        addGoals(goal);
        
        update();
        draw();
    }
    
    public void madeChain(int length, int color)
    {
        for (GoalItem g : m_list)
        {
            if (!g.isCompleted())
                g.madeChain(length, color);
        }
    }
    
    public void update()
    {
        for (GoalItem g : m_list)
        {
            if (!g.isCompleted())
                g.updateText();
        }
        
        LayerRedrawManager.get().schedule(m_layer);
    }
    
    public boolean isGoalReached()
    {
        if (m_list.size() == 0)
            return false; // no goals - no end
        
        for (GoalItem goal : m_list)
        {
            if (!goal.isCompleted())
                return false;
        }
        return true;
    }
    
    public void addGoals(Goal goal)
    {
        m_list = new ArrayList<GoalItem>();
        int[] dots = goal.getDots();
        for (int i = 0; i < dots.length; i++)
        {
            int need = dots[i];
            if (need > 0)
                m_list.add(new DotGoal(i, need, ctx));
        }
        
        int need = goal.getAnchors();
        if (need != 0)
            m_list.add(new AnchorGoal(need, ctx));
        
        need = goal.getDiamonds();
        if (need != 0)
            m_list.add(new DiamondGoal(need, ctx));
        
        need = goal.getAnimals();
        if (need != 0)
            m_list.add(new AnimalGoal(need, ctx));
        
        need = goal.getFire();
        if (need != 0)
            m_list.add(new FireGoal(need, ctx));
        
        need = goal.getKnights();
        if (need != 0)
            m_list.add(new KnightGoal(need, ctx));
        
        need = goal.getClocks();
        if (need != 0)
            m_list.add(new ClockGoal(need, ctx));
        
        need = goal.getIce();
        if (need != 0)
            m_list.add(new IceGoal(need, ctx));
        
        need = goal.getDoors();
        if (need != 0)
            m_list.add(new DoorGoal(need, ctx));
        
        need = goal.getCages();
        if (need != 0)
            m_list.add(new CageGoal(need, ctx));
        
        need = goal.getBubbles();
        if (need != 0)
            m_list.add(new BubbleGoal(need, ctx));
        
        need = goal.getCircuits();
        if (need != 0)
            m_list.add(new CircuitGoal(need, ctx));
        
        need = goal.getBlockers();
        if (need != 0)
            m_list.add(new BlockerGoal(need, ctx));
        
        need = goal.getZapBlockers();
        if (need != 0)
            m_list.add(new ZapBlockerGoal(need, ctx));
        
        need = goal.getLasers();
        if (need != 0)
            m_list.add(new LaserGoal(need, ctx));
        
        need = goal.getBirds();
        if (need != 0)
            m_list.add(new BirdGoal(need, ctx));
        
        need = goal.getMirrors();
        if (need != 0)
            m_list.add(new MirrorGoal(need, ctx));
        
        need = goal.getRockets();
        if (need != 0)
            m_list.add(new RocketGoal(need, ctx));
        
        need = goal.getBlasters();
        if (need != 0)
            m_list.add(new BlasterGoal(need, ctx));
        
        need = goal.getBombs();
        if (need != 0)
            m_list.add(new BombGoal(need, ctx));
        
        need = goal.getColorBombs();
        if (need != 0)
            m_list.add(new ColorBombGoal(need, ctx));
        
        need = goal.getDominoes();
        if (need != 0)
            m_list.add(new DominoGoal(need, ctx));

        need = goal.getScore();
        if (need != 0)
            m_list.add(new ScoreGoal(need, ctx));

        need = goal.getWords();
        if (need != 0)
            m_list.add(new WordsGoal(need, ctx));

        need = goal.getCoins();
        if (need != 0)
            m_list.add(new CoinGoal(need, ctx));

        need = goal.getSpiders();
        if (need != 0)
            m_list.add(new SpiderGoal(need, ctx));

        need = goal.getChests();
        if (need != 0)
            m_list.add(new ChestGoal(need, ctx));

        need = goal.getWrappedDots();
        if (need != 0)
            m_list.add(new WrappedDotGoal(need, ctx));

        need = goal.getStriped();
        if (need != 0)
            m_list.add(new StripedGoal(need, ctx));

        ChainGoal combi = goal.getChainGoal();
        if (combi != null)
            m_list.add(ChainGoalItem.createChainGoal(combi, ctx));
        
        // Layout the GoalItems
        int cols = Math.max(8, ctx.cfg.numColumns);
        int rows = m_list.size() > cols ? 2 : 1;
        setPixelSize((int) m_width, rows == 1 ? ROW_HEIGHT : ROW_HEIGHT + 50);
        
        if (rows == 1)
        {
            int n = m_list.size();
            double w = (m_width - n * GoalItem.GOAL_WIDTH - (n - 1) * GOAL_SPACING) / 2;
            
            double x = w;
            for (int i = 0; i < m_list.size(); i++)
            {
                m_list.get(i).addToPanel(x, 22, m_layer);
                x += GoalItem.GOAL_WIDTH + GOAL_SPACING;
            }
        }
        else
        {
            int bottom = m_list.size() / 2;
            int top = m_list.size() - bottom;
            
            int n = top;
            double w = (m_width - n * GoalItem.GOAL_WIDTH - (n - 1) * GOAL_SPACING) / 2;
            
            double x = w;
            for (int i = 0; i < top; i++)
            {
                m_list.get(i).addToPanel(x, 22, m_layer);
                x += GoalItem.GOAL_WIDTH + GOAL_SPACING;
            }
            
            n = bottom;
            w = (m_width - n * GoalItem.GOAL_WIDTH - (n - 1) * GOAL_SPACING) / 2;
            
            x = w;
            for (int i = top; i < m_list.size(); i++)
            {
                m_list.get(i).addToPanel(x, 22 + 50, m_layer);
                x += GoalItem.GOAL_WIDTH + GOAL_SPACING;
            }
        }
    }
    
    public static class GoalItemState
    {
        public boolean completed;
        public int currGoal;    // used by ChainGoalItem
    }
    
    public abstract static class GoalItem
    {
        public static double SHAPE_SIZE = 40;
        public static double GOAL_WIDTH = 40;
        
        protected Context ctx;
        
        private Text m_text;
        private IPrimitive<?> m_shape;

        private Group m_group;
        private PolyLine m_check;
        
        protected boolean m_completed;
        
        public GoalItem(Context ctx)
        {
            this.ctx = ctx;            
        }
        
        public void madeChain(int length, int color)
        {
            // override
        }

        public boolean isCompleted()
        {
            return m_completed;
        }
        
        public GoalItemState copyState()
        {
            GoalItemState undoState = new GoalItemState();
            undoState.completed = m_completed;
            return undoState;
        }
        
        public void restoreState(GoalItemState undoState)
        {
            m_completed = undoState.completed;
            updateText();
        }

        public void addToPanel(double x, double y, Layer layer)
        {
            m_shape = createShape();
            m_shape.setX(GOAL_WIDTH / 2);
            m_shape.setY(GOAL_WIDTH / 2);
            
            m_text = createText();
            
            m_check = new PolyLine(-3,-2, 0,1, 6,-6);
            m_check.setX(GOAL_WIDTH / 2);
            m_check.setY(GOAL_WIDTH);
            m_check.setStrokeColor(ColorName.BLACK);
            m_check.setStrokeWidth(2);
            
            m_check.setVisible(false);
            
            m_group = new Group();
            m_group.add(m_shape);
            m_group.add(m_text);
            m_group.add(m_check);
            
            m_group.setX(x);
            m_group.setY(y);
            layer.add(m_group);
            
            updateText();
        }
        
        public void removeFromPanel(Layer layer)
        {
            layer.remove(m_group);
        }
        
        protected abstract IPrimitive<?> createShape();
        protected abstract void updateText();
        
        protected Text createText()
        {
            Text txt = new Text("12 / 30", FONT, "bold", 7);
            txt.setFillColor(ColorName.BLACK);
            txt.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
            txt.setX(GOAL_WIDTH / 2 - 10);
            txt.setY(GOAL_WIDTH - 5);
            return txt;
        }
        
        protected void setText(String txt)
        {
            m_text.setText(txt); //TODO recenter
            BoundingBox box = m_text.getBoundingBox();
            m_text.setX((GOAL_WIDTH - box.getWidth())/ 2);
            
            m_check.setVisible(false);
            m_text.setVisible(true);
        }
        
        protected void setCompleted()
        {
            m_check.setVisible(true);
            m_text.setVisible(false);
            
            m_completed = true;
        }
    }
    
    public static class ChainGoalItem extends GoalItem
    {
        private int m_curr = 0;
        private int[] m_combiLengths;
        private int[] m_colors;
        
        private Group m_group;
        
        public static ChainGoalItem createChainGoal(ChainGoal ch, Context ctx)
        {
            return new ChainGoalItem(ch.getChainLengths(), ch.getColors(), ctx);
        }
        
        public ChainGoalItem(int[] combiLengths, int[] colors, Context ctx)
        {
            super(ctx);
            m_combiLengths = combiLengths;
            m_colors = colors;
        }

        @Override
        public GoalItemState copyState()
        {
            GoalItemState undoState = new GoalItemState();
            undoState.completed = m_completed;
            undoState.currGoal = m_curr;
            return undoState;
        }
        
        @Override
        public void restoreState(GoalItemState undoState)
        {
            m_curr = undoState.currGoal;
            m_completed = undoState.completed;
            if (m_completed)
            {
                setCompleted();
            }
            else
            {
                updateDots();
                updateText();
            }
        }
        
        @Override
        public void madeChain(int length, int color)
        {
            if (length < m_combiLengths[m_curr] || color != m_colors[m_curr])
                return;
            
            m_curr++;
            if (m_curr >= m_colors.length)
            {
                setCompleted();
            }
            else
            {
                updateDots();
            }
        }
        
        @Override
        protected IPrimitive<?> createShape()
        {
            m_group = new Group();
            
            updateDots();
            
            return m_group;
        }

        protected void updateDots()
        {
            m_group.removeAll();
            
            double sz = SHAPE_SIZE / 2;
            double s = sz / 4;
            
            for (int i = 0; i < 3; i++)
            {
                Dot dot = new Dot(m_colors[m_curr]);
                dot.setContext(ctx);
                
                int dx = (i & 1) == 1 ? 1 : -1;
                int dy = (i & 2) == 2 ? 1 : -1;
                IPrimitive<?> shape = dot.createShape(sz);
                shape.setX(dx * s);
                shape.setY(dy * s);
                
                m_group.add(shape);
            }
        }
        
        @Override
        protected void updateText()
        {
            if (!isCompleted())
                setText("" + m_combiLengths[m_curr]);
        }
    }
    
    public static class DotGoal extends GoalItem
    {
        private int m_color;
        private int m_goal;
        
        public DotGoal(int color, int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
            m_color = color;
        }
        
        protected IPrimitive<?> createShape()
        {
            Dot dot = new Dot(m_color);
            dot.setContext(ctx);
            return dot.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            int got = ctx.score.getExplodedDots()[m_color];
            if (got >= m_goal)
                setCompleted();
            else
                setText(got + " / " + m_goal);
        }
    }
    
    public static class AnchorGoal extends GoalItem
    {
        private int m_goal;
        
        public AnchorGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Anchor anchor = new Anchor(false);
            anchor.setContext(ctx);
            return anchor.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getAnchorsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getDroppedAnchors();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class SpiderGoal extends GoalItem
    {
        private int m_goal;
        
        public SpiderGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Spider s = new Spider();
            s.setContext(ctx);
            return s.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getSpidersInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedSpiders();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class ChestGoal extends GoalItem
    {
        private int m_goal;
        
        public ChestGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Chest s = new Chest(null, 1);
            s.setContext(ctx);
            return s.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getChestsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getOpenedChests();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class WrappedDotGoal extends GoalItem
    {
        private int m_goal;
        
        public WrappedDotGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            WrappedDot s = new WrappedDot(0);
            s.setContext(ctx);
            return s.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            int got = ctx.score.getUsedWrappedDots();
            if (got >= m_goal)
                setCompleted();
            else
                setText(got + " / " + m_goal);
        }
    }
    
    public static class StripedGoal extends GoalItem
    {
        private int m_goal;
        
        public StripedGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Striped s = new Striped(1, false);
            s.setContext(ctx);
            return s.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            int got = ctx.score.getUsedStriped();
            if (got >= m_goal)
                setCompleted();
            else
                setText(got + " / " + m_goal);
        }
    }
    
    public static class DiamondGoal extends GoalItem
    {
        private int m_goal;
        
        public DiamondGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Diamond d = new Diamond();
            d.setContext(ctx);
            return d.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getDiamondsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getDroppedDiamonds();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class KnightGoal extends GoalItem
    {
        private int m_goal;
        
        public KnightGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Knight knight = new Knight(1, false);
            knight.setContext(ctx);
            return knight.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getKnightsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedKnights();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class ClockGoal extends GoalItem
    {
        private int m_goal;
        
        public ClockGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Clock clock = new Clock(1, false);
            clock.setContext(ctx);
            return clock.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getClocksInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getDroppedClocks();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class AnimalGoal extends GoalItem
    {
        private int m_goal;
        
        public AnimalGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Animal animal = new Animal(0, 0, Animal.Type.DEFAULT, false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE * 0.8);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getAnimalsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedAnimals();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class CoinGoal extends GoalItem
    {
        private int m_goal;
        
        public CoinGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Coin coin = new Coin();
            coin.setContext(ctx);
            return coin.createShape(SHAPE_SIZE * 0.8);
        }
        
        protected void updateText()
        {
//            if (m_goal == Goal.ALL)
//            {
//                int gen = ctx.score.getAnimalsInGrid();
//                if (gen > 0)
//                    setText("" + gen);
//                else 
//                    setCompleted();
//            }
//            else
//            {
                int got = ctx.score.getExplodedCoins();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
//            }
        }
    }
    
    public static class IceGoal extends GoalItem
    {
        private int m_goal;
        
        public IceGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Group g = new Group();
            
            double sz = SHAPE_SIZE * 0.65;
            Rectangle ice = new Rectangle(sz, sz);
            ice.setX(- sz / 2);
            ice.setY(- sz / 2); 
            
            int r = 255 - 3 * 20;
            if (r < 0) r = 0;
            ice.setFillColor(new Color(r, r, r));
            g.add(ice);
            
            Text txt = new Text("ICE");
            txt.setFontSize(7);
            txt.setFontStyle("bold");
            txt.setFillColor(ColorName.BLACK);
            txt.setTextBaseLine(TextBaseLine.MIDDLE);
            txt.setTextAlign(TextAlign.CENTER);
            
            g.add(txt);
            
            return g;            
        }
        
        protected void updateText()
        {
            int goal = m_goal == Goal.ALL ? ctx.score.getInitialIce() : m_goal;
            int got = ctx.score.getExplodedIce();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class ScoreGoal extends GoalItem
    {
        private int m_goal;
        
        public ScoreGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Group g = new Group();
            
            double h = SHAPE_SIZE * 0.65;
            double w = SHAPE_SIZE;
            Rectangle ice = new Rectangle(w, h);
            ice.setX(-w / 2);
            ice.setY(-h / 2); 
            
            int r = 255 - 3 * 20;
            if (r < 0) r = 0;
            ice.setFillColor(new Color(r, r, r));
            g.add(ice);
            
            Text txt = new Text("SCORE");
            txt.setFontSize(7);
            txt.setFontStyle("bold");
            txt.setFillColor(ColorName.BLACK);
            txt.setTextBaseLine(TextBaseLine.MIDDLE);
            txt.setTextAlign(TextAlign.CENTER);
            
            g.add(txt);
            
            return g;            
        }
        
        protected void updateText()
        {
            int goal = m_goal;
            int got = ctx.score.getScore();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class WordsGoal extends GoalItem
    {
        private int m_goal;
        
        public WordsGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Group g = new Group();
            
            double h = SHAPE_SIZE * 0.65;
            double w = SHAPE_SIZE;
            Rectangle rect = new Rectangle(w, h);
            rect.setX(-w / 2);
            rect.setY(-h / 2); 
            
            int r = 255 - 3 * 20;
            if (r < 0) r = 0;
            rect.setFillColor(new Color(r, r, r));
            g.add(rect);
            
            Text txt = new Text("WORDS");
            txt.setFontSize(7);
            txt.setFontStyle("bold");
            txt.setFillColor(ColorName.BLACK);
            txt.setTextBaseLine(TextBaseLine.MIDDLE);
            txt.setTextAlign(TextAlign.CENTER);
            
            g.add(txt);
            
            return g;            
        }
        
        protected void updateText()
        {
            int goal = m_goal;
            int got = ctx.score.getWordCount();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class DoorGoal extends GoalItem
    {
        private int m_goal;
        
        public DoorGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Door animal = new Door(1, Direction.NONE, 0);
            double sz = SHAPE_SIZE * 0.6;
            
            Group wrap = new Group();
            
            Group shape = animal.createShape(sz);
            shape.setX(-sz/2);
            shape.setY(-sz/2);
            
            wrap.add(shape);
            return wrap;
        }
        
        protected void updateText()
        {
            int goal = m_goal == Goal.ALL ? ctx.score.getInitialDoors() : m_goal;
            int got = ctx.score.getExplodedDoors();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class BlockerGoal extends GoalItem
    {
        private int m_goal;
        
        public BlockerGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Blocker sg = new Blocker(1, false, false);
            sg.setContext(ctx);
            return sg.createShape(SHAPE_SIZE * 0.8); 
        }

        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getBlockersInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedBlockers();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }    
    
    
    public static class ZapBlockerGoal extends GoalItem
    {
        private int m_goal;
        
        public ZapBlockerGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Blocker sg = new Blocker(1, false, true);
            sg.setContext(ctx);
            return sg.createShape(SHAPE_SIZE * 0.8); 
        }

        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getZapBlockersInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedZapBlockers();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }    
    
    public static class CageGoal extends GoalItem
    {
        private int m_goal;
        
        public CageGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Cage animal = new Cage(1, false);
            double sz = SHAPE_SIZE * 0.6;
            
            Group wrap = new Group();
            
            Group shape = animal.createShape(sz);
            shape.setX(-sz/2);
            shape.setY(-sz/2);
            
            wrap.add(shape);
            return wrap;
        }
        
        protected void updateText()
        {
            int goal = m_goal == Goal.ALL ? ctx.score.getInitialCages() : m_goal;
            int got = ctx.score.getExplodedCages();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    
    public static class BubbleGoal extends GoalItem
    {
        private int m_goal;
        
        public BubbleGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Bubble animal = new Bubble();
            double sz = SHAPE_SIZE * 0.6;
            
            Group wrap = new Group();
            
            Group shape = animal.createShape(sz);
            shape.setX(-sz/2);
            shape.setY(-sz/2);
            
            wrap.add(shape);
            return wrap;
        }
        
        protected void updateText()
        {
            int goal = m_goal == Goal.ALL ? ctx.score.getInitialBubbles() : m_goal;
            int got = ctx.score.getExplodedBubbles();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class CircuitGoal extends GoalItem
    {
        private int m_goal;
        
        public CircuitGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            CircuitCell circuit = new CircuitCell();
            double sz = SHAPE_SIZE * 0.6;
            
            Group wrap = new Group();
            
            Group shape = circuit.createOnShape(sz);
//            shape.setX(-sz/2);
//            shape.setY(-sz/2);
            
            wrap.add(shape);
            return wrap;
        }
        
        protected void updateText()
        {
            int goal = m_goal == Goal.ALL ? ctx.score.getInitialCircuits() : m_goal;
            int got = ctx.score.getExplodedCircuits();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class LaserGoal extends GoalItem
    {
        private int m_goal;
        
        public LaserGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Laser laser = new Laser(Direction.EAST, false);
            laser.setContext(ctx);
            return laser.createShape(SHAPE_SIZE * 0.8);           
        }
        
        protected void updateText()
        {
            int goal = m_goal;
            int got = ctx.score.getShortCircuitedLasers();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class BirdGoal extends GoalItem
    {
        private int m_goal;
        
        public BirdGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Picture p = new Picture("images/chicken.png");
            p.setX(-16);
            p.setY(-16);
            
            Group g = new Group();
            g.add(p);
            return g;
            
//            Egg egg = new Egg(true);
//            egg.setContext(ctx);
//            return egg.createShape(SHAPE_SIZE * 0.8);           
        }
        
        protected void updateText()
        {
            int goal = m_goal;
            int got = ctx.score.getBirds();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class DominoGoal extends GoalItem
    {
        private int m_goal;
        
        public DominoGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Domino domino = new Domino(1, 2, true, false);
            domino.setContext(ctx);
            return domino.createShape(SHAPE_SIZE * 0.8);           
        }
        
        protected void updateText()
        {
            int goal = m_goal;
            int got = ctx.score.getExplodedDominoes();
            
            if (got >= goal)
                setCompleted();
            else
                setText(got + " / " + goal);
        }
    }
    
    public static class FireGoal extends GoalItem
    {
        private int m_goal;
        
        public FireGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Fire animal = new Fire(false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getFireInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedFire();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class MirrorGoal extends GoalItem
    {
        private int m_goal;
        
        public MirrorGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Mirror animal = new Mirror(false, false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getMirrorsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedMirrors();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class RocketGoal extends GoalItem
    {
        private int m_goal;
        
        public RocketGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Rocket animal = new Rocket(Direction.EAST, false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
            if (m_goal == Goal.ALL)
            {
                int gen = ctx.score.getRocketsInGrid();
                if (gen > 0)
                    setText("" + gen);
                else 
                    setCompleted();
            }
            else
            {
                int got = ctx.score.getExplodedRockets();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
            }
        }
    }
    
    public static class BlasterGoal extends GoalItem
    {
        private int m_goal;
        
        public BlasterGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Blaster animal = new Blaster(false, false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
//            if (m_goal == Goal.ALL)
//            {
//                int gen = ctx.score.getBlasters();
//                if (gen > 0)
//                    setText("" + gen);
//                else 
//                    setCompleted();
//            }
//            else
//            {
                int got = ctx.score.getUsedBlasters();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
//            }
        }
    }
    
    public static class BombGoal extends GoalItem
    {
        private int m_goal;
        
        public BombGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            Bomb animal = new Bomb();
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
//            if (m_goal == Goal.ALL)
//            {
//                int gen = ctx.score.getRocketsInGrid();
//                if (gen > 0)
//                    setText("" + gen);
//                else 
//                    setCompleted();
//            }
//            else
//            {
                int got = ctx.score.getUsedBombs();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
//            }
        }
    }
    
    public static class ColorBombGoal extends GoalItem
    {
        private int m_goal;
        
        public ColorBombGoal(int need, Context ctx)
        {
            super(ctx);
            
            m_goal = need;
        }
        
        protected IPrimitive<?> createShape()
        {
            ColorBomb animal = new ColorBomb(false);
            animal.setContext(ctx);
            return animal.createShape(SHAPE_SIZE);
        }
        
        protected void updateText()
        {
//            if (m_goal == Goal.ALL)
//            {
//                int gen = ctx.score.getRocketsInGrid();
//                if (gen > 0)
//                    setText("" + gen);
//                else 
//                    setCompleted();
//            }
//            else
//            {
                int got = ctx.score.getUsedColorBombs();
                if (got >= m_goal)
                    setCompleted();
                else
                    setText(got + " / " + m_goal);
//            }
        }
    }
}
