package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import darrylbu.icon.StretchIcon;

public class AlwaysOnTopTimer implements ActionListener {
	
	private static final StretchIcon[] GRAPHICS;
	
	static {
		//Ten digits, and a colon
		GRAPHICS = new StretchIcon[11];
		
		try {
			for (int i = 0; i < GRAPHICS.length - 1; i++) {
				GRAPHICS[i] = new StretchIcon(ImageIO.read(AlwaysOnTopTimer.class.getResource("graphics/Clock_Number_" + i + ".png")));
			}
			
			GRAPHICS[GRAPHICS.length - 1] = new StretchIcon(ImageIO.read(AlwaysOnTopTimer.class.getResource("graphics/Clock_Colon.png")));
		} catch (IOException e) {
			//TODO: Decide how to handle this. The graphics are important, so maybe just close the application?
		}
	}
	
	private enum TimerType {
		NONE,
		STOPWATCH,
		COUNTDOWN,
		INTERVAL;
	}
	
	private TimerType currentType = TimerType.NONE;
	
	class ClockFace extends JPanel {
		
		/**
		 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
		 */
		private static final long serialVersionUID = 1L;
		
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
			
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (tenthSeconds == 0) return;
				
				int x, y = 1;
				int barWidth = (getWidth() - westBorder.getWidth() - eastBorder.getWidth() - 6);
				int modNine = barWidth % 9;
				int width, height = northBorder.getHeight() - 1;
				
				if (countdown) {
					int correction = modNine - tenthSeconds;
					if (correction < 0) correction = 0;
					x = (westBorder.getWidth() + 3) + ((barWidth/9) * (9 - tenthSeconds)) + correction;
					width = (getWidth() - eastBorder.getWidth() - 3 - x);
				} else {
					int correction = modNine;
					if (correction > tenthSeconds) correction = tenthSeconds;
					x = westBorder.getWidth() + 3;
					width = (barWidth / 9) * tenthSeconds + correction;
				}
				
				g.setColor(new Color(112, 146, 190));
				g.fillRect(x, y, width, height);
			}
		};
		
		/**
		 * Dictates whether this clock face ticks up or down.
		 * <p>
		 * When true, this clock face will tick down.
		 */
		boolean countdown;
		
		boolean stopAtZero;
		
		boolean ticking = false;
		
		java.util.Timer timer;
		
		ActionListener listener;
		
		JLabel[] clockDisplay = new JLabel[8];
		
		ClockFace(boolean stopTickingAtZero) {
			setLayout(new GridLayout(1, 0, 3, 0));
			stopAtZero = stopTickingAtZero;
			
			for (int i = 0; i < clockDisplay.length; i++) {
				clockDisplay[i] = new JLabel();
				
				if (i == 2 || i == 5)
					//Set colons
					clockDisplay[i].setIcon(GRAPHICS[10]);
				else
					//Set zero digit
					clockDisplay[i].setIcon(GRAPHICS[0]);
				
				add(clockDisplay[i]);
			}
		}
		
		ClockFace(ActionListener listener, boolean stopTickingAtZero) {
			this(stopTickingAtZero);
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
		
		void setTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
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
			timer = new java.util.Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					SwingUtilities.invokeLater(() -> {
						tick();
						updateClockFace();
						fireActionEventIfCountdownReachedZero();
					});
				}
			}, 0, 100);
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
			for (int i = 0; i < clockDisplay.length; i++) {
				switch (i) {
					case 2:
					case 5:
						//Colon slots. Do nothing.
						break;
					case 0: //decaHours
						clockDisplay[i].setIcon(GRAPHICS[decaHours]);
						break;
					case 1: //hours
						clockDisplay[i].setIcon(GRAPHICS[hours]);
						break;
					case 3: //decaMinutes
						clockDisplay[i].setIcon(GRAPHICS[decaMinutes]);
						break;
					case 4: //minutes
						clockDisplay[i].setIcon(GRAPHICS[minutes]);
						break;
					case 6: //decaSeconds
						clockDisplay[i].setIcon(GRAPHICS[decaSeconds]);
						break;
					case 7: //seconds
						clockDisplay[i].setIcon(GRAPHICS[seconds]);
						break;
				}
			}
			window.repaint();
			window.revalidate();
		}
		
		private void fireActionEventIfCountdownReachedZero() {
			if (!countdown) return;
			if (tenthSeconds == 0 & seconds == 0 & decaSeconds == 0 & minutes == 0 & decaMinutes == 0 & hours == 0 & decaHours == 0) {
				if (stopAtZero) stop();
				final ClockFace source = this;
				listener.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
			}
		}
	}
	
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
	private ClockFace windowTimerPanel;
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
		this.swingTimer = new Timer(100, this);
		this.swingTimer.setInitialDelay(0);
		window.setVisible(true);
	}
	
	private void returnToStartWindow() {
		if (this.swingTimer.isRunning()) {
			this.windowTimerPanel.stop();
			this.swingTimer.stop();
		}
		this.window.remove(this.windowTimerPanel);
		this.window.add(this.startingWindowPanel, BorderLayout.CENTER);
		this.window.remove(this.buttonPanel);
		this.window.add(this.southBorder, BorderLayout.SOUTH);
		this.resetEntireSystem();
	}
	
	private void addClockFaceAndButtons(TimerType type) {
		switch (type) {
			case STOPWATCH:
				windowTimerPanel = new ClockFace(this, true);
				windowTimerPanel.setCountdown(false);
				
				this.window.remove(this.startingWindowPanel);
				this.window.setTitle("Yon Timer -- Stopwatch");
				this.window.add(this.windowTimerPanel, BorderLayout.CENTER);
				this.window.remove(this.northBorder);
				this.window.add(this.windowTimerPanel.getTenthsIndicator(), BorderLayout.NORTH);
				this.window.remove(this.southBorder);
				this.window.add(this.buttonPanel, BorderLayout.SOUTH);
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.start);
				break;
			case COUNTDOWN:
			case INTERVAL:
				if (type == TimerType.COUNTDOWN) {
					this.window.setTitle("Yon Timer -- Countdown");
					windowTimerPanel = new ClockFace(this, true);
				} else if (type == TimerType.INTERVAL) {
					this.window.setTitle("Yon Timer -- Interval");
					windowTimerPanel = new ClockFace(this, false);
				}
				windowTimerPanel.setCountdown(true);
				
				this.window.remove(this.startingWindowPanel);
				this.window.add(this.windowTimerPanel, BorderLayout.CENTER);
				this.window.remove(this.northBorder);
				this.window.add(this.windowTimerPanel.getTenthsIndicator(), BorderLayout.NORTH);
				this.window.remove(this.southBorder);
				this.window.add(this.buttonPanel, BorderLayout.SOUTH);
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.setup);
				break;
			default:
				throw new IllegalArgumentException("The paramater 'type' must be one of the type constants defined in the AlwaysOnTopTimer Class.");
		}
		this.currentType = type;
		this.window.repaint();
		this.window.revalidate();
	}
	
	private void resetEntireSystem() {
		this.windowTimerPanel.stop();
		if (this.swingTimer.isRunning()) {
			this.swingTimer.stop();
		}
		
		if (this.swingTimer.getDelay() != 100) {
			this.swingTimer.setDelay(100);
		}
		
		this.window.remove(this.windowTimerPanel.getTenthsIndicator());
		this.window.add(this.northBorder, BorderLayout.NORTH);
		
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
		this.currentType = TimerType.NONE;
		this.windowTimerPanel.setTime(0, 0, 0, 0, 0, 0);
		this.window.setTitle("Yon Timer");
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
			this.number[i].setIcon(GRAPHICS[i]);
			this.number[i].addActionListener(this);
			this.number[i].setFocusable(false);
			setupWindowKeypad.add(number[i]);
			if (i == this.number.length - 1) {
				this.number[0] = new JButton();
				this.number[0].setIcon(GRAPHICS[0]);
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
		this.setupWindow.setSize(275 + 14, 198 + 7);
		this.setupWindow.setMaximumSize(setupWindow.getSize());
		if (this.currentType == TimerType.COUNTDOWN) {
			Point windowPoint = this.window.getLocation();
			int x = (int) windowPoint.getX();
			int y = (int) windowPoint.getY();
			this.setupWindow.setLocation((int) (x + this.window.getWidth() - 14), y - (setupWindow.getHeight() - this.window.getHeight()));
		} else if (this.currentType == TimerType.INTERVAL) {
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
		if (this.windowTimerPanel.isRunning()) {
			this.edit.setEnabled(false);
		} else {
			this.edit.setEnabled(true);
		}
		this.prompt.setSize(241 + 14, 89 + 7);
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
			this.editNumber[i].setIcon(GRAPHICS[i]);
			this.editNumber[i].addActionListener(this);
			this.editNumber[i].setFocusable(false);
			this.editNumber[i].setActionCommand(command);
			setupWindowKeypad.add(this.editNumber[i]);
			if (i == this.editNumber.length - 1) {
				this.editNumber[0] = new JButton();
				this.editNumber[0].setIcon(GRAPHICS[0]);
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
		this.editWindow.setSize(275 + 14, 198 + 7);
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
			this.countdownInput[(this.countdownInput.length - 1) - this.inputCount] = input;
			this.inputCount++;
			this.applyCountdownSetupWindowInput();
		} else if (this.inputCount == 6) {
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
	}
	
	private void intervalSetupWindowInput(int input) {
		if (this.intervalInputCount < 6) {
			this.intervalInput[(this.intervalInput.length - 1) - this.intervalInputCount] = input;
			this.intervalInputCount++;
		} else if (this.intervalInputCount == 6) {
			return;
		}
	}
	
	private static JLabel[] getLabelsForTime(int seconds, int decaSeconds, int minutes, int decaMinutes, int hours, int decaHours) {
		JLabel[] clockDisplay = new JLabel[8];
		for (int i = 0; i < clockDisplay.length; i++) {
			clockDisplay[i] = new JLabel();
		}
		clockDisplay[0].setIcon(GRAPHICS[decaHours]);
		clockDisplay[1].setIcon(GRAPHICS[hours]);
		clockDisplay[2].setIcon(GRAPHICS[10]);
		clockDisplay[3].setIcon(GRAPHICS[decaMinutes]);
		clockDisplay[4].setIcon(GRAPHICS[minutes]);
		clockDisplay[5].setIcon(GRAPHICS[10]);
		clockDisplay[6].setIcon(GRAPHICS[decaSeconds]);
		clockDisplay[7].setIcon(GRAPHICS[seconds]);
		return clockDisplay;
	}
	
	private void addTimeLabelsForInterval(int interval, JLabel[] labels) {
		this.intervalTrackingUIArray[interval].removeAll();
		for (JLabel timeLabel: labels) {
			this.intervalTrackingUIArray[interval].add(timeLabel);
		}
	}
	
	private void applyIntervalSetupWindowInput(int interval) {
		int seconds, decaSeconds, minutes, decaMinutes, hours, decaHours;
		if (this.intervalInputCount == 1) {
			decaHours = this.intervalInput[4];
			hours = this.intervalInput[3];
			decaMinutes = this.intervalInput[2];
			minutes = this.intervalInput[1];
			decaSeconds = this.intervalInput[0];
			seconds = this.intervalInput[5];
		} else if (this.intervalInputCount == 2) {
			decaHours = this.intervalInput[3];
			hours = this.intervalInput[2];
			decaMinutes = this.intervalInput[1];
			minutes = this.intervalInput[0];
			decaSeconds = this.intervalInput[5];
			seconds = this.intervalInput[4];
		} else if (this.intervalInputCount == 3) {
			decaHours = this.intervalInput[2];
			hours = this.intervalInput[1];
			decaMinutes = this.intervalInput[0];
			minutes = this.intervalInput[5];
			decaSeconds = this.intervalInput[4];
			seconds = this.intervalInput[3];
		} else if (this.intervalInputCount == 4) {
			decaHours = this.intervalInput[1];
			hours = this.intervalInput[0];
			decaMinutes = this.intervalInput[5];
			minutes = this.intervalInput[4];
			decaSeconds = this.intervalInput[3];
			seconds = this.intervalInput[2];
		} else if (this.intervalInputCount == 5) {
			decaHours = this.intervalInput[0];
			hours = this.intervalInput[5];
			decaMinutes = this.intervalInput[4];
			minutes = this.intervalInput[3];
			decaSeconds = this.intervalInput[2];
			seconds = this.intervalInput[1];
		} else if (this.intervalInputCount == 6) {
			decaHours = this.intervalInput[5];
			hours = this.intervalInput[4];
			decaMinutes = this.intervalInput[3];
			minutes = this.intervalInput[2];
			decaSeconds = this.intervalInput[1];
			seconds = this.intervalInput[0];
		} else /*if (this.intervalInputCount >= 7)*/ {
			throw new IllegalArgumentException("You cannot input past the end of the clock face.");
		}
		addTimeLabelsForInterval(interval, getLabelsForTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours));
		this.intervalTracker.repaint();
		this.intervalTracker.revalidate();
	}
	
	private void saveIntervalSetupWindowInput(int interval, boolean edit) {
		if (this.savedIntervals.length == this.savedIntervalCount) {
			int[][][] temp = new int[this.savedIntervals.length + 1][this.savedIntervals[0].length][this.savedIntervals[0][0].length];
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
			this.intervalTracker.setSize(256, 136 + 7 - 55);
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
		for (int i = 0; i < this.intervalCount; i++) {
			this.intervalTrackingUIArray[i] = new JButton();
			this.intervalTrackingUIArray[i].addActionListener(this);
			this.intervalTrackingUIArray[i].setFocusable(false);
			this.intervalTrackingUIArray[i].setActionCommand("" + i);
			this.intervalTrackingUIArray[i].setLayout(new GridLayout(1, 0, 3, 0));
			addTimeLabelsForInterval(i, getLabelsForTime(0, 0, 0, 0, 0, 0));
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
		Point intervalTrackerPoint = this.intervalTracker.getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		this.intervalTracker.setLocation(x, y + 55);
		this.buildIntervalTrackingUIArray();
		this.intervalTrackerColorIndicatorUpdate();
	}
	
	private void updateIntervalTrackingUI() {
		int interInputCount = 0;
		int[] input = new int[6];
		for (int interval = 0; interval < this.savedIntervalCount; interval++) {
			interInputCount = this.savedIntervals[interval][1][0];
			for (int i = 0; i < input.length; i++) {
				input[i] = this.savedIntervals[interval][0][i];
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
			addTimeLabelsForInterval(interval, getLabelsForTime(seconds, decaSeconds, minutes, decaMinutes, hours, decaHours));
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
	
	private void intervalClockFaceUpdate() {
		if (this.clockFaceInterval >= this.savedIntervalCount) {
			this.clockFaceInterval = 0;
		}
		int interInputCount = this.savedIntervals[this.clockFaceInterval][1][0];
		int[] input = new int[6];
		for (int i = 0; i < input.length; i++) {
			input[i] = this.savedIntervals[this.clockFaceInterval][0][i];
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
		if (e.getSource() == this.stopwatch) {
			this.addClockFaceAndButtons(TimerType.STOPWATCH);
		} else if (e.getSource() == this.countdown) {
			this.addClockFaceAndButtons(TimerType.COUNTDOWN);
		} else if (e.getSource() == this.interval) {
			this.addClockFaceAndButtons(TimerType.INTERVAL);
		} else if (e.getSource() == this.back) {
			this.returnToStartWindow();
		} else if (this.currentType == TimerType.STOPWATCH) {
			if (e.getSource() == this.start) {
				this.windowTimerPanel.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.pause) {
				this.windowTimerPanel.stop();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.windowTimerPanel.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.reset) {
				if (this.windowTimerPanel.isRunning()) {
					this.windowTimerPanel.stop();
				}
				this.resetEntireSystem();
				this.addClockFaceAndButtons(TimerType.STOPWATCH);
			}
		} else if (this.currentType == TimerType.COUNTDOWN) {
			if (e.getSource() == this.windowTimerPanel || e.getSource() == this.swingTimer) {
				if (e.getSource() == this.windowTimerPanel) {
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
				this.windowTimerPanel.start();
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
				if (this.windowTimerPanel.isRunning()) {
					this.windowTimerPanel.stop();
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
				this.applyCountdownSetupWindowInput();
			} else if (e.getSource() == this.pause) {
				this.windowTimerPanel.stop();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.windowTimerPanel.start();
				this.buttonPanel.removeAll();
				this.buttonPanel.add(this.back);
				this.buttonPanel.add(this.reset);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			}
		} else if (this.currentType == TimerType.INTERVAL) {
			if (e.getSource() == this.setup) {
				this.callIntervalTrackerWindow();
			} else if (e.getSource() == this.addInterval) {
				this.intervalCount++;
				this.currentInterval++;
				if (!this.windowTimerPanel.isRunning()) {
					this.buttonPanel.add(this.start);
					this.window.repaint();
					this.window.revalidate();
				}
				this.buildIntervalTrackingUIArray();
				if (!this.windowTimerPanel.isRunning()) {
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
				this.intervalTrackerColorIndicatorUpdate(); // also calls repaint/revalidate
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
				this.intervalTracker.setSize(256, 136 + 7 - 55);
				Point intervalTrackerPoint = this.intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				this.intervalTracker.setLocation(x, y + (55 * this.intervalCount));
				this.savedIntervals = null;
				this.savedIntervals = new int[1][2][6];
				this.savedIntervalCount = 0;
				this.intervalCount = 0;
				this.currentInterval = -1;
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
				Point intervalTrackerPoint = this.intervalTracker.getLocation();
				int x = (int) intervalTrackerPoint.getX();
				int y = (int) intervalTrackerPoint.getY();
				this.intervalTracker.setLocation(x, y + 55);
				this.intervalTracker.repaint();
				this.intervalTracker.revalidate();
				this.intervalTrackerColorIndicatorUpdate(); // also calls repaint/revalidate
				this.setupWindow.dispose();
			} else if (e.getSource() == this.confirm) {
				this.saveIntervalSetupWindowInput(-1, false);
				this.updateIntervalTrackingUI();
				if (!this.windowTimerPanel.isRunning()) {
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
				this.windowTimerPanel.start();
				if (this.swingTimer.getDelay() != 100) {
					this.swingTimer.setDelay(100);
				}
			} else if (e.getSource() == this.pause) {
				this.windowTimerPanel.stop();
				this.buttonPanel.remove(this.pause);
				this.buttonPanel.add(this.resume);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.resume) {
				this.windowTimerPanel.start();
				this.buttonPanel.remove(this.resume);
				this.buttonPanel.add(this.pause);
				this.window.repaint();
				this.window.revalidate();
			} else if (e.getSource() == this.reset) {
				this.windowTimerPanel.stop();
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
				this.intervalClockFaceUpdate();
				this.intervalTrackerColorIndicatorUpdate();
			} else if (e.getSource() == this.windowTimerPanel || e.getSource() == this.swingTimer) {
				if (e.getSource() == this.windowTimerPanel) {
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
						if (this.clockFaceInterval != Integer.parseInt(command) || !this.windowTimerPanel.isRunning()) {
							this.callIntervalPromptWindow(command);
						}
					}
				}
			}
		}
	}
}
