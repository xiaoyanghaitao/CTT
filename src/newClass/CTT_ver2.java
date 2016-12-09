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
	private double[] genLatterCost; // 生成某个数据集并存储到m个服务商上分别的花费

	public CTT_ver2(int cloudServiceNum) {
		this(new Dataset(), -1, -1, cloudServiceNum);
	}

	public CTT_ver2(Dataset ds, int dsno, int csid, int cloudServiceNum) {
		dataset = ds;
		predecessor = null;
		datasetNum = dsno;
		cloudServiceID = csid;
		minGenCost = 0;
		genLatterCost = new double[cloudServiceNum];
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

	public double[] getGenLatterCost() {
		return genLatterCost;
	}

	public void setGenCostInK(int k, double cost) { // 生成保存在服务商k上的当前数据集的费用
		genLatterCost[k] = cost;
	}

	public double getGenCostInK(int k) {
		return genLatterCost[k];
	}
}
