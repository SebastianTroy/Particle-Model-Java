package ParMod;

import java.awt.Graphics2D;

import tCode.RenderableObject;
import tComponents.components.TButton;
import tComponents.components.TLabel;
import tComponents.components.TMenu;
import tComponents.components.TNumberField;
import tComponents.utils.events.TActionEvent;

public class ParameterInput extends RenderableObject
	{

		private TMenu menu;
		private TButton beginButton;
		private TNumberField particleNumberField;

		Simulation simulation;

		@Override
		protected void initiate()
			{
				menu = new TMenu(0, 0, Main.canvasWidth, Main.canvasHeight, TMenu.VERTICAL);
				menu.setBorderSize(1);
				add(menu);

				// Create a text box that only accepts numbers up to 8 digits
				particleNumberField = new TNumberField(0, 0, 125, 25, 8);
				particleNumberField.setText("10000");

				beginButton = new TButton("Start");

				// Add the components to a menu that automatically arranges everything
				menu.add(new TLabel("Number of Particles:"), false);
				menu.add(particleNumberField, false);
				menu.add(beginButton, false);
			}

		@Override
		public void tick(double secondsPassed)
			{}

		@Override
		protected void render(Graphics2D g)
			{}

		@Override
		public final void tActionEvent(TActionEvent e)
			{
				Object source = e.getSource();

				simulation = new Simulation((int) particleNumberField.getValue());

				if (source == beginButton)
					changeRenderableObject(simulation);
			}
	}