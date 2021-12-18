package us.yon.timer;


public interface ClockFace {
	
	public int[] getTime();
	
	public void setTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours);
	
}
