package ParMod;

import java.awt.Graphics2D;

import tCode.RenderableObject;
import tComponents.components.TButton;
import tComponents.components.TLabel;
import tComponents.components.TMenu;
import tComponents.components.TNumberField;

/**
 * 
 * @author Sebastian Troy
 */
public class ParameterInput extends RenderableObject
	{

		private TMenu menu;
		private TNumberField particleNumberField;

		@Override
		protected void initiate()
			{
				menu = new TMenu(0, 0, Main.canvasWidth, Main.canvasHeight, TMenu.VERTICAL);
				menu.setBorderSize(1);
				add(menu);

				// Create a text box that only accepts numbers up to 8 digits long
				particleNumberField = new TNumberField(0, 0, 125, 25, 8);
				particleNumberField.setText("10000");

				// Add the components to a menu that automatically arranges everything on screen
				menu.add(new TLabel("Number of Particles:"), false);
				menu.add(particleNumberField, false);
				// Add a button that will begin the simulation when pressed.
				menu.add(new TButton("Start")
					{
						@Override
						public void pressed()
							{
								// Create a new simulation using the parameters set by the user.
								Main.sim = new Simulation((int) particleNumberField.getValue());
								// Make the Simulation the current screen, instead of the ParameterInput.
								changeRenderableObject(Main.sim);
							}
					}, false);
			}

		// The following methods are not used but the supertype for this class requires them.
		
		@Override
		public void tick(double secondsPassed)
			{}

		@Override
		protected void render(Graphics2D g)
			{}
	}