import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter {

	public void keyPressed(KeyEvent e) {
		//int key = e.getKeyCode();
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:	// Quit
				System.exit(1);
				break;
			case KeyEvent.VK_SPACE:		// New image
				FaceDisplayer.generateNewFace();;
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {}
}