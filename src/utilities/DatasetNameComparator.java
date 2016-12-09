package utilities;

import java.util.Comparator;

/**
 * Comparator for sorting the datasets based on the dataset index number
 * @author Saichander
 *
 */
public class DatasetNameComparator  implements Comparator<Dataset> {

	public int compare(Dataset o1, Dataset o2) {
		int datasetNumber1 = Integer.parseInt(o1.getName().substring(1));
		int datasetNumber2 = Integer.parseInt(o2.getName().substring(1));
		return (datasetNumber1 - datasetNumber2);
	}
}
