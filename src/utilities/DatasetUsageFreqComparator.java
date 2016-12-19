package utilities;

import java.util.Comparator;

/**
 * Comparator for comparing the usage frequencies of Dataset. Used in Often Used
 * Storage Strategy.
 * 
 * @author Saichander
 * 
 */
public class DatasetUsageFreqComparator implements Comparator<Dataset> {

	public int compare(Dataset o1, Dataset o2) {
		return (new Float(o2.getUsageFrequency())).compareTo(new Float(o1
				.getUsageFrequency()));
	}

}
