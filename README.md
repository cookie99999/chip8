# CHIP-8 in Rust

This is a CHIP-8 interpreter made as a learning project, translated from the java branch. It currently passes the Timendus tests aside from some of the quirks accuracy. Audio is not yet implemented.

## Usage
```
cargo run <bin file path>
```

## Building
Just use cargo build, written and tested on rustc 1.83.0.

## Todo
- Add sound support
- Add support for extended Super CHIP and XOCHIP instructions and hires mode
- GUI configuration (or at least command line switches)