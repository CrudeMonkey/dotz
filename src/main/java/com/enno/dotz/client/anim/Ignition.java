package com.enno.dotz.client.anim;

import java.util.Random;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.IColor;
import com.enno.dotz.client.Context;
import com.enno.dotz.client.SoundManager.Sound;

public class Ignition implements IAnimationCallback
{
    private static int N = 30;
    private static double R = 15;
    public static IColor[] COLORS = { ColorName.RED, ColorName.DARKORANGE, ColorName.BLUE, ColorName.MAGENTA, ColorName.LIMEGREEN };
    
    private Random rnd = new Random();        
    private Line[] m_lines = new Line[N];
    private Context ctx;
    private double m_x;
    private double m_y;
    private boolean m_playSound;
    
    public Ignition(double x, double y, Context ctx, boolean playSound)
    {
        this.ctx = ctx;
        m_x = x;
        m_y = y;
        m_playSound = playSound;
    }
    
    @Override
    public void onStart(IAnimation animation, IAnimationHandle handle)
    {
        if (m_playSound)
            Sound.LIGHT_FUSE.play();
        
        for (int i = 0; i < N; i++)
        {
            Line a = new Line();
            a.setStrokeWidth(1);
            a.setVisible(false);
            a.setX(m_x);
            a.setY(m_y);
            a.setStrokeColor(COLORS[rnd.nextInt(COLORS.length)]);
            m_lines[i] = a;
            ctx.laserLayer.add(a);
        }
    }

    @Override
    public void onFrame(IAnimation animation, IAnimationHandle handle)
    {
        for (int i = 0; i < N; i++)
        {
            Line a = m_lines[i];
            if (rnd.nextBoolean())
            {
                a.setVisible(true);
                double r1 = rnd.nextDouble() * R;
                double r2 = rnd.nextDouble() * R;
                double angle = rnd.nextDouble() * Math.PI * 2;
                                    
                Point2DArray points = new Point2DArray();
                points.push(r1 * Math.sin(angle), r1 * Math.cos(angle));
                points.push(r2 * Math.sin(angle), r2 * Math.cos(angle));
                a.setPoints(points);
            }
            else
                a.setVisible(false);
        }
    }

    @Override
    public void onClose(IAnimation animation, IAnimationHandle handle)
    {
        for (int i = 0; i < N; i++)
        {
            ctx.laserLayer.remove(m_lines[i]);
        }
    }
}