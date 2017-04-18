/* In this algorithm, all the cloud services have the capacity to computation and storage
 * And based on the CTT_SPnew, I only change the  "getEdgeCost" function
 * */



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
public class CTT_SP3 {
	private static SimpleDirectedWeightedGraph<CTT_ver, DefaultWeightedEdge> cttGraph;
	public static int m=0;
	// compute cost==================================================================
	public static double computeCttCost(DataDependencyGraph graph,int cm,CloudService[] CS) 
	{//graph 是DDG图
		long startmxbe=0;
		long endmxbe=0;
		long timemxbe=0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		m=cm;
		double totalCostR=0.0;
		cttGraph = new SimpleDirectedWeightedGraph<CTT_ver, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);//use the funtion: public SimpleDirectedWeightedGraph(EdgeFactory<V,E> ef) 
		//create start and end，add them into graph
		CTT_ver startver = new CTT_ver();// start node
		CTT_ver endver = new CTT_ver(); // end node
		startver.getdataset().setName("start");
		startver.setcsid(-1);
		endver.getdataset().setName("end");
		endver.setcsid(-1);
		startver.getdataset().setcsid(-1);
		endver.getdataset().setcsid(-1);
		cttGraph.addVertex(startver);
		cttGraph.addVertex(endver);
		//set the predecessor and successor of the datasets in startver and endver******************
		Dataset firstDataset = graph.getFirstDataset();
		startver.getdataset().addSuccessor(firstDataset);
		firstDataset.addPredecessor(startver.getdataset());
		Dataset lastDataset = graph.getLastDataset();
		endver.getdataset().addPredecessor(lastDataset);
		lastDataset.addSuccessor(endver.getdataset());
		graph.addDataset(startver.getdataset());
		graph.addDataset(endver.getdataset());
		// create ctt-----------------------------------
		createCttGraph(graph.getDatasets(),startver,endver,m,CS);
        // find the shortestPath----------------------------------
		List<DefaultWeightedEdge> shortestPath = DijkstraShortestPath
				.findPathBetween(cttGraph, startver, endver);
		
		for (Dataset aDataset : graph.getDatasets()) 
		{
			if (aDataset.getPredecessors().contains(startver.getdataset()))
				aDataset.getPredecessors().remove(startver.getdataset());
			if (aDataset.getSuccessors().contains(endver.getdataset()))
				aDataset.getSuccessors().remove(endver.getdataset());
		}
		graph.removeDataset(startver.getdataset());
		graph.removeDataset(endver.getdataset());
		//compute totalCostR------------------------
	    for(DefaultWeightedEdge aEdge : shortestPath)
	    {
	    	totalCostR=totalCostR+cttGraph.getEdgeWeight(aEdge);
	    }
	    endmxbe=threadMXBean.getCurrentThreadCpuTime();
        timemxbe=endmxbe-startmxbe;
        System.out.println("the CTT_SP2 Execution time is ："+timemxbe);
	    System.out.println("the total cost of CTT_SP3 is "+totalCostR);
	    //输出相关数据
	    CTT_SP3Print.print(shortestPath,m,cttGraph,graph,CS,endver);
	    return totalCostR;
	}
	// create ctt====================================================================================
	private static void createCttGraph(List<Dataset> datasets,CTT_ver startver,CTT_ver endver,int m, CloudService[] CS) {
		
		//create  vertices  ################################
		int datasetno=datasets.size();
		CTT_ver[][] ctt_ver=new CTT_ver[datasetno][m];
		int i=0;
		int j=0;
		int k=0;
		int l=0;
		//in datasets, there are "start" and "end", datasetno is changed 
		for(i=0;i<datasetno-2;i++)
		{
			for(j=0;j<m;j++)
			{
				ctt_ver[i][j]=new CTT_ver(datasets.get(i),j);
				cttGraph.addVertex(ctt_ver[i][j]);
			}
		}
		//create edges##########################################
		for(i=0;i<datasetno-2;i++)
		{
			for(j=0;j<m;j++)
			{
				cttGraph.addEdge(startver, ctt_ver[i][j]);
				cttGraph.setEdgeWeight(
						cttGraph.getEdge(startver, ctt_ver[i][j]),
						getEdgeCost(startver, ctt_ver[i][j],CS));
			}
		}
		cttGraph.addEdge(startver, endver);
		cttGraph.setEdgeWeight(
				cttGraph.getEdge(startver, endver),
				getEdgeCost(startver, endver,CS));
		
		for(i=0;i<datasetno-3;i++)
		{
			for(j=0;j<m;j++)
			{
				for(k=i+1;k<datasetno-2;k++)
				{
					for(l=0;l<m;l++)
					{
						cttGraph.addEdge(ctt_ver[i][j], ctt_ver[k][l]);
						cttGraph.setEdgeWeight(
								cttGraph.getEdge(ctt_ver[i][j], ctt_ver[k][l]),
								getEdgeCost(ctt_ver[i][j], ctt_ver[k][l],CS));
					}
				}
			}
		}
		for(i=0;i<datasetno-2;i++)
		{
			for(j=0;j<m;j++)
			{
				cttGraph.addEdge(ctt_ver[i][j],endver);
				cttGraph.setEdgeWeight(
						cttGraph.getEdge(ctt_ver[i][j],endver),getEdgeCost(ctt_ver[i][j], endver,CS));
			}
		}
	}
	
	//compute edge cost 计算边的值
	public static double getEdgeCost(CTT_ver start_ver, CTT_ver end_ver,CloudService[] CS)
	{
		double edgeCost=0.0;//整条边的cost
		double endCost=0.0;//边的终点的cost
		double interCost=0.0;//所有中间节点的cost
		//首先计算边的终点的存储代价
		//如果边的终点为虚拟节点，则endCost为0
		if(end_ver.getcsid()==-1)
		{
			endCost=0;
		}
		else
		{
			//endCost=终点所在的cloud的costS*终点数据集的大小
			endCost=CS[end_ver.getcsid()].getcostS()*end_ver.getdataset().getSize();
		}
		//如果边的始点的后继等于终点，则说明无中间数据集；边的cost=endCost
		if(start_ver.getdataset().getSuccessors().get(0).equals(end_ver.getdataset()))
		{
			
			edgeCost=endCost;
			return edgeCost;
		}
		// 若存在中间数据集，则首先计算第一个中间数据集firstSucc的cost，再计算其他数据集的cost
		else
		{
			double[] temp=new double[m];
			double[] temp1=new double[m];
			double[] t=new double[m];
			// compute the cost of the first intermediate dataset 
			//数据集firstSucc是第一个中间数据集
			Dataset firstSucc=start_ver.getdataset().getSuccessors().get(0);
			double Minvalue=0.0;
			//若边的始点为虚拟数据集对应的节点，则始点的传输代价为0，firstSucc的cost即为它本身的产生代价
			if(start_ver.getcsid()==-1)
			{
				for(int j=0;j<m;j++)
				{
					temp[j]=CS[j].getcostC()*firstSucc.getGenerationTime();
				}
			}
			//若边的始点不为虚拟节点，则firstSucc的cost=边的始点的传输代价+firstSucc的产生代价
			else
			{
				//csid为边始点的cloud
				int csid=start_ver.getcsid();
				for(int i=0;i<m;i++)
				{
					if(i==csid)
					{
						temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime();
					}
					else
					{
						temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime()+start_ver.getdataset().getSize()*CS[csid].getcostT();
					}
				}
			}
			//Minvalue为firstSucc的一次产生的cost*firstSucc的使用频率
			Minvalue=findminvalue(temp)*firstSucc.getUsageFrequency();
			//中间节点的cost加入Minvalue
			interCost=interCost+Minvalue;
			
			Dataset currentSucc=firstSucc.getSuccessors().get(0);
			Dataset oldSucc=firstSucc;
			//如果firstSucc的后继节点不等于边的终点，则继续计算
			while(!currentSucc.getName().equals(end_ver.getdataset().getName()))
			{
				//计算currentSucc对应的m个节点的值
				for(int j=0;j<m;j++)
				{
					//计算j节点的值，从m个中选择一个最小的
					for(int k=0;k<m;k++)
					{
						//从cloud k 传输oldSucc，若k=j，则t只为currentSucc的产生时间
						if(k==j)
						{
							t[k]=temp[k]+currentSucc.getGenerationTime()*CS[j].getcostC();
						}
						//k不等于j，则需要加上oldSucc的传输代价
						else
						{
							t[k]=temp[k]+oldSucc.getSize()*CS[k].getcostT()+currentSucc.getGenerationTime()*CS[j].getcostC();
						}
					}
					//取最小值作为j节点的值
					temp1[j]=findminvalue(t);
				}
				//得到currentSucc的cost
				Minvalue=findminvalue(temp1)*currentSucc.getUsageFrequency();
				interCost=interCost+Minvalue;
				if(currentSucc.getName().equals("end"))
				{
					break;
				}
				else
				{
					oldSucc=currentSucc;
					currentSucc=currentSucc.getSuccessors().get(0);
				}
				//将temp1的值赋给temp
				temp=temp1;
			}
			//边的值=边终点的cost+中间节点的cost
		edgeCost=endCost+interCost;
		return edgeCost;
		}
	}
	// find the minimum value of a marry
	public static double findminvalue(double[] t)
	{
		double min=t[0];
		for(int i=1;i<m;i++)
		{
			if(t[i]<min)
			{
				min=t[i];
			}
		}
		return min;
	}
	// /////////////////////////////////////
	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG300.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		long startTime = System.currentTimeMillis();
		double result;
		double[] bandwiths0={0,0.128,0.128};
		double[] bandwiths1={0.128,0,0.128};
		double[] bandwiths2={0.128,0.128,0.0};
		//CloudService(double costS,double costC, double costT, int csid,double[] bandwiths)
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		CloudService CS1=new CloudService(0.06/30,0.12*24,0.03,1,bandwiths1);
		CloudService CS2=new CloudService(0.05/30,0.15*24,0.15,2,bandwiths2);//costT由0.06变成0.1
		CloudService[] CS=new CloudService[3];
		CS[0]=CS0;
		CS[1]=CS1;
		CS[2]=CS2;
		int m=3;
		result = CTT_SP3.computeCttCost(graph, m,CS);
		System.out.println("totalcost:" + result);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		System.out.println("Program is Over");
	}
}
