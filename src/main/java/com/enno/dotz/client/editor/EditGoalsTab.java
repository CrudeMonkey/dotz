package com.enno.dotz.client.editor;

import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.Goal;
import com.enno.dotz.client.item.Dot;
import com.enno.dotz.client.ui.MXCheckBox;
import com.enno.dotz.client.ui.MXForm;
import com.enno.dotz.client.ui.MXTextInput;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.form.fields.CanvasItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class EditGoalsTab extends VLayout
{
    private Num m_moves;
    private Num m_score;
    private TimeItem m_time;
    private All  m_circuits;
    private All m_ice;
    private All m_fire;
    private All m_animals;
    private All m_knights;
    private All m_anchors;
    private All m_clocks;
    private All m_doors;
    private All m_mirrors;
    private All m_rockets;
    private Num m_lasers;

    private DotAll[] m_dots = new DotAll[Config.MAX_COLORS];
    private FormItem[] m_fields;

    public EditGoalsTab(boolean isNew, Config level)
    {
        m_moves = new Num("Moves") {
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setMaxMoves(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getMaxMoves());
            }
        };        
        m_score = new Num("Score") {
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setScore(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getScore());
            }
        };               
        m_time = new TimeItem();

        m_lasers = new Num("Lasers") {
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setLasers(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getLasers());
            }
        };
        m_circuits = new All("Circuits") {
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setCircuits(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getCircuits());
            }
        };
            
        m_ice = new All("Ice") {
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setIce(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getIce());
            }
        };
            
        m_fire = new All("Fire"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setFire(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getFire());
            }
        };
            
        m_anchors = new All("Anchors"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setAnchors(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getAnchors());
            }
        };
            
        m_doors = new All("Doors"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setDoors(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getDoors());
            }};
            
        m_animals = new All("Animals"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setAnimals(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getAnimals());
            }
        };

        m_knights = new All("Knights"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setKnights(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getKnights());
            }
        };

        m_clocks = new All("Clocks"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setClocks(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getClocks());
            }
        };

        m_mirrors = new All("Mirrors"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setMirrors(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getMirrors());
            }
        };

        m_rockets = new All("Rockets"){
            @Override
            public void prepareSave(Goal goal)
            {
                goal.setRockets(val());
            }

            @Override
            public void initGoal(Goal goal)
            {
                val(goal.getRockets());
            }
        };

        for (int i = 0; i < Config.MAX_COLORS; i++)
        {
            m_dots[i] = new DotAll(i);
        }
        
        MXForm form = new MXForm();
        form.setNumCols(6);
        
        Context ctx = new Context(); // just used for drawing the dots
        ctx.cfg = new Config();      //TODO could copy the colors later
        
        m_fields = new FormItem[] {
                new DotImageItem(0, ctx), m_dots[0], m_anchors,  m_time,
                new DotImageItem(1, ctx), m_dots[1], m_ice,      m_lasers,   
                new DotImageItem(2, ctx), m_dots[2], m_animals,  m_rockets,
                new DotImageItem(3, ctx), m_dots[3], m_doors,    m_knights,
                new DotImageItem(4, ctx), m_dots[4], m_fire,     m_clocks,
                new DotImageItem(5, ctx), m_dots[5], m_circuits, m_mirrors,
                m_moves, m_score
        };
        
        form.setFields(m_fields);
        
        addMember(form);
        
        if (!isNew)
        {
            Goal goal = level.goals;
            
            for (int i = 0; i < m_fields.length; i++)
            {
                if (!(m_fields[i] instanceof GoalField))
                    continue;
                
                ((GoalField) m_fields[i]).initGoal(goal);
            }
        }
    }

    public boolean validate()
    {
        for (int i = 0; i < m_fields.length; i++)
        {
            if (!(m_fields[i] instanceof GoalField))
                continue;
                
            if (!((GoalField) m_fields[i]).validateGoal())
                return false;
        }
        return true;
    }

    public void prepareSave(Config level)
    {
        Goal g = level.goals;
        if (g == null)
            level.goals = g = new Goal();
        
        for (int i = 0; i < m_fields.length; i++)
        {
            if (!(m_fields[i] instanceof GoalField))
                continue;
            
            ((GoalField) m_fields[i]).prepareSave(g);
        }
    }
    
    public interface GoalField
    {
        public void initGoal(Goal goal);
        public boolean validateGoal();
        public void prepareSave(Goal goal);
    }

    public abstract static class Check extends MXCheckBox implements GoalField
    {
        public Check(String title)
        {
            setTitle(title);
            setLabelAsTitle(true);
        }
        
        @Override
        public boolean validateGoal()
        {
            return true;
        }
    }
    
    public abstract static class All extends CanvasItem implements GoalField
    {
        private static String SOME = "Some:";
        
        private RadioGroupItem m_all;
        private MXTextInput m_text;
        
        public All(String title)
        {
            setTitle(title);
            
            MXForm form = new MXForm();
            form.setNumCols(4);
            form.setColWidths(70, 20, 60);
            
            m_all = new RadioGroupItem();
            m_all.setValueMap("All", SOME);
            m_all.setVertical(false);
            m_all.setShowTitle(false);
            m_all.setValue(SOME);
            
            m_all.addChangedHandler(new ChangedHandler()
            {
                @Override
                public void onChanged(ChangedEvent event)
                {
                    updateTextEnabled();
                }
            });
            
            m_text = new MXTextInput();
            m_text.setShowTitle(false);
            m_text.setWidth(60);
            
            form.setFields(m_all, m_text);
            
            setCanvas(form);
        }
        
        public void val(int val)
        {
            if (val == Goal.ALL)
            {
                m_all.setValue("All");
                m_text.setValue("");
            }
            else
            {
                m_all.setValue(SOME);
                m_text.setValue(val == 0 ? "" : Integer.toString(val));
            }
            updateTextEnabled();
        }
        
        protected void updateTextEnabled()
        {
            m_text.setDisabled(m_all.getValueAsString().equals("All"));
        }
        
        public int val()
        {
            if (m_all.getValueAsString().equals("All"))
            {
                return Goal.ALL;
            }
            else
            {
                try
                {
                    String s = m_text.getValueAsString();
                    if (s == null || s.length() == 0)
                        return 0;
                    
                    return Integer.parseInt(s);
                }
                catch (NumberFormatException e)
                {
                    SC.warn("Invalid goal " + getTitle());
                    return 0;
                }
            }
        }
        
        @Override
        public boolean validateGoal()
        {
            if (m_all.getValueAsString().equals("All"))
            {
                return true;
            }
                        
            String s = m_text.getValueAsString();
            
            if (s == null || s.equals(""))
                return true;
            
            try
            {
                Integer.parseInt(s);
                return true;
            }
            catch (Exception e)
            {
                SC.warn("Invalid goal " + getTitle());
                return false;
            }
        }
    }
    
    public abstract static class Num extends MXTextInput implements GoalField
    {
        public Num(String title)
        {
            setTitle(title);
            setWidth(60);
        }
        
        public void val(int val)
        {
            setValue(val == 0 ? "" : Integer.toString(val));
        }
        
        public int val()
        {
            String s = getValueAsString();
            if (s == null || s.equals(""))
                return 0;
            
            try
            {
                int x = Integer.parseInt(s);
                return x;
            }
            catch (Exception e)
            {
                SC.warn("Invalid goal " + getTitle());
                return 0;
            }
        }
        
        @Override
        public boolean validateGoal()
        {
            String s = getValueAsString();
            if (s == null || s.equals(""))
                return true;
            
            try
            {
                Integer.parseInt(s);
                return true;
            }
            catch (Exception e)
            {
                SC.warn("Invalid goal " + getTitle());
                return false;
            }
        }
    }
    
    public static class TimeItem extends MXTextInput implements GoalField
    {
        public TimeItem()
        {
            setTitle("Time");            
        }
        
        @Override
        public void prepareSave(Goal goal)
        {
            goal.setTime(val());
        }

        @Override
        public void initGoal(Goal goal)
        {
            setValue(format(goal.getTime()));
        }
        
        protected String format(int sec)
        {
            if (sec == 0)
                return "";
            
            int min = sec / 60;
            sec = sec % 60;
            return min + (sec < 10 ? ":0" + sec : ":" + sec);
        }
        
        protected int val() throws NumberFormatException
        {
            String s = getValueAsString();
            if (s == null || s.equals(""))
                return 0;
            
            int colon = s.indexOf(':');
            if (colon != -1)
            {
                return Integer.parseInt(s.substring(0, colon)) * 60 + Integer.parseInt(s.substring(colon + 1));
            }
            else
            {
                return Integer.parseInt(s);
            }
        }
        
        @Override
        public boolean validateGoal()
        {
            try
            {
                val();
                return true;
            }
            catch (Exception e)
            {
                SC.warn("Invalid goal " + getTitle() + "<br>Format e.g. '25' (seconds) or '5:30' (minutes and seconds)");
                return false;
            }
        }
    }
    
    public static class DotImageItem extends CanvasItem
    {
        public static final int ICON_SIZE = 30;
        
        public DotImageItem(int color, Context ctx)
        {
            setWidth(50);
            
            Dot dot = new Dot(color);
            dot.setContext(ctx);
            IPrimitive<?> shape = dot.createShape(ICON_SIZE);
            
            setTitle(null);
            setShowTitle(false);
            setVAlign(VerticalAlignment.CENTER);
            setAlign(Alignment.RIGHT);
            
            int sz = ICON_SIZE;
            LienzoPanel panel = new LienzoPanel(sz, sz);
            
            Layer layer = new Layer();
            panel.add(layer);
            shape.setX(sz/2);
            shape.setY(sz/2);
            layer.add(shape);
            
            Canvas canvas = new Canvas();
            canvas.setWidth(sz);
            canvas.setHeight(sz);
            canvas.addChild(panel);
            setCanvas(canvas);
        }
    }
    
    public static class DotAll extends Num
    {
        private int m_color;
        
        public DotAll(int color)
        {
            super(null);
            setShowTitle(false);
            
            m_color = color;
        }
        
        @Override
        public void prepareSave(Goal goal)
        {
            goal.setDots(m_color, val());
        }
        
        public void initGoal(Goal goal)
        {
            if (goal.getDots()[m_color] > 0)
                setValue("" + goal.getDots()[m_color]);
        }
    }
}
