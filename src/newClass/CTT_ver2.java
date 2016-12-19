package newClass;

import utilities.Dataset;

/**
 * �������ڶ�̬�滮��Ϣ��ctt����
 * 
 * @author chenx
 *
 */
public class CTT_ver2 {

	private CTT_ver2 predecessor;
	private Dataset dataset;
	private int datasetNum;
	private int cloudServiceID;
	private double minGenCost; // ����ö������õ�����С������Ĭ��Ϊ0
	public double interGenCost; //�м����ݼ�����С���ɻ�����Ĭ��Ϊ0
	private double[] verWeightForCurrentDataset; // ���ɵ�ǰ��������ݼ�����С���ɴ���

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
