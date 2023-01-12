import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DisplayPanel extends JPanel implements ActionListener{
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

    public void startDisplay() {
	Timer t = new Timer(1000/60, this);
	t.start();
    }

    public void actionPerformed(ActionEvent e) {
	repaint();
    }
}
