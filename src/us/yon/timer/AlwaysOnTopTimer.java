package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import us.yon.timer.IntervalTracker.IntervalInputListener;
import us.yon.timer.KeypadInputDialog.KeypadInputListener;

public class AlwaysOnTopTimer implements ActionListener {
	
	private enum TimerType {
		NONE,
		STOPWATCH,
		COUNTDOWN,
		INTERVAL;
	}
	
	static Color defaultPanelColor = new JPanel().getBackground();
	static Color flashColor = new Color(204, 204, 255);
	static Color activeIntervalColor = new Color(217, 39, 66);
	
	private TimerType currentType = TimerType.NONE;
	
	private int[] countdownInput;
		
	private JFrame window = new JFrame("Yon Timer");
	
	private IntervalTracker intervalTracker;
	
	private Timer swingTimer;
	
	private ClockFacePanel windowTimerPanel = new ClockFacePanel(this);
	
	private JPanel startingWindowPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	
	private JPanel northBorder = new JPanel();
	private JPanel southBorder = new JPanel();
	private JPanel eastBorder = new JPanel();
	private JPanel westBorder = new JPanel();
	
	private boolean flashing = false;
	
	private JButton setup = new JButton("Setup");
	private JButton reset = new JButton("Reset");
	private JButton pause = new JButton("Pause");
	private JButton resume = new JButton("Resume");
	private JButton start = new JButton("Start");
	private JButton stopwatch = new JButton("Stopwatch");
	private JButton countdown = new JButton("Countdown");
	private JButton interval = new JButton("Interval");
	private JButton back = new JButton("Back");
	
	private AlwaysOnTopTimer() {
		setup.addActionListener(this);
		reset.addActionListener(this);
		pause.addActionListener(this);
		resume.addActionListener(this);
		start.addActionListener(this);
		stopwatch.addActionListener(this);
		countdown.addActionListener(this);
		interval.addActionListener(this);
		back.addActionListener(this);
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setAlwaysOnTop(true);
		window.setLayout(new BorderLayout());
		
		startingWindowPanel.setLayout(new GridLayout(3, 1));
		startingWindowPanel.add(stopwatch);
		startingWindowPanel.add(countdown);
		startingWindowPanel.add(interval);
		window.add(startingWindowPanel, BorderLayout.CENTER);
		
		window.add(northBorder, BorderLayout.NORTH);
		window.add(southBorder, BorderLayout.SOUTH);
		window.add(eastBorder, BorderLayout.EAST);
		window.add(westBorder, BorderLayout.WEST);
		
		window.setSize(242 + 14, 136 + 7); // Windows 10 takes the extra amounts and uses them as a shadow for the window
		window.setMaximumSize(window.getSize());
		window.setLocation(415 - 7, 823);
		swingTimer = new Timer(100, this);
		swingTimer.setInitialDelay(0);
		window.setVisible(true);
	}
	
	private void returnToStartWindow() {
		if (swingTimer.isRunning()) {
			windowTimerPanel.stop();
			swingTimer.stop();
		}
		window.remove(windowTimerPanel);
		window.add(startingWindowPanel, BorderLayout.CENTER);
		window.remove(buttonPanel);
		window.add(southBorder, BorderLayout.SOUTH);
		resetEntireSystem();
	}
	
	private void addClockFaceAndButtons(TimerType type) {
		switch (type) {
			case STOPWATCH:
				windowTimerPanel.setCountdown(false);
				windowTimerPanel.setStopAtZero(true);
				
				window.remove(startingWindowPanel);
				window.setTitle("Yon Timer -- Stopwatch");
				window.add(windowTimerPanel, BorderLayout.CENTER);
				window.remove(northBorder);
				window.add(windowTimerPanel.getTenthsIndicator(), BorderLayout.NORTH);
				window.remove(southBorder);
				window.add(buttonPanel, BorderLayout.SOUTH);
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(start);
				break;
			case COUNTDOWN:
			case INTERVAL:
				if (type == TimerType.COUNTDOWN) {
					window.setTitle("Yon Timer -- Countdown");
					windowTimerPanel.setStopAtZero(true);
				} else if (type == TimerType.INTERVAL) {
					window.setTitle("Yon Timer -- Interval");
					windowTimerPanel.setStopAtZero(false);
				}
				windowTimerPanel.setCountdown(true);
				
				window.remove(startingWindowPanel);
				window.add(windowTimerPanel, BorderLayout.CENTER);
				window.remove(northBorder);
				window.add(windowTimerPanel.getTenthsIndicator(), BorderLayout.NORTH);
				window.remove(southBorder);
				window.add(buttonPanel, BorderLayout.SOUTH);
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				break;
			default:
				throw new IllegalArgumentException("The paramater 'type' must be one of the type constants defined in the AlwaysOnTopTimer Class.");
		}
		currentType = type;
	}
	
	private void resetEntireSystem() {
		windowTimerPanel.stop();
		if (swingTimer.isRunning()) {
			swingTimer.stop();
		}
		
		if (swingTimer.getDelay() != 100) {
			swingTimer.setDelay(100);
		}
		
		window.remove(windowTimerPanel.getTenthsIndicator());
		window.add(northBorder, BorderLayout.NORTH);
		
		if (flashing) {
			flashing = false;
		}
		
		if (windowTimerPanel.getBackground() == flashColor) {
			borderAlertFlashing();
		}
		
		countdownInput = null;
		
		if (intervalTracker != null) intervalTracker.dispose();
		intervalTracker = null;
		
		currentType = TimerType.NONE;
		windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
		window.setTitle("Yon Timer");
	}
	
	private void callSetupWindow() {
		if (currentType == TimerType.COUNTDOWN) {
			int[] oldTime = windowTimerPanel.getTime();
			
			final int oldSeconds = oldTime[0], oldDecaSeconds = oldTime[1],
					  oldMinutes = oldTime[2], oldDecaMinutes = oldTime[3],
					  oldHours = oldTime[4], oldDecaHours = oldTime[5];
			
			KeypadInputListener listener = new KeypadInputListener() {
				@Override
				public void previewInput(int... currentInput) {
					windowTimerPanel.displayTimeOnClockFace(currentInput);
				}
				
				@Override
				public void confirmInput(int... currentInput) {
					windowTimerPanel.displayTimeOnClockFace(currentInput);
					
					countdownInput = currentInput;
					buttonPanel.removeAll();
					buttonPanel.add(back);
					buttonPanel.add(setup);
					buttonPanel.add(start);
					window.repaint();
					window.revalidate();
				}
				
				@Override
				public void cancelInput() {
					windowTimerPanel.setTime(oldSeconds, oldDecaSeconds, oldMinutes, oldDecaMinutes, oldHours, oldDecaHours);
				}
			};
			
			new KeypadInputDialog(window, "Set Countdown:", listener);
		} else if (currentType == TimerType.INTERVAL) {
			if (intervalTracker != null) {
				intervalTracker.setVisible(true);
			} else {
				IntervalInputListener listener = new IntervalInputListener() {
					@Override
					public void intervalAdded() {
						if (intervalTracker.getIntervalCount() == 1) {
							buttonPanel.add(start);
							
							windowTimerPanel.setTime(intervalTracker.getActiveInterval());
							window.repaint();
							window.revalidate();
						}
					}
					
					@Override
					public void intervalRemoved() {
						//If there aren't any intervals left, the countdown is stopped.
						if (intervalTracker.getIntervalCount() == 0) {
							buttonPanel.remove(start);
							
							windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
							window.repaint();
							window.revalidate();
						}
					}
					
					@Override
					public void activeIntervalChangedByUser() {
						windowTimerPanel.setTime(intervalTracker.getActiveInterval());
					}
				};
				
				intervalTracker = new IntervalTracker(window, listener);
			}
		}
	}
	
	private void borderAlertFlashing() {
		if (windowTimerPanel.getBackground() == defaultPanelColor && flashing) {
			westBorder.setBackground(flashColor);
			eastBorder.setBackground(flashColor);
			windowTimerPanel.setBackground(flashColor);
		} else {
			westBorder.setBackground(defaultPanelColor);
			eastBorder.setBackground(defaultPanelColor);
			windowTimerPanel.setBackground(defaultPanelColor);
		}
	}
	
	public static void main(String[] args) {
		new AlwaysOnTopTimer();
		
		//Setting the L&F should probably come first... or at least be placed on the EDT.
		//Setting it after the UI is constructed creates a mixed effect which is desired.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			//Do nothing; if the system L&F is not available, then that just sucks.
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == stopwatch) {
			addClockFaceAndButtons(TimerType.STOPWATCH);
		} else if (e.getSource() == countdown) {
			addClockFaceAndButtons(TimerType.COUNTDOWN);
		} else if (e.getSource() == interval) {
			addClockFaceAndButtons(TimerType.INTERVAL);
		} else if (e.getSource() == back) {
			returnToStartWindow();
		} else if (e.getSource() == setup) {
			callSetupWindow();
		} else if (currentType == TimerType.STOPWATCH) {
			if (e.getSource() == start) {
				windowTimerPanel.start();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(resume);
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
			} else if (e.getSource() == reset) {
				if (windowTimerPanel.isRunning()) {
					windowTimerPanel.stop();
				}
				resetEntireSystem();
				addClockFaceAndButtons(TimerType.STOPWATCH);
			}
		} else if (currentType == TimerType.COUNTDOWN) {
			if (e.getSource() == windowTimerPanel || e.getSource() == swingTimer) {
				if (e.getSource() == windowTimerPanel) {
					swingTimer.start();
				} else {
					if (swingTimer.getDelay() == 100) {
						swingTimer.stop();
						swingTimer.setDelay(1000);
						swingTimer.start();
					} else {
						swingTimer.stop();
						swingTimer.setDelay(700);
						swingTimer.start();
					}
					if (flashing == false) {
						flashing = true;
					}
					buttonPanel.removeAll();
					buttonPanel.add(back);
					buttonPanel.add(reset);
					borderAlertFlashing();
				}
			} else if (e.getSource() == start) {
				windowTimerPanel.start();
				if (swingTimer.getDelay() != 100) {
					swingTimer.setDelay(100);
				}
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
			} else if (e.getSource() == reset) {
				if (windowTimerPanel.isRunning()) {
					windowTimerPanel.stop();
				}
				if (swingTimer.isRunning()) {
					swingTimer.stop();
				}
				if (flashing) {
					flashing = false;
					borderAlertFlashing();
				}
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				buttonPanel.add(start);
				windowTimerPanel.displayTimeOnClockFace(countdownInput);
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(resume);
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
			}
		} else if (currentType == TimerType.INTERVAL) {
			if (e.getSource() == start) {
				buttonPanel.remove(start);
				buttonPanel.remove(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				
				intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "start"));
				windowTimerPanel.start();
				if (swingTimer.getDelay() != 100) {
					swingTimer.setDelay(100);
				}
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stop"));
				
				buttonPanel.remove(pause);
				buttonPanel.add(resume);
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.remove(resume);
				buttonPanel.add(pause);
			} else if (e.getSource() == reset) {
				windowTimerPanel.stop();
				windowTimerPanel.setTime(intervalTracker.resetActiveInterval());
				intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stop"));
				
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				buttonPanel.add(start);
			} else if (e.getSource() == windowTimerPanel || e.getSource() == swingTimer) {
				if (e.getSource() == windowTimerPanel) {
					swingTimer.start();
				} else {
					if (swingTimer.getDelay() == 100) {
						swingTimer.stop();
						swingTimer.setDelay(1000);
						swingTimer.start();
					} else {
						swingTimer.stop();
						swingTimer.setDelay(700);
						swingTimer.start();
					}
					if (flashing == false) {
						flashing = true;
						windowTimerPanel.setTime(intervalTracker.advanceToNextInterval());
					} else if (flashing) {
						flashing = false;
						swingTimer.stop();
						swingTimer.setDelay(100);
					}
					borderAlertFlashing();
				}
			}
		}
		window.repaint();
		window.revalidate();
	}
}
