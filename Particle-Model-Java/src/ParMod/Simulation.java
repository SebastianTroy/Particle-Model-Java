package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import tCode.RenderableObject;
import tools.RandTools;

/**
 * 
 * @author Sebastian Troy
 */
public class Simulation extends RenderableObject
	{
		// The simulation takes place within a cuboid with the following parameters
		int width; // The length of the simulation's short sides in meters
		int depth; // The length of the simualtion's long sides in meters
		int pace; // The time in minutes that pass for each simulation 'tick'

		LinkedList<Particle> particles; // Every particle being modelled is stored here.
		double particleSinkingRate /* ? */; // The distance a particle will sink through the water column in a single minute

		Chunk[/* x */][/* y */][/* z */] chunks; // The simulation is subdivided into chunks which contain localised information.
		double chunkSize; // Chunk size in meters

		/**
		 * 
		 * @param numParticles
		 *            - The number of particles to be modelled
		 */
		Simulation(int width, int depth, int pace, int numParticles, double chunkSize)
			{
				super();

				this.width = width;
				this.depth = depth;
				this.pace = pace;
				particles = new LinkedList<Particle>();
				// this.particleSinkingRate =... <-- make this a constant or variable?
				this.chunkSize = chunkSize;

				// allocate memory for the particles array
				for (int i = 0; i < numParticles; i++)
					{
						Particle p = new Particle();
						p.x = RandTools.getDouble(0, width);
						p.y = RandTools.getDouble(0, depth);
						p.z = RandTools.getDouble(0, width);
						particles.add(p);

					}
				// allocate memory for the chunks array
				chunks = new Chunk[(int) (width / chunkSize)][(int) (depth / chunkSize)][(int) (width / chunkSize)];

				for (int x = 0; x < chunks.length; x++)
					for (int y = 0; y < chunks[0].length; y++)
						for (int z = 0; z < chunks[0][0].length; z++)
							chunks[x][y][z] = new Chunk();

			}

		@Override
		protected void initiate()
			{}

		@Override
		public void tick(double secondsPassed) // TODO remove secondsPassed variable, the simulation should run in steps of 20 minutes a tick
			{
				// TODO remove this for final simulation
				double pace = this.pace * secondsPassed * 10; // Slows down the simulation so that it can be observed during development

				for (Particle p : particles)
					{
						// If particle hasn't sunk yet
						if (p.y < 50)
							{

								// Deal with random movements of particle
								 p.x += (RandTools.getDouble(-0.001, 0.001) * pace);
								 p.y += (RandTools.getDouble(-0.001, 0.001) * pace);
								 p.z += (RandTools.getDouble(-0.001, 0.001) * pace);

								// Make the particles sink
								p.y += pace * particleSinkingRate;

								// Apply local currents to particle's movements
								p.x += chunks[(int) (p.x * chunkSize)][(int) (p.y * chunkSize)][(int) (p.z * chunkSize)].xVel;
								p.y += chunks[(int) (p.x * chunkSize)][(int) (p.y * chunkSize)][(int) (p.z * chunkSize)].yVel;
								p.z += chunks[(int) (p.x * chunkSize)][(int) (p.y * chunkSize)][(int) (p.z * chunkSize)].zVel;

								// If a particle has left the boundaries of the water column,
								if (p.x > width)
									p.x -= width;
								else if (p.x < 0)
									p.x += width;

								if (p.y > depth) // If below the sea floor
									p.y = depth; // Return to the sea floor //TODO remove particle from simulation
								else if (p.y < 0)
									p.y = 0;

								if (p.z > width)
									p.z -= width;
								else if (p.z < 0)
									p.z += width;
							}
					}
				// Calls a method in the graphical output class that checks for user interaction with the simulation
				Main.graphicalOutput.tick(secondsPassed);
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
	}