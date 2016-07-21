package floating_buoys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Run the estimator on input arrays of size N containing elements {0, 1, ..., N-1} shuffled in 
 * random order.
 * 
 * @author Ruixin Yang
 */
public class ShuffledArray {
	
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
		int arraySize = 1000000;
		
		// Percentile to estimate
		double percentile = 0.75;
		
		// Estimate percentile value
		int estimate = 0;
		
		// Stopping threshold
		double threshold = 0.0001;
		
		// Percent error
		double error = Double.MAX_VALUE;
		
		// Number of input arrays
		int run = 1;
		
		// Run the estimator on shuffled input arrays
		while (Math.abs(error) > threshold) {
			int[] input = generateShuffledArray(arraySize);
			
			for (int index = 0; index < input.length; index++) {
				if (input[index] > estimate) {
					if (rand.nextDouble() < percentile) {
						estimate++;
					}
				} else if (input[index] < estimate) {
					if (rand.nextDouble() > percentile) {
						estimate--;
					}
				}
			}
			
			// Output
			DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
			int actual = (int) Math.round(percentile * (arraySize-1));
			error = (double) (actual - estimate) / actual;
			System.out.println("Run " + run++ + ":"
					+ " Estimate: " + estimate
					+ " Error: " + DECIMAL_FORMAT.format(100 * error) + "%");
		}
	}
	
	/**
	 * Generate an array of size N containing elements {0, 1, ..., N-1} shuffled in random order.
	 * 
	 * @param N  the size of the array
	 * @return  the generated array
	 */
	public static int[] generateShuffledArray(int N) {
		int[] array = new int[N];
		
		for (int index = 0; index < N-1; index++) {
			array[index] = index;
		}
		
		shuffleArray(array);
		return array;
	}
	
	/**
	 * Randomly shuffle an array using the Fisher-Yates algorithm.
	 * 
	 * @param array  the input array
	 */
	public static void shuffleArray(int[] array) {
		for (int index = array.length-1; index > 0; index--) {
			int randomIndex = rand.nextInt(index+1);
			int value = array[randomIndex];
			array[randomIndex] = array[index];
			array[index] = value;
		}
	}
}