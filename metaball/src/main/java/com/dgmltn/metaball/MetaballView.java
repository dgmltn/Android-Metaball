package com.dgmltn.metaball;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

@ViewPager.DecorView
public class MetaballView extends View {

    /**
     * Radius of the fixed dots.
     */
    private float dotRadius = getResources().getDimension(R.dimen.mv_defaultRadius);
    public void setDotRadius(float radius) {
        dotRadius = radius;
    }
    public float getDotRadius() {
        return dotRadius;
    }

    /**
     * Radius of the cursor dot.
     */
    private float getCursorRadius() {
        return dotRadius * 1.4f;
    }

    /**
     * Number of fixed dots.
     */
    private int dotCount = 3;
    void setDotCount(int count) {
        dotCount = count;
        initDots();
    }
    int getDotCount() {
        return dotCount;
    }

    /**
     * Amount by which the thickness of the elastic band thins in the middle when it's stretched. 0f = no thinning.
     */
    private float bandThinning = 2f;

    /**
     * Number of pixels by which to space apart the fixed circles.
     */
    private float getSpacingPx() {
        return getCursorRadius() * 4f;
    }

    /**
     * Maximum length of the band, before it snaps
     */
    private float getBandMaxLength() {
        return getSpacingPx();
    }

    /**
     * Rate at which to scale the fixed circle due to the proximity of the cursor.
     */
    private float scaleRate = 0.3f;

    /**
     * Thickness of the elastic band (relative to the circle radius). [0f, 1f]
     */
    private float bandThickness = 0.5f;

    /**
     * Which fixed circle is currently connected to the cursor via surface tension?
     */
    private int connectedIndex = 0;
    void setConnectedIndex(int index) {
        connectedIndex = index;
        invalidate();
    }

    /**
     * The scaled x-position of the cursor. 0f = centered on the first circle,
     * 1f = centered on the second circle, etc.
     */
    void setCursorPosition(float position) {
        if (dots.size() > 0) {
            cursor.x = getSpacingPx() * position + dots.get(0).x;
        }
        invalidate();
    }

    // Current state of the circles
    private final Circle cursor = new Circle();
    private final ArrayList<Circle> dots = new ArrayList<>();

    // Cached variables used during onDraw
    private final Paint paint = new Paint();
    private final Paint paint2 = new Paint();
    private final Path path1 = new Path();

    public MetaballView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setColor(getResources().getColor(R.color.dotSelected));
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        paint2.setColor(getResources().getColor(R.color.dotUnselected));
        paint2.setStyle(Paint.Style.FILL);
        paint2.setAntiAlias(true);

        if (attrs != null) {
            final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.mv_MetaballView, 0, 0);
            paint.setColor(ta.getColor(R.styleable.mv_MetaballView_mv_selectedColor, paint.getColor()));
            paint2.setColor(ta.getColor(R.styleable.mv_MetaballView_mv_unselectedColor, paint2.getColor()));
            dotRadius = ta.getDimension(R.styleable.mv_MetaballView_mv_dotRadius, dotRadius);
            dotCount = ta.getInt(R.styleable.mv_MetaballView_mv_dotCount, dotCount);
            ta.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewParent p = getParent();
        if (p instanceof ViewPager) {
            viewPagerHelper = new ViewPagerHelper(this, (ViewPager)p);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (viewPagerHelper != null) {
            viewPagerHelper.detach();
        }
    }

    private void initDots() {
        // Initialize the cursor
        cursor.x = (getWidth() - getPaddingRight() + getPaddingLeft() - getSpacingPx() * (dotCount - 1)) / 2f;
        cursor.y = (getHeight() - getPaddingBottom() + getPaddingTop()) / 2f;
        cursor.radius = getCursorRadius();

        // Initialize/update the page dots
        while (dots.size() < dotCount) {
            dots.add(new Circle());
        }
        while (dots.size() > dotCount) {
            dots.remove(0);
        }
        for (int i = 0; i < dotCount; i++) {
            Circle c = dots.get(i);
            c.x = getSpacingPx() * i + cursor.x;
            c.y = cursor.y;
            c.radius = dotRadius;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initDots();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the page dots
        int l = dots.size();
        for (int i = 0; i < l; i++) {
            Circle c = dots.get(i);
            if (i == connectedIndex) {
                metaball(canvas, paint, c, cursor);
            }
            else {
                c.draw(canvas, paint2);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = View.resolveSizeAndState((int)(dotCount * (getCursorRadius() * 2 + getSpacingPx())), widthMeasureSpec, 0);
        int measuredHeight = View.resolveSizeAndState((int)(2f * getCursorRadius() * 1.4f), heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Main drawring method
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param canvas       canvas
     * @param paint        paint used to draw the circles and the band
     * @param fixedCircle
     * @param movingCircle
     */
    private void metaball(Canvas canvas, Paint paint, @NonNull Circle fixedCircle, @NonNull Circle movingCircle) {

        float d = movingCircle.getDistanceTo(fixedCircle);

        float radius1 = movingCircle.radius;
        float radius2 = fixedCircle.radius;
        float scale2 = 1f;

        //        Log.d("Metaball_radius", "radius1:" + radius1 + ",radius2:" + radius2);
        if (radius1 == 0f || radius2 == 0f) {
            return;
        }

        // Scale up the fixed circle if they're close together
        if (d <= getBandMaxLength()) {
            scale2 = 1 + scaleRate * (1 - d / getBandMaxLength());
            radius2 *= scale2;
        }

        // Draw cursor
        movingCircle.draw(canvas, paint);

        // No need to draw the fixed circle if it's overlapped by the cursor
        if (d <= Math.abs(radius1 - radius2)) {
            return;
        }

        // Draw fixed circle
        fixedCircle.draw(canvas, paint, scale2);

        // Nothing else to do if the circles are too far apart to draw the band
        if (d > getBandMaxLength()) {
            return;
        }

        float u1;
        float u2;

        if (d < radius1 + radius2) {
            u1 = (float) Math.acos(((radius1 * radius1 + d * d - radius2 * radius2) / (2f * radius1 * d)));
            u2 = (float) Math.acos(((radius2 * radius2 + d * d - radius1 * radius1) / (2f * radius2 * d)));
        }
        else {
            u1 = 0f;
            u2 = 0f;
        }

        //        Log.d("Metaball", "center2:" + Arrays.toString(center2) + ",center1:" + Arrays.toString(center1));
        float[] centermin = new float[] { fixedCircle.x - movingCircle.x, fixedCircle.y - movingCircle.y };

        float angle1 = (float) Math.atan2(centermin[1], centermin[0]);
        float angle2 = (float) Math.acos((radius1 - radius2) / d);
        float angle1a = angle1 + u1 + (angle2 - u1) * bandThickness;
        float angle1b = angle1 - u1 - (angle2 - u1) * bandThickness;
        float angle2a = (float) (angle1 + Math.PI - u2 - (Math.PI - u2 - angle2) * bandThickness);
        float angle2b = (float) (angle1 - Math.PI + u2 + (Math.PI - u2 - angle2) * bandThickness);

        //        Log.d("Metaball", "angle1:" + angle1 + ",angle2:" + angle2 + ",angle1a:" + angle1a + ",angle1b:" + angle1b + ",angle2a:" + angle2a + ",angle2b:" + angle2b);


        float[] p1a1 = getVector(angle1a, radius1);
        float[] p1b1 = getVector(angle1b, radius1);
        float[] p2a1 = getVector(angle2a, radius2);
        float[] p2b1 = getVector(angle2b, radius2);

        float[] p1a = new float[] { p1a1[0] + movingCircle.x, p1a1[1] + movingCircle.y };
        float[] p1b = new float[] { p1b1[0] + movingCircle.x, p1b1[1] + movingCircle.y };
        float[] p2a = new float[] { p2a1[0] + fixedCircle.x, p2a1[1] + fixedCircle.y };
        float[] p2b = new float[] { p2b1[0] + fixedCircle.x, p2b1[1] + fixedCircle.y };


        //        Log.d("Metaball", "p1a:" + Arrays.toString(p1a) + ",p1b:" + Arrays.toString(p1b) + ",p2a:" + Arrays.toString(p2a) + ",p2b:" + Arrays.toString(p2b));

        float[] p1_p2 = new float[] { p1a[0] - p2a[0], p1a[1] - p2a[1] };

        float totalRadius = radius1 + radius2;
        float d2 = Math.min(bandThickness * bandThinning, getLength(p1_p2) / totalRadius);
        d2 *= Math.min(1f, d * 2 / (radius1 + radius2));
        //        Log.d("Metaball", "d2:" + d2);
        radius1 *= d2;
        radius2 *= d2;

        // The points along the edge of the circles from which to draw the elastic band
        float[] sp1 = getVector(angle1a - PI2, radius1);
        float[] sp2 = getVector(angle2a + PI2, radius2);
        float[] sp3 = getVector(angle2b - PI2, radius2);
        float[] sp4 = getVector(angle1b + PI2, radius1);
        //        Log.d("Metaball", "sp1:" + Arrays.toString(sp1) + ",sp2:" + Arrays.toString(sp2) + ",sp3:" + Arrays.toString(sp3) + ",sp4:" + Arrays.toString(sp4));


        path1.reset();
        path1.moveTo(p1a[0], p1a[1]);
        path1.cubicTo(p1a[0] + sp1[0], p1a[1] + sp1[1], p2a[0] + sp2[0], p2a[1] + sp2[1], p2a[0], p2a[1]);
        path1.lineTo(p2b[0], p2b[1]);
        path1.cubicTo(p2b[0] + sp3[0], p2b[1] + sp3[1], p1b[0] + sp4[0], p1b[1] + sp4[1], p1b[0], p1b[1]);
        path1.lineTo(p1a[0], p1a[1]);
        path1.close();
        canvas.drawPath(path1, paint);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static helpers
    ///////////////////////////////////////////////////////////////////////////

    private static final float PI2 = (float) (Math.PI / 2.0f);

    private static Float getLength(float[] b) {
        return (float) Math.sqrt((b[0] * b[0] + b[1] * b[1]));
    }

    private static float[] getVector(Float radians, Float length) {
        float x = (float) (Math.cos(radians) * length);
        float y = (float) (Math.sin(radians) * length);
        return new float[] {x, y};
    }

    ///////////////////////////////////////////////////////////////////////////
    // ViewPager helper
    ///////////////////////////////////////////////////////////////////////////

    private ViewPagerHelper viewPagerHelper = null;

}
