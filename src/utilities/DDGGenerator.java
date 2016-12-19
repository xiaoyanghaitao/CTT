package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DDGGenerator 
{
	private static String filePath=null;
	private static DataDependencyGraph graph=null;

	public static void setFilePath(String filePath1)
	{
		File file=new File(filePath1);
		boolean res=file.exists();
		if(res)
		{
			DDGGenerator.filePath=filePath1;
			System.out.println("Modify XML successfully£º"+filePath);
			graph = new DataDependencyGraph();
		}
		else
			System.out.println("Failed to modify the file£º"+filePath);
	}

//test whether a file exists==========================================
	public void fileTest(String XMLpath)
	{
		boolean res=false;
		File file=new File(XMLpath);
		res=file.exists();
		if(res)
			this.filePath = XMLpath;
		else
			System.out.println(" the XML file does not exist£¡");
	}
//////////////////////////////////////////////////////////////////////////
	public static DataDependencyGraph getDDG() 
	{		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		NodeList datasetNodeList = null;

		try 
		{
			DocumentBuilder db = dbf.newDocumentBuilder(); 
			Document dom;
			try 
			{
				dom = db.parse(filePath);
				if (dom != null) 
				{
					Element docEle = dom.getDocumentElement();
					datasetNodeList = docEle.getElementsByTagName("Dataset");
					constructDDG(datasetNodeList);
				}
			} 
			catch (FileNotFoundException ex) 
			{
				System.out.println("DDG xml file is not found.");
			}
		} 
		catch (ParserConfigurationException pce) 
		{
			System.out.println("Error while parsing the DDG xml.");
		} 
		catch (SAXException se) 
		{
			System.out.println("Error while parsing the DDG xml.");
		}
		catch (IOException ioe)
		{
			System.out.println("Exception while reading the DDG xml.");
		}
		return graph;
	}

	/**
	 * construct the DDG
	 * 
	 * @param datasetNodeList
	 */
	private static void constructDDG(NodeList datasetNodeList)
	{

		for (int i = 0; i < datasetNodeList.getLength(); i++) 
		{

			Element datasetElement = (Element) datasetNodeList.item(i);
			String datasetName = getValueOfTag("Name", datasetElement);
			Dataset currentDS = getDataset(datasetName);
			String datasetSize = getValueOfTag("Size", datasetElement);
			currentDS.setSize(Double.parseDouble(datasetSize));

			String datasetGenTime = getValueOfTag("GenTime", datasetElement);
			currentDS.setGenerationTime(Double.parseDouble(datasetGenTime));

			String datasetUsageFrequency = getValueOfTag("UsageFrequency",datasetElement);
	
			currentDS.setUsageFrequency(Double.parseDouble(datasetUsageFrequency));

			String datasetDelayTolerance = getValueOfTag("DelayTolerance",datasetElement);

			currentDS.setDelayTolerance(Double.parseDouble(datasetDelayTolerance));


			String datasetCostTolerance = getValueOfTag("CostTolerance",datasetElement);

			currentDS.setCostTolerance(Double.parseDouble(datasetCostTolerance));

			List<Dataset> predecessors = getPredecessors(datasetElement);
			currentDS.setPredecessors(predecessors);
			

			List<Dataset> successors = getSuccessors(datasetElement);
			currentDS.setSuccessors(successors);
			
		}
	}

	/**
	 * Reads the value of the tag with the given tag name and returns it
	 * 
	 * @param tagName
	 * @param datasetElement
	 * @return
	 */
	private static String getValueOfTag(String tagName, Element datasetElement) 
	{
		NodeList aNodeList = datasetElement.getElementsByTagName(tagName);

		Element aElement = (Element) aNodeList.item(0);
		String tagValue = aElement.getFirstChild().getNodeValue();
		return tagValue;
	}

	/**
	 * Reads the Predecessors tag of the xml and returns the list of
	 * predecessors
	 * 
	 * @param datasetElement
	 * @return
	 */
	private static List<Dataset> getPredecessors(Element datasetElement) 
	{
		List<Dataset> predecessors = new ArrayList<Dataset>();

		NodeList predecessorsNodeList = datasetElement.getElementsByTagName("Predecessors");
		if (predecessorsNodeList != null&& predecessorsNodeList.getLength() > 0) 
		{
			Element predecessorsElement = (Element) predecessorsNodeList.item(0);
			NodeList predecessorNodeList = predecessorsElement.getElementsByTagName("Predecessor");
			for (int i = 0; i < predecessorNodeList.getLength(); i++) 
			{
				Element predecessorElement = (Element) predecessorNodeList.item(i);
				String predecessorName = predecessorElement.getFirstChild().getNodeValue();
				Dataset predecessorDS = getDataset(predecessorName);
				predecessors.add(predecessorDS);
			}
		}
		return predecessors;
	}

	/**
	 * Reads the Successors tag of the xml and returns the list of successors
	 * 
	 * @param datasetElement
	 * @return
	 */
	private static List<Dataset> getSuccessors(Element datasetElement) 
	{
		List<Dataset> successors = new ArrayList<Dataset>();

		NodeList successorsNodeList = datasetElement.getElementsByTagName("Successors");
		if (successorsNodeList != null && successorsNodeList.getLength() > 0)
		{
			Element successorsElement = (Element) successorsNodeList.item(0);
			NodeList successorNodeList = successorsElement.getElementsByTagName("Successor");
			for (int i = 0; i < successorNodeList.getLength(); i++)
			{
				Element successorElement = (Element) successorNodeList.item(i);
				String successorName = successorElement.getFirstChild().getNodeValue();
				Dataset successorDS = getDataset(successorName);
				successors.add(successorDS);
			}
		}
		return successors;
	}

	/**
	 * Checks if the DDG has a dataset with the given name. Returns the dataset
	 * if it exists. Else it would create a new dataset and returns it.
	 * 
	 * @param datasetName
	 * @return
	 */
	private static Dataset getDataset(String datasetName)
	{
		Dataset aDataset = graph.getDataset(datasetName);
		if (aDataset == null) 
		{
			aDataset = new Dataset(datasetName);
			graph.addDataset(aDataset);
		}
		return aDataset;
	}
}
