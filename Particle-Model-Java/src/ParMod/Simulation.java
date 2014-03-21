package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

import ParMod.VectorField.Axis;
import tCode.RenderableObject;
import tComponents.utils.events.TScrollEvent;
import tools.Rand;

/**
 * 
 * @author Sebastian Troy
 */
public class Simulation extends RenderableObject
	{
		// The simulation takes place within a cuboid with the following parameters
		int width; // The length of the simulation's short sides in meters
		int depth; // The length of the simualtion's long sides in meters
		int mixedLayerDepth; // The y value for the last chunk within the mixed surface layer
		int pace; // The time in minutes that pass for each simulation 'tick'

		LinkedList<Particle> particles; // Every particle being modelled is stored here.
		final double particleSinkingRate = 0.0005; // The distance a particle will sink through the water column in a single minute

		VectorField vecField;
		Chunk[/* x */][/* y */][/* z */] chunks; // The simulation is subdivided into chunks which contain localised information.
		double chunkSize; // Chunk size in meters

		/**
		 * 
		 * @param numParticles
		 *            - The number of particles to be modelled
		 */
		Simulation(int width, int depth, int mixedLayerDepth, int pace, int numParticles, double chunkSize)
			{
				this.width = width;
				this.depth = depth;
				this.mixedLayerDepth = (int) (mixedLayerDepth / chunkSize);
				this.pace = pace;
				particles = new LinkedList<Particle>();
				this.chunkSize = chunkSize;

				vecField = new VectorField(width, depth);

				// allocate memory for the particles array
				for (int i = 0; i < numParticles; i++)
					{
						Particle p = new Particle();
						p.x = Rand.double_(0, width);
						p.y = Rand.double_(0, depth);
						p.z = Rand.double_(0, width);
						// p.x = width / 2;
						// p.y = 0;
						// p.z = width / 2;
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
			{
				// Initiate a new Graphical output that will display the simulation
				Main.graphicalOutput = new GraphicalOutput();
			}

		@Override
		public void tick(double secondsPassed)
			{
				// TODO remove this for final simulation
				double pace = this.pace * secondsPassed * 5; // Slows down the simulation so that it can be observed during development

				// TODO Add logic to chunks to allow tracking of nutrients/light levels e.t.c...
				// for (int x = 0; x < chunks.length; x++)
				// for (int y = 0; y < mixedLayerDepth; y++)
				// for (int z = 0; z < chunks[0][0].length; z++)
				// chunks[x][y][z].tick(this.pace);

				for (Particle p : particles)
						// If particle hasn't sunk yet
						if (p.y < depth)// TODO remove sunk particles
							{
								// Apply local currents to particle's movements
								p.x += (vecField.getVelocityAt(p.x, p.y, p.z, Axis.x));
								p.y += (vecField.getVelocityAt(p.x, p.y, p.z, Axis.y));
								p.z += (vecField.getVelocityAt(p.x, p.y, p.z, Axis.z));

								// Deal with random movements of particle
								p.x += (Rand.double_(-0.001, 0.001) * pace);
								p.y += (Rand.double_(-0.001, 0.001) * pace);
								p.z += (Rand.double_(-0.001, 0.001) * pace);

								// Make the particles sink
								p.y += pace * particleSinkingRate;

								// If a particle has left the boundaries of the water column,
								if (p.x >= width)
									p.x -= width;
								else if (p.x < 0)
									p.x += width;

								if (p.y > depth) // If below the sea floor
									p.y = depth; // Return to the sea floor //TODO remove particle from simulation
								else if (p.y < 0)
									p.y = 0;

								if (p.z >= width)
									p.z -= width;
								else if (p.z < 0)
									p.z += width;
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

		// The following methods pass user input to the GraphicalOutput class so it knows what to show:

		@Override
		public final void keyPressed(KeyEvent event)
			{
				Main.graphicalOutput.keyPressed(event);
			}

		@Override
		public final void tScrollEvent(TScrollEvent event)
			{
				if (Main.graphicalOutput != null)
					Main.graphicalOutput.tScrollEvent(event);
			}
	}