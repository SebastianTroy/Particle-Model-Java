package ParMod;

import java.awt.Color;

/**
 * 
 * @author Sebastian Troy
 */
public class Particle implements Comparable<Particle>
	{
		double x, y, z;
		int colour = 0;

		/**
		 * This method uses the particles x, y & z coordinates to work out which chunk it is in
		 */
		public final int[] getChunkCoordinates()
			{
				int[] coordinates = { (int) (x / Main.sim.chunkSize), (int) (y / Main.sim.chunkSize), (int) (z / Main.sim.chunkSize) };

				return coordinates;
			}

		public void calculateColour()
			{
				colour = (int) (30 + ((((1 - x) + z) / 2) * 225));
			}

		@Override
		public int compareTo(Particle p)
			{
				return colour - p.colour;
			}
	}
