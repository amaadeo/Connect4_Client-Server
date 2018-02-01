import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ServerConfiguration {
    private static String host;
    private static int port;

    static {
        try {
            loadFromXML();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFromXML() throws Exception {
        File xmlFile = new File("serverconfiguration.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);

        doc.getDocumentElement().normalize();

        NodeList nl = doc.getElementsByTagName("server");
        Node nNode = nl.item(0);
        if(nNode.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) nNode;
            host = e.getElementsByTagName("host").item(0).getTextContent();
            port = Integer.parseInt(e.getElementsByTagName("port").item(0).getTextContent());
        }

    }

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }
}