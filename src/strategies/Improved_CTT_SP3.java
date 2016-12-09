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
			// ��ʼ���������̵�SumX
			double[][] cloudServicesSumX = new double[datasetNum][m];// ���з����̵�SumX(k)=X_k+...X_now
			// ��ʼ�����������ϱߵļ���Ȩ��
			CloudServicesComputingCost = new double[datasetNum][m];
			// ��ʼ����ѡ���б�
			LinkedList<CTT_ver2> candidates = new LinkedList<>();
			candidates.add(startVer);
			CTT_ver2[] datasetKInCS = new CTT_ver2[m]; // Dk�����з����̴��洢�Ľڵ�

			for (int k = 0; k < datasetNum; k++) { // ����ȫ������������ݼ������м���
				int i = Math.max(candidates.peek().getDatasetNum(), 0); // �õ�������ı߿�ʼλ��
				for (; i < k; i++) {
					for (int j = 0; j < m; j++) { // ͨ��SumX������ߵļ���Ȩ��
						cloudServicesSumX[i][j] += datasets.get(k - 1).getGenerationTime() * CS[j].getcostC();
						CloudServicesComputingCost[i][j] += 
								cloudServicesSumX[i][j] * datasets.get(k - 1).getUsageFrequency();
					}
				}
				CTT_ver2 chosenStorageForK = null; // ������ݼ�K��ŵ�λ��
				for (int j = 0; j < m; j++) { // ����λ�����з����̵Ķ���
					datasetKInCS[j] = new CTT_ver2(datasets.get(k), k, j, m);
					double minGenCost = Double.MAX_VALUE; // �洢���ﵱǰ�������С�ɱ�
					for (CTT_ver2 candidate : candidates) {
//						candidate.setGenCostInK(j, candidate.getMinGenCost() + getEdgeCost(candidate, datasetKInCS[j], CS, m));
						candidate.setGenCostInK(j, candidate.getMinGenCost() + getEdgeCost(candidate, datasetKInCS[j]));
						if (minGenCost > candidate.getGenCostInK(j)) { // �ҳ���С�ɱ�
							minGenCost = candidate.getGenCostInK(j);
							datasetKInCS[j].setPredecessor(candidate);
						}
					}
					datasetKInCS[j].setMinGenCost(minGenCost); // ����С�ɱ���������
					if (chosenStorageForK == null || chosenStorageForK.getMinGenCost() > minGenCost) // �õ����Ѵ洢K��С�Ķ���
						chosenStorageForK = datasetKInCS[j];
				}
				// ������candidates������̭
				double[] chosenCosts = chosenStorageForK.getPredecessor().getGenLatterCost(); // ��ѡ�еĴ洢�ڵ��ǰ����֮�����Ļ���
				for (ListIterator<CTT_ver2> lit = candidates.listIterator(); lit.hasNext();) {
					CTT_ver2 current = lit.next();
					if (current.equals(chosenStorageForK.getPredecessor())) // �����ǰ�ڵ���Ǵ洢�ڵ��ǰ�������ý��бȽ�
						continue;
					double[] currentCosts = current.getGenLatterCost();
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
				for (CTT_ver2 v : datasetKInCS) // �����������ɵĽڵ���뵽��ѡ�ڵ���
					candidates.add(v);
			}

			// ����candidates�������ڵ����С����
			{
				int i = Math.max(candidates.peek().getDatasetNum(), 0); // �õ�������ı߿�ʼλ��
				for (; i < datasetNum; i++) {
					for (int j = 0; j < m; j++) { // ͨ��SumX������ߵļ���Ȩ��
						cloudServicesSumX[i][j] += datasets.get(datasetNum - 1).getGenerationTime() * CS[j].getcostC();
						CloudServicesComputingCost[i][j] += 
								cloudServicesSumX[i][j] * datasets.get(datasetNum - 1).getUsageFrequency();
					}
				}
				double minGenCost = Double.MAX_VALUE; // �洢���ﵱǰ�������С�ɱ�
				for (CTT_ver2 candidate : candidates) {
//					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer, CS, m);
					double costToEndVer = candidate.getMinGenCost() + getEdgeCost(candidate, endVer);
					if (minGenCost > costToEndVer) { // �ҳ���С�ɱ�
						minGenCost = costToEndVer;
						endVer.setPredecessor(candidate);
					}
					endVer.setMinGenCost(minGenCost); // ����С�ɱ���������
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
	
//	//compute edge cost ����ߵ�ֵ
//	private static double getEdgeCost(CTT_ver2 start_ver, CTT_ver2 end_ver,CloudService[] CS, int m)
//	{
//		double edgeCost=0.0;//�����ߵ�cost
//		double endCost=0.0;//�ߵ��յ��cost
//		double interCost=0.0;//�����м�ڵ��cost
//		//���ȼ���ߵ��յ�Ĵ洢����
//		//����ߵ��յ�Ϊ����ڵ㣬��endCostΪ0
//		if(end_ver.getCloudServiceID()==-1)
//		{
//			endCost=0;
//		}
//		else
//		{
//			//endCost=�յ����ڵ�cloud��costS*�յ����ݼ��Ĵ�С
//			endCost=CS[end_ver.getCloudServiceID()].getcostS()*end_ver.getDataset().getSize();
//		}
//		//����ߵ�ʼ��ĺ�̵����յ㣬��˵�����м����ݼ����ߵ�cost=endCost
//		if(start_ver.getDataset().getSuccessors().get(0).equals(end_ver.getDataset()))
//		{
//			
//			edgeCost=endCost;
//			return edgeCost;
//		}
//		// �������м����ݼ��������ȼ����һ���м����ݼ�firstSucc��cost���ټ����������ݼ���cost
//		else
//		{
//			double[] temp=new double[m];
//			double[] temp1=new double[m];
//			double[] t=new double[m];
//			// compute the cost of the first intermediate dataset 
//			//���ݼ�firstSucc�ǵ�һ���м����ݼ�
//			Dataset firstSucc=start_ver.getDataset().getSuccessors().get(0);
//			double Minvalue=0.0;
//			//���ߵ�ʼ��Ϊ�������ݼ���Ӧ�Ľڵ㣬��ʼ��Ĵ������Ϊ0��firstSucc��cost��Ϊ������Ĳ�������
//			if(start_ver.getCloudServiceID()==-1)
//			{
//				for(int j=0;j<m;j++)
//				{
//					temp[j]=CS[j].getcostC()*firstSucc.getGenerationTime();
//				}
//			}
//			//���ߵ�ʼ�㲻Ϊ����ڵ㣬��firstSucc��cost=�ߵ�ʼ��Ĵ������+firstSucc�Ĳ�������
//			else
//			{
//				//csidΪ��ʼ���cloud
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
//			//MinvalueΪfirstSucc��һ�β�����cost*firstSucc��ʹ��Ƶ��
//			Minvalue=findminvalue(temp,m)*firstSucc.getUsageFrequency();
//			//�м�ڵ��cost����Minvalue
//			interCost=interCost+Minvalue;
//			
//			Dataset currentSucc=firstSucc.getSuccessors().get(0);
//			Dataset oldSucc=firstSucc;
//			//���firstSucc�ĺ�̽ڵ㲻���ڱߵ��յ㣬���������
//			while(!currentSucc.getName().equals(end_ver.getDataset().getName()))
//			{
//				//����currentSucc��Ӧ��m���ڵ��ֵ
//				for(int j=0;j<m;j++)
//				{
//					//����j�ڵ��ֵ����m����ѡ��һ����С��
//					for(int k=0;k<m;k++)
//					{
//						//��cloud k ����oldSucc����k=j����tֻΪcurrentSucc�Ĳ���ʱ��
//						if(k==j)
//						{
//							t[k]=temp[k]+currentSucc.getGenerationTime()*CS[j].getcostC();
//						}
//						//k������j������Ҫ����oldSucc�Ĵ������
//						else
//						{
//							t[k]=temp[k]+oldSucc.getSize()*CS[k].getcostT()+currentSucc.getGenerationTime()*CS[j].getcostC();
//						}
//					}
//					//ȡ��Сֵ��Ϊj�ڵ��ֵ
//					temp1[j]=findminvalue(t,m);
//				}
//				//�õ�currentSucc��cost
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
//				//��temp1��ֵ����temp
//				temp=temp1;
//			}
//			//�ߵ�ֵ=���յ��cost+�м�ڵ��cost
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
		double edgeCost = 0.0;// �����ߵ�cost
		double endCost = 0.0;// �ߵ��յ��cost
		double interCost = Double.MAX_VALUE;// �����м�ڵ��cost
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
			//TODO Ϊʲô������Ǩ�Ƴɱ�
			edgeCost = endCost;
			return edgeCost;
		} 

		// �������м����ݼ�����Ҫ�жϳ������������м����㿪����С
		else {
			// ȷ���п���Ϊ��ѡ�����һ��������ID+1
			int computingCSLimit = Math.min(candidate.getCloudServiceID(), target.getCloudServiceID()) + 1;
			if(computingCSLimit == 0) //�����ѡ�ķ�����IDΪ-1����ô��������˵ķ������ϼ���
				computingCSLimit = 1;
			double costOnCm = 0.0; // ͨ��Cm��������Ļ���
			for (int i = 0; i < computingCSLimit; i++) {
				costOnCm = CloudServicesComputingCost[candidate.getDatasetNum() + 1][i]; // �ߵļ��㻨��
				if (candidate.getCloudServiceID() != -1 && i != candidate.getCloudServiceID()) // �����ʼ�ڵ������ڵ�����̲�ͬ���������ݴ���ʼ���˳��ķ���
					costOnCm += candidate.getDataset().getSize() * CS[candidate.getCloudServiceID()].getcostT();
				if (target.getCloudServiceID() != -1 && i != target.getCloudServiceID()) // ��������ڵ�������ڵ�����̲�ͬ���������ݴ�����ڵ��˵������ڵ�ķ���
					costOnCm += target.getDataset().getPredecessors().get(0).getSize() * CS[i].getcostT(); // TODO
																											// ��������������һ�����ݼ���
				if (interCost > costOnCm) // �ҳ�������С���м䲽��
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
		// Ĭ�ϰѷ����̰��ռ�����ô�С�����ź����������㶨��1
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		CloudService CS1 = new CloudService(0.06 / 30, 0.12 * 24, 0.03, 1, bandwiths1);
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT��0.06���0.1
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
