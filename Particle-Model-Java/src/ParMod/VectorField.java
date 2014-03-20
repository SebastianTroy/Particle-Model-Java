package ParMod;

public class VectorField
	{
		private double[] xVel;
		private double[] yVel;
		private double[] zVel;
		private double[] xVelP;
		private double[] yVelP;
		private double[] zVelP;

		private int xSize;
		private int ySize;
		private int zSize;

		double timestep = 0.1;

		private final void stepSimulation()
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

				advect(xVel, xVelP, xVelP, yVelP, zVelP, 1, timestep);
				advect(yVel, yVelP, xVelP, yVelP, zVelP, 2, timestep);
				advect(zVel, zVelP, xVelP, yVelP, zVelP, 3, timestep);
				project(xVel, yVel, zVel, xVelP, yVelP, zVelP);
			}

		private final int getK(int x, int y, int z)
			{
				return x + (y * xSize) + (z * xSize * ySize);
			}

		private void setBounds(int b, double[] d)
		{
			if (b == 1)// if xVelocities
				{
					/*
					 * For everything down the sides, apply friction (to perpetual flow occurring)
					 */
					for (int y = 0; y < ySize; y++)
						{
							d[getK(0, y)] *= 0.95;
							d[getK(xSize - 1, y)] *= 0.95;
						}
					/*
					 * For everything along the top && not at a side, x motion is the same as that in any neighbors
					 */
					for (int x = 1; x < xSize - 1; x++)
						{
							d[getK(x, 0)] = d[getK(x, 1)];
							d[getK(x, ySize - 1)] = d[getK(x, ySize - 2)];
						}
				}
			else if (b == 2)// if yVelocities
				{
					for (int x = 0; x < xSize; x++)
						{
							d[getK(x, 0)] = 0;
							d[getK(x, ySize - 1)] = 0;
						}
				}
			else if (b == 3)// if zVelocities
				{
					for (int x = 0; x < xSize; x++)
						{
							d[getK(x, 0)] = 0;
							d[getK(x, ySize - 1)] = 0;
						}
				}
			else
				// b == 0
				{
					for (int x = 1; x < xSize - 1; x++)
						{
							d[getK(x, 0)] = d[getK(x, 1)];
							d[getK(x, ySize - 1)] = d[getK(x, ySize - 2)];
						}
					/*
					 * For corners, velocity is interpolation of neighbors
					 */
					d[getK(0, 0)] = 0.5 * (d[getK(0, 1)] + d[getK(1, 0)]);
					d[getK(0, ySize - 1)] = 0.5 * (d[getK(1, ySize - 1)] + d[getK(0, ySize - 2)]);
					d[getK(xSize - 1, 0)] = 0.5 * (d[getK(xSize - 1, 1)] + d[getK(xSize - 2, 0)]);
					d[getK(xSize - 1, ySize - 1)] = 0.5 * (d[getK(xSize - 1, ySize - 2)] + d[getK(xSize - 2, ySize - 1)]);
				}
		
		}

		private void advect(double[] dest, double[] src, double[] xVelocity, double[] yVelocity, int b, double dt)
			{
				// for non top/bottom edge chunks
				for (int y = 1; y < ySize - 1; y++)
					{
						int yIndex = y * xSize;
						// for all chunks within above range
						for (int x = 0; x < xSize; x++)
							{
								int k = x + yIndex;
								// Reverse velocity, since we are interpolating backwards
								// xSrc and ySrc is the position of the source density.
								double xSrc = x - dt * xVelocity[k];
								double ySrc = y - dt * yVelocity[k];

								// if x-source is too close to the left edge, wrap to right edge
								if (xSrc < 0)
									xSrc += xSize;
								if (xSrc >= xSize)
									xSrc -= xSize;

								int xi0 = (int) xSrc;
								int xi1 = xi0 + 1;
								if (xi1 == xSize)
									xi1 -= xSize;

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

								dest[k] = xProp0 * (yProp0 * src[getK(xi0, yi0)] + yProp1 * src[getK(xi0, yi1)]) + xProp1 * (yProp0 * src[getK(xi1, yi0)] + yProp1 * src[getK(xi1, yi1)]);
							}
					}

				setBounds(b, dest);
			}

		private void project(double[] xV, double[] yV, double[] p, double[] div)
			{
				double h = 0.1; // /(xSize-2);
				for (int y = 1; y < ySize - 1; y++)
					{
						int yIndex = y * xSize;
						for (int x = 0; x < xSize; x++)
							{
								int k = x + yIndex;
								// Negative divergence
								div[k] = -0.5 * h * (xV[k + 1] - xV[k - 1] + yV[k + xSize] - yV[k - xSize]);
								// Pressure field
								p[k] = 0;
							}
					}
				setBounds(0, div);
				setBounds(0, p);

				linearSolve(p, div, 0, 1, 4);

				for (int y = 1; y < ySize - 1; y++)
					{
						int yIndex = y * xSize;
						for (int x = 0; x < xSize; x++)
							{
								int k = x + yIndex;
								xV[k] -= 0.5 * (p[k + 1] - p[k - 1]) / h;
								yV[k] -= 0.5 * (p[k + xSize] - p[k - xSize]) / h;
							}
					}
				setBounds(1, xV);
				setBounds(2, yV);
			}

		// 4-10 iterations is good for real-time, and not noticably inaccurate. For real accuracy, upwards of 20 is good.
		private void linearSolve(double[] dest, double[] src, int b, double a, double c)
			{
				double wMax = 1.9;
				double wMin = 1.5;
				for (int i = 0; i < 6; i++)
					{
						double w = Math.max((wMin - wMax) * i / 60.0 + wMax, wMin);
						for (int y = 1; y < ySize - 1; y++)
							{
								int yIndex = y * xSize;
								for (int x = 0; x < xSize; x++)
									{
										int k = x + yIndex;
										dest[k] = dest[k] + w * ((a * (dest[k - 1] + dest[k + 1] + dest[k - xSize] + dest[k + xSize]) + src[k]) / c - dest[k]);
										// dest[getK(x, y)] = (a * (dest[getK(x-1,y)] + dest[getK(x+1,y)] + dest[getK(x,y-1)] + dest[getK(x,y+1)]) +
										// src[getK(x,y)]) / c;
									}
							}
						setBounds(b, dest);
					}
			}
	
	}
