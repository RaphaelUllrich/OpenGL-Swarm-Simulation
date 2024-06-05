import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class FensterErzeugen {
	public FensterErzeugen() {
		try {
			Display.setDisplayMode(new DisplayMode(640, 480));
			Display.setTitle("Ein Fenster");
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		// render loop
		while (!Display.isCloseRequested()) {
			Display.update();
			Display.sync(60);
		}
		// Display ordnungsgemäß schließen und Programm beenden
		Display.destroy();
		System.exit(0);
	}

	public static void main(String[] args) {
		new FensterErzeugen();
	}
}
