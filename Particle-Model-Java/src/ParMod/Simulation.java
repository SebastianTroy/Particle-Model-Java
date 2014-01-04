package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;

import tCode.RenderableObject;
import tools.RandTools;

public class Simulation extends RenderableObject
	{
		// The simulation takes place within a cuboid with the following
		// parameters
		int x = 10, y = 35, scaleWidth = 150, scaleHeight = 12;
		int width = 1; // The length of the simulation's short sides
		int depth = 50; // The length of the simualtion's long sides
		double zoom = 20, tilt = 30;

		Particle[] particles;

		Simulation(int numParticles)
			{
				super();

				particles = new Particle[numParticles];
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
				if (Main.input.getKeyState(38) == true)
					tilt += 10 * secondsPassed;
				if (Main.input.getKeyState(40) == true)
					tilt -= 10 * secondsPassed;

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
		 * This method displays some basic information about the ongoing
		 * simulation and gives options to view more in depth graphs or to view
		 * the simulation in real-time.
		 */
		@Override
		protected void render(Graphics2D g)
			{
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);

				// TODO create options to view graphs of data

				// The following code renders the water column and particles

				// Draw the wire frame that sits behind the particles
				g.setColor(Color.GRAY);
				// Top left
				g.drawLine(this.x, this.y, this.x + scaleWidth, this.y - (int) tilt);
				// Top right
				g.drawLine(this.x + 2 * scaleWidth, this.y, this.x + scaleWidth, this.y - (int) tilt);
				// Back centre
				g.drawLine(this.x + scaleWidth, this.y - (int) tilt, this.x + scaleWidth, this.y - (int) tilt + (50 * scaleHeight));
				// Bottom left
				g.drawLine(this.x, this.y + (scaleHeight * 50), this.x + scaleWidth, this.y - (int) tilt + (scaleHeight * 50));
				// Bottom right
				g.drawLine(this.x + 2 * scaleWidth, this.y + (scaleHeight * 50), this.x + scaleWidth, this.y - (int) tilt + (scaleHeight * 50));

				g.setColor(Color.WHITE);
				for (Particle p : particles)
					{
						int screenX = (int) ((scaleWidth * p.x) + (scaleWidth * p.z)) + this.x;
						int screenY = (int) ((scaleHeight * p.y) - (tilt * p.x) + (tilt * p.z)) + this.y;
						g.drawLine(screenX, screenY, screenX, screenY);
					}

				// Draw the wire frame that sits in front of the particles
				g.setColor(Color.LIGHT_GRAY);
				// Top left
				g.drawLine(this.x, this.y, this.x + scaleWidth, this.y + (int) tilt);
				// Top right
				g.drawLine(this.x + (2 * scaleWidth), this.y, this.x + scaleWidth, this.y + (int) tilt);
				// Front centre
				g.drawLine(this.x + scaleWidth, this.y + (int) tilt, this.x + scaleWidth, this.y + (int) tilt + (50 * scaleHeight));
				// Front left
				g.drawLine(this.x, this.y, this.x, this.y + (50 * scaleHeight));
				// Front right
				g.drawLine(this.x + (2 * scaleWidth), this.y, this.x + (2 * scaleWidth), this.y + (50 * scaleHeight));
				// Bottom left
				g.drawLine(this.x, this.y + (scaleHeight * 50), this.x + scaleWidth, this.y + (int) tilt + (scaleHeight * 50));
				// Bottom right
				g.drawLine(this.x + (2 * scaleWidth), this.y + (scaleHeight * 50), this.x + scaleWidth, this.y + (int) tilt + (scaleHeight * 50));

			}
	}
