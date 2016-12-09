package newClass;

import utilities.Dataset;

public class CTT_ver {
	private double costS;// storage cost rate $/GB*day,
	private double costT; //transfer cost rate
    private Dataset dataset=new Dataset();
    private int csid;// the number of cloud service that the dataset saved in  
	public CTT_ver(double costS, double costT, Dataset dataset,int csid)
	{
		this.dataset=dataset;
		this.costS=costS;
		this.costT=costT;
		this.csid=csid;
	}
	public CTT_ver(Dataset dataset,int csid)
	{
		this.dataset=dataset;
		this.csid=csid;
		
	}
	public CTT_ver()
	{
		this.dataset=new Dataset();
		this.costS=0.0;
		this.costT=0.0;
	}
	public double getCostS() {
		return costS;
	}
	public  double getCostT() {
		return costT;
	}
	public Dataset getdataset() {
		return dataset;
	}
	public int getcsid()
	{
		return csid;
	}
	public void setcsid(int csid)
	{
		this.csid=csid;
	}
}
