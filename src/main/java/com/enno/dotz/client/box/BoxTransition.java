package com.enno.dotz.client.box;

import com.ait.lienzo.client.core.animation.IAnimation;
import com.ait.lienzo.client.core.animation.IAnimationCallback;
import com.ait.lienzo.client.core.animation.IAnimationHandle;
import com.ait.lienzo.client.core.animation.LayerRedrawManager;
import com.ait.lienzo.client.core.animation.TimedAnimation;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.enno.dotz.client.util.Matrix;
import com.enno.dotz.client.util.TransformGroup;

public abstract class BoxTransition
{
    public abstract void transition(TransformGroup shape, Extent r, LienzoPanel panel, Layer layer, Runnable whenDone);
    
    protected void transition(final TransformGroup shape, final Transform fromTransform, final Transform toTransform, final Layer layer, final Runnable whenDone)
    {        
        final double[] transform = new double[6];
        
        TimedAnimation handle = new TimedAnimation(500, new IAnimationCallback() 
        {            
            @Override  
            public void onStart(IAnimation animation, IAnimationHandle handle) 
            {  
            }  
  
            @Override  
            public void onFrame(IAnimation animation, IAnimationHandle handle) 
            {
                draw(animation.getPercent());
            }
            
            protected void draw(double pct)
            {
                // Calculate the transform between the fromTransform and the toTransform.  
                // If percent=0, transform will result in the fromTransform.  
                // If percent=1, transform will result in the toTransform.  
                // Any other percent value will fall somewhere in between (in a linear fashion.)   
                for (int i = 0; i < 6; i++) // a Transform matrix has 6 values  
                {  
                    double d = fromTransform.get(i);  
                    transform[i] = d + pct * (toTransform.get(i) - d);  
                }  
                  
                // Set the Transform on the node  
                shape.setTransform(new Transform(transform));  
                LayerRedrawManager.get().schedule(layer); 
            }  
  
            @Override  
            public void onClose(IAnimation animation, IAnimationHandle handle) 
            {
                draw(1);
                whenDone.run();
            }              
        }); 
        handle.run();
    }
    
    public static class SlideIn extends BoxTransition
    {
        public void transition(TransformGroup shape, Extent r, LienzoPanel panel, Layer layer, Runnable whenDone)
        {
            Point2DArray src = new Point2DArray(new Point2D(0, r.y), new Point2D(-r.w, r.y), new Point2D(0, r.y + r.h));
            Point2DArray target = new Point2DArray(new Point2D(r.x, r.y), new Point2D(r.x + r.w, r.y), new Point2D(r.x, r.y + r.h));

            Transform toTransform = new Transform().translate(r.x, r.y);
            
            Transform fromTransform = Matrix.create3PointTransform(src, target).getInverse().multiply(toTransform);
            shape.setTransform(fromTransform);
            transition(shape, fromTransform, toTransform, layer, whenDone);
        }
    }
    
    public static class SlideOut extends BoxTransition
    {
        public void transition(TransformGroup shape, Extent r, LienzoPanel panel, Layer layer, Runnable whenDone)
        {
            double w = panel.getWidth();
            
            Point2DArray src = new Point2DArray(new Point2D(r.x, 0), new Point2D(r.x + r.w, 0), new Point2D(r.x, r.h));
            Point2DArray target = new Point2DArray(new Point2D(w + r.w, 0), new Point2D(w, 0), new Point2D(w + r.w, r.h));
                    
            Transform fromTransform = new Transform().translate(r.x, r.y); // identity
            Transform toTransform = Matrix.create3PointTransform(src, target).multiply(fromTransform);
            
            transition(shape, fromTransform, toTransform, layer, whenDone);
        }
    }
}
