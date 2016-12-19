package strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import newClass.CloudService;
import utilities.DataDependencyGraph;
import utilities.Dataset;
import utilities.DatasetUsageFreqComparator;

/**
 * Implementation of the often used storage strategy for storing the
 * intermediate data storage in cloud computing.
 * 
 * @author Saichander
 * 
 */
public class OftenUsedStorageStrategy {
	private static double totalCostR = 0.0;
	private static double costS;
	private static double costC;
	private static int SELECTION_BOX = 50; // top 50%
	//change percentage========================
	public static void setSelectionBox(int SELECTION_BOX)
	{
		OftenUsedStorageStrategy.SELECTION_BOX=SELECTION_BOX;
	}
//////////////////////////////////////
	/**
	 * Computes the total system cost rate
	 * @param graph
	 * @return
	 */
	public static double computeOUSCostRate(DataDependencyGraph graph,CloudService cloud) 
	{
		costS=cloud.getcostS();
		costC=cloud.getcostC();
		totalCostR = 0.0;
		List<Dataset> datasets = graph.getDatasets();
		List<Dataset> selectedDatasets = new ArrayList<Dataset>();
		DatasetUsageFreqComparator usageComparator = new DatasetUsageFreqComparator();
		Collections.sort(datasets, usageComparator);
		int numSelectedDatasets = (datasets.size() * SELECTION_BOX) / 100;
		int count = 0;
		Iterator<Dataset> iter = datasets.iterator();
		while (iter.hasNext()) 
		{
			Dataset aDataset = iter.next();
			count++;
			if (count <= numSelectedDatasets) 
			{
				selectedDatasets.add(aDataset);
			}
		}
		computeTotalCostR(graph, selectedDatasets);
		double tempCost=totalCostR;
		totalCostR = 0.0;
		return tempCost;
	}

	/**
	 * Computes the total cost rate
	 * 
	 * @param graph
	 * @param selectedDatasets
	 */
	private static void computeTotalCostR(DataDependencyGraph graph,List<Dataset> selectedDatasets) 
	{
		double storageCost = 0.0;
		double genCost = 0.0;
		Dataset currentDS = graph.getFirstDataset();
//get the first dataset 

		if (selectedDatasets.contains(currentDS)) 
		{
			//use frequency is big £¬ store
			storageCost = currentDS.getSize() * costS;
			currentDS.setStored(true);
			currentDS.setCostR(storageCost);
			totalCostR += storageCost;
		} 
		else 
		{
			//use frequency is common£¬not store
			genCost = currentDS.getGenerationTime() * costC* currentDS.getUsageFrequency();
			currentDS.setStored(false);
			currentDS.setCostR(genCost);
			totalCostR += genCost;
		}
		computeCostROfSuccessors(currentDS, selectedDatasets);
	}

	/**
	 * Computes the cost rate of all the successors
	 * 
	 * @param aDataset
	 * @param selectedDatasets
	 */
	private static void computeCostROfSuccessors(Dataset aDataset,List<Dataset> selectedDatasets) 
	{
		List<Dataset> successors = aDataset.getSuccessors();
		for (Dataset currentDS : successors) 
		{
			if (currentDS.getCostR() == 0.0)
			{
				computeDatasetCostR(currentDS, selectedDatasets);
			}
		}

		for (Dataset currentDS : successors)
		{
			computeCostROfSuccessors(currentDS, selectedDatasets);
		}
	}

	/**
	 * Computes the cost rate of the given dataset based on whether it is in the
	 * selected datasets list
	 * 
	 * @param aDataset
	 * @param selectedDatasets
	 */
	private static void computeDatasetCostR(Dataset aDataset,List<Dataset> selectedDatasets) 
	{
		
		double storageCost = 0.0;
		double genCost = 0.0;
		if (selectedDatasets.contains(aDataset)) 
		{
			storageCost = aDataset.getSize() * costS;
			aDataset.setStored(true);
			aDataset.setCostR(storageCost);
			totalCostR += storageCost;
		} 
		else 
		{
			genCost = aDataset.getGenerationTime() * costC* aDataset.getUsageFrequency();
			Set<Dataset> pSet = getPSet(aDataset);
			//get all not save intermediate datasets before aDataset
			for (Dataset pSetDS : pSet) {
				genCost += pSetDS.getGenerationTime() * costC* aDataset.getUsageFrequency();
			}
			aDataset.setStored(false);
			aDataset.setCostR(genCost);
			totalCostR += genCost;
		}

	}

	/**
	 * Returns the set of deleted datasets between the given dataset and its
	 * stored predecessor
	 * 
	 * @param aDataset
	 * @return
	 */
	private static Set<Dataset> getPSet(Dataset aDataset) 
	{
//get all not save intermediate datasets
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
}
