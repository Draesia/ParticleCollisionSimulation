package ramsden.ryan.GUI;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;


public final class GradientImage extends Object {

    private static Ellipse2D ball;
    private static RadialGradientPaint rgp;
    private static BufferedImage image;
    
    public static BufferedImage createImage(int radius, Color color)
    {
        int diameter = radius * 2;
        ball = new Ellipse2D.Double(0, 0, diameter, diameter);
        rgp = new RadialGradientPaint(new Point2D.Double(radius, radius), new Point2D.Double(0, radius+40), color, Color.BLACK);
        image = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, diameter, diameter);
        g2.setComposite(AlphaComposite.Src);
        g2.setPaint(rgp);
        g2.fill(ball);
        return image;
    }
}

class RadialGradientPaint implements Paint {
    private Point2D point;
    private Point2D radius;
    private Color pointColor, backgroundColor;
    
    /**
     * Construct an acyclic, radial, gradient paint centered on p,
     * proportional to the length of r, spanning c1 to c2.
     */
    public RadialGradientPaint(Point2D p, Point2D r, Color c1, Color c2) {
        if (r.distance(0, 0) <= 0)
            throw new IllegalArgumentException("Radius > 0 required.");
        this.point = p;
        this.radius = r;
        this.pointColor = c1;
        this.backgroundColor = c2;
    }
    
    public PaintContext createContext(ColorModel cm,
            Rectangle deviceBounds, Rectangle2D userBounds,
            AffineTransform xform, RenderingHints hints) {
        Point2D transformedPoint = xform.transform(point, null);
        Point2D transformedRadius = xform.deltaTransform(radius, null);
        return new RadialGradientContext(
            transformedPoint, transformedRadius,
            pointColor, backgroundColor);
    }
    
    public int getTransparency() {
        int a1 = pointColor.getAlpha();
        int a2 = backgroundColor.getAlpha();
        return (((a1 & a2) == 0xff) ? OPAQUE : TRANSLUCENT);
    }
}

class RadialGradientContext implements PaintContext {
    private Point2D point;
    private Point2D radius;
    private Color c1, c2;

    public RadialGradientContext(Point2D p, Point2D r, Color c1, Color c2) {
        this.point = p;
        this.radius = r;
        this.c1 = c1;
        this.c2 = c2;
    }
    
    public void dispose() {}
    
    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }
    
    public Raster getRaster(int x, int y, int w, int h) {
        int[] ia = new int[w * h * 4];
        int ix = 0;
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                double pointDistance = point.distance(col + x, row + y);
                double radialDistance = radius.distance(0, 0);
                double dr = Math.min(pointDistance / radialDistance, 1.0);
                ia[ix++] = (int) (c1.getRed()   + dr * (c2.getRed()   - c1.getRed()));
                ia[ix++] = (int) (c1.getGreen() + dr * (c2.getGreen() - c1.getGreen()));
                ia[ix++] = (int) (c1.getBlue()  + dr * (c2.getBlue()  - c1.getBlue()));
                ia[ix++] = (int) (c1.getAlpha() + dr * (c2.getAlpha() - c1.getAlpha()));
            }
        }
        WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
        raster.setPixels(0, 0, w, h, ia);
        return raster;
    }
}
