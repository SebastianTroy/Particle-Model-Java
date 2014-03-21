package ParMod;

/**
 * This class holds all of the information and methods required to compute a vector field for the model. It is based on the Navier-Stokes equations and was
 * 
 * @author Sebastian Troy
 */
public class VectorField
	{
		/**
		 * Keeps track of the axis currently being worked on
		 */
		private enum Axis
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

		// TODO constructor

		/**
		 * This updates all velocity values to represent those that would be expected in the next time slice of the model.
		 */
		public final void stepSimulation()
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
				 * Secondly we apply the following methods to calculate the new velocities for the next time step.
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
				return x + (y * xSize) + (z * xSize * ySize);
			}

		/**
		 * TODO
		 * 
		 * @param axis
		 * @param velocityData
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
							for (int x = 1; x < xSize - 1; x++)
								// for breadth of model
								for (int z = 1; z < zSize - 1; z++)
									{
										// xVel = xVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;

						case y:
							// Prohibit motion up or down motion at the surface and at the thermocline

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
							for (int x = 1; x < xSize - 1; x++)
								// for breadth of model
								for (int z = 1; z < zSize - 1; z++)
									{
										// zVel = zVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;

						case undefined:
							// for each chunk at the surface and at the thermocline

							// for width of model
							for (int x = 1; x < xSize - 1; x++)
								// for breadth of model
								for (int z = 1; z < zSize - 1; z++)
									{
										// axesVel = axesVel of neighbour below/above
										velocityData[getIndex(x, 0, z)] = velocityData[getIndex(x, 1, z)];
										velocityData[getIndex(x, ySize - 1, z)] = velocityData[getIndex(x, ySize - 2, z)];
									}
							break;
					}
			}

		/**
		 * TODO
		 * 
		 * @param dest
		 * @param src
		 * @param xVelocity
		 * @param yVelocity
		 * @param zVelocity
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

								int xi0 = (int) xSrc;
								int xi1 = xi0 + 1;
								if (xi1 == xSize)
									xi1 -= xSize;

								int zi0 = (int) zSrc;
								int zi1 = zi0 + 1;
								if (zi1 == zSize)
									zi1 -= zSize;

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
								double zProp1 = zSrc - yi0;
								double zProp0 = 1.0 - zProp1;

								dest[k] = xProp0 * (yProp0 * src[getK(xi0, yi0)] + yProp1 * src[getK(xi0, yi1)]) 
										+ xProp1 * (yProp0 * src[getK(xi1, yi0)] + yProp1 * src[getK(xi1, yi1)]);
								// TODO work out how to add z axis to this ^
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
				double h = 0.1;
				// from just below the surface to the depth just above the thermocline
				for (int y = 1; y < ySize - 1; y++)
					// for width of model
					for (int x = 0; x < xSize; x++)
						// for breadth of model
						for (int z = 0; z < zSize; z++)
							{
								int k = getIndex(x, y, z);
								// Negative divergence ~ TODO should 0.5 change now 2 more chunks are involved?
								div[k] = -0.5 * h * (xV[k + 1] - xV[k - 1] + yV[k + xSize] - yV[k - xSize] + zV[k + layerSize] - zV[k - layerSize]);
								// Pressure field
								p[k] = 0;
							}

				// Fill in data for surface and thermocline layers that we previously missed
				correctEdgeCases(Axis.undefined, div);
				correctEdgeCases(Axis.undefined, p);

				// Diverge flow at leading edge of currents
				linearSolve(p, div, Axis.undefined);

				// to the depth of the thermocline
				for (int y = 1; y < ySize - 1; y++)
					// for width of model
					for (int x = 0; x < xSize; x++)
						// for breadth of model
						for (int z = 0; z < zSize; z++)
							{
								int k = getIndex(x, y, z);
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
										dest[k] += w * ((dest[k - 1] + dest[k + 1] + dest[k - xSize] + dest[k + xSize] + dest[k - layerSize] + dest[k + layerSize] + src[k]) / 4 - dest[k]);
									}
						correctEdgeCases(axis, dest);
					}
			}
	}