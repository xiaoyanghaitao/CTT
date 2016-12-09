package strategies;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import newClass.CTT_ver2;
import newClass.CloudService;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

public class Improved_CTT_SP3 {

	private static CloudService[] CS = new CloudService[3];
	private static double[][] CloudServicesComputingCost;

	private static double computeCttCost(DataDependencyGraph graph, int m) {
		long startmxbe = 0;
		long endmxbe = 0;
		long timemxbe = 0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		double totalCostR = 0.0;
		{
			List<Dataset> datasets = graph.getDatasets();
			// 初始化开始与结束节点
			CTT_ver2 startVer = new CTT_ver2(m);
			CTT_ver2 endVer = new CTT_ver2(m);
			startVer.getDataset().setName("start");
			endVer.getDataset().setName("end");
			startVer.getDataset().setcsid(-1);
			endVer.getDataset().setcsid(-1);
			Dataset firstDataset = graph.getFirstDataset();
			startVer.getDataset().addSuccessor(firstDataset);
			firstDataset.addPredecessor(startVer.getDataset());
			Dataset lastDataset = graph.getLastDataset();
			endVer.getDataset().addPredecessor(lastDataset);
			lastDataset.addSuccessor(endVer.getDataset());
			graph.addDataset(startVer.getDataset());
			graph.addDataset(endVer.getDataset());

			int datasetNum = datasets.size() - 2; // 得到有实际意义的数据集的个数
			// 初始化各服务商的SumX
			double[][] cloudServicesSumX = new double[datasetNum][m];// 所有服务商的SumX(k)=X_k+...X_now
			// 初始化个服务商上边的计算权重
			CloudServicesComputingCost = new double[datasetNum][m];
			// 初始化候选者列表
			LinkedList<CTT_ver2> candidates = new LinkedList<>();
			candidates.add(startVer);
			CTT_ver2[] datasetKInCS = new CTT_ver2[m]; // Dk在所有服务商处存储的节点

			for (int k = 0; k < datasetNum; k++) { // 遍历全部有意义的数据集，进行计算
				int i = Math.max(candidates.peek().getDatasetNum(), 0); // 得到有意义的边开始位置
				for (; i < k; i++) {
					for (int j = 0; j < m; j++) { // 通过SumX计算出边的计算权重
						cloudServicesSumX[i][j] += datasets.get(k - 1).getGenerationTime() * CS[j].getcostC();
						CloudServicesComputingCost[i][j] += 
								cloudServicesSumX[i][j] * datasets.get(k - 1).getUsageFrequency();
					}
				}
				CTT_ver2 chosenStorageForK = null; // 最后数据集K存放的位置
				for (int j = 0; j < m; j++) { // 创建位于所有服务商的顶点
					datasetKInCS[j] = new CTT_ver2(datasets.get(k), k, j, m);
					double minGenCost = Double.MAX_VALUE; // 存储到达当前顶点的最小成本
					for (CTT_ver2 candidate : candidates) {
//						candidate.setGenCostInK(j, candidate.getMinGenCost() + getEdgeCost(candidate, datasetKInCS[j], CS, m));
						candidate.setGenCostInK(j, candidate.getMinGenCost() + getEdgeCost(candidate, datasetKInCS[j]));
						if (minGenCost > candidate.getGenCostInK(j)) { // 找出最小成本
							minGenCost = candidate.getGenCostInK(j);
							datasetKInCS[j].setPredecessor(candidate);
						}
					}
					datasetKInCS[j].setMinGenCost(minGenCost); // 将最小成本赋给顶点
					if (chosenStorageForK == null || chosenStorageForK.getMinGenCost() > minGenCost) // 得到花费存储K最小的顶点
						chosenStorageForK = datasetKInCS[j];
				}
				// 对现有candidates进行淘汰
				double[] chosenCosts = chosenStorageForK.getPredecessor().getGenLatterCost(); // 被选中的存储节点的前驱到之后各点的花费
				for (ListIterator<CTT_ver2> lit = candidates.listIterator(); lit.hasNext();) {
					CTT_ver2 current = lit.next();
					if (current.equals(chosenStorageForK.getPredecessor())) // 如果当前节点就是存储节点的前驱，则不用进行比较
						continue;
					double[] currentCosts = current.getGenLatterCost();
					boolean shouldRemove = false;
					for (int j = 0; j < chosenCosts.length; j++) {
						if (currentCosts[j] > chosenCosts[j]) { // 如果当前candidate的全部生成花费都大于存储节点的前驱的相应花费，则删除当前candidate
							shouldRemove = true;
						} else { // 如果某一次生成花费小于等于存储节点的前驱的相应花费，那么就不删除
							shouldRemove = false;
							break;
						}
					}
					if (shouldRemove) // 删除当前候选
						lit.remove();
				}
				for (CTT_ver2 v : datasetKInCS) // 把所有新生成的节点加入到候选节点中
					candidates.add(v);
			}

			// 计算candidates到结束节点的最小花费
			{
				int i = Math.max(candidates.peek().getDatasetNum(), 0); // 得到有意义的边开始位置
				for (; i < datasetNum; i++) {
					for (int j = 0; j < m; j++) { // 通过SumX计算出边的计算权重
						cloudServicesSumX[i][j] += datasets.get(datasetNum - 1).getGenerationTime() * CS[j].getcostC();
						CloudServicesComputingCost[i][j] += 
								cloudServicesSumX[i][j] * datasets.get(datasetNum - 1).getUsageFrequency();
					}
				}
				double minGenCost = Double.MAX_VALUE; // 存储到达当前顶点的最小成本
				for (CTT_ver2 candidate : candidates) {
//					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer, CS, m);
					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer);
					if (minGenCost > costToEndVer) { // 找出最小成本
						minGenCost = costToEndVer;
						endVer.setPredecessor(candidate);
					}
					endVer.setMinGenCost(minGenCost); // 将最小成本赋给顶点
				}
				totalCostR = endVer.getMinGenCost();
			}
		}
		endmxbe = threadMXBean.getCurrentThreadCpuTime();
		timemxbe = endmxbe - startmxbe;
		System.out.println("the Improved_CTT_SP3 Execution time is " + timemxbe);
		System.out.println("the total cost of Improved_CTT_SP3 Strategy is : " + totalCostR);
		return totalCostR;
	}
	
//	//compute edge cost 计算边的值
//	private static double getEdgeCost(CTT_ver2 start_ver, CTT_ver2 end_ver,CloudService[] CS, int m)
//	{
//		double edgeCost=0.0;//整条边的cost
//		double endCost=0.0;//边的终点的cost
//		double interCost=0.0;//所有中间节点的cost
//		//首先计算边的终点的存储代价
//		//如果边的终点为虚拟节点，则endCost为0
//		if(end_ver.getCloudServiceID()==-1)
//		{
//			endCost=0;
//		}
//		else
//		{
//			//endCost=终点所在的cloud的costS*终点数据集的大小
//			endCost=CS[end_ver.getCloudServiceID()].getcostS()*end_ver.getDataset().getSize();
//		}
//		//如果边的始点的后继等于终点，则说明无中间数据集；边的cost=endCost
//		if(start_ver.getDataset().getSuccessors().get(0).equals(end_ver.getDataset()))
//		{
//			
//			edgeCost=endCost;
//			return edgeCost;
//		}
//		// 若存在中间数据集，则首先计算第一个中间数据集firstSucc的cost，再计算其他数据集的cost
//		else
//		{
//			double[] temp=new double[m];
//			double[] temp1=new double[m];
//			double[] t=new double[m];
//			// compute the cost of the first intermediate dataset 
//			//数据集firstSucc是第一个中间数据集
//			Dataset firstSucc=start_ver.getDataset().getSuccessors().get(0);
//			double Minvalue=0.0;
//			//若边的始点为虚拟数据集对应的节点，则始点的传输代价为0，firstSucc的cost即为它本身的产生代价
//			if(start_ver.getCloudServiceID()==-1)
//			{
//				for(int j=0;j<m;j++)
//				{
//					temp[j]=CS[j].getcostC()*firstSucc.getGenerationTime();
//				}
//			}
//			//若边的始点不为虚拟节点，则firstSucc的cost=边的始点的传输代价+firstSucc的产生代价
//			else
//			{
//				//csid为边始点的cloud
//				int csid=start_ver.getCloudServiceID();
//				for(int i=0;i<m;i++)
//				{
//					if(i==csid)
//					{
//						temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime();
//					}
//					else
//					{
//						temp[i]=CS[i].getcostC()*firstSucc.getGenerationTime()+start_ver.getDataset().getSize()*CS[csid].getcostT();
//					}
//				}
//			}
//			//Minvalue为firstSucc的一次产生的cost*firstSucc的使用频率
//			Minvalue=findminvalue(temp,m)*firstSucc.getUsageFrequency();
//			//中间节点的cost加入Minvalue
//			interCost=interCost+Minvalue;
//			
//			Dataset currentSucc=firstSucc.getSuccessors().get(0);
//			Dataset oldSucc=firstSucc;
//			//如果firstSucc的后继节点不等于边的终点，则继续计算
//			while(!currentSucc.getName().equals(end_ver.getDataset().getName()))
//			{
//				//计算currentSucc对应的m个节点的值
//				for(int j=0;j<m;j++)
//				{
//					//计算j节点的值，从m个中选择一个最小的
//					for(int k=0;k<m;k++)
//					{
//						//从cloud k 传输oldSucc，若k=j，则t只为currentSucc的产生时间
//						if(k==j)
//						{
//							t[k]=temp[k]+currentSucc.getGenerationTime()*CS[j].getcostC();
//						}
//						//k不等于j，则需要加上oldSucc的传输代价
//						else
//						{
//							t[k]=temp[k]+oldSucc.getSize()*CS[k].getcostT()+currentSucc.getGenerationTime()*CS[j].getcostC();
//						}
//					}
//					//取最小值作为j节点的值
//					temp1[j]=findminvalue(t,m);
//				}
//				//得到currentSucc的cost
//				Minvalue=findminvalue(temp1,m)*currentSucc.getUsageFrequency();
//				interCost=interCost+Minvalue;
//				if(currentSucc.getName().equals("end"))
//				{
//					break;
//				}
//				else
//				{
//					oldSucc=currentSucc;
//					currentSucc=currentSucc.getSuccessors().get(0);
//				}
//				//将temp1的值赋给temp
//				temp=temp1;
//			}
//			//边的值=边终点的cost+中间节点的cost
//		edgeCost=endCost+interCost;
//		return edgeCost;
//		}
//	}
//	// find the minimum value of a marry
//	public static double findminvalue(double[] t, int m)
//	{
//		double min=t[0];
//		for(int i=1;i<m;i++)
//		{
//			if(t[i]<min)
//			{
//				min=t[i];
//			}
//		}
//		return min;
//	}
	private static double getEdgeCost(CTT_ver2 candidate, CTT_ver2 target) {
		double edgeCost = 0.0;// 整条边的cost
		double endCost = 0.0;// 边的终点的cost
		double interCost = Double.MAX_VALUE;// 所有中间节点的cost
		// 首先计算边的终点的存储代价
		// 如果边的终点为虚拟节点，则endCost为0
		if (target.getCloudServiceID() == -1) {
			endCost = 0;
		} else {
			// endCost=终点所在的cloud的costS*终点数据集的大小
			endCost = CS[target.getCloudServiceID()].getcostS() * target.getDataset().getSize();
		}
		// 如果边的始点的后继等于终点，则说明无中间数据集；边的cost=endCost
		if (candidate.getDataset().getSuccessors().get(0).equals(target.getDataset())) {
			//TODO 为什么不考虑迁移成本
			edgeCost = endCost;
			return edgeCost;
		} 

		// 若存在中间数据集，则要判断出经过怎样的中间运算开销最小
		else {
			// 确定有可能为候选的最后一个服务商ID+1
			int computingCSLimit = Math.min(candidate.getCloudServiceID(), target.getCloudServiceID()) + 1;
			if(computingCSLimit == 0) //如果候选的服务商ID为-1，那么就在最便宜的服务商上计算
				computingCSLimit = 1;
			double costOnCm = 0.0; // 通过Cm进行运算的花费
			for (int i = 0; i < computingCSLimit; i++) {
				costOnCm = CloudServicesComputingCost[candidate.getDatasetNum() + 1][i]; // 边的计算花费
				if (candidate.getCloudServiceID() != -1 && i != candidate.getCloudServiceID()) // 如果起始节点和运算节点服务商不同，增加数据从起始点运出的费用
					costOnCm += candidate.getDataset().getSize() * CS[candidate.getCloudServiceID()].getcostT();
				if (target.getCloudServiceID() != -1 && i != target.getCloudServiceID()) // 如果结束节点与运算节点服务商不同，增加数据从运算节点运到结束节点的费用
					costOnCm += target.getDataset().getPredecessors().get(0).getSize() * CS[i].getcostT(); // TODO
																											// 我现在生成了哪一个数据集？
				if (interCost > costOnCm) // 找出花销最小的中间步骤
					interCost = costOnCm;
			}
			edgeCost = endCost + interCost;
			return edgeCost;
		}
	}

	// /////////////////////////////////////
	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG5.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		long startTime = System.currentTimeMillis();
		double result;
		double[] bandwiths0 = { 0, 0.128, 0.128 };
		double[] bandwiths1 = { 0.128, 0, 0.128 };
		double[] bandwiths2 = { 0.128, 0.128, 0.0 };
		// 默认把服务商按照计算费用从小到大排好序，用于满足定理1
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		CloudService CS1 = new CloudService(0.06 / 30, 0.12 * 24, 0.03, 1, bandwiths1);
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT由0.06变成0.1
		CS[0] = CS0;
		CS[1] = CS1;
		CS[2] = CS2;
		int m = 3;
		result = Improved_CTT_SP3.computeCttCost(graph, m);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		System.out.println("totalcost:" + result);
		System.out.println("Program is Over");
	}

}
