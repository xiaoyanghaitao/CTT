
package strategies;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import newClass.*;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import utilities.DDGGenerator;
import utilities.DataDependencyGraph;
import utilities.Dataset;
import utilities.DatasetNameComparator;

public class test {

	/**
	 *clear the datasets status
	 */
	private static void resetDatasetStatus(DataDependencyGraph graph) {
		List<Dataset> datasets = graph.getDatasets();
		Collections.sort(datasets, new DatasetNameComparator());

		for (Dataset aDataset : datasets) {
			aDataset.clear();
		}

	}

	public void testStart(String folderPath) {
		System.out.println("test start------");
		PrintStream os = System.out;
		PrintStream ose = System.err;
		PrintStream newP;
        //create a new txt file
		try {
			newP = new PrintStream("testResult.txt");
			System.setOut(newP);
			System.setErr(newP);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
		System.out.println("test start");
		testEightMethod("D:/workspace/ALgorithm/local optimal v4(0808)/local optimal v4/xmlFolder/LineXML/testlineDDG500.xml");
		System.out.println("test end");
		System.setOut(os);
		System.setErr(ose);
		System.out.println("test end------");
	}
	
	public void testEightMethod(String fileName) {
		int percent = 0;
		DDGGenerator.setFilePath(fileName);
		DataDependencyGraph graph = DDGGenerator.getDDG();
        System.out.println("The used DDG is ： "+ fileName);
		long startmxbe=0;
		long endmxbe=0;
		long timemxbe=0;
		double cost = 0.0;
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		
		//使用三个cloud service 进行测试
		double[] bandwiths0={0,0.128,0.128};
		double[] bandwiths1={0.128,0,0.128};
		double[] bandwiths2={0.128,0.128,0.0};
		CloudService CS0=new CloudService(0.1/30,0.11*24,0.01,0,bandwiths0);
		CloudService CS1=new CloudService(0.05/30,0.15*24,0.15,1,bandwiths1);//costT由0.06变成0.1
		CloudService CS2=new CloudService(0.06/30,0.12*24,0.03,2,bandwiths2);
		CloudService[] CS=new CloudService[3];
		CS[0]=CS0;
		CS[1]=CS1;
		CS[2]=CS2;
		int m=3;
		// ==============1 all delete ==========================
		System.out.println("==============1 all delete ================");
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		cost = AllDelete.computeAllDeleteCost(graph,CS[0]);
		System.out.println("Total Cost Rate for All delete Strategy: " + cost);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		timemxbe=endmxbe-startmxbe;
		System.out.println("Execution Timemxbean : " + timemxbe);
		resetDatasetStatus(graph);
		
		// ==============2 all save=========================
		System.out.println("==============2  all save================");
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		cost = AllSave.computeAllSaveCost(graph,CS[0]);
		System.out.println("Total Cost Rate for All save Strategy: " + cost);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		timemxbe=endmxbe-startmxbe;
		System.out.println("Execution Timemxbean : " + timemxbe);
		resetDatasetStatus(graph);
		

		
		// ==============3 OftenUsed============================
		System.out.println("==============3 OftenUsed================");
		OftenUsedStorageStrategy.setSelectionBox(percent);
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		cost = OftenUsedStorageStrategy.computeOUSCostRate(graph,CS[0]);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		System.out.println("Total Cost Rate for Often Used Strategy: " + cost);
		timemxbe=endmxbe-startmxbe;
		System.out.println("Execution Timemxbean : " + timemxbe);
		resetDatasetStatus(graph);
		
		// ==============4 HighGenerationCost============================
		System.out.println("==============4 HighGenerationCost================");
		HighGenerationCostStrategy.setSelectionBox(percent);
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		cost = HighGenerationCostStrategy.computeHGCSCostRate(graph,CS[0]);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		System.out.println("Total Cost Rate for High Generation Cost Strategy: "+ cost);
		timemxbe=endmxbe-startmxbe;
		System.out.println("Execution Timemxbean : " + timemxbe);
		resetDatasetStatus(graph);
		
		// ==============5 data dependence strategy=====================
		System.out.println("==============5 data dependence strategy================");
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		cost = DataDependencyStrategy.computeDDSCostRate(graph,CS[0]);
		endmxbe=threadMXBean.getCurrentThreadCpuTime();
		System.out.println("Total Cost Rate for Data Dependency Strategy: "+ cost);
		timemxbe=endmxbe-startmxbe;
		System.out.println("The algorithm execution time is : " + timemxbe);
		resetDatasetStatus(graph);
		
		
		// =============6 overall CTT_SP===============
		System.out.println("==========6 overall CTT_SP===========");
		startmxbe = threadMXBean.getCurrentThreadCpuTime();
		CTT_SP.computeCttCost(graph,CS[0]);
        resetDatasetStatus(graph);
		
		
		
		// ==============7 CTT_SP2 strategy=====================
		System.out.println("==============7 CTT_SP2 strategy================");
		//set parament values##########################S###############################################
        CTT_SP2.computeCttCost(graph,m,CS);
      	resetDatasetStatus(graph);
      	
        // ==============8 CTT_SP3 strategy=====================
		System.out.println("==============8 CTT_SP3 strategy================");
        CTT_SP3.computeCttCost(graph, m,CS);
        resetDatasetStatus(graph);
	}

	//===========================main===========================
	public static void main(String[] args) {
		test et = new test();
		et.testStart(" ");
	}

}
