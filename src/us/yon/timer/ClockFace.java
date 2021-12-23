package us.yon.timer;


public interface ClockFace {
	
	public int[] getTime();
	
	public void setTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours);
	
	public default void setTime(ClockFace clockface) {
		int[] time = clockface.getTime();
		setTime(time[0], time[1], time[2], time[3], time[4], time[5]);
	}
	
	public default void displayTimeOnClockFace(int...input) {
		switch (input.length) {
			case 0:
				setTime(0, 0, 0, 0, 0, 0);
				break;
			case 1:
				setTime(input[0], 0, 0, 0, 0, 0);
				break;
			case 2:
				setTime(input[1], input[0], 0, 0, 0, 0);
				break;
			case 3:
				setTime(input[2], input[1], input[0], 0, 0, 0);
				break;
			case 4:
				setTime(input[3], input[2], input[1], input[0], 0, 0);
				break;
			case 5:
				setTime(input[4], input[3], input[2], input[1], input[0], 0);
				break;
			case 6:
				setTime(input[5], input[4], input[3], input[2], input[1], input[0]);
				break;
			default:
				throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
	}
	
}
