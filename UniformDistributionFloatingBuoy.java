package floating_buoys;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

/**
 * Floating buoy algorithm for estimating percentiles on samples generated from an uniform 
 * distribution.
 * 
 * @author Ruixin Yang
 */
public class UniformDistributionFloatingBuoy {
	
	// Random instance
	protected static Random rand = new Random();
	
	// Format for outputting decimals
	protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	
	/**
	 * Main class.
	 * 
	 * @param args  no arguments necessary
	 */
	public static void main(String[] args) {
		// Number of elements spent tuning each cast
		int castSize = 10000;
		
		// Number of casts
		int numCast = 3;
		
		// Input range (0, range-1)
		int range = 1000000;
		
		// Number of tracer groups to cast (choose g s.t. 100 % g+1 = 0 and 0 < g <= 99)
		int numGroups = 4;
		
		// Number of tracers per group (choose t >= 2)
		int numTracers = 11;
		
		// Cast the tracer groups to obtain the initial buoy locations
		UniformDistributionFloatingBuoy buoys = new UniformDistributionFloatingBuoy();
		int[] initialLocations = buoys.cast(numGroups, numTracers, range, castSize, numCast);
		
		// Connect the initial locations to get a buoy for each percentile
		int[] allBuoys = linkBuoys(initialLocations);
		
		// Output the estimates
		for (int index = 0; index < allBuoys.length; index++) {
			System.out.println(allBuoys[index]);
		}
		
		// Measure the error
		DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
		System.out.println("Error: " + DECIMAL_FORMAT.format(100 * getError(allBuoys, range-1)) 
				+ "%.");
	}
	
	/**
	 * Cast a set of tracer groups and prune to obtain the initial buoy locations.
	 * 
	 * @param numGroups  number of tracer groups to cast
	 * @param numTracers  number of tracers in each group
	 * @param range  the maximum input value
	 * @param castSize  the number of input elements evaluating each cast
	 * @param numCast  the number of casts
	 * @return  the initial location (estimate) of each buoy (percentile)
	 */
	public int[] cast(int numGroups, int numTracers, int range, int castSize, int numCast) {
		// Initialize the set of tracers
		int[][] tracers = initializeTracers(numGroups, numTracers, range);
		
		// Generate the baseline
		int[][] baseline = generateBaseline(tracers);
		
		// Generate the percentiles
		double[] percentile = getPercentiles(numGroups);
		
		// Run each tracer group on random samples
		for (int cast = 0; cast < numCast; cast++) {
			for (int iter = 0; iter < castSize; iter++) {
				int input = sample(0, range);
				double updateThreshold = rand.nextDouble();
				
				for (int index = 0; index < numGroups; index++) {
					for (int estimator = 0; estimator < numTracers; estimator++) {
						if (input > tracers[index][estimator]) {
							if (updateThreshold < percentile[index]) {
								tracers[index][estimator]++;
							}
						} else if (input < tracers[index][estimator]) {
							if (updateThreshold > percentile[index]) {
								tracers[index][estimator]--;
							}
						}
					}
				}
			}
			
			// Repartition the tracers
			prune(tracers, baseline);
			baseline = generateBaseline(tracers);
		}
		
		// Return the estimate from the middle tracer
		int[] output = new int[numGroups+2];
		output[0] = 0;
		output[numGroups+1] = range-1;
		
		for (int index = 0; index < numGroups; index++) {
			output[index+1] = baseline[index][numTracers/2];
		}
		
		return output;
	}
	
	/**
	 * Initialize the set of tracer groups that are evenly spaced from 0 to range, inclusive.
	 * 
	 * @param numGroups  number of tracer groups to cast
	 * @param numTracers  number of tracers in each group
	 * @param range  the maximum input value
	 * @return  the initialized set of tracer groups
	 */
	public static int[][] initializeTracers(int numGroups, int numTracers, int range) {
		int[][] tracers = new int[numGroups][numTracers];
		
		for (int estimator = 0; estimator < tracers[0].length; estimator++) {
			tracers[0][estimator] = estimator * (range / (tracers[0].length-1));
			
			for (int index = 1; index < numGroups; index++) {
				tracers[index][estimator] = tracers[0][estimator];
			}
		}
		
		return tracers;
	}
	
	/**
	 * Generate a baseline copy of the current tracer estimates.
	 * 
	 * @param tracers  the tracer array
	 * @return  the baseline
	 */
	public static int[][] generateBaseline(int[][] tracers) {
		int[][] baseline = new int[tracers.length][tracers[0].length];
		
		for (int index = 0; index < tracers.length; index++) {
			for (int estimator = 0; estimator < tracers[0].length; estimator++) {
				baseline[index][estimator] = tracers[index][estimator];
			}
		}
		
		return baseline;
	}
	
	/**
	 * Get the target percentiles to estimate for casting.
	 * 
	 * @param numGroups  number of tracer groups to cast
	 * @return  the target percentiles
	 */
	public static double[] getPercentiles(int numGroups) {
		double[] percentile = new double[numGroups];
		
		for (int index = 0; index < percentile.length; index++) {
			percentile[index] = (index+1) * (1.0 / (percentile.length+1));
		}
		
		return percentile;
	}
	
	/**
	 * Generate a random sample from an uniform distribution [minimum, maximum).
	 * 
	 * @param minimum  the lower bound
	 * @param maximum  the upper bound
	 * @return  the random sample
	 */
	public int sample(int minimum, int maximum) {
		return rand.nextInt(maximum - minimum) + minimum;
	}
	
	/**
	 * Prune the tracers that are far from the actual percentile and repartition.
	 * 
	 * @param tracers  the tracer groups
	 * @param baseline  the baseline
	 */
	public static void prune(int[][] tracers, int[][] baseline) {
		for (int index = 0; index < tracers.length; index++) {
			int low = 0;
			int high = tracers[index].length-1;
			
			while (low + 1 < high && tracers[index][low+1] > baseline[index][low+1]) {
				low++;
			}
			
			while (high - 1 > low && tracers[index][high-1] < baseline[index][high-1]) {
				high--;
			}
			
			if (low != 0 || high != tracers[index].length-1) {
				int lowValue = tracers[index][low];
				int highValue = tracers[index][high];
				
				for (int estimator = 0; estimator < tracers[index].length; estimator++) {
					tracers[index][estimator] = estimator * 
							((highValue-lowValue) / (tracers[index].length-1)) + lowValue;
				}
			}
		}
	}
	
	/**
	 * Generate all 101 buoys from the initial locations.
	 * 
	 * @param initialLocations  the initial buoy locations
	 * @return  the estimate for each percentile (from 0 to 100 percentile)
	 */
	public static int[] linkBuoys(int[] initialLocations) {
		// Construct the complete set of 101 linked buoys (from 0 to 100 percentile)
		int[] allBuoys = new int[101];
		
		for (int index = 0; index < allBuoys.length; index++) {
			int groupWidth = 100 / (initialLocations.length-1);
			int modValue = index % groupWidth;
			
			if (modValue == 0) {
				// Initial location
				allBuoys[index] = initialLocations[index / groupWidth];
			} else {
				// Interpolate value for an intermediate buoy
				double location = (double) index / groupWidth;
				int low = initialLocations[(int) Math.floor(location)];
				int high = initialLocations[(int) Math.ceil(location)];
				allBuoys[index] = (int) ((location-((int) location)) * (high-low) + low);
			}
		}
		
		return allBuoys;
	}
	
	/**
	 * Get the percent error across all buoy estimates (percentiles).
	 * 
	 * @param allBuoys  the array of buoys
	 * @param range  the maximum input value
	 * @return  the percent error
	 */
	public static double getError(int[] allBuoys, int range) {
		long totalError = 0;
		long total = 0;
		
		for (int index = 0; index < allBuoys.length; index++) {
			int actual = (int) (((long) index * range) / (allBuoys.length-1));
			totalError += Math.abs((double) actual - allBuoys[index]);
			total += actual;
		}
		
		return (double) totalError / total;
	}
}