# CHIP-8 in Java

This is a CHIP-8 interpreter made as a learning project. It currently passes all tests in Timendus' test suite, aside from the various quirks settings as compatibility levels have not been implemented yet.

## Usage
```
java Chip8 <bin file path>
```

## Building
This interpreter uses no libraries outside of the standard JDK. It can be built by `javac Chip8.java`. It has only been tested on Java 17 and 19, however it should work on any version of Java 8 or higher.

## Todo
- Add sound support
- Add support for extended Super CHIP and XOCHIP instructions