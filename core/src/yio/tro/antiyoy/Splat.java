package yio.tro.antiyoy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 小球
 * Created by yiotro on 13.08.2014.
 */
public class Splat {

    final TextureRegion textureRegion;
    float x, y,
    //    x速度
    dx,
    //    y速度
    dy,
    //  风的加速度
    wind,
    //半径
    r,
    //   速度常数
    speedMultiplier;


    public Splat(TextureRegion textureRegion, float x, float y) {
        this.textureRegion = textureRegion;
        this.x = x;
        this.y = y;
    }


    void move() {
        x += dx;
        y += dy * speedMultiplier;
        dx += wind;
        if (Math.abs(dx) > 0.001f * Gdx.graphics.getWidth()) wind = -wind;
    }


    void set(float x, float y) {
        this.x = x;
        this.y = y;
    }


    void setSpeed(float sdx, float sdy) {
        dx = sdx;
        dy = sdy;
        wind = -0.01f * dx;
    }


    public void setRadius(float r) {
        this.r = r;
        speedMultiplier = (0.05f * Gdx.graphics.getHeight()) / r;
        //        speedMultiplier = (float)Math.sqrt(speedMultiplier);
    }


    boolean isVisible() {
        return y < Gdx.graphics.getHeight() + r;
    }
}
