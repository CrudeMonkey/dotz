package com.enno.dotz.client.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.ait.lienzo.client.core.event.NodeMouseMoveEvent;
import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Ellipse;
import com.ait.lienzo.client.core.shape.Group;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.SoundManager.Sound;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.shared.HandlerRegistration;

public class Animal extends Item
{
    private static List<Animal> s_list = new ArrayList<Animal>();
    private static Random s_blinkRandom = new Random();

    private static int BLINK_DURATION = 300;

    public enum Type
    {
        DEFAULT("Default", ColorName.WHITE, ColorName.BLACK),
        FOLLOW("Follow", ColorName.FIREBRICK, ColorName.WHITE),
        SCARED("Scared", ColorName.LIGHTBLUE, ColorName.BLACK),
        FROZEN("Frozen", ColorName.WHITE, ColorName.BLACK);
        
        private String m_name;
        private IColor m_eyeColor;
        private IColor m_pupilColor;
        
        public IColor getEyeColor()
        {
            return m_eyeColor;
        }

        public IColor getPupilColor()
        {
            return m_pupilColor;
        }

        Type(String name, IColor eyeColor, IColor pupilColor)
        {
            m_name = name;
            m_eyeColor = eyeColor;
            m_pupilColor = pupilColor;
        }
        
        public String getName()
        {
            return m_name;
        }

        public static Type fromName(String name)
        {
            for (Type type : values())
            {
                if (name.equals(type.getName()))
                    return type;
            }
            return DEFAULT;
        }

        public static LinkedHashMap<String,String> getValueMap()
        {
            LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
            for (Type type : values())
            {
                map.put(type.getName(), type.getName());
            }
            return map;
        }
    };
    
    /** What happens when Animal moves into new cell */
    public enum Action
    {
        DEFAULT("Default"), // replace item with dot of Animal's color
        BOMBIFY("Bombify"), // replace Dot with DotBomb
        SWAP("Swap");       // swap item with Animal
        
        private String m_name;

        Action(String name)
        {
            m_name = name;
        }
        
        public String getName()
        {
            return m_name;
        }

        public static Action fromName(String name)
        {
            for (Action type : values())
            {
                if (name.equals(type.getName()))
                    return type;
            }
            return DEFAULT;
        }

        public static LinkedHashMap<String,String> getValueMap()
        {
            LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
            for (Action type : values())
            {
                map.put(type.getName(), type.getName());
            }
            return map;
        }
    };
    
    private int m_strength;
    private int m_color;

    private Type m_type = Type.FOLLOW;
    private Action m_action = Action.DEFAULT;
    
    private Text m_text;
    private Ellipse[] m_eyes = new Ellipse[2];
    private Circle[] m_pupils = new Circle[2];
    
    private double[] m_x = new double[2]; // center of eye
    private double[] m_y = new double[2];
    private double m_pupilRadius;
    
    private boolean m_stunned;
    public int lastDirection = Direction.NONE;
    private long m_startBlink;
    private double m_eyeHeight;
    
    public Animal(int color, int strength, Type type, boolean stuck)
    {
        this(color, strength, type, Action.DEFAULT, stuck);
    }
    
    public Animal(int color, int strength, Type type, Action action, boolean stuck)
    {
        m_strength = strength;
        m_color = color;
        m_type = type;
        m_action = action;
        m_stuck = stuck;
    }

    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean stopsLaser()
    {
        return true;
    }
    
    public int getStrength()
    {
        return m_strength;
    }

    public void setStrength(int strength)
    {
        m_strength = strength;
    }
    
    @Override
    public boolean canIncrementStrength()
    {
        return true;
    }
    
    @Override
    public void incrementStrength(int ds)
    {
        if (m_strength <= 1 && ds == -1)
            return;
        
        m_strength += ds;
        
        if (m_text != null)
            m_text.setText("" + m_strength);
    }
    
    public Type getType()
    {
        return m_type;
    }
    
    public void setType(Type type)
    {
        m_type = type;
    }
    
    public Action getAction()
    {
        return m_action;
    }
    
    public void setAction(Action action)
    {
        m_action = action;
    }
    
    public boolean isStunned()
    {
        if (m_type == Type.FROZEN)
            return true;
        
        return m_stunned;
    }

    public void setStunned(boolean stunned)
    {
        if (m_type == Type.FROZEN)
            return;
        
        m_stunned = stunned;
    }

    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        double r = size * 0.4;
        Circle c = new Circle(r);
        c.setFillColor(cfg.drawColor(m_color));        
        g.add(c);
        
        if (m_action == Action.SWAP)
        {
            // add Mickey ears
            double r2 = r * 0.3;
            double x = (r + r2/2) * Math.cos(Math.PI / 4);
            double y = (r + r2/2) * Math.sin(Math.PI / 4);
            
            Circle c2 = new Circle(r2);
            c2.setFillColor(cfg.drawColor(m_color));
            c2.setX(x);
            c2.setY(-y);
            g.add(c2);
            
            c2 = new Circle(r2);
            c2.setFillColor(cfg.drawColor(m_color));
            c2.setX(-x);
            c2.setY(-y);
            g.add(c2);
        }
        else if (m_action == Action.BOMBIFY)
        {
            // add devil horns
            double r2 = r * 0.8;
            double x = 0;
            double y = r2/2 - r;
            double angle = Math.PI * 0.15;
            
            MultiPath horn = new MultiPath();
            horn.M(x, y);
            horn.A(r2*0.6, r2/2, 0, 0, 0, x, y - r2);
            horn.A(r2*0.8, r2*0.8, 0, 0, 1, x, y);
            horn.Z();
            
            horn.setFillColor(cfg.drawColor(m_color));
            horn.setRotation(angle);
            
            g.add(horn);
            
            horn = new MultiPath();
            horn.M(x, y);
            horn.A(r2*0.6, r2/2, 0, 0, 1, x, y - r2);
            horn.A(r2*0.8, r2*0.8, 0, 0, 0, x, y);
            horn.Z();
            
            horn.setFillColor(cfg.drawColor(m_color));
            horn.setRotation(-angle);
            
            g.add(horn);
        }

        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.WHITE);
        m_text.setFontSize(7);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setY(5);
        m_text.setTextBaseLine(TextBaseLine.TOP); // y position is position of top of the text
        g.add(m_text);
        
        m_pupilRadius = r * 0.25;
        m_eyeHeight = r * 0.7;
        double eyeWidth = m_eyeHeight;
        
        if (m_type == Type.FROZEN)      // looks sleepy
        {
            m_eyeHeight *= 0.5;
            m_pupilRadius *= 0.5;
        }
        
        for (int i = 0; i < 2; i++)
        {
            Ellipse eye = new Ellipse(eyeWidth, m_eyeHeight);
            eye.setFillColor(m_type.getEyeColor());
            
            double x = r * 0.5 * (i == 0 ? -1 : 1);
            double y = -r * 0.35;
            m_x[i] = x;
            m_y[i] = y;
            
            eye.setX(x);
            eye.setY(y);
            
            g.add(eye);            
            m_eyes[i] = eye;
            
            Circle pupil = new Circle(2);
            pupil.setFillColor(m_type.getPupilColor());
            
            pupil.setX(x);
            pupil.setY(y);
            
            g.add(pupil);
            m_pupils[i] = pupil;
        }
        return g;
    }
    
    @Override
    public Integer getColor()
    {
        return m_color;
    }
    
    @Override
    public boolean canConnect()
    {
        return true;
    }
    
    @Override
    protected Item doCopy()
    {
        Animal a = new Animal(m_color, m_strength, m_type, m_action, m_stuck);
        a.lastDirection = lastDirection;
        a.m_stunned = m_stunned;
        return a;
    }

    @Override
    public ExplodeAction explode(Integer color, int chainSize)
    {
        m_stunned = true;
        
        m_strength -= chainSize;
        if (m_strength <= 0)
        {
            Sound.ANIMAL_DIED.play();
            ctx.score.explodedAnimal(m_color);
            return ExplodeAction.REMOVE; // remove animal
        }
        
        m_text.setText("" + m_strength);
        return ExplodeAction.NONE; // don't remove animal
    }

    @Override
    public void addShapeToLayer(Layer layer)
    {
        super.addShapeToLayer(layer);        
        s_list.add(this);
    }

    @Override
    public void removeShapeFromLayer(Layer layer)
    {
        s_list.remove(this);
        super.removeShapeFromLayer(layer);
    }

    @Override
    public void animate(long t, double x, double y)
    {
        long dt = t - m_startBlink;
        if (dt > BLINK_DURATION)
        {
            m_startBlink = 2 * BLINK_DURATION + s_blinkRandom.nextInt(4 * BLINK_DURATION) + System.currentTimeMillis();
        }
        else if (dt > 0)
        {
            double half = BLINK_DURATION / 2.0;
            double h;
            if (dt < half)
            {
                h = 1 - dt / half;
            }
            else
            {
                h = (dt - half) / half;
            }
            m_eyes[0].setHeight(h * m_eyeHeight);
            m_eyes[1].setHeight(h * m_eyeHeight);
        }
        
        // pupils track the mouse pointer
        for (int i = 0; i < 2; i++)
        {
            double dx = x - m_x[i] - shape.getX();
            double dy = y - m_y[i] - shape.getY();
            double angle = Math.atan2(dy, dx);
            double nx = m_x[i] + Math.cos(angle) * m_pupilRadius;
            double ny = m_y[i] + Math.sin(angle) * m_pupilRadius;
            m_pupils[i].setX(nx);
            m_pupils[i].setY(ny);
        }
    }
    
    public static class EyeTracker
    {
        private int m_x, m_y;
        private Layer m_layer;
        private NodeMouseMoveHandler m_moveHandler;
        private HandlerRegistration m_moveReg;

        public EyeTracker(Layer layer)
        {
            m_layer = layer;
            
            m_moveHandler = new NodeMouseMoveHandler() {

                @Override
                public void onNodeMouseMove(NodeMouseMoveEvent event)
                {
                    m_x = event.getX();
                    m_y = event.getY();
                }
            };
        }
        
        public double getX()
        {
            return m_x;
        }
        
        public double getY()
        {
            return m_y;
        }
        
        public void start()
        {
            m_moveReg = m_layer.addNodeMouseMoveHandler(m_moveHandler);
        }
        
        public void stop()
        {
            m_moveReg.removeHandler();
        }
    }
}
