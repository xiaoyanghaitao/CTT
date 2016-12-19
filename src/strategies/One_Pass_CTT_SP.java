package strategies;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
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

/*
 * 
 * Date：2016-10-19
 * Author：2414890918@qq.com
 * 
 * 算法改进，对CTT_SP算法进行改进，以获得更高的速度。
 * 第一步：Linear+1个服务提供商。
 * 
 */
public class One_Pass_CTT_SP {
	private static double averageGenerateTime = 0.0;
	private static WeightedGraph<Dataset, DefaultWeightedEdge> cttGraph;
	private static CloudService cloud;

	public static double getaverageGenerateTime() {
		return averageGenerateTime;
	}

	// compute the total
	// cost==================================================================
	public static void computeCttCost(DataDependencyGraph graph, CloudService clouds) {
		long startmxbe = 0;
		long endmxbe = 0;
		long timemxbe = 0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		cloud = clouds;
		{
			startmxbe = threadMXBean.getCurrentThreadCpuTime();
			// initialize 2016-10-19 zjh
			List<Dataset> datasets = graph.getDatasets();
			double costS = cloud.getcostS();
			double costC = cloud.getcostC();
			double generationCost[] = new double[datasets.size() + 2];
			double storageCost[] = new double[datasets.size() + 2];
			double useFrequency[] = new double[datasets.size() + 2];
			for (int i = 1; i <= datasets.size(); i++) {
				generationCost[i] = datasets.get(i - 1).getGenerationTime() * costC;
				storageCost[i] = datasets.get(i - 1).getSize() * costS;
				useFrequency[i] = datasets.get(i - 1).getUsageFrequency();
			}

			double SumX[] = new double[datasets.size() + 2];// SumX(k)=X_k+...X_now
			double Weight[] = new double[datasets.size() + 2];
			double Length[] = new double[datasets.size() + 2];
			int StPred[] = new int[datasets.size() + 2];

			for (int i = 0; i < datasets.size() + 2; i++) {
				Length[i] = Double.MAX_VALUE;
			}
			// SumX[0]=datasets.get(0).getGenerationTime()*costC;
			Weight[0] = storageCost[1];
			Length[0] = 0;
			Length[1] = storageCost[1];

			for (int i = 2; i < datasets.size() + 2; i++) {
				for (int k = StPred[i - 1]; k < i - 1; k++) {
					SumX[k] = SumX[k] + generationCost[i - 1];
					Weight[k] = Weight[k] + SumX[k] * useFrequency[i - 1] - storageCost[i - 1] + storageCost[i];
					if ((Length[k] + Weight[k]) <= Length[i]) {
						Length[i] = Length[k] + Weight[k];
						StPred[i] = k;
					}
				}
				{
					int k = i - 1;
					Weight[k] = storageCost[i];
					if ((Length[k] + Weight[k]) <= Length[i]) {
						Length[i] = Length[k] + Weight[k];
						StPred[i] = k;
					}
				}
			}
			System.out.println("the total cost of One_Pass_CTT_SP is : " + Length[datasets.size() + 1]);
			endmxbe = threadMXBean.getCurrentThreadCpuTime();
			timemxbe = endmxbe - startmxbe;
			System.out.println("The One_Pass_CTT_SP Strategy Execution Timemxbean : " + timemxbe);

			ArrayList<Integer> storedDS = new ArrayList<>();
			storedDS.add(StPred[datasets.size() + 1]);
			while (storedDS.get(storedDS.size() - 1) > 0) {
				storedDS.add(StPred[storedDS.get(storedDS.size() - 1)]);
			}
			for (int i = 0; i < storedDS.size() && storedDS.get(i) > 0; i++) {
				datasets.get(storedDS.get(i) - 1).setStored(true);
			}
		}
		// 输出相应的数据
		// One_Pass_CTT_SP.print(graph);

	}

	// create
	// ctt====================================================================================
	private static void createCttGraph(List<Dataset> datasets, Dataset startDS, Dataset endDS) {
		// add all datasets into ctt

		for (Dataset aDataset : datasets) {
			cttGraph.addVertex(aDataset);
		}
		// create all edges
		int i = 0;
		int j = 0;
		for (i = 0; i < datasets.size() - 1; i++) {
			for (j = i + 1; j < datasets.size(); j++) {
				cttGraph.addEdge(datasets.get(i), datasets.get(j));
				cttGraph.setEdgeWeight(cttGraph.getEdge(datasets.get(i), datasets.get(j)),
						getEdgeCost(datasets.get(i), datasets.get(j), i, j, datasets));
			}
		}
		for (i = 0; i < datasets.size(); i++) {
			cttGraph.addEdge(startDS, datasets.get(i));
			cttGraph.setEdgeWeight(cttGraph.getEdge(startDS, datasets.get(i)),
					getEdgeCost(startDS, datasets.get(i), -1, i, datasets));
		}
		cttGraph.addEdge(startDS, endDS);
		cttGraph.setEdgeWeight(cttGraph.getEdge(startDS, endDS),
				getEdgeCost(startDS, endDS, -1, datasets.size(), datasets));
		for (i = 0; i < datasets.size(); i++) {
			cttGraph.addEdge(datasets.get(i), endDS);
			cttGraph.setEdgeWeight(cttGraph.getEdge(datasets.get(i), endDS),
					getEdgeCost(datasets.get(i), endDS, i, datasets.size(), datasets));
		}
	}

	// compute the edge
	// cost=====================================================================
	private static double getEdgeCost(Dataset startDataset, Dataset endDataset, int start, int end,
			List<Dataset> datasets) {
		double edgeCost = 0.0;
		double costS = cloud.getcostS();
		double costC = cloud.getcostC();
		double endDataset_cost = endDataset.getSize() * costS;
		int k = 0;
		double allinterdsCost = 0.0;
		double tempCost = 0.0;
		double interdsCost = 0.0;
		Dataset d;
		for (k = start + 1; k < end; k++) {
			d = datasets.get(k);
			tempCost = tempCost + d.getGenerationTime() * costC;
			interdsCost = interdsCost + tempCost * d.getUsageFrequency();
			allinterdsCost = allinterdsCost + interdsCost;
			interdsCost = 0.0;
		}
		edgeCost = endDataset_cost + allinterdsCost;
		return edgeCost;
	}

	public static void print(DataDependencyGraph graph) {
		// 计算平均产生时间：获得所有数据集的平均时间=获得每个数据集的时间/数据集的个数
		double genetime = 0.0;
		double temptime = 0.0;
		int count = 0;
		List<Dataset> datasets = graph.getDatasets();
		for (Dataset aDataset : datasets) {
			if (!aDataset.isStored()) {
				temptime += aDataset.getGenerationTime();
				Set<Dataset> pSet = getPSet(aDataset);
				for (Dataset pSetDS : pSet) {
					temptime = temptime + pSetDS.getGenerationTime();
				}
				genetime = genetime + temptime;
				temptime = 0;
				count++;
			}
		}
		averageGenerateTime = genetime / graph.getDatasets().size();
		System.out.println("the average generation time is : " + averageGenerateTime);
		System.out.println("the number of datasets generated in local is : " + count);
		System.out.println("the datasets generated in local is : ");
		for (Dataset ds : datasets) {
			if (!ds.isStored())
				System.out.println("the dataset generated in local is " + ds.getName());
		}
		System.out.println("the number of datasets stored in local is : " + (graph.getDatasets().size() - count));
	}

	// get all datasets that are not saved before aDataset
	private static Set<Dataset> getPSet(Dataset aDataset) {
		Set<Dataset> pSet = new HashSet<Dataset>();
		List<Dataset> predecessors = aDataset.getPredecessors();
		for (Dataset predecessor : predecessors) {
			if (!predecessor.isStored()) {
				pSet.add(predecessor);
				pSet.addAll(getPSet(predecessor));
			}
		}
		return pSet;
	}

	///////////////////////////////////////
	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG200.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		// print(graph);
		long startTime = System.currentTimeMillis();
		double[] bandwiths0 = { 0, 0.128, 0.128 };
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		One_Pass_CTT_SP.computeCttCost(graph, CS0);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		int count = 0;
		for (Dataset d : graph.getDatasets()) {
			if (d.isStored()) {
				System.out.println("save:" + d.getName());
				count++;
			}
		}
		System.out.println("the number of save datasets：" + count);
	}
}
