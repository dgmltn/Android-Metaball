package com.dgmltn.metaball;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by dmelton on 9/15/16.
 */

class Circle {

    float x = 0f;
    float y = 0f;
    float radius = 1f;

    void draw(Canvas canvas, Paint paint) {
        canvas.drawCircle(x, y, radius, paint);
    }

    void draw(Canvas canvas, Paint paint, Float scale) {
        canvas.drawCircle(x, y, radius * scale, paint);
    }

    float getDistanceTo(Circle other) {
        float dx = x - other.x;
        float dy = y - other.y;
        return (float) Math.sqrt((dx * dx + dy * dy));
    }
}
