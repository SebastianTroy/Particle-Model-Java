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
		private TNumberField widthNumberField;
		private TNumberField depthNumberField;
		private TNumberField paceNumberField;
		
		// Particle parameters
		private TNumberField particleNumberField;
		
		// Chunks (a chunk is a subsection of the simulation)
		private TNumberField chunkNumberField;
		private TLabel totalChunks = new TLabel(" (Total chunks: 5000) ");

		@Override
		protected void initiate()
			{
				// Create a menu to add all of the input fields into
				menu = new TMenu(0, 0, Main.canvasWidth / 4, Main.canvasHeight, TMenu.VERTICAL);
				menu.setBorderSize(5);
				add(menu);

				// Simulation parameters
				widthNumberField = new TNumberField(0, 0, 125, 25, 3); // limited to 3 digits long
				widthNumberField.setText("1");
				depthNumberField = new TNumberField(0, 0, 125, 25, 4); // limited to 4 digits long
				depthNumberField.setText("50");
				paceNumberField = new TNumberField(0, 0, 125, 25, 3); // limited to 3 digits long
				paceNumberField.setText("20");
				
				// Particle parameters
				particleNumberField = new TNumberField(0, 0, 125, 25, 8); // limited to 8 digits long
				particleNumberField.setText("10000");
				
				// Create a text box that only accepts numbers up to 4 digits long
				chunkNumberField = new TNumberField(0, 0, 125, 25, 4); // limited to 4 digits long
				chunkNumberField.setText("10");

				// Add the components to a menu that automatically arranges everything on screen ~~~~~~~~~~
		
				// Simulation parameters
				menu.add(new TLabel(" Width of Simulation (meters): "), false);
				menu.add(widthNumberField, false);
				menu.add(new TLabel(" Depth of Simulation (meters): "), false);
				menu.add(depthNumberField, false);
				menu.add(new TLabel(" Timestep of simulation (minutes): "), false);
				menu.add(paceNumberField, false);
				
				// Particle parameters
				menu.add(new TLabel(" Number of Particles: "), false);
				menu.add(particleNumberField, false);
				
				// Chunks (a chunk is a subsection of the simulation)
				menu.add(new TLabel(" Number of Chunks per meter: "), false);
				menu.add(totalChunks, false);
				menu.add(chunkNumberField, false);
				
				// Add a button that will begin the simulation when pressed.
				menu.add(new TButton("Start")
					{
						@Override
						public void pressed()
							{
								// Check that all parameters are reasonable, if not warn user
								// Particle number
								if (particleNumberField.getValue() != Math.floor(particleNumberField.getValue())/* not a whole number */|| /* or less than 1 */particleNumberField.getValue() < 1)
									{
										WindowTools.informationWindow("Warning - Please enter a valid number of particles", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Chunk number
								if (chunkNumberField.getValue() != Math.floor(chunkNumberField.getValue())/* not a whole number */|| /* or less than 1 */chunkNumberField.getValue() < 1)
									{
										WindowTools.informationWindow("Warning - Please enter a valid number of chunks", "Cannot start Simulation");
										return; // Don't start the simulation yet
									}

								// Create a new simulation using the parameters set by the user.
								Main.sim = new Simulation((int) particleNumberField.getValue(), 1.0 / chunkNumberField.getValue());
								// Make the Simulation the current screen, instead of the ParameterInput.
								changeRenderableObject(Main.sim);
							}
					}, false);
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
				// Update the label, that informs us how many chunks will be simulated, whenever a key is pressed, just in case the chunk number has been updated
				totalChunks.setLabelText(" (Total chunks: " + (long) (chunkNumberField.getValue() * chunkNumberField.getValue() * chunkNumberField.getValue() * 50) + ") ");
				menu.setBorderSize(5);
			}
	}