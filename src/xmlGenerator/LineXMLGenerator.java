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


public class LineXMLGenerator
{
	static boolean timeEffect=true;//time tolerance is effective or not; change the value of timeEffect as true
	static boolean costEffect=false;//cost tolerance is effective or not
	static int ddgSize=50;//the number of node 
	
	public static void changeDDGSize(int size)
	{
		LineXMLGenerator.ddgSize = size;
	}
	
	public static void generateXML(String filePath) throws IOException 
	{
		int ddgSize=LineXMLGenerator.ddgSize;
		int MIN_SIZE = 1;//dataset size***G
		int MAX_SIZE = 100;
		double MIN_GEN_TIME = 10.0/24;//generate time/day
		double MAX_GEN_TIME = 100.0/24;
		
		double MIN_USAGE_FREQ = 1.0/(30*365);//use frequency /day
		double MAX_USAGE_FREQ = 1.0/365;

		int MIN_DELAY_TOLERANCE = 0;//time tolerance
		int MAX_DELAY_TOLERANCE = 20000;
		
		Element datasetElement = null;
		Node textNode = null;
		
		Document xmldoc = new DocumentImpl();
		
		Element root = xmldoc.createElement("Datasets");

		for (int i = 1; i <= ddgSize; i++) 
		{
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
			double temptime=generateRandomNumber(MIN_GEN_TIME, MAX_GEN_TIME);
			textNode = xmldoc.createTextNode(""+temptime);
			genTimeElement.appendChild(textNode);
			datasetElement.appendChild(genTimeElement);
			
			

			Element usageFrequencyElement = xmldoc.createElementNS(null,
					"UsageFrequency");
			textNode = xmldoc.createTextNode(""
					+ generateRandomNumber(MIN_USAGE_FREQ, MAX_USAGE_FREQ));
			usageFrequencyElement.appendChild(textNode);
			datasetElement.appendChild(usageFrequencyElement);

			Element delayToleranceElement = xmldoc.createElementNS(null,"DelayTolerance");
			textNode = xmldoc.createTextNode(""+generateRandomNumber3(MIN_DELAY_TOLERANCE,MAX_DELAY_TOLERANCE));
			delayToleranceElement.appendChild(textNode);
			datasetElement.appendChild(delayToleranceElement);
			
			Element costToleranceElement = xmldoc.createElementNS(null,"CostTolerance");
			textNode = xmldoc.createTextNode(""+generateRandomNumber2());
			costToleranceElement.appendChild(textNode);
			datasetElement.appendChild(costToleranceElement);

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
				Element successorsElement = xmldoc.createElementNS(null,"Successors");

				Element successorElement = xmldoc.createElementNS(null,"Successor");
				int successor = i + 1;
				textNode = xmldoc.createTextNode("d" + successor);
				successorElement.appendChild(textNode);

				successorsElement.appendChild(successorElement);
				datasetElement.appendChild(successorsElement);
			}

			root.appendChild(datasetElement);
		}

		xmldoc.appendChild(root);

		FileOutputStream fos = new FileOutputStream("D:/workspace/ALgorithm/local optimal v4(0808)/local optimal v4/xmlFolder/LineXML/"+filePath);

		OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
		of.setIndent(1);
		of.setIndenting(true);

		XMLSerializer serializer = new XMLSerializer(fos, of);

		serializer.asDOMSerializer();
		serializer.serialize(xmldoc.getDocumentElement());
		fos.close();

	}

	/**
	 * generate the cost tolerance ¦Ë
	 * @return
	 */
	private static double generateRandomNumber2() 
	{
		double randomNumber=1.0;
		if(costEffect)
		{
			Random random = new Random();
			randomNumber=random.nextDouble();
		}
		randomNumber=1.0;
		return randomNumber;
	}
	/**
	 * Generates and returns a random nuber between the given min and max values
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	private static double generateRandomNumber(double min, double max) 
	{
		
			Random random = new Random();
			double range =  max - min;
			double fraction = range * random.nextDouble();
			double randomNumber = fraction + min;

		return randomNumber;
	}
	/**
	 * generate time tolerance
	 * @param args
	 */	private static double generateRandomNumber3(double min, double max) 
	                                                                        
		{
			double randomNumber=-1.0;
			if(timeEffect)
			{
				Random random = new Random();
				double range =  max -  min + 1;
				double fraction = range * random.nextDouble();
				randomNumber = fraction + min;
			}
			//set time tolerance as infinity
			return randomNumber;
		}
//////////////////////////////	
	public static void main(String []args)
	{
		LineXMLGenerator.changeDDGSize(2);
		try 
		{
			LineXMLGenerator.generateXML("testlineDDG2.xml");
			System.out.println("The end of the generated");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

}
