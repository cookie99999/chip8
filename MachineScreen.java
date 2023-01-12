import java.awt.Graphics;
import java.awt.Color;

public class MachineScreen {
    private final int xmax = 64;
    private final int ymax = 32;
    private int scale;
    private int[] pixels = new int[xmax * ymax];

    public MachineScreen(int scale) {
	for (int i : pixels) {
	    i = 0;
	}
	this.scale = scale;
    }

    public int getWidth() {
	return this.xmax * scale;
    }

    public int getHeight() {
	return this.ymax * scale;
    }

    public void setPixel(int x, int y, int color) {
	//todo: bound checking

	this.pixels[y * xmax + x] = color;
    }

    public int getPixel(int x, int y) {
	//todo: bound checking

	return this.pixels[y * xmax + x];
    }

    public void drawSpriteLine(int x, int y, byte line) {
	for (int i = 7; i >= 0; i--) {
	    setPixel(x + (7 - i), y, ((line >>> i) & 1) == 1 ? 1 : 0);
	}
    }

    public void draw(Graphics g) {
	for (int x = 0; x < this.xmax; x++) {
	    for (int y = 0; y < this.ymax; y++) {
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
		g.fillRect(x * this.scale, y * this.scale, scale, scale);
	    }
	}
    }
}
