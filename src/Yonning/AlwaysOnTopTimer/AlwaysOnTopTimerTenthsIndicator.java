package Yonning.AlwaysOnTopTimer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AlwaysOnTopTimerTenthsIndicator extends JPanel {
	
	AlwaysOnTopTimer AoTT;
	
	public AlwaysOnTopTimerTenthsIndicator(AlwaysOnTopTimer AoTT) {
		super();
		this.AoTT = AoTT;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (this.AoTT.tenthSeconds == 0) return;
		
		int x = this.AoTT.westBorder.getWidth() + 3;
		int y = 1;
		int height = this.AoTT.northBorder.getHeight() - 1;
		
		int barWidth = ((this.AoTT.tenthsIndicator.getWidth()) - (this.AoTT.westBorder.getWidth()) - (this.AoTT.eastBorder.getWidth())-6);
		int modNine = barWidth % 9;
		int correction = modNine;
		if (correction > this.AoTT.tenthSeconds) correction = this.AoTT.tenthSeconds;
		
		int width = (barWidth/9)*this.AoTT.tenthSeconds + correction;
		
		g.setColor(new Color(112, 146, 190));
//		g.fillRect(this.AoTT.westBorder.getWidth() + 3, 1, (((this.AoTT.tenthsIndicator.getWidth()) - (this.AoTT.westBorder.getWidth()) - (this.AoTT.eastBorder.getWidth())-6)/9)*this.AoTT.tenthSeconds, this.AoTT.northBorder.getHeight() - 1);
		g.fillRect(x, y, width, height);
	}
	
}

