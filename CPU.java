public class CPU {
    private byte[] memory = new byte[4096];
    
    private short pc;
    private short ir;
    private byte delay_timer;
    private byte sound_timer;
    private byte[] registers = new byte[16];
    private short[] stack = new short[16];
    private int sp;

    private boolean isDebug = false;

    private MachineScreen screen;
    
    public CPU() {
	this.pc = 0x200;
	this.ir = 0;
	this.delay_timer = (byte)0xff;
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

    public void setScreen(MachineScreen s) {
	this.screen = s;
    }

    private void debugPrint(short opcode) {
	System.out.println(String.format("%04x\npc: %04x ir: %04x delay: %02x sound: %02x",
					 opcode, pc, ir, delay_timer, sound_timer));
	for (byte i = 0; i < 16; i++)
	    System.out.print(String.format("V%x: %02x ", i, registers[i]));
	System.out.println();
    }
    
    public void writeMem(short address, byte data) {
	//check for errors
	memory[address] = data;
    }
    
    public void step() {
	short opcode = (short)((memory[pc] << 8) | (memory[pc + 1] & 0xff));
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
		pc = stack[sp];
		sp--; //todo: bounds checking
	    }
	    break;
	case 0x1:
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
		break;
	    case 0x2:
		registers[x] &= registers[y];
		break;
	    case 0x3:
		registers[x] ^= registers[y];
		break;
	    case 0x4:
		registers[0xf] = (byte)((int)(registers[x] + registers[y]) > 255 ? 1 : 0);
		registers[x] += registers[y];
		break;
	    case 0x5:
		registers[0xf] = (byte)((registers[x] > registers[y]) ? 1 : 0);
		registers[x] = (byte)(registers[x] - registers[y]);
		break;
	    case 0x6:
		//todo: configurable for old behavior
		registers[0xf] = (byte)(registers[x] & 1);
		registers[x] >>>= 1;
		break;
	    case 0x7:
		registers[0xf] = (byte)((registers[y] > registers[x]) ? 1 : 0);
		registers[x] = (byte)(registers[y] - registers[x]);
		break;
	    case 0xe:
		//todo: configurable for old behavior
		registers[0xf] = (byte)((registers[x] & 0x80) >>> 1);
		registers[x] <<= 1;
		break;
	    default:
		System.out.println(String.format("<ERROR> unimplented opcode %04x", opcode));
		System.exit(-1);
		break;
	    }
	    break;
	case 0xa:
	    ir = nnn;
	    break;
	case 0xd:
	    int xcoord = registers[x] % 64;
	    int ycoord = registers[y] % 32;
	    
	    for (int i = 0; i < n; i++) {
		byte line = memory[ir + i];
		boolean collided = screen.drawSpriteLine(xcoord, ycoord + i, line);
		registers[0xf] = (byte)(collided ? 1 : 0);
	    }
	    break;
	default:
	    System.out.println("<ERROR> unimplemented opcode " + String.format("%04x", opcode));
	    System.exit(-1);
	}
    }
}
