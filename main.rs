extern crate sdl2;
mod cpu;

fn load_binary(cpu: &mut cpu::CPU, path: &str, offset: u16) {
    let buf: Vec<u8> = std::fs::read(path).unwrap();
    for (i, b) in buf.iter().enumerate() {
	cpu.write_mem(offset + (i as u16), *b);
    }
}

fn main() {
    let mut cpu = cpu::CPU::new();
    cpu.reset();
    load_binary(&mut cpu, "2-ibm-logo.ch8", 0x200);

    'running: loop {
	cpu.step();
    }
}
