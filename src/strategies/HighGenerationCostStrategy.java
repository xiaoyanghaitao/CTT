package strategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import newClass.*;

import utilities.DataDependencyGraph;
import utilities.Dataset;

/**
 * Implementation of the High Generation Cost storage strategy for storing the
 * intermediate data storage in cloud computing.
 * 
 * @author Saichander
 * 
 */
public class HighGenerationCostStrategy 
{
	private static double totalCostR = 0.0;
	private static double costS ;
	private static double costC;
	private static  int SELECTION_BOX = 50; // top 50%
//change the percentage========================
	public static void setSelectionBox(int SELECTION_BOX)
	{
		HighGenerationCostStrategy.SELECTION_BOX=SELECTION_BOX;
	}
//////////////////////////////////////
	/**
	 * Computes the total system cost rate
	 * 
	 * @param graph
	 * @return
	 */
	public static double computeHGCSCostRate(DataDependencyGraph graph,CloudService cloud) 
	{
		costS=cloud.getcostS();
		costC=cloud.getcostC();
		totalCostR=0.0;
		List<Dataset> datasets = graph.getDatasets();
		Map<Dataset, Double> datasetGenCostMap = computeGenerationCosts(datasets);
		datasetGenCostMap = sortByGenCost(datasetGenCostMap);
		int numSelectedDatasets = (datasets.size() * SELECTION_BOX) / 100;
		List<Dataset> selectedDatasets = new ArrayList<Dataset>();
		int count = 0;
		Iterator<Dataset> iter = datasetGenCostMap.keySet().iterator();
		while (iter.hasNext()) {
			Dataset aDataset = iter.next();
			count++;
			if (count <= numSelectedDatasets) {
				selectedDatasets.add(aDataset);
			}
		}
		computeTotalCostR(graph, selectedDatasets);
		double temptotalCostR=totalCostR;
		totalCostR=0.0;
		return temptotalCostR;
	}

	

	/**
	 * Computes the total cost rate
	 * 
	 * @param graph
	 * @param selectedDatasets
	 */
	private static void computeTotalCostR(DataDependencyGraph graph,
			List<Dataset> selectedDatasets) {
		double storageCost = 0.0;
		double genCost = 0.0;
		Dataset currentDS = graph.getFirstDataset();

		if (selectedDatasets.contains(currentDS)) {
			storageCost = currentDS.getSize() * costS;
			currentDS.setStored(true);
			currentDS.setCostR(storageCost);
			totalCostR += storageCost;
		} else {
			genCost = currentDS.getGenerationTime() * costC
					* currentDS.getUsageFrequency();
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
	private static void computeCostROfSuccessors(Dataset aDataset,
			List<Dataset> selectedDatasets) {
		List<Dataset> successors = aDataset.getSuccessors();
		for (Dataset currentDS : successors) {
			if (currentDS.getCostR() == 0.0)
				computeDatasetCostR(currentDS, selectedDatasets);
		}

		for (Dataset currentDS : successors) {
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
	private static void computeDatasetCostR(Dataset aDataset,
			List<Dataset> selectedDatasets) {
		double storageCost = 0.0;
		double genCost = 0.0;
		if (selectedDatasets.contains(aDataset)) {
			storageCost = aDataset.getSize() * costS;
			aDataset.setStored(true);
			aDataset.setCostR(storageCost);
			totalCostR += storageCost;
		} else {
			genCost = aDataset.getGenerationTime() * costC
					* aDataset.getUsageFrequency();
			Set<Dataset> pSet = getPSet(aDataset);
			for (Dataset pSetDS : pSet) {
				genCost += pSetDS.getGenerationTime() * costC
						* aDataset.getUsageFrequency();
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
	private static Set<Dataset> getPSet(Dataset aDataset) {
		Set<Dataset> pSet = new HashSet<Dataset>();
		List<Dataset> predecessors = aDataset.getPredecessors();
		for (Dataset predecessor : predecessors) {
			if (!predecessor.isStored()) {
				pSet.add(predecessor);
				pSet.addAll(getPSet(predecessor));
			}
		}
		return pSet;
	}

	/**
	 * Computes the generation cost for each dataset in the list of datasets
	 * 
	 * @param datasets
	 * @return
	 */
	private static Map<Dataset, Double> computeGenerationCosts(
			List<Dataset> datasets) {
		Map<Dataset, Double> datasetGenCostMap = new HashMap<Dataset, Double>();

		for (Dataset aDataset : datasets) {
			double genCost = aDataset.getGenerationTime() * costC;
			datasetGenCostMap.put(aDataset, new Double(genCost));
		}
		return datasetGenCostMap;
	}

	/**
	 * Returns the dataset to generation cost map in the decreasing order of
	 * generation costs
	 * 
	 * @param map
	 * @return
	 */
	private static Map<Dataset, Double> sortByGenCost(Map<Dataset, Double> map) {
		List<Map.Entry<Dataset, Double>> list = new LinkedList<Map.Entry<Dataset, Double>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Dataset, Double>>() {
			public int compare(Map.Entry<Dataset, Double> o1,
					Map.Entry<Dataset, Double> o2) {
				return ((Comparable<Double>) ((Map.Entry<Dataset, Double>) (o2))
						.getValue())
						.compareTo((Double) ((Map.Entry<Dataset, Double>) (o1))
								.getValue());
			}
		});

		Map<Dataset, Double> result = new LinkedHashMap<Dataset, Double>();
		Iterator<Map.Entry<Dataset, Double>> it = list.iterator();
		while (it.hasNext()) {
			Map.Entry<Dataset, Double> entry = (Map.Entry<Dataset, Double>) it
					.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
