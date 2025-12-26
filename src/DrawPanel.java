import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawPanel extends JPanel {
    BufferedImage frame;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frame != null) {
            g.drawImage(frame, 0, 0, null);
        }
    }
}
