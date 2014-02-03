package ParMod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import tCode.RenderableObject;
import tComponents.components.TButton;
import tComponents.components.TLabel;
import tComponents.components.TMenu;
import tComponents.components.TNumberField;
import tools.WindowTools;

/**
 * 
 * @author Sebastian Troy
 */
public class ParameterInput extends RenderableObject
	{
		private TMenu menu;

		// Simulation parameters
		private TNumberField depthNumberField;
		private TNumberField mixedDepthNumberField;
		private TNumberField paceNumberField;

		// Particle parameters
		private TNumberField particleNumberField;

		// Chunks (a chunk is a subsection of the simulation)
		private TNumberField chunkNumberField;
		private TLabel totalChunks = new TLabel(" (Total chunks: 50000) ");

		@Override
		protected void initiate()
			{
				// Create a menu to add all of the input fields into
				menu = new TMenu(0, 0, Main.canvasWidth / 4, Main.canvasHeight, TMenu.VERTICAL);
				menu.setBorderSize(5);
				add(menu);

				// Simulation parameters
				depthNumberField = new TNumberField(0, 0, 125, 25, 4); // limited to 4 digits long
				depthNumberField.setText("50");
				mixedDepthNumberField = new TNumberField(0, 0, 125, 25, 4); // limited to 4 digits long
				mixedDepthNumberField.setText("25");
				paceNumberField = new TNumberField(0, 0, 125, 25, 3); // limited to 3 digits long
				paceNumberField.setText("20");

				// Particle parameters
				particleNumberField = new TNumberField(0, 0, 125, 25, 8); // limited to 8 digits long
				particleNumberField.setText("10000");

				// Chunk parameters (a chunk is a subsection of the simulation)
				chunkNumberField = new TNumberField(0, 0, 125, 25, 4); // limited to 4 digits long
				chunkNumberField.setText("10");

				// Add the components to a menu that automatically arranges everything on screen ~~~~~~~~~~

				// Simulation parameters
				menu.add(new TLabel(" Depth of Simulation (meters): "), false);
				menu.add(depthNumberField, false);
				menu.add(new TLabel(" Depth of Mixed Layer (meters): "), false);
				menu.add(mixedDepthNumberField, false);
				menu.add(new TLabel(" Timestep of simulation (minutes): "), false);
				menu.add(paceNumberField, false);

				// Particle parameters
				menu.add(new TLabel(" Number of Particles: "), false);
				menu.add(particleNumberField, false);

				// Chunks (a chunk is a subsection of the simulation)
				menu.add(new TLabel(" Number of Chunks per meter: "), false);
				menu.add(chunkNumberField, false);
				menu.add(totalChunks, false);

				// Add a button that will begin the simulation when pressed.
				menu.add(new TButton("Start")
					{
						// Tell the button what to do when it is pressed
						@Override
						public void pressed()
							{
								double depth = depthNumberField.getValue();
								double mixedLayerDepth = mixedDepthNumberField.getValue();
								double pace = paceNumberField.getValue();
								double numParticles = particleNumberField.getValue();
								double chunks = chunkNumberField.getValue();
								// Check that all parameters are reasonable, if not warn user

								// Depth of Simulation
								if (depth <= 0)
									{
										WindowTools.informationWindow("Warning - The depth must be: \n -Greater than 0m", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Depth of Mixed Layer
								if (mixedLayerDepth > depth)
									{
										WindowTools.informationWindow("Warning - The depth of the mixed layer must be: \n -Less than or equal to the depth of the Simulation", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Pace of simulation
								if (pace <= 0)
									{
										WindowTools.informationWindow("Warning - The pace must be: \n -Greater than 0", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Particle number
								if (particleNumberField.getValue() != Math.floor(particleNumberField.getValue())/* not a whole number */|| /* or less than 1 */particleNumberField.getValue() < 1)
									{
										WindowTools.informationWindow("Warning - The number of particles must be: \n -A whole number \n -Greater than 0", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Chunk number
								if (chunkNumberField.getValue() != Math.floor(chunkNumberField.getValue())/* not a whole number */|| /* or less than 1 */chunkNumberField.getValue() < 1)
									{
										WindowTools.informationWindow("Warning - The number of chunkis must be: \n -A whole number \n -Greater than 0", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Create a new simulation using the parameters set by the user.
								Main.sim = new Simulation(1/* width set to 1 meter */, (int) depth, (int) mixedLayerDepth, (int) pace, (int) numParticles, 1.0 / chunks);

								// Make the Simulation the current screen, instead of this ParameterInput.
								changeRenderableObject(Main.sim);
							}
					}, false); //end of adding the start button to the menu
				// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

			}

		// The following methods are not used but the supertype for this class requires them.

		@Override
		public void tick(double secondsPassed)
			{} // Nothing to process here

		@Override
		protected void render(Graphics2D g)
			{
				// Clear the background to black
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, Main.canvasWidth, Main.canvasHeight);
			}

		@Override
		public void keyPressed(KeyEvent e)
			{
				// Update the label, that informs us how many chunks will be simulated, whenever a key is pressed, just in case the chunk number has been
				// updated
				totalChunks.setLabelText(" (Total chunks: " + (long) (chunkNumberField.getValue() * chunkNumberField.getValue() * chunkNumberField.getValue() * depthNumberField.getValue()) + ") ");
				menu.setBorderSize(5);
			}
	}