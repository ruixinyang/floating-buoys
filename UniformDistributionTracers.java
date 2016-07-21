package floating_buoys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Run multiple "tracer" estimators on samples generated from an uniform distribution.
 * 
 * @author Ruixin Yang
 */
public class UniformDistributionTracers {
	
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
		
		// Run the estimator on random samples generated from an uniform distribution.
		for (int iter = 0; iter < inputSize; iter++) {
			int input = rand.nextInt(range);
			
			for (int estimator = 0; estimator < numTracers; estimator++) {
				if (input > tracers[estimator]) {
					if (rand.nextDouble() < percentile) {
						tracers[estimator]++;
					}
				} else if (input < tracers[estimator]) {
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
}