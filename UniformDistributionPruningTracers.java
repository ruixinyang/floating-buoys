package floating_buoys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Run multiple "tracer" estimators with pruning on samples generated from an uniform distribution.
 * 
 * @author Ruixin Yang
 */
public class UniformDistributionPruningTracers {
	
	// Random instance
	private static Random rand = new Random();

	// Format for outputting decimals
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	
	/**
	 * Main class.
	 * 
	 * @param args  no arguments necessary
	 */
	public static void main(String[] args) {
		// Number of input values
		int inputSize = 1000000;
		
		// Input range (0, range-1)
		int range = 1000000;
		
		// Percentile to estimate
		double percentile = 0.75;
		
		// Number of tracers
		int numTracers = 11;
		
		// Tracer estimators
		int[] tracers = initializeTracers(numTracers, range);
		
		// Baseline copy
		int[] baseline = generateBaseline(tracers);
		
		// Pruning frequency
		int frequency = 100000;
		
		// Run the estimator on random samples generated from an uniform distribution.
		for (int iter = 0; iter < inputSize; iter++) {
			// Prune before the run, if necessary
			if (iter != 0 && iter % frequency == 0) {
				prune(tracers, baseline);
				baseline = generateBaseline(tracers);
			}
			
			int input = rand.nextInt(range);
			
			for (int estimator = 0; estimator < numTracers; estimator++) {
				if (input > tracers[estimator] && tracers[estimator] < Integer.MAX_VALUE) {
					if (rand.nextDouble() < percentile) {
						tracers[estimator]++;
					}
				} else if (input < tracers[estimator] && tracers[estimator] > 0) {
					if (rand.nextDouble() > percentile) {
						tracers[estimator]--;
					}
				}
			}
			
			// Output
			DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
			int actual = (int) Math.round(percentile * (range-1));
			System.out.print("Actual: " + actual);
			
			for (int estimator = 0; estimator < numTracers; estimator++) {
				System.out.print(" [" + estimator + "] " + tracers[estimator] + ", "
						+ DECIMAL_FORMAT.format(
								100 * ((double) (actual - tracers[estimator]) / actual)) + "%");
			}
			
			System.out.print("\n");
		}
	}
	
	/**
	 * Initialize tracer estimators to be evenly spaced from 0 to range, inclusive.
	 * 
	 * @param numTracers  number of tracers
	 * @param range  the maximum input value
	 * @return  the initialized tracers
	 */
	public static int[] initializeTracers(int numTracers, int range) {
		int[] tracers = new int[numTracers];
		
		for (int estimator = 0; estimator < tracers.length; estimator++) {
			tracers[estimator] = estimator * (range / (tracers.length-1));
		}
		
		return tracers;
	}
	
	/**
	 * Generate a baseline copy of the current tracer estimates.
	 * 
	 * @param tracers  the tracer estimators
	 * @return  the baseline
	 */
	public static int[] generateBaseline(int[] tracers) {
		int[] baseline = new int[tracers.length];
		
		for (int estimator = 0; estimator < tracers.length; estimator++) {
			baseline[estimator] = tracers[estimator];
		}
		
		return baseline;
	}
	
	/**
	 * Prune the tracers that are far from the actual percentile and repartition.
	 * 
	 * @param tracers  the tracer estimators
	 * @param baseline  the baseline
	 */
	public static void prune(int[] tracers, int[] baseline) {
		int low = 0;
		int high = tracers.length-1;
		
		while (low + 1 < high && tracers[low+1] > baseline[low+1]) {
			low++;
		}
		
		while (high - 1 > low && tracers[high-1] < baseline[high-1]) {
			high--;
		}
		
		if (low != 0 || high != tracers.length-1) {
			int lowValue = tracers[low];
			int highValue = tracers[high];
			
			for (int estimator = 0; estimator < tracers.length; estimator++) {
				tracers[estimator] = estimator * ((highValue-lowValue) / (tracers.length-1))
						+ lowValue;
			}
		}
	}
}