package Yonning.AlwaysOnTopTimer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import darrylbu.icon.StretchIcon;

public class AlwaysOnTopTimer implements ActionListener {

	public static final int STOPWATCH_TYPE = 1;
	public static final int COUNTDOWN_TYPE = 2;
	public static final int INTERVAL_TYPE = 3;
	private int currentType = 0;
	protected int tenthSeconds = 0;
	protected AlwaysOnTopTimerTenthsIndicator tenthsIndicator;
	protected AlwaysOnTopTimerReverseTenthsIndicator tenthsReverseIndicator;
	private int seconds = 0;
	private int decaSeconds = 0;
	private int minutes = 0;
	private int decaMinutes = 0;
	private int hours = 0;
	private int decaHours = 0;
	private int inputCount = 0;
	private int intervalCount = 0;
	private int intervalInputCount = 0;
	private int inputCountBackup = 0;
	private int[] countdownInput = new int[6];
	private int[] countdownInputBackup = new int[6];
	private int[] intervalInput = new int[6];
	private int[][][] savedIntervals = new int[1][2][6]; //[Interval][0 contains input in next bracket, 1 contains input count in next bracket][input or input count]
	private int savedIntervalCount = 0;
	private int currentInterval = -1;
	private int clockFaceInterval = 0;
	protected JFrame window = new JFrame("Yon Timer");
	private JFrame intervalTracker;
	private JDialog setupWindow;
	private JDialog editWindow;
	private JDialog prompt;
	private JLabel[][] gfx = new JLabel[10][8];
	private JLabel[][][] gfxCopy;
	private StretchIcon[] gfxIcon = new StretchIcon[10];
	private AlwaysOnTopTimerClock timer;
	private Timer swingTimer;
	private JPanel startingWindowPanel = new JPanel();
	private JPanel windowTimerPanel = new JPanel();
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
	private Font font = new Font("Segoe UI", Font.PLAIN, 14);

	private AlwaysOnTopTimer() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		UIManager.put("Button.font", this.font);
		this.setup.addActionListener(this);
		this.reset.addActionListener(this);
		this.pause.addActionListener(this);
		this.resume.addActionListener(this);
		this.start.addActionListener(this);
		this.stopwatch.addActionListener(this);
		this.countdown.addActionListener(this);
		this.interval.addActionListener(this);
		this.addInterval.addActionListener(this);
		this.clearIntervals.addActionListener(this);
		this.back.addActionListener(this);
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.window.setAlwaysOnTop(true);
		this.window.setLayout(new BorderLayout());
		this.startingWindowPanel.setLayout(new GridLayout(3, 1));
		this.startingWindowPanel.add(this.stopwatch);
		this.startingWindowPanel.add(this.countdown);
		this.startingWindowPanel.add(this.interval);
		this.window.add(startingWindowPanel, BorderLayout.CENTER);
		this.window.add(this.northBorder, BorderLayout.NORTH);
		this.window.add(this.southBorder, BorderLayout.SOUTH);
		this.window.add(this.eastBorder, BorderLayout.EAST);
		this.window.add(this.westBorder, BorderLayout.WEST);
		this.defaultPanelColor = this.northBorder.getBackground();
		this.window.setSize(242 + 14, 136 + 7); // Windows 10 takes the extra amounts and uses them as a shadow for the window
		this.window.setMaximumSize(this.window.getSize());
		this.window.setLocation(415 - 7, 823);
		this.windowTimerPanel.setLayout(new GridLayout(1, 0, 3, 0));
		for (int i = 0; i < this.gfx.length; i++) {
			this.gfxIcon[i] = new StretchIcon(AlwaysOnTopTimer.class.getResource("/Graphics/Clock_Number_" + i + ".png"), true);
			for (int j = 0; j < this.gfx[i].length; j++) {
				this.gfx[i][j] = new JLabel(this.gfxIcon[i]);
			}
		}
		for (int i = 0; i < this.gfx.length; i++) {
			this.gfx[i][2] = null;
			this.gfx[i][5] = null;
		}
		this.gfx[0][2] = new JLabel(new StretchIcon(AlwaysOnTopTimer.class.getResource("/Graphics/Clock_Colon.png")));
		this.gfx[0][5] = new JLabel(new StretchIcon(AlwaysOnTopTimer.class.getResource("/Graphics/Clock_Colon.png")));
		for (int i = 0; i < this.gfx[0].length; i++) {
			this.windowTimerPanel.add(this.gfx[0][i]);
		}
		this.timer =  new AlwaysOnTopTimerClock(100, this);
		this.swingTimer = new Timer(100, this);
		this.swingTimer.setInitialDelay(0);
		this.tenthsIndicator = new AlwaysOnTopTimerTenthsIndicator(this);
		this.tenthsReverseIndicator = new AlwaysOnTopTimerReverseTenthsIndicator(this);
	}

	private void returnToStartWindow() {
		if (this.swingTimer.isRunning()) {
			this.timer.stop();
			this.swingTimer.stop();
		}
		this.window.remove(this.windowTimerPanel);
		this.window.add(this.startingWindowPanel, BorderLayout.CENTER);
		this.window.remove(this.buttonPanel);
		this.window.add(this.southBorder, BorderLayout.SOUTH);
		this.resetEntireSystem();
	}

	private void addClockFaceAndButtons(int type) {
		if (type == AlwaysOnTopTimer.STOPWATCH_TYPE) {
			this.currentType = AlwaysOnTopTimer.STOPWATCH_TYPE;
			this.window.remove(this.startingWindowPanel);
			this.window.setTitle("Yon Timer -- Stopwatch");
			this.window.add(this.windowTimerPanel, BorderLayout.CENTER);
			this.window.remove(this.northBorder);
			this.window.add(this.tenthsIndicator, BorderLayout.NORTH);
			this.window.remove(this.southBorder);
			this.window.add(this.buttonPanel, BorderLayout.SOUTH);
			this.buttonPanel.removeAll();
			this.buttonPanel.add(this.back);
			this.buttonPanel.add(this.reset);
			this.buttonPanel.add(this.start);
			this.window.repaint();
			this.window.revalidate();
		} else if (type == AlwaysOnTopTimer.COUNTDOWN_TYPE || type == AlwaysOnTopTimer.INTERVAL_TYPE) {
			if (type == AlwaysOnTopTimer.COUNTDOWN_TYPE) {
				this.currentType = AlwaysOnTopTimer.COUNTDOWN_TYPE;
				this.window.setTitle("Yon Timer -- Countdown");
			} else if (type == AlwaysOnTopTimer.INTERVAL_TYPE) {
				this.currentType = AlwaysOnTopTimer.INTERVAL_TYPE;
				this.window.setTitle("Yon Timer -- Interval");
			}
			this.window.remove(this.startingWindowPanel);
			this.window.add(this.windowTimerPanel, BorderLayout.CENTER);
			this.window.remove(this.northBorder);
			this.window.add(this.tenthsReverseIndicator, BorderLayout.NORTH);
			this.window.remove(this.southBorder);
			this.window.add(this.buttonPanel, BorderLayout.SOUTH);
			this.buttonPanel.removeAll();
			this.buttonPanel.add(this.back);
			this.buttonPanel.add(this.setup);
			this.window.repaint();
			this.window.revalidate();
		} else {
			throw new IllegalArgumentException("The paramater 'type' must be one of the type constants defined in the AlwaysOnTopTimer Class.");
		}
	}

	private void resetEntireSystem() {
		this.timer.stop();
		if (this.swingTimer.isRunning()) {
			this.swingTimer.stop();
		}
		if (this.swingTimer.getDelay() != 100) {
			this.swingTimer.setDelay(100);
		}
		if (this.currentType == AlwaysOnTopTimer.STOPWATCH_TYPE) {
			this.window.remove(this.tenthsIndicator);
			this.window.add(this.northBorder, BorderLayout.NORTH);
		}
		if (this.currentType == AlwaysOnTopTimer.COUNTDOWN_TYPE || this.currentType == AlwaysOnTopTimer.INTERVAL_TYPE) {
			this.window.remove(this.tenthsReverseIndicator);
			this.window.add(this.northBorder, BorderLayout.NORTH);
		}
		if (this.flashing) {
			this.flashing = false;
		}
		if (this.westBorder.getBackground() == this.flashColor) {
			this.borderAlertFlashing();
		}
		for (int i = 0; i < this.countdownInput.length; i++) {
			this.countdownInput[i] = 0;
			this.countdownInputBackup[i] = 0;
		}
		this.inputCount = 0;
		this.inputCountBackup = 0;
		this.tenthSeconds = 0;
		this.seconds = 0;
		this.decaSeconds = 0;
		this.minutes = 0;
		this.decaMinutes = 0;
		this.hours = 0;
		this.decaHours = 0;
		this.currentType = 0;
		this.window.setTitle("Yon Timer");
		this.updateClockFace();
	}

	private void updateClockFace() {
		this.windowTimerPanel.removeAll();
		this.windowTimerPanel.add(this.gfx[this.decaHours][0]);
		this.windowTimerPanel.add(this.gfx[this.hours][1]);
		this.windowTimerPanel.add(this.gfx[0][2]);
		this.windowTimerPanel.add(this.gfx[this.decaMinutes][3]);
		this.windowTimerPanel.add(this.gfx[this.minutes][4]);
		this.windowTimerPanel.add(this.gfx[0][5]);
		this.windowTimerPanel.add(this.gfx[this.decaSeconds][6]);
		this.windowTimerPanel.add(this.gfx[this.seconds][7]);
		this.window.repaint();
		this.window.revalidate();
	}

	private void callSetupWindow() {
		this.setupWindow = new JDialog(this.window, "Set Countdown:");
		this.setupWindow.setLayout(new BorderLayout());
		JPanel setupWindowKeypad = new JPanel();
		this.cancel = new JButton("Cancel");
		this.cancel.addActionListener(this);
		this.confirm = new JButton("Confirm");
		this.confirm.addActionListener(this);
		setupWindowKeypad.setLayout(new GridLayout(0, 3, 3, 3));
		this.number = new JButton[10];
		for (int i = 1; i < this.number.length; i++) {
			this.number[i] = new JButton();
			this.number[i].setIcon(this.gfxIcon[i]);
			this.number[i].addActionListener(this);
			this.number[i].setFocusable(false);
			setupWindowKeypad.add(number[i]);
			if (i == this.number.length - 1) {
				this.number[0] = new JButton();
				this.number[0].setIcon(this.gfxIcon[0]);
				this.number[0].addActionListener(this);
				this.number[0].setFocusable(false);
				setupWindowKeypad.add(this.cancel);
				setupWindowKeypad.add(this.number[0]);
				setupWindowKeypad.add(this.confirm);
			}
		}
		this.setupWindow.add(setupWindowKeypad, BorderLayout.CENTER);
		this.setupWindow.add(new JPanel(), BorderLayout.NORTH);
		this.setupWindow.add(new JPanel(), BorderLayout.SOUTH);
		this.setupWindow.add(new JPanel(), BorderLayout.EAST);
		this.setupWindow.add(new JPanel(), BorderLayout.WEST);
		this.setupWindow.setSize(275+14, 198+7);
		this.setupWindow.setMaximumSize(setupWindow.getSize());
		if (this.currentType == AlwaysOnTopTimer.COUNTDOWN_TYPE ) {
			Point windowPoint = this.window.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			this.setupWindow.setLocation((int) (x + this.window.getWidth() - 14), y - (setupWindow.getHeight() - this.window.getHeight()));
		} else if (this.currentType == AlwaysOnTopTimer.INTERVAL_TYPE) {
			Point windowPoint = this.intervalTracker.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			this.setupWindow.setLocation((int) (x + this.intervalTracker.getWidth() - 14), y - (setupWindow.getHeight() - this.intervalTracker.getHeight()));
		}
		this.setupWindow.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		this.setupWindow.setAlwaysOnTop(true);
		this.setupWindow.setModal(true);
		this.setupWindow.setVisible(true);
	}
	
	private void callIntervalPromptWindow(String command) {
		this.edit = new JButton("Edit");
		this.edit.addActionListener(this);
		this.skipto = new JButton("Skip To");
		this.skipto.addActionListener(this);
		this.delete = new JButton("Delete");
		this.delete.addActionListener(this);
		this.prompt = new JDialog(this.intervalTracker, "Interval " + (Integer.parseInt(command) + 1));
		this.prompt.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.prompt.setLayout(new BorderLayout());
		this.prompt.add(new JPanel(), BorderLayout.NORTH);
		this.prompt.add(new JPanel(), BorderLayout.SOUTH);
		this.prompt.add(new JPanel(), BorderLayout.EAST);
		this.prompt.add(new JPanel(), BorderLayout.WEST);
		JPanel buttons = new JPanel();
		this.prompt.add(buttons, BorderLayout.CENTER);
		buttons.add(this.edit);
		buttons.add(this.skipto);
		buttons.add(this.delete);
		this.edit.setActionCommand(command);
		this.skipto.setActionCommand(command);
		this.delete.setActionCommand(command);
		if (this.timer.isRunning()) {
			this.edit.setEnabled(false);
		} else {
			this.edit.setEnabled(true);
		}
		this.prompt.setSize(241+14, 89+7);
		this.prompt.setLocationRelativeTo(this.intervalTracker);
		this.prompt.setAlwaysOnTop(true);
		this.prompt.setModal(true);
		this.prompt.setVisible(true);
	}
	
	private void callEditSetupWindow(String command) {
		this.editWindow = new JDialog(this.window, "Set Countdown:");
		this.editWindow.setLayout(new BorderLayout());
		JPanel setupWindowKeypad = new JPanel();
		this.editCancel = new JButton("Cancel");
		this.editCancel.setActionCommand(command);
		this.editCancel.addActionListener(this);
		this.editConfirm = new JButton("Confirm");
		this.editConfirm.setActionCommand(command);
		this.editConfirm.addActionListener(this);
		setupWindowKeypad.setLayout(new GridLayout(0, 3, 3, 3));
		this.editNumber = new JButton[10];
		for (int i = 1; i < this.editNumber.length; i++) {
			this.editNumber[i] = new JButton();
			this.editNumber[i].setIcon(this.gfxIcon[i]);
			this.editNumber[i].addActionListener(this);
			this.editNumber[i].setFocusable(false);
			this.editNumber[i].setActionCommand(command);
			setupWindowKeypad.add(this.editNumber[i]);
			if (i == this.editNumber.length - 1) {
				this.editNumber[0] = new JButton();
				this.editNumber[0].setIcon(this.gfxIcon[0]);
				this.editNumber[0].addActionListener(this);
				this.editNumber[0].setFocusable(false);
				this.editNumber[0].setActionCommand(command);
				setupWindowKeypad.add(this.editCancel);
				setupWindowKeypad.add(this.editNumber[0]);
				setupWindowKeypad.add(this.editConfirm);
			}
		}
		this.editWindow.add(setupWindowKeypad, BorderLayout.CENTER);
		this.editWindow.add(new JPanel(), BorderLayout.NORTH);
		this.editWindow.add(new JPanel(), BorderLayout.SOUTH);
		this.editWindow.add(new JPanel(), BorderLayout.EAST);
		this.editWindow.add(new JPanel(), BorderLayout.WEST);
		this.editWindow.setSize(275+14, 198+7);
		this.editWindow.setMaximumSize(editWindow.getSize());
		Point windowPoint = this.intervalTracker.getLocation();
		int x = (int) windowPoint.getX();
		int y = (int) windowPoint.getY();
		this.editWindow.setLocation((int) (x + this.intervalTracker.getWidth() - 14), y - (editWindow.getHeight() - this.intervalTracker.getHeight()));
		this.editWindow.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.editWindow.setAlwaysOnTop(true);
		this.editWindow.setModal(true);
		this.editWindow.setVisible(true);
	}
	
	private void countdownSetupWindowInput(int input) {
		if (this.inputCount < 6) {
			this.countdownInput[(this.countdownInput.length-1) - this.inputCount] = input;
			this.inputCount++;
			this.applyCountdownSetupWindowInput();
		} else if (this.inputCount == 6) {
			return; //not needed to explicitly return, but if this user is too dumb to realize the numbers are filled out, then I wanna slap him <_<
		}
	}
	
	private void applyCountdownSetupWindowInput() {
		if (this.inputCount == 0) {
			this.seconds = 0;
			this.decaSeconds = 0;
			this.minutes = 0;
			this.decaMinutes = 0;
			this.hours = 0;
			this.decaHours = 0;
		} else if (this.inputCount == 1) {
			this.seconds = this.countdownInput[5];
			this.decaSeconds = this.countdownInput[0];
			this.minutes = this.countdownInput[1];
			this.decaMinutes = this.countdownInput[2];
			this.hours = this.countdownInput[3];
			this.decaHours = this.countdownInput[4];
		} else if (this.inputCount == 2) {
			this.seconds = this.countdownInput[4];
			this.decaSeconds = this.countdownInput[5];
			this.minutes = this.countdownInput[0];
			this.decaMinutes = this.countdownInput[1];
			this.hours = this.countdownInput[2];
			this.decaHours = this.countdownInput[3];
		} else if (this.inputCount == 3) {
			this.seconds = this.countdownInput[3];
			this.decaSeconds = this.countdownInput[4];
			this.minutes = this.countdownInput[5];
			this.decaMinutes = this.countdownInput[0];
			this.hours = this.countdownInput[1];
			this.decaHours = this.countdownInput[2];
		} else if (this.inputCount == 4) {
			this.seconds = this.countdownInput[2];
			this.decaSeconds = this.countdownInput[3];
			this.minutes = this.countdownInput[4];
			this.decaMinutes = this.countdownInput[5];
			this.hours = this.countdownInput[0];
			this.decaHours = this.countdownInput[1];
		} else if (this.inputCount == 5) {
			this.seconds = this.countdownInput[1];
			this.decaSeconds = this.countdownInput[2];
			this.minutes = this.countdownInput[3];
			this.decaMinutes = this.countdownInput[4];
			this.hours = this.countdownInput[5];
			this.decaHours = this.countdownInput[0];
		} else if (this.inputCount == 6) {
			this.seconds = this.countdownInput[0];
			this.decaSeconds = this.countdownInput[1];
			this.minutes = this.countdownInput[2];
			this.decaMinutes = this.countdownInput[3];
			this.hours = this.countdownInput[4];
			this.decaHours = this.countdownInput[5];
		} else if (this.inputCount >= 7) {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		this.updateClockFace();
	}
	
	private void intervalSetupWindowInput(int input) {
		if (this.intervalInputCount < 6) {
			this.intervalInput[(this.intervalInput.length-1) - this.intervalInputCount] = input;
			this.intervalInputCount++;
		} else if (this.intervalInputCount == 6) {
			return;
		}
	}
	
	private void applyIntervalSetupWindowInput(int interval) {
		if (this.intervalInputCount == 1) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][7]);
		} else if (this.intervalInputCount == 2) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][7]);
		} else if (this.intervalInputCount == 3) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][7]);
		} else if (this.intervalInputCount == 4) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][7]);
		} else if (this.intervalInputCount == 5) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][7]);
		} else if (this.intervalInputCount == 6) {
			this.intervalTrackingUIArray[interval].removeAll();
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[5]][0]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[4]][1]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[3]][3]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[2]][4]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[1]][6]);
			this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][this.intervalInput[0]][7]);
		} else if (this.intervalInputCount >= 7) {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		this.intervalTracker.repaint();
		this.intervalTracker.revalidate();
	}
	
	private void saveIntervalSetupWindowInput(int interval, boolean edit) {
		if (this.savedIntervals.length == this.savedIntervalCount) {
			int[][][] temp = new int[this.savedIntervals.length+1][this.savedIntervals[0].length][this.savedIntervals[0][0].length];
			for (int i = 0; i < this.savedIntervals.length; i++) {
				for (int j = 0; j < this.savedIntervals[0].length; j++) {
					for (int k = 0; k < this.savedIntervals[0][0].length; k++) {
						temp[i][j][k] = new Integer(this.savedIntervals[i][j][k]);
					}
				}
			}
			this.savedIntervals = temp;
		}
		if (interval == -1) {
			for (int i = 0; i < this.intervalInput.length; i++) {
				this.savedIntervals[this.currentInterval][0][i] = this.intervalInput[i];
			}
			this.savedIntervals[this.currentInterval][1][0] = this.intervalInputCount;
		} else {
			for (int i = 0; i < this.intervalInput.length; i++) {
				this.savedIntervals[interval][0][i] = this.intervalInput[i];
			}
			this.savedIntervals[interval][1][0] = this.intervalInputCount;
		}
		if (!edit) {
			this.savedIntervalCount++;
		}
		this.intervalInput = null;
		this.intervalInput = new int[6];
		this.intervalInputCount = 0;
	}
	
	private void callIntervalTrackerWindow() {
		if (this.firstIntervalOpen) {
			this.intervalTracker = new JFrame();
			this.intervalTracker.setTitle("Intervals:");
			this.intervalTracker.setLayout(new BorderLayout());
			this.intervalTracker.add(new JPanel(), BorderLayout.NORTH);
			this.intervalTracker.add(new JPanel(), BorderLayout.EAST);
			this.intervalTracker.add(new JPanel(), BorderLayout.WEST);
			this.intervalTrackerPanel = new JPanel();
			this.intervalTracker.add(this.intervalTrackerPanel, BorderLayout.CENTER);
			this.intervalTrackerPanel.setLayout(new BoxLayout(this.intervalTrackerPanel, BoxLayout.Y_AXIS));
			this.intervalTrackerButtonPanel = new JPanel();
			this.intervalTrackerButtonPanel.add(this.addInterval);
			this.intervalTrackerPanel.add(this.intervalTrackerButtonPanel);
			this.intervalTracker.add(this.intervalTrackerButtonPanel, BorderLayout.SOUTH);
			this.intervalTracker.setSize(256, 136+7-55);
			Point windowPoint = this.window.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			this.intervalTracker.setLocation((int) (x + this.window.getWidth() - 14), y - (this.intervalTracker.getHeight() - this.window.getHeight()));
			this.intervalTracker.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			this.intervalTracker.setAlwaysOnTop(true);
			this.firstIntervalOpen = false;
			this.intervalTracker.setVisible(true);
		} else {
			this.intervalTracker.setVisible(true);
		}
	}
	
	private void buildIntervalTrackingUIArray() {
		this.intervalTrackerPanel.removeAll();
		this.intervalTrackingUIArray = new JButton[this.intervalCount];
		this.gfxCopy = new JLabel[this.intervalCount][10][8];
		for (int i = 0; i < this.intervalCount; i++) {
			for (int j = 0; j < this.gfxCopy[i].length; j++) {
				for (int k = 0; k < this.gfxCopy[i][j].length; k++) {
					this.gfxCopy[i][j][k] = new JLabel(this.gfxIcon[j]);
				}
			}
			for (int j = 0; j < gfxCopy[i].length; j++) {
				this.gfxCopy[i][j][2] = null;
				this.gfxCopy[i][j][5] = null;
			}
			this.gfxCopy[i][0][2] = new JLabel(new StretchIcon(AlwaysOnTopTimer.class.getResource("/Graphics/Clock_Colon.png")));
			this.gfxCopy[i][0][5] = new JLabel(new StretchIcon(AlwaysOnTopTimer.class.getResource("/Graphics/Clock_Colon.png")));
			this.intervalTrackingUIArray[i] = new JButton();
			this.intervalTrackingUIArray[i].addActionListener(this);
			this.intervalTrackingUIArray[i].setFocusable(false);
			this.intervalTrackingUIArray[i].setActionCommand("" + i);
			this.intervalTrackingUIArray[i].setLayout(new GridLayout(1, 0, 3, 0));
			for (int j = 0; j < gfxCopy[i][0].length; j++) {
				this.intervalTrackingUIArray[i].add(gfxCopy[i][0][j]);
			}
		}
		this.updateIntervalTrackingUI();
		for (int i = 0; i < this.intervalTrackingUIArray.length; i++) {
			this.intervalTrackerPanel.add(this.intervalTrackingUIArray[i]);
		}
		this.intervalTracker.repaint();
		this.intervalTracker.revalidate();
	}
	
	private void deleteIntervalArrayIndex(int intervalIndex) {
		int newLength = this.savedIntervals.length - 1;
		if (newLength == 0) {
			this.savedIntervals = null;
			this.savedIntervals = new int[1][2][6];
		} else {
			int[][][] temp = new int[newLength][2][6];
			int h = 0;
			for (int i = 0; i < temp.length; i++) {
				if (i == intervalIndex) {
					h++;
				}
				for (int j = 0; j < 6; j++) {
					temp[i][0][j] = this.savedIntervals[h][0][j];
				}
				temp[i][1][0] = this.savedIntervals[h][1][0];
				h++;
			}
			this.savedIntervals = temp;
		}
		this.intervalTracker.setSize(this.intervalTracker.getWidth(), this.intervalTracker.getHeight() - 55);
		this.savedIntervalCount--;
		this.intervalCount--;
		this.currentInterval--;
		if (this.intervalCount == 0) {
			this.intervalTrackerButtonPanel.remove(this.clearIntervals);
			this.buttonPanel.remove(this.start);
			this.window.repaint();
			this.window.revalidate();
		}
//		Point windowPoint = this.window.getLocation();
//		int x = (int) windowPoint.getX();
//		int y = (int) windowPoint.getY();
//		this.intervalTracker.setLocation((int) (x + this.window.getWidth() - 14), y - (this.intervalTracker.getHeight() - this.window.getHeight()));
		Point intervalTrackerPoint = this.intervalTracker.getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		this.intervalTracker.setLocation(x, y+55);
		this.buildIntervalTrackingUIArray();
		this.intervalTrackerColorIndicatorUpdate();
	}
	
	private void updateIntervalTrackingUI() {
		int interInputCount = 0;
		int input[] = new int[6];
		for (int interval = 0; interval < this.savedIntervals.length; interval++) {
			interInputCount = this.savedIntervals[interval][1][0];
			for (int i = 0; i < input.length; i++) {
				input[i] = this.savedIntervals[interval][0][i];
			}
			if (interInputCount == 1) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][7]);
			} else if (interInputCount == 2) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][7]);
			} else if (interInputCount == 3) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][7]);
			} else if (interInputCount == 4) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][7]);
			} else if (interInputCount == 5) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][7]);
			} else if (interInputCount == 6) {
				this.intervalTrackingUIArray[interval].removeAll();
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[5]][0]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[4]][1]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][5]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[3]][3]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[2]][4]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][0][2]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[1]][6]);
				this.intervalTrackingUIArray[interval].add(this.gfxCopy[interval][input[0]][7]);
			} else if (interInputCount >= 7) {
				throw new IllegalArgumentException("You cannot input past the end of the clock face.");
			}
		}
		this.intervalTracker.repaint();
		this.intervalTracker.revalidate();
	}
	
	private void borderAlertFlashing() {
		if (this.westBorder.getBackground() == this.defaultPanelColor && this.flashing) {
			this.westBorder.setBackground(this.flashColor);
			this.eastBorder.setBackground(this.flashColor);
			this.windowTimerPanel.setBackground(this.flashColor);
		} else {
			this.westBorder.setBackground(this.defaultPanelColor);
			this.eastBorder.setBackground(this.defaultPanelColor);
			this.windowTimerPanel.setBackground(this.defaultPanelColor);
		}
		this.window.repaint();
		this.window.revalidate();
	}

	private void clockFaceCountUp() {
		this.tenthSeconds++;
		if (this.tenthSeconds == 10) {
			this.tenthSeconds = 0;
			this.seconds++;
			if (this.seconds == 10) {
				this.seconds = 0;
				this.decaSeconds++;
				if (this.decaSeconds == 6) {
					this.decaSeconds = 0;
					this.minutes++;
					if (this.minutes == 10) {
						this.minutes = 0;
						this.decaMinutes++;
						if (this.decaMinutes == 6) {
							this.decaMinutes = 0;
							this.hours++;
							if (this.hours == 10) {
								this.hours = 0;
								this.decaHours++;
								if (this.decaHours == 10) {
									this.decaHours = 0;
								}
							}
						}
					}
				}
			}
		}
		this.updateClockFace();
	}
	
	private void clockFaceCountDown() {
		if (this.tenthSeconds > 0) {
			this.tenthSeconds--;
			this.updateClockFace();
			return;
		}
		if (this.tenthSeconds == 0 && (this.seconds > 0 || this.decaSeconds > 0 || this.minutes > 0 || this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
			this.tenthSeconds = 9;
			if (this.seconds > 0) {
				this.seconds--;
				this.updateClockFace();
				return;
			}
			if (this.seconds == 0 && (this.decaSeconds > 0 || this.minutes > 0 || this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
				this.seconds = 9;
				if (this.decaSeconds > 0) {
					this.decaSeconds--;
					this.updateClockFace();
					return;
				}
				if (this.decaSeconds == 0 && (this.minutes > 0 || this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
					this.decaSeconds = 5;
					if (this.minutes > 0) {
						this.minutes--;
						this.updateClockFace();
						return;
					}
					if (this.minutes == 0 && (this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
						this.minutes = 9;
						if (this.decaMinutes > 0) {
							this.decaMinutes--;
							this.updateClockFace();
							return;
						}
						if (this.decaMinutes == 0 && (this.hours > 0 || this.decaHours > 0)) {
							this.decaMinutes = 5;
							if (this.hours > 0) {
								this.hours--;
								this.updateClockFace();
								return;
							}
							if (this.hours == 0 && this.decaHours > 0) {
								this.hours = 9;
								if (this.decaHours > 0) {
									this.decaHours--;
								}
							}
						}
					}
				}
			}
		}
		this.updateClockFace();
	}
	
	private void intervalClockFaceUpdate() {
		if (this.clockFaceInterval >= this.savedIntervalCount) {
			this.clockFaceInterval = 0;
		}
		int interInputCount = this.savedIntervals[this.clockFaceInterval][1][0];
		int[] input = new int[6];
		for (int i = 0; i < input.length; i++) {
			input[i] = this.savedIntervals[this.clockFaceInterval][0][i];
		}
		if (interInputCount == 0) {
			this.tenthSeconds = 0;
			this.seconds = 0;
			this.decaSeconds = 0;
			this.minutes = 0;
			this.decaMinutes = 0;
			this.hours = 0;
			this.decaHours = 0;
		} else if (interInputCount == 1) {
			this.seconds = input[5];
			this.decaSeconds = input[0];
			this.minutes = input[1];
			this.decaMinutes = input[2];
			this.hours = input[3];
			this.decaHours = input[4];
		} else if (interInputCount == 2) {
			this.seconds = input[4];
			this.decaSeconds = input[5];
			this.minutes = input[0];
			this.decaMinutes = input[1];
			this.hours = input[2];
			this.decaHours = input[3];
		} else if (interInputCount == 3) {
			this.seconds = input[3];
			this.decaSeconds = input[4];
			this.minutes = input[5];
			this.decaMinutes = input[0];
			this.hours = input[1];
			this.decaHours = input[2];
		} else if (interInputCount == 4) {
			this.seconds = input[2];
			this.decaSeconds = input[3];
			this.minutes = input[4];
			this.decaMinutes = input[5];
			this.hours = input[0];
			this.decaHours = input[1];
		} else if (interInputCount == 5) {
			this.seconds = input[1];
			this.decaSeconds = input[2];
			this.minutes = input[3];
			this.decaMinutes = input[4];
			this.hours = input[5];
			this.decaHours = input[0];
		} else if (interInputCount == 6) {
			this.seconds = input[0];
			this.decaSeconds = input[1];
			this.minutes = input[2];
			this.decaMinutes = input[3];
			this.hours = input[4];
			this.decaHours = input[5];
		} else if (interInputCount >= 7) {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		this.updateClockFace();
		this.intervalTrackerColorIndicatorUpdate();
	}
	
	private void intervalTrackerColorIndicatorUpdate() {
		for (int i = 0; i < this.intervalTrackingUIArray.length; i++) {
			this.intervalTrackingUIArray[i].setBackground(new JButton().getBackground());
		}
		if (this.clockFaceInterval >= 0 && this.clockFaceInterval < this.intervalTrackingUIArray.length) {
			this.intervalTrackingUIArray[this.clockFaceInterval].setBackground(this.activeIntervalColor);
			this.intervalTrackingUIArray[this.clockFaceInterval].setBorderPainted(true);
			this.intervalTracker.repaint();
			this.intervalTracker.revalidate();
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException {
		AlwaysOnTopTimer thisTimer = new AlwaysOnTopTimer();
		thisTimer.window.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.stopwatch) {
			this.addClockFaceAndButtons(AlwaysOnTopTimer.STOPWATCH_TYPE);
		} else if (e.getSource() == this.countdown) {
			this.addClockFaceAndButtons(AlwaysOnTopTimer.COUNTDOWN_TYPE);
		} else if (e.getSource() == this.interval) {
			this.addClockFaceAndButtons(AlwaysOnTopTimer.INTERVAL_TYPE);
		}  else if (e.getSource() == this.back) {
			this.returnToStartWindow();
		} else if (this.currentType == AlwaysOnTopTimer.STOPWATCH_TYPE) {
			if (e.getSource() == this.timer) {
				this.clockFaceCountUp();
			} else if (e.getSource() == this.start) {
//				if (this.timer.getDelay() != 100) {
//					this.timer.setDelay(100);
//				}
				this.timer.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.pause) {
				this.timer.stop();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.timer.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.reset) {
				if (this.timer.isRunning()) {
					this.timer.stop();
				}
				this.resetEntireSystem();
				this.addClockFaceAndButtons(AlwaysOnTopTimer.STOPWATCH_TYPE);
			}
		} else if (this.currentType == AlwaysOnTopTimer.COUNTDOWN_TYPE) {
			if (e.getSource() == this.timer || e.getSource() == this.swingTimer) {
				if (e.getSource() == this.timer && (this.tenthSeconds > 0 || this.seconds > 0 || this.decaSeconds > 0 || this.minutes > 0 || this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
					this.clockFaceCountDown();
				} else if (e.getSource() == this.timer && this.tenthSeconds == 0 && this.seconds == 0 && this.decaSeconds == 0 && this.minutes == 0 && this.decaMinutes == 0 && this.hours == 0 && this.decaHours == 0) {
					this.swingTimer.start();
				} else {
					if (this.swingTimer.getDelay() == 100) {
						this.swingTimer.stop();
						this.swingTimer.setDelay(1000);
						this.swingTimer.start();
					} else {
						this.swingTimer.stop();
						this.swingTimer.setDelay(700);
						this.swingTimer.start();
					}
					if (this.flashing == false) {
						this.flashing = true;
					}
					this.buttonPanel.removeAll();
					this.buttonPanel.add(this.back);
					this.buttonPanel.add(this.reset);
					this.borderAlertFlashing();
				}
			} else if (e.getSource() == this.setup) {
				if (this.inputCount != 0) {
					for (int i = 0; i < this.countdownInput.length; i++) {
						this.countdownInputBackup[i] = new Integer(this.countdownInput[i]);
						this.countdownInput[i] = 0;
					}
					this.inputCountBackup = new Integer(this.inputCount);
					this.inputCount = 0;
				}
				this.applyCountdownSetupWindowInput();
				this.callSetupWindow();
			} else if (e.getSource() == this.cancel) {
				for (int i = 0; i < this.countdownInput.length; i++) {
					this.countdownInput[i] = new Integer(this.countdownInputBackup[i]);
					this.countdownInputBackup[i] = 0;
				}
				this.inputCount = new Integer(this.inputCountBackup);
				this.inputCountBackup = 0;
				this.applyCountdownSetupWindowInput();
				this.setupWindow.dispose();
			} else if (e.getSource() == this.confirm) {
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.setup);
				this.buttonPanel.add(this.start);
				this.setupWindow.dispose();
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.number[0]) {
				this.countdownSetupWindowInput(0);
			} else if (e.getSource() == this.number[1]) {
				this.countdownSetupWindowInput(1);
			} else if (e.getSource() == this.number[2]) {
				this.countdownSetupWindowInput(2);
			} else if (e.getSource() == this.number[3]) {
				this.countdownSetupWindowInput(3);
			} else if (e.getSource() == this.number[4]) {
				this.countdownSetupWindowInput(4);
			} else if (e.getSource() == this.number[5]) {
				this.countdownSetupWindowInput(5);
			} else if (e.getSource() == this.number[6]) {
				this.countdownSetupWindowInput(6);
			} else if (e.getSource() == this.number[7]) {
				this.countdownSetupWindowInput(7);
			} else if (e.getSource() == this.number[8]) {
				this.countdownSetupWindowInput(8);
			} else if (e.getSource() == this.number[9]) {
				this.countdownSetupWindowInput(9);
			} else if (e.getSource() == this.start) {
				this.timer.start();
				if (this.swingTimer.getDelay() != 100) {
					this.swingTimer.setDelay(100);
				}
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.reset) {
				if (this.timer.isRunning()) {
					this.timer.stop();
				}
				if (this.swingTimer.isRunning()) {
					this.swingTimer.stop();
				}
				if (this.flashing) {
					this.flashing = false;
					this.borderAlertFlashing();
				}
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.setup);
				this.buttonPanel.add(this.start);
				this.tenthSeconds = 0;
				this.applyCountdownSetupWindowInput();
			} else if (e.getSource() == this.pause) {
				this.timer.stop();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.timer.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			}
		} else if (this.currentType == AlwaysOnTopTimer.INTERVAL_TYPE) {
			if (e.getSource() == this.setup) {
				this.callIntervalTrackerWindow();
			} else if (e.getSource() == this.addInterval) {
				this.intervalCount++;
				this.currentInterval++;
				if (!this.timer.isRunning()) {
					this.buttonPanel.add(this.start);
					this.window.repaint();
					this.window.revalidate();
				}
				this.buildIntervalTrackingUIArray();
				if (!this.timer.isRunning()) {
					this.intervalTrackerButtonPanel.add(this.clearIntervals);
				}
				Point windowPoint = this.intervalTracker.getLocation();
				this.intervalTracker.setLocation((int) windowPoint.getX(), (int) windowPoint.getY() - 55);
				Dimension windowSize = this.intervalTracker.getSize();
				int windowHeight = windowSize.height;
				int windowWidth = windowSize.width;
				this.intervalTracker.setSize(windowWidth, windowHeight + 55);
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.intervalTrackerColorIndicatorUpdate(); //also calls repaint/revalidate
				this.callSetupWindow();
			} else if (e.getSource() == this.clearIntervals) {
				this.intervalTrackerPanel.removeAll();
				this.intervalTrackerButtonPanel.removeAll();
				this.intervalTrackerButtonPanel.add(this.addInterval);
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.buttonPanel.remove(this.start);
				this.window.repaint();
				this.window.revalidate();
//				Point windowPoint = this.window.getLocation();
//				int x = (int) windowPoint.getX();
//				int y = (int) windowPoint.getY();
				this.intervalTracker.setSize(256, 136+7-55);
				Point intervalTrackerPoint = this.intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				this.intervalTracker.setLocation(x, y+(55*this.intervalCount));
//				this.intervalTracker.setLocation((int) (x + this.window.getWidth() - 14), y - (this.intervalTracker.getHeight() - this.window.getHeight()));
//				this.intervalTracker.repaint();
//				this.intervalTracker.revalidate();
				this.savedIntervals = null;
				this.savedIntervals = new int[1][2][6];
				this.savedIntervalCount = 0;
				this.intervalCount = 0;
				this.currentInterval = -1;
				this.tenthSeconds = 0;
				this.seconds = 0;
				this.decaSeconds = 0;
				this.minutes = 0;
				this.decaMinutes = 0;
				this.hours = 0;
				this.decaHours = 0;
				this.intervalClockFaceUpdate();
				this.buildIntervalTrackingUIArray();
			} else if (e.getSource() == this.number[0]) {
				this.intervalSetupWindowInput(0);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[1]) {
				this.intervalSetupWindowInput(1);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[2]) {
				this.intervalSetupWindowInput(2);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[3]) {
				this.intervalSetupWindowInput(3);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[4]) {
				this.intervalSetupWindowInput(4);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[5]) {
				this.intervalSetupWindowInput(5);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[6]) {
				this.intervalSetupWindowInput(6);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[7]) {
				this.intervalSetupWindowInput(7);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[8]) {
				this.intervalSetupWindowInput(8);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.number[9]) {
				this.intervalSetupWindowInput(9);
				this.applyIntervalSetupWindowInput(this.currentInterval);
			} else if (e.getSource() == this.cancel) {
				this.intervalTracker.setSize(this.intervalTracker.getWidth(), this.intervalTracker.getHeight() - 55);
				this.intervalCount--;
				this.currentInterval--;
				if (this.intervalCount == 0) {
					this.intervalTrackerButtonPanel.remove(this.clearIntervals);
					this.buttonPanel.remove(this.start);
					this.window.repaint();
					this.window.revalidate();
				}
				this.intervalInput = null;
				this.intervalInput = new int[6];
				this.intervalInputCount = 0;
				this.buildIntervalTrackingUIArray();
//				Point windowPoint = this.window.getLocation();
//				int x = (int) windowPoint.getX();
//				int y = (int) windowPoint.getY();
//				this.intervalTracker.setLocation((int) (x + this.window.getWidth() - 14), y - (this.intervalTracker.getHeight() - this.window.getHeight()));
				Point intervalTrackerPoint = this.intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				this.intervalTracker.setLocation(x, y+55);
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.intervalTrackerColorIndicatorUpdate(); //also calls repaint/revalidate
				this.setupWindow.dispose();
			} else if (e.getSource() == this.confirm) {
				this.saveIntervalSetupWindowInput(-1, false);
				this.updateIntervalTrackingUI();
				if (!this.timer.isRunning()) {
					this.intervalClockFaceUpdate();
				}
				this.intervalTrackerColorIndicatorUpdate();
				this.setupWindow.dispose();
			} else if (e.getSource() == this.start) {
				this.buttonPanel.remove(this.start);
				this.buttonPanel.remove(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.intervalTrackerButtonPanel.remove(this.clearIntervals);
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.intervalClockFaceUpdate();
				this.intervalTrackerColorIndicatorUpdate();
				this.timer.start();
				if (this.swingTimer.getDelay() != 100) {
					this.swingTimer.setDelay(100);
				}
			} else if (e.getSource() == this.pause) {
				this.timer.stop();
				this.buttonPanel.remove(this.pause);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.timer.start();
				this.buttonPanel.remove(this.resume);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.reset) {
				this.timer.stop();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.setup);
				this.buttonPanel.add(this.start);
				if (this.savedIntervalCount > 0) {
					this.intervalTrackerButtonPanel.add(this.clearIntervals);
				}
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.clockFaceInterval = 0;
				this.tenthSeconds = 0;
				this.seconds = 0;
				this.decaSeconds = 0;
				this.minutes = 0;
				this.decaMinutes = 0;
				this.hours = 0;
				this.decaHours = 0;
				this.intervalClockFaceUpdate();
				this.intervalTrackerColorIndicatorUpdate();
			} else if (e.getSource() == this.timer || e.getSource() == this.swingTimer) {
				if (e.getSource() == this.timer && (this.tenthSeconds > 0 || this.seconds > 0 || this.decaSeconds > 0 || this.minutes > 0 || this.decaMinutes > 0 || this.hours > 0 || this.decaHours > 0)) {
					this.clockFaceCountDown();
				} else if (e.getSource() == this.timer && this.tenthSeconds == 0 && this.seconds == 0 && this.decaSeconds == 0 && this.minutes == 0 && this.decaMinutes == 0 && this.hours == 0 && this.decaHours == 0) {
					this.swingTimer.start();
				} else {
					if (this.swingTimer.getDelay() == 100) {
						this.swingTimer.stop();
						this.swingTimer.setDelay(1000);
						this.swingTimer.start();
					} else {
						this.swingTimer.stop();
						this.swingTimer.setDelay(700);
						this.swingTimer.start();
					}
					if (this.flashing == false) {
						this.flashing = true;
						this.clockFaceInterval++;
						this.intervalClockFaceUpdate();
					} else if (this.flashing) {
						this.flashing = false;
						this.swingTimer.stop();
						this.swingTimer.setDelay(100);
					}
					this.borderAlertFlashing();
				}
			} else if (e.getSource() == this.edit) {
				this.prompt.dispose();
				this.callEditSetupWindow(this.edit.getActionCommand());
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
			} else if (e.getSource() == this.skipto) {
				this.prompt.dispose();
				this.tenthSeconds = 0;
				this.clockFaceInterval = Integer.parseInt(this.skipto.getActionCommand());
				this.intervalClockFaceUpdate();
				this.intervalTrackerColorIndicatorUpdate();
			} else if (e.getSource() == this.delete) {
				this.prompt.dispose();
				int intervalIndex = Integer.parseInt(this.delete.getActionCommand());
				this.deleteIntervalArrayIndex(intervalIndex);
				if (this.savedIntervalCount == 0) {
					this.intervalClockFaceUpdate();
					this.buttonPanel.removeAll();
					this.buttonPanel.add(this.back);
					this.buttonPanel.add(this.setup);
					this.window.repaint();
					this.window.revalidate();
				}
			} else if (e.getSource() == this.editNumber[0]) {
				this.intervalSetupWindowInput(0);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[0].getActionCommand()));
			} else if (e.getSource() == this.editNumber[1]) {
				this.intervalSetupWindowInput(1);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[1].getActionCommand()));
			} else if (e.getSource() == this.editNumber[2]) {
				this.intervalSetupWindowInput(2);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[2].getActionCommand()));
			} else if (e.getSource() == this.editNumber[3]) {
				this.intervalSetupWindowInput(3);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[3].getActionCommand()));
			} else if (e.getSource() == this.editNumber[4]) {
				this.intervalSetupWindowInput(4);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[4].getActionCommand()));
			} else if (e.getSource() == this.editNumber[5]) {
				this.intervalSetupWindowInput(5);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[5].getActionCommand()));
			} else if (e.getSource() == this.editNumber[6]) {
				this.intervalSetupWindowInput(6);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[6].getActionCommand()));
			} else if (e.getSource() == this.editNumber[7]) {
				this.intervalSetupWindowInput(7);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[7].getActionCommand()));
			} else if (e.getSource() == this.editNumber[8]) {
				this.intervalSetupWindowInput(8);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[8].getActionCommand()));
			} else if (e.getSource() == this.editNumber[9]) {
				this.intervalSetupWindowInput(9);
				this.applyIntervalSetupWindowInput(Integer.parseInt(this.editNumber[9].getActionCommand()));
			} else if (e.getSource() == this.editCancel) {
				this.intervalInput = null;
				this.intervalInput = new int[6];
				this.intervalInputCount = 0;
				this.updateIntervalTrackingUI();
				this.editWindow.dispose();
			} else if (e.getSource() == this.editConfirm) { 
				this.saveIntervalSetupWindowInput(Integer.parseInt(this.editConfirm.getActionCommand()), true);
				this.buildIntervalTrackingUIArray();
				this.intervalClockFaceUpdate();
				this.intervalTrackerColorIndicatorUpdate();
				this.editWindow.dispose();
			} else {
				for (int i = 0; i < this.intervalTrackingUIArray.length; i++) {
					if (e.getSource() == this.intervalTrackingUIArray[i]) {
						String command = this.intervalTrackingUIArray[i].getActionCommand();
						if (this.clockFaceInterval != Integer.parseInt(command) || !this.timer.isRunning()) {
							this.callIntervalPromptWindow(command);
						}
					}
				}
			}
		}
	}
}
