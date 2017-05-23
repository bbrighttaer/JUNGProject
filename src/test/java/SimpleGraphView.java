import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by brigh on 5/23/2017.
 */
public class SimpleGraphView {
    private Graph<Integer, String> g;

    public SimpleGraphView() {
        this.g = new SparseMultigraph<Integer, String>();
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addEdge("Edge-A", 1, 3);
        g.addEdge("Edge-B", 2, 3, EdgeType.DIRECTED);
        g.addEdge("Edge-C", 3, 2, EdgeType.DIRECTED);
        g.addEdge("Edge-P", 2, 3);
    }

    public static void main(String[] args) {
        SimpleGraphView sgv = new SimpleGraphView();
        Layout<Integer, String> layout = new CircleLayout<Integer, String>(sgv.g);
        layout.setSize(new Dimension(300, 300));
        BasicVisualizationServer<Integer, String> vv =
                new BasicVisualizationServer<Integer, String>(layout);
        vv.setPreferredSize(new Dimension(350, 350));

        JFrame frame = new JFrame("Simple Graph View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(vv);
        frame.pack();
        frame.setVisible(true);
    }
}
