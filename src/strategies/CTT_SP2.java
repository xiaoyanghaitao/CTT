package strategies;

import newClass.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

/**
 * 
 * m-th cloud services
 */
public class CTT_SP2 {
	private static double storeCost=0.0;
	private static double computeCost=0.0;
	private static SimpleDirectedWeightedGraph<CTT_ver, DefaultWeightedEdge> cttGraph;
	public static double getstoreCost()
	{
		return storeCost;
	}
	public static double getcomputeCost()
	{
		return computeCost;
	}
	// compute cost==================================================================
	public static void computeCttCost(DataDependencyGraph graph,int m,CloudService[] CS) {//graph ÊÇDDGÍ¼
		long startmxbe=0;
		long endmxbe=0;
		long timemxbe=0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		double totalCostR=0.0;
		cttGraph = new SimpleDirectedWeightedGraph<CTT_ver, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		//create start and end£¬add them into graph
		CTT_ver startver = null;
		CTT_ver endver = null;

		startver = new CTT_ver();// start node 
		endver = new CTT_ver();// end node
		startver.getdataset().setName("start");
		endver.getdataset().setName("end");
		cttGraph.addVertex(startver);
		cttGraph.addVertex(endver);
		
		// create ctt-----------------------------------
		createCttGraph(graph.getDatasets(),startver,endver,m,CS);
        // find the shortestPath----------------------------------
		List<DefaultWeightedEdge> shortestPath = DijkstraShortestPath
				.findPathBetween(cttGraph, startver, endver);
		//compute totalCostR------------------------
	    for(DefaultWeightedEdge anEdge : shortestPath)
	    {
	    	
	    	totalCostR=totalCostR+cttGraph.getEdgeWeight(anEdge);
	    }
	    endmxbe=threadMXBean.getCurrentThreadCpuTime();
        timemxbe=endmxbe-startmxbe;
        System.out.println("the CTT_SP2 Execution time is £º"+timemxbe);
        System.out.println("the total cost of  CTT_SP2 Strategy is : " +totalCostR);
        CTT_SP2Print.print(shortestPath, m,cttGraph,CS,graph);

	}

	// create ctt====================================================================================
	private static void createCttGraph(List<Dataset> datasets,CTT_ver startver,CTT_ver endver,int m,CloudService[] CS) {
		
		//create  vertices  ################################
		int datasetno=datasets.size();
		CTT_ver[][] ctt_ver=new CTT_ver[datasetno][m];
		int i=0;
		int j=0;
		int k=0;
		int l=0;
		for(i=0;i<datasetno;i++)
		{
			for(j=0;j<m;j++)
			{
				ctt_ver[i][j]=new CTT_ver(CS[j].getcostS(),CS[j].getcostT(),datasets.get(i),j);
				cttGraph.addVertex(ctt_ver[i][j]);
			}
		}
		//create edges##########################################
		for(i=0;i<datasetno;i++)
		{
			for(j=0;j<m;j++)
			{
				cttGraph.addEdge(startver, ctt_ver[i][j]);
				cttGraph.setEdgeWeight(
						cttGraph.getEdge(startver, ctt_ver[i][j]),
						getEdgeCost(startver, ctt_ver[i][j],-1,i, datasets,CS[0].getcostC()));
			}
		}
		
		cttGraph.addEdge(startver, endver);
		cttGraph.setEdgeWeight(
				cttGraph.getEdge(startver, endver),
				getEdgeCost(startver, endver,-1,datasetno, datasets,CS[0].getcostC()));
		
		for(i=0;i<datasetno-1;i++)
		{
			for(j=0;j<m;j++)
			{
				for(k=i+1;k<datasetno;k++)
				{
					for(l=0;l<m;l++)
					{
						cttGraph.addEdge(ctt_ver[i][j], ctt_ver[k][l]);
						cttGraph.setEdgeWeight(
								cttGraph.getEdge(ctt_ver[i][j], ctt_ver[k][l]),
								getEdgeCost(ctt_ver[i][j], ctt_ver[k][l], i, k, datasets,CS[0].getcostC()));
					}
				}
			}
		}
		for(i=0;i<datasetno;i++)
		{
			for(j=0;j<m;j++)
			{
				cttGraph.addEdge(ctt_ver[i][j],endver);
				cttGraph.setEdgeWeight(
						cttGraph.getEdge(ctt_ver[i][j],endver),getEdgeCost(ctt_ver[i][j], endver, i, datasetno, datasets,CS[0].getcostC()));
			}
		}
	}
	
	//compute edge cost
	private static double getEdgeCost(CTT_ver start_ver, CTT_ver end_ver,int start,int end,List<Dataset> datasets,double costC)
	//start is the index of  start_ver dataset in datasets;end is the index of end_ver dataset in datasets(it is a List);if start_ver is start node (startver)£¬its index is -1£»if end_ver is end node (endver), the index is datasetno
	{
		double edgeCost=0.0;
		Dataset d;
		d=end_ver.getdataset();
		double endCost=end_ver.getCostT()*d.getSize()*d.getUsageFrequency()+end_ver.getCostS()*d.getSize();
		double alldeverCost=0.0;
		double start_verCostT=start_ver.getCostT()*start_ver.getdataset().getSize();
		double tempCost=start_verCostT;
		double interverCost=0.0;
		int p=0;
		for(p=start+1;p<end;p++)
		{
		    d=datasets.get(p);
			tempCost=tempCost+d.getGenerationTime()*costC;
			interverCost=tempCost*d.getUsageFrequency();
			alldeverCost=alldeverCost+interverCost;
			interverCost=0.0;
		}
		edgeCost=endCost+alldeverCost;
		return edgeCost;
	}

	// /////////////////////////////////////
	public static void main(String[] args) {
		DDGGenerator.setFilePath("G:/ctt/local optimal v4/xmlFolder/LineXML/testlineDDG50.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		double[] bandwiths0={0,0.128,0.128};
		double[] bandwiths1={0.128,0,0.128};
		double[] bandwiths2={0.128,0.128,0.0};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		CloudService CS1=new CloudService(0.05/30,0.15*24,0.15,1,bandwiths1);
		CloudService CS2=new CloudService(0.06/30,0.12*24,0.03,2,bandwiths2);
		CloudService[] CS=new CloudService[3];
		CS[0]=CS0;
		CS[1]=CS1;
		CS[2]=CS2;
		int m=3;
		CTT_SP2.computeCttCost(graph, m,CS);
		int count = 0;
		for (Dataset d : graph.getDatasets()) {
			if (d.isStored()) {
				count = count + 1;
			}
		}
		System.out.println("the number of save datasets£º  " + count);
		System.out.println("Program is Over");
	}
}
