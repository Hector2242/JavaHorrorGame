package core.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

/**
 * TileMap that works with INDIVIDUAL TILE FILES instead of a spritesheet!
 *
 * MUCH SIMPLER APPROACH:
 * - Each tile is a separate PNG file
 * - Easy to add new tiles (just add a new file!)
 * - Easy to update tiles (just replace the file!)
 * - No need to calculate grid positions
 *
 * HOW IT WORKS:
 * 1. Load all PNG files from assets/tiles/ folder
 * 2. Store them in a HashMap with IDs
 * 3. Place tiles by referencing their IDs
 */
public class TileMap {

    // ------------------ CONSTANTS ------------------

    /**
     * All tiles render at this size in the game world.
     * Your tiles appear to be 32×32 pixels based on the images.
     */
    public static final int TILE_SIZE = 32;

    // ------------------ TILE IDS ------------------

    /**
     * Tile IDs make your code readable!
     * Instead of: mapData[y][x] = 5;
     * You write:   mapData[y][x] = TILE_FLOOR_CLEAN;
     */
    public static final int TILE_EMPTY = -1;           // No tile (transparent)
    public static final int TILE_FLOOR_CLEAN = 0;      // Clean floor tile
    public static final int TILE_FLOOR_CHECKER = 1;    // Checkerboard floor
    public static final int TILE_FLOOR_DARK = 2;       // Dark floor
    public static final int TILE_FLOOR_BLOODY = 3;     // Floor with blood
    public static final int TILE_FURNITURE_DESK = 10;  // Desk/table
    public static final int TILE_FURNITURE_CABINET = 11; // Cabinet/dresser
    public static final int TILE_WINDOW = 20;          // Window/dark tile

    // ------------------ MAP DATA ------------------

    private int[][] mapData;  // Simple 2D array: [y][x] = tileId
    private int mapWidth;
    private int mapHeight;

    // ------------------ TILE STORAGE ------------------

    /**
     * HashMap stores all loaded tiles.
     * Key = tile ID (int)
     * Value = TextureRegion (the actual image)
     */
    private Map<Integer, TextureRegion> tileTextures;

    /**
     * Keep track of Texture objects for disposal.
     */
    private Map<Integer, Texture> ownedTextures;

    // ------------------ CONSTRUCTOR ------------------

    /**
     * Creates a tilemap that loads individual tile files.
     *
     * @param mapWidth   Number of tiles wide
     * @param mapHeight  Number of tiles tall
     */
    public TileMap(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        // Initialize map data (all tiles start as EMPTY)
        mapData = new int[mapHeight][mapWidth];
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                mapData[y][x] = TILE_EMPTY;
            }
        }

        // Initialize tile storage
        tileTextures = new HashMap<>();
        ownedTextures = new HashMap<>();

        // Load all tile images
        loadTiles();

        // Generate a sample map
        generateSampleMap();
    }

    /**
     * Loads individual tile files from the assets/tiles/ folder.
     *
     * NAMING CONVENTION:
     * Put your tile files in assets/tiles/ with descriptive names:
     * - floor_clean.png
     * - floor_checker.png
     * - floor_bloody.png
     * - desk.png
     * - cabinet.png
     * etc.
     */
    private void loadTiles() {
        System.out.println("📦 Loading individual tile files...");

        /**
         * HELPER METHOD: Load a single tile file.
         *
         * @param id       The tile ID constant (e.g., TILE_FLOOR_CLEAN)
         * @param filename The PNG file name (e.g., "floor_clean.png")
         */
        loadTile(TILE_FLOOR_CLEAN, "hospitalFloor1.png");
        loadTile(TILE_FLOOR_CHECKER, "hospitalFloor2.png");
        loadTile(TILE_FLOOR_DARK, "hospitalFloor3.png");
        loadTile(TILE_FLOOR_BLOODY, "hospitalFloorBloodClear.png");
        loadTile(TILE_FURNITURE_DESK, "hospitalDesk.png");
        loadTile(TILE_FURNITURE_CABINET, "hospitalCabinet.png");
        loadTile(TILE_WINDOW, "window.png");

        System.out.println("✅ Loaded " + tileTextures.size() + " tile types");
    }

    /**
     * Loads a single tile file.
     *
     * HOW IT WORKS:
     * 1. Try to load the file from assets/tiles/
     * 2. If successful, store it in the HashMap
     * 3. If failed, print a warning but continue (game won't crash)
     */
    private void loadTile(int id, String filename) {
        try {
            String path = "assets/tiles/" + filename;

            // Check if file exists before trying to load
            FileHandle file = Gdx.files.internal(path);
            if (!file.exists()) {
                System.out.println("⚠️  Tile file not found: " + filename + " (skipping)");
                return;
            }

            // Load the texture
            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            // Store texture and region
            ownedTextures.put(id, texture);
            tileTextures.put(id, new TextureRegion(texture));

            System.out.println("   ✓ Loaded: " + filename + " (ID: " + id + ")");

        } catch (Exception e) {
            System.out.println("   ✗ ERROR loading " + filename + ": " + e.getMessage());
        }
    }

    /**
     * Generates a sample map to demonstrate the tiles.
     *
     * CUSTOMIZE THIS: Replace with your own level design!
     */
    private void generateSampleMap() {
        System.out.println("🎨 Generating sample map...");

        // Fill with clean floor
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                // Create a pattern: checker floor in some areas, clean in others
                if (x < mapWidth / 3) {
                    mapData[y][x] = TILE_FLOOR_CHECKER;
                } else if (x < 2 * mapWidth / 3) {
                    mapData[y][x] = TILE_FLOOR_CLEAN;
                } else {
                    mapData[y][x] = TILE_FLOOR_DARK;
                }
            }
        }

        // Add some bloody floors
        for (int i = 0; i < 20; i++) {
            int x = (i * 7) % mapWidth;
            int y = (i * 11) % mapHeight;
            mapData[y][x] = TILE_FLOOR_BLOODY;
        }

        // Add some furniture
        if (mapWidth > 10 && mapHeight > 10) {
            mapData[5][5] = TILE_FURNITURE_DESK;
            mapData[5][10] = TILE_FURNITURE_CABINET;
            mapData[10][15] = TILE_FURNITURE_DESK;
            mapData[15][8] = TILE_FURNITURE_CABINET;
        }

        // Add windows along top edge
        for (int x = 2; x < mapWidth - 2; x += 4) {
            if (mapHeight > 2) {
                mapData[mapHeight - 2][x] = TILE_WINDOW;
            }
        }

        System.out.println("✅ Sample map generated");
    }

    // ------------------ RENDERING ------------------

    /**
     * Renders the visible portion of the map.
     *
     * CAMERA CULLING: Only draws tiles the camera can see (performance!).
     */
    public void render(SpriteBatch batch, float camX, float camY, float camW, float camH) {
        // Calculate visible tile range
        float camLeft   = camX - camW / 2f;
        float camRight  = camX + camW / 2f;
        float camBottom = camY - camH / 2f;
        float camTop    = camY + camH / 2f;

        int startX = Math.max(0, (int)(camLeft / TILE_SIZE) - 1);
        int endX   = Math.min(mapWidth - 1, (int)(camRight / TILE_SIZE) + 1);
        int startY = Math.max(0, (int)(camBottom / TILE_SIZE) - 1);
        int endY   = Math.min(mapHeight - 1, (int)(camTop / TILE_SIZE) + 1);

        // Draw visible tiles
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                int tileId = mapData[y][x];

                // Skip empty tiles
                if (tileId == TILE_EMPTY) continue;

                // Get the texture for this tile
                TextureRegion texture = tileTextures.get(tileId);
                if (texture == null) {
                    // Tile ID has no texture loaded - skip silently
                    continue;
                }

                // Calculate world position
                float worldX = x * TILE_SIZE;
                float worldY = y * TILE_SIZE;

                // Draw the tile
                batch.draw(texture, worldX, worldY, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    // ------------------ MAP EDITING ------------------

    /**
     * Sets a tile at a specific position.
     *
     * @param worldX  X coordinate in tiles (not pixels!)
     * @param worldY  Y coordinate in tiles
     * @param tileId  The tile ID to place (e.g., TILE_FLOOR_CLEAN)
     */
    public void setTile(int worldX, int worldY, int tileId) {
        if (worldX < 0 || worldX >= mapWidth || worldY < 0 || worldY >= mapHeight) {
            return;  // Out of bounds
        }
        mapData[worldY][worldX] = tileId;
    }

    /**
     * Gets the tile at a specific position.
     *
     * @return Tile ID, or TILE_EMPTY if out of bounds
     */
    public int getTile(int worldX, int worldY) {
        if (worldX < 0 || worldX >= mapWidth || worldY < 0 || worldY >= mapHeight) {
            return TILE_EMPTY;
        }
        return mapData[worldY][worldX];
    }

    /**
     * Clears a tile (makes it empty/transparent).
     */
    public void clearTile(int worldX, int worldY) {
        setTile(worldX, worldY, TILE_EMPTY);
    }

    // ------------------ UTILITY ------------------

    /**
     * Converts world pixel coordinates to tile coordinates.
     */
    public int[] worldToTile(float worldX, float worldY) {
        return new int[]{
                (int)(worldX / TILE_SIZE),
                (int)(worldY / TILE_SIZE)
        };
    }

    /**
     * Checks if a tile type is loaded.
     */
    public boolean hasTile(int tileId) {
        return tileTextures.containsKey(tileId);
    }

    // ------------------ GETTERS ------------------

    public int getMapWidth() { return mapWidth; }
    public int getMapHeight() { return mapHeight; }
    public int getMapWidthWorld() { return mapWidth * TILE_SIZE; }
    public int getMapHeightWorld() { return mapHeight * TILE_SIZE; }

    // ------------------ CLEANUP ------------------

    public void dispose() {
        System.out.println("🧹 Disposing TileMap textures...");
        for (Texture texture : ownedTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        tileTextures.clear();
        ownedTextures.clear();
    }
}