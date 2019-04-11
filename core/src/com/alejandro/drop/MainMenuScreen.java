package com.alejandro.drop;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created by alejandrotorresruiz on 04/03/2019.
 */

public class MainMenuScreen implements Screen{

    private Drop game;
    private OrthographicCamera camera;
    private Texture txBackground;
    private String puntos;

    public MainMenuScreen(Drop game){
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,480);
        txBackground = new Texture("imagenes/background.jpg");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        leerFicheroPuntos();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(txBackground,0,0);
        game.font.draw(game.batch, "Juego de Gotas ", 100, 200);
        game.font.draw(game.batch, "Pulsa para jugar!!", 100, 150);
        game.font.draw(game.batch, "Nivel maximo alcanzado "+puntos, 100, 100);

        game.batch.end();

        if (Gdx.input.isTouched()) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }


    public void leerFicheroPuntos(){
        /*FileHandle file = Gdx.files.internal("puntos.txt");
        puntos = file.readString();
        file.writeString("papa",true);
        puntos = file.readString();*/

       FileHandle fileRead = Gdx.files.local("Text/puntos.txt");
        puntos = fileRead.readString();

    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        txBackground.dispose();
    }
}
