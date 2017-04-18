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

	public static double computeCttCost(DataDependencyGraph graph, int m, CloudService[] CS) {
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
			// 初始化候选者列表
			LinkedList<CTT_ver2> candidates = new LinkedList<>();
			candidates.add(startVer);
			CTT_ver2[] dataset_iInCS = new CTT_ver2[m]; // Di在所有服务商处存储的节点

			for (int i = 0; i < datasetNum; i++) { // 遍历全部有意义的数据集，进行计算

				// 找出到数据集i花销最小的存放方式
				CTT_ver2 chosenStorageForI = null; // 最后数据集i存放的位置
				for (int j = 0; j < m; j++) { // 创建位于所有服务商的顶点
					dataset_iInCS[j] = new CTT_ver2(datasets.get(i), i, j, m);
					double minGenCost = Double.MAX_VALUE; // 存储到达当前顶点的最小成本
					for (CTT_ver2 candidate : candidates) {
						double genCostFromCandidate = candidate.getMinGenCost()
								+ getEdgeCost(candidate, dataset_iInCS[j], CS);
						if (minGenCost > genCostFromCandidate) { // 找出最小成本
							minGenCost = genCostFromCandidate;
							dataset_iInCS[j].setPredecessor(candidate);
						}
					}
					dataset_iInCS[j].setMinGenCost(minGenCost); // 将最小成本赋给顶点
					if (chosenStorageForI == null || chosenStorageForI.getMinGenCost() > minGenCost) // 得到花费存储K最小的顶点
						chosenStorageForI = dataset_iInCS[j];
				}

				// 对每个candidate计算数据集K在各个服务商上存放的verWeight
				for (CTT_ver2 candidate : candidates) {
					double[] verWeightForCurrentDataset = new double[m];
					for (int j = 0; j < m; j++) {
						verWeightForCurrentDataset[j] = getVerWeight(candidate, dataset_iInCS[j], CS);
					}
					candidate.setVerWeightForCurrentDataset(verWeightForCurrentDataset);
				}

				// 对现有candidates进行淘汰
				double[] chosenCosts = chosenStorageForI.getPredecessor().getVerWeightForCurrentDataset(); // 被选中的存储节点的前驱到之后各点的花费
				for (ListIterator<CTT_ver2> lit = candidates.listIterator(); lit.hasNext();) {
					CTT_ver2 current = lit.next();
					if (current.equals(chosenStorageForI.getPredecessor())) // 如果当前节点就是存储节点的前驱，则不用进行比较
						continue;
					double[] currentCosts = current.getVerWeightForCurrentDataset();
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

				// 动态规划，计算interGenCost
				for (CTT_ver2 candidate : candidates) {
					double minVerWeight = Double.MAX_VALUE;
					double[] verWeightForCurrentDataset = candidate.getVerWeightForCurrentDataset();
					for (int j = 0; j < m; j++) {
						if (minVerWeight > verWeightForCurrentDataset[j])
							minVerWeight = verWeightForCurrentDataset[j];
					}
					candidate.interGenCost += minVerWeight * datasets.get(i).getUsageFrequency(); // 动态规划，增加当前数据集的生成耗费
				}

				// 把所有新生成的节点加入到候选节点中
				for (CTT_ver2 v : dataset_iInCS)
					candidates.add(v);
			}

			// 计算candidates到结束节点的最小花费
			{
				double minGenCost = Double.MAX_VALUE; // 存储到达当前顶点的最小成本
				for (CTT_ver2 candidate : candidates) {
					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer, CS);
					if (minGenCost > costToEndVer) { // 找出最小成本
						minGenCost = costToEndVer;
						endVer.setPredecessor(candidate);
					}
				}
				totalCostR = minGenCost;
			}

			CTT_ver2 storedVer = endVer.getPredecessor();
			Dataset tmp = null;
			while (!storedVer.getDataset().getName().equals("start")) {
				tmp = storedVer.getDataset();
				tmp.setcsid(storedVer.getCloudServiceID());
				tmp.setStored(true);
				storedVer = storedVer.getPredecessor();
			}
		}
		endmxbe = threadMXBean.getCurrentThreadCpuTime();
		timemxbe = endmxbe - startmxbe;
		System.out.println("the Improved_CTT_SP3 Execution time is " + timemxbe);
		System.out.println("the total cost of Improved_CTT_SP3 Strategy is : " + totalCostR);
		return totalCostR;
	}

	private static double getVerWeight(CTT_ver2 start, CTT_ver2 end, CloudService[] CS) {
		double verWeight = end.getDataset().getGenerationTime() * CS[end.getCloudServiceID()].getcostC(); // 在end所在的云服务商上计算的费用
		// start的后继等于end，即无中间数据集
		if (start.getDataset().getSuccessors().get(0).equals(end.getDataset())) {
			if (start.getCloudServiceID() != -1 && start.getCloudServiceID() != end.getCloudServiceID())
				verWeight += start.getDataset().getSize() * CS[start.getCloudServiceID()].getcostT(); // 从start所在的云服务商传到end所在的云服务商的费用
		}

		// 存在中间数据集，要找出min(verWeight+运输费用)
		else {
			double minSumCost = Double.MAX_VALUE;
			double[] verWeightForPreviousDataset = start.getVerWeightForCurrentDataset(); // 在计算新的verWeight时，目前start节点中保存的就是之前数据集的verWeight
			double sumCost = 0.0; // 用于暂存(verWeight+运输费用)
			for (int i = 0; i < verWeightForPreviousDataset.length; i++) {
				// 在服务商i上的verWeight + end的前驱数据集从服务商i上移动到end所在的服务商的费用
				sumCost = verWeightForPreviousDataset[i];
				if (i != end.getCloudServiceID())
					sumCost += end.getDataset().getPredecessors().get(0).getSize() * CS[i].getcostT();
				if (minSumCost > sumCost) {
					minSumCost = sumCost;
				}
			}
			verWeight += minSumCost;
		}
		return verWeight;
	}

	private static double getEdgeCost(CTT_ver2 candidate, CTT_ver2 target, CloudService[] CS) {
		double edgeCost = 0.0;// 整条边的cost
		double endCost = 0.0;// 边的终点的cost
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
			edgeCost = endCost;
			return edgeCost;
		}

		// 若存在中间数据集，则要增加中间生成费用
		else {
			edgeCost = endCost + candidate.interGenCost;
			return edgeCost;
		}
	}

	// /////////////////////////////////////
	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG400.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		long startTime = System.currentTimeMillis();
		double result;
		double[] bandwiths0 = { 0, 0.128, 0.128 };
		double[] bandwiths1 = { 0.128, 0, 0.128 };
		double[] bandwiths2 = { 0.128, 0.128, 0.0 };
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		CloudService CS1 = new CloudService(0.06 / 30, 0.12 * 24, 0.03, 1, bandwiths1);
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT由0.06变成0.1
		CloudService[] CS = new CloudService[3];
		CS[0] = CS0;
		CS[1] = CS1;
		CS[2] = CS2;
		int m = 3;
		result = Improved_CTT_SP3.computeCttCost(graph, m, CS);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		System.out.println("totalcost:" + result);
		System.out.println("Program is Over");
	}

}
