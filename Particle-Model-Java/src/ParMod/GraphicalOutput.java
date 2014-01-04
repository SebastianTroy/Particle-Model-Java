package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 
 * @author Sebastian Troy
 */
public class GraphicalOutput
	{
		// Note that the width and height of the graph do not represent the scale of the simulation to scale, they are merely a convenient size for observation.
		private int graphX = 10, graphY = 35, graphWidth = 150, graphHeight = 600, graphTilt = 30;

		/**
		 * This method compartmentalises the code used to visualise the simulation for readability.
		 */
		final void drawWaterColumn(Graphics2D g)
			{
				// Draw the wire frame that sits behind the particles
				g.setColor(Color.GRAY);
				// Top left
				g.drawLine(graphX, graphY, graphX + graphWidth, graphY - graphTilt);
				// Top right
				g.drawLine(graphX + 2 * graphWidth, graphY, graphX + graphWidth, graphY - graphTilt);
				// Back centre
				g.drawLine(graphX + graphWidth, graphY - graphTilt, graphX + graphWidth, graphY - graphTilt + graphHeight);
				// Bottom left
				g.drawLine(graphX, graphY + graphHeight, graphX + graphWidth, graphY - graphTilt + graphHeight);
				// Bottom right
				g.drawLine(graphX + 2 * graphWidth, graphY + graphHeight, graphX + graphWidth, graphY - graphTilt + graphHeight);

				g.setColor(Color.WHITE);
				for (Particle p : Main.sim.particles)
					{
						/*
						 * The simulation is drawn as if looking in through a corner of the water column, and slightly from above. The screenX and screenY
						 * variables are used to convert the particles 3D location in the simulation into 2D coordinates on the graph.
						 */
						int screenX = (int) ((graphWidth * p.x) + (graphWidth * p.z)) + graphX;
						int screenY = (int) ((graphHeight / 50 * p.y) - (graphTilt * p.x) + (graphTilt * p.z)) + graphY;

						// Draw the particle as a single pixel, a particle is drawn over all previous particles regardless of actual position.
						g.drawLine(screenX, screenY, screenX, screenY);
					}

				// Draw the wire frame that sits in front of the particles
				g.setColor(Color.LIGHT_GRAY);
				// Top left
				g.drawLine(graphX, graphY, graphX + graphWidth, graphY + graphTilt);
				// Top right
				g.drawLine(graphX + (2 * graphWidth), graphY, graphX + graphWidth, graphY + graphTilt);
				// Front centre
				g.drawLine(graphX + graphWidth, graphY + graphTilt, graphX + graphWidth, graphY + graphTilt + graphHeight);
				// Front left
				g.drawLine(graphX, graphY, graphX, graphY + graphHeight);
				// Front right
				g.drawLine(graphX + (2 * graphWidth), graphY, graphX + (2 * graphWidth), graphY + graphHeight);
				// Bottom left
				g.drawLine(graphX, graphY + graphHeight, graphX + graphWidth, graphY + graphTilt + graphHeight);
				// Bottom right
				g.drawLine(graphX + (2 * graphWidth), graphY + graphHeight, graphX + graphWidth, graphY + graphTilt + graphHeight);
			}
	}
