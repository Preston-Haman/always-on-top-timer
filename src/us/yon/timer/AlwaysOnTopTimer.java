package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import us.yon.timer.KeypadInputDialog.AbstractKeypadInputListener;

public class AlwaysOnTopTimer implements ActionListener {
	
	private enum TimerType {
		NONE,
		STOPWATCH,
		COUNTDOWN,
		INTERVAL;
	}
	
	private TimerType currentType = TimerType.NONE;
	
	private int[] countdownInput;
	
	private int clockFaceInterval = 0;
	private JFrame window = new JFrame("Yon Timer");
	private JFrame intervalTracker;
	private JDialog prompt;
	private Timer swingTimer;
	private JPanel startingWindowPanel = new JPanel();
	private ClockFace windowTimerPanel = new ClockFace(this);
	private JPanel buttonPanel = new JPanel();
	private JPanel intervalTrackerPanel;
	private JPanel intervalTrackerButtonPanel;
	private JPanel northBorder = new JPanel();
	private JPanel southBorder = new JPanel();
	private JPanel eastBorder = new JPanel();
	private JPanel westBorder = new JPanel();
	private final ArrayList<ClockFaceButton> intervalTrackingUIArray = new ArrayList<>();
	private boolean flashing = false;
	private boolean firstIntervalOpen = true;
	private Color defaultPanelColor;
	private Color flashColor = new Color(204, 204, 255);
	private Color activeIntervalColor = new Color(217, 39, 66);
	private JButton setup = new JButton("Setup");
	private JButton reset = new JButton("Reset");
	private JButton pause = new JButton("Pause");
	private JButton resume = new JButton("Resume");
	private JButton start = new JButton("Start");
	private JButton stopwatch = new JButton("Stopwatch");
	private JButton countdown = new JButton("Countdown");
	private JButton interval = new JButton("Interval");
	private JButton addInterval = new JButton("Add Interval");
	private JButton clearIntervals = new JButton("Clear All");
	private JButton back = new JButton("Back");
	private JButton delete;
	private JButton skipto;
	private JButton edit;
	
	private AlwaysOnTopTimer() {
		setup.addActionListener(this);
		reset.addActionListener(this);
		pause.addActionListener(this);
		resume.addActionListener(this);
		start.addActionListener(this);
		stopwatch.addActionListener(this);
		countdown.addActionListener(this);
		interval.addActionListener(this);
		addInterval.addActionListener(this);
		clearIntervals.addActionListener(this);
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
		
		defaultPanelColor = northBorder.getBackground();
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
		
		if (westBorder.getBackground() == flashColor) {
			borderAlertFlashing();
		}
		
		if (countdownInput != null)
			for (int i = 0; i < countdownInput.length; i++) {
				countdownInput[i] = 0;
			}
		
		currentType = TimerType.NONE;
		windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
		window.setTitle("Yon Timer");
	}
	
	private void callSetupWindow(final ClockFaceTime clockface, Frame owner, boolean newInterval) {
		int[] oldTime = clockface.getTime();
		
		final int oldSeconds = oldTime[0], oldDecaSeconds = oldTime[1],
				  oldMinutes = oldTime[2], oldDecaMinutes = oldTime[3],
				  oldHours = oldTime[4], oldDecaHours = oldTime[5];
		
		AbstractKeypadInputListener listener = new AbstractKeypadInputListener() {
			@Override
			public void previewInput(int... currentInput) {
				displayTimeOnClockFace(clockface, currentInput);
			}
			
			@Override
			public void confirmInput(int... currentInput) {
				displayTimeOnClockFace(clockface, currentInput);
				
				if (currentType == TimerType.COUNTDOWN) {
					countdownInput = currentInput;
					buttonPanel.removeAll();
					buttonPanel.add(back);
					buttonPanel.add(setup);
					buttonPanel.add(start);
					window.repaint();
					window.revalidate();
				}
				
				if (currentType == TimerType.INTERVAL) {
					if (!windowTimerPanel.isRunning()) {
						intervalClockFaceUpdate();
					}
					intervalTrackerColorIndicatorUpdate();
				}
			}
			
			@Override
			public void cancelInput() {
				clockface.setTime(oldSeconds, oldDecaSeconds, oldMinutes, oldDecaMinutes, oldHours, oldDecaHours);
				if (newInterval) {
					intervalTracker.setSize(intervalTracker.getWidth(), intervalTracker.getHeight() - 55);
					intervalTrackingUIArray.remove(intervalTrackingUIArray.size() - 1);
					if (intervalTrackingUIArray.size() == 0) {
						intervalTrackerButtonPanel.remove(clearIntervals);
						buttonPanel.remove(start);
						window.repaint();
						window.revalidate();
					}
					Point intervalTrackerPoint = intervalTracker.getLocation();
					int x = (int) intervalTrackerPoint.getX();
					int y = (int) intervalTrackerPoint.getY();
					intervalTracker.setLocation(x, y + 55);
					intervalTracker.repaint();
					intervalTracker.revalidate();
					intervalTrackerColorIndicatorUpdate(); // also calls repaint/revalidate
				}
			}
		};
		
		new KeypadInputDialog(owner, "Set Countdown:", listener);
	}
	
	private static void displayTimeOnClockFace(ClockFaceTime clockface, int...input) {
		switch (input.length) {
			case 0:
				clockface.setTime(0, 0, 0, 0, 0, 0);
				break;
			case 1:
				clockface.setTime(input[0], 0, 0, 0, 0, 0);
				break;
			case 2:
				clockface.setTime(input[1], input[0], 0, 0, 0, 0);
				break;
			case 3:
				clockface.setTime(input[2], input[1], input[0], 0, 0, 0);
				break;
			case 4:
				clockface.setTime(input[3], input[2], input[1], input[0], 0, 0);
				break;
			case 5:
				clockface.setTime(input[4], input[3], input[2], input[1], input[0], 0);
				break;
			case 6:
				clockface.setTime(input[5], input[4], input[3], input[2], input[1], input[0]);
				break;
			default:
				throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
	}
	
	private void callIntervalPromptWindow(String command) {
		edit = new JButton("Edit");
		edit.addActionListener(this);
		skipto = new JButton("Skip To");
		skipto.addActionListener(this);
		delete = new JButton("Delete");
		delete.addActionListener(this);
		prompt = new JDialog(intervalTracker, "Interval " + (Integer.parseInt(command) + 1));
		prompt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		prompt.setLayout(new BorderLayout());
		prompt.add(new JPanel(), BorderLayout.NORTH);
		prompt.add(new JPanel(), BorderLayout.SOUTH);
		prompt.add(new JPanel(), BorderLayout.EAST);
		prompt.add(new JPanel(), BorderLayout.WEST);
		JPanel buttons = new JPanel();
		prompt.add(buttons, BorderLayout.CENTER);
		buttons.add(edit);
		buttons.add(skipto);
		buttons.add(delete);
		edit.setActionCommand(command);
		skipto.setActionCommand(command);
		delete.setActionCommand(command);
		if (windowTimerPanel.isRunning()) {
			edit.setEnabled(false);
		} else {
			edit.setEnabled(true);
		}
		prompt.setSize(241 + 14, 89 + 7);
		prompt.setLocationRelativeTo(intervalTracker);
		prompt.setAlwaysOnTop(true);
		prompt.setModal(true);
		prompt.setVisible(true);
	}
	
	private void callIntervalTrackerWindow() {
		if (firstIntervalOpen) {
			intervalTracker = new JFrame();
			intervalTracker.setTitle("Intervals:");
			intervalTracker.setLayout(new BorderLayout());
			intervalTracker.add(new JPanel(), BorderLayout.NORTH);
			intervalTracker.add(new JPanel(), BorderLayout.EAST);
			intervalTracker.add(new JPanel(), BorderLayout.WEST);
			intervalTrackerPanel = new JPanel();
			intervalTracker.add(intervalTrackerPanel, BorderLayout.CENTER);
			intervalTrackerPanel.setLayout(new BoxLayout(intervalTrackerPanel, BoxLayout.Y_AXIS));
			intervalTrackerButtonPanel = new JPanel();
			intervalTrackerButtonPanel.add(addInterval);
			intervalTrackerPanel.add(intervalTrackerButtonPanel);
			intervalTracker.add(intervalTrackerButtonPanel, BorderLayout.SOUTH);
			intervalTracker.setSize(256, 136 + 7 - 55);
			Point windowPoint = window.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			intervalTracker.setLocation((int) (x + window.getWidth() - 14), y - (intervalTracker.getHeight() - window.getHeight()));
			intervalTracker.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			intervalTracker.setAlwaysOnTop(true);
			firstIntervalOpen = false;
			intervalTracker.setVisible(true);
		} else {
			intervalTracker.setVisible(true);
		}
	}
	
	private void addInterval() {
		ClockFaceButton clockFaceButton = new ClockFaceButton();
		clockFaceButton.addActionListener(this);
		clockFaceButton.setActionCommand("" + intervalTrackingUIArray.size());
		intervalTrackingUIArray.add(clockFaceButton);
		intervalTrackerPanel.add(clockFaceButton);
	}
	
	private void deleteIntervalArrayIndex(int intervalIndex) {
		intervalTrackingUIArray.remove(intervalIndex);
		
		int c = 0;
		intervalTrackerPanel.removeAll();
		for (ClockFaceButton clockfaceButton: intervalTrackingUIArray) {
			clockfaceButton.setActionCommand("" + c++);
			intervalTrackerPanel.add(clockfaceButton);
		}
		intervalTracker.setSize(intervalTracker.getWidth(), intervalTracker.getHeight() - 55);
		if (intervalTrackingUIArray.size() == 0) {
			intervalTrackerButtonPanel.remove(clearIntervals);
			buttonPanel.remove(start);
			window.repaint();
			window.revalidate();
		}
		Point intervalTrackerPoint = this.intervalTracker.getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		this.intervalTracker.setLocation(x, y + 55);
		
		intervalTrackerColorIndicatorUpdate();
	}
	
	private void borderAlertFlashing() {
		if (westBorder.getBackground() == defaultPanelColor && flashing) {
			westBorder.setBackground(flashColor);
			eastBorder.setBackground(flashColor);
			windowTimerPanel.setBackground(flashColor);
		} else {
			westBorder.setBackground(defaultPanelColor);
			eastBorder.setBackground(defaultPanelColor);
			windowTimerPanel.setBackground(defaultPanelColor);
		}
	}
	
	private void intervalClockFaceUpdate() {
		if (clockFaceInterval >= intervalTrackingUIArray.size()) {
			clockFaceInterval = 0;
		}
		
		if (intervalTrackingUIArray.size() > 0) {
			int[] time = intervalTrackingUIArray.get(clockFaceInterval).getTime();
			windowTimerPanel.setTime(time[0], time[1], time[2], time[3], time[4], time[5]);
		} else {
			windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
		}
		intervalTrackerColorIndicatorUpdate();
	}
	
	private void intervalTrackerColorIndicatorUpdate() {
		for (int i = 0; i < intervalTrackingUIArray.size(); i++) {
			intervalTrackingUIArray.get(i).setBackground(defaultPanelColor);
		}
		if (clockFaceInterval >= 0 && clockFaceInterval < intervalTrackingUIArray.size()) {
			intervalTrackingUIArray.get(clockFaceInterval).setBackground(activeIntervalColor);
			intervalTrackingUIArray.get(clockFaceInterval).setBorderPainted(true);
			intervalTracker.repaint();
			intervalTracker.revalidate();
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
			} else if (e.getSource() == setup) {
				callSetupWindow(windowTimerPanel, window, false);
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
				displayTimeOnClockFace(windowTimerPanel, countdownInput);
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
			if (e.getSource() == setup) {
				callIntervalTrackerWindow();
			} else if (e.getSource() == addInterval) {
				addInterval();
				
				if (!windowTimerPanel.isRunning()) {
					buttonPanel.add(start);
					intervalTrackerButtonPanel.add(clearIntervals);
				}
				
				Point windowPoint = intervalTracker.getLocation();
				intervalTracker.setLocation((int) windowPoint.getX(), (int) windowPoint.getY() - 55);
				Dimension windowSize = intervalTracker.getSize();
				int windowHeight = windowSize.height;
				int windowWidth = windowSize.width;
				intervalTracker.setSize(windowWidth, windowHeight + 55);
				intervalTracker.repaint();
				intervalTracker.revalidate();
				intervalTrackerColorIndicatorUpdate(); // also calls repaint/revalidate
				callSetupWindow(intervalTrackingUIArray.get(intervalTrackingUIArray.size() - 1), intervalTracker, true);
			} else if (e.getSource() == clearIntervals) {
				intervalTrackerPanel.removeAll();
				intervalTrackerButtonPanel.removeAll();
				intervalTrackerButtonPanel.add(addInterval);
				intervalTracker.repaint();
				intervalTracker.revalidate();
				buttonPanel.remove(start);
				intervalTracker.setSize(256, 136 + 7 - 55);
				Point intervalTrackerPoint = intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				intervalTracker.setLocation(x, y + (55 * intervalTrackingUIArray.size()));
				
				intervalTrackingUIArray.clear();
				intervalClockFaceUpdate();
			} else if (e.getSource() == start) {
				buttonPanel.remove(start);
				buttonPanel.remove(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				intervalTrackerButtonPanel.remove(clearIntervals);
				intervalTracker.repaint();
				intervalTracker.revalidate();
				intervalClockFaceUpdate();
				intervalTrackerColorIndicatorUpdate();
				windowTimerPanel.start();
				if (swingTimer.getDelay() != 100) {
					swingTimer.setDelay(100);
				}
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				buttonPanel.remove(pause);
				buttonPanel.add(resume);
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.remove(resume);
				buttonPanel.add(pause);
			} else if (e.getSource() == reset) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				buttonPanel.add(start);
				if (intervalTrackingUIArray.size() > 0) {
					intervalTrackerButtonPanel.add(clearIntervals);
				}
				intervalTracker.repaint();
				intervalTracker.revalidate();
				clockFaceInterval = 0;
				intervalClockFaceUpdate();
				intervalTrackerColorIndicatorUpdate();
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
						clockFaceInterval++;
						intervalClockFaceUpdate();
					} else if (flashing) {
						flashing = false;
						swingTimer.stop();
						swingTimer.setDelay(100);
					}
					borderAlertFlashing();
				}
			} else if (e.getSource() == edit) {
				prompt.dispose();
				callSetupWindow(intervalTrackingUIArray.get(Integer.parseInt(edit.getActionCommand())), intervalTracker, false);
				intervalTracker.repaint();
				intervalTracker.revalidate();
			} else if (e.getSource() == skipto) {
				prompt.dispose();
				clockFaceInterval = Integer.parseInt(skipto.getActionCommand());
				intervalClockFaceUpdate();
				intervalTrackerColorIndicatorUpdate();
			} else if (e.getSource() == delete) {
				prompt.dispose();
				int intervalIndex = Integer.parseInt(delete.getActionCommand());
				deleteIntervalArrayIndex(intervalIndex);
				if (intervalTrackingUIArray.size() == 0) {
					intervalClockFaceUpdate();
					buttonPanel.removeAll();
					buttonPanel.add(back);
					buttonPanel.add(setup);
				}
			} else {
				for (int i = 0; i < intervalTrackingUIArray.size(); i++) {
					if (e.getSource() == intervalTrackingUIArray.get(i)) {
						String command = intervalTrackingUIArray.get(i).getActionCommand();
						if (clockFaceInterval != Integer.parseInt(command) || !windowTimerPanel.isRunning()) {
							callIntervalPromptWindow(command);
						}
					}
				}
			}
		}
		window.repaint();
		window.revalidate();
		
		if (intervalTracker != null && intervalTracker.isVisible()) {
			intervalTracker.repaint();
			intervalTracker.revalidate();
		}
	}
}
