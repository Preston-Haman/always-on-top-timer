package Yonning.AlwaysOnTopTimer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AlwaysOnTopTimerReverseTenthsIndicator extends JPanel {
	
	AlwaysOnTopTimer AoTT;

	public AlwaysOnTopTimerReverseTenthsIndicator(AlwaysOnTopTimer AoTT) {
		super();
		this.AoTT = AoTT;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int barWidth = (this.AoTT.tenthsReverseIndicator.getWidth() - this.AoTT.westBorder.getWidth() - this.AoTT.eastBorder.getWidth() - 6);
		int modNine = barWidth % 9;
		int correction = modNine - this.AoTT.tenthSeconds;
		if (correction < 0) correction = 0;
		
		int x = (this.AoTT.westBorder.getWidth() + 3);

		
		if (this.AoTT.tenthSeconds == 8) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + (barWidth/9) + correction);
		} else if (this.AoTT.tenthSeconds == 7) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*2) + correction);
		} else if (this.AoTT.tenthSeconds == 6) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*3) + correction);
		} else if (this.AoTT.tenthSeconds == 5) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*4) + correction);
		} else if (this.AoTT.tenthSeconds == 4) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*5) + correction);
		} else if (this.AoTT.tenthSeconds == 3) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*6) + correction);
		} else if (this.AoTT.tenthSeconds == 2) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*7) + correction);
		} else if (this.AoTT.tenthSeconds == 1) {
			x =	((this.AoTT.westBorder.getWidth() + 3) + ((barWidth/9)*8) + correction);
		} else if (this.AoTT.tenthSeconds == 0) {
			return;
		}
		
		int y =	1;
		int width = (this.AoTT.tenthsReverseIndicator.getWidth() - this.AoTT.eastBorder.getWidth() - 3 - x);
		int height = this.AoTT.northBorder.getHeight() - 1;
		
		g.setColor(new Color(112, 146, 190));
//		g.fillRect(((this.AoTT.westBorder.getWidth() + 3) + (((this.AoTT.tenthsReverseIndicator.getWidth() - this.AoTT.westBorder.getWidth() - this.AoTT.eastBorder.getWidth() - 6)/9)*(9 - this.AoTT.tenthSeconds))), 1, (((this.AoTT.tenthsReverseIndicator.getWidth() - this.AoTT.westBorder.getWidth() - this.AoTT.eastBorder.getWidth() - 6)/9)*this.AoTT.tenthSeconds), this.AoTT.northBorder.getHeight() - 1);
		g.fillRect(x, y, width, height);
	}
}
