package newClass;

public class CloudService {
	private double costS; //storage cost rate$ /GB*day
	private double costT;//the tranfer cost rate Spread from cloud service   $/GB
//	private int csid;
	private double costC; //compute cost rate  $/day
	private double[] bandwiths;
	public CloudService(double costS,double costC, double costT, int csid, double[] bandwiths)
	{
		this.costS=costS;
		this.costT=costT;
//		this.csid=csid;
		this.costC=costC;
		this.bandwiths=bandwiths;
	}
	
	public double getcostT()
	{
		return costT;
	}
	public double getcostS()  
	{
		return costS;
	}
    public  double getcostC()
    {
    	return costC;
    }
    public double[] getbandwiths()
    {
    	return bandwiths;
    }
}
