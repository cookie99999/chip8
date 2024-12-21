extern crate sdl2;
mod cpu;
mod screen;

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
    let mut screen = screen::Screen::new(64, 32, 10);
    let mut cpu = cpu::CPU::new(&mut screen);
    cpu.reset();
    load_binary(&mut cpu, "3-corax+.ch8", 0x200);
    
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

    canvas.set_draw_color(Color::RGB(0, 0, 255));
    canvas.clear();
    canvas.present();

    let mut event_pump = sdl_context.event_pump().unwrap();

    'running: loop {
	for e in event_pump.poll_iter() {
	    match e {
		Event::Quit {..} |
		Event::KeyDown { keycode: Some(Keycode::Escape), .. } => {
		    break 'running;
		},
		_ => {}
	    }
	}
	
	cpu.step();
	draw_screen(&cpu.screen, &mut canvas);
	canvas.present();
    }
}
