import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
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

    Vector3 subtract(Vector3 o) {
        return new Vector3(x - o.x, y - o.y, z - o.z);
    }

    Vector3 cross(Vector3 o) {
        return new Vector3(
            y * o.z - z * o.y,
            z * o.x - x * o.z,
            x * o.y - y * o.x
        );
    }

    Vector3 normalize() {
        double len = Math.sqrt(x*x + y*y + z*z);
        return new Vector3((float)(x/len), (float)(y/len), (float)(z/len));
    }

    float dot(Vector3 o) {
        return x * o.x + y * o.y + z * o.z;
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
    public int a, b, c; // vertex indices
    public int na, nb, nc; // normal indices

    Face(int a, int b, int c, int na, int nb, int nc) {
        this.a = a;
        this.b = b;
        this.c = c;

        this.na = na;
        this.nb = nb;
        this.nc = nc;
    }
}

class Model {
    List<Vector3> vertices = new ArrayList<>();
    List<Vector3> normals = new ArrayList<>();
    List<Face> faces = new ArrayList<>();
}

class Renderer {
    private final Graphics2D g;
    private final BufferedImage img;
    private final int width, height;

    private final Color colA = Color.RED;
    private final Color colB = Color.GREEN;
    private final Color colC = Color.BLUE;

    private float[] depthBuffer;


    Renderer(BufferedImage img, Graphics2D g, int w, int h) {
        this.img = img;
        this.g = g;
        this.width = w;
        this.height = h;
    }

    static double edgeFunction(Vector3 a, Vector3 b, int px, int py) {
        return (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x);
    }

    static double edgeFunction(Vector3 a, Vector3 b, Vector3 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    };

    static Vector3 faceNormal(Vector3 a, Vector3 b, Vector3 c) {
        Vector3 edge1 = b.subtract(a);
        Vector3 edge2 = c.subtract(a);
        return edge1.cross(edge2).normalize();
    }

    void clear(Color c) {
        depthBuffer = new float[width * height];
        Arrays.fill(depthBuffer, Float.MAX_VALUE)
        ;
        g.setColor(c);
        g.fillRect(0, 0, width, height);
    }

    void setPixel(int x, int y, Color color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            img.setRGB(x, y, color.getRGB());
        }
    }

    void drawTriangle(Vector3 a, Vector3 b, Vector3 c, 
                      Vector3 na, Vector3 nb, Vector3 nc,
                      Vector3 sunDir) {
        
        double ABC = edgeFunction(a, b, c);

        if (ABC < 0) {
            return;
        }

        int minX = (int) Math.max(0, Math.min(a.x, Math.min(b.x, c.x)));
        int maxX = (int) Math.min(width - 1, Math.max(a.x, Math.max(b.x, c.x)));
        int minY = (int) Math.max(0, Math.min(a.y, Math.min(b.y, c.y)));
        int maxY = (int) Math.min(height - 1, Math.max(a.y, Math.max(b.y, c.y)));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double ABP = edgeFunction(a, b, x, y);
                double BCP = edgeFunction(b, c, x, y);
                double CAP = edgeFunction(c, a, x, y);

                boolean inside = (ABP >= 0 && BCP >= 0 && CAP >= 0);

                if (inside) {
                    double normA = BCP / ABC;
                    double normB = CAP / ABC;
                    double normC = ABP / ABC;
                    // System.out.println(brightness);
                    // int r = (int)((colA.getRed() * normA + colB.getRed() * normB + colC.getRed() * normC) * brightness);
                    // int g = (int)((colA.getGreen() * normA + colB.getGreen() * normB + colC.getGreen() * normC) * brightness);
                    // int blu = (int)((colA.getBlue() * normA + colB.getBlue() * normB + colC.getBlue() * normC) * brightness);
                    
                    float nx = (float)(na.x * normA + nb.x * normB + nc.x * normC);
                    float ny = (float)(na.y * normA + nb.y * normB + nc.y * normC);
                    float nz = (float)(na.z * normA + nb.z * normB + nc.z * normC);
                    Vector3 normal = new Vector3(nx, ny, nz).normalize();

                    float brightness = Math.max(0, normal.dot(sunDir));

                    int r = (int)(255f * brightness);
                    int g = (int)(255f * brightness);
                    int blu = (int)(255f * brightness);


                    float z = (float)(a.z * normA + b.z * normB + c.z * normC);
                    int idx = (int)(y * width + x);

                    if (z < depthBuffer[idx]) {
                        depthBuffer[idx] = z;
                        setPixel(x, y, new Color(r, g, blu));
                    }
                    
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

    Vector3 screen(Vector3 normalPos) {
        return new Vector3(
            ((normalPos.x + 1) * 0.5f * width),
            ((1 - normalPos.y) * 0.5f * height),
            normalPos.z
        );
    }

    Vector3 project(Vector3 v) {
        float fov = 40f;
        float fovFactor = (float) (1 / Math.tan(Math.toRadians(fov / 2)));
        float aspect = (float) width / height;

        float z = v.z + 4;
        return new Vector3(
            (v.x * fovFactor) / (z * aspect),
            (v.y * fovFactor) / z,
            z
        );
    }
}