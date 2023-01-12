import java.awt.Graphics;
import java.awt.Dimension;
import javax.swing.JPanel;

public class DisplayPanel extends JPanel {
    private MachineScreen screen;
    
    public DisplayPanel(MachineScreen screen) {
	this.screen = screen;
    }

    @Override
    public Dimension getPreferredSize() {
	return new Dimension(screen.getWidth(), screen.getHeight());
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	screen.draw(g);
    }
}
