package ParMod;

/**
 * 
 * @author Sebastian Troy
 */
public class Particle
	{
		double x, y, z;

		/**
		 * This method uses the particles x, y & z coordinates to work out which chunk it is in
		 */
		public final int[] getChunkCoordinates()
			{
				int[] coordinates = { (int) (x / Main.sim.chunkSize), (int) (y / Main.sim.chunkSize), (int) (z / Main.sim.chunkSize) };

				return coordinates;
			}
	}
