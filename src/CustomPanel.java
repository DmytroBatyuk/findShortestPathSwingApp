import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Dmytro Batyuk
 */
public class CustomPanel extends JComponent {

    private int padding = 0;
    private final int POINT_RADIUS = 10;
    private final int ARROW_WIDTH = 25;
    private final int ARROW_HEIGHT = 4;
    private int counter = 1;

    private Point draggingLineStartPoint;
    private Point draggingLineEndPoint;
    private ArrayList<Integer> highlightRectangleIdList = new ArrayList<>(2);

    private HashSet<Integer> pathVertex = new HashSet<>();
    private HashMap<Integer, Integer> pathEdge = new HashMap<>();

    private HashMap<Integer, Rectangle> vertexMap = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> edgeMap = new HashMap<>();


    public void clear() {
        draggingLineStartPoint = null;
        draggingLineEndPoint = null;
        highlightRectangleIdList.clear();
        vertexMap.clear();
        edgeMap.clear();
        pathVertex.clear();
        pathEdge.clear();
        repaint();
    }

    public void calculate(Integer calcFrom, Integer calcTo) {
        pathVertex.clear();
        pathEdge.clear();

        Graph<Integer, DefaultOpenWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultOpenWeightedEdge.class);
        for (Integer v : vertexMap.keySet()) {
            graph.addVertex(v);
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : edgeMap.entrySet()) {
            Integer from = entry.getKey();
            ArrayList<Integer> toList = entry.getValue();
            for (Integer to : toList) {
                DefaultOpenWeightedEdge edge = graph.addEdge(from, to);
                Point fromP = new Point((int) vertexMap.get(from).getCenterX(), (int) vertexMap.get(from).getCenterY());
                Point toP = new Point((int) vertexMap.get(to).getCenterX(), (int) vertexMap.get(to).getCenterY());
                graph.setEdgeWeight(edge, getDistance(fromP, toP));
            }
        }


        GraphPath<Integer, DefaultOpenWeightedEdge> path = new DijkstraShortestPath<>(graph).getPath(calcFrom, calcTo);
        List<Integer> vertexList = new ArrayList<>();
        if (path != null && path.getVertexList() != null) {
            vertexList.addAll(path.getVertexList());
        }
        List<DefaultOpenWeightedEdge> edgeList = new ArrayList<>();
        if (path != null && path.getEdgeList() != null) {
            edgeList.addAll(path.getEdgeList());
        }
        pathVertex.addAll(vertexList);
        for (DefaultOpenWeightedEdge edge : edgeList) {
            pathEdge.put((Integer)edge.getSource(), (Integer)edge.getTarget());
        }
        System.out.println("!!! shortest: vertex=" + pathVertex + ", edge=" + pathEdge);
        repaint();
    }

    public CustomPanel() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (null != draggingLineStartPoint) {
                    draggingLineEndPoint = e.getPoint();
                    calculateHighlightRectangles();
                    CustomPanel.this.repaint();
                }
            }
        });
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                calculateClick(e.getPoint());
                CustomPanel.this.repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                draggingLineStartPoint = e.getPoint();
                calculateHighlightRectangles();
                CustomPanel.this.repaint();
                System.out.println("pressed=" + e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                calculateVertexConnections();
                draggingLineStartPoint = null;
                draggingLineEndPoint = null;
                calculateHighlightRectangles();
                CustomPanel.this.repaint();
                System.out.println("released=" + e.getPoint());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);

        drawVertexConnections(g2);

        drawVertexes(g2);

        drawDraggingLine(g2);

    }

    private void calculateClick(Point p) {
        Integer id = findVertexByPoint(p);
        if (null != id) {
            vertexMap.remove(id);
            if (null != edgeMap.remove(id)) {
                System.out.println("remove connections from " + id);
            }
            for (Map.Entry<Integer, ArrayList<Integer>> entity : edgeMap.entrySet()) {
                ArrayList<Integer> toList = entity.getValue();
                if (toList.remove(id)) {
                    System.out.println("remove connection from " + entity.getKey() + " to " + id);
                }
            }
        } else {
            vertexMap.put(counter++, new Rectangle(p.x - POINT_RADIUS, p.y - POINT_RADIUS, POINT_RADIUS * 2, POINT_RADIUS * 2));
        }
    }

    private Integer findVertexByPoint(Point p) {
        for (Map.Entry<Integer, Rectangle> entity : vertexMap.entrySet()) {
            if (entity.getValue().contains(p)) {
                return entity.getKey();
            }
        }
        return null;
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(padding, padding, getWidth() - (2 * padding), getHeight() - 2 * padding);

        g2.setColor(Color.GRAY);
        int step = 50;
        for (int x = step; x < getWidth(); x += step) {
            g2.drawLine(x, padding*2, x, padding*2 + POINT_RADIUS);
            FontMetrics metrics = g2.getFontMetrics();

            String label = String.valueOf(x);
            int labelWidth = metrics.stringWidth(label)/2;
//            Point p = new Point(x, entity.getValue().y);
            g2.drawString(label, x - labelWidth, padding*2 + POINT_RADIUS*2);
        }
        for (int y = step; y < getHeight(); y += step) {
            g2.drawLine(padding*2, y, padding*2 + POINT_RADIUS, y);
            FontMetrics metrics = g2.getFontMetrics();

            String label = String.valueOf(y);
            int labelWidth = metrics.stringWidth(label)/2;
//            Point p = new Point(x, entity.getValue().y);
            g2.drawString(label, padding*2 + POINT_RADIUS*2, y - labelWidth);
        }
    }

    private void drawVertexConnections(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        for (Map.Entry<Integer, ArrayList<Integer>> entity : edgeMap.entrySet()) {
            Integer id1 = entity.getKey();
            Rectangle r1 = vertexMap.get(id1);
            ArrayList<Integer> r2l = entity.getValue();
            for (Integer id2 : r2l) {
                if (!pathEdge.keySet().contains(id1) || !pathEdge.get(id1).equals(id2)) {
                    Rectangle r2 = vertexMap.get(id2);
                    drawArrowLine(g2, r1, r2, ARROW_WIDTH, ARROW_HEIGHT);
                }
            }
        }

        g2.setColor(Color.orange);
        for (Map.Entry<Integer, ArrayList<Integer>> entity : edgeMap.entrySet()) {
            Integer id1 = entity.getKey();
            Rectangle r1 = vertexMap.get(id1);
            ArrayList<Integer> r2l = entity.getValue();
            for (Integer id2 : r2l) {
                if (pathEdge.keySet().contains(id1) && pathEdge.get(id1).equals(id2)) {
                    Rectangle r2 = vertexMap.get(id2);
                    drawArrowLine(g2, r1, r2, ARROW_WIDTH, ARROW_HEIGHT);
                }
            }
        }
    }

    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics2D g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;

        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;

        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;

        int[] xpoints = {x2, (int) xm, (int) xn};
        int[] ypoints = {y2, (int) ym, (int) yn};

        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(xpoints, ypoints, 3);
    }

    private void drawArrowLine(Graphics2D g2, Rectangle r1, Rectangle r2, int d, int h) {
        drawArrowLine(g2, (int)r1.getCenterX(), (int)r1.getCenterY(), (int)r2.getCenterX(), (int)r2.getCenterY(), ARROW_WIDTH, ARROW_HEIGHT);
    }


    private void drawVertexes(Graphics2D g2) {
        // draw not selected ovals
        g2.setColor(Color.CYAN);
        for (Map.Entry<Integer, Rectangle> entity : vertexMap.entrySet()) {
            if (!highlightRectangleIdList.contains(entity.getKey()) && !pathVertex.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, POINT_RADIUS * 2, POINT_RADIUS * 2);
            }
        }

        // draw selected ovals
        g2.setColor(Color.red);
        for (Map.Entry<Integer, Rectangle> entity : vertexMap.entrySet()) {
            if (highlightRectangleIdList.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, POINT_RADIUS * 2, POINT_RADIUS * 2);
            }
        }

        g2.setColor(Color.orange);
        for (Map.Entry<Integer, Rectangle> entity : vertexMap.entrySet()) {
            if (pathVertex.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, POINT_RADIUS * 2, POINT_RADIUS * 2);
            }
        }

        // draw text labels
        g2.setColor(Color.BLACK);
        FontMetrics metrics = g2.getFontMetrics();
        for (Map.Entry<Integer, Rectangle> entity : vertexMap.entrySet()) {
            String label = entity.getKey().toString();
            int labelWidth = metrics.stringWidth(label)/2;
            Point p = new Point(entity.getValue().x, entity.getValue().y);
            g2.drawString(label, p.x - labelWidth + POINT_RADIUS, p.y + (metrics.getHeight() / 3) + POINT_RADIUS);
        }
    }

    private void drawDraggingLine(Graphics2D g2) {
        if (draggingLineStartPoint != null && draggingLineEndPoint != null) {
            g2.setColor(Color.red);
            g2.drawLine(draggingLineStartPoint.x, draggingLineStartPoint.y, draggingLineEndPoint.x, draggingLineEndPoint.y);
        }
    }

    private void calculateVertexConnections() {
        if (highlightRectangleIdList.size() == 2) {
            Integer[] a = highlightRectangleIdList.toArray(new Integer[2]);

            ArrayList<Integer> l0 = edgeMap.containsKey(a[0]) ? edgeMap.get(a[0]) : new ArrayList<>();
            l0.add(a[1]);
            edgeMap.put(a[0], l0);
            System.out.println("add edge " + a[0] + " -> " + a[1]);
        }
    }

    private void calculateHighlightRectangles() {
        highlightRectangleIdList.clear();
        //find start vertex
        if (null != draggingLineStartPoint) {
            for (Map.Entry<Integer, Rectangle> entry : vertexMap.entrySet()) {
                if (entry.getValue().contains(draggingLineStartPoint)) {
                    draggingLineStartPoint = centerPointFromRect(entry.getValue());
                    highlightRectangleIdList.add(entry.getKey());
                    break;
                }
            }
        }
        //if found start vertex find end vertex
        if (!highlightRectangleIdList.isEmpty() && null != draggingLineEndPoint) {
            for (Map.Entry<Integer, Rectangle> entry : vertexMap.entrySet()) {
                if (entry.getValue().contains(draggingLineEndPoint)) {
                    draggingLineEndPoint = centerPointFromRect(entry.getValue());
                    highlightRectangleIdList.add(entry.getKey());
                    break;
                }
            }
        }

        System.out.println("calculateHighlightRectangles: " + highlightRectangleIdList);
    }

    private Point centerPointFromRect(Rectangle rect) {
        return new Point((int)rect.getCenterX(), (int)rect.getCenterY());
    }

    private static void createAndShowGui() {
        CustomPanel mainPanel = new CustomPanel();
        mainPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("DrawGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static CustomPanel createCustomPanel() {
        CustomPanel mainPanel = new CustomPanel();
        mainPanel.setPreferredSize(new Dimension(800, 600));
        return mainPanel;
    }

    /**
     * Returns distance between two 2D points
     *
     * @param point1
     *            first point
     * @param point2
     *            second point
     * @return distance between points
     */
    public static double getDistance(Point point1, Point point2)
    {
        return getDistance(point1.x, point1.y, point2.x, point2.y);
    }


    /**
     * Returns distance between two sets of coords
     *
     * @param x1
     *            first x coord
     * @param y1
     *            first y coord
     * @param x2
     *            second x coord
     * @param y2
     *            second y coord
     * @return distance between sets of coords
     */
    public static double getDistance(float x1, float y1, float x2, float y2)
    {
        // using long to avoid possible overflows when multiplying
        double dx = x2 - x1;
        double dy = y2 - y1;

        // return Math.hypot(x2 - x1, y2 - y1); // Extremely slow
        // return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); // 20 times faster than hypot
        return Math.sqrt(dx * dx + dy * dy); // 10 times faster then previous line
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomPanel::createAndShowGui);
    }
}