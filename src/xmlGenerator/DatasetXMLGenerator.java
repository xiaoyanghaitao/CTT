package xmlGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Class to generate an xml which represents a large linear DDG
 * 
 * @author Saichander
 * 
 */
public class DatasetXMLGenerator
{

	/**
	 * Generates a large xml which represents a large linear DDG
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void generateXML() throws IOException 
	{
		int ddgSize = 200;

		int MIN_SIZE = 100;
		int MAX_SIZE = 20000;
		int MIN_GEN_TIME = 10;
		int MAX_GEN_TIME = 1000;
		int MIN_USAGE_FREQ = 1;
		int MAX_USAGE_FREQ = 10;
		int MIN_DELAY_TOLERANCE = 0;
		int MAX_DELAY_TOLERANCE = 1;

		Element datasetElement = null;
		Node textNode = null;
		// Document (Xerces implementation only).
		Document xmldoc = new DocumentImpl();
		// Root element.
		Element root = xmldoc.createElement("Datasets");

		for (int i = 1; i <= ddgSize; i++) 
		{
			// double size = generateRandomNumber(MIN_SIZE, MAX_SIZE);
			// double genTime = generateRandomNumber(MIN_GEN_TIME,
			// MAX_GEN_TIME);
			// double usageFreq = generateRandomNumber(MIN_USAGE_FREQ,
			// MAX_USAGE_FREQ);
			// double delayTolerance = generateRandomNumber(MIN_DELAY_TOLERANCE,
			// MAX_DELAY_TOLERANCE);

			datasetElement = xmldoc.createElementNS(null, "Dataset");

			Element nameElement = xmldoc.createElementNS(null, "Name");
			textNode = xmldoc.createTextNode("d" + i);
			nameElement.appendChild(textNode);
			datasetElement.appendChild(nameElement);

			Element sizeElement = xmldoc.createElementNS(null, "Size");
			textNode = xmldoc.createTextNode(""+ generateRandomNumber(MIN_SIZE, MAX_SIZE));
			sizeElement.appendChild(textNode);
			datasetElement.appendChild(sizeElement);

			Element genTimeElement = xmldoc.createElementNS(null, "GenTime");
			textNode = xmldoc.createTextNode(""+ generateRandomNumber(MIN_GEN_TIME, MAX_GEN_TIME));
			genTimeElement.appendChild(textNode);
			datasetElement.appendChild(genTimeElement);

			Element usageFrequencyElement = xmldoc.createElementNS(null,
					"UsageFrequency");
			textNode = xmldoc.createTextNode(""
					+ generateRandomNumber(MIN_USAGE_FREQ, MAX_USAGE_FREQ));
			usageFrequencyElement.appendChild(textNode);
			datasetElement.appendChild(usageFrequencyElement);

			Element delayToleranceElement = xmldoc.createElementNS(null,
					"DelayTolerance");
			textNode = xmldoc.createTextNode(""
					+ generateRandomNumber(MIN_DELAY_TOLERANCE,
							MAX_DELAY_TOLERANCE));
			delayToleranceElement.appendChild(textNode);
			datasetElement.appendChild(delayToleranceElement);

			if (i > 1) {
				Element predecessorsElement = xmldoc.createElementNS(null,
						"Predecessors");

				Element predecessorElement = xmldoc.createElementNS(null,
						"Predecessor");
				int predecessor = i - 1;
				textNode = xmldoc.createTextNode("d" + predecessor);
				predecessorElement.appendChild(textNode);

				predecessorsElement.appendChild(predecessorElement);
				datasetElement.appendChild(predecessorsElement);
			}

			if (i < ddgSize) 
			{
				Element successorsElement = xmldoc.createElementNS(null,
						"Successors");

				Element successorElement = xmldoc.createElementNS(null,
						"Successor");
				int successor = i + 1;
				textNode = xmldoc.createTextNode("d" + successor);
				successorElement.appendChild(textNode);

				successorsElement.appendChild(successorElement);
				datasetElement.appendChild(successorsElement);
			}

			root.appendChild(datasetElement);
		}

		xmldoc.appendChild(root);

		FileOutputStream fos = new FileOutputStream("SampleDDG.xml");
		// XERCES 1 or 2 additionnal classes.
		OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
		of.setIndent(1);
		of.setIndenting(true);
		// of.setDoctype(null,"users.dtd");
		XMLSerializer serializer = new XMLSerializer(fos, of);
		// As a DOM Serializer
		serializer.asDOMSerializer();
		serializer.serialize(xmldoc.getDocumentElement());
		fos.close();

	}

	/**
	 * Generates and returns a random nuber between the given min and max values
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	private static double generateRandomNumber(int min, int max) 
	{
		Random random = new Random();
		long range = (long) max - (long) min + 1;
		double fraction = range * random.nextDouble();
		double randomNumber = fraction + min;

		return randomNumber;
	}

}
