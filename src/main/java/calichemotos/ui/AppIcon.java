package calichemotos.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class AppIcon {

    private AppIcon() {}

    public static Image getIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(200, 60, 30));
            g.fillRect(0, 0, size, size);

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // silueta simple tipo llave/tuerca
            g.drawOval(9, 6, 14, 14);
            g.drawOval(13, 10, 6, 6);
            g.drawLine(14, 20, 10, 26);
            g.drawLine(18, 20, 22, 26);
        } finally {
            g.dispose();
        }
        return image;
    }
}
