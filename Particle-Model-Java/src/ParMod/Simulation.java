package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;

import tCode.RenderableObject;
import tools.RandTools;

/**
 * 
 * @author Sebastian Troy
 */
public class Simulation extends RenderableObject
	{
		// The simulation takes place within a cuboid with the following parameters
		int width = 1; // The length of the simulation's short sides
		int depth = 50; // The length of the simualtion's long sides

		// Every particle being modelled is stored here.
		Particle[] particles;
		
		// The simulation is subdivided into chunks which contain localised information.
		Chunk[/*x*/][/*y*/][/*z*/] chunks;

		/**
		 * 
		 * @param numParticles
		 */
		Simulation(int numParticles)
			{
				super();

				// allocate memory for the particles array
				particles = new Particle[numParticles];
				
				// allocate memory for the chunks array
				for (int x = 0; x < 100; x++)
					for (int y = 0; y < 500; y++)
						for (int z = 0; z < 100; z++)
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
		public void tick(double secondsPassed)
			{
				for (int i = 0; i < particles.length; i++)
					{
						// If particle hasn't sunk yet
						if (particles[i].y < 50)
							{

								// Deal with random movements of particle
								particles[i].x += (0.2 * secondsPassed);
								particles[i].y += (RandTools.getDouble(-0.1, 0.1) * secondsPassed);
								particles[i].z += (RandTools.getDouble(-0.1, 0.1) * secondsPassed);

								if (particles[i].x > 1)
									particles[i].x--;
								else if (particles[i].x < 0)
									particles[i].x++;

								if (particles[i].y > 50)
									particles[i].y = 50;
								else if (particles[i].y < 0)
									particles[i].y = 0;

								if (particles[i].z > 1)
									particles[i].z--;
								else if (particles[i].z < 0)
									particles[i].z++;

								// Deal with sinking of particles
								particles[i].y += (0.3 * secondsPassed);

								if (particles[i].y > 50)
									particles[i].y = 50;
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

				// TODO create options to view graphs of data

				// Visualise the particles in the water column
				Main.graphicalOutput.drawWaterColumn(g);
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

			}
	}