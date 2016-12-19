package utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java class which holds the datasets and their dependencies
 * 
 * @author Saichander
 * 
 */
public class DataDependencyGraph 
{
	private List<Dataset> datasets;

	private Set<Dataset> mainBranchDatasets = null;//得到编号较小的分支上的数据集和所有主干上的数据集
	private Set<Dataset> blockDatasets = null;//得到block块中所有的数据集
	private Dataset branchDS = null;
	private Dataset mergeDS = null;
	
	public void modifyTolerance(double t, double c)
	{
		System.out.println("datasets: "+datasets.size() + "  time:"+t+"  cost:"+c);
		for(Dataset d : datasets)
		{
			if(t>0)
				d.setDelayTolerance( t * d.getGenerationTime() );
				
			d.setCostTolerance(c);
		}
	}

	/**
	 * Default constructor
	 */
	public DataDependencyGraph() 
	{
		datasets = new ArrayList<Dataset>();
		mainBranchDatasets = new HashSet<Dataset>();
	}
	public DataDependencyGraph(ArrayList<Dataset> datasets) 
	{
		this.datasets = datasets;
		mainBranchDatasets = new HashSet<Dataset>();
	}


	/**
	 * initializes the graph by generating the datasets list for block, main
	 * branch and the branch and merge datasets in case of a single block DDG
	 */
	public void initialize() 
	{
		getBlockDatasets();
		getMainBranch();
		getBranchDS();
		getMergeDS();
	}

	/**
	 * Returns the list of datasets
	 * 
	 * @return
	 */
	public List<Dataset> getDatasets() 
	{
		return datasets;
	}

	/**
	 * returns the dataset with the given name
	 * 
	 * @param datasetName
	 * @return
	 */
	public Dataset getDataset(String datasetName) 
	{
		Dataset result = null;
		for (Dataset aDataset : datasets) 
		{
			if (aDataset.getName().equals(datasetName)) 
			{
				result = aDataset;
				break;
			}
		}
		return result;
	}

	/**
	 * Add dataset to the graph.
	 * 
	 * @param aDataset
	 */
	public void addDataset(Dataset aDataset) 
	{
		datasets.add(aDataset);
	}

	/**
	 * remove dataset from the graph
	 * 
	 * @param aDataset
	 */
	public void removeDataset(Dataset aDataset) 
	{
		datasets.remove(aDataset);
	}

	/**
	 * Returns the first dataset in the graph
	 *
	 * @return
	 */
	public Dataset getFirstDataset() 
	{
		//get the first dataset
		Dataset result = null;
		for (Dataset aDataset : datasets) 
		{
			if (aDataset.getPredecessors().size() == 0) 
			{
				result = aDataset;
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the final dataset in the graph
	 * 
	 * @return
	 */
	public Dataset getLastDataset() 
	{
		Dataset result = null;
		for (Dataset aDataset : datasets) 
		{
			if (aDataset.getSuccessors().size() == 0)
			{
				result = aDataset;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if the graph has a block
	 * 
	 * @return
	 */
	public boolean hasBlock() 
	{
		boolean result = false;
		for (Dataset aDataset : datasets) 
		{
			if (aDataset.getSuccessors().size() > 1) 
			{
				branchDS = aDataset;
				result = true;
			}
			else if (aDataset.getPredecessors().size() > 1)
			{
				result = true;
				mergeDS = aDataset;
			}
		}
		return result;
	}

	/**
	 * Returns the list of datasets in the block
	 * 
	 * @return
	 */
	public Set<Dataset> getBlockDatasets()
	{
		blockDatasets = getBlockSuccessors(branchDS, mergeDS);
		for (Dataset aDataset : blockDatasets) 
		{
			aDataset.setBlockDS(true);
		}

		return blockDatasets;
	}

	/**
	 * Returns the list of datasets between the branch and merge datasets
	 * 
	 * @param branchDS
	 * @param mergeDS
	 * @return
	 */
	private Set<Dataset> getBlockSuccessors(Dataset branchDS, Dataset mergeDS) 
	{
		Set<Dataset> blockDatasets = new HashSet<Dataset>();
		if (branchDS == null)
			branchDS = getBranchDS();
		if (mergeDS == null)
			mergeDS = getMergeDS();
		List<Dataset> successors = branchDS.getSuccessors();
		for (Dataset aDataset : successors)
		{
			if (!aDataset.getName().equals(mergeDS.getName()))
			{
				blockDatasets.add(aDataset);
				blockDatasets.addAll(getBlockSuccessors(aDataset, mergeDS));
			}
		}
		return blockDatasets;
	}

	/**
	 * Returns the set of datasets in the main branch of the DDG
	 * 
	 * @return
	 */
	public Set<Dataset> getMainBranch() 
	{
		if (blockDatasets == null)
			blockDatasets = getBlockDatasets();
		if (!this.hasBlock()) 
		{
			mainBranchDatasets.addAll(datasets);
		}
		else 
		{
			mainBranchDatasets.addAll(selectBlockBranch());
			for (Dataset aDataset : datasets)
			{
				if (!blockDatasets.contains(aDataset))
					mainBranchDatasets.add(aDataset);
			}
		}

		return mainBranchDatasets;
	}

	/**
	 * Selecs one of the branches in the block returns the set of datasets in
	 * the branch
	 * 
	 * @return
	 */
	private Set<Dataset> selectBlockBranch()
	{
		Set<Dataset> selectedBlockBranch = new HashSet<Dataset>();

		List<Dataset> branchSuccessors = branchDS.getSuccessors();
		Dataset selectedDS = branchSuccessors.get(0);
		selectedBlockBranch.add(selectedDS);
		selectedBlockBranch.addAll(getBlockSuccessors(selectedDS, mergeDS));

		for (Dataset aDataset : blockDatasets) 
		{
			if (!selectedBlockBranch.contains(aDataset))
				aDataset.setMainBranchDS(false);
		}

		return selectedBlockBranch;
	}

	/**
	 * Returns the set of datasets in the sub branch of the block
	 * 
	 * @return
	 */
	public Set<Dataset> getSubBranch() 
	{
		Set<Dataset> subBranchDatasets = new HashSet<Dataset>();
		if (mainBranchDatasets == null || mainBranchDatasets.size() == 0) 
		{
			mainBranchDatasets = getMainBranch();
		}

		for (Dataset aDataset : blockDatasets) 
		{
			if (!mainBranchDatasets.contains(aDataset))
				subBranchDatasets.add(aDataset);
		}
		return subBranchDatasets;
	}

	/**
	 * Returns the branch dataset for the block
	 * 
	 * @return
	 */
	public Dataset getBranchDS() 
	{
		if (branchDS == null) 
		{
			for (Dataset aDataset : datasets) 
			{
				if (aDataset.getSuccessors().size() > 1)
					branchDS = aDataset;
			}
		}
		return branchDS;
	}

	/**
	 * Returns the merge dataset for the block
	 * 
	 * @return
	 */
	public Dataset getMergeDS() 
	{
		if (mergeDS == null)
		{
			for (Dataset aDataset : datasets) 
			{
				if (aDataset.getPredecessors().size() > 1)
					mergeDS = aDataset;
			}
		}
		return mergeDS;
	}

}
