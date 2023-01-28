import java.awt.Graphics;
import java.awt.Color;

public class MachineScreen {
    private int xmax = 64;
    private int ymax = 32;
    private int scale;
    private int[] pixels = null;

    public MachineScreen(int xmax, int ymax, int scale) {
	this.scale = scale;
	this.xmax = xmax;
	this.ymax = ymax;
	this.pixels = new int[xmax * ymax];
	for (int i : pixels)
	    i = 0;
    }

    public int getWidth() {
	return xmax * scale;
    }

    public int getHeight() {
	return ymax * scale;
    }

    public void setPixel(int x, int y, int color) {
	pixels[(y % ymax) * xmax + (x % xmax)] = color;
    }

    public int getPixel(int x, int y) {
	return pixels[(y % ymax) * xmax + (x % xmax)];
    }

    public void clear() {
	for (int x = 0; x < xmax; x++) {
	    for (int y = 0; y < ymax; y++) {
		setPixel(x, y, 0);
	    }
	}
    }

    public void draw(Graphics g) {
	for (int x = 0; x < xmax; x++) {
	    for (int y = 0; y < ymax; y++) {
		Color c;
		switch (getPixel(x, y)) {
		case 0:
		    c = Color.BLACK;
		    break;
		case 1:
		    c = Color.WHITE;
		    break;
		default:
		    c = Color.RED;
		    System.out.println("<ERROR> unsupported color in screen buf");
		    break;
		}
		g.setColor(c);
		g.fillRect(x * scale, y * scale, scale, scale);
	    }
	}
    }
}
