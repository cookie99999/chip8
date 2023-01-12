public class CPU {
    private byte[] memory = new byte[4096];
    
    private short pc;
    private short ir;
    private byte delay_timer;
    private byte sound_timer;
    private byte[] registers = new byte[16];

    private boolean isDebug = false;

    private MachineScreen screen;
    
    public void CPU() {
	this.pc = 0x200;
	this.ir = 0;
	this.delay_timer = (byte)0xff;
	this.sound_timer = 0;
	for (byte b : registers)
	    b = 0;
	for (byte b : memory)
	    b = 0;
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
	short opcode = (short)((memory[pc] << 8) | (memory[pc + 1]));
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
		System.out.println("<DEBUG> screen cleared");
	    }
	    break;
	case 0x1:
	    pc = nnn;
	    break;
	case 0x6:
	    registers[x] = nn;
	    break;
	case 0x7:
	    registers[x] += nn;
	    break;
	case 0xa:
	    ir = nnn;
	    break;
	case 0xd:
	    int xcoord = registers[x] % 64;
	    int ycoord = registers[y] % 32;
	    
	    for (int i = 0; i < n; i++) {
		byte line = memory[ir + i];
		screen.drawSpriteLine(xcoord, ycoord + i, line);
	    }
	    break;
	default:
	    System.out.println("<ERROR> unimplemented opcode " + String.format("%04x", opcode));
	}
    }
}
