package Yonning.AlwaysOnTopTimer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.TimerTask;

import javax.swing.Timer;

@SuppressWarnings("serial")
public class AlwaysOnTopTimerClock extends Timer {

	private int delay;
	private java.util.Timer timer;
	private boolean running = false;
	private AlwaysOnTopTimerClock source;
	
	public AlwaysOnTopTimerClock(int delay, ActionListener listener) {
		super(delay, listener);
		this.delay = delay;
		this.source = this;
		this.timer = new java.util.Timer();
	}

	/* (non-Javadoc)
	 * @see javax.swing.Timer#fireActionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	protected void fireActionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		super.fireActionPerformed(e);
	}

	/* (non-Javadoc)
	 * @see javax.swing.Timer#start()
	 */
	@Override
	public void start() {
		this.running = true;
		this.timer = new java.util.Timer();
		this.timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				fireActionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
				
			}
			
		}, new Date(), this.delay);
	}

	/* (non-Javadoc)
	 * @see javax.swing.Timer#stop()
	 */
	@Override
	public void stop() {
		this.timer.cancel();
		this.running = false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Timer#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return running;
	}



}
