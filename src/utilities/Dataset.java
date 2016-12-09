package utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Java class to hold the dataset details like name, size and so on. And also
 * its successors and predecessors.
 * 
 * @author Saichander
 * 
 */
public class Dataset 
{
	private String name;
	private double size;
	private double generationTime;
	private double usageFrequency;
	private double delayTolerance;
	private double costTolerance;
	private boolean isStored;	//store or not store
	private int csid;//the id of cloud service that used to store the dataset
	private boolean isTransferCheap = false;
	private boolean isTransfered = false;
	public void setcsid(int csid)
	{
		this.csid=csid;
	}
	public int getcsid()
	{
		return csid;
	}

	public boolean isTransfered() {
		return isTransfered;
	}

	public void setTransfered(boolean isTransfered) {
		this.isTransfered = isTransfered;
	}

	public boolean isTransferCheap() {
		return isTransferCheap;
	}

	public void setTransferCheap(boolean isTransferCheap) {
		this.isTransferCheap = isTransferCheap;
	}
	
	//clear the dataset status
	public void clear()
	{
		this.isBlockDS = false;
		this.isMainBranchDS = false;
		this.isStored = false;
		this.isTransferCheap = false;
		this.costR = 0.0;
		this.csid=-1;
	}

	//=======================================
	private double costR;

	private boolean isMainBranchDS;
	private boolean isBlockDS;

	private List<Dataset> predecessors = null;
	private List<Dataset> successors = null;

	/**
	 * Default constructor
	 */
	public Dataset() 
	{
		predecessors = new ArrayList<Dataset>();
		successors = new ArrayList<Dataset>();
		isStored = false;
		isMainBranchDS = true;
		isBlockDS = false;
		costR = 0.0;
		csid=-1;
	}

	/**
	 * Constructor which takes the dataset name
	 * 
	 * @param name
	 */
	public Dataset(String name) 
	{
		this.name = name;
		predecessors = new ArrayList<Dataset>();
		successors = new ArrayList<Dataset>();
		isStored = false;
		isMainBranchDS = true;
		isBlockDS = false;
		costR = 0.0;
		csid=-1;
	}
	
//#######=======================================

	//==========================================
	
//  return costTolerance=================================================
	public double getCostTolerance() 
	{
		return costTolerance;
	}
//set  costTolerance=================================================

	public void setCostTolerance(double costTolerance) 
	{
		this.costTolerance = costTolerance;
	}

	/**
	 * Returns name of the dataset
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the datasets
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns size of the dataset
	 * 
	 * @return
	 */
	public double getSize() {
		return size;
	}

	/**
	 * Sets the size of the datasets
	 * 
	 * @param name
	 */
	public void setSize(double size) {
		this.size = size;
	}

	/**
	 * Returns generation time of the dataset
	 * 
	 * @return
	 */
	public double getGenerationTime() {
		return generationTime;
	}

	/**
	 * Sets the generation time of the datasets
	 * 
	 * @param name
	 */
	public void setGenerationTime(double generationTime) {
		this.generationTime = generationTime;
	}

	/**
	 * Returns usage frequency of the dataset
	 * 
	 * @return
	 */
	public double getUsageFrequency() {
		return usageFrequency;
	}

	/**
	 * Sets the usage frequency of the datasets
	 * 
	 * @param name
	 */
	public void setUsageFrequency(double usageFrequency) {
		this.usageFrequency = usageFrequency;
	}

	/**
	 * Returns delay tolerance of the dataset
	 * 
	 * @return
	 */
	public double getDelayTolerance() {
		return delayTolerance;
	}

	/**
	 * Sets the delay tolerance of the datasets
	 * 
	 * @param name
	 */
	public void setDelayTolerance(double delayTolerance) {
		this.delayTolerance = delayTolerance;
	}

	/**
	 * Returns list of predecessors
	 * 
	 * @return
	 */
	public List<Dataset> getPredecessors() {
		return predecessors;
	}

	/**
	 * add a predecessor
	 * 
	 * @param predecessor
	 */
	public void addPredecessor(Dataset predecessor) {
		predecessors.add(predecessor);
	}

	/**
	 * sets the list of predecessors
	 * 
	 * @param predecessors
	 */
	public void setPredecessors(List<Dataset> predecessors) {
		this.predecessors = predecessors;
	}

	/**
	 * Returns the list of successors
	 * 
	 * @return
	 */
	public List<Dataset> getSuccessors() {
		return successors;
	}

	/**
	 * add a new successor
	 * 
	 * @param successor
	 */
	public void addSuccessor(Dataset successor) {
		successors.add(successor);
	}

	/**
	 * Sets the list of successors
	 * 
	 * @param successors
	 */
	public void setSuccessors(List<Dataset> successors) {
		this.successors = successors;
	}

	/**
	 * Checks if the dataset is stored
	 * 
	 * @return
	 */
	public boolean isStored() {
		return isStored;
	}

	/**
	 * Sets the storage status of the dataset
	 * 
	 * @param isStored
	 */
	public void setStored(boolean isStored) {
		this.isStored = isStored;
	}

	/**
	 * Returns cost rate of the dataset
	 * 
	 * @return
	 */
	public double getCostR() {
		return costR;
	}

	/**
	 * Sets the cost rate of the datasets
	 * 
	 * @param name
	 */
	public void setCostR(double costR) {
		this.costR = costR;
	}
	/**
	 * Checks if the dataset is in the main branch
	 */
	public boolean isMainBranchDS() {
		return isMainBranchDS;
	}

	/**
	 * sets the status of datasets which shows whether the dataset is in main
	 * branch or not
	 * 
	 * @param isMainBranchDS
	 */
	public void setMainBranchDS(boolean isMainBranchDS) {
		this.isMainBranchDS = isMainBranchDS;
	}

	/**
	 * checks if the dataset is in the block
	 * 
	 * @return
	 */
	public boolean isBlockDS() {
		return isBlockDS;
	}

	/**
	 * Sets the status of the datasets which shows whether the dataset is in
	 * block or not
	 * 
	 * @param isBlockDS
	 */
	public void setBlockDS(boolean isBlockDS) {
		this.isBlockDS = isBlockDS;
	}
}
