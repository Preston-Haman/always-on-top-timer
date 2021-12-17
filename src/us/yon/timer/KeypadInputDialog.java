package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


public class KeypadInputDialog extends JDialog implements ActionListener {
	
	/**
	 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
	 */
	private static final long serialVersionUID = 1L;
	
	public static abstract class AbstractKeypadInputListener implements ActionListener {
		
		public abstract void cancelInput();
		
		public abstract void confirmInput(int...currentInput);
		
		public abstract void previewInput(int...currentInput);
		
		public final void setInputDialog(KeypadInputDialog dialog) {
			this.dialog = dialog;
		}
		
		private KeypadInputDialog dialog;
		
		@Override
		public final void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "cancel":
					cancelInput();
					break;
				case "confirm":
					confirmInput(dialog.input.stream().mapToInt(Integer::intValue).toArray());
					break;
				case "input":
					previewInput(dialog.input.stream().mapToInt(Integer::intValue).toArray());
					break;
				default:
					throw new IllegalArgumentException("Unsupported Action Command.");
			}
		}
	}
	
	private JButton cancel = new JButton("Cancel");
	
	private JButton confirm = new JButton("Confirm");
	
	private JButton[] numbers = new JButton[10];
	
	private AbstractKeypadInputListener listener;
	
	private ArrayList<Integer> input = new ArrayList<>(8);
	
	public KeypadInputDialog(final Frame owner, final String title, final AbstractKeypadInputListener listener) {
		super(owner, title);
		setSize(275 + 14, 198 + 7);
		setMaximumSize(getSize());
		setAlwaysOnTop(true);
		setModal(true);
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "cancel"));
				dispose();
			}
		});
		
		this.listener = listener;
		this.listener.setInputDialog(this);
		//Pre-fire for zero input event if caller wants one
		listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "input"));
		
		cancel.addActionListener(this);
		confirm.addActionListener(this);
		
		JPanel setupWindowKeypad = new JPanel();
		setupWindowKeypad.setLayout(new GridLayout(0, 3, 3, 3));
		Icon[] graphics = ClockFace.getClockGraphics();
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = new JButton();
			numbers[i].setIcon(graphics[i]);
			numbers[i].addActionListener(this);
			numbers[i].setFocusable(false);
			if (i > 0) setupWindowKeypad.add(numbers[i]);
		}
		setupWindowKeypad.add(cancel);
		setupWindowKeypad.add(numbers[0]);
		setupWindowKeypad.add(confirm);
		add(setupWindowKeypad, BorderLayout.CENTER);
		
		//Pads the outer edge of the window... somewhat silly method to do so
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);
		add(new JPanel(), BorderLayout.WEST);
		
		//TODO: Check for room on screen!
		Point windowPoint = owner.getLocation();
		int x = (int) windowPoint.getX();
		int y = (int) windowPoint.getY();
		setLocation((int) (x + owner.getWidth() - 14), y - (getHeight() - owner.getHeight()));
		setVisible(true);
	}
	
	public List<Integer> getInput() {
		return input;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(cancel)) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "cancel"));
			dispose();
		}
		if (e.getSource().equals(confirm)) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "confirm"));
			dispose();
		}
		for (int i = 0; i < numbers.length; i++) {
			if (e.getSource().equals(numbers[i])) {
				input.add(i);
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "input"));
			}
		}
	}
}
