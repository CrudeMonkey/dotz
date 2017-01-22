package com.enno.dotz.client.anim;

import java.util.Random;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.shape.IPrimitive;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.item.Item;
import com.enno.dotz.client.util.Debug;

public abstract class Transition implements IAnimationCallback
{
    double from_x, from_y, dx, dy;
    public Item item;
    boolean bounce;
    double bounceFactor = 0.6;
    double bounceUp = 0.5;
    
    protected IPrimitive<?> m_shape;
    
    protected Transition(double from_x, double from_y, double to_x, double to_y)
    {
        this.from_x = from_x;
        this.from_y = from_y;
        this.dx = to_x - from_x;
        this.dy = to_y - from_y;
    }
    
    protected Transition(double from_x, double from_y, double to_x, double to_y, Item item)
    {
        this(from_x, from_y, to_x, to_y, item.shape);
        
        this.item = item;        
    }
    
    protected Transition(double from_x, double from_y, double to_x, double to_y, IPrimitive<?> shape)
    {
        this(from_x, from_y, to_x, to_y);
        
        m_shape = shape;        
    }
    
    public boolean containsItem(Item item)
    {
        return this.item == item;
    }
    
    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        //p("[" + from_x + "," + from_y + "] [" + to_x + "," + to_y + "]");
        move(0);
        afterStart();
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {
        move(animation.getPercent());
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        move(1);
        afterEnd();
    }
    
    protected void move(double pct)
    {
        double x, y;
        if (bounce)
        {
            if (pct < bounceFactor)
            {
                x = from_x + pct * dx / bounceFactor;
                y = from_y + pct * dy / bounceFactor;
            }
            else
            {
                double left = pct - bounceFactor;
                double hl = (1 - bounceFactor) / 2;
                if (left < hl)
                {
                    // bounce up
                    double t = 1 - (left / hl) * bounceUp;
                    x = from_x + t * dx;
                    y = from_y + t * dy;
                }
                else
                {
                    // bounce down
                    left -= hl;
                    double t = 1 - bounceUp + (left / hl) * bounceUp;
                    x = from_x + t * dx;
                    y = from_y + t * dy;
                }
            }
        }
        else
        {
            x = from_x + pct * dx;
            y = from_y + pct * dy;
        }
        moveShape(x, y);
    }
    
    protected void moveShape(double x, double y)
    {
        m_shape.setX(x);
        m_shape.setY(y);
    }
    
    public void afterStart()
    {
    }
    
    public void afterEnd()
    {
    }
    
    public static class DropTransition extends Transition
    {
        public DropTransition(double from_x, double from_y, double to_x, double to_y, Item item)
        {
            super(from_x, from_y, to_x, to_y, item);
        }
    }

    protected static Random s_rnd = new Random();
    
    public static class ArchTransition extends Transition
    {
        private final double DX = 50;
        
        private final double SLACK = 0.2;
        private final double SCALE = 1 - SLACK;
        
        private double[] m_abc;

        private double m_start;
        private double m_end;

        private boolean m_fadeOut;

        public ArchTransition(double from_x, double from_y, double to_x, double to_y, double dy, Item item)
        {
            this(from_x, from_y, to_x, to_y, dy, item.shape);
        }
        
        public ArchTransition(double from_x, double from_y, double to_x, double to_y, double dy, IPrimitive<?> shape)
        {
            super(from_x, from_y, to_x, to_y, shape);
            
            double top = Math.min(from_y, to_y) + dy;
            
            if (dx == 0)
                m_abc = test(from_x, from_y, to_x + DX, to_y, top);
            else
                m_abc = test(from_x, from_y, to_x, to_y, top);
            
            m_start = s_rnd.nextDouble() * SLACK;
            m_end = m_start + SCALE;
        }
        
        public IAnimationCallback fadeOut()
        {
            m_fadeOut = true;
            return this;
        }
        
        @Override
        protected void move(double pct)
        {
            if (pct < m_start)
                move2(0);
            else if (pct >= m_end)
                move2(1);
            else
            {
                move2((pct - m_start) / SCALE);
            }
        }
        
        protected void move2(double pct)
        {
            if (dx == 0)
            {
                double x = from_x + pct * DX;
                double y = m_abc[0] * (x + m_abc[1]) * (x + m_abc[1]) + m_abc[2];
                moveShape(from_x, y);
            }
            else
            {
                double x = from_x + pct * dx;
                double y = m_abc[0] * (x + m_abc[1]) * (x + m_abc[1]) + m_abc[2];
                moveShape(x, y);
            }
            
            if (m_fadeOut)
                m_shape.setAlpha(1 - pct);
        }

        // http://math.stackexchange.com/questions/1257301/formula-of-parabola-from-two-points-and-the-y-coordinate-of-the-vertex
        public static double[] solve(double x1, double y1, double x2, double y2, double y0)
        {
            // y = a(x + b)^2 + y0
            // y1 = a(x1 + b)^2 + y0
            // y2 = a(x2 + b)^2 + y0
            // 
            // a = (y1-y0)/(x1+b)^2
            // (y1-y0)/(x1+b)^2 * (x2 + b)^2 = y2 - y0
            // (y1-y0) * (x2+b)^2 = (y2-y0) * (x1+b)^2
            // 
            
            double A = y1 - y2;
            
            if (A == 0)
            {
                double b = -(x1 + x2) / 2;
                double a = (y1 - y0) / ((x1 + b) * (x1 + b));
                return new double[] { a, b, y0 };
            }
            
            double B = 2 * x2 * (y1 - y0) - 2 * x1 * (y2 - y0);
            double C = x2 * x2 * (y1 - y0) - x1 * x1 * (y2 - y0);
            
            double D = Math.sqrt(B * B - 4 * A * C);
            double b1 = (-B + D) / (2 * A);
            double b2 = (-B - D) / (2 * A);
            
            double x0 = -b1;
            if (x0 >= x1 && x0 <= x2 || x0 <= x1 && x0 >= x2)
            {
                double a = (y1 - y0) / ((x1 + b1) * (x1 + b1));
                double b = b1;
                return new double[] { a, b, y0 };
            }
            else
            {
                double a = (y1 - y0) / ((x1 + b2) * (x1 + b2));
                double b = b2;                
                return new double[] { a, b, y0 };
            }
        }
        
        public static double[] test(double x1, double y1, double x2, double y2, double y0)
        {
            double[] abc = solve(x1, y1, x2, y2, y0);
            
            double _y1 = abc[0] * (x1 + abc[1]) * (x1 + abc[1]) + abc[2];
            double _y2 = abc[0] * (x2 + abc[1]) * (x2 + abc[1]) + abc[2];

            if (!eq(_y1, y1) || !eq(_y2, y2))
            {
                p(x1 + "," + y1 + " " + x2 + "," + y2 + "  y0=" + y0);
                p("y1=" + _y1 + " was " + y1);
                p("y2=" + _y2 + " was " + y2);
            }            
            return abc;
        }

        private static void p(String s)
        {
            Debug.p(s);
            System.out.println(s);
        }

        private static boolean eq(double a, double b)
        {
            return Math.abs(a - b) < 0.01;
        }
    }

    public static class RollTransition extends Transition
    {
        public RollTransition(double from_x, double from_y, double to_x, double to_y, Item item)
        {
            super(from_x, from_y, to_x, to_y, item);
        }
        
        @Override
        protected void move(double pct)
        {
            if (pct < 0.5)
            {
                double x = from_x + pct * 2 * dx;
                moveShape(x, from_y);
            }
            else
            {
                double y = from_y + (pct - 0.5) * 2 * dy;
                moveShape(from_x + dx, y);
            }
        }
    }

    public static class GrowFireTransition extends Transition
    {
        public GrowFireTransition(double from_x, double from_y, double to_x, double to_y, Item item)
        {
            super(from_x, from_y, to_x, to_y, item);
        }
    }
    
    public static class MoveAnimalTransition extends Transition
    {
        public MoveAnimalTransition(double from_x, double from_y, double to_x, double to_y, Item item)
        {
            super(from_x, from_y, to_x, to_y, item);
        }
    }
    
    public static class RotateTransition extends Transition
    {
        public RotateTransition(double from_x, double from_y, double to_x, double to_y, Item item)
        {
            super(from_x, from_y, to_x, to_y, item);
        }
    }
    
    public static class ExplosionTransition extends Transition
    {
        private Line m_line;
        private Context ctx;
        
        public ExplosionTransition(double from_x, double from_y, double to_x, double to_y, Context ctx)
        {
            super(from_x, from_y, to_x, to_y);
            this.ctx = ctx;
        }
        
        @Override
        public void afterStart()
        {
            m_line = new Line(from_x, from_y, from_x, from_y);
            m_line.setStrokeColor(ColorName.BLACK);
            m_line.setStrokeWidth(5);
            m_line.setDashArray(2, 2);
            ctx.nukeLayer.add(m_line);
        }
        
        @Override
        public void afterEnd()
        {
            ctx.nukeLayer.remove(m_line);
        }
        
        @Override
        protected void move(double pct)
        {
            if (m_line == null)
                return; // m_line doesn't exists yet at move(0)
            
            Point2DArray pts = m_line.getPoints();
            Point2D pt = pts.get(1);
            pt.setX(from_x + dx * pct);
            pt.setY(from_y + dy * pct);
            m_line.setPoints(pts);
        }
    }
    
    public static class BlastTransition extends Transition
    {
        private Line[] m_line = new Line[2];
        private Context ctx;
        private boolean m_isWide;
        
        public BlastTransition(double from_x, double from_y, double to_x, double to_y, boolean isWide, Context ctx)
        {
            super(from_x, from_y, to_x, to_y);
            this.ctx = ctx;
            m_isWide = isWide;
        }
        
        @Override
        public void afterStart()
        {
            for (int i = 0; i < 2; i++)
            {
                m_line[i] = new Line(from_x, from_y, from_x, from_y);
                m_line[i].setStrokeColor(ColorName.BLACK);
                m_line[i].setStrokeWidth(m_isWide ? 30 + 2 * ctx.cfg.size : 30);
                m_line[i].setDashArray(1, 1);
                if (i == 0)
                    m_line[i].setDashOffset(1); // align left/right sides
                ctx.nukeLayer.add(m_line[i]);
            }
        }
        
        @Override
        public void afterEnd()
        {
            ctx.nukeLayer.remove(m_line[0]);
            ctx.nukeLayer.remove(m_line[1]);
        }
        
        @Override
        protected void move(double pct)
        {
            if (m_line[0] == null)
                return; // m_line doesn't exists yet at move(0)
            
            for (int i = 0; i < 2; i++)
            {
                int d = i == 0 ? 1 : -1;
                Point2DArray pts = m_line[i].getPoints();
                Point2D pt = pts.get(1);
                pt.setX(from_x + d * dx * pct);
                pt.setY(from_y + d * dy * pct);
                m_line[i].setPoints(pts);
            }
        }
    }
    
    public static class RadioActiveTransition extends Transition
    {
        public RadioActiveTransition(double x, double y)
        {
            super(x, y, x, y);
        }
    }
    
    public static void main(String[] args)
    {
//        ArchTransition.test(-1, 1, 2, 4, 0);
//        ArchTransition.test(-1, 1, 1, 1, 0);
        ArchTransition.test(350,200, 250,200, 100);
    }
}