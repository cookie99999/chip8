extern crate rand;
use crate::screen;

#[derive(Debug, PartialEq)]
enum CompatLevel {
    Chip8,
    Chip48,
    SChip1_0,
    SChip_1_1,
    XOChip,
}

#[derive(Debug)]
pub struct CPU<'a> {
    memory: [u8; 4096],
    pub screen: &'a mut screen::Screen,
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

impl<'a> CPU<'a> {
    pub fn new(screen: &'a mut screen::Screen) -> Self {
	CPU {
	    memory: [0; 4096],
	    screen: screen,
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
	let x: usize = ((opcode & 0x0f00) >> 8) as usize;
	let y: usize = ((opcode & 0x00f0) >> 4) as usize;
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
			self.screen.clear(),
		    0x00ee => {
			self.sp = self.sp.saturating_sub(1);
			self.pc = self.stack[self.sp as usize];
		    },
		    _ =>
			println!("Unimplemented opcode {:04X}", opcode),
		},
	    0x1 => {
		if (self.pc - 2) == nnn && self.debug {
		    //panic!("Infinite loop entered, quitting...");
		}
		self.pc = nnn;
	    },
	    0x2 => {
		self.stack[self.sp as usize] = self.pc;
		self.sp += 1;
		if self.sp > 15 {
		    self.sp = 15;
		}
		self.pc = nnn;
	    },
	    0x3 =>
		if self.registers[x] == nn {
		    self.pc += 2;
		},
	    0x4 =>
		if self.registers[x] != nn {
		    self.pc += 2;
		},
	    0x5 =>
		if self.registers[x] == self.registers[y] {
		    self.pc += 2;
		},
	    0x6 =>
		self.registers[x] = nn,
	    0x7 =>
		self.registers[x] = self.registers[x].wrapping_add(nn),
	    0x8 => match n {
		0x0 =>
		    self.registers[x] = self.registers[y],
		0x1 => {
		    self.registers[x] |= self.registers[y];
		    if self.compat == CompatLevel::Chip8 {
			self.registers[0xf] = 0;
		    }
		},
		0x2 => {
		    self.registers[x] &= self.registers[y];
		    if self.compat == CompatLevel::Chip8 {
			self.registers[0xf] = 0;
		    }
		},
		0x3 => {
		    self.registers[x] ^= self.registers[y];
		    if self.compat == CompatLevel::Chip8 {
			self.registers[0xf] = 0;
		    }
		},
		0x4 => {
		    let tmp: u8 = self.registers[x];
		    self.registers[x] = self.registers[x].wrapping_add(self.registers[y]);
		    if tmp as u16 + self.registers[y] as u16 > 255 {
			self.registers[0xf] = 1;
		    } else {
			self.registers[0xf] = 0;
		    }
		},
		0x5 => {
		    if self.registers[x] > self.registers[y] {
			self.registers[0xf] = 1;
		    } else {
			self.registers[0xf] = 0;
		    }
		    self.registers[x] = self.registers[x].wrapping_sub(self.registers[y]);
		},
		0x6 => {
		    if self.compat == CompatLevel::Chip8 ||
			self.compat == CompatLevel::XOChip {
			    self.registers[x] = self.registers[y];
			}
		    let tmp: u8 = self.registers[x] & 1;
		    self.registers[x] >>= 1;
		    self.registers[0xf] = tmp;
		},
		0x7 => {
		    if self.registers[y] > self.registers[x] {
			self.registers[0xf] = 1;
		    } else {
			self.registers[0xf] = 0;
		    }
		    self.registers[x] = self.registers[y].wrapping_sub(self.registers[x]);
		},
		0xe => {
		    if self.compat == CompatLevel::Chip8 ||
			self.compat == CompatLevel::XOChip {
			    self.registers[x] = self.registers[y];
			}
		    let tmp: u8 = (self.registers[x] >> 7) & 1;
		    self.registers[x] <<= 1;
		    self.registers[0xf] = tmp;
		},
		_ => println!("Unsupported sub-op in op 0x8"),
	    },
	    0xa =>
		self.ir = nnn,
	    0xb =>
		self.pc = match self.compat {
		    CompatLevel::Chip8 =>
			nnn.wrapping_add(self.registers[0] as u16),
		    _ =>
			nnn.wrapping_add(self.registers[x] as u16),
		},
	    0xc =>
		self.registers[x] = rand::random::<u8>() & nn,
	    0xd => {
		let mut spx = self.registers[x];
		let mut spy = self.registers[y];
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
				self.screen.set_pixel(pix_x, spy,
						      ((line >> j) & 1) ^ tmp);
				if tmp == 1 && self.screen.get_pixel(pix_x, spy) == 0 {
				    collided = true;
				}
			    }
			}
		    }
		    spy += 1;
		}
		self.registers[0xf] = collided as u8;
	    },
	    0xf => match nn {
		0x07 =>
		    self.registers[x] = self.delay_timer,
		0x15 =>
		    self.delay_timer = self.registers[x],
		0x18 =>
		    self.sound_timer = self.registers[x],
		0x1e => {
		    self.ir += self.registers[x] as u16;
		    if self.ir > 0x0fff  && self.compat != CompatLevel::Chip8 {
			self.registers[0xf] = 1;
			self.ir -= 0x0fff;
		    }
		},
		0x29 =>
		    //0x0050 is font base, 5 bytes per char
		    self.ir = 0x0050 + ((self.registers[x] & 0x0f) * 5) as u16,
		0x33 => {
		    let first: u8 = (self.registers[x] / 100) % 10;
		    let second: u8 = (self.registers[x] / 10) % 10;
		    let third: u8 = self.registers[x] % 10;
		    self.write_mem(self.ir, first);
		    self.write_mem(self.ir + 1, second);
		    self.write_mem(self.ir + 2, third);
		},
		0x55 => {
		    for i in 0..=x {
			self.write_mem(self.ir + i as u16, self.registers[i]);
		    }
		    if self.compat == CompatLevel::Chip8 {
			self.ir += x as u16 + 1;
		    }
		},
		0x65 => {
		    for i in 0..=x {
			self.registers[i] = self.read_mem(self.ir + i as u16).unwrap();
		    }
		    if self.compat == CompatLevel::Chip8 {
			self.ir += x as u16 + 1;
		    }
		},
		_ =>
		    println!("Unimplemented sub-op in op 0xf"),
	    },
	    _ =>
		println!("Unimplemented opcode {:04X}", opcode),
	}
    }
}
