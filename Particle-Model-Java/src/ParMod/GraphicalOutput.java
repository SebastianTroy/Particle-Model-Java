package ParMod;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;

import ParMod.VectorField.Axis;
import tComponents.components.TScrollBar;
import tComponents.utils.events.TScrollEvent;

/**
 * 
 * @author Sebastian Troy
 */
public class GraphicalOutput
	{
		// Constants that represent viewing modes
		public static final int VIEW_PARTICLES = 0;
		public static final int VIEW_CURRENTS = 1;
		public static final int VIEW_GRAPH = 2;

		// This variable denotes the current viewing mode
		int currentViewMode = VIEW_PARTICLES;

		// Note that the width and height of the graph do not represent the scale of the simulation to scale, they are merely a convenient size for observation.
		private int graphX, graphY;
		private double graphWidth, graphHeight, graphTilt, scale = 1;

		double maxTiltProportion = 0.45;

		//
		private TScrollBar graphScroller = new TScrollBar(0, 0, Main.canvasHeight, Main.canvasHeight, true, new Rectangle(0, 0, Main.canvasWidth, Main.canvasHeight));
		private boolean usingScrollBar = false;
		
		// Used to draw dotted lines for the graph mode
		Stroke frontOfCubeStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0);
		Stroke backOfCubeStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);

		public GraphicalOutput()
			{
				Main.sim.add(graphScroller);

				graphWidth = Main.sim.width;
				graphHeight = Main.sim.depth;

				graphTilt = maxTiltProportion * graphWidth;

				graphX = 5;
				graphY = 5 + (int) graphTilt;

				// Initialise the graph so that it fills the screen vertically
				setScale((Main.canvasHeight - 10) / (graphHeight + (2 * graphTilt)));
			}

		final void setScale(double newScale)
			{
				double multiplier = newScale / scale;

				graphWidth *= multiplier;
				graphHeight *= multiplier;

				graphTilt *= multiplier;

				if (graphHeight + (2 * (Math.abs(graphTilt))) > Main.canvasHeight)
					{
						Main.sim.add(graphScroller);
						usingScrollBar = true;
						graphScroller.setMaxScrollDistance(graphHeight + 10 + (2 * Math.abs(graphTilt)));

						graphX = 25;
						graphY = (int) -(graphScroller.getCurrentScrollDistance() - 5 - Math.abs(graphTilt));
					}
				else
					{
						Main.sim.remove(graphScroller);
						usingScrollBar = false;
						graphX = 5;
						graphY = 5 + (int) (maxTiltProportion * graphWidth);
					}
			}

		final void tick(double secondsPassed)
			{
				double tiltMax = graphWidth * maxTiltProportion;

				if (Main.input.getKeyState(KeyEvent.VK_UP) && graphTilt > 0)
					{
						graphTilt -= tiltMax * secondsPassed;
						setScale(scale);
					}
				if (Main.input.getKeyState(KeyEvent.VK_DOWN) && graphTilt < tiltMax)
					{
						graphTilt += tiltMax * secondsPassed;
						setScale(scale);
					}

				if (Main.input.getKeyState(KeyEvent.VK_EQUALS) && graphWidth < 300)
					{
						setScale(scale + (2 * secondsPassed));
					}
				if (Main.input.getKeyState(KeyEvent.VK_MINUS) && usingScrollBar)
					{
						setScale(scale - (2 * secondsPassed));
					}
			}

		/**
		 * This method compartmentalises the code used to visualise the simulation. It is compartmentalised to reduce code bloat.
		 */
		final void drawWaterColumn(Graphics2D g)
			{
				int graphTilt = (int) this.graphTilt;
				int roundedGraphWidth = Math.round(Math.round(graphWidth));

				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// Draw the parts of the wire frame for water column that are behind everything
				// draw them dotted if graph mode is on
				Stroke oldStroke = g.getStroke();
				if (currentViewMode == VIEW_GRAPH)
					g.setStroke(backOfCubeStroke);
				
				g.setColor(Color.GRAY);
				// Top left
				g.drawLine(graphX, graphY, graphX + roundedGraphWidth, graphY - graphTilt);
				// Top right
				g.drawLine(graphX + 2 * roundedGraphWidth, graphY, graphX + roundedGraphWidth, graphY - graphTilt);
				// Back centre
				g.drawLine(graphX + roundedGraphWidth, graphY - graphTilt, graphX + roundedGraphWidth, graphY - graphTilt + (int) graphHeight);
				// Bottom left
				g.drawLine(graphX, graphY + (int) graphHeight, graphX + roundedGraphWidth, graphY - graphTilt + (int) graphHeight);
				// Bottom right
				g.drawLine(graphX + 2 * roundedGraphWidth, graphY + (int) graphHeight, graphX + roundedGraphWidth, graphY - graphTilt + (int) graphHeight);
				
				// If graph mode is on, return line drawing to normal
				if (currentViewMode == VIEW_GRAPH)
					g.setStroke(oldStroke);
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

				// This switch statement checks to see which viewing mode is selected and then only draws the appropriate information.
				switch (currentViewMode)
					{
						case VIEW_PARTICLES:
							{
								g.setColor(Color.WHITE);
								for (Particle p : Main.sim.particles)
									{
										/*
										 * The simulation is drawn as if looking in through an edge of the water column, and slightly from above. The screenX
										 * and screenY variables are used to convert the particles 3D location in the simulation into 2D coordinates on the
										 * graph.
										 */
										int screenX = (int) Math.round(((graphWidth * p.x) + (graphWidth * p.z)) + graphX);
										int screenY = (int) ((graphHeight / Main.sim.depth * p.y) - (graphTilt * p.x) + (graphTilt * p.z)) + graphY;

										// Draw the particle as a single pixel, a particle is drawn over all previous particles regardless of actual position.
										g.drawLine(screenX, screenY, screenX, screenY);
									}
								break; // Once that is drawn, ignore any further options
							}
						case VIEW_CURRENTS:
							{
								double xSpacing = graphWidth / (Main.sim.chunks.length);
								double ySpacing = graphHeight / (Main.sim.chunks[0].length);
								double tiltSpacing = this.graphTilt / (Main.sim.chunks[0][0].length);

								g.setColor(Color.WHITE);
								for (int x = 0; x < Main.sim.chunks.length; x++)
									for (int y = 0; y < Main.sim.chunks[0].length; y++)
										for (int z = 0; z < Main.sim.chunks[0][0].length; z++)
											{
												int screenX = (int) ((xSpacing * x + (0.5 * xSpacing)) + (xSpacing * z + (0.5 * xSpacing))) + graphX;
												int screenY = (int) ((ySpacing * y + (0.5 * ySpacing)) - (tiltSpacing * x + (0.5 * xSpacing)) + (tiltSpacing * z + (0.5 * xSpacing))) + graphY;

												double xVel = Main.sim.vecField.getVelocityAt(x, y, z, Axis.x);
												double yVel = Main.sim.vecField.getVelocityAt(x, y, z, Axis.y);
												double zVel = Main.sim.vecField.getVelocityAt(x, y, z, Axis.z);

												int velocityX = (int) ((xSpacing * (x + 10 * xVel) + (0.5 * xSpacing)) + (xSpacing * (z + 10 * zVel) + (0.5 * xSpacing))) + graphX;
												int velocityY = (int) ((ySpacing * (y + 10 * yVel) + (0.5 * ySpacing)) - (tiltSpacing * (x + 10 * xVel) + (0.5 * xSpacing)) + (tiltSpacing
														* (z + 10 * zVel) + (0.5 * xSpacing)))
														+ graphY;

												g.drawLine(screenX, screenY, velocityX, velocityY);
											}
								break; // Once that is drawn, ignore any further options
							}
						case VIEW_GRAPH:
							{
								for (Particle p : Main.sim.particles)
									p.calculateColour();

								Collections.sort(Main.sim.particles);

								for (Particle p : Main.sim.particles)
									{
										g.setColor(new Color(p.colour, p.colour, p.colour));
										/*
										 * The simulation is drawn as if looking in through an edge of the water column, and slightly from above. The screenX
										 * and screenY variables are used to convert the particles 3D location in the simulation into 2D coordinates on the
										 * graph.
										 */
										int screenX = (int) Math.round(((graphWidth * p.x) + (graphWidth * p.z)) + graphX);
										int screenY = (int) ((graphHeight / Main.sim.depth * p.y) - (graphTilt * p.x) + (graphTilt * p.z)) + graphY;

										// Draw the particle as a single pixel, a particle is drawn over all previous particles regardless of actual position.
										g.drawLine(screenX, screenY, screenX, screenY);
									}
								break; // Once that is drawn, ignore any further options
							}

					}

				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
				// Draw the parts of the wire frame of the water column that are in front of everything
				
				if (currentViewMode == VIEW_GRAPH)
					g.setStroke(frontOfCubeStroke);
				
				g.setColor(Color.LIGHT_GRAY);
				// Top left
				g.drawLine(graphX, graphY, graphX + roundedGraphWidth, graphY + graphTilt);
				// Top right
				g.drawLine(graphX + (2 * roundedGraphWidth), graphY, graphX + roundedGraphWidth, graphY + graphTilt);
				// Front centre
				g.drawLine(graphX + roundedGraphWidth, graphY + graphTilt, graphX + roundedGraphWidth, graphY + graphTilt + (int) graphHeight);
				// Front left
				g.drawLine(graphX, graphY, graphX, graphY + (int) graphHeight);
				// Front right
				g.drawLine(graphX + (2 * roundedGraphWidth), graphY, graphX + (2 * roundedGraphWidth), graphY + (int) graphHeight);
				// Bottom left
				g.drawLine(graphX, graphY + (int) graphHeight, graphX + roundedGraphWidth, graphY + graphTilt + (int) graphHeight);
				// Bottom right
				g.drawLine(graphX + (2 * roundedGraphWidth), graphY + (int) graphHeight, graphX + roundedGraphWidth, graphY + graphTilt + (int) graphHeight);
				
				g.setStroke(oldStroke);
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			}

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
						case '3':
							Main.graphicalOutput.currentViewMode = GraphicalOutput.VIEW_GRAPH;
							return; // Once that is done, ignore the rest of the options
					}
			}

		public final void tScrollEvent(TScrollEvent event)
			{
				graphY = (int) -(event.getScrollValue() - 5 - Math.abs(graphTilt));
			}
	}