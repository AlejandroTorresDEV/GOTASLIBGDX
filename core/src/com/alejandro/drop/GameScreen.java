package com.alejandro.drop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * Created by alejandrotorresruiz on 04/03/2019.
 */

public class GameScreen implements Screen {

    private OrthographicCamera camera;
    private SpriteBatch batch;

    public Sprite backgroundSprite;

    private Texture txDrop, txBucket, txBackground, txAgua, txPipe,txBalas;
    private Sound sndDrop;
    private Music musRain;

    private int puntos = 0;
    private int balasLanzadas = 0;

    private Rectangle recBucket, recBalas, recAgua, recPipe;

    private long lastDropTime;
    private long ultimoDisparo;

    private Array<Rectangle> recDrops;
    private Array<Rectangle> recBullets;

    private Drop game;

    private Actor actBucket;

    private int tiempoRespawnGotas = 800;
    public String nivelDifilcultad = "1";
    private int aguaAumentada = 10;
    private int aguaReducida = 20;


    public GameScreen(Drop game) {
        this.game = game;

        recDrops = new Array<Rectangle>();
        recBullets = new Array<Rectangle>();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = game.batch;

        //Cargamos las texturas.
        txDrop = new Texture("imagenes/droplet.png");
        txBucket = new Texture("imagenes/bucket.png");
        txAgua = new Texture(("imagenes/agua.png"));
        txPipe = new Texture("imagenes/pipe.png");
        txBackground = new Texture("imagenes/background_land.png");
        txBalas = new Texture("imagenes/shoot.png");

        recPipe = new Rectangle();
        recPipe.x = 705 - recPipe.getWidth();
        recPipe.y = 430;
        recPipe.width = 96;
        recPipe.height = 61;

        recAgua = new Rectangle();
        recAgua.x = 0;
        recAgua.y = -500;
        recAgua.width = 800;
        recAgua.height = 480;

        recBucket = new Rectangle();
        recBucket.x = 800 / 2 - 64 / 2;
        recBucket.y = 20;
        recBucket.width = 64;
        recBucket.height = 64;

        sndDrop = Gdx.audio.newSound(Gdx.files.internal("sonidos/gota.wav"));
        musRain = Gdx.audio.newMusic(Gdx.files.internal("sonidos/lluvia.mp3"));

        musRain.setLooping(true);
    }

    private void spawnRaindrop() {

        Rectangle recDrop = new Rectangle(
                MathUtils.random(0, Gdx.graphics.getWidth() - txDrop.getWidth()),
                Gdx.graphics.getHeight(),
                txDrop.getWidth(),
                txDrop.getHeight());

        recDrops.add(recDrop);
        lastDropTime = TimeUtils.millis();
    }

    @Override
    public void render(float delta) {
        //backgroundSprite.draw(batch);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        TouchGame();

        OffsetBorder();

        AjustarDificultad();

        //Generamos gotas cada segundo.
        if (TimeUtils.millis() - lastDropTime > tiempoRespawnGotas) spawnRaindrop();

        //Si llegan al suelo las eliminamos.
        for (Iterator<Rectangle> iter = recDrops.iterator(); iter.hasNext(); ) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

            if (raindrop.overlaps(recAgua)) {
                recAgua.y = recAgua.y + aguaAumentada;
                iter.remove();
            }

            if (raindrop.overlaps(recBucket)) {
                sndDrop.play();
                puntos++;
                iter.remove();
            }
        }

        ShootBullets();

        if (recAgua.overlaps(recBucket)) {
            recBucket.y = recBucket.y + 2;
        }

        //Si llegamos al tubo perdemos y volvemos a la pantalla principal
        if (recAgua.overlaps(recPipe)) {
            //Guardamos el nivel de dificultad en un fichero
            FileHandle file = Gdx.files.local("Text/puntos.txt");
            file.writeString(nivelDifilcultad, false);
            musRain.stop();
            game.setScreen(new MainMenuScreen(game));
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(txBackground, 0, 0);

        batch.draw(txBucket, recBucket.x, recBucket.y);
        batch.draw(txPipe, recPipe.x, recPipe.y);
        batch.draw(txAgua, recAgua.x, recAgua.y);

        game.font.draw(game.batch, "Puntos:" + puntos, 10, 400);
        game.font.draw(game.batch, "Dificultad:" + nivelDifilcultad, 10,450);

        //Dibujamos las gotas.
        drawRain();
        drawBullet();
        batch.end();
    }

    private void drawRain() {
        for (Rectangle drop : recDrops) {
            batch.draw(txDrop, drop.x, drop.y);
        }
    }



    private void drawBullet() {
        for (Rectangle bullet : recBullets) {
            batch.draw(txBalas, bullet.x, bullet.y);
        }
    }

    public void ShootBullets(){
        for (Iterator<Rectangle> iter = recBullets.iterator(); iter.hasNext(); ) {
            Rectangle bullets = iter.next();
            bullets.y += 200 * Gdx.graphics.getDeltaTime();


            if(bullets.overlaps((recPipe))){
                recAgua.y = recAgua.y - aguaReducida;
                balasLanzadas ++;
                iter.remove();
            }

            //Si le he lanzado mas de 4 balas al tubo pierde agua
            if(balasLanzadas > 4){
                balasLanzadas = 0;
                for(int i = 0 ; i<3 ;i++){
                    Rectangle recDrop = new Rectangle(
                            recPipe.x-i,
                            recPipe.y,
                            txDrop.getWidth(),
                            txDrop.getHeight());
                    recDrops.add(recDrop);
                }

            }
        }
    }

    public void AjustarDificultad(){

        if(puntos>10){
            tiempoRespawnGotas = 600;
            nivelDifilcultad = "2";
            aguaAumentada = 13;
            aguaReducida = 16;
        }

        if(puntos>15){
            tiempoRespawnGotas = 450;
            nivelDifilcultad = "3";
            aguaAumentada = 17;
            aguaReducida = 12;
        }

        if(puntos>20){
            tiempoRespawnGotas = 300;
            nivelDifilcultad = "4";
            aguaAumentada = 25;
            aguaReducida = 8;
        }

        if(puntos>35){
            tiempoRespawnGotas = 180;
            nivelDifilcultad = "5";
            aguaAumentada = 40;
            aguaReducida = 4;
        }
    }


    private void OffsetBorder() {
        if (recBucket.x < 0) recBucket.x = 0;
        if (recBucket.x > 800 - 64) recBucket.x = 800 - 64;
    }

    private void TouchGame() {

        //Por click en la pantalla
        if (Gdx.input.isTouched()) {
            Vector3 pos = new Vector3();
            pos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(pos);
            recBucket.x = pos.x - recBucket.width / 2;

            Rectangle colisionTubo = new Rectangle(pos.x,pos.y,recBucket.getWidth(),recBucket.getHeight());

            //Solo puede disparar una bala cada dos segundos
            if (TimeUtils.millis() - ultimoDisparo > 2000){
                //Si toco el cubo le disparo
                if(colisionTubo.overlaps(recPipe)) {
                    sndDrop.play();

                    recBalas = new Rectangle(recBucket.x,
                            recBucket.y,
                            32,
                            32);

                    recBullets.add(recBalas);
                    ultimoDisparo = TimeUtils.millis();
                }
            }
        }

        //Teclado izquierda y derecha
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            recBucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            recBucket.x += 200 * Gdx.graphics.getDeltaTime();

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        musRain.play();
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
        batch.dispose();
        txBucket.dispose();
        txDrop.dispose();
        musRain.dispose();
        sndDrop.dispose();
        txBackground.dispose();
    }
}