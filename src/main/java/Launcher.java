import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
    private static int[] x = new int[1];
    private static int[] y = new int[1];
    private static Matrix3 transform;

    public static void main(String[] args) {

        // Initialize the transform matrix
        transform = new Matrix3(new double[]{
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        });

        // Declare the frame and container
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Add the container to the frame
        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100),
                        Color.BLUE));

                // Initialize the BufferedImage
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                // Initialize the z-buffer
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    v1.x += getWidth() / 2.0;
                    v1.y += getHeight() / 2.0;
                    v2.x += getWidth() / 2.0;
                    v2.y += getHeight() / 2.0;
                    v3.x += getWidth() / 2.0;
                    v3.y += getHeight() / 2.0;
                    // Calculate the range to be processed
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1,
                            Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                            Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            Vertex p = new Vertex(x, y, 0);
                            // Judge once for each vertex
                            boolean V1 = sameSide(v1, v2, v3, p);
                            boolean V2 = sameSide(v2, v3, v1, p);
                            boolean V3 = sameSide(v3, v1, v2, p);
                            if (V3 && V2 && V1) {
                                double depth = v1.z + v2.z + v3.z;
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, t.color.getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }
                g2.drawImage(img, 0, 0, null);
            }
        };

        renderPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double yi = 180.0 / renderPanel.getHeight();
                double xi = 180.0 / renderPanel.getWidth();
                x[0] = (int) (e.getX() * xi);
                y[0] = -(int) (e.getY() * yi);
                updateTransformMatrix();
                renderPanel.repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

        // Add the render panel to the container
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(600, 600);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void updateTransformMatrix() {
        double heading = Math.toRadians(x[0]);
        double pitch = Math.toRadians(y[0]);

        Matrix3 headingTransform = new Matrix3(new double[]{
                Math.cos(heading), 0, -Math.sin(heading),
                0, 1, 0,
                Math.sin(heading), 0, Math.cos(heading)
        });

        Matrix3 pitchTransform = new Matrix3(new double[]{
                1, 0, 0,
                0, Math.cos(pitch), Math.sin(pitch),
                0, -Math.sin(pitch), Math.cos(pitch)
        });

        transform = headingTransform.multiply(pitchTransform);
    }

    static boolean sameSide(Vertex A, Vertex B, Vertex C, Vertex p) {
        Vertex V1V2 = new Vertex(B.x - A.x, B.y - A.y, B.z - A.z);
        Vertex V1V3 = new Vertex(C.x - A.x, C.y - A.y, C.z - A.z);
        Vertex V1P = new Vertex(p.x - A.x, p.y - A.y, p.z - A.z);

        // If the cross product of vector V1V2 and vector V1V3 is the same as the one of vector V1V2 and vector V1p, they are on the same side.
        // We only need to judge the direction of z
        double V1V2CrossV1V3 = V1V2.x * V1V3.y - V1V3.x * V1V2.y;
        double V1V2CrossP = V1V2.x * V1P.y - V1P.x * V1V2.y;

        return V1V2CrossV1V3 * V1V2CrossP >= 0;
    }
}