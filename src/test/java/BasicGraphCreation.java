import com.google.common.base.Function;
import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brigh on 5/23/2017.
 */
public class BasicGraphCreation {

    private int edgeCount = 0;
    private MyNode n1, n2, n3, n4, n5;
    private Graph<MyNode, MyLink> g;

    public static void main(String[] args) {
        BasicGraphCreation basicGraph = new BasicGraphCreation();
        basicGraph.buildGraph();
        basicGraph.unweightedShortestPath();
        basicGraph.weightedShortestPath();
        basicGraph.maxFlows();
    }

    public void maxFlows(){
        System.out.println("Max flows:");
        Map<MyLink, Double> edgeFlowMap = new HashMap<MyLink, Double>();
        EdmondsKarpMaxFlow<MyNode, MyLink> alg =
                new EdmondsKarpMaxFlow<MyNode, MyLink>((DirectedGraph<MyNode, MyLink>) g, n2, n5,
                        new Function<MyLink, Number>() {
                            public Number apply(MyLink myLink) {
                                return myLink.capacity;
                            }
                        },
                        new HashMap<MyLink, Number>(),
                        new Supplier<MyLink>() {
                            public MyLink get() {
                                return new MyLink(1, 1);
                            }
                        });
        alg.evaluate();
        System.out.println("The max flow is: "+alg.getMaxFlow());
        System.out.println("The edge set is: "+alg.getMinCutEdges()
                .toString());
    }

    public void weightedShortestPath(){
        System.out.println("weighted:");
        DijkstraShortestPath<MyNode, MyLink> alg =
                new DijkstraShortestPath<MyNode, MyLink>(g,
                        new Function<MyLink, Number>() {
                            public Number apply(MyLink myLink) {
                                return myLink.weight;
                            }
                        });
        List<MyLink> l = alg.getPath(n1, n4);
        Number dist = alg.getDistance(n1, n4);
        System.out.println("The shortest path from "+ n1 + " to "
                +n4+" is:");
        System.out.println(l.toString());
        System.out.println("and the length of the path is: "+dist);
    }

    public void unweightedShortestPath(){
        System.out.println("unweighted:");
        DijkstraShortestPath<MyNode, MyLink> alg =
                new DijkstraShortestPath<MyNode, MyLink>(g);
        List<MyLink> l = alg.getPath(n1, n4);
        System.out.println("The shortest unweighted path from "+ n1
        + " to "+ n4 + " is:");
        System.out.println(l.toString());
    }

    public void buildGraph(){
        g = new DirectedSparseMultigraph<MyNode, MyLink>();
        n1 = new MyNode(1);
        n2 = new MyNode(2);
        n3 = new MyNode(3);
        n4 = new MyNode(4);
        n5 = new MyNode(5);

        g.addEdge(new MyLink(2, 48), n1, n2,
                EdgeType.DIRECTED);
        g.addEdge(new MyLink(2, 48), n2, n3,
                EdgeType.DIRECTED);
        g.addEdge(new MyLink(3, 192), n3, n5,
                EdgeType.DIRECTED);
        g.addEdge(new MyLink(2, 48), n5, n4,
                EdgeType.DIRECTED);
        g.addEdge(new MyLink(2, 48), n4, n2);
        g.addEdge(new MyLink(2, 48), n3, n1);
        g.addEdge(new MyLink(10, 48), n2, n5);

        System.out.println(g.toString());
    }

    class MyNode {
        private int id;

        public MyNode(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "V" + id;
        }
    }

    class MyLink {
        private double capacity;
        private double weight;
        private int id;

        public MyLink(double weight, double capacity) {
            this.capacity = capacity;
            this.weight = weight;
            this.id = edgeCount++;
        }

        @Override
        public String toString() {
            return
                    "E" + id;
        }
    }
}
