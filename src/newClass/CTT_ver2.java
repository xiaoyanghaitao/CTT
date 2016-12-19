package newClass;

import utilities.Dataset;

/**
 * 包含用于动态规划信息的ctt顶点
 * 
 * @author chenx
 *
 */
public class CTT_ver2 {

	private CTT_ver2 predecessor;
	private Dataset dataset;
	private int datasetNum;
	private int cloudServiceID;
	private double minGenCost; // 到达该顶点所用到的最小花销，默认为0
	public double interGenCost; //中间数据集的最小生成花销，默认为0
	private double[] verWeightForCurrentDataset; // 生成当前运算的数据集的最小生成代价

	public CTT_ver2(int cloudServiceNum) {
		this(new Dataset(), -1, -1, cloudServiceNum);
	}

	public CTT_ver2(Dataset ds, int dsno, int csid, int cloudServiceNum) {
		dataset = ds;
		predecessor = null;
		datasetNum = dsno;
		cloudServiceID = csid;
		minGenCost = 0;
		interGenCost = 0;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public CTT_ver2 getPredecessor() {
		return predecessor;
	}

	public void setPredecessor(CTT_ver2 predecessor) {
		this.predecessor = predecessor;
	}

	public int getCloudServiceID() {
		return cloudServiceID;
	}

	public int getDatasetNum() {
		return datasetNum;
	}

	public double getMinGenCost() {
		return minGenCost;
	}

	public void setMinGenCost(double minGenCost) {
		this.minGenCost = minGenCost;
	}

	public double[] getVerWeightForCurrentDataset() {
		return verWeightForCurrentDataset;
	}

	public void setVerWeightForCurrentDataset(double[] verWeightForCurrentDataset) {
		this.verWeightForCurrentDataset = verWeightForCurrentDataset;
	}

}
