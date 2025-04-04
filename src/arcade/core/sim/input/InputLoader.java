package arcade.core.sim.input;

import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import arcade.core.util.Box;
import static arcade.core.util.MiniBox.TAG_SEPARATOR;

/**
 * Custom XML file loader that uses SAX parsing to iterate through XML files.
 *
 * <p>XML files should only be one level deep, with all values as attributes. Results are stored in
 * a {@link arcade.core.util.Box} object.
 */
public class InputLoader extends DefaultHandler {
    /** Logger for {@code InputLoader}. */
    private static final Logger LOGGER = Logger.getLogger(InputLoader.class.getName());

    /** XML reader. */
    XMLReader xmlReader;

    /** Box holding parsed XML. */
    Box box;

    /** Creates a {@code InputLoader} using {@code SAXParserFactory}. */
    public InputLoader() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the given XML file into a {@link arcade.core.util.Box}.
     *
     * @param xml the XML file
     * @return the box containing the parsed XML
     */
    public Box load(String xml) throws IOException, SAXException {
        this.box = new Box();
        return load(xml, box);
    }

    /**
     * Loads the given XML file into given {@link arcade.core.util.Box}.
     *
     * @param xml the XML file
     * @param loadBox the box to load into
     * @return the box containing the parsed XML
     */
    public Box load(String xml, Box loadBox) throws IOException, SAXException {
        this.box = loadBox;
        LOGGER.config("loading XML file [ " + xml + " ]");
        xmlReader.parse(xml);
        LOGGER.config("successfully loaded XML file [ " + xml + " ]\n\n" + box.toString());
        return box;
    }

    /**
     * Called at the start of an XML element.
     *
     * <p>Iterates through each attribute and adds to the box.
     *
     * @param uri the namespace URI
     * @param lName the local name
     * @param qName the qualified name
     * @param att the attributes
     */
    @Override
    public void startElement(String uri, String lName, String qName, Attributes att) {
        int numAtts = att.getLength();
        String id;
        String tag;
        String filter;

        // Iterate through each attribute and add to a map.
        if (numAtts > 0) {
            // Set id.
            id = att.getValue("id");

            // Search for any special attributes to set as tag.
            String[] split = qName.split("\\.");
            if (split.length == 2) {
                filter = split[1];
                tag = att.getValue(filter);
                if (tag == null) {
                    return;
                }
                id = tag + TAG_SEPARATOR + id;
            } else {
                filter = "";
            }

            // Add target tag if set.
            if (att.getValue("class") != null) {
                id = att.getValue("class") + TAG_SEPARATOR + id;
            }

            box.addTag(id, split[0].toUpperCase());

            for (int i = 0; i < numAtts; i++) {
                String name = att.getQName(i);
                if (!name.equals("id") && !name.equals(filter)) {
                    box.addAtt(id, name, att.getValue(i));
                }
            }
        }
    }
}
