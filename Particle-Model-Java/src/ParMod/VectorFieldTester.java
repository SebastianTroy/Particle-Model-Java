package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import tCode.RenderableObject;
import tComponents.components.TButton;
import tComponents.components.TLabel;
import tComponents.components.TMenu;
import tComponents.components.TSlider;
import tComponents.utils.events.TScrollEvent;

public class VectorFieldTester extends RenderableObject
	{
		private static final int NUM_CHUNKS = 200, MAX_CURRENT = 20;
		private int chunkWidth, chunkHeight;

		private int renderGap = 5;
		private double timer = 0, timePerTick = 0.01;

		// ~MENU~VARIABLES @formatter:off
		private TMenu menu;
		private final TSlider tickSpeedSlider = new TSlider(TSlider.HORIZONTAL, 0.01, 0.2);
		private final TSlider renderDensitySlider = new TSlider(TSlider.HORIZONTAL, 1, 10);
		private final TButton resetButton = new TButton("Reset"){@Override public void pressed(){refresh();}};

		// ~DATA~VARIABLES @formatter:on
		private double[] xVel;
		private double[] yVel;
		private double[] xVelP;
		private double[] yVelP;

		private int xSize = NUM_CHUNKS;
		private int ySize = NUM_CHUNKS;

		double timestep = 0.1;

		@Override
		public final void initiate()
			{
				chunkWidth = chunkHeight = Main.canvasHeight / NUM_CHUNKS;

				menu = new TMenu(NUM_CHUNKS * chunkWidth, 0, Main.canvasWidth - (NUM_CHUNKS * chunkWidth), Main.canvasHeight, TMenu.VERTICAL);

				renderDensitySlider.setValue(5);
				tickSpeedSlider.setValue(0.01);

				menu.add(new TLabel("<---------- Drag Mouse to create Currents"), false);
				menu.add(new TLabel("Vector line Density"), false);
				menu.add(renderDensitySlider);
				menu.add(new TLabel("Time Between Calculations (s)"), false);
				menu.add(tickSpeedSlider);
				menu.add(resetButton);

				add(menu);
			}

		@Override
		public final void refresh()
			{
				xVel = new double[NUM_CHUNKS * NUM_CHUNKS];
				yVel = new double[NUM_CHUNKS * NUM_CHUNKS];

				xVelP = xVel.clone();
				yVelP = yVel.clone();
			}

		@Override
		public void tick(double secondsPassed)
			{
				timer += secondsPassed;

				while (timer > timePerTick)
					{
						timer -= timePerTick;
						stepSimulation();
					}
			}

		@Override
		protected void render(Graphics2D g)
			{
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);

				g.setColor(Color.WHITE);
				for (int x = 0; x < NUM_CHUNKS; x += renderGap)
					for (int y = 0; y < NUM_CHUNKS; y += renderGap)
						g.drawLine(x * chunkWidth, y * chunkHeight, (int) ((x + xVel[getK(x, y)]) * chunkWidth), (int) ((y + yVel[getK(x, y)]) * chunkHeight));
			}

		@Override
		public final void mouseDragged(MouseEvent e)
			{
				if (e.getX() < 0 || e.getY() < 0 || e.getX() > NUM_CHUNKS * chunkWidth || e.getY() > NUM_CHUNKS * chunkHeight)
					return;

				int radius = NUM_CHUNKS / 10;

				double xVel = Main.input.mouseXVel / 500.0;
				double yVel = Main.input.mouseYVel / 500.0;

				if (xVel < -MAX_CURRENT)
					xVel = -MAX_CURRENT;
				else if (xVel > MAX_CURRENT)
					xVel = MAX_CURRENT;

				if (yVel < -MAX_CURRENT)
					yVel = -MAX_CURRENT;
				else if (yVel > MAX_CURRENT)
					yVel = MAX_CURRENT;

				int x1 = Main.input.mouseX / chunkWidth;
				int y1 = Main.input.mouseY / chunkHeight;

				int x0 = x1 - radius;
				int y0 = y1 - radius;

				int x2 = x1 + radius;
				int y2 = y1 + radius;

				for (int x = x0; x <= x2; x++)
					{
						for (int y = y0; y <= y2; y++)
							{
								if (y < 1 || y >= NUM_CHUNKS - 1)
									break;

								int dx = x - x1;
								int dy = y - y1;
								if ((double) dx * dx * radius * radius + (double) dy * dy * radius * radius <= (double) radius * radius * radius * radius)
									{
										int xIndex = x;

										if (xIndex < 0)
											xIndex += NUM_CHUNKS;
										if (xIndex >= NUM_CHUNKS)
											xIndex -= NUM_CHUNKS;

										int k = getK(xIndex, y);
										this.xVel[k] += xVel;
										this.yVel[k] += yVel;
									}
							}
					}
			}

		@Override
		public final void tScrollEvent(TScrollEvent e)
			{
				if (e.getSource() == renderDensitySlider)
					renderGap = (int) e.getScrollValue();
				else if (e.getSource() == tickSpeedSlider)
					timePerTick = e.getScrollValue();
			}

		private final void stepSimulation()
			{
				double[] temp;

				temp = xVelP;
				xVelP = xVel;
				xVel = temp;

				temp = yVelP;
				yVelP = yVel;
				yVel = temp;

				advect(xVel, xVelP, xVelP, yVelP, 1, timestep);
				advect(yVel, yVelP, xVelP, yVelP, 2, timestep);
				project(xVel, yVel, xVelP, yVelP);
			}

		private final int getK(int x, int y)
			{
				return x + y * xSize;
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
	}