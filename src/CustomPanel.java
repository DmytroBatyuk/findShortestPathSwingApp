import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;

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
    private int pointRadius = 10;
    private int counter = 1;

    private Point draggingLineStartPoint;
    private Point draggingLineEndPoint;
    private HashSet<Integer> highlightRectangleIdSet = new HashSet<>();

    private HashSet<Integer> pathVertex = new HashSet<>();
    private HashMap<Integer, Integer> pathEdge = new HashMap<>();

    private HashMap<Integer, Rectangle> rectangles = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> vertexIdToVertexIdMap = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> vertexIdToVertexIdRevMap = new HashMap<>();


    public void clear() {
        draggingLineStartPoint = null;
        draggingLineEndPoint = null;
        highlightRectangleIdSet.clear();
        rectangles.clear();
        vertexIdToVertexIdMap.clear();
        vertexIdToVertexIdRevMap.clear();
        pathVertex.clear();
        pathEdge.clear();
        repaint();
    }

    public void calculate(Integer fromV, Integer toV) {
        pathVertex.clear();
        pathEdge.clear();

        Graph<Integer, DefaultOpenVertexEdge> graph = new SimpleGraph<>(DefaultOpenVertexEdge.class);
        for (Integer v : getVertexes()) {
            graph.addVertex(v);
        }
        for (Map.Entry<Integer, ArrayList<Integer>> entry : getEdges().entrySet()) {
            Integer v1 = entry.getKey();
            for (Integer v2 : entry.getValue()) {
                graph.addEdge(v1, v2);
            }
        }


        GraphPath<Integer, DefaultOpenVertexEdge> path = new DijkstraShortestPath<>(graph).getPath(fromV, toV);
//        System.out.println("!!!! shortest path edges= " + path.getEdgeList() + ", vertex" + path.getVertexList());
        pathVertex.addAll(path.getVertexList());
        for (DefaultOpenVertexEdge edge : path.getEdgeList()) {
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
            rectangles.remove(id);
            removeIdFromBothMaps(id, vertexIdToVertexIdMap, vertexIdToVertexIdRevMap);
            removeIdFromBothMaps(id, vertexIdToVertexIdRevMap, vertexIdToVertexIdMap);
        } else {
            rectangles.put(counter++, new Rectangle(p.x - pointRadius, p.y - pointRadius, pointRadius * 2, pointRadius * 2));
        }
    }

    private void removeIdFromBothMaps(Integer id, Map<Integer, ArrayList<Integer>> map, Map<Integer, ArrayList<Integer>> revMap) {
        ArrayList<Integer> connIds = map.remove(id);
        if (connIds != null) {
            for (Integer connId : connIds) {
                ArrayList<Integer> revConnIds = revMap.get(connId);
                if (revConnIds != null) {
                    revConnIds.remove(id);
                }
            }
        }
    }
    private Integer findVertexByPoint(Point p) {
        for (Map.Entry<Integer, Rectangle> entity : rectangles.entrySet()) {
            if (entity.getValue().contains(p)) {
                return entity.getKey();
            }
        }
        return null;
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(padding, padding, getWidth() - (2 * padding), getHeight() - 2 * padding);
    }

    private void drawVertexConnections(Graphics2D g2) {
        g2.setColor(Color.CYAN);
        for (Map.Entry<Integer, ArrayList<Integer>> entity : vertexIdToVertexIdMap.entrySet()) {
            Integer id1 = entity.getKey();
            Rectangle r1 = rectangles.get(id1);
            ArrayList<Integer> r2l = entity.getValue();
            for (Integer id2 : r2l) {
                if (!pathEdge.keySet().contains(id1) || !pathEdge.get(id1).equals(id2)) {
                    Rectangle r2 = rectangles.get(id2);
                    g2.drawLine((int)r1.getCenterX(), (int)r1.getCenterY(), (int)r2.getCenterX(), (int)r2.getCenterY());
                }
            }
        }

        g2.setColor(Color.orange);
        for (Map.Entry<Integer, ArrayList<Integer>> entity : vertexIdToVertexIdMap.entrySet()) {
            Integer id1 = entity.getKey();
            Rectangle r1 = rectangles.get(id1);
            ArrayList<Integer> r2l = entity.getValue();
            for (Integer id2 : r2l) {
                if (pathEdge.keySet().contains(id1) && pathEdge.get(id1).equals(id2)) {
                    Rectangle r2 = rectangles.get(id2);
                    g2.drawLine((int)r1.getCenterX(), (int)r1.getCenterY(), (int)r2.getCenterX(), (int)r2.getCenterY());
                }
            }
        }
    }
    private void drawVertexes(Graphics2D g2) {
        // draw not selected ovals
        g2.setColor(Color.CYAN);
        for (Map.Entry<Integer, Rectangle> entity : rectangles.entrySet()) {
            if (!highlightRectangleIdSet.contains(entity.getKey()) && !pathVertex.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, pointRadius * 2, pointRadius * 2);
            }
        }

        // draw selected ovals
        g2.setColor(Color.red);
        for (Map.Entry<Integer, Rectangle> entity : rectangles.entrySet()) {
            if (highlightRectangleIdSet.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, pointRadius * 2, pointRadius * 2);
            }
        }

        g2.setColor(Color.orange);
        for (Map.Entry<Integer, Rectangle> entity : rectangles.entrySet()) {
            if (pathVertex.contains(entity.getKey())) {
                g2.fillOval(entity.getValue().x, entity.getValue().y, pointRadius * 2, pointRadius * 2);
            }
        }

        // draw text labels
        g2.setColor(Color.BLACK);
        FontMetrics metrics = g2.getFontMetrics();
        for (Map.Entry<Integer, Rectangle> entity : rectangles.entrySet()) {
            String label = entity.getKey().toString();
            int labelWidth = metrics.stringWidth(label)/2;
            Point p = new Point(entity.getValue().x, entity.getValue().y);
            g2.drawString(label, p.x - labelWidth + pointRadius, p.y + (metrics.getHeight() / 3) + pointRadius);
        }
    }

    private void drawDraggingLine(Graphics2D g2) {
        if (draggingLineStartPoint != null && draggingLineEndPoint != null) {
            g2.setColor(Color.red);
            g2.drawLine(draggingLineStartPoint.x, draggingLineStartPoint.y, draggingLineEndPoint.x, draggingLineEndPoint.y);
        }
    }

    private void calculateVertexConnections() {
        if (highlightRectangleIdSet.size() == 2) {
            Integer[] a = highlightRectangleIdSet.toArray(new Integer[2]);

            ArrayList<Integer> l0 = vertexIdToVertexIdMap.containsKey(a[0]) ? vertexIdToVertexIdMap.get(a[0]) : new ArrayList<>();
            l0.add(a[1]);
            vertexIdToVertexIdMap.put(a[0], l0);

            ArrayList<Integer> l1 = vertexIdToVertexIdMap.containsKey(a[1]) ? vertexIdToVertexIdMap.get(a[1]) : new ArrayList<>();
            l1.add(a[0]);
            vertexIdToVertexIdRevMap.put(a[1], l1);
        }
    }

    private void calculateHighlightRectangles() {
        highlightRectangleIdSet.clear();
        //find start vertex
        if (null != draggingLineStartPoint) {
            for (Map.Entry<Integer, Rectangle> entry : rectangles.entrySet()) {
                if (entry.getValue().contains(draggingLineStartPoint)) {
                    draggingLineStartPoint = centerPointFromRect(entry.getValue());
                    highlightRectangleIdSet.add(entry.getKey());
                    break;
                }
            }
        }
        //if found start vertex find end vertex
        if (!highlightRectangleIdSet.isEmpty() && null != draggingLineEndPoint) {
            for (Map.Entry<Integer, Rectangle> entry : rectangles.entrySet()) {
                if (entry.getValue().contains(draggingLineEndPoint)) {
                    draggingLineEndPoint = centerPointFromRect(entry.getValue());
                    highlightRectangleIdSet.add(entry.getKey());
                    break;
                }
            }
        }

        System.out.println("calculateHighlightRectangles: " + highlightRectangleIdSet);
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


    private Set<Integer> getVertexes() {
        return rectangles.keySet();
    }

    private Map<Integer, ArrayList<Integer>> getEdges() {
        return new HashMap<>(vertexIdToVertexIdMap);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomPanel::createAndShowGui);
    }
}