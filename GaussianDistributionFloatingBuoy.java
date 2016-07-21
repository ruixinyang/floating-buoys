package floating_buoys;

/**
 * Floating buoy algorithm for estimating percentiles on samples generated from a Gaussian 
 * distribution.
 * 
 * @author Ruixin Yang
 */
public class GaussianDistributionFloatingBuoy extends UniformDistributionFloatingBuoy {
	
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
		GaussianDistributionFloatingBuoy buoys = new GaussianDistributionFloatingBuoy();
		int[] initialLocations = buoys.cast(numGroups, numTracers, range, castSize, numCast);
		
		// Connect the initial locations to get a buoy for each percentile
		int[] allBuoys = linkBuoys(initialLocations);
		
		// Output the estimates
		for (int index = 0; index < allBuoys.length; index++) {
			System.out.println(allBuoys[index]);
		}
	}
	
	/**
	 * Generate a random sample from a truncated (within 3 standard deviations) Gaussian 
	 * distribution [minimum, maximum).
	 * 
	 * @param minimum  the lower bound
	 * @param maximum  the upper bound
	 * @return  the random sample
	 */
	@Override
	public int sample(int minimum, int maximum) {
		int range = (maximum-1) - minimum;
		double std = (double) range / 6;
		double mean = (double) range / 2;
		int sample;
		
		// Resample until a value within 3 standard deviations is obtained
		do {
			sample = (int) Math.round(rand.nextGaussian() * std + mean);
		} while (sample < minimum || sample >= maximum);
		
		return sample;
	}
}