package strategies;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;
import newClass.*;
/*算法思想：
 * 数据集存储在本地或者在本地产生
 * */
public class CTT_SP 
{
	private static double averageGenerateTime = 0.0;
	private static WeightedGraph<Dataset, DefaultWeightedEdge> cttGraph;
	private static CloudService cloud;
	public static double getaverageGenerateTime()
	{
		return averageGenerateTime;
	}
//compute the total cost==================================================================
	public static void computeCttCost(DataDependencyGraph graph,CloudService clouds) 
	{
		long startmxbe=0;
		long endmxbe=0;
		long timemxbe=0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		averageGenerateTime = 0.0;
		double totalCostR=0.0;
		cttGraph = new SimpleDirectedWeightedGraph<Dataset, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		cloud=clouds;
		Dataset startDS = null;
		Dataset endDS = null;
	    //add startDS and endDS into cttGraph
		startDS = new Dataset("start");//start node 
		startDS.setSize(0.0);
		endDS = new Dataset("end");//end node
	    endDS.setSize(0.0);
		cttGraph.addVertex(startDS);
		cttGraph.addVertex(endDS);
//create ctt -----------------------------------
		createCttGraph(graph.getDatasets(),startDS,endDS);
//------------------------------------------	
		List<DefaultWeightedEdge> shortestPath = DijkstraShortestPath.findPathBetween(cttGraph, startDS, endDS);
		for (DefaultWeightedEdge anEdge : shortestPath)
		{
			cttGraph.getEdgeSource(anEdge).setStored(true);
			cttGraph.getEdgeTarget(anEdge).setStored(true);
	        totalCostR=totalCostR+cttGraph.getEdgeWeight(anEdge);
		}
		System.out.println("the total cost of CTT_SP is : "+totalCostR);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		timemxbe=endmxbe-startmxbe;
		System.out.println("The CTT_SP Strategy Execution Timemxbean : " + timemxbe);
		
		//输出相应的数据
		//CTT_SP.print(graph);
	}
	//create ctt====================================================================================
	private static void createCttGraph(List<Dataset> datasets,Dataset startDS,Dataset endDS)
	{
		//add all datasets into ctt	

		for (Dataset aDataset : datasets) 
		{
			cttGraph.addVertex(aDataset);	
		}
		//create all edges
		int i=0;
		int j=0;
		for (i=0;i<datasets.size()-1;i++)
		{
			for(j=i+1;j<datasets.size();j++)
			{
				cttGraph.addEdge(datasets.get(i), datasets.get(j));
				cttGraph.setEdgeWeight(
						cttGraph.getEdge(datasets.get(i), datasets.get(j)),getEdgeCost(datasets.get(i), datasets.get(j),i,j,datasets));
			}
		}
		for(i=0;i<datasets.size();i++)
		{
			cttGraph.addEdge(startDS, datasets.get(i));
			cttGraph.setEdgeWeight(
					cttGraph.getEdge(startDS, datasets.get(i)),getEdgeCost(startDS, datasets.get(i),-1,i,datasets));
		}
		cttGraph.addEdge(startDS, endDS);
		cttGraph.setEdgeWeight(
				cttGraph.getEdge(startDS, endDS),getEdgeCost(startDS, endDS,-1,datasets.size(),datasets));
		for(i=0;i<datasets.size();i++)
		{
			cttGraph.addEdge(datasets.get(i),endDS);
			cttGraph.setEdgeWeight(
					cttGraph.getEdge(datasets.get(i),endDS),getEdgeCost(datasets.get(i),endDS,i,datasets.size(),datasets));
		}
	}
//compute the edge cost=====================================================================
	private static double getEdgeCost(Dataset startDataset, Dataset endDataset,int start,int end,List<Dataset> datasets) 
	{
		double edgeCost = 0.0;
		double costS=cloud.getcostS();
		double costC=cloud.getcostC();
		double endDataset_cost=endDataset.getSize()*costS;
		int k=0;
		double allinterdsCost=0.0;
		double tempCost=0.0;
		double interdsCost=0.0;
		Dataset d;
		for(k=start+1;k<end;k++)
		{
			d=datasets.get(k);
			tempCost=tempCost+d.getGenerationTime()*costC;
			interdsCost=interdsCost+tempCost*d.getUsageFrequency();
			allinterdsCost=allinterdsCost+interdsCost;
			interdsCost=0.0;
		}
		edgeCost=endDataset_cost+allinterdsCost;
		return edgeCost;
	}
	
	public static void print(DataDependencyGraph graph)
	{
		//计算平均产生时间：获得所有数据集的平均时间=获得每个数据集的时间/数据集的个数
		double genetime = 0.0;
		double temptime=0.0;
		int count=0;
		List<Dataset> datasets=graph.getDatasets();
		for (Dataset aDataset : datasets) {
			if (!aDataset.isStored()) {
				temptime += aDataset.getGenerationTime();
				Set<Dataset> pSet = getPSet(aDataset);
				for (Dataset pSetDS : pSet) 
				{
					temptime = temptime+pSetDS.getGenerationTime();
				}
				genetime=genetime+temptime;
				temptime=0;
				count++;
			}
		}
		averageGenerateTime = genetime /graph.getDatasets().size();	
		System.out.println("the average generation time is : "+averageGenerateTime);
		System.out.println("the number of datasets generated in local is : "+count);
		System.out.println("the datasets generated in local is : ");
		for(Dataset ds:datasets)
		{
			if(!ds.isStored())
				System.out.println("the dataset generated in local is "+ds.getName());
		}
		System.out.println("the number of datasets stored in local is : "+(graph.getDatasets().size()-count));
		System.out.println("the datasets stored in local is : ");
		for(Dataset ds:datasets)
		{
			if(!ds.isStored())
				System.out.println("the dataset stored in local is : "+ds.getName());
		}
	}
	//get all datasets that are not saved before aDataset
	private static Set<Dataset> getPSet(Dataset aDataset) 
	{
		Set<Dataset> pSet = new HashSet<Dataset>();
		List<Dataset> predecessors = aDataset.getPredecessors();
		for (Dataset predecessor : predecessors) 
		{
			if (!predecessor.isStored()) 
			{
				pSet.add(predecessor);
				pSet.addAll(getPSet(predecessor));
			}
		}
		return pSet;
	}
	

	///////////////////////////////////////
	public static void main(String[] args) 
	{
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG200.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		long startTime = System.currentTimeMillis();
		double[] bandwiths0={0,0.128,0.128};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		CTT_SP.computeCttCost(graph,CS0);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime)+"ms");
		int count=0;
		for(Dataset d: graph.getDatasets())
		{
			if(d.isStored())
			{
				System.out.println("save:"+d.getName());
				count++;
			}
		}
		System.out.println("the number of save datasets："+count);
	}
}
