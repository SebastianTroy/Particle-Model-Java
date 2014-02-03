package ParMod;

import tools.RandTools;

/**
 * This class represents a small chunk of the simulation. The simulation is divided into a matrix of chunks, each containing information about the specific
 * locality of the simulation.
 * 
 * @author Sebastian Troy
 * 
 */
public class Chunk
	{
		// Current velocity
		double xVel = 0, yVel = 0, zVel = 0;
		private double newX = 0, newY = 0, newZ = 0;
		private static final double maxVelocity = 0.005, rateOfChange = 0.001;

		public Chunk()
			{}

		public final void tick(int pace)
			{
				xVel += (newX - xVel) * (rateOfChange * pace);
				yVel += (newY - yVel) * (rateOfChange * pace);
				zVel += (newZ - zVel) * (rateOfChange * pace);

				// If the current has caught up with its new velocity, change its velocity again
				if (Math.abs(Math.abs(xVel) - Math.abs(newX)) < 0.001)
					newX = RandTools.getDouble(-maxVelocity, maxVelocity);
				if (Math.abs(Math.abs(yVel) - Math.abs(newY)) < 0.001)
					newY = RandTools.getDouble(-maxVelocity, maxVelocity);
				if (Math.abs(Math.abs(zVel) - Math.abs(newZ)) < 0.001)
					newZ = RandTools.getDouble(-maxVelocity, maxVelocity);
			}

	}
