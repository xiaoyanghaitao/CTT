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
	private double[] genLatterCost; // ����ĳ�����ݼ����洢��m���������Ϸֱ�Ļ���

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

	public void setGenCostInK(int k, double cost) { // ���ɱ����ڷ�����k�ϵĵ�ǰ���ݼ��ķ���
		genLatterCost[k] = cost;
	}

	public double getGenCostInK(int k) {
		return genLatterCost[k];
	}
}
