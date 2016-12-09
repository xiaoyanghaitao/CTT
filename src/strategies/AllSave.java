package strategies;

import java.util.List;

import newClass.*;

import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

public class AllSave 
{
	private static double costS;
	/**
	 * compute the cost of all save
	 */
	public static double computeAllSaveCost(DataDependencyGraph graph,CloudService cloud) 
	{
		costS=cloud.getcostS();
		double totalCostR=0.0;
		List<Dataset> datasets = graph.getDatasets();
		for (Dataset aDataset : datasets) 
		{
				
			aDataset.setStored(true);
		}
		for (Dataset aDataset : datasets) 
		{	
			totalCostR += aDataset.getSize()*costS;
		}
		return totalCostR;
	}
	
//////////////////////////////////////////////////
	public static void main(String[] args) 
	{
		DDGGenerator.setFilePath("xmlFolder/LineXML/elineDDG200.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		
		long startTime = 0;
		long endTime = 0;
		startTime = System.currentTimeMillis();
		double[] bandwiths0={0,0.128,0.128};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		CloudService[] CS=new CloudService[3];
		CS[0]=CS0;
		System.out.println("Total Cost Rate for All save Strategy: "+AllSave.computeAllSaveCost(graph,CS0));
		endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime));

	}

}
