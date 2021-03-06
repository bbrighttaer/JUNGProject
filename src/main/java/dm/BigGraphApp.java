package dm;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by brigh on 5/24/2017.
 */
public class BigGraphApp {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        GraphLoader graphLoader = new GraphLoader("airlines.graphml");
        DataMining dataMining = new DataMining(DataMining.GraphType.directed, graphLoader.getDirectedGraph());
        long start = System.nanoTime();
        dataMining.mineBigGraph("Airport Dataset");
        System.out.println("Execution time: "+(System.nanoTime() - start)*1e-9);
    }
}
