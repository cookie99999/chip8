import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Chip8 {
    private static void createAndShowUI(MachineScreen screen) {
	JFrame frame = new JFrame("Chip8");
	DisplayPanel dp = new DisplayPanel(screen);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setLayout(new BorderLayout());
	frame.add(dp);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	dp.startDisplay();
    }
    
    public static byte[] loadBin(String path) {
	Path p = Paths.get(path);
	byte[] data = null;
	try {
	    data = Files.readAllBytes(p);
	} catch (java.io.IOException e) {
	    System.out.println("<ERROR> IOException in loadBin");
	    System.out.println(e);
	    System.exit(-1);
	}
	return data;
    }
    
    public static void main(String[] args) {
	byte[] bin = null;
	try {
	    bin = loadBin(args[0]); //todo: check it's a valid path
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Usage: java Chip8 <bin file path>");
	    System.out.println(e);
	    System.exit(0);
	}

	byte[] font = null;
	try {
	    font = loadBin("font.bin"); //todo: configurable font
	} catch (Exception e) {
	    System.out.println("<ERROR> failed loading font");
	    System.out.println(e);
	    System.exit(-1);
	}
	
	CPU cpu = new CPU();
	MachineScreen screen = new MachineScreen(4); //todo: user settable scale
	cpu.setScreen(screen);
	
	int binLen = bin.length;
	if (binLen > 4096 - 0x200) {
	    System.out.println("<ERROR> input file too large");
	    System.exit(-1);
	}

	int fontLen = font.length;
	if (fontLen != 5 * 16) {
	    System.out.println("<ERROR> font file size incorrect: " + fontLen);
	    System.exit(-1);
	}

	EventQueue.invokeLater(new Runnable()
	    {
		public void run() {
		    createAndShowUI(screen);
		}
	    });
	
	for (short i = 0; i < binLen; i++) {
	    cpu.writeMem((short)(0x200 + i), bin[i]);
	}

	for (short i = 0; i < fontLen; i++) {
	    cpu.writeMem((short)(0x50 + i), font[i]);
	}
	
	while (true) {
	    cpu.step();
	}
    }
}
