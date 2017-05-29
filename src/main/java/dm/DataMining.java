package dm;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.generators.random.KleinbergSmallWorldGenerator;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.GradientVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created by brigh on 5/24/2017.
 */
public class DataMining {
    enum GraphType {directed, undirected}

    private final GraphType graphType;
    private final Graph graph;


    protected Supplier<Graph<Integer, Number>> graphFactory;
    protected Supplier<Integer> vertexFactory;
    protected Supplier<Number> edgeFactory;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Number, Number> vv;

    public DataMining(GraphType graphType, Graph graph) {
        this.graphType = graphType;
        this.graph = graph;
    }

    public GraphType getGraphType() {
        return graphType;
    }

    public Graph getGraph() {
        return graph;
    }

    public void mineBigGraph(String graphTitle) {
        switch (graphType) {
            case directed:
                directedGraphMining(graphTitle);
                break;
            case undirected:
                undirectedGraphMining(graphTitle);
                break;
        }
    }

    private void directedGraphMining(String graphTitle) {
        DirectedGraph directedGraph = (DirectedGraph) graph;
        System.out.println(directedGraph.toString());
        long start = 0L;

        //General information about graph
        start = System.nanoTime();
        generalInfo(directedGraph);
        System.out.println("General Info Execution time: "+(System.nanoTime() - start)*1e-9);

        //Ranking
        start = System.nanoTime();
        rankingAnalysis(directedGraph);
        System.out.println("Ranking Execution time: "+(System.nanoTime() - start)*1e-9);

        //Clustering
        start = System.nanoTime();
        clustering(graph);
        System.out.println("Clustering Execution time: "+(System.nanoTime() - start)*1e-9);

        //Topology
        start = System.nanoTime();
        topologyAnalysis(graph);
        System.out.println("Topology Execution time: "+(System.nanoTime() - start)*1e-9);

        //Randomly Generated sub-graphs or networks
        start = System.nanoTime();
        randomlyGeneratedGraphs(graph);
        System.out.println("Random Graph Execution time: "+(System.nanoTime() - start)*1e-9);

        //display
        start = System.nanoTime();
        visualizeGraph(graph, graphTitle);
        System.out.println("Graph Display Execution time: "+(System.nanoTime() - start)*1e-9);
    }

    private void generalInfo(Graph graph) {
        //number of vertices and edges
        System.out.println(String.format("Edge count: %d, Vertex count: %d", graph.getEdgeCount(),
                                         graph.getVertexCount()));

        //in and out degrees
        Collection edges = graph.getEdges();
        Collection vertices = graph.getVertices();
        int maxInDegree = 0;
        int maxOutDegree = 0;
        Object maxInDegreeVertex = null;
        Object maxOutDegreeVertex = null;
        for (Object v : vertices) {
            int inDegree = graph.inDegree(v);
            int outDegree = graph.outDegree(v);
            if (inDegree > maxInDegree) {
                maxInDegree = inDegree;
                maxInDegreeVertex = v;
            }
            if (outDegree > maxOutDegree) {
                maxOutDegree = outDegree;
                maxOutDegreeVertex = v;
            }
        }
        System.out.println("Max in-degree: " + maxInDegree + ", vertexID: " + maxInDegreeVertex);
        System.out.println("Max out-degree: " + maxOutDegree + ", vertexID: " + maxOutDegreeVertex);
    }

    private void rankingAnalysis(Graph graph) {
        //BetweennessCentrality
        System.out.println("---- BetweennessCentrality Ranking ----");
        BetweennessCentrality bCentrality = new BetweennessCentrality(graph, true, false);
        bCentrality.evaluate();
        bCentrality.printRankings(true, true);

        //PageRank
        System.out.println("---- PageRank ranking ----");
        final PageRank pageRank = new PageRank(graph, .1);
        pageRank.evaluate();
        final List pageRankVertices = new ArrayList(graph.getVertices());
        pageRankSorting(pageRank, pageRankVertices);

        //PageRank with Priors
        System.out.println("---- PageRank with Priors -----");
        PageRankWithPriors pageRankWithPriors = new PageRankWithPriors(graph, new Function() {
            public Object apply(Object o) {
                return getDoubleVal(pageRankVertices.get(0));
            }
        }, 0.1);
        pageRankWithPriors.evaluate();
        final List pageRankWithPriorsVertices = new ArrayList(graph.getVertices());
        pageRankSorting(pageRankWithPriors, pageRankWithPriorsVertices);

    }

    private void pageRankSorting(final PageRankWithPriors pageRank, List pageRankVertices) {
        Collections.sort(pageRankVertices, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((getDoubleVal(pageRank.getVertexScore(o2)) < getDoubleVal(pageRank.getVertexScore(o1))) ? -1 :
                        ((getDoubleVal(pageRank.getVertexScore(o2)) > getDoubleVal(pageRank.getVertexScore(o1))) ? 1 : 0));
            }
        });
        for (Object v :
                pageRankVertices) {
            System.out.println(v.toString() + ", rank score: " + pageRank.getVertexScore(v));
        }
    }

    private void clustering(Graph graph) {
        //EdgeBetweenness
        System.out.println("------ EdgeBetweenness Clustering ----");
        EdgeBetweennessClusterer betweennessClusterer = new EdgeBetweennessClusterer(200);
        Object[] clusters = betweennessClusterer.apply(graph).toArray();
        System.out.println("Number of clusters: " + clusters.length);
        for (int i = 0; i < clusters.length; i++) {
            System.out.println(String.format("---- Cluster %d ---", (i + 1)));
            Set cluster = (Set) clusters[i];
            System.out.println(Arrays.asList(cluster.toArray())+", size: "+cluster.size());
        }

        //WCC
        System.out.println("------ Weakely Connected Component (WCC) Clustering ----");
        WeakComponentClusterer wcc = new WeakComponentClusterer();
        Object[] wccClusters = wcc.apply(graph).toArray();
        System.out.println("Number of WCC clusters: " + wccClusters.length);
        for (int i = 0; i < wccClusters.length; i++) {
            System.out.println(String.format("---- Cluster %d ---", (i + 1)));
            Set cluster = (Set) wccClusters[i];
            System.out.println(Arrays.asList(cluster.toArray()));
        }
    }

    private void topologyAnalysis(final Graph graph) {
        Object[] verticesArr = graph.getVertices().toArray();
        Object firstVertex = verticesArr[0];
        Object lastVertex = verticesArr[verticesArr.length - 1];

        //BFSDistanceLabeler
        System.out.println(String.format("---------- BFSDistanceLabeler %s ----------", firstVertex.toString()));
        BFSDistanceLabeler distanceLabeler = new BFSDistanceLabeler();
        System.out.println("Root vertex: " + firstVertex.toString());
        distanceLabeler.labelDistances(graph, firstVertex);
        Map distanceDecorator = distanceLabeler.getDistanceDecorator();
        distanceDecorator.forEach((k, v) -> System.out.println(String.format("Vertex: %s , distance: %s",
                                                                             k.toString(),
                                                                             getDoubleVal(v))));

        //KNeighborhoodExtractor
        System.out.println("------- KNeighborhood Extractor --------");
        KNeighborhoodFilter neighborhoodFilter = new KNeighborhoodFilter(firstVertex, 1,
                                                                         KNeighborhoodFilter.EdgeType.IN_OUT);
        Graph subGraph = neighborhoodFilter.apply(graph);
        //visualizeGraph(subGraph);

        //DijkstraShortestPath
        System.out.println(String.format("------- Djikstra's Shortest path from %s --------", firstVertex.toString()));
        DijkstraShortestPath shortestPath = new DijkstraShortestPath(graph);
        Map distanceMap = shortestPath.getDistanceMap(firstVertex);
        distanceMap.forEach((v, d) -> System.out.println(String.format("Vertex: %s , distance: %s",
                                                                       v.toString(),
                                                                       getDoubleVal(d))));
    }

    public void randomlyGeneratedGraphs(Graph graph) {
        //BarabasiAlbertGenerator
        System.out.println("---- Barabasi Albert Generator ----");
        int init_vertices = 1;
        int edges_to_add_per_timestep = 1;
        int random_seed = 0;
        int num_timesteps = 20;
        setUpBarabasiVariables();
        BarabasiAlbertGenerator<Integer, Number> barabasiGraphGen =
                new BarabasiAlbertGenerator<>(graphFactory,
                                              vertexFactory, edgeFactory, init_vertices,
                                              edges_to_add_per_timestep, random_seed,
                                              new HashSet<Integer>(graph.getVertices()));
        barabasiGraphGen.evolveGraph(num_timesteps);
        Graph barabasiGraph = barabasiGraphGen.get();
        visualizeGraph(barabasiGraph,"BarabasiAlbertGenerator");

        //Kleinberg Small world generator
        System.out.println("---- Small World Graph Generator ----");
        KleinbergSmallWorldGenerator smallWorldGenerator = new KleinbergSmallWorldGenerator(graphFactory,
                                                                                            vertexFactory,
                                                                                            edgeFactory,
                                                                                            4,
                                                                                            3);
        Graph smallWorldGraph = smallWorldGenerator.get();
        visualizeGraph(smallWorldGraph, "Kleinberg Small world");

        //EppsteinPowerLaw Generator
        EppsteinPowerLawGenerator powerLawGenerator = new EppsteinPowerLawGenerator(graphFactory, vertexFactory,
                                                                                    edgeFactory, 30,
                                                                                    200, 50);
        Graph powerlawGraph = powerLawGenerator.get();
        visualizeGraph(powerlawGraph, "Eppstein Power law");
    }

    private double getDoubleVal(Object o) {
        return Double.valueOf(o.toString());
    }

    private void undirectedGraphMining(String graphTitle) {
        UndirectedGraph undirectedGraph = (UndirectedGraph) graph;
        System.out.println(undirectedGraph.toString());

        //General information about graph
        generalInfo(undirectedGraph);

        //Ranking
        rankingAnalysis(undirectedGraph);

        //Clustering
        clustering(graph);

        //Topology
        topologyAnalysis(graph);

        //Randomly Generated sub-graphs or networks
        randomlyGeneratedGraphs(graph);

        //display
        visualizeGraph(graph, graphTitle);
    }

    private void visualizeGraph(final Graph graph, String title) {
        // create a simple graph for the demo
        vv = new VisualizationViewer<Number, Number>(new FRLayout<Number, Number>(graph));

        vv.addGraphMouseListener(new TestGraphMouseListener<Number>());
        vv.getRenderer().setVertexRenderer(
                new GradientVertexRenderer<Number, Number>(
                        Color.white, Color.red,
                        Color.white, Color.blue,
                        vv.getPickedVertexState(),
                        false));

        // add my listeners for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller());
        vv.setEdgeToolTipTransformer(new Function<Number, String>() {
            public String apply(Number edge) {
                return "E" + graph.getEndpoints(edge).toString();
            }
        });

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
        vv.getRenderer().getVertexLabelRenderer().setPositioner(new BasicVertexLabelRenderer.InsidePositioner());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

        // create a frome to hold the graph
        final JFrame frame = new JFrame(title);
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final AbstractModalGraphMouse graphMouse = new DefaultModalGraphMouse<Number, Number>();
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());

        JMenuBar menubar = new JMenuBar();
        menubar.add(graphMouse.getModeMenu());
        panel.setCorner(menubar);


        vv.addKeyListener(graphMouse.getModeKeyListener());
        vv.setToolTipText("<html><center>Type 'p' for Pick mode<p>Type 't' for Transform mode");

        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1 / 1.1f, vv.getCenter());
            }
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener<V> implements GraphMouseListener<V> {

        public void graphClicked(V v, MouseEvent me) {
            System.out.println("Vertex " + v + " was clicked at (" + me.getX() + "," + me.getY() + ")");
        }

        public void graphPressed(V v, MouseEvent me) {
            System.out.println("Vertex " + v + " was pressed at (" + me.getX() + "," + me.getY() + ")");
        }

        public void graphReleased(V v, MouseEvent me) {
            System.out.println("Vertex " + v + " was released at (" + me.getX() + "," + me.getY() + ")");
        }
    }

    private void setUpBarabasiVariables() {
        graphFactory = new Supplier<Graph<Integer, Number>>() {
            public Graph<Integer, Number> get() {
                return new SparseMultigraph<Integer, Number>();
            }
        };
        vertexFactory = new Supplier<Integer>() {
            int count;

            public Integer get() {
                return count++;
            }
        };
        edgeFactory = new Supplier<Number>() {
            int count;

            public Number get() {
                return count++;
            }
        };
    }
}
