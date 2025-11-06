package desktop;


import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;           // Desktop entry point using LWJGL3
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration; // Window/config settings for the app
import core.MainGame;                                                // Your core LibGDX game class

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration(); // Create window config
        cfg.setTitle("Poke Clone");                                                // Window title
        cfg.setWindowedMode(1200, 720);                                // Window size (3x 320x180 virtual res)
        cfg.useVsync(true);                                                        // Enable V-Sync to cap tearing
        cfg.setForegroundFPS(60);                                                  // Target FPS when focused
        new Lwjgl3Application(new MainGame(), cfg);                                // Launch the game with this config
    }
}
