package strategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import newClass.*;

import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

/**
 * Implementation of the Data Dependency strategy for storing the intermediate
 * data storage in cloud computing.
 * 
 * @author Saichander
 * 
 */
public class DataDependencyStrategy 
{

	private static double totalCostR = 0.0;
	private static double costS;
	private static double costC;

	/**
	 * Computes the total system cost rate
	 * 
	 * @param graph
	 * @return
	 */
	public static double computeDDSCostRate(DataDependencyGraph graph,CloudService cloud)
	{
		costS=cloud.getcostS();
		costC=cloud.getcostC();
		double currentDSGenCost = 0.0;
		double currentDSStorageCost = 0.0;
		totalCostR=0.0;

		Dataset currentDS = graph.getFirstDataset();

		currentDSStorageCost = currentDS.getSize() * costS;
		currentDSGenCost = currentDS.getGenerationTime() * costC;

		setDatasetCostR(currentDS, currentDSGenCost, currentDSStorageCost);
		computeCostROfSuccessors(currentDS);

		totalCostR = computeTotalCostR(graph);
		double tempCost=totalCostR;
		totalCostR=0.0;
		return tempCost;

	}

	/**
	 * Computees the cost rate of the successors of the given dataset
	 * 
	 * @param aDataset
	 */
	private static void computeCostROfSuccessors(Dataset aDataset) {
		List<Dataset> successors = aDataset.getSuccessors();
		for (Dataset currentDS : successors) {
			if (currentDS.getCostR() == 0.0)
				computeDatasetCostR(currentDS);
		}

		for (Dataset currentDS : successors) {
			computeCostROfSuccessors(currentDS);
		}
	}

	/**
	 * Computes the cost rate of the given dataset
	 * 
	 * @param aDataset
	 */
	private static void computeDatasetCostR(Dataset aDataset) {
		double currentDSGenCost = 0.0;
		double currentDSStorageCost = 0.0;
		currentDSStorageCost = aDataset.getSize() * costS;
		Set<Dataset> pSet = getPSet(aDataset);
		currentDSGenCost = aDataset.getGenerationTime() * costC;
		for (Dataset pSetDS : pSet) {
			currentDSGenCost += pSetDS.getGenerationTime() * costC;
		}
		setDatasetCostR(aDataset, currentDSGenCost, currentDSStorageCost);
	}

	/**
	 * Sets the cost rate and storage status of the dataset based on the storage
	 * and generation costs
	 * 
	 * @param currentDS
	 * @param currentDSGenCost
	 * @param currentDSStorageCost
	 */
	private static void setDatasetCostR(Dataset currentDS,
			double currentDSGenCost, double currentDSStorageCost) 
	{
		if ((currentDSGenCost * currentDS.getUsageFrequency()) > currentDSStorageCost)
		{
			currentDS.setStored(true);
			currentDS.setCostR(currentDSStorageCost);
		} else {
			currentDS.setStored(false);
			currentDS
					.setCostR(currentDSGenCost * currentDS.getUsageFrequency());
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
	 * Computes the total cost rate for the given DDG
	 * @param graph
	 * @return
	 */
	private static double computeTotalCostR(DataDependencyGraph graph) {
		double totalCostR = 0.0;
		List<Dataset> datasets = graph.getDatasets();
		for (Dataset aDataset : datasets) {
			totalCostR += aDataset.getCostR();
		}
		return totalCostR;
	}
	///////////////////////////////////////////////////
	public static void main(String []args)
	{
		DDGGenerator.setFilePath("xmlFolder/LineXML/elineDDG200.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		System.out.println("============== data dependence strategy================");
		long startTime = System.currentTimeMillis();
		double[] bandwiths0={0,0.128,0.128};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		double cost=DataDependencyStrategy.computeDDSCostRate(graph,CS0);
		System.out.println("Total Cost Rate for Data Dependency Strategy: "+cost);
		long endTime = System.currentTimeMillis();
		long time=endTime - startTime;
		System.out.println("Execution Time : " + time);
	}
}
