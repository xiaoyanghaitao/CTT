package strategies;

import java.util.ArrayList;
import java.util.List;

import newClass.CTT_ver;
import newClass.CloudService;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import utilities.DataDependencyGraph;
import utilities.Dataset;

public class CTT_SP3Print {

	/**
	 * 输出：
	 * 1.存储在各个cloud上的数据集的个数及具体的数据集
	 * 2.各个cloud上产生的数据集的个数及具体的数据集
	 * 3.输出平均产生时间
	 * 3.输出传输cost、存储cost、计算cost
	 * 
	 */
	private static int minvalueno=0;
	private static int m=0;
	private static double averageGenerateTime = 0.0;
	public static void print(List<DefaultWeightedEdge> shortestPath,int cm,SimpleDirectedWeightedGraph<CTT_ver, DefaultWeightedEdge> cttGraph,DataDependencyGraph graph,CloudService[] CS,CTT_ver endver)
	{
		m=cm;
		// 在确定最短路径后，输出相关数据--------------
		int[] geneDSnum=new int[m];
		int[] storeDSnum=new int[m];
		ArrayList<ArrayList<String>> storeDS=new ArrayList<ArrayList<String>>();
	    ArrayList<ArrayList<String>> geneDS=new ArrayList<ArrayList<String>>();
		for(int i=0;i<m;i++)
		{
		    storeDS.add(new ArrayList<String>());
		    geneDS.add(new ArrayList<String>());
		}
		// cost的组成：存储cost，传输cost，计算cost
		double storecost=0.0;
		double transcost=0.0;
		double computecost=0.0;
		//产生时间
		double genetime=0.0;
	    CTT_ver ver1;
		CTT_ver ver2;
		//最短路径包含两个虚拟节点
		for (DefaultWeightedEdge anEdge : shortestPath) {
			ver1 = cttGraph.getEdgeSource(anEdge);
			ver2=cttGraph.getEdgeTarget(anEdge);
			if(!ver1.getdataset().getName().equals("start"))//如果ver1为初始虚拟数据集，则不需要计算其存储cost，也不计入存储序列中
			{
				//相应云服务上存储数据集个数加1，并加入云服务对应的存储序列中
				storeDSnum[ver1.getcsid()]++;
				storeDS.get(ver1.getcsid()).add(ver1.getdataset().getName());
				//计算存储数据集的存储代价
				storecost=storecost+CS[ver1.getcsid()].getcostS()*ver1.getdataset().getSize();//每次只加源节点的存储cost即可
			}
			//判断ver1是否为最后一个数据集，如果是的话，则说明ver1的后继为虚拟节点endver
			Dataset DS;
			if(ver1.getdataset().getName().equals(graph.getLastDataset().getName()))
			{
				DS=endver.getdataset();
			}
			else
			{
				DS=ver1.getdataset().getSuccessors().get(0);
			}
			if(!DS.equals(ver2.getdataset()))//若该判断式为true，则说明两个数据不同，即存在中间产生的数据集
			{
				// 统计第一个中间数据集，即ver1的第一个后继的相关输出数据
				Dataset firstSucc=ver1.getdataset().getSuccessors().get(0);
				double[] temp=new double[m];
				double[] temp1=new double[m];
				double[] t=new double[m];
				//tempcost[i][0]为传输cost，tempcost[i][1]为计算cost，tempcost[i][2]为得到数据集所需时间=传输时间+生成时间
				double[][] tempcost=new double[m][3];
				double[][] tempcostt=new double[m][3];
				double[][] tempcost1=new double[m][3];
				if(ver1.getdataset().getName().equals("start"))//如果ver1是初始虚拟数据集，则传输的cost为0
				{
					for(int i=0;i<m;i++)
					{
						temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime();//一次的计算代价
						tempcost[i][0]=0;
						tempcost[i][1]=temp[i];
						tempcost[i][2]=firstSucc.getGenerationTime();
					}
				}
				else
				{
					for(int i=0;i<m;i++)
					{
						if(i==ver1.getcsid())//若在相同的cloud上，则传输cost为0
						{
							temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime();
							tempcost[i][0]=0;
							tempcost[i][1]=temp[i];
							tempcost[i][2]=firstSucc.getGenerationTime();
						}
						else
						{
							temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime()+ver1.getdataset().getSize()*CS[ver1.getcsid()].getcostT();
							tempcost[i][0]=ver1.getdataset().getSize()*CS[ver1.getcsid()].getcostT();
							tempcost[i][1]=CS[i].getcostC()*firstSucc.getGenerationTime();
							tempcost[i][2]=firstSucc.getGenerationTime()
											+(ver1.getdataset().getSize()/CS[ver1.getcsid()].getbandwiths()[i])/(60*24*60*1.0);
						}
					}
				}
				//minvalueno为firstSucc的产生cloud
				//统计产生数据集的信息
				findmin(temp);
				geneDSnum[minvalueno]++;
				geneDS.get(minvalueno).add(firstSucc.getName());
				transcost=transcost+tempcost[minvalueno][0]*firstSucc.getUsageFrequency();
				computecost=computecost+tempcost[minvalueno][1]*firstSucc.getUsageFrequency();
				genetime=genetime+tempcost[minvalueno][2];			
			    //当前的数据集为第一个中间数据集的后继
				Dataset currentSucc;
				if(firstSucc.equals(graph.getLastDataset()))
				{
					currentSucc=endver.getdataset();
				}
				else
				{
					currentSucc=firstSucc.getSuccessors().get(0);
				}
				Dataset oldSucc=firstSucc;
				while(!currentSucc.getName().equals(ver2.getdataset().getName()))//若currentSucc等于ver2，则判断是为false，跳出循环
				{			
					for(int i=0;i<m;i++)
					{
						for(int j=0;j<m;j++)
						{
							//产生currentSucc一次的代价,从j向i传，并加上在i上的计算代价
							if(j==i)
							{
								t[j]=temp[j]+currentSucc.getGenerationTime()*CS[i].getcostC();
								tempcostt[j][0]=tempcost[j][0]+0;
								tempcostt[j][1]=tempcost[j][1]+currentSucc.getGenerationTime()*CS[i].getcostC();
								tempcostt[j][2]=tempcost[j][2]+currentSucc.getGenerationTime();
							}
							else
							{
								t[j]=temp[j]+oldSucc.getSize()*CS[j].getcostT()+currentSucc.getGenerationTime()*CS[i].getcostC();
								tempcostt[j][0]=tempcost[j][0]+oldSucc.getSize()*CS[j].getcostT();
								tempcostt[j][1]=tempcost[j][1]+currentSucc.getGenerationTime()*CS[i].getcostC();
								tempcostt[j][2]=tempcost[j][2]+currentSucc.getGenerationTime()
												+(oldSucc.getSize()/CS[j].getbandwiths()[i])/(60*24*60*1.0);
							}
						}
						temp1[i]=findmin(t);
						tempcost1[i][0]=tempcostt[minvalueno][0];
						tempcost1[i][1]=tempcostt[minvalueno][1];
						tempcost1[i][2]=tempcostt[minvalueno][2];
					}
					findmin(temp1);
					transcost=transcost+tempcost1[minvalueno][0]*currentSucc.getUsageFrequency();
					computecost=computecost+tempcost1[minvalueno][1]*currentSucc.getUsageFrequency();
					genetime=genetime+tempcost1[minvalueno][2];
					//统计重新产生的数据集的相关输出信息
					geneDSnum[minvalueno]++;
					geneDS.get(minvalueno).add(currentSucc.getName());
					if(currentSucc.getName().equals("end"))
					{
						break;
					}
					oldSucc=currentSucc;
					if(currentSucc.equals(graph.getLastDataset()))
					{
						currentSucc=endver.getdataset();
					}
					else
					{
						currentSucc=currentSucc.getSuccessors().get(0);
					}
					temp=temp1;
					tempcost=tempcost1;
				}
		   }
	     }
		System.out.println("the store cost is : "+storecost);
	    System.out.println("the transfer cost is : "+transcost);
	    System.out.println("the computation cost is :"+computecost);
		System.out.println("the total of three is "+(storecost+transcost+computecost));
		averageGenerateTime=genetime/graph.getDatasets().size();
	    System.out.println("the average generation time is : "+averageGenerateTime);
		//输出总共存储了多少数据集
	    int storeTotalNum=0;
	    for(int i=0;i<m;i++)
	    {
		    storeTotalNum=storeTotalNum+storeDSnum[i];
	    }
	    System.out.println("the total number of dataset stored is : "+storeTotalNum);
	    System.out.println("the total number of dataset generated is : "+(graph.getDatasets().size()-storeTotalNum));
	    for(int i=0;i<m;i++)
	    {
			System.out.println("the number of dataset stored in cloud "+i+" is "+storeDSnum[i]);
			for(int j=0;j<storeDS.get(i).size();j++)
			{
			    System.out.println("the dataset stored in cloud "+i+" is "+storeDS.get(i).get(j));
			}
			System.out.println("the number of dataset generated in cloud "+i+" is "+geneDSnum[i]);
			for(int k=0;k<geneDS.get(i).size();k++)
			{
			    System.out.println("the dataset generated in cloud "+i+" is "+geneDS.get(i).get(k));
			}
	    }
		
	}
	// find the minimum value of a marry
	public static double findmin(double[] t)
	{
		double min=t[0];
		int j=0;
		for(int i=1;i<m;i++)
		{
			if(t[i]<min)
			{
				min=t[i];
				j=i;
			}
			minvalueno=j;
		}
		return min;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
