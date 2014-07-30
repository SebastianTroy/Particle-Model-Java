package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import tCode.RenderableObject;
import tComponents.components.TButton;
import tComponents.components.TCheckBox;
import tComponents.components.TLabel;
import tComponents.components.TMenu;
import tComponents.components.TSlider;
import tComponents.utils.events.TScrollEvent;
import tools.DrawTools;
import tools.NumTools;
import tools.Rand;
import tools.WindowTools;

public class VectorFieldTester extends RenderableObject
	{
		/**
		 * Used to keep track of, and pass around, the axis currently being worked on between functions
		 */
		enum Axis
			{
				x, y, undefined
			}

		private static final int NUM_CHUNKS = 200, MAX_CURRENT = 20;
		private int chunkWidth, chunkHeight, halfChunkWidth, halfChunkHeight;

		private int renderGap = 5;
		private double timer = 0, timePerTick = 0.01;

		// Whether the vectors are drawn with arrowheads or not
		boolean arrows = true;

		// ~MENU~VARIABLES @formatter:off
		private TMenu menu;
		private final TSlider tickSpeedSlider = new TSlider(TSlider.HORIZONTAL, 0.01, 0.2);
		private final TSlider iterationsSlider = new TSlider(TSlider.HORIZONTAL, 1, 20);
		private final TSlider renderDensitySlider = new TSlider(TSlider.HORIZONTAL, 1, 10);
		private final TSlider particleDiffusionSlider = new TSlider(TSlider.HORIZONTAL, 0, 10);
		private final TSlider  particleBrightnessSlider = new TSlider(TSlider.HORIZONTAL, 0, 255);
		private final TButton resetButton = new TButton("Reset"){@Override public void pressed(){refresh();}};
		private final TCheckBox showParticleBox = new TCheckBox("Show Particles"){@Override public void pressed(){showParticles = isChecked();}};
		private final TCheckBox showVectorsBox = new TCheckBox("Show Vectors"){@Override public void pressed(){showVectors = isChecked();}};
		private final TCheckBox showArrowHeadsBox = new TCheckBox("Show Vectors"){@Override public void pressed(){arrows = isChecked();}};

		// ~VELOCITY~DATA~VARIABLES @formatter:on
		private double[] xVel;
		private double[] yVel;
		private double[] xVelP;
		private double[] yVelP;
		private boolean showVectors = true;

		private int xSize = NUM_CHUNKS;
		private int ySize = NUM_CHUNKS;
		private double fieldWidth;
		private double fieldHeight;

		private double timestep = 0.1;

		// ~PARTICLE~DATA~VARIABLES~
		private Particle[] particles;
		private double particleDiffusionRate = 0.5;
		private boolean showParticles = false;
		private Color particleColour = Color.BLUE;

		@Override
		public final void initiate()
			{
				chunkWidth = chunkHeight = Main.canvasHeight / NUM_CHUNKS;
				halfChunkWidth = halfChunkHeight = chunkWidth / 2;
				fieldWidth = chunkWidth * NUM_CHUNKS;
				fieldHeight = chunkHeight * NUM_CHUNKS;

				particles = new Particle[20000];
				for (int i = 0; i < particles.length; i++)
					particles[i] = new Particle(fieldWidth / 2, fieldHeight / 2);

				menu = new TMenu(NUM_CHUNKS * chunkWidth, 0, Main.canvasWidth - (NUM_CHUNKS * chunkWidth), Main.canvasHeight, TMenu.VERTICAL);

				renderDensitySlider.setValue(5);
				tickSpeedSlider.setValue(0.01);
				iterationsSlider.setValue(6);
				particleDiffusionSlider.setValue(0.5);
				particleBrightnessSlider.setValue(0);

				showParticleBox.setChecked(true);
				showVectorsBox.setChecked(true);
				showArrowHeadsBox.setChecked(true);

				menu.add(new TLabel("<---------- Drag Mouse to create Currents"), false);
				menu.add(new TLabel("Vector line Density"), false);
				menu.add(renderDensitySlider);
				menu.add(new TLabel("Time Between Calculations (s)"), false);
				menu.add(tickSpeedSlider);
				menu.add(new TLabel("Iterations of linear solving"), false);
				menu.add(iterationsSlider);
				menu.add(new TLabel("Particle Diffusion"), false);
				menu.add(particleDiffusionSlider);
				menu.add(new TLabel("Particle Brightness"), false);
				menu.add(particleBrightnessSlider);
				menu.add(showVectorsBox, false);
				menu.add(showParticleBox, false);
				menu.add(showArrowHeadsBox, false);
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

				for (int i = 0; i < particles.length; i++)
					{
						particles[i].x = fieldWidth / 2;
						particles[i].y = fieldHeight / 2;
					}
			}

		@Override
		public void tick(double secondsPassed)
			{
				timer += secondsPassed;

				while (timer > timePerTick)
					{
						timer -= timePerTick;
						stepSimulation();

						Particle p;
						for (int i = 0; i < particles.length; i++)
							{
								p = particles[i];

								// wrap particles on edges
								if (p.x < 0)
									p.x += fieldWidth;
								if (p.x >= fieldWidth)
									p.x -= fieldWidth;

								if (p.y < 0)
									p.y += fieldHeight;
								if (p.y >= fieldHeight - 1)
									p.y -= fieldHeight;

								// apply local velocity
								p.x += getVelocity(p.x / chunkWidth, p.y / chunkHeight, Axis.x);
								p.y += getVelocity(p.x / chunkWidth, p.y / chunkHeight, Axis.y);

								// diffuse particles
								p.x += Rand.double_(-particleDiffusionRate, particleDiffusionRate);
								p.y += Rand.double_(-particleDiffusionRate, particleDiffusionRate);
							}
					}
			}

		@Override
		protected void render(Graphics2D g)
			{
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);

				if (showVectors)
					{
						g.setColor(Color.WHITE);
						for (int x = 0; x < NUM_CHUNKS; x += renderGap)
							for (int y = 0; y < NUM_CHUNKS; y += renderGap)
								if (arrows)
									DrawTools.drawArrow((x * chunkWidth) + halfChunkWidth, (y * chunkHeight) + halfChunkHeight, (int) ((x + xVel[getK(x, y)]) * chunkWidth) + halfChunkWidth,
											(int) ((y + yVel[getK(x, y)]) * chunkHeight) + halfChunkHeight, g, 4);
								else
									g.drawLine((x * chunkWidth) + halfChunkWidth, (y * chunkHeight) + halfChunkHeight, (int) ((x + xVel[getK(x, y)]) * chunkWidth) + halfChunkWidth,
											(int) ((y + yVel[getK(x, y)]) * chunkHeight) + halfChunkHeight);
					}

				if (showParticles)
					{
						g.setColor(particleColour);
						for (Particle p : particles)
							g.drawLine(p.getX(), p.getY(), p.getX(), p.getY());
					}
			}

		@Override
		public final void mouseDragged(MouseEvent e)
			{
				if (e.getX() < 0 || e.getY() < 0 || e.getX() > (NUM_CHUNKS - 1) * chunkWidth || e.getY() > (NUM_CHUNKS - 1) * chunkHeight)
					return;

				int radius = NUM_CHUNKS / 10;

				double xVel = Main.input.mouseXVel / 5000.0;
				double yVel = Main.input.mouseYVel / 5000.0;

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

				for (int y = y0; y <= y2; y++)
					for (int x = x0; x <= x2; x++)
						{
							if (y < 1 || y >= NUM_CHUNKS - 1)
								break;

							int xIndex = x;

							if (xIndex < 0)
								xIndex += NUM_CHUNKS;
							if (xIndex >= NUM_CHUNKS)
								xIndex -= NUM_CHUNKS;

							int index = getK(xIndex, y);
							// Stronger currents added closer to the mouse
							double distanceModifier = radius - Math.abs(NumTools.distance(x1, y1, x, y));
							this.xVel[index] += xVel * distanceModifier;
							this.yVel[index] += yVel * distanceModifier;
						}
			}

		@Override
		public final void tScrollEvent(TScrollEvent e)
			{
				if (e.getSource() == renderDensitySlider)
					renderGap = (int) e.getScrollValue();
				else if (e.getSource() == tickSpeedSlider)
					timePerTick = e.getScrollValue();
				else if (e.getSource() == particleDiffusionSlider)
					particleDiffusionRate = particleDiffusionSlider.getValue();
				else if (e.getSource() == particleBrightnessSlider)
					particleColour = new Color((int) particleBrightnessSlider.getValue(), (int) particleBrightnessSlider.getValue(), 255);
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

				advect(xVel, xVelP, xVelP, yVelP, Axis.x, timestep);
				advect(yVel, yVelP, xVelP, yVelP, Axis.y, timestep);
				project(xVel, yVel, xVelP, yVelP);
			}

		private final int getK(int x, int y)
			{
				return x + y * xSize;
			}

		private final double getVelocity(double x, double y, Axis a)
			{
				x *= 0.99;
				y *= 0.99;

				double[] velocity = null;

				switch (a)
					{
						case x:
							velocity = xVel;
							break;
						case y:
							velocity = yVel;
							break;

						case undefined:
						default:
							WindowTools.debugWindow("Cannot get vector without spoecifying an axis");
					}

				int xi0 = (int) x;
				int xi1 = xi0 + 1;
				if (xi1 == xSize)
					xi1 = 0;

				// if y-source is above surface or below thermocline, restrict to surface/thermocline
				if (y < 0.5)
					y = 0.5;
				else if (y >= ySize - 1.5)
					y = ySize - 1.5;

				int yi0 = (int) y;
				int yi1 = yi0 + 1;

				// Linear interpolation factors. Ex: 0.6 and 0.4
				double xProp1 = x - xi0;
				double xProp0 = 1.0 - xProp1;
				double yProp1 = y - yi0;
				double yProp0 = 1.0 - yProp1;

				/*
				 * Here we find the velocity at a point in the middle of 8 chunks. We basically interpolate the velocity between all of the yAxis neighbours,
				 * then we interpolate those values along the x axis, then finally, to combine them into a point we combine those velocities along the z axis.
				 */
				return (xProp0 * (yProp0 * velocity[getK(xi0, yi0)] + yProp1 * velocity[getK(xi0, yi1)]) + xProp1 * (yProp0 * velocity[getK(xi1, yi0)] + yProp1 * velocity[getK(xi1, yi1)]));
			}

		private void setBounds(Axis axis, double[] d)
			{
				if (axis == Axis.x)// if xVelocities
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
						 * For everything along the top & bottom that is not at a side, x motion is the same as that of nearest neighbour on y axis
						 */
						for (int x = 1; x < xSize - 2; x++)
							{
								d[getK(x, 0)] = d[getK(x, 1)];
								d[getK(x, ySize - 1)] = d[getK(x, ySize - 2)];
							}
					}
				else if (axis == Axis.y)// if yVelocities
					{
						// No y velocities for top or bottom layer
						for (int x = 0; x < xSize; x++)
							{
								d[getK(x, 0)] = 0;
								d[getK(x, ySize - 1)] = 0;
							}
					}
				else
					// b == 0
					{
						for (int x = 1; x < xSize - 2; x++)
							{
								d[getK(x, 0)] = d[getK(x, 1)];
								d[getK(x, ySize - 1)] = d[getK(x, ySize - 2)];
							}

						d[getK(0, 0)] = 0.5 * (d[getK(0, 1)] + d[getK(1, 0)]);
						d[getK(0, ySize - 1)] = 0.5 * (d[getK(1, ySize - 1)] + d[getK(0, ySize - 2)]);
						d[getK(xSize - 1, 0)] = 0.5 * (d[getK(xSize - 1, 1)] + d[getK(xSize - 2, 0)]);
						d[getK(xSize - 1, ySize - 1)] = 0.5 * (d[getK(xSize - 1, ySize - 2)] + d[getK(xSize - 2, ySize - 1)]);
						// For corners, velocity is interpolation of neighbors
					}

			}

		private void advect(double[] dest, double[] src, double[] xVelocity, double[] yVelocity, Axis axis, double dt)
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

				setBounds(axis, dest);
			}

		private void project(double[] xV, double[] yV, double[] p, double[] div)
			{
				double h = 0.00000000000000001;
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
				setBounds(Axis.undefined, div);
				setBounds(Axis.undefined, p);

				linearSolve(p, div, Axis.undefined);

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
				setBounds(Axis.x, xV);
				setBounds(Axis.y, yV);
			}

		// 4-10 iterations is good for real-time, and not noticeably inaccurate. For real accuracy, upwards of 20 is good.
		private void linearSolve(double[] dest, double[] src, Axis axis)
			{
				double w = 1.9;
				for (int i = 0; i < iterationsSlider.getValue(); i++)
					{
						for (int y = 1; y < ySize - 1; y++)
							{
								int yIndex = y * xSize;
								for (int x = 0; x < xSize; x++)
									{
										int k = x + yIndex;
										dest[k] += w * ((dest[k - 1] + dest[k + 1] + dest[k - xSize] + dest[k + xSize] + src[k]) / 4 - dest[k]);
									}
							}
						w -= 0.01;
						if (w < 1.5)
							w = 1.5;
					}
				setBounds(axis, dest);
			}

		private class Particle
			{
				double x, y;

				private Particle(double x, double y)
					{
						this.x = x;
						this.y = y;
					}

				private int getX()
					{
						return (int) x;
					}

				private int getY()
					{
						return (int) y;
					}
			}
	}