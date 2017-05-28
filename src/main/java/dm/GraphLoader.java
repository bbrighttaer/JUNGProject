package dm;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.io.GraphMLReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by brigh on 5/24/2017.
 */
public class GraphLoader {
    private String filename;

    public GraphLoader(String filename) {
        this.filename = "./src/main/resources/"+filename;
    }

    public Graph getDirectedGraph() throws ParserConfigurationException, SAXException, IOException {
        Supplier<Number> vertexFactory = new Supplier<Number>() {
            int n = 0;
            public Number get() { return n++; }
        };
        Supplier<Number> edgeFactory = new Supplier<Number>() {
            int n = 0;
            public Number get() { return n++; }
        };

        GraphMLReader<DirectedGraph<Number,Number>, Number, Number> gmlr =
                new GraphMLReader<DirectedGraph<Number, Number>, Number, Number>(vertexFactory, edgeFactory);
        DirectedGraph<Number,Number> graph = new DirectedSparseGraph<Number, Number>();
        gmlr.load(filename, graph);
        return graph;
    }

    public Graph getUnDirectedGraph() throws ParserConfigurationException, SAXException, IOException {
        Supplier<Number> vertexFactory = new Supplier<Number>() {
            int n = 0;
            public Number get() { return n++; }
        };
        Supplier<Number> edgeFactory = new Supplier<Number>() {
            int n = 0;
            public Number get() { return n++; }
        };

        GraphMLReader<UndirectedGraph<Number,Number>, Number, Number> gmlr =
            new GraphMLReader<UndirectedGraph<Number, Number>, Number, Number>(vertexFactory, edgeFactory);
        UndirectedGraph<Number,Number> graph = new UndirectedSparseGraph<Number, Number>();
       gmlr.load(filename, graph);
       return graph;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
