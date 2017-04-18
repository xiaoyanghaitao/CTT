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
			// ��ʼ����ʼ������ڵ�
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

			int datasetNum = datasets.size() - 2; // �õ���ʵ����������ݼ��ĸ���
			// ��ʼ����ѡ���б�
			LinkedList<CTT_ver2> candidates = new LinkedList<>();
			candidates.add(startVer);
			CTT_ver2[] dataset_iInCS = new CTT_ver2[m]; // Di�����з����̴��洢�Ľڵ�

			for (int i = 0; i < datasetNum; i++) { // ����ȫ������������ݼ������м���

				// �ҳ������ݼ�i������С�Ĵ�ŷ�ʽ
				CTT_ver2 chosenStorageForI = null; // ������ݼ�i��ŵ�λ��
				for (int j = 0; j < m; j++) { // ����λ�����з����̵Ķ���
					dataset_iInCS[j] = new CTT_ver2(datasets.get(i), i, j, m);
					double minGenCost = Double.MAX_VALUE; // �洢���ﵱǰ�������С�ɱ�
					for (CTT_ver2 candidate : candidates) {
						double genCostFromCandidate = candidate.getMinGenCost()
								+ getEdgeCost(candidate, dataset_iInCS[j], CS);
						if (minGenCost > genCostFromCandidate) { // �ҳ���С�ɱ�
							minGenCost = genCostFromCandidate;
							dataset_iInCS[j].setPredecessor(candidate);
						}
					}
					dataset_iInCS[j].setMinGenCost(minGenCost); // ����С�ɱ���������
					if (chosenStorageForI == null || chosenStorageForI.getMinGenCost() > minGenCost) // �õ����Ѵ洢K��С�Ķ���
						chosenStorageForI = dataset_iInCS[j];
				}

				// ��ÿ��candidate�������ݼ�K�ڸ����������ϴ�ŵ�verWeight
				for (CTT_ver2 candidate : candidates) {
					double[] verWeightForCurrentDataset = new double[m];
					for (int j = 0; j < m; j++) {
						verWeightForCurrentDataset[j] = getVerWeight(candidate, dataset_iInCS[j], CS);
					}
					candidate.setVerWeightForCurrentDataset(verWeightForCurrentDataset);
				}

				// ������candidates������̭
				double[] chosenCosts = chosenStorageForI.getPredecessor().getVerWeightForCurrentDataset(); // ��ѡ�еĴ洢�ڵ��ǰ����֮�����Ļ���
				for (ListIterator<CTT_ver2> lit = candidates.listIterator(); lit.hasNext();) {
					CTT_ver2 current = lit.next();
					if (current.equals(chosenStorageForI.getPredecessor())) // �����ǰ�ڵ���Ǵ洢�ڵ��ǰ�������ý��бȽ�
						continue;
					double[] currentCosts = current.getVerWeightForCurrentDataset();
					boolean shouldRemove = false;
					for (int j = 0; j < chosenCosts.length; j++) {
						if (currentCosts[j] > chosenCosts[j]) { // �����ǰcandidate��ȫ�����ɻ��Ѷ����ڴ洢�ڵ��ǰ������Ӧ���ѣ���ɾ����ǰcandidate
							shouldRemove = true;
						} else { // ���ĳһ�����ɻ���С�ڵ��ڴ洢�ڵ��ǰ������Ӧ���ѣ���ô�Ͳ�ɾ��
							shouldRemove = false;
							break;
						}
					}
					if (shouldRemove) // ɾ����ǰ��ѡ
						lit.remove();
				}

				// ��̬�滮������interGenCost
				for (CTT_ver2 candidate : candidates) {
					double minVerWeight = Double.MAX_VALUE;
					double[] verWeightForCurrentDataset = candidate.getVerWeightForCurrentDataset();
					for (int j = 0; j < m; j++) {
						if (minVerWeight > verWeightForCurrentDataset[j])
							minVerWeight = verWeightForCurrentDataset[j];
					}
					candidate.interGenCost += minVerWeight * datasets.get(i).getUsageFrequency(); // ��̬�滮�����ӵ�ǰ���ݼ������ɺķ�
				}

				// �����������ɵĽڵ���뵽��ѡ�ڵ���
				for (CTT_ver2 v : dataset_iInCS)
					candidates.add(v);
			}

			// ����candidates�������ڵ����С����
			{
				double minGenCost = Double.MAX_VALUE; // �洢���ﵱǰ�������С�ɱ�
				for (CTT_ver2 candidate : candidates) {
					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer, CS);
					if (minGenCost > costToEndVer) { // �ҳ���С�ɱ�
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
		double verWeight = end.getDataset().getGenerationTime() * CS[end.getCloudServiceID()].getcostC(); // ��end���ڵ��Ʒ������ϼ���ķ���
		// start�ĺ�̵���end�������м����ݼ�
		if (start.getDataset().getSuccessors().get(0).equals(end.getDataset())) {
			if (start.getCloudServiceID() != -1 && start.getCloudServiceID() != end.getCloudServiceID())
				verWeight += start.getDataset().getSize() * CS[start.getCloudServiceID()].getcostT(); // ��start���ڵ��Ʒ����̴���end���ڵ��Ʒ����̵ķ���
		}

		// �����м����ݼ���Ҫ�ҳ�min(verWeight+�������)
		else {
			double minSumCost = Double.MAX_VALUE;
			double[] verWeightForPreviousDataset = start.getVerWeightForCurrentDataset(); // �ڼ����µ�verWeightʱ��Ŀǰstart�ڵ��б���ľ���֮ǰ���ݼ���verWeight
			double sumCost = 0.0; // �����ݴ�(verWeight+�������)
			for (int i = 0; i < verWeightForPreviousDataset.length; i++) {
				// �ڷ�����i�ϵ�verWeight + end��ǰ�����ݼ��ӷ�����i���ƶ���end���ڵķ����̵ķ���
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
		double edgeCost = 0.0;// �����ߵ�cost
		double endCost = 0.0;// �ߵ��յ��cost
		// ���ȼ���ߵ��յ�Ĵ洢����
		// ����ߵ��յ�Ϊ����ڵ㣬��endCostΪ0
		if (target.getCloudServiceID() == -1) {
			endCost = 0;
		} else {
			// endCost=�յ����ڵ�cloud��costS*�յ����ݼ��Ĵ�С
			endCost = CS[target.getCloudServiceID()].getcostS() * target.getDataset().getSize();
		}
		// ����ߵ�ʼ��ĺ�̵����յ㣬��˵�����м����ݼ����ߵ�cost=endCost
		if (candidate.getDataset().getSuccessors().get(0).equals(target.getDataset())) {
			edgeCost = endCost;
			return edgeCost;
		}

		// �������м����ݼ�����Ҫ�����м����ɷ���
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
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT��0.06���0.1
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
