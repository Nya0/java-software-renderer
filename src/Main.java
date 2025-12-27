import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JFrame;

public class Main {
    static final int W = 1440;
    static final int H = 720;
    

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
                    String[] parts = line.split(" ");
                    model.faces.add(new Face(
                        Integer.parseInt(parts[1].split("/")[0]) - 1,
                        Integer.parseInt(parts[2].split("/")[0]) - 1,
                        Integer.parseInt(parts[3].split("/")[0]) - 1
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
        Model suzanne = loadObjModel("C:\\Users\\alert\\Misc\\University\\T5\\Advanced Programming\\Practice\\FinalRev\\suzanne.obj");

        DrawPanel panel = new DrawPanel();

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
                    Vector3 v1 = suzanne.vertices.get(face.a).rotateY(time).rotateX(time / 1.5f);
                    Vector3 v2 = suzanne.vertices.get(face.b).rotateY(time).rotateX(time / 1.5f);
                    Vector3 v3 = suzanne.vertices.get(face.c).rotateY(time).rotateX(time / 1.5f);
    
                    // 0 1 2
                    r.drawTriangle(r.screen(r.project(v1)), r.screen(r.project(v2)), r.screen(r.project(v3)));
                    // r.drawLine(r.screen(r.project(v1)), r.screen(r.project(v2)));
                    // r.drawLine(r.screen(r.project(v2)), r.screen(r.project(v3)));
                    // r.drawLine(r.screen(r.project(v3)), r.screen(r.project(v1)));
                }

                // r.drawTriangle(
                //     new Vector2(W/2 - 100, H/2 + 100), 
                //     new Vector2(W/2, H/2 - 100),
                //     new Vector2(W/2 + 100, H/2 + 100) 
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
