package com.enno.dotz.client.item;

import java.util.LinkedHashMap;
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
import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;
import com.enno.dotz.client.Config;
import com.enno.dotz.client.Direction;
import com.enno.dotz.client.SoundManager.Sound;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.shared.HandlerRegistration;

public class Animal extends Item
{
    static Random s_blinkRandom = new Random();

    static int BLINK_DURATION = 300;

    public static final int BLACK_ANIMAL = -1;
    
    public enum Type
    {
        DEFAULT("Default", ColorName.WHITE, ColorName.BLACK),
        FOLLOW("Follow", Color.fromColorString("#F2A59C"), ColorName.RED),
        SCARED("Scared", ColorName.LIGHTBLUE, ColorName.BLACK),
        FROZEN("Frozen", ColorName.WHITE, ColorName.BLACK);
        
        private String m_name;
        private IColor m_eyeColor;
        private IColor m_pupilColor;
        
        Type(String name, IColor eyeColor, IColor pupilColor)
        {
            m_name = name;
            m_eyeColor = eyeColor;
            m_pupilColor = pupilColor;
        }
        
        public IColor getEyeColor()
        {
            return m_eyeColor;
        }

        public IColor getPupilColor()
        {
            return m_pupilColor;
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
    
    private Eyes m_eyes;
    private Text m_text;
    
    private boolean m_stunned;
    public int lastDirection = Direction.NONE;
    
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

    public boolean isBlack()
    {
        return m_color == BLACK_ANIMAL;
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
    public Integer getColor()
    {
        return m_color;
    }

    public void setColor(int color)
    {
        m_color = color;
    }

    @Override
    public boolean canBeEaten()
    {
        return false;
    }
    
    @Override
    public boolean canSwap()
    {
        return false;
    }
    
    @Override
    public boolean canBeReplaced()
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
    public boolean canConnect()
    {
        return !isBlack();
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
    
    @Override
    public IPrimitive<?> createShape(double size)
    {
        Group g = new Group();
        
        if (isStuck())
            g.add(createStuckShape(size));
        
        IColor fillColor = m_color == BLACK_ANIMAL ? ColorName.BLACK : (cfg == null ? Config.COLORS[0] : cfg.drawColor(m_color));       // cfg is null in ModePalette
        
        double r = size * 0.4;
        Circle c = new Circle(r);
        c.setFillColor(fillColor);        
        g.add(c);
        
        if (m_action == Action.SWAP)
        {
            // add Mickey ears
            double r2 = r * 0.3;
            double x = (r + r2/2) * Math.cos(Math.PI / 4);
            double y = (r + r2/2) * Math.sin(Math.PI / 4);
            
            Circle c2 = new Circle(r2);
            c2.setFillColor(fillColor);
            c2.setX(x);
            c2.setY(-y);
            g.add(c2);
            
            c2 = new Circle(r2);
            c2.setFillColor(fillColor);
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
            
            horn.setFillColor(fillColor);
            horn.setRotation(angle);
            
            g.add(horn);
            
            horn = new MultiPath();
            horn.M(x, y);
            horn.A(r2*0.6, r2/2, 0, 0, 1, x, y - r2);
            horn.A(r2*0.8, r2*0.8, 0, 0, 0, x, y);
            horn.Z();
            
            horn.setFillColor(fillColor);
            horn.setRotation(-angle);
            
            g.add(horn);
        }

        m_text = new Text("" + m_strength);
        m_text.setFillColor(ColorName.WHITE);
        m_text.setFontSize(7);
        m_text.setFontStyle(FontWeight.BOLD.getCssName());
        m_text.setTextAlign(TextAlign.CENTER);
        m_text.setTextBaseLine(TextBaseLine.MIDDLE);
        m_text.setY(r * 0.5);
        g.add(m_text);
        
        double pupilRadius = r * 0.25;
        double eyeHeight = r * 0.7;
        double eyeWidth = eyeHeight;
        
        if (m_type == Type.FROZEN)      // looks sleepy
        {
            eyeHeight *= 0.5;
            pupilRadius *= 0.5;
        }
        
        IColor strokeColor = m_type == Type.FOLLOW ? ColorName.RED : null;
        m_eyes = new Eyes(BLINK_DURATION, true, eyeWidth, eyeHeight, pupilRadius, 2, m_type.getEyeColor(), strokeColor, m_type.getPupilColor());
        
        for (int i = 0; i < 2; i++)
        {
            double x = r * 0.5 * (i == 0 ? -1 : 1);
            double y = -r * 0.35;
            m_eyes.add(i, x, y, g);
        }
        return g;
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
    public void animate(long t, double x, double y)
    {
        m_eyes.animate(t, x - shape.getX(), y - shape.getY());
    }
    
    public static class Eyes
    {
        private int     m_blinkDuration;    // ms
        private boolean m_hidePupils;
        
        private Ellipse[] m_eyes = new Ellipse[2];
        private Circle[] m_pupils = new Circle[2];
        private double[] m_x = new double[2];
        private double[] m_y = new double[2];
        private double[] m_px = new double[2];  // pupil x
        private double[] m_py = new double[2];  // pupil y
        
        private double m_eyeWidth;
        private double m_eyeHeight;
        private double m_pupilRadius;
        private double m_pupilSize;
        
        private IColor m_fillColor;
        private IColor m_strokeColor;
        private IColor m_pupilColor;
        
        private long m_startBlink;
        
        public Eyes(int blinkDuration, boolean hidePupils, double eyeWidth, double eyeHeight, double pupilRadius, double pupilSize, IColor fillColor, IColor strokeColor, IColor pupilColor)
        {
            m_blinkDuration = blinkDuration;
            m_hidePupils = hidePupils;
            
            m_eyeWidth = eyeWidth;
            m_eyeHeight = eyeHeight;
            m_pupilRadius = pupilRadius;
            m_pupilSize = pupilSize;
            
            m_fillColor = fillColor;
            m_strokeColor = strokeColor;
            m_pupilColor = pupilColor;
        }
        
        public void add(int i, double x, double y, Group g)
        {
            Ellipse eye = new Ellipse(m_eyeWidth, m_eyeHeight);
            eye.setFillColor(m_fillColor);
            if (m_strokeColor != null)
                eye.setStrokeColor(m_strokeColor);
            
//            double x = r * 0.5 * (i == 0 ? -1 : 1);
//            double y = -r * 0.35;
            m_x[i] = x;
            m_y[i] = y;
            
            eye.setX(x);
            eye.setY(y);
            
            g.add(eye);            
            m_eyes[i] = eye;
            
            Circle pupil = new Circle(m_pupilSize);
            pupil.setFillColor(m_pupilColor);
            
            pupil.setX(x);
            pupil.setY(y);
            
            g.add(pupil);
            m_pupils[i] = pupil;
        }
        
        public void animate(long t, double x, double y)
        {
            // pupils track the mouse pointer
            for (int i = 0; i < 2; i++)
            {
                double dx = x - m_x[i];
                double dy = y - m_y[i];
                double angle = Math.atan2(dy, dx);
                m_px[i] = m_x[i] + Math.cos(angle) * m_pupilRadius;
                m_py[i] = m_y[i] + Math.sin(angle) * m_pupilRadius;
                m_pupils[i].setX(m_px[i]);
                m_pupils[i].setY(m_py[i]);
            }

            
            double curr_h = m_eyeHeight;
            
            long dt = t - m_startBlink;
            if (dt > m_blinkDuration)
            {
                m_startBlink = 2 * m_blinkDuration + s_blinkRandom.nextInt(4 * m_blinkDuration) + System.currentTimeMillis();
            }
            else if (dt > 0)
            {
                double half = m_blinkDuration / 2.0;
                double h;
                if (dt < half)
                {
                    h = 1 - dt / half;
                }
                else
                {
                    h = (dt - half) / half;
                }
                
                curr_h = h * m_eyeHeight;
                m_eyes[0].setHeight(curr_h);
                m_eyes[1].setHeight(curr_h);
            }
            
            if (m_hidePupils)
            {
                // Hide the pupils if they're outside the eye's oval
                if (curr_h == m_eyeHeight)  // not blinking
                {
                    m_pupils[0].setVisible(true);
                    m_pupils[1].setVisible(true);
                }
                else
                {
                    //TODO optimize
                    for (int i = 0; i < 2; i++)
                    {
                        double dx = m_pupils[i].getX() - m_x[i];
                        double dy = m_pupils[i].getY() - m_y[i];
                        double d = (dx * dx) / (m_eyeWidth * m_eyeWidth) + (dy * dy) / (curr_h * curr_h);
                        m_pupils[i].setVisible(d < 0.25);   // d * 4 < 1 (i.e. radius * 2 = diameter)
                    }
                }
            }
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
