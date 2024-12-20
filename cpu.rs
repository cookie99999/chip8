#[derive(Debug, PartialEq)]
enum CompatLevel {
    Chip8,
    Chip48,
    SChip1_0,
    SChip_1_1,
    XOChip,
}

#[derive(Debug)]
pub struct CPU {
    memory: [u8; 4096],
    pc: u16,
    ir: u16,
    delay_timer: u8,
    sound_timer: u8,
    registers: [u8; 16],
    stack: [u16; 16],
    sp: u8,
    compat: CompatLevel,
    debug: bool,
}

impl CPU {
    pub fn new() -> Self {
	CPU {
	    memory: [0; 4096],
	    pc: 0x200,
	    ir: 0,
	    delay_timer: 0,
	    sound_timer: 0,
	    registers: [0; 16],
	    stack: [0; 16],
	    sp: 0,
	    compat: CompatLevel::Chip48,
	    debug: true,
	}
    }

    pub fn reset(&mut self) {
	self.pc = 0x200;
	self.ir = 0;
	self.delay_timer = 0;
	self.sound_timer = 0;
	self.sp = 0;
	self.registers.fill(0);
	self.stack.fill(0);
    }

    pub fn dbg_print(&self, opcode: u16) {
	println!(
	    "{:04X}: {:04X} IR: {:04X} SP: {:02X} delay: {:02X} sound: {:02X}",
	    self.pc, opcode, self.ir, self.sp, self.delay_timer, self.sound_timer
	);
	for (i, r) in self.registers.iter().enumerate() {
	    print!("{:X}:{:02X} ", i, r);
	}
	println!("\n");
    }

    pub fn write_mem(&mut self, addr: u16, data: u8) {
	match addr {
	    0..4096 =>
		self.memory[addr as usize] = data,
	    _ =>
		println!("<ERROR> Out of bounds write at {:04X}", addr),
	}
    }

    pub fn read_mem(&self, addr: u16) -> Result<u8, String> {
	let data = match addr {
	    0..4096 =>
		Ok(self.memory[addr as usize]),
	    _ =>
		Err(format!("<ERROR> Out of bounds read at {:04X}", addr)),
	};
	data
    }

    pub fn step(&mut self) {
	let opcode: u16 = ((self.read_mem(self.pc).unwrap_or(0) as u16) << 8) | (self.read_mem(self.pc + 1).unwrap_or(0) as u16);
	let op: u8 = ((opcode & 0xf000) >> 12) as u8;
	let x: u8 = ((opcode & 0x0f00) >> 8) as u8;
	let y: u8 = ((opcode & 0x00f0) >> 4) as u8;
	let n: u8 = (opcode & 0x000f) as u8;
	let nn: u8 = (opcode & 0x00ff) as u8;
	let nnn: u16 = opcode & 0x0fff;

	if self.debug {
	    self.dbg_print(opcode);
	}

	self.pc += 2;
	self.pc %= 4096; //wrap in memory

	match op {
	    0x0 =>
		match opcode {
		    0x00e0 =>
			println!("Screen cleared"),
		    0x00ee => {
			self.sp = self.sp.saturating_sub(1);
			self.pc = self.stack[self.sp as usize];
		    }
		    _ =>
			println!("Unimplemented opcode {:04X}", opcode),
		},
	    0x1 => {
		if (self.pc - 2) == nnn && self.debug {
		    panic!("Infinite loop entered, quitting...");
		}
		self.pc = nnn;
	    },
	    0x6 =>
		self.registers[x as usize] = nn,
	    0x7 =>
		self.registers[x as usize] = self.registers[x as usize].wrapping_add(nn),
	    0xa =>
		self.ir = nnn,
	    0xd => {
		let mut spx = self.registers[x as usize];
		let mut spy = self.registers[y as usize];
		let mut collided: bool = false;

		spx %= 64;
		spy %= 32;
		
		for i in 0..n {
		    let line: u8 = self.read_mem(self.ir + i as u16).unwrap();
		    if spy < 32 || self.compat == CompatLevel::XOChip {
			for j in (0..=7).rev() {
			    let pix_x = spx + (7 - j);
			    let tmp = 0; //will be the previous pixel value when done
			    if pix_x < 64 || self.compat == CompatLevel::XOChip {
				//todo: actually set pixel graphically
				println!("Pixel at {}, {} ^ w/ {}", pix_x, spy, (line >> j) & 1);
				//next check for collision
			    }
			}
		    }
		    spy += 1;
		}
		//set VF to whether or not a collision occurred
	    },
	    _ =>
		println!("Unimplemented opcode {:04X}", opcode),
	}
    }
}
