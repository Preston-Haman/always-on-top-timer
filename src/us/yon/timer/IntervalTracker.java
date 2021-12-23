package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import us.yon.timer.IntervalEditPrompt.PromptListener;
import us.yon.timer.KeypadInputDialog.KeypadInputListener;


public class IntervalTracker extends JFrame implements ActionListener {
	
	/**
	 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
	 */
	private static final long serialVersionUID = 1L;
	
	public static abstract class IntervalInputListener implements ActionListener {
		
		public abstract void intervalAdded();
		
		public abstract void intervalRemoved();
		
		public abstract void activeIntervalChangedByUser();
		
		@Override
		public final void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "add":
					intervalAdded();
					break;
				case "remove":
					intervalRemoved();
					break;
				case "skipto":
					activeIntervalChangedByUser();
					break;
				default:
					throw new IllegalArgumentException("Unsupported Action Command.");
			}
		}
	}
	
	private IntervalInputListener listener;
	
	private ArrayList<ClockFaceButton> intervals = new ArrayList<>();
	
	private int activeInterval = 0;
	
	private boolean ticking = false;
	
	private JPanel intervalPanel = new JPanel();
	
	private JPanel buttonPanel = new JPanel();
	
	private JButton addInterval = new JButton("Add Interval");
	
	private JButton clearIntervals = new JButton("Clear All");
	
	public IntervalTracker(Frame owner, IntervalInputListener listener) {
		super("Intervals:");
		this.listener = listener;
		
		Point windowPoint = owner.getLocation();
		int x = (int) windowPoint.getX();
		int y = (int) windowPoint.getY();
		setSize(256, 136 + 7 - 55);
		setLocation((int) (x + owner.getWidth() - 14), y - (getHeight() - owner.getHeight()));
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setAlwaysOnTop(true);
		
		intervalPanel.setLayout(new BoxLayout(intervalPanel, BoxLayout.Y_AXIS));
		
		addInterval.addActionListener(this);
		clearIntervals.addActionListener(this);
		
		buttonPanel.add(addInterval);
		
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.EAST);
		add(new JPanel(), BorderLayout.WEST);
		add(buttonPanel, BorderLayout.SOUTH);
		add(intervalPanel, BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	public int getIntervalCount() {
		return intervals.size();
	}
	
	public ClockFace getActiveInterval() {
		if (intervals.size() > 0) {
			return intervals.get(activeInterval);
		} else {
			return null;
		}
	}
	
	public ClockFace advanceToNextInterval() {
		activeInterval++;
		if (activeInterval >= intervals.size()) {
			activeInterval = 0;
		}
		intervalTrackerColorIndicatorUpdate();
		return getActiveInterval();
	}

	public ClockFace resetActiveInterval() {
		activeInterval = 0;
		intervalTrackerColorIndicatorUpdate();
		return getActiveInterval();
	}
	
	private void callSetupWindow(final ClockFace clockface, Frame owner, boolean newInterval) {
		int[] oldTime = clockface.getTime();
		
		final int oldSeconds = oldTime[0], oldDecaSeconds = oldTime[1],
				  oldMinutes = oldTime[2], oldDecaMinutes = oldTime[3],
				  oldHours = oldTime[4], oldDecaHours = oldTime[5];
		
		KeypadInputListener keypadListener = new KeypadInputListener() {
			@Override
			public void previewInput(int... currentInput) {
				clockface.displayTimeOnClockFace(currentInput);
			}
			
			@Override
			public void confirmInput(int... currentInput) {
				clockface.displayTimeOnClockFace(currentInput);
				intervalTrackerColorIndicatorUpdate();
				
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "add"));
			}
			
			@Override
			public void cancelInput() {
				clockface.setTime(oldSeconds, oldDecaSeconds, oldMinutes, oldDecaMinutes, oldHours, oldDecaHours);
				if (newInterval) {
					deleteIntervalArrayIndex(intervals.size() - 1);
				}
			}
		};
		
		new KeypadInputDialog(owner, "Set Countdown:", keypadListener);
	}
	
	private void addInterval() {
		ClockFaceButton clockFaceButton = new ClockFaceButton();
		clockFaceButton.addActionListener(this);
		clockFaceButton.setActionCommand("" + intervals.size());
		intervals.add(clockFaceButton);
		intervalPanel.add(clockFaceButton);
		
		if (intervals.size() == 1) {
			buttonPanel.add(clearIntervals);
		}
		
		Point windowPoint = getLocation();
		setLocation((int) windowPoint.getX(), (int) windowPoint.getY() - 55);
		Dimension windowSize = getSize();
		int windowHeight = windowSize.height;
		int windowWidth = windowSize.width;
		setSize(windowWidth, windowHeight + 55);
		intervalTrackerColorIndicatorUpdate();
		
		callSetupWindow(clockFaceButton, this, true);
	}
	
	private void deleteIntervalArrayIndex(int intervalIndex) {
		if (intervalIndex == activeInterval) return;
		
		intervalPanel.remove(intervals.remove(intervalIndex));
		
		if (activeInterval > intervalIndex) {
			activeInterval--;
		}
		
		setSize(getWidth(), getHeight() - 55);
		
		if (intervals.size() == 0) {
			buttonPanel.remove(clearIntervals);
		}
		Point intervalTrackerPoint = getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		setLocation(x, y + 55);
		
		intervalTrackerColorIndicatorUpdate();
		listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "remove"));
	}
	
	private void clearIntervals() {
		intervalPanel.removeAll();
		buttonPanel.remove(clearIntervals);
		
		setSize(256, 136 + 7 - 55);
		Point intervalTrackerPoint = getLocation();
		int x = (int) intervalTrackerPoint.getX();
		int y = (int) intervalTrackerPoint.getY();
		setLocation(x, y + (55 * intervals.size()));
		
		activeInterval = 0;
		intervals.clear();
		listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "remove"));
	}
	
	private void intervalTrackerColorIndicatorUpdate() {
		for (int i = 0; i < intervals.size(); i++) {
			intervals.get(i).setBackground(AlwaysOnTopTimer.defaultPanelColor);
			intervals.get(i).setBorderPainted(false);
		}
		
		if (activeInterval >= 0 && activeInterval < intervals.size()) {
			intervals.get(activeInterval).setBackground(AlwaysOnTopTimer.activeIntervalColor);
			intervals.get(activeInterval).setBorderPainted(true);
		}
		repaint();
		revalidate();
	}
	
	private void promptUserDrivenChange(final int intervalIndex) {
		final ClockFaceButton interval = intervals.get(intervalIndex);
		final IntervalTracker thisTracker = this;
		
		PromptListener promptListener = new PromptListener() {
			@Override
			public void edit() {
				callSetupWindow(interval, thisTracker, false);
			}
			
			@Override
			public void skipto() {
				activeInterval = intervalIndex;
				intervalTrackerColorIndicatorUpdate();
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "skipto"));
			}
			
			@Override
			public void delete() {
				deleteIntervalArrayIndex(intervalIndex);
			}
		};
		
		new IntervalEditPrompt(this, "Interval " + (intervalIndex + 1), promptListener, !ticking);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(addInterval)) {
			addInterval();
		} else if (e.getSource().equals(clearIntervals)) {
			clearIntervals();
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "remove"));
		} else if ("start".equals(e.getActionCommand())) { //Method can return null
			ticking = true;
			buttonPanel.remove(clearIntervals);
		} else if ("stop".equals(e.getActionCommand())) { //Method can return null
			ticking = false;
			if (intervals.size() > 0) {
				buttonPanel.add(clearIntervals);
			}
		} else for (int i = 0; i < intervals.size(); i++) {
			if (e.getSource().equals(intervals.get(i))) {
				if (!ticking || activeInterval != i) promptUserDrivenChange(i);
			}
		}
	}
	
}
