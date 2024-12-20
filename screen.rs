#[derive(Debug)]
pub struct Screen {
    pub xmax: u8,
    pub ymax: u8,
    pub scale: u8,
    pixels: Vec<u8>,
}

impl Screen {
    pub fn new(xmax: u8, ymax: u8, scale: u8) -> Self {
	Screen {
	    xmax: xmax,
	    ymax: ymax,
	    scale: scale,
	    pixels: vec![0; xmax as usize * ymax as usize], //todo: pack them
	}
    }

    pub fn get_width(&self) -> u16 {
	self.xmax as u16 * self.scale as u16
    }

    pub fn get_height(&self) -> u16 {
	self.ymax as u16 * self.scale as u16
    }

    pub fn get_pixel(&self, x: u8, y: u8) -> u8 {
	self.pixels[(y as usize % self.ymax as usize) * self.xmax as usize + (x as usize % self.xmax as usize)]
    }

    pub fn set_pixel(&mut self, x: u8, y: u8, color: u8) {
	self.pixels[(y as usize % self.ymax as usize) * self.xmax as usize +
		     (x as usize % self.xmax as usize)] = color;
    }

    pub fn clear(&mut self) {
	self.pixels.fill(0);
    }
}
