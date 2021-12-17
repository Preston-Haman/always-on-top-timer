package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AlwaysOnTopTimer implements ActionListener {
	
	private enum TimerType {
		NONE,
		STOPWATCH,
		COUNTDOWN,
		INTERVAL;
	}
	
	private TimerType currentType = TimerType.NONE;
	
	private int inputCount = 0;
	private int intervalCount = 0;
	private int intervalInputCount = 0;
	private int inputCountBackup = 0;
	private int[] countdownInput = new int[6];
	private int[] countdownInputBackup = new int[6];
	private int[] intervalInput = new int[6];
	
	//[Interval][0 contains input in next bracket, 1 contains input count in next bracket][input or input count]
	private int[][][] savedIntervals = new int[1][2][6];
	private int savedIntervalCount = 0;
	private int currentInterval = -1;
	private int clockFaceInterval = 0;
	protected JFrame window = new JFrame("Yon Timer");
	private JFrame intervalTracker;
	private JDialog setupWindow;
	private JDialog editWindow;
	private JDialog prompt;
	private Timer swingTimer;
	private JPanel startingWindowPanel = new JPanel();
	private ClockFace windowTimerPanel = new ClockFace(this);
	private JPanel buttonPanel = new JPanel();
	private JPanel intervalTrackerPanel;
	private JPanel intervalTrackerButtonPanel;
	protected JPanel northBorder = new JPanel();
	private JPanel southBorder = new JPanel();
	protected JPanel eastBorder = new JPanel();
	protected JPanel westBorder = new JPanel();
	private JButton[] intervalTrackingUIArray;
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
	private JButton cancel;
	private JButton confirm;
	private JButton editCancel;
	private JButton editConfirm;
	private JButton delete;
	private JButton skipto;
	private JButton edit;
	private JButton[] number;
	private JButton[] editNumber = new JButton[10];
	
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
		window.repaint();
		window.revalidate();
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
		window.repaint();
		window.revalidate();
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
		
		for (int i = 0; i < countdownInput.length; i++) {
			countdownInput[i] = 0;
			countdownInputBackup[i] = 0;
		}
		
		inputCount = 0;
		inputCountBackup = 0;
		currentType = TimerType.NONE;
		windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
		window.setTitle("Yon Timer");
	}
	
	private void callSetupWindow() {
		setupWindow = new JDialog(window, "Set Countdown:");
		setupWindow.setLayout(new BorderLayout());
		JPanel setupWindowKeypad = new JPanel();
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		confirm = new JButton("Confirm");
		confirm.addActionListener(this);
		setupWindowKeypad.setLayout(new GridLayout(0, 3, 3, 3));
		number = new JButton[10];
		Icon[] graphics = ClockFace.getClockGraphics();
		for (int i = 1; i < number.length; i++) {
			number[i] = new JButton();
			number[i].setIcon(graphics[i]);
			number[i].addActionListener(this);
			number[i].setFocusable(false);
			setupWindowKeypad.add(number[i]);
			if (i == number.length - 1) {
				number[0] = new JButton();
				number[0].setIcon(graphics[0]);
				number[0].addActionListener(this);
				number[0].setFocusable(false);
				setupWindowKeypad.add(cancel);
				setupWindowKeypad.add(number[0]);
				setupWindowKeypad.add(confirm);
			}
		}
		setupWindow.add(setupWindowKeypad, BorderLayout.CENTER);
		setupWindow.add(new JPanel(), BorderLayout.NORTH);
		setupWindow.add(new JPanel(), BorderLayout.SOUTH);
		setupWindow.add(new JPanel(), BorderLayout.EAST);
		setupWindow.add(new JPanel(), BorderLayout.WEST);
		setupWindow.setSize(275 + 14, 198 + 7);
		setupWindow.setMaximumSize(setupWindow.getSize());
		if (currentType == TimerType.COUNTDOWN) {
			Point windowPoint = window.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			setupWindow.setLocation((int) (x + window.getWidth() - 14), y - (setupWindow.getHeight() - window.getHeight()));
		} else if (currentType == TimerType.INTERVAL) {
			Point windowPoint = intervalTracker.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			setupWindow.setLocation((int) (x + intervalTracker.getWidth() - 14), y - (setupWindow.getHeight() - intervalTracker.getHeight()));
		}
		setupWindow.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setupWindow.setAlwaysOnTop(true);
		setupWindow.setModal(true);
		setupWindow.setVisible(true);
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
	
	private void callEditSetupWindow(String command) {
		editWindow = new JDialog(window, "Set Countdown:");
		editWindow.setLayout(new BorderLayout());
		JPanel setupWindowKeypad = new JPanel();
		editCancel = new JButton("Cancel");
		editCancel.setActionCommand(command);
		editCancel.addActionListener(this);
		editConfirm = new JButton("Confirm");
		editConfirm.setActionCommand(command);
		editConfirm.addActionListener(this);
		setupWindowKeypad.setLayout(new GridLayout(0, 3, 3, 3));
		editNumber = new JButton[10];
		Icon[] graphics = ClockFace.getClockGraphics();
		for (int i = 1; i < editNumber.length; i++) {
			editNumber[i] = new JButton();
			editNumber[i].setIcon(graphics[i]);
			editNumber[i].addActionListener(this);
			editNumber[i].setFocusable(false);
			editNumber[i].setActionCommand(command);
			setupWindowKeypad.add(editNumber[i]);
			if (i == editNumber.length - 1) {
				editNumber[0] = new JButton();
				editNumber[0].setIcon(graphics[0]);
				editNumber[0].addActionListener(this);
				editNumber[0].setFocusable(false);
				editNumber[0].setActionCommand(command);
				setupWindowKeypad.add(editCancel);
				setupWindowKeypad.add(editNumber[0]);
				setupWindowKeypad.add(editConfirm);
			}
		}
		editWindow.add(setupWindowKeypad, BorderLayout.CENTER);
		editWindow.add(new JPanel(), BorderLayout.NORTH);
		editWindow.add(new JPanel(), BorderLayout.SOUTH);
		editWindow.add(new JPanel(), BorderLayout.EAST);
		editWindow.add(new JPanel(), BorderLayout.WEST);
		editWindow.setSize(275 + 14, 198 + 7);
		editWindow.setMaximumSize(editWindow.getSize());
		Point windowPoint = intervalTracker.getLocation();
		int x = (int) windowPoint.getX();
		int y = (int) windowPoint.getY();
		editWindow.setLocation((int) (x + intervalTracker.getWidth() - 14), y - (editWindow.getHeight() - intervalTracker.getHeight()));
		editWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		editWindow.setAlwaysOnTop(true);
		editWindow.setModal(true);
		editWindow.setVisible(true);
	}
	
	private void countdownSetupWindowInput(int input) {
		if (inputCount < 6) {
			countdownInput[(countdownInput.length - 1) - inputCount] = input;
			inputCount++;
			applyCountdownSetupWindowInput();
		} else if (inputCount == 6) {
			return; // not needed to explicitly return, but if this user is too dumb to realize the numbers are filled out, then I wanna slap him <_<
		}
	}
	
	private void applyCountdownSetupWindowInput() {
		int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
		if (inputCount == 0) {
			seconds = 0;
			decaSeconds = 0;
			minutes = 0;
			decaMinutes = 0;
			hours = 0;
			decaHours = 0;
		} else if (inputCount == 1) {
			seconds = countdownInput[5];
			decaSeconds = countdownInput[0];
			minutes = countdownInput[1];
			decaMinutes = countdownInput[2];
			hours = countdownInput[3];
			decaHours = countdownInput[4];
		} else if (inputCount == 2) {
			seconds = countdownInput[4];
			decaSeconds = countdownInput[5];
			minutes = countdownInput[0];
			decaMinutes = countdownInput[1];
			hours = countdownInput[2];
			decaHours = countdownInput[3];
		} else if (inputCount == 3) {
			seconds = countdownInput[3];
			decaSeconds = countdownInput[4];
			minutes = countdownInput[5];
			decaMinutes = countdownInput[0];
			hours = countdownInput[1];
			decaHours = countdownInput[2];
		} else if (inputCount == 4) {
			seconds = countdownInput[2];
			decaSeconds = countdownInput[3];
			minutes = countdownInput[4];
			decaMinutes = countdownInput[5];
			hours = countdownInput[0];
			decaHours = countdownInput[1];
		} else if (inputCount == 5) {
			seconds = countdownInput[1];
			decaSeconds = countdownInput[2];
			minutes = countdownInput[3];
			decaMinutes = countdownInput[4];
			hours = countdownInput[5];
			decaHours = countdownInput[0];
		} else if (inputCount == 6) {
			seconds = countdownInput[0];
			decaSeconds = countdownInput[1];
			minutes = countdownInput[2];
			decaMinutes = countdownInput[3];
			hours = countdownInput[4];
			decaHours = countdownInput[5];
		} else /*if (inputCount >= 7)*/ {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		windowTimerPanel.setTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours);
		window.repaint();
		window.revalidate();
	}
	
	private void intervalSetupWindowInput(int input) {
		if (intervalInputCount < 6) {
			intervalInput[(intervalInput.length - 1) - intervalInputCount] = input;
			intervalInputCount++;
		} else if (intervalInputCount == 6) {
			return;
		}
	}
	
	private void addTimeLabelsForInterval(int interval, int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
		intervalTrackingUIArray[interval].removeAll();
		for (JLabel timeLabel: ClockFace.getLabelsForTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours)) {
			intervalTrackingUIArray[interval].add(timeLabel);
		}
	}
	
	private void applyIntervalSetupWindowInput(int interval) {
		int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
		if (intervalInputCount == 1) {
			decaHours = intervalInput[4];
			hours = intervalInput[3];
			decaMinutes = intervalInput[2];
			minutes = intervalInput[1];
			decaSeconds = intervalInput[0];
			seconds = intervalInput[5];
		} else if (intervalInputCount == 2) {
			decaHours = intervalInput[3];
			hours = intervalInput[2];
			decaMinutes = intervalInput[1];
			minutes = intervalInput[0];
			decaSeconds = intervalInput[5];
			seconds = intervalInput[4];
		} else if (intervalInputCount == 3) {
			decaHours = intervalInput[2];
			hours = intervalInput[1];
			decaMinutes = intervalInput[0];
			minutes = intervalInput[5];
			decaSeconds = intervalInput[4];
			seconds = intervalInput[3];
		} else if (intervalInputCount == 4) {
			decaHours = intervalInput[1];
			hours = intervalInput[0];
			decaMinutes = intervalInput[5];
			minutes = intervalInput[4];
			decaSeconds = intervalInput[3];
			seconds = intervalInput[2];
		} else if (intervalInputCount == 5) {
			decaHours = intervalInput[0];
			hours = intervalInput[5];
			decaMinutes = intervalInput[4];
			minutes = intervalInput[3];
			decaSeconds = intervalInput[2];
			seconds = intervalInput[1];
		} else if (intervalInputCount == 6) {
			decaHours = intervalInput[5];
			hours = intervalInput[4];
			decaMinutes = intervalInput[3];
			minutes = intervalInput[2];
			decaSeconds = intervalInput[1];
			seconds = intervalInput[0];
		} else /*if (intervalInputCount >= 7)*/ {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		addTimeLabelsForInterval(interval, seconds, decaSeconds, minutes, decaMinutes, hours, decaHours);
		intervalTracker.repaint();
		intervalTracker.revalidate();
	}
	
	private void saveIntervalSetupWindowInput(int interval, boolean edit) {
		if (savedIntervals.length == savedIntervalCount) {
			int[][][] temp = new int[savedIntervals.length + 1][savedIntervals[0].length][savedIntervals[0][0].length];
			for (int i = 0; i < savedIntervals.length; i++) {
				for (int j = 0; j < savedIntervals[0].length; j++) {
					for (int k = 0; k < savedIntervals[0][0].length; k++) {
						temp[i][j][k] = new Integer(savedIntervals[i][j][k]);
					}
				}
			}
			savedIntervals = temp;
		}
		if (interval == -1) {
			for (int i = 0; i < intervalInput.length; i++) {
				savedIntervals[currentInterval][0][i] = intervalInput[i];
			}
			savedIntervals[currentInterval][1][0] = intervalInputCount;
		} else {
			for (int i = 0; i < intervalInput.length; i++) {
				savedIntervals[interval][0][i] = intervalInput[i];
			}
			savedIntervals[interval][1][0] = intervalInputCount;
		}
		if (!edit) {
			savedIntervalCount++;
		}
		intervalInput = null;
		intervalInput = new int[6];
		intervalInputCount = 0;
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
	
	private void buildIntervalTrackingUIArray() {
		intervalTrackerPanel.removeAll();
		intervalTrackingUIArray = new JButton[intervalCount];
		for (int i = 0; i < intervalCount; i++) {
			intervalTrackingUIArray[i] = new JButton();
			intervalTrackingUIArray[i].addActionListener(this);
			intervalTrackingUIArray[i].setFocusable(false);
			intervalTrackingUIArray[i].setActionCommand("" + i);
			intervalTrackingUIArray[i].setLayout(new GridLayout(1, 0, 3, 0));
			addTimeLabelsForInterval(i, 0, 0, 0, 0, 0, 0);
		}
		updateIntervalTrackingUI();
		for (int i = 0; i < intervalTrackingUIArray.length; i++) {
			intervalTrackerPanel.add(intervalTrackingUIArray[i]);
		}
		intervalTracker.repaint();
		intervalTracker.revalidate();
	}
	
	private void deleteIntervalArrayIndex(int intervalIndex) {
		int newLength = savedIntervals.length - 1;
		if (newLength == 0) {
			savedIntervals = null;
			savedIntervals = new int[1][2][6];
		} else {
			int[][][] temp = new int[newLength][2][6];
			int h = 0;
			for (int i = 0; i < temp.length; i++) {
				if (i == intervalIndex) {
					h++;
				}
				for (int j = 0; j < 6; j++) {
					temp[i][0][j] = savedIntervals[h][0][j];
				}
				temp[i][1][0] = savedIntervals[h][1][0];
				h++;
			}
			savedIntervals = temp;
		}
		intervalTracker.setSize(intervalTracker.getWidth(), intervalTracker.getHeight() - 55);
		savedIntervalCount--;
		intervalCount--;
		currentInterval--;
		if (intervalCount == 0) {
			intervalTrackerButtonPanel.remove(clearIntervals);
			buttonPanel.remove(start);
			window.repaint();
			window.revalidate();
		}
		Point intervalTrackerPoint = intervalTracker.getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		intervalTracker.setLocation(x, y + 55);
		buildIntervalTrackingUIArray();
		intervalTrackerColorIndicatorUpdate();
	}
	
	private void updateIntervalTrackingUI() {
		int interInputCount = 0;
		int[] input = new int[6];
		for (int interval = 0; interval < savedIntervalCount; interval++) {
			interInputCount = savedIntervals[interval][1][0];
			for (int i = 0; i < input.length; i++) {
				input[i] = savedIntervals[interval][0][i];
			}
			
			int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
			if (interInputCount == 0) {
				decaHours = 0;
				hours = 0;
				decaMinutes = 0;
				minutes = 0;
				decaSeconds = 0;
				seconds = 0;
			} else if (interInputCount == 1) {
				decaHours = input[4];
				hours = input[3];
				decaMinutes = input[2];
				minutes = input[1];
				decaSeconds = input[0];
				seconds = input[5];
			} else if (interInputCount == 2) {
				decaHours = input[3];
				hours = input[2];
				decaMinutes = input[1];
				minutes = input[0];
				decaSeconds = input[5];
				seconds = input[4];
			} else if (interInputCount == 3) {
				decaHours = input[2];
				hours = input[1];
				decaMinutes = input[0];
				minutes = input[5];
				decaSeconds = input[4];
				seconds = input[3];
			} else if (interInputCount == 4) {
				decaHours = input[1];
				hours = input[0];
				decaMinutes = input[5];
				minutes = input[4];
				decaSeconds = input[3];
				seconds = input[2];
			} else if (interInputCount == 5) {
				decaHours = input[0];
				hours = input[5];
				decaMinutes = input[4];
				minutes = input[3];
				decaSeconds = input[2];
				seconds = input[1];
			} else if (interInputCount == 6) {
				decaHours = input[5];
				hours = input[4];
				decaMinutes = input[3];
				minutes = input[2];
				decaSeconds = input[1];
				seconds = input[0];
			} else /*if (interInputCount >= 7)*/ {
				throw new IllegalArgumentException("You cannot input past the end of the clock face.");
			}
			addTimeLabelsForInterval(interval, seconds, decaSeconds, minutes, decaMinutes, hours, decaHours);
		}
		intervalTracker.repaint();
		intervalTracker.revalidate();
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
		window.repaint();
		window.revalidate();
	}
	
	private void intervalClockFaceUpdate() {
		if (clockFaceInterval >= savedIntervalCount) {
			clockFaceInterval = 0;
		}
		int interInputCount = savedIntervals[clockFaceInterval][1][0];
		int[] input = new int[6];
		for (int i = 0; i < input.length; i++) {
			input[i] = savedIntervals[clockFaceInterval][0][i];
		}
		int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
		if (interInputCount == 0) {
			seconds = 0;
			decaSeconds = 0;
			minutes = 0;
			decaMinutes = 0;
			hours = 0;
			decaHours = 0;
		} else if (interInputCount == 1) {
			seconds = input[5];
			decaSeconds = input[0];
			minutes = input[1];
			decaMinutes = input[2];
			hours = input[3];
			decaHours = input[4];
		} else if (interInputCount == 2) {
			seconds = input[4];
			decaSeconds = input[5];
			minutes = input[0];
			decaMinutes = input[1];
			hours = input[2];
			decaHours = input[3];
		} else if (interInputCount == 3) {
			seconds = input[3];
			decaSeconds = input[4];
			minutes = input[5];
			decaMinutes = input[0];
			hours = input[1];
			decaHours = input[2];
		} else if (interInputCount == 4) {
			seconds = input[2];
			decaSeconds = input[3];
			minutes = input[4];
			decaMinutes = input[5];
			hours = input[0];
			decaHours = input[1];
		} else if (interInputCount == 5) {
			seconds = input[1];
			decaSeconds = input[2];
			minutes = input[3];
			decaMinutes = input[4];
			hours = input[5];
			decaHours = input[0];
		} else if (interInputCount == 6) {
			seconds = input[0];
			decaSeconds = input[1];
			minutes = input[2];
			decaMinutes = input[3];
			hours = input[4];
			decaHours = input[5];
		} else /*if (interInputCount >= 7)*/ {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		windowTimerPanel.setTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours);
		intervalTrackerColorIndicatorUpdate();
	}
	
	private void intervalTrackerColorIndicatorUpdate() {
		for (int i = 0; i < intervalTrackingUIArray.length; i++) {
			intervalTrackingUIArray[i].setBackground(new JButton().getBackground());
		}
		if (clockFaceInterval >= 0 && clockFaceInterval < intervalTrackingUIArray.length) {
			intervalTrackingUIArray[clockFaceInterval].setBackground(activeIntervalColor);
			intervalTrackingUIArray[clockFaceInterval].setBorderPainted(true);
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
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(resume);
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				window.repaint();
				window.revalidate();
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
				if (inputCount != 0) {
					for (int i = 0; i < countdownInput.length; i++) {
						countdownInputBackup[i] = new Integer(countdownInput[i]);
						countdownInput[i] = 0;
					}
					inputCountBackup = new Integer(inputCount);
					inputCount = 0;
				}
				applyCountdownSetupWindowInput();
				callSetupWindow();
			} else if (e.getSource() == cancel) {
				for (int i = 0; i < countdownInput.length; i++) {
					countdownInput[i] = new Integer(countdownInputBackup[i]);
					countdownInputBackup[i] = 0;
				}
				inputCount = new Integer(inputCountBackup);
				inputCountBackup = 0;
				applyCountdownSetupWindowInput();
				setupWindow.dispose();
			} else if (e.getSource() == confirm) {
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				buttonPanel.add(start);
				setupWindow.dispose();
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == number[0]) {
				countdownSetupWindowInput(0);
			} else if (e.getSource() == number[1]) {
				countdownSetupWindowInput(1);
			} else if (e.getSource() == number[2]) {
				countdownSetupWindowInput(2);
			} else if (e.getSource() == number[3]) {
				countdownSetupWindowInput(3);
			} else if (e.getSource() == number[4]) {
				countdownSetupWindowInput(4);
			} else if (e.getSource() == number[5]) {
				countdownSetupWindowInput(5);
			} else if (e.getSource() == number[6]) {
				countdownSetupWindowInput(6);
			} else if (e.getSource() == number[7]) {
				countdownSetupWindowInput(7);
			} else if (e.getSource() == number[8]) {
				countdownSetupWindowInput(8);
			} else if (e.getSource() == number[9]) {
				countdownSetupWindowInput(9);
			} else if (e.getSource() == start) {
				windowTimerPanel.start();
				if (swingTimer.getDelay() != 100) {
					swingTimer.setDelay(100);
				}
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				window.repaint();
				window.revalidate();
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
				applyCountdownSetupWindowInput();
			} else if (e.getSource() == pause) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(resume);
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(reset);
				buttonPanel.add(pause);
				window.repaint();
				window.revalidate();
			}
		} else if (currentType == TimerType.INTERVAL) {
			if (e.getSource() == setup) {
				callIntervalTrackerWindow();
			} else if (e.getSource() == addInterval) {
				intervalCount++;
				currentInterval++;
				if (!windowTimerPanel.isRunning()) {
					buttonPanel.add(start);
					window.repaint();
					window.revalidate();
				}
				buildIntervalTrackingUIArray();
				if (!windowTimerPanel.isRunning()) {
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
				callSetupWindow();
			} else if (e.getSource() == clearIntervals) {
				intervalTrackerPanel.removeAll();
				intervalTrackerButtonPanel.removeAll();
				intervalTrackerButtonPanel.add(addInterval);
				intervalTracker.repaint();
				intervalTracker.revalidate();
				buttonPanel.remove(start);
				window.repaint();
				window.revalidate();
				intervalTracker.setSize(256, 136 + 7 - 55);
				Point intervalTrackerPoint = intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				intervalTracker.setLocation(x, y + (55 * intervalCount));
				savedIntervals = null;
				savedIntervals = new int[1][2][6];
				savedIntervalCount = 0;
				intervalCount = 0;
				currentInterval = -1;
				intervalClockFaceUpdate();
				buildIntervalTrackingUIArray();
			} else if (e.getSource() == number[0]) {
				intervalSetupWindowInput(0);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[1]) {
				intervalSetupWindowInput(1);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[2]) {
				intervalSetupWindowInput(2);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[3]) {
				intervalSetupWindowInput(3);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[4]) {
				intervalSetupWindowInput(4);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[5]) {
				intervalSetupWindowInput(5);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[6]) {
				intervalSetupWindowInput(6);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[7]) {
				intervalSetupWindowInput(7);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[8]) {
				intervalSetupWindowInput(8);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == number[9]) {
				intervalSetupWindowInput(9);
				applyIntervalSetupWindowInput(currentInterval);
			} else if (e.getSource() == cancel) {
				intervalTracker.setSize(intervalTracker.getWidth(), intervalTracker.getHeight() - 55);
				intervalCount--;
				currentInterval--;
				if (intervalCount == 0) {
					intervalTrackerButtonPanel.remove(clearIntervals);
					buttonPanel.remove(start);
					window.repaint();
					window.revalidate();
				}
				intervalInput = null;
				intervalInput = new int[6];
				intervalInputCount = 0;
				buildIntervalTrackingUIArray();
				Point intervalTrackerPoint = intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				intervalTracker.setLocation(x, y + 55);
				intervalTracker.repaint();
				intervalTracker.revalidate();
				intervalTrackerColorIndicatorUpdate(); // also calls repaint/revalidate
				setupWindow.dispose();
			} else if (e.getSource() == confirm) {
				saveIntervalSetupWindowInput(-1, false);
				updateIntervalTrackingUI();
				if (!windowTimerPanel.isRunning()) {
					intervalClockFaceUpdate();
				}
				intervalTrackerColorIndicatorUpdate();
				setupWindow.dispose();
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
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == resume) {
				windowTimerPanel.start();
				buttonPanel.remove(resume);
				buttonPanel.add(pause);
				window.repaint();
				window.revalidate();
			} else if (e.getSource() == reset) {
				windowTimerPanel.stop();
				buttonPanel.removeAll();
				buttonPanel.add(back);
				buttonPanel.add(setup);
				buttonPanel.add(start);
				if (savedIntervalCount > 0) {
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
				callEditSetupWindow(edit.getActionCommand());
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
				if (savedIntervalCount == 0) {
					intervalClockFaceUpdate();
					buttonPanel.removeAll();
					buttonPanel.add(back);
					buttonPanel.add(setup);
					window.repaint();
					window.revalidate();
				}
			} else if (e.getSource() == editNumber[0]) {
				intervalSetupWindowInput(0);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[0].getActionCommand()));
			} else if (e.getSource() == editNumber[1]) {
				intervalSetupWindowInput(1);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[1].getActionCommand()));
			} else if (e.getSource() == editNumber[2]) {
				intervalSetupWindowInput(2);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[2].getActionCommand()));
			} else if (e.getSource() == editNumber[3]) {
				intervalSetupWindowInput(3);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[3].getActionCommand()));
			} else if (e.getSource() == editNumber[4]) {
				intervalSetupWindowInput(4);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[4].getActionCommand()));
			} else if (e.getSource() == editNumber[5]) {
				intervalSetupWindowInput(5);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[5].getActionCommand()));
			} else if (e.getSource() == editNumber[6]) {
				intervalSetupWindowInput(6);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[6].getActionCommand()));
			} else if (e.getSource() == editNumber[7]) {
				intervalSetupWindowInput(7);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[7].getActionCommand()));
			} else if (e.getSource() == editNumber[8]) {
				intervalSetupWindowInput(8);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[8].getActionCommand()));
			} else if (e.getSource() == editNumber[9]) {
				intervalSetupWindowInput(9);
				applyIntervalSetupWindowInput(Integer.parseInt(editNumber[9].getActionCommand()));
			} else if (e.getSource() == editCancel) {
				intervalInput = null;
				intervalInput = new int[6];
				intervalInputCount = 0;
				updateIntervalTrackingUI();
				editWindow.dispose();
			} else if (e.getSource() == editConfirm) {
				saveIntervalSetupWindowInput(Integer.parseInt(editConfirm.getActionCommand()), true);
				buildIntervalTrackingUIArray();
				intervalClockFaceUpdate();
				intervalTrackerColorIndicatorUpdate();
				editWindow.dispose();
			} else {
				for (int i = 0; i < intervalTrackingUIArray.length; i++) {
					if (e.getSource() == intervalTrackingUIArray[i]) {
						String command = intervalTrackingUIArray[i].getActionCommand();
						if (clockFaceInterval != Integer.parseInt(command) || !windowTimerPanel.isRunning()) {
							callIntervalPromptWindow(command);
						}
					}
				}
			}
		}
	}
}
