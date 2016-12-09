package strategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import newClass.*;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

/**
 * compute the cost of all delete
 */
public class AllDelete 
{
	private static double costC;

	
/**
 * compute the minimum cost
 * 
 */
	public static double computeAllDeleteCost(DataDependencyGraph graph,CloudService cloud) 
	{
		costC=cloud.getcostC();
		double totalCostR = 0.0;
		List<Dataset> datasets = graph.getDatasets();
		for (Dataset aDataset : datasets) 
		{
				aDataset.setStored(false);
		}
		for (Dataset aDataset : datasets)
		{
			totalCostR += computeDatasetCostR(aDataset);
		}
		return totalCostR;
	}

//compute the cost of each dataset=========================================================
	private static double computeDatasetCostR(Dataset aDataset) 
	{
		double genCost = 0.0;
		double costR = 0.0;
		
		genCost = aDataset.getGenerationTime() * costC* aDataset.getUsageFrequency();
		Set<Dataset> pSet = getPSet(aDataset);
		for (Dataset pSetDS : pSet) 
		{
			genCost += pSetDS.getGenerationTime() * costC* aDataset.getUsageFrequency();
		}
		aDataset.setCostR(genCost);
		costR = genCost;
		
		return costR;
	}


//get all datasets that are not saved before aDataset=============================================
	private static Set<Dataset> getPSet(Dataset aDataset) 
	{
		Set<Dataset> pSet = new HashSet<Dataset>();
		List<Dataset> predecessors = aDataset.getPredecessors();
		for (Dataset predecessor : predecessors) 
		{
			if (!predecessor.isStored()) 
			{
				pSet.add(predecessor);
				pSet.addAll(getPSet(predecessor));
			}
		}
		return pSet;
	}
///////////////////////////////////////////////////////////////
	public static void main(String []args)
	{
		DDGGenerator.setFilePath("xmlFolder/LineXML/elineDDG200.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		
	    long startTime = 0;
		long endTime = 0;
		startTime = System.currentTimeMillis();
		double[] bandwiths0={0,0.128,0.128};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
        System.out.println("Total Cost Rate for All delete Strategy: "+AllDelete.computeAllDeleteCost(graph,CS0));
		endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime));

	}
}
