package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import tCode.RenderableObject;
import tools.RandTools;

/**
 * 
 * @author Sebastian Troy
 */
public class Simulation extends RenderableObject
	{
		// The simulation takes place within a cuboid with the following parameters
		final int width = 1; // The length of the simulation's short sides in meters
		final int depth = 25; // The length of the simualtion's long sides in meters
		final int pace = 20; // The time in minutes that pass for each simulation 'tick'

		Particle[] particles; // Every particle being modelled is stored here.
		double particleSinkingRate /* ? */; // The distance a particle will sink through the water column in a single minute

		Chunk[/* x */][/* y */][/* z */] chunks; // The simulation is subdivided into chunks which contain localised information.
		double chunkSize; // Chunk size in meters, ensure that

		/**
		 * 
		 * @param numParticles
		 *            - The number of particles to be modelled
		 */
		Simulation(int numParticles, double chunkSize)
			{
				super();

				// allocate memory for the particles array
				particles = new Particle[numParticles];
				this.chunkSize = chunkSize;

				// allocate memory for the chunks array
				chunks = new Chunk[(int) (width / chunkSize)][(int) (depth / chunkSize)][(int) (width / chunkSize)];

				for (int x = 0; x < chunks.length; x++)
					for (int y = 0; y < chunks[0].length; y++)
						for (int z = 0; z < chunks[0][0].length; z++)
							chunks[x][y][z] = new Chunk();

			}

		@Override
		protected void initiate()
			{
				for (int i = 0; i < particles.length; i++)
					{
						particles[i] = new Particle();
						particles[i].x = RandTools.getDouble(0, width);
						particles[i].y = RandTools.getDouble(0, depth);
						particles[i].z = RandTools.getDouble(0, width);
					}
			}

		@Override
		public void tick(double secondsPassed) // TODO remove secondsPassed variable, the simulation should run in steps of 20 minutes a tick
			{
				// TODO remove this for final simulation
				double pace = this.pace * secondsPassed * 10; // Slows down the simulation so that it can be observed during development
				
				for (int i = 0; i < particles.length; i++)
					{
						// If particle hasn't sunk yet
						if (particles[i].y < 50)
							{

								// Deal with random movements of particle
								particles[i].x += (RandTools.getDouble(-0.001, 0.001) * pace);
								particles[i].y += (RandTools.getDouble(-0.001, 0.001) * pace);
								particles[i].z += (RandTools.getDouble(-0.001, 0.001) * pace);

								// Make the particles sink
								particles[i].y += pace * particleSinkingRate;

								// If a particle has left the boundaries of the water column,
								if (particles[i].x > width)
									particles[i].x -= width;
								else if (particles[i].x < 0)
									particles[i].x += width;

								if (particles[i].y > depth) // If below the sea floor
									particles[i].y = depth; // Return to the sea floor //TODO remove particle from simulation
								else if (particles[i].y < 0)
									particles[i].y = 0;

								if (particles[i].z > width)
									particles[i].z -= width;
								else if (particles[i].z < 0)
									particles[i].z += width;
							}
					}
			}

		/**
		 *
		 */
		@Override
		protected void render(Graphics2D g)
			{
				// Clear the canvas
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);

				// Visualise data in the water column, there are a number of views to chose from allowing different data to be viewed
				Main.graphicalOutput.drawWaterColumn(g);
			}

		@Override
		public final void keyPressed(KeyEvent event)
			{
				// If a key is pressed, find out which key...
				switch (event.getKeyChar())
					{
					// If that key is a number, change to the appropriate viewing mode
						case '1':
							Main.graphicalOutput.currentViewMode = GraphicalOutput.VIEW_PARTICLES;
							return; // Once that is done, ignore the rest of the options
						case '2':
							Main.graphicalOutput.currentViewMode = GraphicalOutput.VIEW_CURRENTS;
							return; // Once that is done, ignore the rest of the options
					}
			}

		/**
		 * This class represents a small chunk of the simulation. The simulation is divided into a matrix of chunks, each containing information about the
		 * specific locality of the simulation.
		 * 
		 * @author Sebastian Troy
		 * 
		 */
		private class Chunk
			{
				// Current velocity
				double xVel = 0, yVel = 0, zVel = 0;

			}
	}