package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Iterator;
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
		Iterator<Particle> iter;
		final double particleSinkingRate = 0.0001; // The distance a particle will sink through the water column in a single minute

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

				// Create a new vector field
				vecField = new VectorField((int) (width / chunkSize), (int) (mixedLayerDepth / chunkSize));

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
				// Calls a method in the graphical output class that checks for user interaction with the simulation
				Main.graphicalOutput.tick(secondsPassed);

				// TODO remove this for final simulation
				double pace = this.pace * secondsPassed * 5; // Slows down the simulation so that it can be observed during development

				iter = particles.iterator();
				Particle p;
				while (iter.hasNext())
					{
						p = iter.next();
						// If particle hasn't sunk yet
						if (p.y < depth)// TODO remove sunk particles
							{
								// 1. Apply local currents to particle's movements
								// 2. Deal with random movements of particle
								// 3. If a particle has left the boundaries of the water column, correct it

								 p.x += 0.05 * (vecField.getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, Axis.x));
								//p.x += getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, 'x');
								p.x += (Rand.double_(-0.001, 0.001) * pace);

								if (p.x >= width)
									p.x -= width;
								if (p.x < 0)
									p.x += width;

								 p.y += 0.05 * (vecField.getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, Axis.y));
								//p.y += getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, 'y');
								p.y += (Rand.double_(-0.001, 0.001) * pace);
								p.y += pace * particleSinkingRate; // Make the particles sink

								if (p.y > depth) // If below the sea floor
									iter.remove(); // Remove from simulation
								else if (p.y < 0) // If above surface
									p.y = 0; // Return to surface

								 p.z += 0.05 * (vecField.getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, Axis.z));
								//p.z += getVelocityAt(p.x / chunkSize, p.y / chunkSize, p.z / chunkSize, 'z');
								p.z += (Rand.double_(-0.001, 0.001) * pace);

								if (p.z >= width)
									p.z -= width;
								if (p.z < 0)
									p.z += width;
							}
					}

				if (Rand.percent() > 90)
					vecField.stepSimulation();

				// TODO Add logic to chunks to allow tracking of nutrients/light levels e.t.c...
				for (int x = 0; x < chunks.length; x++)
					for (int y = 0; y < mixedLayerDepth; y++)
						for (int z = 0; z < chunks[0][0].length; z++)
							chunks[x][y][z].tick(this.pace);
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

		// Old method used to take snapshots of the watercolumn as they looked in older versions
//		private final double getVelocityAt(double x, double y, double z, char axis)
//			{
//				if (y >= mixedLayerDepth)
//					return 0;
//
//				int xi0 = (int) x;
//				int xi1 = xi0 + 1;
//				if (xi1 == chunks.length)
//					xi1 = 0;
//
//				int zi0 = (int) z;
//				int zi1 = zi0 + 1;
//				if (zi1 == chunks.length)
//					zi1 = 0;
//
//				// if y-source is above surface or below thermocline, restrict to surface/thermocline
//				if (y < 0.5)
//					y = 0.5;
//				else if (y >= chunks[0][0].length - 1.5)
//					y = chunks[0][0].length - 1.5;
//
//				int yi0 = (int) y;
//				int yi1 = yi0 + 1;
//
//				// Linear interpolation factors. Ex: 0.6 and 0.4
//				double xProp1 = x - xi0;
//				double xProp0 = 1.0 - xProp1;
//				double yProp1 = y - yi0;
//				double yProp0 = 1.0 - yProp1;
//				double zProp1 = z - zi0;
//				double zProp0 = 1.0 - zProp1;
//
//				/*
//				 * Here we find the velocity at a point in the middle of 8 chunks. We basically interpolate the velocity between all of the yAxis neighbours,
//				 * then we interpolate those values along the x axis, then finally, to combine them into a point we combine those velocities along the z axis.
//				 */
//				switch (axis)
//					{
//						case 'x':
//							return (zProp0 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi0].xVel + yProp1 * chunks[xi0][yi1][zi0].xVel) + xProp1
//									* (yProp0 * chunks[xi1][yi0][zi0].xVel + yProp1 * chunks[xi1][yi1][zi0].xVel)))
//									+ (zProp1 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi1].xVel + yProp1 * chunks[xi0][yi1][zi1].xVel) + xProp1
//											* (yProp0 * chunks[xi1][yi0][zi1].xVel + yProp1 * chunks[xi1][yi1][zi1].xVel)));
//						case 'y':
//							return (zProp0 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi0].yVel + yProp1 * chunks[xi0][yi1][zi0].yVel) + xProp1
//									* (yProp0 * chunks[xi1][yi0][zi0].yVel + yProp1 * chunks[xi1][yi1][zi0].yVel)))
//									+ (zProp1 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi1].yVel + yProp1 * chunks[xi0][yi1][zi1].yVel) + xProp1
//											* (yProp0 * chunks[xi1][yi0][zi1].yVel + yProp1 * chunks[xi1][yi1][zi1].yVel)));
//						case 'z':
//							return (zProp0 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi0].zVel + yProp1 * chunks[xi0][yi1][zi0].zVel) + xProp1
//									* (yProp0 * chunks[xi1][yi0][zi0].zVel + yProp1 * chunks[xi1][yi1][zi0].zVel)))
//									+ (zProp1 * (xProp0 * (yProp0 * chunks[xi0][yi0][zi1].zVel + yProp1 * chunks[xi0][yi1][zi1].zVel) + xProp1
//											* (yProp0 * chunks[xi1][yi0][zi1].zVel + yProp1 * chunks[xi1][yi1][zi1].zVel)));
//					}
//
//				return 0;
//
//				// SIMPLE, return velocity of current chunk
//				//
//				// Chunk chunk = chunks[(int) x][(int) y][(int) z];
//				// switch (axis)
//				// {
//				// case 'x':
//				// return chunk.xVel;
//				// case 'y':
//				// return chunk.yVel;
//				// case 'z':
//				// return chunk.zVel;
//				// }
//				// return 0;
//			}

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