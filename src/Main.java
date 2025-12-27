import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JFrame;

public class Main {
    static final int W = 500;
    static final int H = 500;

    static float rotX = 0, rotY = 0;
    static int lastMouseX, lastMouseY;
	static float zoom = 4f;


    static Model loadObjModel(String filePath) {
        Model model = new Model();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("v ")) {
                    String[] parts = line.split(" ");
                    model.vertices.add(new Vector3(
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                    ));
                }

                if (line.startsWith("f ")) {
                    // f v/vt/vn
                    String[] parts = line.split(" ");
                    String[] p1 = parts[1].split("/");
                    String[] p2 = parts[2].split("/");
                    String[] p3 = parts[3].split("/");
                    
                    int v1 = Integer.parseInt(p1[0]) - 1;
                    int v2 = Integer.parseInt(p2[0]) - 1;
                    int v3 = Integer.parseInt(p3[0]) - 1;
                    
                    int n1 = -1, n2 = -1, n3 = -1;
                    
                    // check if normals exist 
                    if (p1.length >= 3 && !p1[2].isEmpty()) {
                        n1 = Integer.parseInt(p1[2]) - 1;
                        n2 = Integer.parseInt(p2[2]) - 1;
                        n3 = Integer.parseInt(p3[2]) - 1;
                    }
                    
                    model.faces.add(new Face(v1, v2, v3, n1, n2, n3));
                }

                if (line.startsWith("vn ")) {
                    String[] parts = line.split(" ");
                    model.normals.add(new Vector3(
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]),
                        Float.parseFloat(parts[3])
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("vertices: " + model.vertices.size());
        System.out.println("faces: " + model.faces.size());
        return model;
    }
    public static void main(String[] args) {
        Model suzanne = loadObjModel("C:\\Users\\alert\\Misc\\University\\T5\\Advanced Programming\\Practice\\FinalRev\\V3.obj");

        DrawPanel panel = new DrawPanel();

        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        panel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                
                rotY += dx * 0.01f;
                rotX -= dy * 0.01f;
                
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        panel.addMouseWheelListener(e -> {
            zoom += e.getWheelRotation() * 0.5f;
            zoom = Math.max(1f, zoom);  // don't go too close
        });

        JFrame frame = new JFrame("boogie man");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(W, H);
        frame.add(panel);
        frame.setVisible(true);
        

        BufferedImage backBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        new Thread(() -> {
            long frameTime = 16_666_666;

            while (true) {
                long start = System.nanoTime();
                float time = (System.currentTimeMillis() % 1000000) / 1000f;

                // snapshot rot
                float frameRotX = rotX;
                float frameRotY = rotY;
				float frameZoom = zoom;

                // ----- DRAW -----
                Graphics2D g = backBuffer.createGraphics();
                Renderer r = new Renderer(backBuffer, g, W, H);

                r.clear(Color.darkGray);

                // r.drawPoint(r.screen(r.project(new Vector3(0.5f, 0, z) )));
                // r.drawLine(
                //     r.screen(new Vector2(-0.5f, -0.5f)),
                //     r.screen(new Vector2(0.5f, 0.5f))
                // );
                // g.dispose();



                for (Face face : suzanne.faces) {
                    // Vector3 v1 = suzanne.vertices.get(face.a).rotateY(time).rotateX(time / 1.5f);
                    // Vector3 v2 = suzanne.vertices.get(face.b).rotateY(time).rotateX(time / 1.5f);
                    // Vector3 v3 = suzanne.vertices.get(face.c).rotateY(time).rotateX(time / 1.5f);

                    // Vector3 n1 = suzanne.normals.get(face.na).rotateY(time).rotateX(time / 1.5f);
                    // Vector3 n2 = suzanne.normals.get(face.nb).rotateY(time).rotateX(time / 1.5f);
                    // Vector3 n3 = suzanne.normals.get(face.nc).rotateY(time).rotateX(time / 1.5f);

                    Vector3 v1 = suzanne.vertices.get(face.a).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 v2 = suzanne.vertices.get(face.b).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 v3 = suzanne.vertices.get(face.c).rotateY(frameRotY).rotateX(frameRotX);

                    Vector3 n1 = suzanne.normals.get(face.na).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 n2 = suzanne.normals.get(face.nb).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 n3 = suzanne.normals.get(face.nc).rotateY(frameRotY).rotateX(frameRotX);
    
                    // 0 1 2
                    // Vector3 normal = Renderer.faceNormal(v1, v2, v3);
                    Vector3 sunDir = new Vector3(0, 1, -1).normalize();
                    r.drawTriangle(r.screen(r.project(v1, frameZoom)), r.screen(r.project(v2, frameZoom)), r.screen(r.project(v3, frameZoom)),
                                   n1, n2, n3,
                                   sunDir
                                );


                    // r.drawLine(r.screen(r.project(v1, frameZoom)), r.screen(r.project(v2, frameZoom)));
                    // r.drawLine(r.screen(r.project(v2, frameZoom)), r.screen(r.project(v3, frameZoom)));
                    // r.drawLine(r.screen(r.project(v3, frameZoom)), r.screen(r.project(v1, frameZoom)));
                }

                // r.drawTriangle(
                //     new Vector3(W/2 - 100, H/2 + 100, 0), 
                //     new Vector3(W/2, H/2 - 100, 0),
                //     new Vector3(W/2 + 100, H/2 + 100, 0) 
                // );

                // ----------------

                panel.frame = backBuffer;
                panel.repaint();

                long sleep = frameTime - (System.nanoTime() - start);
                if (sleep > 0) {
                    try {
                        Thread.sleep(sleep / 1_000_000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }
}
