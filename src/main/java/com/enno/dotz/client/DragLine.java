package com.enno.dotz.client;

import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.LineJoin;

public class DragLine extends PolyLine
{
    public DragLine()
    {
        setStrokeWidth(4);
        setStrokeColor(ColorName.BLACK);
        setLineJoin(LineJoin.BEVEL);
    }
    
    public void clear()
    {
        setPoints(new Point2DArray());
    }

    public void pop()
    {
        getPoints().pop();
    }

    public void add(double x, double y)
    {
        Point2DArray pts = getPoints();
        pts.push(x, y);
        setPoints(pts);
    }
    
    public void adjust(double x, double y)
    {
        Point2DArray pts = getPoints();
        Point2D pt = pts.get(pts.size() - 1);
        pt.setX(x);
        pt.setY(y);
        setPoints(pts);
    }
}