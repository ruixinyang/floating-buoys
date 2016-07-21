package floating_buoys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Run the estimator on samples generated from an uniform distribution.
 * 
 * @author Ruixin Yang
 */
public class UniformDistribution {
	
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
		int inputSize = 10000000;
		
		// Input range (0, range-1)
		int range = 1000000;
		
		// Percentile to estimate
		double percentile = 0.75;
		
		// Estimate percentile value
		int estimate = 0;
		
		// Run the estimator on random samples generated from an uniform distribution.
		for (int iter = 0; iter < inputSize; iter++) {
			int input = rand.nextInt(range);
			
			if (input > estimate) {
				if (rand.nextDouble() < percentile) {
					estimate++;
				}
			} else if (input < estimate) {
				if (rand.nextDouble() > percentile) {
					estimate--;
				}
			}		
			
			// Output
			DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
			int actual = (int) Math.round(percentile * (range-1));
			System.out.println("Run: " + iter
					+ " Estimate: " + estimate
					+ " Error: " + DECIMAL_FORMAT.format(
							100 * ((double) (actual - estimate) / actual)) + "%");
		}
	}
}