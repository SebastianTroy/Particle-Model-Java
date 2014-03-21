package ParMod;

/**
 * This class 
 * 
 * @author Sebastian Troy
 */
public class VectorField
	{
		/**
		 * Keeps track of the axis currently being worked on
		 */
		private enum Axes
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
		 * Where xSize is width, zSize is breadth and ySize is depth from surface to thermocline.
		 */
		private int xSize, ySize, zSize;

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

				advect(xVel, xVelP, xVelP, yVelP, zVelP, Axes.x);
				advect(yVel, yVelP, xVelP, yVelP, zVelP, Axes.y);
				advect(zVel, zVelP, xVelP, yVelP, zVelP, Axes.z);
				project(xVel, yVel, zVel, /* reused to prevent unnecessary memory allocation -> */xVelP, yVelP);
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
		private final int getIndex(int x, int y, int z)// refacter to getIndex
			{
				return x + (y * xSize) + (z * xSize * ySize);
			}

		/**
		 * TODO
		 * 
		 * @param axis
		 * @param velocityData
		 */
		private void correctEdgeCases(Axes axis, double[] velocityData)
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
		 */
		private void advect(double[] dest, double[] src, double[] xVelocity, double[] yVelocity, double[] zVelocity, Axes axis)
			{
				// for all chunks which are not surface or thermocline chunks

				// to the depth of the thermocline
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
								if (xSrc >= xSize)
									xSrc -= xSize;

								// if z-source is off either end of the axis, wrap to other end
								if (zSrc < 0)
									zSrc += zSize;
								if (zSrc >= zSize)
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
									{
										ySrc = 0.5;
									}
								if (ySrc > ySize - 1.5)
									{
										ySrc = ySize - 1.5;
									}
								int yi0 = (int) ySrc;
								int yi1 = yi0 + 1;

								// Linear interpolation factors. Ex: 0.6 and 0.4
								double xProp1 = xSrc - xi0;
								double xProp0 = 1.0 - xProp1;
								double yProp1 = ySrc - yi0;
								double yProp0 = 1.0 - yProp1;
								double zProp1 = zSrc - yi0;
								double zProp0 = 1.0 - zProp1;

								dest[k] = xProp0 * (yProp0 * src[getK(xi0, yi0)] + yProp1 * src[getK(xi0, yi1)]) + xProp1 * (yProp0 * src[getK(xi1, yi0)] + yProp1 * src[getK(xi1, yi1)]);
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
				// to the depth of the thermocline
				for (int y = 1; y < ySize - 1; y++)
					// for width of model
					for (int x = 0; x < xSize; x++)
						// for breadth of model
						for (int z = 0; z < zSize; z++)
							{
								int k = getIndex(x, y, z);
								// Negative divergence ~ TODO should 0.5 change now 2 more chunks are involved?
								div[k] = -0.5 * h * (xV[k + 1] - xV[k - 1] + yV[k + xSize] - yV[k - xSize] + zV[k + zSize * zSize] - zV[k - zSize * zSize]);
								// Pressure field
								p[k] = 0;
							}
				correctEdgeCases(Axes.undefined, div);
				correctEdgeCases(Axes.undefined, p);

				linearSolve(p, div, Axes.undefined, 4);

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
								zV[k] -= 0.5 * (p[k + zSize * zSize] - p[k - zSize * zSize]) / h;
							}

				correctEdgeCases(Axes.x, xV);
				correctEdgeCases(Axes.y, yV);
				correctEdgeCases(Axes.z, zV);
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
		 *            - The current {@link Axes} data to which this method is being applied.
		 * @param c
		 *            - TODO previously used by other more complex aspects that have been stripped out, only ever 4.
		 */
		private void linearSolve(double[] dest, double[] src, Axes axis, double c)
			{
				double wMax = 1.9;
				double wMin = 1.5;
				for (int i = 0; i < 6; i++)
					{
						double w = Math.max((wMin - wMax) * i / 60.0 + wMax, wMin);
						// to the depth of the thermocline
						for (int y = 1; y < ySize - 1; y++)
							// for width of model
							for (int x = 0; x < xSize; x++)
								// for breadth of model
								for (int z = 0; z < zSize; z++)
									{
										int k = getIndex(x, y, z);
										 // TODO upgrade this to 3 dimensions v
										dest[k] = dest[k] + w * ((dest[k - 1] + dest[k + 1] + dest[k - xSize] + dest[k + xSize] + src[k]) / c - dest[k]);
									}
						correctEdgeCases(axis, dest);
					}
			}

	}
