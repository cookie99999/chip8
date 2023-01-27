import java.awt.event.*;

public class CPU implements ActionListener {
    private byte[] memory = new byte[4096];
    
    private short pc;
    private short ir;
    private byte delay_timer;
    private byte sound_timer;
    private byte[] registers = new byte[16];
    private short[] stack = new short[16];
    private int sp;

    private boolean isDebug = false;
    private final int MEM_MAX = 4096;

    private MachineScreen screen;
    private KeyPad keypad;

    public enum CompatLevel {
	CHIP_8, CHIP_48, SCHIP_1_0, SCHIP_1_1, XOCHIP
    }
    private CompatLevel compat;
    
    public CPU() {
	this.pc = 0x200;
	this.ir = 0;
	this.delay_timer = 0;
	this.sound_timer = 0;
	this.sp = 0;
	for (byte b : registers)
	    b = 0;
	for (byte b : memory)
	    b = 0;
	for (short s : stack)
	    s = 0;
	this.screen = null;
    }

    public void reset() {
	this.pc = 0x200;
	this.ir = 0;
	this.delay_timer = 0;
	this.sound_timer = 0;
	this.sp = 0;
	for (byte b : registers)
	    b = 0;
	//for (byte b : memory)
	//b = 0;
	for (short s : stack)
	    s = 0;
	screen.clear();
    }
    
    public void actionPerformed(ActionEvent e) {
	delay_timer -= (delay_timer > 0) ? 1 : 0;
	sound_timer -= (sound_timer > 0) ? 1 : 0;
    }

    public void setScreen(MachineScreen s) {
	this.screen = s;
    }

    public void setKeyPad(KeyPad p) {
	this.keypad = p;
    }

    public void setCompatLevel(CompatLevel cl) {
	this.compat = cl;
    }

    private void debugPrint(short opcode) {
	System.out.println(String.format("%04x\npc: %04x ir: %04x delay: %02x sound: %02x",
					 opcode, pc, ir, delay_timer, sound_timer));
	for (byte i = 0; i < 16; i++)
	    System.out.print(String.format("V%x: %02x ", i, registers[i]));
	System.out.println();
    }
    
    public void writeMem(short address, byte data) {
	if (address >= MEM_MAX || address < 0) {
	    System.out.println("<ERROR> out of bounds write at " + address);
	    System.exit(-1);
	}
	memory[address] = data;
    }

    public byte readMem(short address) {
	if (address >= MEM_MAX || address < 0) {
	    System.out.println("<ERROR> out of bounds read at " + address);
	    System.exit(-1);
	}
	return (byte)(memory[address] & 0xff);
    }
    
    public void step() {
	short opcode = (short)((readMem(pc) << 8) | (readMem((short)(pc + 1)) & 0xff));
	byte op = (byte)((opcode & 0xf000) >>> 12);
	byte x = (byte)((opcode & 0x0f00) >>> 8);
	byte y = (byte)((opcode & 0x00f0) >>> 4);
	byte n = (byte)(opcode & 0x000f);
	byte nn = (byte)(opcode & 0x00ff);
	short nnn = (short)(opcode & 0x0fff);

	if (isDebug)
	    debugPrint(opcode);

	pc += 2; //increment before execution so jmp won't be messed up
	
	switch (op) {
	case 0x0:
	    if (opcode == 0x00e0) {
		screen.clear();
	    } else if (opcode == 0x00ee) {
		if (--sp < 0)
		    sp = 0;
		pc = stack[sp];
	    }
	    break;
	case 0x1:
	    if ((pc - 2) == nnn && isDebug) {
		System.out.println("<DEBUG> infinite loop entered, quitting...");
		System.exit(0);
	    }
	    pc = nnn;
	    break;
	case 0x2:
	    stack[sp] = pc;
	    if (++sp > 15)
		sp = 15;
	    pc = nnn;
	    break;
	case 0x3:
	    if (registers[x] == nn)
		pc += 2;
	    break;
	case 0x4:
	    if (registers[x] != nn)
		pc += 2;
	    break;
	case 0x5:
	    if (registers[x] == registers[y])
		pc += 2;
	    break;
	case 0x6:
	    registers[x] = nn;
	    break;
	case 0x7:
	    registers[x] += nn;
	    break;
	case 0x8:
	    switch (n) {
	    case 0x0:
		registers[x] = registers[y];
		break;
	    case 0x1:
		registers[x] |= registers[y];
		if (compat == CompatLevel.CHIP_8)
		    registers[0xf] = 0;
		break;
	    case 0x2:
		registers[x] &= registers[y];
		if (compat == CompatLevel.CHIP_8)
		    registers[0xf] = 0;
		break;
	    case 0x3:
		registers[x] ^= registers[y];
		if (compat == CompatLevel.CHIP_8)
		    registers[0xf] = 0;
		break;
	    case 0x4:
		byte tmp = registers[x];
		registers[x] += registers[y];
		if (Integer.compareUnsigned((int)((tmp & 0xff) + (registers[y] & 0xff)), 255) > 0)
		    registers[0xf] = 1;
		else
		    registers[0xf] = 0;
		break;
	    case 0x5:
		tmp = registers[x];
		registers[x] = (byte)(registers[x] - registers[y]);
		if (Integer.compareUnsigned(tmp, registers[y]) > 0)
		    registers[0xf] = 1;
		else
		    registers[0xf] = 0;
		break;
	    case 0x6:
	        if (compat != CompatLevel.CHIP_48 && compat != CompatLevel.SCHIP_1_0 &&
		    compat != CompatLevel.SCHIP_1_1)
		    registers[x] = registers[y];
		tmp = (byte)(registers[x] & 1);
		registers[x] = (byte)((registers[x] & 0xff) >>> 1);
		registers[0xf] = tmp;
		break;
	    case 0x7:
		tmp = registers[x];
		registers[x] = (byte)(registers[y] - registers[x]);
		if (Integer.compareUnsigned(registers[y], tmp) > 0)
		    registers[0xf] = 1;
		else
		    registers[0xf] = 0;
		break;
	    case 0xe:
		if (compat != CompatLevel.CHIP_48 && compat != CompatLevel.SCHIP_1_0 &&
		    compat != CompatLevel.SCHIP_1_1) 
		    registers[x] = registers[y];
		tmp = (byte)((registers[x] & 0x80) >>> 7);
		registers[x] <<= 1;
		registers[0xf] = tmp;
		break;
	    default:
		System.out.println(String.format("<ERROR> unimplented opcode %04x", opcode));
		System.exit(-1);
		break;
	    }
	    break;
	case 0x9:
	    if (registers[x] != registers[y])
		pc += 2;
	    break;
	case 0xa:
	    ir = nnn;
	    break;
	case 0xb:
	    if (compat == CompatLevel.CHIP_8)
		pc = (short)(nnn + (registers[0] & 0xff)); //todo: check bounds
	    else
		pc = (short)(nnn + (registers[x] & 0xff));
	    break;
	case 0xc:
	    registers[x] = (byte)((byte)(Math.random() * 256) & nn);
	    break;
	case 0xd: //correct clipping off behavior as of 6125792
	    int xcoord = registers[x];
	    int ycoord = registers[y];
	    boolean collided = false;
	    
	    for (int i = 0; i < n; i++) {
		byte line = readMem((short)(ir + i));
		if (screen.drawSpriteLine(xcoord, ycoord + i, line))
		    collided = true;
	    }
	    registers[0xf] = (byte)(collided ? 1 : 0);
	    break;
	case 0xe:
	    switch (nn) {
	    case (byte)0x9e:
		pc += keypad.getKey(registers[x]) ? 2 : 0;
		break;
	    case (byte)0xa1:
		pc += keypad.getKey(registers[x]) ? 0 : 2;
		break;
	    default:
		break;
	    }
	    break;
	case 0xf:
	    switch (nn) {
	    case 0x07:
		registers[x] = delay_timer;
		break;
	    case 0x0a:
		pc -= 2;
		for (int i = 0; i < 16; i++) {
		    if(keypad.getKey(i)) {
			registers[x] = (byte)i;
			pc += 2;
			break;
		    }
		}
		break;
	    case 0x15:
		delay_timer = registers[x];
		break;
	    case 0x18:
		sound_timer = registers[x];
		break;
	    case 0x1e:
		//todo: configurable old behavior
		ir += (short)(registers[x] & 0xff);
		if (ir > 0x0fff) {
		    registers[0xf] = 1;
		    ir = (short)(0 + (ir - 0x0fff));
		}
		break;
	    case 0x29:
		ir = (short)(0x0050 + ((registers[x] & 0x0f) * 5)); //5 bytes per char
		break;
	    case 0x33: //bcd
		int first, second, third;
		int tens = Integer.divideUnsigned(registers[x] & 0xff, 10);
		int hundreds = Integer.divideUnsigned(registers[x] & 0xff, 100);
		third = Integer.remainderUnsigned(registers[x] & 0xff, 10);
		second = Integer.remainderUnsigned(tens, 10);
		first = Integer.remainderUnsigned(hundreds, 10);
		writeMem(ir, (byte)first);
		writeMem((short)(ir + 1), (byte)second);
		writeMem((short)(ir + 2), (byte)third);
		break;
	    case 0x55:
		//todo: configurable old behavior
		for (int i = 0; i <= x; i++) {
		    writeMem((short)(ir + i), registers[i]);
		}
		if (compat == CompatLevel.CHIP_8)
		    ir += x + 1;
		break;
	    case 0x65:
		//todo: configurable old behavior
		for (int i = 0; i <= x; i++) {
		    registers[i] = readMem((short)(ir + i));
		}
		if (compat == CompatLevel.CHIP_8)
		    ir += x + 1;
		break;
	    default:
		System.out.println("<ERROR> unimplemented opcode " + String.format("%04x", opcode));
	    System.exit(-1);
	    break;
	    }
	    break;
	default:
	    System.out.println("<ERROR> unimplemented opcode " + String.format("%04x", opcode));
	    System.exit(-1);
	    break;
	}
    }
}
