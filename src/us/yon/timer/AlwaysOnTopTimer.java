package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
	
	private enum ButtonBarState {
		NONE,
		STARTED,
		PAUSED,
		RESUMED,
		RESET,
		STOPWATCH,
		COUNTDOWN,
		COUNTED_DOWN;
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
		
		window.add(new JPanel(), BorderLayout.NORTH);
		window.add(new JPanel(), BorderLayout.SOUTH);
		window.add(new JPanel(), BorderLayout.EAST);
		window.add(new JPanel(), BorderLayout.WEST);
		
		window.setSize(242 + 14, 136 + 7); // Windows 10 takes the extra amounts and uses them as a shadow for the window
		window.setMaximumSize(window.getSize());
		window.setLocation(415 - 7, 823);
		swingTimer = new Timer(100, this);
		swingTimer.setInitialDelay(0);
		window.setVisible(true);
	}
	
	private void addClockFaceAndButtons(TimerType type) {
		Component northBorder = ((BorderLayout) window.getContentPane().getLayout()).getLayoutComponent(BorderLayout.NORTH);
		Component southBorder = ((BorderLayout) window.getContentPane().getLayout()).getLayoutComponent(BorderLayout.SOUTH);
		
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
				changeButtonState(ButtonBarState.STOPWATCH);
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
				changeButtonState(ButtonBarState.COUNTDOWN);
				break;
			default:
				throw new IllegalArgumentException("The paramater 'type' must be one of the type constants defined in the AlwaysOnTopTimer Class.");
		}
		currentType = type;
	}
	
	private void changeButtonState(ButtonBarState newState) {
		buttonPanel.removeAll();
		switch (newState) {
			case COUNTDOWN:
				buttonPanel.add(back);
				buttonPanel.add(setup);
				return;
			case COUNTED_DOWN:
				buttonPanel.add(back);
				buttonPanel.add(reset);
				return;
			case NONE:
				//Nothing to do
				return;
			case PAUSED:
				buttonPanel.add(currentType == TimerType.INTERVAL ? setup : back);
				buttonPanel.add(reset);
				buttonPanel.add(resume);
				return;
			case RESET:
			case STOPWATCH:
				buttonPanel.add(back);
				buttonPanel.add(currentType == TimerType.STOPWATCH ? reset : setup);
				buttonPanel.add(start);
				return;
			case RESUMED:
			case STARTED:
				buttonPanel.add(currentType == TimerType.INTERVAL ? setup : back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				return;
			default:
				throw new IllegalArgumentException("Unsupported ButtonBarState: " + newState);
		}
	}
	
	private void returnToStartWindow() {
		if (swingTimer.isRunning()) {
			windowTimerPanel.stop();
			swingTimer.stop();
		}
		window.remove(windowTimerPanel);
		window.add(startingWindowPanel, BorderLayout.CENTER);
		window.remove(buttonPanel);
		window.add(new JPanel(), BorderLayout.SOUTH);
		resetEntireSystem();
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
		window.add(new JPanel(), BorderLayout.NORTH);
		
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
		changeButtonState(ButtonBarState.NONE);
		
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
		Component westBorder = ((BorderLayout) window.getContentPane().getLayout()).getLayoutComponent(BorderLayout.WEST);
		Component eastBorder = ((BorderLayout) window.getContentPane().getLayout()).getLayoutComponent(BorderLayout.EAST);
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
		} else if (e.getSource() == start) {
			if (currentType != TimerType.STOPWATCH) {
				if (swingTimer.getDelay() != 100) {
					swingTimer.setDelay(100);
				}
			}
			
			if (intervalTracker != null) {
				assert currentType == TimerType.INTERVAL;
				intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "start"));
			}
			
			windowTimerPanel.start();
			changeButtonState(ButtonBarState.STARTED);
		} else if (e.getSource() == pause) {
			windowTimerPanel.stop();
			
			if (intervalTracker != null) {
				assert currentType == TimerType.INTERVAL;
				intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stop"));
			}
			
			changeButtonState(ButtonBarState.PAUSED);
		} else if (e.getSource() == resume) {
			windowTimerPanel.start();
			changeButtonState(ButtonBarState.RESUMED);
		} else if (e.getSource() == reset) {
			windowTimerPanel.stop();
			
			if (swingTimer.isRunning()) {
				swingTimer.stop();
			}
			
			if (flashing) {
				flashing = false;
				borderAlertFlashing();
			}
			
			switch (currentType) {
				case COUNTDOWN:
					windowTimerPanel.displayTimeOnClockFace(countdownInput);
					break;
				case INTERVAL:
					windowTimerPanel.setTime(intervalTracker.resetActiveInterval());
					intervalTracker.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stop"));
					break;
				case STOPWATCH:
					windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
					break;
				default:
					throw new IllegalStateException("Cannot reset timer due to illegal state; TimerType: " + currentType);
			}
			
			changeButtonState(ButtonBarState.RESET);
		} else if (e.getSource() == windowTimerPanel) {
			if (currentType == TimerType.INTERVAL) windowTimerPanel.setTime(intervalTracker.advanceToNextInterval());
			swingTimer.start();
		} else if (e.getSource() == swingTimer) {
			if (swingTimer.getDelay() == 100) {
				swingTimer.stop();
				swingTimer.setDelay(1000);
				swingTimer.start();
			} else {
				swingTimer.stop();
				swingTimer.setDelay(700);
				swingTimer.start();
			}
			
			if (!flashing) {
				flashing = true;
			} else if (currentType == TimerType.INTERVAL) {
				flashing = false;
				swingTimer.stop();
				swingTimer.setDelay(100);
			}
			
			if (currentType == TimerType.COUNTDOWN) changeButtonState(ButtonBarState.COUNTED_DOWN);
			borderAlertFlashing();
		}
		window.repaint();
		window.revalidate();
	}
}
