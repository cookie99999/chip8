public class CPU {
    private byte[] memory = new byte[4096];
    
    private short pc;
    private short ir;
    private byte delay_timer;
    private byte sound_timer;
    private byte[] registers = new byte[16];

    private boolean isDebug = true;

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
					 opcode, this.pc, this.ir, this.delay_timer, this.sound_timer));
	for (byte i = 0; i < 16; i++)
	    System.out.print(String.format("V%x: %02x ", i, registers[i]));
	System.out.println();
    }
    
    public void writeMem(short address, byte data) {
	//check for errors
	this.memory[address] = data;
    }
    
    public void step() {
	short opcode = (short)((this.memory[this.pc] << 8) | (this.memory[this.pc + 1]));
	byte op = (byte)((opcode & 0xf000) >>> 12);
	byte x = (byte)((opcode & 0x0f00) >>> 8);
	byte y = (byte)((opcode & 0x00f0) >>> 4);
	byte n = (byte)(opcode & 0x000f);
	byte nn = (byte)(opcode & 0x00ff);
	short nnn = (short)(opcode & 0x0fff);

	if (this.isDebug)
	    debugPrint(opcode);

	this.pc += 2; //increment before execution so jmp won't be messed up
	
	switch (op) {
	case 0x0:
	    if (opcode == 0x00e0) {
		System.out.println("<DEBUG> screen cleared");
	    }
	    break;
	case 0x1:
	    this.pc = nnn;
	    break;
	case 0x6:
	    this.registers[x] = nn;
	    break;
	case 0x7:
	    this.registers[x] += nn;
	    break;
	case 0xa:
	    this.ir = nnn;
	    break;
	case 0xd:
	    System.out.println("<DEBUG> drew " + n + "to " + registers[x] + ", " + registers[y]);
	    break;
	default:
	    System.out.println("<ERROR> unimplemented opcode " + String.format("%04x", opcode));
	}
    }
}
