package com.javajackson.pbs;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    public static int worldHeight = 8;
    public static int worldWidth = 8;

    Texture backgroudTexture;
    Texture playerTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    Texture opponentTexture;
    Texture thunderboltTexture;

    SpriteBatch spriteBatch;
    FitViewport viewport;

    Sprite playerSprite;
    Sprite opponentSprite;

    Array<Sprite> dropSprites;
    Array<Sprite> thunderboltSprites;

    float dropTimer;
    float thunderboltTimer;

    Rectangle playerRectangle;
    Rectangle dropRectangle;
    Rectangle opponentRectangle;
    Rectangle thunderboltRectangle;

    @Override
    public void create() {
        //Prepare your application here.
        backgroudTexture = new Texture("background_arena.png");
        playerTexture = new Texture("pokemon_pikachu.png");
        dropTexture = new Texture("drop.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        opponentTexture = new Texture("pokemon_squirtle.png");
        thunderboltTexture = new Texture("thunderbolt.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(worldWidth, worldHeight);

        // Initialise the Pikachu sprite
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(1, 1);
        // Initialise the Opponent sprite
        opponentSprite = new Sprite(opponentTexture);
        opponentSprite.setSize(1, 1);
        opponentSprite.setY(worldHeight-opponentSprite.getHeight());

        dropSprites = new Array<>();
        thunderboltSprites = new Array<>();

        playerRectangle = new Rectangle();
        opponentRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        thunderboltRectangle = new Rectangle();

        music.setLooping(true);
        music.setVolume(.5f);
        music.play();

    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;
        // Resize your application here. The parameters represent the new window size.
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // Draw your application here.
        input();
        logic();
        draw();
    }

    private void input() {
        float playerSpeed = 4f;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            playerSprite.translateX(playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            playerSprite.translateX(-playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.UP)) {
            playerSprite.translateY(playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
            playerSprite.translateY(-playerSpeed * delta);
        } else if ((Gdx.input.isKeyPressed(Keys.SPACE)) & (thunderboltTimer > 1f)) {
            createThunderbolt();
            thunderboltTimer = 0f;
        } else if (Gdx.input.isKeyPressed(Keys.A)) {
            opponentSprite.translateX(-playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.D)) {
            opponentSprite.translateX(playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.W)) {
            opponentSprite.translateY(playerSpeed * delta);
        } else if (Gdx.input.isKeyPressed(Keys.S)) {
            opponentSprite.translateY(-playerSpeed * delta);
        } else if ((Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) & (dropTimer > 1f)) {
            createDroplet();
            dropTimer = 0f;
        }
    }

    private void logic() {
        float delta = Gdx.graphics.getDeltaTime();
        float attackSpeed = 4f;

        // Player movement updates
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();
        // Restrict the range of x values of the player
        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth - playerWidth));
        playerRectangle.set(playerSprite.getX(), playerSprite.getY(), playerWidth, playerHeight);

        // opponent movement updates
        float opponentWidth = opponentSprite.getWidth();
        float opponentHeight = opponentSprite.getHeight();
        // Restrict the range of x values of the opponent
        opponentSprite.setX(MathUtils.clamp(opponentSprite.getX(), 0, worldWidth - opponentWidth));
        opponentRectangle.set(opponentSprite.getX(), opponentSprite.getY(), opponentWidth, opponentHeight);

        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-attackSpeed * delta);
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);

            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i);
            else if (playerRectangle.overlaps(dropRectangle)) {
                dropSprites.removeIndex(i);
                dropSound.play();
            }
        }

        for (int i = thunderboltSprites.size - 1; i >= 0; i--) {
            Sprite thunderboltSprite = thunderboltSprites.get(i);
            float thunderboltWidth = thunderboltSprite.getWidth();
            float thunderboltHeight = thunderboltSprite.getHeight();

            thunderboltSprite.translateY(attackSpeed * delta);
            thunderboltRectangle.set(thunderboltSprite.getX(), thunderboltSprite.getY(), thunderboltWidth, thunderboltHeight);

            if (thunderboltSprite.getY() < -thunderboltHeight) thunderboltSprites.removeIndex(i);
            else if (opponentRectangle.overlaps(thunderboltRectangle)) {
                thunderboltSprites.removeIndex(i);
                // TODO :: Need to make this a bolt sound!!!
                dropSound.play();
            }
        }

        dropTimer += delta;
        thunderboltTimer += delta;
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        // draw the background first
        spriteBatch.draw(backgroudTexture, 0, 0, worldWidth, worldHeight);

        // draw pokemon_pikachu
        playerSprite.draw(spriteBatch);

        // draw pokemon_squirtle
        opponentSprite.draw(spriteBatch);

        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }
        for (Sprite thunderboltSprite : thunderboltSprites) {
            thunderboltSprite.draw(spriteBatch);
        }

        spriteBatch.end();
    }

    private void createDroplet() {
        float dropWidth = 0.2f;
        float dropHeight = 1;


        Sprite dropSprite = new Sprite(dropTexture);
        dropSprite.setSize(dropWidth, dropHeight);

        dropSprite.setX(opponentSprite.getX());
        dropSprite.setY(opponentSprite.getY());
        dropSprites.add(dropSprite);
    }

    private void createThunderbolt() {
        // createThunderbolt;
        float thunderboltWidth = 0.2f;
        float thunderboltHeight = 1;

        Sprite thunderboltSprite = new Sprite(thunderboltTexture);
        thunderboltSprite.setSize(thunderboltWidth, thunderboltHeight);

        thunderboltSprite.setX(playerSprite.getX());
        thunderboltSprite.setY(playerSprite.getY());
        thunderboltSprites.add(thunderboltSprite);
    }


    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}
