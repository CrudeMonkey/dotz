package com.enno.dotz.client.util;

import com.ait.lienzo.shared.core.types.Color;
import com.ait.lienzo.shared.core.types.IColor;

public class ColorUtil
{
    public static IColor darker(IColor col, double f)
    {
        double[] hsl = ColorUtil.rgbToHsl(col.getR(), col.getG(), col.getB());
        IColor c = Color.fromNormalizedHSL(hsl[0], hsl[1], hsl[2] * f);
        
//        Console.log("new r,g,b=" + c.getR() + "," + c.getG() + "," + c.getB());
        
        return c;
    }

    public static double[] rgbToHsl(double r, double g, double b)
        {
    //        Console.log("r,g,b=" + r + "," + g + "," + b);
    
            r /= 255;
            g /= 255; 
            b /= 255;
                
            double max = Math.max(r, Math.max(g, b));
            double min = Math.min(r, Math.min(g, b));
            double h, s, l = (max + min) / 2;
    
            if (max == min) 
            {
                h = s = 0; // achromatic
            } 
            else 
            {
                double d = max - min;
                s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
                
                if (max == r)
                    h = (g - b) / d + (g < b ? 6 : 0);
                else if (max == g)
                    h = (b - r) / d + 2;
                else
                    h = (r - g) / d + 4;
                
                h /= 6;
            }
    
//            Console.log("h,s,l=" + h + ", " + s + ", " + l);
            
            return new double[] { h, s, l };
        }
}
