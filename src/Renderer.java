import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class Vector2 {
    public float x, y;

    Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

class Vector3 {
    public float x, y, z;

    Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

        Vector3 rotateY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3(
            x * cos - z * sin,
            y,
            x * sin + z * cos
        );
    }

    Vector3 rotateX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return new Vector3(
            x,
            y * cos - z * sin,
            y * sin + z * cos
        );
    }
}

class Face { // triangulated faces
    public int a, b, c;

    Face(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}

class Model {
    List<Vector3> vertices = new ArrayList<>();
    List<Face> faces = new ArrayList<>();
}

class Renderer {
    private final Graphics2D g;
    private final BufferedImage img;
    private final int w, h;

    private final Color colA = Color.RED;
    private final Color colB = Color.GREEN;
    private final Color colC = Color.BLUE;

    private float[] depthBuffer;


    Renderer(BufferedImage img, Graphics2D g, int w, int h) {
        this.img = img;
        this.g = g;
        this.w = w;
        this.h = h;
    }

    static double edgeFunction(Vector2 a, Vector2 b, int px, int py) {
        return (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
    }

    static double edgeFunction(Vector2 a, Vector2 b, Vector2 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    };

    void clear(Color c) {
        g.setColor(c);
        g.fillRect(0, 0, w, h);
    }

    void setPixel(int x, int y, Color color) {
        if (x >= 0 && x < w && y >= 0 && y < h) {
            img.setRGB(x, y, color.getRGB());
        }
    }

    void drawTriangle(Vector2 a, Vector2 b, Vector2 c) {
        double ABC = edgeFunction(a, b, c);

        if (ABC < 0) {
            return;
        }

        int minX = (int) Math.max(0, Math.min(a.x, Math.min(b.x, c.x)));
        int maxX = (int) Math.min(w - 1, Math.max(a.x, Math.max(b.x, c.x)));
        int minY = (int) Math.max(0, Math.min(a.y, Math.min(b.y, c.y)));
        int maxY = (int) Math.min(h - 1, Math.max(a.y, Math.max(b.y, c.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double ABP = edgeFunction(a, b, x, y);
                double BCP = edgeFunction(b, c, x, y);
                double CAP = edgeFunction(c, a, x, y);

                boolean inside = (ABP >= 0 && BCP >= 0 && CAP >= 0);

                if (inside) {
                    double u = BCP / ABC;
                    double v = CAP / ABC;
                    double w = ABP / ABC;

                    int r = (int)(colA.getRed() * u + colB.getRed() * v + colC.getRed() * w);
                    int g = (int)(colA.getGreen() * u + colB.getGreen() * v + colC.getGreen() * w);
                    int blu = (int)(colA.getBlue() * u + colB.getBlue() * v + colC.getBlue() * w);
                    

                    setPixel(x, y, new Color(r, g, blu));
                }
            }
        }
    }

    void drawPoint(Vector2 pos) {
        g.setColor(Color.magenta);
        g.fillRect((int)pos.x, (int)pos.y, 4, 4);
    }

    void drawLine(Vector2 p1, Vector2 p2) {
        g.setColor(Color.cyan);
        g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
    }

    Vector2 screen(Vector2 normalPos) {
        return new Vector2(
            ((normalPos.x + 1) * 0.5f * w),
            ((1 - normalPos.y) * 0.5f * h)
        );
    }

    Vector2 project(Vector3 v) {
        float fov = 100f;
        float fovFactor = (float) (1 / Math.tan(Math.toRadians(fov / 2)));
        float aspect = (float) w / h;

        float z = v.z + 4;
        return new Vector2(
            (v.x * fovFactor) / (z * aspect),
            (v.y * fovFactor) / z
        );
    }
}