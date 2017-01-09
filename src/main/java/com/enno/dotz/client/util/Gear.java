package com.enno.dotz.client.util;

import com.ait.lienzo.client.core.shape.MultiPath;

public class Gear extends MultiPath
{
    private int m_teeth;
    private double m_innerRadius;
    private double m_outerRadius;
    private double m_innerRatio;
    private double m_outerRatio;

    public Gear(int teeth, double innerRadius, double outerRadius, double innerRatio, double outerRatio)
    {
        m_teeth = teeth;
        m_innerRadius = innerRadius;
        m_outerRadius = outerRadius;
        m_innerRatio = innerRatio;
        m_outerRatio = outerRatio;
        
        makePath();
    }
    
    //
    //
    //          __
    //         /  \
    //        /    \
    //    ---/      \
    //
    //
    //
    protected void makePath()
    {
        int n = 1; // m_teeth
        
        double da = Math.PI  * 2.0 / m_teeth;   // angle covered by one cog
        
        double dia = da * (1 - m_innerRatio) / 2;
        double doa = da * (1 - m_outerRatio) / 2;
        double a0 = Math.PI / 2 + da / 2 + dia;
                
        double x = m_innerRadius * Math.cos(a0 + dia);
        double y = m_innerRadius * Math.sin(a0 + dia);
        
        M(x, y);
        for (int i = 0; i < m_teeth; i++)
        {
            double a1 = a0 - dia;
            x = m_innerRadius * Math.cos(a1);
            y = m_innerRadius * Math.sin(a1);
            A(m_innerRadius, m_innerRadius, 0, 0, 0, x, y);
            //L(x,y);
            
            x = m_outerRadius * Math.cos(a0 - doa);
            y = m_outerRadius * Math.sin(a0 - doa);
            L(x, y);
            
            x = m_outerRadius * Math.cos(a0 - doa - m_outerRatio * da);
            y = m_outerRadius * Math.sin(a0 - doa - m_outerRatio * da);
            L(x, y);
            
            a1 -= m_innerRatio * da;
            x = m_innerRadius * Math.cos(a1);
            y = m_innerRadius * Math.sin(a1);
            L(x, y);
            
            a0 -= da;
        }
        Z();
    }
}
