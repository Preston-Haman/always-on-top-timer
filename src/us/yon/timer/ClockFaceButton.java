package us.yon.timer;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;


public class ClockFaceButton extends JButton implements ClockFaceTime {

	/**
	 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * A unit of time that is displayable on this clock.
	 */
	private int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
	
	public ClockFaceButton() {
		super();
		setFocusable(false);
		setLayout(new GridLayout(1, 0, 3, 0));
		
		setTime(0, 0, 0, 0, 0, 0);
	}
	
	@Override
	public int[] getTime() {
		return new int[] {seconds, decaSeconds, minutes, decaMinutes, hours, decaHours};
	}
	
	@Override
	public void setTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
		this.seconds = seconds;
		this.decaSeconds = decaSeconds;
		this.minutes = minutes;
		this.decaMinutes = decaMinutes;
		this.hours = hours;
		this.decaHours = decaHours;
		
		removeAll();
		for (JLabel timeLabel: ClockFace.getLabelsForTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours)) {
			add(timeLabel);
		}
		repaint();
		revalidate();
	}
}
