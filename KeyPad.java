import javax.swing.*;
import java.awt.event.*;

public class KeyPad implements KeyListener {
    private boolean[] keys = new boolean[16];

    public KeyPad() {
	for (boolean k : keys)
	    k = false;
    }

    public void keyTyped(KeyEvent e) {
	//only need for interface
    }
    
    public void keyPressed(KeyEvent e) {
	int code = e.getKeyCode();
	switch (code) {
	case KeyEvent.VK_1:
	    keys[1] = true;
	    break;
	case KeyEvent.VK_2:
	    keys[2] = true;
	    break;
	case KeyEvent.VK_3:
	    keys[3] = true;
	    break;
	case KeyEvent.VK_4:
	    keys[0xc] = true;
	    break;
	case KeyEvent.VK_Q:
	    keys[4] = true;
	    break;
	case KeyEvent.VK_W:
	    keys[5] = true;
	    break;
	case KeyEvent.VK_E:
	    keys[6] = true;
	    break;
	case KeyEvent.VK_R:
	    keys[0xd] = true;
	    break;
	case KeyEvent.VK_A:
	    keys[7] = true;
	    break;
	case KeyEvent.VK_S:
	    keys[8] = true;
	    break;
	case KeyEvent.VK_D:
	    keys[9] = true;
	    break;
	case KeyEvent.VK_F:
	    keys[0xe] = true;
	    break;
	case KeyEvent.VK_Z:
	    keys[0xa] = true;
	    break;
	case KeyEvent.VK_X:
	    keys[0] = true;
	    break;
	case KeyEvent.VK_C:
	    keys[0xb] = true;
	    break;
	case KeyEvent.VK_V:
	    keys[0xf] = true;
	    break;
	default:
	    break;
	}
    }

    public void keyReleased(KeyEvent e) {
	int code = e.getKeyCode();
	switch (code) {
	case KeyEvent.VK_1:
	    keys[1] = false;
	    break;
	case KeyEvent.VK_2:
	    keys[2] = false;
	    break;
	case KeyEvent.VK_3:
	    keys[3] = false;
	    break;
	case KeyEvent.VK_4:
	    keys[0xc] = false;
	    break;
	case KeyEvent.VK_Q:
	    keys[4] = false;
	    break;
	case KeyEvent.VK_W:
	    keys[5] = false;
	    break;
	case KeyEvent.VK_E:
	    keys[6] = false;
	    break;
	case KeyEvent.VK_R:
	    keys[0xd] = false;
	    break;
	case KeyEvent.VK_A:
	    keys[7] = false;
	    break;
	case KeyEvent.VK_S:
	    keys[8] = false;
	    break;
	case KeyEvent.VK_D:
	    keys[9] = false;
	    break;
	case KeyEvent.VK_F:
	    keys[0xe] = false;
	    break;
	case KeyEvent.VK_Z:
	    keys[0xa] = false;
	    break;
	case KeyEvent.VK_X:
	    keys[0] = false;
	    break;
	case KeyEvent.VK_C:
	    keys[0xb] = false;
	    break;
	case KeyEvent.VK_V:
	    keys[0xf] = false;
	    break;
	default:
	    break;
	}
    }

    public boolean getKey(int key) {
	//todo: check bounds
	return keys[key];
    }
}
