extern crate sdl2;
mod cpu;
mod screen;

use std::env;
use sdl2::pixels::Color;
use sdl2::rect::Rect;
use sdl2::event::Event;
use sdl2::keyboard::Keycode;

fn load_binary(cpu: &mut cpu::CPU, path: &str, offset: u16) {
    let buf: Vec<u8> = std::fs::read(path).unwrap();
    for (i, b) in buf.iter().enumerate() {
	cpu.write_mem(offset + (i as u16), *b);
    }
}

fn draw_screen(screen: &screen::Screen,
	       canvas: &mut sdl2::render::WindowCanvas) {
    for x in (0..screen.xmax).enumerate() {
	for y in (0..screen.ymax).enumerate() {
	    let c: Color = match screen.get_pixel(x.1, y.1) {
		0 =>
		    Color::RGB(0, 0, 0),
		1 =>
		    Color::RGB(255, 255, 255),
		_ =>
		    Color::RGB(255, 0, 0),
	    };
	    canvas.set_draw_color(c);
	    canvas.fill_rect(Rect::new(x.1 as i32 * screen.scale as i32,
				       y.1 as i32 * screen.scale as i32,
				       screen.scale as u32,
				       screen.scale as u32)).unwrap();
	}
    }
}

fn main() {
    let path = env::args().nth(1).expect("Usage: main <binary path>");
    let mut screen = screen::Screen::new(64, 32, 10);
    let mut cpu = cpu::CPU::new(&mut screen);
    cpu.reset();
    load_binary(&mut cpu, &path, 0x200);
    
    let sdl_context = sdl2::init().unwrap();
    let video = sdl_context.video().unwrap();
    let width: u32 = cpu.screen.get_width().into();
    let height: u32 = cpu.screen.get_height().into();
    let window = video.window("Chip8-rs", width, height)
	.position_centered()
	.opengl()
	.build()
	.unwrap();
    let mut canvas = window.into_canvas().build().unwrap();
    canvas.clear();
    
    let mut curtime = std::time::Instant::now();
    let mut prevtime = curtime;
    let mut tick_counter = 0;
    let mut cycle_counter = 0;
    let cycles_sec = 700; //todo: configurable
    let interval = std::time::Duration::from_nanos(1000000000 / cycles_sec);

    let mut event_pump = sdl_context.event_pump().unwrap();

    'running: loop {
	for e in event_pump.poll_iter() {
	    match e {
		Event::Quit {..} |
		Event::KeyDown { keycode: Some(Keycode::Escape), .. } => {
		    break 'running;
		},
		Event::KeyDown { keycode: Some(kc), .. } => {
		    match kc {
			Keycode::NUM_1 =>
			    cpu.key_down(1),
			Keycode::NUM_2 =>
			    cpu.key_down(2),
			Keycode::NUM_3 =>
			    cpu.key_down(3),
			Keycode::NUM_4 =>
			    cpu.key_down(0xc),
			Keycode::Q =>
			    cpu.key_down(4),
			Keycode::W =>
			    cpu.key_down(5),
			Keycode::E =>
			    cpu.key_down(6),
			Keycode::R =>
			    cpu.key_down(0xd),
			Keycode::A =>
			    cpu.key_down(7),
			Keycode::S =>
			    cpu.key_down(8),
			Keycode::D =>
			    cpu.key_down(9),
			Keycode::F =>
			    cpu.key_down(0xe),
			Keycode::Z =>
			    cpu.key_down(0xa),
			Keycode::X =>
			    cpu.key_down(0),
			Keycode::C =>
			    cpu.key_down(0xb),
			Keycode::V =>
			    cpu.key_down(0xf),
			_ => {},
		    };
		},
		Event::KeyUp { keycode: Some(kc), .. } => {
		    match kc {
			Keycode::NUM_1 =>
			    cpu.key_up(1),
			Keycode::NUM_2 =>
			    cpu.key_up(2),
			Keycode::NUM_3 =>
			    cpu.key_up(3),
			Keycode::NUM_4 =>
			    cpu.key_up(0xc),
			Keycode::Q =>
			    cpu.key_up(4),
			Keycode::W =>
			    cpu.key_up(5),
			Keycode::E =>
			    cpu.key_up(6),
			Keycode::R =>
			    cpu.key_up(0xd),
			Keycode::A =>
			    cpu.key_up(7),
			Keycode::S =>
			    cpu.key_up(8),
			Keycode::D =>
			    cpu.key_up(9),
			Keycode::F =>
			    cpu.key_up(0xe),
			Keycode::Z =>
			    cpu.key_up(0xa),
			Keycode::X =>
			    cpu.key_up(0),
			Keycode::C =>
			    cpu.key_up(0xb),
			Keycode::V =>
			    cpu.key_up(0xf),
			_ => {},
		    };
		},
		_ => {}
	    }
	}

	prevtime = curtime;
	curtime = std::time::Instant::now();
	let dtime = curtime - prevtime;
	tick_counter += dtime.as_nanos();
	cycle_counter += dtime.as_nanos();
	'ticking: loop {
	    if tick_counter < (1000000000 / 60) {
		break 'ticking;
	    }
	    cpu.tick_timers();
	    tick_counter -= 1000000000 / 60;
	}

	cpu.step();
	draw_screen(&cpu.screen, &mut canvas);
	canvas.present();
    }
}
