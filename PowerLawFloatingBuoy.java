package floating_buoys;

import java.util.Random;

/**
 * Floating buoy algorithm for estimating percentiles on samples generated from a truncated 
 * power-law distribution.
 * 
 * @author Ruixin Yang
 */
public class PowerLawFloatingBuoy extends UniformDistributionFloatingBuoy {
	
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
		int numGroups = 99;
		
		// Number of tracers per group (choose t >= 2)
		int numTracers = 11;
		
		// Cast the tracer groups to obtain the initial buoy locations
		PowerLawFloatingBuoy buoys = new PowerLawFloatingBuoy();
		int[] initialLocations = buoys.cast(numGroups, numTracers, range, castSize, numCast);
		
		// Connect the initial locations to get a buoy for each percentile
		int[] allBuoys = linkBuoys(initialLocations);
		
		// Output the estimates
		for (int index = 0; index < allBuoys.length; index++) {
			System.out.println(allBuoys[index]);
		}
	}
	
	/**
	 * Generate a random sample from a truncated power-law distribution P(x) = C*x^n for x in 
	 * [0.00001, 10] scaled to [minimum, maximum].
	 * 
	 * @param minimum  the lower bound
	 * @param maximum  the upper bound
	 * @return  the random sample
	 */
	@Override
	public int sample(int minimum, int maximum) {
		Random rand = new Random();
		double lower = 0.00001;
		double upper = 10;
		double n = -2;
		double sample = Math.pow((rand.nextDouble() * (Math.pow(upper, n+1)-Math.pow(lower, n+1)) 
				+ Math.pow(lower, n+1)), (1.0/(n+1)));
		return (int) Math.round(((sample-lower) / (upper-lower)) * (maximum-1));
	}
}