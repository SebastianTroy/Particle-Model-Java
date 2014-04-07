package ParMod;

import tools.Rand;
import tools.WindowTools;

/**
 * This class holds all of the information and methods required to compute a vector field for the model. It is based on the Navier-Stokes equations.
 * <p>
 * The velocity data held here corresponds directly to the {@link Chunk}s in the mixed layer, i.e. the velocity of the chunk at 0, 1, 32 == the velocity stored
 * at velocity[getIndex(0, 1, 32)]
 * 
 * @author Sebastian Troy
 */
public class VectorField
	{
		/**
		 * Used to keep track of, and pass around, the axis currently being worked on between functions
		 */
		enum Axis
			{
				x, y, z, undefined
			}

		/**
		 * The current velocities for each axis for each {@link Chunk} of the model. In total these represent a vector field.
		 */
		private double[] xVel, yVel, zVel;
		/**
		 * Temporary data storage used when calculating the velocities for the next step of the model.
		 */
		private double[] xVelP, yVelP, zVelP;

		/**
		 * Where: <br>
		 * xSize is width,<br>
		 * ySize is depth from surface to thermocline,<br>
		 * zSize is breadth &<br>
		 * layerSize is the number of elements in a single layer of the model (xSize * zSize).
		 */
		private int xSize, ySize, zSize, layerSize;

		/**
		 * An arbitrary value used to control how quickly pressure differences are resolved.
		 */
		private static final double TIMESTEP = 0.1;

		/**
		 * Simply allocates the appropriate amount of memory for the Vector field.
		 * 
		 * @param width
		 *            - The length of the short sides of the model in {@link Chunk}s
		 * @param depth
		 *            - The depth of the mixed layer in {@link Chunk}s
		 */
		VectorField(int width, int depth)
			{
				// establish the bounds of the VectorField
				xSize = zSize = width;
				ySize = depth;
				layerSize = xSize * zSize;

				// Allocate memory
				xVel = new double[width * width * depth];
				yVel = new double[width * width * depth];
				zVel = new double[width * width * depth];

				xVelP = xVel.clone();
				yVelP = yVel.clone();
				zVelP = zVel.clone();

				double vel = 0.1;

				for (int i = 0; i < xVel.length; i++)
					xVel[i] = Rand.double_(-vel, vel);
				for (int i = 0; i < yVel.length; i++)
					yVel[i] = Rand.double_(-vel, vel);
				for (int i = 0; i < zVel.length; i++)
					zVel[i] = Rand.double_(-vel, vel);

				for (int i = 0; i < 100; i++)
					stepSimulation();
			}

		final void addDisturbance(int radius, int chunkX, int chunkY, int chunkZ, double xVel, double yVel, double zVel)
			{
				// Limited to below the surface layer and above the thermocline layer
				// For each depth in the range
				for (int y = Math.max(1, chunkY - radius); y < Math.min(ySize - 2, chunkY + radius); y++)
					// For every chunk at that depth within the range
					for (int x = chunkX - radius; x < chunkX + radius; x++)
						for (int z = chunkZ - radius; z < chunkZ + radius; z++)
							{
								int xIndex = x;
								// y doesn't need to be checked as it doesn't wrap
								int zIndex = z;

								// wrap the coordinates if they go off the x or z axes
								if (xIndex < 0)
									xIndex += xSize;
								if (xIndex >= xSize)
									xIndex -= xSize;

								if (zIndex < 0)
									zIndex += zSize;
								if (zIndex >= zSize)
									zIndex -= zSize;

								int index = getIndex(xIndex, y, zIndex);
								this.xVel[index] += xVel;
								this.yVel[index] += yVel;
								this.zVel[index] += zVel;
								// System.out.println("(" + this.xVel[index] + ", " + this.yVel[index] + ", " + this.zVel[index] + ")");
							}
			}

		/**
		 * The velocity at the centre of a specific {@link Chunk}
		 * 
		 * @param x
		 *            - The x coordinate of the {@link Chunk}
		 * @param y
		 *            - The y coordinate of the {@link Chunk}
		 * @param z
		 *            - The z coordinate of the {@link Chunk}
		 * @param axis
		 *            - The {@link Axis} for which the required velocity will be calculated
		 * 
		 * @return - The velocity of the {@link Chunk} at the specified coordinates
		 */
		final double getVelocityAt(int x, int y, int z, Axis axis)
			{
				// If the requested velocity is below of the mixed layer
				if (y >= ySize)
					return 0;

				switch (axis)
					{
						case x:
							return xVel[getIndex(x, y, z)];
						case y:
							return yVel[getIndex(x, y, z)];
						case z:
							return zVel[getIndex(x, y, z)];
						case undefined:
						default:
							WindowTools.debugWindow("Cannot return velocity for undetermined axis");
							return 0;
					}
			}

		/**
		 * Method uses linear interpolation to calculate the exact velocity anywhere within the model despite stored data being grainy.
		 * 
		 * @param x
		 *            - The x coordinate at which the velocity will be calculated
		 * @param y
		 *            - The y coordinate at which the velocity will be calculated
		 * @param z
		 *            - The z coordinate at which the velocity will be calculated
		 * @param axis
		 *            - The {@link Axis} for which the required velocity will be calculated
		 * 
		 * @return - The velocity on the specified axis at the specified coordinates
		 */
		final double getVelocityAt(double x, double y, double z, Axis axis)
			{
				if (y >= ySize)
					return 0;

				double[] velocity = null;

				switch (axis)
					{
						case x:
							velocity = xVel;
							break;
						case y:
							velocity = yVel;
							break;
						case z:
							velocity = zVel;
							break;
						case undefined:
						default:
							WindowTools.debugWindow("Cannot get vecor without spoecifying an axis");
					}

				int xi0 = (int) x;
				int xi1 = xi0 + 1;
				if (xi1 == xSize)
					xi1 -= xSize;

				int zi0 = (int) z;
				int zi1 = zi0 + 1;
				if (zi1 == zSize)
					zi1 -= zSize;

				// if y-source is above surface or below thermocline, restrict to surface/thermocline
				if (y < 0.5)
					y = 0.5;
				else if (y > ySize - 1.5)
					y = ySize - 1.5;

				int yi0 = (int) y;
				int yi1 = yi0 + 1;

				// Linear interpolation factors. Ex: 0.6 and 0.4
				double xProp1 = x - xi0;
				double xProp0 = 1.0 - xProp1;
				double yProp1 = y - yi0;
				double yProp0 = 1.0 - yProp1;
				double zProp1 = z - zi0;
				double zProp0 = 1.0 - zProp1;

				/*
				 * Here we find the velocity at a point in the middle of 8 chunks. We basically interpolate the velocity between all of the yAxis neighbours,
				 * then we interpolate those values along the x axis, then finally, to combine them into a point we combine those velocities along the z axis.
				 */
				return (zProp0 * (xProp0 * (yProp0 * velocity[getIndex(xi0, yi0, zi0)] + yProp1 * velocity[getIndex(xi0, yi1, zi0)]) + xProp1
						* (yProp0 * velocity[getIndex(xi1, yi0, zi0)] + yProp1 * velocity[getIndex(xi1, yi1, zi0)])))
						+ (zProp1 * (xProp0 * (yProp0 * velocity[getIndex(xi0, yi0, zi1)] + yProp1 * velocity[getIndex(xi0, yi1, zi1)]) + xProp1
								* (yProp0 * velocity[getIndex(xi1, yi0, zi1)] + yProp1 * velocity[getIndex(xi1, yi1, zi1)])));
			}

		/**
		 * This updates all velocity values to represent those that would be expected in the next time slice of the model.
		 */
		final void stepSimulation()
			{
				/*
				 * First we need to move our old velocity data into temporary storage so we can use it to compute new velocity data without modifying it.
				 */
				double[] temp;

				temp = xVelP;
				xVelP = xVel;
				xVel = temp;

				temp = yVelP;
				yVelP = yVel;
				yVel = temp;

				temp = zVelP;
				zVelP = zVel;
				zVel = temp;

				/*
				 * Secondly we ensure the mixed layer is turning over
				 */
				// int i = getIndex(xSize / 2, 1, zSize / 2);
				// for (int y = 1; y < ySize - 1; y++)
				// {
				// yVelP[i] = -0.01;
				// i += layerSize;
				// }

				/*
				 * Thirdly we apply the following methods to calculate the new velocities for the next time step.
				 */
				advect(xVel, xVelP, xVelP, yVelP, zVelP, Axis.x);
				advect(yVel, yVelP, xVelP, yVelP, zVelP, Axis.y);
				advect(zVel, zVelP, xVelP, yVelP, zVelP, Axis.z);
				project(xVel, yVel, zVel, /* arbitrarily reused to prevent unnecessary new memory allocation -> */xVelP, yVelP);
			}

		/**
		 * Translates x, y & z coordinates into the single integer that is required to retrieve velocity data from the contiguous 1-dimensional arrays used to
		 * store it.
		 * 
		 * @param x
		 *            - The x coordinates of a {@link Chunk} in the model.
		 * @param y
		 *            - The y coordinates of a {@link Chunk} in the model.
		 * @param z
		 *            - The z coordinates of a {@link Chunk} in the model.
		 * 
		 * @return - The index of velocity data for the above {@link Chunk} coordinates.
		 */
		private final int getIndex(int x, int y, int z)
			{
				return x + (y * layerSize) + (z * xSize);
			}

		/**
		 * Iterate through the velocities of {@link Chunk}s at the edges or surfaces of the simulation and apply special rules to prevent unexpected behaviour.
		 * 
		 * @param axis
		 *            - The axis currently in need of correcting
		 * @param velocityData
		 *            - The velocity data for the axis
		 */
		private void correctEdgeCases(Axis axis, double[] velocityData)
			{
				switch (axis)
					{
						case x:
							// For each chunk on the faces at the ends of the xAxis, apply friction to prevent perpetual current forming.

							// to the depth of the thermocline
							for (int y = 0; y < ySize; y++)
								// for the breadth of the model
								for (int z = 0; z < zSize; z++)
									{
										velocityData[getIndex(0, y, z)] *= 0.95;
										velocityData[getIndex(xSize - 1, y, z)] *= 0.95;
									}

							// For each chunk at the surface and at the thermocline
							// for width of model
							for (int x = 0; x < xSize; x++)
								// for breadth of model
								for (int z = 0; z < zSize; z++)
									{
										// xVel = xVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;

						case y:
							// Prohibit up or down (y) velocities at the surface and at the thermocline

							// for the width of the model
							for (int x = 0; x < xSize; x++)
								// for the breadth of the model
								for (int z = 0; z < zSize; z++)
									{
										velocityData[getIndex(x, 0, z)] = 0;
										velocityData[getIndex(x, ySize - 1, z)] = 0;
									}
							break;

						case z:
							// For each chunk on the faces at the ends of the zAxis, apply friction to prevent perpetual current forming.

							// to the depth of the thermocline
							for (int y = 0; y < ySize; y++)
								// for the width of the model
								for (int x = 0; x < xSize; x++)
									{
										velocityData[getIndex(x, y, 0)] *= 0.95;
										velocityData[getIndex(x, y, zSize - 1)] *= 0.95;
									}

							// For each chunk at the surface and at the thermocline
							// for width of model
							for (int x = 0; x < xSize; x++)
								// for breadth of model
								for (int z = 0; z < zSize; z++)
									{
										// zVel = zVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;

						case undefined:
							// for each chunk at the surface and at the thermocline

							// for width of model
							for (int x = 0; x < xSize - 1; x++)
								// for breadth of model
								for (int z = 0; z < zSize - 1; z++)
									{
										// axesVel = axesVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;
					}
			}

		/**
		 * Calculate the new velocity for a particular axis by finding the point at which the old velocity originated and deriving the velocity from the centre
		 * of the {@link Chunk}s by linear interpolation between them.
		 * 
		 * @param dest
		 *            - Storage for velocities to be calculated for the current axes
		 * @param src
		 *            - Old velocities for current axis
		 * @param xVelocity
		 *            - Old xVelocities from the last time step
		 * @param yVelocity
		 *            - Old yVelocities from the last time step
		 * @param zVelocity
		 *            - Old zVelocities from the last time step
		 * @param axis
		 *            - the axis currently being worked on.
		 */
		private void advect(double[] dest, double[] src, double[] xVelocity, double[] yVelocity, double[] zVelocity, Axis axis)
			{
				// from just below the surface to the depth just above the thermocline
				for (int y = 1; y < ySize - 1; y++)
					// for width of model
					for (int x = 0; x < xSize; x++)
						// for breadth of model
						for (int z = 0; z < zSize; z++)
							{
								int k = getIndex(x, y, z);
								// Reverse velocity, since we are interpolating backwards
								// xSrc, ySrc & zSrc is the position of the source density.
								double xSrc = x - TIMESTEP * xVelocity[k];
								double ySrc = y - TIMESTEP * yVelocity[k];
								double zSrc = z - TIMESTEP * zVelocity[k];

								// if x-source is off either end of the axis, wrap to other end
								if (xSrc < 0)
									xSrc += xSize;
								else if (xSrc >= xSize)
									xSrc -= xSize;

								// if z-source is off either end of the axis, wrap to other end
								if (zSrc < 0)
									zSrc += zSize;
								else if (zSrc >= zSize)
									zSrc -= zSize;

								// The chunk where our x velocity originated and the chunk after it so we can interpolate
								int xi0 = (int) xSrc;
								int xi1 = xi0 + 1;
								if (xi1 == xSize)
									xi1 = 0;

								// The chunk where our z velocity originated and the chunk after it so we can interpolate
								int zi0 = (int) zSrc;
								int zi1 = zi0 + 1;
								if (zi1 == zSize)
									zi1 = 0;

								// if y-source is above surface or below thermocline, restrict to surface/thermocline
								if (ySrc < 0.5)
									ySrc = 0.5;
								else if (ySrc > ySize - 1.5)
									ySrc = ySize - 1.5;

								int yi0 = (int) ySrc;
								int yi1 = yi0 + 1;

								// Linear interpolation factors. Ex: 0.6 and 0.4
								double xProp1 = xSrc - xi0;
								double xProp0 = 1.0 - xProp1;
								double yProp1 = ySrc - yi0;
								double yProp0 = 1.0 - yProp1;
								double zProp1 = zSrc - zi0;
								double zProp0 = 1.0 - zProp1;

								/*
								 * Here we find the velocity at a point in the middle of 8 chunks. We basically interpolate the velocity between all of the
								 * yAxis neighbours, then we interpolate those values along the x axis, then finally, to combine them into a point we combine
								 * those velocities along the z axis.
								 */
								dest[k] = (zProp0 * (xProp0 * (yProp0 * src[getIndex(xi0, yi0, zi0)] + yProp1 * src[getIndex(xi0, yi1, zi0)]) + xProp1
										* (yProp0 * src[getIndex(xi1, yi0, zi0)] + yProp1 * src[getIndex(xi1, yi1, zi0)])))
										+ (zProp1 * (xProp0 * (yProp0 * src[getIndex(xi0, yi0, zi1)] + yProp1 * src[getIndex(xi0, yi1, zi1)]) + xProp1
												* (yProp0 * src[getIndex(xi1, yi0, zi1)] + yProp1 * src[getIndex(xi1, yi1, zi1)])));
							}

				correctEdgeCases(axis, dest);
			}

		/**
		 * @param xV
		 *            - the newly calculated xVelocities that need to have this step applied
		 * @param yV
		 *            - the newly calculated yVelocities that need to have this step applied
		 * @param zV
		 *            - the newly calculated zVelocities that need to have this step applied
		 * @param p
		 *            - an array that will be used to store pressure values
		 * @param div
		 *            - an array that will be used to store divergence values
		 */
		private void project(double[] xV, double[] yV, double[] zV, double[] p, double[] div)
			{
				double h = 1000.1;
				// from just below the surface to the depth just above the thermocline
				for (int k = layerSize; k < div.length - layerSize; k++)
					{
						// int k = getIndex(x, y, z);
						// Negative divergence
						div[k] = -0.5 * h * (xV[k + 1] - xV[k - 1] + yV[k + xSize] - yV[k - xSize] + zV[k + layerSize] - zV[k - layerSize]);
						// Pressure field
						p[k] = 0;
					}

				// Fill in data for surface and thermocline layers that we previously missed
				correctEdgeCases(Axis.undefined, div);
				correctEdgeCases(Axis.undefined, p);

				// Add vortices:
				// Assign values to p
				linearSolve(p, div, Axis.undefined);
				// Make p values interact with velocity data
				// from just below the surface to the depth just above the thermocline
				for (int k = layerSize; k < div.length - layerSize; k++)
					{
						xV[k] -= 0.5 * (p[k + 1] - p[k - 1]) / h;
						yV[k] -= 0.5 * (p[k + xSize] - p[k - xSize]) / h;
						zV[k] -= 0.5 * (p[k + layerSize] - p[k - layerSize]) / h;
					}

				// Do one final check for all velocities to finalise the values
				correctEdgeCases(Axis.x, xV);
				correctEdgeCases(Axis.y, yV);
				correctEdgeCases(Axis.z, zV);
			}

		/**
		 * 4-10 iterations gives fast results that are not noticeably inaccurate. For real accuracy, upwards of 20 is good. This function causes the leading
		 * edge of a flow of water to diverge, resulting in vortices.
		 * 
		 * @param dest
		 *            - The pressure data calculated during the {@link VectorField#project(double[], double[], double[], double[], double[])} step.
		 * @param src
		 *            - The divergence data calculated during the {@link VectorField#project(double[], double[], double[], double[], double[])} step.
		 * @param axis
		 *            - The current {@link Axis} data to which this method is being applied.
		 */
		private void linearSolve(double[] dest, double[] src, Axis axis)
			{
				double w = 2;
				for (int i = 0; i < 6; i++)
					{
						w -= 0.1;
						// to the depth of the thermocline
						for (int y = 1; y < ySize - 1; y++)
							// for width of model
							for (int x = 0; x < xSize; x++)
								// for breadth of model
								for (int z = 0; z < zSize; z++)
									{
										int k = getIndex(x, y, z);
										dest[k] += w * ((dest[k - 1] + dest[k + 1] + dest[k - xSize] + dest[k + xSize] + dest[k - layerSize] + dest[k + layerSize] + src[k]) / 6 - dest[k]);
									}
					}
				correctEdgeCases(axis, dest);
			}
	}