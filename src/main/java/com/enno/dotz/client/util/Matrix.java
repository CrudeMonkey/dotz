package com.enno.dotz.client.util;

import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.core.types.Transform;

public class Matrix
{
    public static Transform create3PointTransform(Point2DArray src, Point2DArray target)
    {
        //
        // T = (m00 m01 m02)    (x)    (x * m00 + y * m01 + m02)
        //     (m10 m11 m12)  * (y)  = (x * m10 + y * m11 + m12)
        //     ( 0   0   1 )    (1)    (           1           )
        //
        // src = (a,b,c)          target = (p,q,r)
        //
        // T * a = p,   T * b = q,  T * c = r           m00,m01,m02
        //                                              
        // ax * m00 + ay * m01 + m02 = px               (ax ay 1 | px)
        // bx * m00 + by * m01 + m02 = qx               (bx by 1 | qx)
        // cx * m00 + cy * m01 + m02 = rx               (cx cy 1 | rx)
        //
        
        double ax = src.get(0).getX();
        double ay = src.get(0).getY();
        double bx = src.get(1).getX();
        double by = src.get(1).getY();
        double cx = src.get(2).getX();
        double cy = src.get(2).getY();

        double px = target.get(0).getX();
        double py = target.get(0).getY();
        double qx = target.get(1).getX();
        double qy = target.get(1).getY();
        double rx = target.get(2).getX();
        double ry = target.get(2).getY();

        double[][] M = 
        {
        //  m00 m01 m02
          { ax, ay, 1, px },
          { bx, by, 1, qx },
          { cx, cy, 1, rx }
        };
        double[] solution = new double[3];
        solve(M, solution);
        
        double m00 = solution[0];
        double m01 = solution[1];
        double m02 = solution[2];
        
        double[][] M2 = 
        {
        //  m10 m11 m12
          { ax, ay, 1, py },
          { bx, by, 1, qy },
          { cx, cy, 1, ry }
        };
        
        solve(M2, solution);
        
        double m10 = solution[0];
        double m11 = solution[1];
        double m12 = solution[2];
        
        return new Transform(new double[] { m00, m10, m01, m11, m02, m12 });
    }
    
    protected static void solve(double[][] M, double[] solution)
    {
        // solve using Cramer's rule
        // xi = det(Mi)/det(M), 
        // where Mi = M with column i replaced by the right hand side
        
        double detA = det(M[0][0], M[0][1], M[0][2],
                          M[1][0], M[1][1], M[1][2],
                          M[2][0], M[2][1], M[2][2]);
        if (detA == 0)
            throw new RuntimeException("determinant = 0, no solutions");
        
        double det0 = det(M[0][3], M[0][1], M[0][2],
                          M[1][3], M[1][1], M[1][2],
                          M[2][3], M[2][1], M[2][2]);
        double det1 = det(M[0][0], M[0][3], M[0][2],
                          M[1][0], M[1][3], M[1][2],
                          M[2][0], M[2][3], M[2][2]);
        double det2 = det(M[0][0], M[0][1], M[0][3],
                          M[1][0], M[1][1], M[1][3],
                          M[2][0], M[2][1], M[2][3]);

        solution[0] = det0 / detA;
        solution[1] = det1 / detA;
        solution[2] = det2 / detA;
    }
    
    protected static double det(double a, double b, double c, 
                                double d, double e, double f,
                                double g, double h, double i)
    {
        // see https://en.wikipedia.org/wiki/Determinant
        return a * (e * i - f * h) - b * ( d * i - f * g) + c * (d * h - e * g);
    }
    
    public static void test()
    {
        double[][][] z = {
                { {0,0}, {1,0}, {1,1}, {2,0}, {3,0}, {3,1} },      
        };
        
        for (int row = 0; row < z.length; row++)
        {
            double[][] pts = z[row];
            Point2D a = new Point2D(pts[0][0], pts[0][1]);
            Point2D b = new Point2D(pts[1][0], pts[1][1]);
            Point2D c = new Point2D(pts[2][0], pts[2][1]);
            Point2D p = new Point2D(pts[3][0], pts[3][1]);
            Point2D q = new Point2D(pts[4][0], pts[4][1]);
            Point2D r = new Point2D(pts[5][0], pts[5][1]);
            
            Point2DArray src = new Point2DArray(a, b, c);
            Point2DArray target = new Point2DArray(p, q, r);
            Transform T = create3PointTransform(src, target);
            for (int j = 0; j < 3; j++)
            {
                Point2D result = new Point2D();
                T.transform(src.get(j), result);
                Point2D desired = target.get(j);

                double dx = result.getX() - desired.getX();
                double dy = result.getY() - desired.getY();
                if (dx != 0 || dy != 0)
                {
                    Debug.p("j= " + j + ": got [" + result.getX() + "," + result.getY() + "]  desired [" + desired.getX() + "," + desired.getY() + "]");
                    Debug.p("M=" + T);
                }
            }
        }
    }
}
