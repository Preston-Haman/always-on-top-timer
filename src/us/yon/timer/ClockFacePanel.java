package us.yon.timer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import us.yon.timer.graphics.ClockFaceGraphics;


public class ClockFacePanel extends JPanel implements ClockFace {
	
	/**
	 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
	 */
	private static final long serialVersionUID = 1L;
	
	public static JLabel[] getLabelsForTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
		JLabel[] clockDisplay = new JLabel[8];
		for (int i = 0; i < clockDisplay.length; i++) {
			clockDisplay[i] = new JLabel();
		}
		
		Icon[] graphics = ClockFaceGraphics.getClockGraphics();
		clockDisplay[0].setIcon(graphics[decaHours]);
		clockDisplay[1].setIcon(graphics[hours]);
		clockDisplay[2].setIcon(graphics[10]);
		clockDisplay[3].setIcon(graphics[decaMinutes]);
		clockDisplay[4].setIcon(graphics[minutes]);
		clockDisplay[5].setIcon(graphics[10]);
		clockDisplay[6].setIcon(graphics[decaSeconds]);
		clockDisplay[7].setIcon(graphics[seconds]);
		return clockDisplay;
	}
	
	/**
	 * A unit of time that is displayable on this clock.
	 */
	private int tenthSeconds, seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
	
	/**
	 * A {@link JPanel} that visually represents the value of {@link #tenthSeconds} to the user.
	 */
	private JPanel tenthsIndicator = new JPanel() {
		/**
		 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
		 */
		private static final long serialVersionUID = 1L;
		
		private JPanel init(ClockFacePanel clock) {
			this.clock = clock;
			return this;
		}
		
		private ClockFacePanel clock;
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (tenthSeconds == 0) return;
			
			int offset = Math.abs(getX() - clock.getX());
			int x = offset, y = 1;
			int barWidth = clock.getWidth();
			int modNine = barWidth % 9;
			int width, height = getHeight() - 1;
			
			if (countdown) {
				int correction = modNine - tenthSeconds;
				if (correction < 0) correction = 0;
				x += ((barWidth/9) * (9 - tenthSeconds)) + correction;
				width = (barWidth - x + offset);
			} else {
				int correction = modNine;
				if (correction > tenthSeconds) correction = tenthSeconds;
				width = (barWidth / 9) * tenthSeconds + correction;
			}
			
			g.setColor(new Color(112, 146, 190));
			g.fillRect(x, y, width, height);
		}
	}.init(this);
	
	/**
	 * Dictates whether this clock face ticks up or down.
	 * <p>
	 * When true, this clock face will tick down.
	 */
	boolean countdown;
	
	boolean stopAtZero;
	
	boolean ticking = false;
	
	Timer timer;
	
	ActionListener listener;
	
	JLabel[] clockDisplay = new JLabel[8];
	
	ClockFacePanel() {
		setLayout(new GridLayout(1, 0, 3, 0));
		stopAtZero = true;
		
		Icon[] graphics = ClockFaceGraphics.getClockGraphics();
		for (int i = 0; i < clockDisplay.length; i++) {
			clockDisplay[i] = new JLabel();
			
			if (i == 2 || i == 5)
				//Set colons
				clockDisplay[i].setIcon(graphics[10]);
			else
				//Set zero digit
				clockDisplay[i].setIcon(graphics[0]);
			
			add(clockDisplay[i]);
		}
	}
	
	ClockFacePanel(ActionListener listener) {
		this();
		this.listener = listener;
	}
	
	/**
	 * Returns {@link #tenthsIndicator}, so it may be added to the UI separately.
	 *  
	 * @return {@link #tenthsIndicator}.
	 */
	JPanel getTenthsIndicator() {
		return tenthsIndicator;
	}
	
	/**
	 * Sets {@link #countdown} to the given value.
	 */
	void setCountdown(boolean countdown) {
		this.countdown = countdown;
	}
	
	void setStopAtZero(boolean stopAtZero) {
		this.stopAtZero = stopAtZero;
	}
	
	@Override
	public int[] getTime() {
		return new int[] {seconds, decaSeconds, minutes, decaMinutes, hours, decaHours};
	}
	
	@Override
	public void setTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
		tenthSeconds = 0;
		this.seconds = seconds;
		this.decaSeconds = decaSeconds;
		this.minutes = minutes;
		this.decaMinutes = decaMinutes;
		this.hours = hours;
		this.decaHours = decaHours;
		updateClockFace();
	}
	
	void start() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(() -> {
					tick();
					updateClockFace();
					fireActionEventIfCountdownReachedZero();
				});
			}
		}, 100, 100);
		ticking = true;
	}
	
	void stop() {
		if (ticking) {
			timer.cancel();
			ticking = false;
		}
	}
	
	boolean isRunning() {
		return ticking;
	}
	
	void tick() {
		if (countdown) {
			if (tenthSeconds > 0) {
				tenthSeconds--;
				return;
			}
			if (tenthSeconds == 0 && (seconds > 0 || decaSeconds > 0 || minutes > 0 || decaMinutes > 0 || hours > 0 || decaHours > 0)) {
				tenthSeconds = 9;
				if (seconds > 0) {
					seconds--;
					return;
				}
				if (seconds == 0 && (decaSeconds > 0 || minutes > 0 || decaMinutes > 0 || hours > 0 || decaHours > 0)) {
					seconds = 9;
					if (decaSeconds > 0) {
						decaSeconds--;
						return;
					}
					if (decaSeconds == 0 && (minutes > 0 || decaMinutes > 0 || hours > 0 || decaHours > 0)) {
						decaSeconds = 5;
						if (minutes > 0) {
							minutes--;
							return;
						}
						if (minutes == 0 && (decaMinutes > 0 || hours > 0 || decaHours > 0)) {
							minutes = 9;
							if (decaMinutes > 0) {
								decaMinutes--;
								return;
							}
							if (decaMinutes == 0 && (hours > 0 || decaHours > 0)) {
								decaMinutes = 5;
								if (hours > 0) {
									hours--;
									return;
								}
								if (hours == 0 && decaHours > 0) {
									hours = 9;
									if (decaHours > 0) {
										decaHours--;
									}
								}
							}
						}
					}
				}
			}
		} else {
			tenthSeconds++;
			if (tenthSeconds == 10) {
				tenthSeconds = 0;
				seconds++;
				if (seconds == 10) {
					seconds = 0;
					decaSeconds++;
					if (decaSeconds == 6) {
						decaSeconds = 0;
						minutes++;
						if (minutes == 10) {
							minutes = 0;
							decaMinutes++;
							if (decaMinutes == 6) {
								decaMinutes = 0;
								hours++;
								if (hours == 10) {
									hours = 0;
									decaHours++;
									if (decaHours == 10) {
										decaHours = 0;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void updateClockFace() {
		Icon[] graphics = ClockFaceGraphics.getClockGraphics();
		
		for (int i = 0; i < clockDisplay.length; i++) {
			switch (i) {
				case 2:
				case 5:
					//Colon slots. Do nothing.
					break;
				case 0: //decaHours
					clockDisplay[i].setIcon(graphics[decaHours]);
					break;
				case 1: //hours
					clockDisplay[i].setIcon(graphics[hours]);
					break;
				case 3: //decaMinutes
					clockDisplay[i].setIcon(graphics[decaMinutes]);
					break;
				case 4: //minutes
					clockDisplay[i].setIcon(graphics[minutes]);
					break;
				case 6: //decaSeconds
					clockDisplay[i].setIcon(graphics[decaSeconds]);
					break;
				case 7: //seconds
					clockDisplay[i].setIcon(graphics[seconds]);
					break;
			}
		}
		
		if (tenthsIndicator.isVisible()) {
			tenthsIndicator.repaint();
			tenthsIndicator.revalidate();
		}
		if (isVisible()) {
			repaint();
			revalidate();
		}
	}
	
	private void fireActionEventIfCountdownReachedZero() {
		if (!countdown) return;
		if (tenthSeconds == 0 & seconds == 0 & decaSeconds == 0 & minutes == 0 & decaMinutes == 0 & hours == 0 & decaHours == 0) {
			if (stopAtZero) stop();
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
		}
	}
}
