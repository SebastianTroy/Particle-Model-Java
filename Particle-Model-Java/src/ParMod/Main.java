package ParMod;

/*
 * TCode is a library that takes care of user input and program flow, it also integrates with TComponents, which I use for buttons, text fields and menus.
 */
import tCode.TCode;

/**
 * 
 * @author Sebastian Troy
 */
public class Main extends TCode
	{
		/*
		 * These classes are here so that they can be easily accessed by any part of the simulation.
		 */
		static Simulation sim; // Simulation not initialised until parameters are chosen.
		static GraphicalOutput graphicalOutput;

		/**
		 * This is the program entry.
		 */
		public static void main(String[] args)
			{
				// Create an instance of the Main.class (this file)
				new Main(true, false);
			}

		/**
		 * This constructor specifies the details needed to create the window that the program will run in and begins the program with a new instance of
		 * ParameterInput.
		 * 
		 * @param framed
		 *            - The window will be framed if true.
		 * @param resizable
		 *            - The window will be resizable if true.
		 */
		public Main(boolean framed, boolean resizable)
			{
				super(framed, resizable);
				programName = "Particle Model";
				versionNumber = "alpha";
				frame.simplifyTitle(true);
				frameWidth = 900;
				frameHeight = 700;

				DEBUG = true;

				begin(new ParameterInput());
			}
	}