import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.event.*;

public class Chip8 {
    private static CPU cpu;
    private volatile static ProgramState state;
    private static final JFileChooser fc = new JFileChooser();
    private volatile static int cycles = 700;

    public enum ProgramState {
	RUNNING, RESET, HALT
    }
    
    private static void createAndShowUI(MachineScreen screen, KeyPad kp) {
	JFrame frame = new JFrame("Chip8");
	DisplayPanel dp = new DisplayPanel(screen);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setLayout(new BorderLayout());
	frame.add(dp);
	frame.addKeyListener(kp);

	JMenuBar mb = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	fileMenu.setMnemonic(KeyEvent.VK_F);
	fileMenu.getAccessibleContext().setAccessibleDescription("File menu");
	mb.add(fileMenu);

	JMenuItem loadItem = new JMenuItem("Load...", KeyEvent.VK_L);
	loadItem.getAccessibleContext().setAccessibleDescription("Load a program file");
	fileMenu.add(loadItem);
	
	JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
	exitItem.getAccessibleContext().setAccessibleDescription("Exit the program");
	fileMenu.add(exitItem);

	JMenu machineMenu = new JMenu("Machine");
	machineMenu.setMnemonic(KeyEvent.VK_M);
	machineMenu.getAccessibleContext().setAccessibleDescription("Machine menu");
	mb.add(machineMenu);

	JMenuItem resetItem = new JMenuItem("Reset", KeyEvent.VK_R);
	resetItem.getAccessibleContext().setAccessibleDescription("Reset the machine");
	machineMenu.add(resetItem);

	JMenuItem prefItem = new JMenuItem("Preferences...", KeyEvent.VK_P);
	prefItem.getAccessibleContext().setAccessibleDescription("Machine preferences");
	machineMenu.add(prefItem);

	GuiListener listen = new GuiListener();
	loadItem.addActionListener(listen);
	exitItem.addActionListener(listen);
	resetItem.addActionListener(listen);
	prefItem.addActionListener(listen);

	fileMenu.addMenuListener(listen);
	machineMenu.addMenuListener(listen);
	
	frame.setJMenuBar(mb);
	frame.pack();
	frame.setLocationRelativeTo(null);
	frame.setVisible(true);
	dp.startDisplay();
    }
    
    public static void loadBin(String path, short start) {
	Path p = Paths.get(path);
	byte[] data = null;
	try {
	    data = Files.readAllBytes(p);
	} catch (java.io.IOException e) {
	    System.out.println("<ERROR> IOException in loadBin");
	    System.out.println(e);
	    System.exit(-1);
	}

	int len = data.length;
	if (len > 4096 - start) { //todo: show error dialogue instead or something
	    System.out.println("<ERROR> data too large for emulated memory");
	    System.exit(-1);
	}

	for (int i = 0; i < len; i++)
	    cpu.writeMem((short)(start + i), data[i]);
    }
    
    public static void main(String[] args) {
	cpu = new CPU();
	MachineScreen screen = new MachineScreen(64, 32, 4); //todo: user settable scale
	KeyPad kp = new KeyPad();
	cpu.setKeyPad(kp);
	cpu.setScreen(screen);
	cpu.setCompatLevel(CPU.CompatLevel.CHIP_48);

	try {
	    loadBin(args[0], (short)0x200); //todo: check it's a valid path
	} catch (ArrayIndexOutOfBoundsException e) {
	    //can load from gui if no command line arguments present
	}

	try {
	    loadBin("font.bin", (short)0x50); //todo: configurable font
	} catch (Exception e) {
	    System.out.println("<ERROR> failed loading font");
	    System.out.println(e);
	    System.exit(-1);
	}

	EventQueue.invokeLater(new Runnable()
	    {
		public void run() {
		    createAndShowUI(screen, kp);
		}
	    });

	Timer t = new Timer(1000/60, cpu);
	t.start();

	state = ProgramState.RUNNING;

	long start = System.nanoTime();
	for (long i = 0; ; i++) {
	    switch (state) {
	    case RUNNING:
		cpu.step();
		break;
	    case HALT:
		break;
	    case RESET:
		cpu.reset();
		state = ProgramState.RUNNING;
		break;
	    default:
		System.out.println("Unimplemented program state");
		System.exit(-1);
		break;
	    }
	    long end = start + i * 1000000000L / cycles;
	    while (System.nanoTime() < end)
		;
	}
    }

    public static class GuiListener implements ActionListener, MenuListener {
	public void actionPerformed(ActionEvent e) {
	    switch (e.getActionCommand()) {
	    case "Load...":
		int ret = fc.showOpenDialog(null);
		if (ret == JFileChooser.APPROVE_OPTION) {
		    loadBin(fc.getSelectedFile().getAbsolutePath(), (short)0x200);
		    state = ProgramState.RESET;
		}
		break;
	    case "Exit":
		System.exit(0);
		break;
	    case "Reset":
		state = ProgramState.RESET;
		break;
	    case "Preferences...":
		EventQueue.invokeLater(new Runnable() {
			public void run() {
			   OptionsFrame of = new OptionsFrame();
			}
		    });
		break;
	    default:
		System.out.println("Unimplemented menu item");
		break;
	    }
	}

	public void menuSelected(MenuEvent e) {
	    state = ProgramState.HALT;
	}

	public void menuDeselected(MenuEvent e) {
	    state = ProgramState.RUNNING;
	}

	public void menuCanceled(MenuEvent e) {
	    state = ProgramState.RUNNING;
	}
    }

    public static class OptionsFrame extends JFrame implements ActionListener {
	/* needs:
	   compatlevel
	   scale (might need screen refactor)
	   cycles per second
	   sound toggle/volume
	   ok/apply/cancel
	*/
	private JComboBox<String> compatList;
	private JFormattedTextField cyclesField;

	public OptionsFrame() {
	    super("Machine Preferences");
	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	    setResizable(false);
	    this.getContentPane().setLayout(new GridBagLayout());

	    JLabel compatLabel = new JLabel("Compatibility level:");
	    String[] compatStrings = { "CHIP-8", "CHIP-48", "Super-CHIP 1.0", "Super-CHIP 1.1", "XOCHIP" };
	    compatList = new JComboBox<>(compatStrings);
	    compatList.setSelectedIndex(cpu.getCompatLevel().ordinal());
	    compatList.addActionListener(this);

	    JLabel cyclesLabel = new JLabel("Cycles per second:");
	    NumberFormat fmt = NumberFormat.getInstance();
	    NumberFormatter fmtr = new NumberFormatter(fmt);
	    fmtr.setValueClass(Integer.class);
	    fmtr.setMinimum(1);
	    fmtr.setMaximum(Integer.MAX_VALUE);
	    fmtr.setAllowsInvalid(false);
	    cyclesField = new JFormattedTextField(fmtr);
	    cyclesField.setValue(cycles);

	    JButton okbtn = new JButton("Ok");
	    okbtn.setMnemonic(KeyEvent.VK_O);
	    okbtn.setActionCommand("ok");
	    okbtn.addActionListener(this);

	    JButton cancelbtn = new JButton("Cancel");
	    cancelbtn.setMnemonic(KeyEvent.VK_C);
	    cancelbtn.setActionCommand("cancel");
	    cancelbtn.addActionListener(this);

	    GridBagConstraints con = new GridBagConstraints();
	    con.gridx = 0;
	    con.gridy = 0;
	    con.gridwidth = 2;
	    con.anchor = GridBagConstraints.LINE_START;
	    add(compatLabel, con);
	    
	    con.gridx = 4;
	    con.fill = GridBagConstraints.HORIZONTAL;
	    con.anchor = GridBagConstraints.LINE_END;
	    add(compatList, con);
	    
	    con.gridx = 0;
	    con.gridy = 1;
	    con.gridwidth = 1;
	    con.fill = GridBagConstraints.NONE;
	    con.anchor = GridBagConstraints.LINE_START;
	    add(cyclesLabel, con);
	    
	    con.gridx = 4;
	    con.gridwidth = 2;
	    con.fill = GridBagConstraints.HORIZONTAL;
	    con.anchor = GridBagConstraints.LINE_END;
	    add(cyclesField, con);
	    
	    con.gridx = 1;
	    con.gridy = 2;
	    con.gridwidth = 1;
	    con.fill = GridBagConstraints.NONE;
	    con.anchor = GridBagConstraints.CENTER;
	    add(okbtn, con);

	    con.gridx = 2;
	    add(cancelbtn, con);
	    
	    pack();
	    setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
	    switch (e.getActionCommand()) {
	    case "ok":
		cycles = (int)cyclesField.getValue();
		switch ((String)compatList.getSelectedItem()) {
		case "CHIP-8":
		    cpu.setCompatLevel(CPU.CompatLevel.CHIP_8);
		    break;
		case "CHIP-48":
		    cpu.setCompatLevel(CPU.CompatLevel.CHIP_48);
		    break;
		case "Super-CHIP 1.0":
		    cpu.setCompatLevel(CPU.CompatLevel.SCHIP_1_0);
		    break;
		case "Super-CHIP 1.1":
		    cpu.setCompatLevel(CPU.CompatLevel.SCHIP_1_1);
		    break;
		case "XOCHIP":
		    cpu.setCompatLevel(CPU.CompatLevel.XOCHIP);
		    break;
		}
		dispose();
		break;
	    case "cancel":
		dispose();
		break;
	    default:
		break;
	    }
	}
    }
}
