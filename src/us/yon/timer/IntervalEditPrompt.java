package us.yon.timer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


public class IntervalEditPrompt extends JDialog implements ActionListener {
	
	/**
	 * Declared for the sole purpose of preventing the JVM from calculating a value at startup.
	 */
	private static final long serialVersionUID = 1L;
	
	public static abstract class PromptListener implements ActionListener {
		
		public abstract void edit();
		
		public abstract void skipto();
		
		public abstract void delete();
		
		@Override
		public final void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "edit":
					edit();
					break;
				case "skipto":
					skipto();
					break;
				case "delete":
					delete();
					break;
				default:
					throw new IllegalArgumentException("Unsupported Action Command.");
			}
		}
	}
	
	private PromptListener listener;
	
	private JButton edit = new JButton("Edit");
	
	private JButton skipto = new JButton("Skip To");
	
	private JButton delete = new JButton("Delete");
	
	public IntervalEditPrompt(Frame owner, String title, PromptListener listener, boolean enableEdit) {
		super(owner, title);
		this.listener = listener;
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(241 + 14, 89 + 7);
		setLocationRelativeTo(owner);
		setAlwaysOnTop(true);
		setModal(true);
		
		edit.setEnabled(enableEdit);
		edit.addActionListener(this);
		skipto.addActionListener(this);
		delete.addActionListener(this);
		
		JPanel buttons = new JPanel();
		buttons.add(edit);
		buttons.add(skipto);
		buttons.add(delete);
		
		add(new JPanel(), BorderLayout.NORTH);
		add(new JPanel(), BorderLayout.SOUTH);
		add(new JPanel(), BorderLayout.EAST);
		add(new JPanel(), BorderLayout.WEST);
		add(buttons, BorderLayout.CENTER);
		
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		dispose(); //The only things we subscribe to are listed below; and each one will close this dialog.
		
		if (e.getSource().equals(edit)) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "edit"));
		}
		
		if (e.getSource().equals(skipto)) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "skipto"));
		}
		
		if (e.getSource().equals(delete)) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "delete"));
		}
	}
	
}
