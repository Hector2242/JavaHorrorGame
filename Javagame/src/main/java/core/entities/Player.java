package core.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {

    // --- Tuning ---
    private static final float FRAME_DURATION = 0.12f;   // seconds per anim frame
    private static final float DEFAULT_SPEED  = 60f;     // walk speed (world units / s)
    private static final float RUN_MULT       = 1.6f;    // sprint multiplier
    private static final float TARGET_HEIGHT_WORLD = 32f; // draw height in world units

    // --- Position / state ---
    private float x, y;
    private float speed = DEFAULT_SPEED;
    private float stateTime = 0f;
    private boolean moving = false;

    private enum Dir { DOWN, UP, LEFT, RIGHT }
    private Dir facing = Dir.DOWN;

    // --- Graphics ---
    private final TextureRegion idleDown, idleUp, idleLeft, idleRight;
    private final Animation<TextureRegion> walkDown, walkUp, walkLeft, walkRight;
    private final Texture[] ownedTextures;

    public Player(float startX, float startY, float speed) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;

        // Load textures
        Texture idleFrontTex  = load("assets/player/idleFront.PNG");
        Texture idleBackTex   = load("assets/player/idleBack.PNG");
        Texture idleLeftTex   = load("assets/player/idleLeftView.PNG");
        Texture idleRightTex  = load("assets/player/IdleRightView.PNG");

        Texture f1 = load("assets/player/animationFront1.PNG");
        Texture f2 = load("assets/player/animationFront2.PNG");
        Texture b1 = load("assets/player/animationBackWalk1.PNG");
        Texture b2 = load("assets/player/animationBackWalk2.PNG");
        Texture l1 = load("assets/player/animationLeftWalk1.PNG");
        Texture l2 = load("assets/player/animationLeftWalk2.PNG");
        Texture r1 = load("assets/player/animationRightWalk1.PNG");
        Texture r2 = load("assets/player/animationRightWalk2.PNG");

        ownedTextures = new Texture[] {
                idleFrontTex, idleBackTex, idleLeftTex, idleRightTex,
                f1, f2, b1, b2, l1, l2, r1, r2
        };

        // Regions & animations
        idleDown  = new TextureRegion(idleFrontTex);
        idleUp    = new TextureRegion(idleBackTex);
        idleLeft  = new TextureRegion(idleLeftTex);
        idleRight = new TextureRegion(idleRightTex);

        walkDown  = loop(frames(f1, f2));
        walkUp    = loop(frames(b1, b2));
        walkLeft  = loop(frames(l1, l2));
        walkRight = loop(frames(r1, r2));
    }

    private static Texture load(String path) {
        Texture t = new Texture(path);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }
    private static Animation<TextureRegion> loop(TextureRegion[] fr) {
        Animation<TextureRegion> a = new Animation<>(FRAME_DURATION, fr);
        a.setPlayMode(Animation.PlayMode.LOOP);
        return a;
    }
    private static TextureRegion[] frames(Texture a, Texture b) {
        return new TextureRegion[] { new TextureRegion(a), new TextureRegion(b) };
    }

    public void update(float delta) {
        float vx = 0f, vy = 0f;

        // Input → direction vector
        boolean up    = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down  = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left  = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        if (up)    vy += 1f;
        if (down)  vy -= 1f;
        if (left)  vx -= 1f;
        if (right) vx += 1f;

        // Facing from input signs
        if      (vy > 0) facing = Dir.UP;
        else if (vy < 0) facing = Dir.DOWN;
        else if (vx > 0) facing = Dir.RIGHT;
        else if (vx < 0) facing = Dir.LEFT;

        // Normalize so diagonals aren't faster
        float len = (float) Math.hypot(vx, vy);
        if (len > 0f) { vx /= len; vy /= len; }

        // Sprint as a speed scalar
        boolean isSprinting = shiftPressed && len > 0f;
        float effectiveSpeed = speed * (isSprinting ? RUN_MULT : 1f);

        // Integrate
        x += vx * effectiveSpeed * delta;
        y += vy * effectiveSpeed * delta;

        moving = (len > 0f);

        // Slightly faster animation while sprinting
        float animScale = isSprinting ? 1.25f : 1f;
        stateTime += delta * animScale;
    }

    public void render(SpriteBatch batch) {
        TextureRegion frame;
        if (moving) {
            switch (facing) {
                case UP:    frame = walkUp.getKeyFrame(stateTime, true);    break;
                case DOWN:  frame = walkDown.getKeyFrame(stateTime, true);  break;
                case LEFT:  frame = walkLeft.getKeyFrame(stateTime, true);  break;
                case RIGHT: frame = walkRight.getKeyFrame(stateTime, true); break;
                default:    frame = idleDown;
            }
        } else {
            switch (facing) {
                case UP:    frame = idleUp;    break;
                case DOWN:  frame = idleDown;  break;
                case LEFT:  frame = idleLeft;  break;
                case RIGHT: frame = idleRight; break;
                default:    frame = idleDown;
            }
        }

        // Scale to target world height, keep aspect, snap to whole pixels
        float srcW = frame.getRegionWidth();
        float srcH = frame.getRegionHeight();
        float scale = TARGET_HEIGHT_WORLD / srcH;
        float drawW = Math.round(srcW * scale);
        float drawH = Math.round(srcH * scale);

        batch.draw(frame, Math.round(x), Math.round(y), drawW, drawH);
    }

    public void dispose() {
        for (Texture t : ownedTextures) if (t != null) t.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
