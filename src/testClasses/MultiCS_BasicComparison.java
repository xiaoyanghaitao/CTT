package testClasses;

import java.util.ArrayList;
import java.util.LinkedList;

import newClass.CTT_ver;
import newClass.CloudService;
import strategies.CTT_SP3;
import strategies.Improved_CTT_SP3;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;

public class MultiCS_BasicComparison {

	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG500.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		int blockSize = 250;
		int subgraphNum = 500/blockSize;
		DataDependencyGraph subgraphs[] = new DataDependencyGraph[subgraphNum];
		ArrayList<Dataset> l = new ArrayList<>(blockSize);
		int i = 0;
		for (Dataset d : graph.getDatasets()) {
			l.add(d);
			if (l.size() == blockSize) {
				l.get(0).setPredecessors(new ArrayList<>());
				l.get(blockSize-1).setSuccessors(new ArrayList<>());
				subgraphs[i++] = new DataDependencyGraph(l);
				l = new ArrayList<>(blockSize);
			}
		}
		long startTime = System.currentTimeMillis();
		double result = 0;
		double[] bandwiths0 = { 0, 0.128, 0.128 };
		double[] bandwiths1 = { 0.128, 0, 0.128 };
		double[] bandwiths2 = { 0.128, 0.128, 0.0 };
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		CloudService CS1 = new CloudService(0.06 / 30, 0.12 * 24, 0.03, 1, bandwiths1);
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT”…0.06±‰≥…0.1
		CloudService[] CS = new CloudService[3];
		CS[0] = CS0;
		CS[1] = CS1;
		CS[2] = CS2;
		int m = 3;
		for (i = 0; i < subgraphNum; i++) {
			result += CTT_SP3.computeCttCost(subgraphs[i], m, CS);
//			result += Improved_CTT_SP3.computeCttCost(subgraphs[i], m, CS);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		System.out.println("totalcost:" + result);
		
//		LinkedList<CTT_ver> verList = new LinkedList<>();
//		for (Dataset d : graph.getDatasets()) {
//			if (d.isStored()) {
////				 System.out.println(d.getName() + ":" + d.getcsid());
//				verList.add(new CTT_ver(d, d.getcsid()));
//			}
//		}
//		int size = graph.getDatasets().size();
//		for (i=0; i<size; i++) {
//			ArrayList<Dataset> al;
//			if(i != size - 1) {
//				al = new ArrayList<>();
//				al.add(graph.getDatasets().get(i+1));
//				graph.getDatasets().get(i).setSuccessors(al);
//			}
//			if(i != 0) {
//				al = new ArrayList<>();
//				al.add(graph.getDatasets().get(i-1));
//				graph.getDatasets().get(i).setPredecessors(al);
//			}
//		}
//		result = 0.0;
//		CTT_ver start = new CTT_ver();
//		CTT_ver end = new CTT_ver();
//		start.getdataset().addSuccessor(graph.getDatasets().get(0));
//		end.getdataset().setName("end");
//		ArrayList<Dataset> al = new ArrayList<>();
//		al.add(end.getdataset());
//		graph.getDatasets().get(size - 1).setSuccessors(al);
//		verList.add(end);
//		CTT_SP3.m = m;
//		for (CTT_ver v : verList) {
//			result += CTT_SP3.getEdgeCost(start, v, CS);
//			start = v;
//		}
//		System.out.println("totalcost:" + result);
		
		System.out.println("Program is Over");
	}

}
