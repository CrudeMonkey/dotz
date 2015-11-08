package com.enno.dotz.client.anim;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.item.Item;

public abstract class Transition implements IAnimationCallback
{
    double from_x, from_y, dx, dy;
    public Item item;
    boolean bounce;
    double bounceFactor = 0.6;
    double bounceUp = 0.5;
    
    protected Transition(double from_x, double from_y, double to_x, double to_y, Item item)
    {
        this.from_x = from_x;
        this.from_y = from_y;
        this.dx = to_x - from_x;
        this.dy = to_y - from_y;
        this.item = item;
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
        item.moveShape(x, y);
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
            super(from_x, from_y, to_x, to_y, null);
            this.ctx = ctx;
        }
        
        public void afterStart()
        {
            m_line = new Line(from_x, from_y, from_x, from_y);
            m_line.setStrokeColor(ColorName.BLACK);
            m_line.setStrokeWidth(2);
            ctx.nukeLayer.add(m_line);
        }
        
        public void afterEnd()
        {
            ctx.nukeLayer.remove(m_line);
        }
        
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
}