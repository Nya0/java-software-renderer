import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class Main {
    static final int W = 700;
    static final int H = 500;

    static float rotX = 0, rotY = 0;
    static int lastMouseX, lastMouseY;
    static float zoom = 4f;

    static float specularStrength = 0.5f;
    static int shininess = 64;
    static float sunX = 0f, sunY = 1f, sunZ = -1f;

    static Color modelColor = new Color(50, 100, 200);


    static Model loadObjModel(String filePath) {
        Model model = new Model();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
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
                    String[] p1 = parts[1].split("/");
                    String[] p2 = parts[2].split("/");
                    String[] p3 = parts[3].split("/");

                    int v1 = Integer.parseInt(p1[0]) - 1;
                    int v2 = Integer.parseInt(p2[0]) - 1;
                    int v3 = Integer.parseInt(p3[0]) - 1;

                    int n1 = -1, n2 = -1, n3 = -1;

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
        Model suzanne = loadObjModel("C:\\Users\\alert\\Misc\\University\\T5\\Advanced Programming\\Practice\\FinalRev\\suzanne.obj");

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
            zoom = Math.max(1f, zoom);
        });

		JPanel controls = new JPanel();
		controls.setLayout(new GridLayout(6, 1, 5, 5));
		controls.setBackground(Color.DARK_GRAY);

		JSlider specSlider = new JSlider(0, 100, 50);
		specSlider.setBackground(Color.DARK_GRAY);
		JLabel specLabel = new JLabel("Specular: 0.50");
		specLabel.setForeground(Color.WHITE);
		specSlider.addChangeListener(e -> {
			specularStrength = specSlider.getValue() / 100f;
			specLabel.setText("Specular: " + String.format("%.2f", specularStrength));
		});

		JSlider shinySlider = new JSlider(1, 256, 64);
		shinySlider.setBackground(Color.DARK_GRAY);
		JLabel shinyLabel = new JLabel("Shininess: 64");
		shinyLabel.setForeground(Color.WHITE);
		shinySlider.addChangeListener(e -> {
			shininess = shinySlider.getValue();
			shinyLabel.setText("Shininess: " + shininess);
		});

		JSlider hueSlider = new JSlider(0, 360, 220);
		hueSlider.setBackground(Color.DARK_GRAY);
		JLabel hueLabel = new JLabel("Hue: 220");
		hueLabel.setForeground(Color.WHITE);
		hueSlider.addChangeListener(e -> {
			modelColor = Color.getHSBColor(hueSlider.getValue() / 360f, 0.7f, 0.8f);
			hueLabel.setText("Hue: " + hueSlider.getValue());
		});

		java.util.function.BiFunction<JLabel, JSlider, JPanel> pair = (label, slider) -> {
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			p.setBackground(Color.DARK_GRAY);
			label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
			slider.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
			slider.setOpaque(false);
			p.add(label);
			p.add(slider);
			return p;
		};

		controls.add(pair.apply(specLabel, specSlider));
		controls.add(pair.apply(shinyLabel, shinySlider));
		controls.add(pair.apply(hueLabel, hueSlider));

		JFrame frame = new JFrame("boogie man");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.add(controls, BorderLayout.WEST);
		frame.setSize(W + 200, H); 
		frame.setVisible(true);
		
        BufferedImage backBuffer = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);

        new Thread(() -> {
            long frameTime = 16_666_666;

            while (true) {
                long start = System.nanoTime();

                float frameRotX = rotX;
                float frameRotY = rotY;
                float frameZoom = zoom;

                Graphics2D g = backBuffer.createGraphics();
                Renderer r = new Renderer(backBuffer, g, W, H);

                r.clear(Color.darkGray);

                Vector3 sunDir = new Vector3(sunX, sunY, sunZ).normalize();

                for (Face face : suzanne.faces) {
                    Vector3 v1 = suzanne.vertices.get(face.a).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 v2 = suzanne.vertices.get(face.b).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 v3 = suzanne.vertices.get(face.c).rotateY(frameRotY).rotateX(frameRotX);

                    Vector3 n1 = suzanne.normals.get(face.na).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 n2 = suzanne.normals.get(face.nb).rotateY(frameRotY).rotateX(frameRotX);
                    Vector3 n3 = suzanne.normals.get(face.nc).rotateY(frameRotY).rotateX(frameRotX);

                    r.drawTriangle(
                        r.screen(r.project(v1, frameZoom)),
                        r.screen(r.project(v2, frameZoom)),
                        r.screen(r.project(v3, frameZoom)),
                        n1, n2, n3,
                        sunDir
                    );
                }

                g.dispose();

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