package core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import core.entities.Player;
import core.world.TileMap;  // ✅ Correct package for TileMap!

/**
 * MainGame is the core LibGDX ApplicationAdapter.
 *
 * NEW: Now includes tilemap rendering!
 */
public class MainGame extends ApplicationAdapter {

    // ------------------ VIRTUAL RESOLUTION ------------------

    /** Logical width of the world, in world units (treated like pixels). */
    public static final int VIRTUAL_WIDTH  = 320;

    /** Logical height of the world, in world units. */
    public static final int VIRTUAL_HEIGHT = 180;

    // ------------------ RENDERING CAMERA / VIEWPORT ------------------

    private OrthographicCamera camera;
    private FitViewport viewport;
    private SpriteBatch batch;

    // ------------------ GAME OBJECTS ------------------

    /** The controllable player entity */
    private Player player;

    /**
     * The tilemap that renders the ground/environment.
     *
     * NEW: This replaces our hardcoded MAP_WIDTH/MAP_HEIGHT!
     * The map now defines the world boundaries.
     */
    private TileMap tileMap;

    /**
     * Track whether F11 was pressed last frame to prevent toggle spam.
     * This is a simple "debounce" mechanism.
     */
    private boolean wasF11Pressed = false;

    // ------------------ LIFECYCLE: CREATE ------------------

    @Override
    public void create() {
        System.out.println("🎮 MainGame.create() started...");

        // 1) Create camera and viewport
        camera = new OrthographicCamera();
        System.out.println("✅ Camera created");

        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        System.out.println("✅ Viewport created");

        // 2) Create the SpriteBatch
        batch = new SpriteBatch();
        System.out.println("✅ SpriteBatch created");

        // 3) Create the tilemap (loads individual tile files)
        System.out.println("⏳ Loading tilemap...");
        try {
            tileMap = new TileMap(40, 23);  // No file path needed!
            System.out.println("✅ TileMap created successfully!");
        } catch (Exception e) {
            System.out.println("❌ ERROR creating TileMap:");
            e.printStackTrace();
        }

        // 4) Create the player in the center of the map
        System.out.println("⏳ Creating player...");
        try {
            float startX = tileMap.getMapWidthWorld() / 2f;
            float startY = tileMap.getMapHeightWorld() / 2f;
            player = new Player(startX, startY, 60f);
            System.out.println("✅ Player created at: " + startX + ", " + startY);
        } catch (Exception e) {
            System.out.println("❌ ERROR creating Player:");
            e.printStackTrace();
        }

        System.out.println("🎮 MainGame.create() finished!");
    }

    // ------------------ LIFECYCLE: RENDER ------------------

    private int frameCount = 0;  // Track frames for debugging

    @Override
    public void render() {
        // Debug: Print every 60 frames (once per second at 60fps)
        if (frameCount % 60 == 0) {
            System.out.println("🎬 Frame " + frameCount + " - Rendering...");
        }
        frameCount++;

        // 1) Clear screen
        Gdx.gl.glClearColor(0.11f, 0.13f, 0.17f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) Handle fullscreen toggle (F11 key)
        handleFullscreenToggle();

        // 3) Update game logic
        float dt = Gdx.graphics.getDeltaTime();

        // Null check: Make sure player exists before updating
        if (player != null) {
            player.update(dt);
        } else {
            System.out.println("⚠️ WARNING: Player is null!");
        }

        // 3) Update camera to follow player
        updateCamera();

        // 4) Draw everything
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        /**
         * RENDERING ORDER MATTERS!
         *
         * Draw from back to front (painter's algorithm):
         * 1. Tilemap (ground layer)
         * 2. Player (on top of ground)
         * 3. UI elements (later, on top of everything)
         *
         * WHY? Whatever you draw last appears on top!
         */

        // Draw the tilemap first (ground layer)
        if (tileMap != null) {
            tileMap.render(batch, camera.position.x, camera.position.y,
                    VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        } else {
            // If no tilemap, draw a visible rectangle so we know rendering works
            // This shouldn't happen in production, just for debugging
            if (frameCount % 60 == 0) {
                System.out.println("⚠️ WARNING: TileMap is null!");
            }
        }

        // Draw the player on top
        if (player != null) {
            player.render(batch);
        } else {
            if (frameCount % 60 == 0) {
                System.out.println("⚠️ WARNING: Player is null!");
            }
        }

        batch.end();
    }

    /**
     * Handles fullscreen toggling with the F11 key.
     *
     * DEBOUNCING: We track if F11 was pressed last frame to prevent
     * rapid toggling (pressing F11 once shouldn't toggle 60 times!).
     *
     * HOW IT WORKS:
     * - Frame 1: F11 pressed → toggle, set wasF11Pressed = true
     * - Frame 2-60: F11 still held → do nothing (wasF11Pressed is true)
     * - Frame 61: F11 released → set wasF11Pressed = false
     * - Frame 62: F11 pressed again → toggle works again!
     */
    private void handleFullscreenToggle() {
        boolean isF11Pressed = Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.F11);

        // Only toggle when F11 transitions from NOT pressed to pressed
        if (isF11Pressed && !wasF11Pressed) {
            if (Gdx.graphics.isFullscreen()) {
                // Exit fullscreen → restore windowed mode
                Gdx.graphics.setWindowedMode(1200, 720);
            } else {
                // Enter fullscreen → use current monitor's resolution
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        wasF11Pressed = isF11Pressed;
    }

    /**
     * Updates camera to follow player with map boundaries.
     *
     * NOW: Uses the tilemap's dimensions instead of hardcoded values!
     */
    private void updateCamera() {
        // Safety check
        if (player == null || tileMap == null) {
            return;
        }

        float targetX = player.getX();
        float targetY = player.getY();

        float halfWidth  = VIRTUAL_WIDTH  / 2f;
        float halfHeight = VIRTUAL_HEIGHT / 2f;

        // Use tilemap dimensions for boundaries
        float mapWidth  = tileMap.getMapWidthWorld();
        float mapHeight = tileMap.getMapHeightWorld();

        /**
         * EDGE CASE: If map is smaller than screen, center the camera.
         * This prevents weird clamping issues with tiny maps.
         */
        float camX, camY;

        if (mapWidth <= VIRTUAL_WIDTH) {
            camX = mapWidth / 2f;  // Center on small map
        } else {
            camX = clamp(targetX, halfWidth, mapWidth - halfWidth);
        }

        if (mapHeight <= VIRTUAL_HEIGHT) {
            camY = mapHeight / 2f;  // Center on small map
        } else {
            camY = clamp(targetY, halfHeight, mapHeight - halfHeight);
        }

        // Pixel-perfect positioning
        camera.position.set(Math.round(camX), Math.round(camY), 0);
        camera.update();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // ------------------ LIFECYCLE: RESIZE ------------------

    @Override
    public void resize(int width, int height) {
        /**
         * INTEGER SCALING for pixel-perfect rendering.
         *
         * This is what keeps your tiles crisp in fullscreen!
         *
         * HOW IT WORKS:
         * 1. Calculate the largest integer scale that fits
         * 2. Create a viewport at that scale
         * 3. Center it in the window (letterboxing)
         *
         * EXAMPLE: 1920×1080 window with 320×180 virtual resolution
         * - 1920 / 320 = 6
         * - 1080 / 180 = 6
         * - Scale = 6 → viewport is 1920×1080 (perfect fit!)
         *
         * EXAMPLE 2: 1366×768 window
         * - 1366 / 320 = 4.26... → floor to 4
         * - 768 / 180 = 4.26... → floor to 4
         * - Scale = 4 → viewport is 1280×720
         * - Letterboxing: (1366-1280)/2 = 43px black bars on sides
         */
        int scale = Math.max(1, Math.min(width / VIRTUAL_WIDTH, height / VIRTUAL_HEIGHT));

        int vpW = VIRTUAL_WIDTH * scale;
        int vpH = VIRTUAL_HEIGHT * scale;

        int vpX = (width  - vpW) / 2;
        int vpY = (height - vpH) / 2;

        viewport.setScreenBounds(vpX, vpY, vpW, vpH);
        viewport.apply(true);
    }

    // ------------------ LIFECYCLE: DISPOSE ------------------

    @Override
    public void dispose() {
        player.dispose();
        tileMap.dispose();  // NEW: Don't forget to dispose the tilemap!
        batch.dispose();
    }
}