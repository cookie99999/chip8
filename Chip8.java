import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
    
public class Chip8 {
    public static byte[] loadBin(String path) {
	Path p = Paths.get(path);
	byte[] data = null;
	try {
	    data = Files.readAllBytes(p);
	} catch (java.io.IOException e) {
	    System.out.println("<ERROR> IOException in loadBin");
	    System.out.println(e);
	    System.exit(-1);
	}
	return data;
    }
    
    public static void main(String[] args) {
	byte[] bin = null;
	try {
	    bin = loadBin(args[0]); //todo: check it's a valid path
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Usage: java Chip8 <bin file path>");
	    System.out.println(e);
	    System.exit(0);
	}
	CPU cpu = new CPU();
	int binLen = bin.length;
	if (binLen > 4096 - 0x200) {
	    System.out.println("<ERROR> input file too large");
	    System.exit(-1);
	}

	for (short i = 0; i < binLen; i++) {
	    cpu.writeMem((short)(0x200 + i), bin[i]);
	}

	while (true) {
	    cpu.step();
	}
    }
}
