package ParMod;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

/*
 * TCode is a library that takes care of user input and program flow, it also integrates with TComponents, which I use for buttons, text fields and menus.
 */
import tCode.TCode;

/**
 * @author Sebastian Troy
 */
public class Main extends TCode
	{
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

				programName = "FluidApp by Sebastian Troy";
				versionNumber = "1.0";
				frame.simplifyTitle(true);
				frame.setIconImage(loadImage("wave.png"));

				frameWidth = 900;
				frameHeight = 630;

				FORCE_SINGLE_THREAD = true;

				begin(new VectorFieldTester());
			}
		
		public static BufferedImage loadImage(String name)
			{
				try
					{
						return ImageIO.read(Main.class.getResource("/" + name));
					}
				catch (Exception e)
					{
						e.printStackTrace();
						return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
					}
			}
	}