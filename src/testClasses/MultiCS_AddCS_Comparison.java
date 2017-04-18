package testClasses;

import newClass.CloudService;
import strategies.CTT_SP3;
import strategies.Improved_CTT_SP3;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;

public class MultiCS_AddCS_Comparison {

	public static void main(String[] args) {
		DDGGenerator.setFilePath("xmlFolder/LineXML/testlineDDG100.xml");
		DataDependencyGraph graph = DDGGenerator.getDDG();
		long startTime = System.currentTimeMillis();
		int m = 10;
		double result;
		double[] bandwiths0 = { 0, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128 };
		double[] bandwiths1 = { 0.128, 0, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128 };
		double[] bandwiths2 = { 0.128, 0.128, 0, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128, 0.128 };
		double[] bandwiths3 = { 0.22, 0.22, 0.22, 0, 0.22, 0.22, 0.22, 0.22, 0.22, 0.22 };
		double[] bandwiths4 = { 0.14, 0.14, 0.14, 0.14, 0, 0.14, 0.14, 0.14, 0.14, 0.14 };
		double[] bandwiths5 = { 0.135, 0.135, 0.135, 0.135, 0.135, 0, 0.135, 0.135, 0.135, 0.135 };
		double[] bandwiths6 = { 0.15, 0.15, 0.15, 0.15, 0.15, 0.15, 0, 0.15, 0.15, 0.15 };
		double[] bandwiths7 = { 0.16, 0.16, 0.16, 0.16, 0.16, 0.16, 0.16, 0, 0.16, 0.16 };
		double[] bandwiths8 = { 0.18, 0.18, 0.18, 0.18, 0.18, 0.18, 0.18, 0.18, 0, 0.18 };
		double[] bandwiths9 = { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0 };
		CloudService CS0 = new CloudService(0.1 / 30, 0.11 * 24, 0.01, 0, bandwiths0);
		CloudService CS1 = new CloudService(0.06 / 30, 0.12 * 24, 0.03, 1, bandwiths1);
		CloudService CS2 = new CloudService(0.05 / 30, 0.15 * 24, 0.15, 2, bandwiths2);// costT”…0.06±‰≥…0.1
		CloudService[] CS = new CloudService[m];
		CS[0] = CS0;
		CS[1] = CS1;
		CS[2] = CS2;
		CS[3] = new CloudService(0.08 / 30, 0.09 * 24, 0.05, 3, bandwiths3);
		CS[4] = new CloudService(0.07 / 30, 0.13 * 24, 0.06, 4, bandwiths4);
		CS[5] = new CloudService(0.07 / 30, 0.15 * 24, 0.03, 5, bandwiths5);
		CS[6] = new CloudService(0.06 / 30, 0.12 * 24, 0.07, 6, bandwiths6);
		CS[7] = new CloudService(0.09 / 30, 0.13 * 24, 0.02, 7, bandwiths7);
		CS[8] = new CloudService(0.05 / 30, 0.12 * 24, 0.06, 8, bandwiths8);
		CS[9] = new CloudService(0.04 / 30, 0.16 * 24, 0.08, 9, bandwiths9);
//		result = CTT_SP3.computeCttCost(graph, m, CS);
		result = Improved_CTT_SP3.computeCttCost(graph, m, CS);
		long endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (endTime - startTime) + "ms");
		System.out.println("totalcost:" + result);
		System.out.println("Program is Over");
	}

}
