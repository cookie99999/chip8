mod cpu;

fn load_binary(cpu: &mut cpu::CPU, path: &str, offset: u16) {
    
}

fn main() {
    let mut cpu = cpu::CPU::new();
    cpu.dbg_print(0xf00f);
    cpu.reset();
    let mut n: u8 = 0;
    n = n.saturating_sub(1);
    println!("n = {:02X}", n);
}
