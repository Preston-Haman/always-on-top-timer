package us.yon.timer.graphics;

import java.io.IOException;

import javax.imageio.ImageIO;

import darrylbu.icon.StretchIcon;

public class ClockFaceGraphics {
	
	private static final StretchIcon[] GRAPHICS;
	
	static {
		//Ten digits, and a colon
		GRAPHICS = new StretchIcon[11];
		
		try {
			for (int i = 0; i < GRAPHICS.length - 1; i++) {
				GRAPHICS[i] = new StretchIcon(ImageIO.read(ClockFaceGraphics.class.getResource("Clock_Number_" + i + ".png")));
			}
			
			GRAPHICS[GRAPHICS.length - 1] = new StretchIcon(ImageIO.read(ClockFaceGraphics.class.getResource("Clock_Colon.png")));
		} catch (IOException e) {
			//TODO: Decide how to handle this. The graphics are important, so maybe just close the application?
		}
	}
	
	public static StretchIcon[] getClockGraphics() {
		return GRAPHICS;
	}
	
}
