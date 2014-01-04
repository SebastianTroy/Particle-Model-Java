package ParMod;

import tCode.TCode;

public class Main extends TCode
	{
		public static void main(String[] args)
			{
				new Main(true, false);
			}

		public Main(boolean framed, boolean resizable)
			{
				super(framed, resizable);
				programName = "ParMod";
				frameWidth = 900;
				frameHeight = 700;
				
				DEBUG = true;

				begin(new ParameterInput());
			}
	}