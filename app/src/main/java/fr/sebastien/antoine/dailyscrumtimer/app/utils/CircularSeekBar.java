/**
 * @author Raghav Sood
 * @version 1
 * @date 26 January, 2013
 */
package fr.sebastien.antoine.dailyscrumtimer.app.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import fr.sebastien.antoine.dailyscrumtimer.app.R;


/**
 * The Class CircularSeekBar.
 */
public class CircularSeekBar extends View {

    /**
     * The context
     */
    private Context mContext;

    /**
     * The listener to listen for changes
     */
    private OnSeekChangeListener mListener;

    /**
     * The color of the progress ring
     */
    private Paint circleProgressColor;

    /**
     * the color of the inside circle. Acts as background color
     */
    private Paint transparentColorFill;

    /**
     * the color of the inside circle. Acts as background color
     */
    private Paint transparentColorStroke;

    /**
     * color of background
     */
    private Paint backgroundColor;

    /**
     * The progress circle ring background
     */
    private Paint circleRing;

    /**
     * The angle of progress
     */
    private int angle = 0;

    /**
     * The start angle (12 O'clock
     */
    private int startAngle = 270;

    /**
     * The width of the progress ring
     */
    private int barWidth = 4;

    /**
     * The width of the view
     */
    private int width;

    /**
     * The height of the view
     */
    private int height;

    /**
     * Padding of the view
     */
    private int padding;

    /**
     * The maximum progress amount
     */
    private int maxProgress = 100;

    /**
     * The current progress
     */
    private int progress;

    /**
     * The progress percent
     */
    private int progressPercent;

    /**
     * The radius of the inner circle
     */
    private float innerRadius;

    /**
     * The radius of the outer circle
     */
    private float outerRadius;

    /**
     * Size of Halo
     */
    private int haloSize;

    /**
     * The circle's center X coordinate
     */
    private float cx;

    /**
     * The circle's center Y coordinate
     */
    private float cy;

    /**
     * The left bound for the circle RectF
     */
    private float left;

    /**
     * The right bound for the circle RectF
     */
    private float right;

    /**
     * The top bound for the circle RectF
     */
    private float top;

    /**
     * The bottom bound for the circle RectF
     */
    private float bottom;

    /**
     * The X coordinate for the top left corner of the marking drawable
     */
    private float dx;

    /**
     * The Y coordinate for the top left corner of the marking drawable
     */
    private float dy;

    /**
     * The X coordinate for 12 O'Clock
     */
    private float startPointX;

    /**
     * The Y coordinate for 12 O'Clock
     */
    private float startPointY;

    /**
     * The X coordinate for the current position of the marker, pre adjustment
     * to center
     */
    private float markPointX;

    /**
     * The Y coordinate for the current position of the marker, pre adjustment
     * to center
     */
    private float markPointY;

    /**
     * Size of the view
     */
    private int size;

    /**
     * The adjustment factor. This adds an adjustment of the specified size to
     * both sides of the progress bar, allowing touch events to be processed
     * more user friendlily (yes, I know that's not a word)
     */
    private float adjustmentFactor = 100;

    /**
     * The progress mark when the view isn't being progress modified
     */
    private Bitmap progressMark;

    /**
     * The progress mark when the view is being progress modified.
     */
    private Bitmap progressMarkPressed;

    /**
     * The flag to see if view is pressed
     */
    private boolean IS_PRESSED = false;

    /**
     * The flag to see if the setProgress() method was called from our own
     * View's setAngle() method, or externally by a user.
     */
    private boolean CALLED_FROM_ANGLE = false;

    private RectF rectMask = new RectF();

    /**
     * The rectangle containing our circles and arcs.
     */
    private RectF rect = new RectF();
    {
        mListener = new OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress) {
            }
        };

        haloSize = 30;

        circleProgressColor = new Paint(); // Progress paint
        circleProgressColor.setColor(getContext().getResources().getColor(R.color.green_light)); // Set default progress color to blue valtech
      //  circleProgressColor.setShadowLayer(haloSize, 2, 2, getContext().getResources().getColor(R.color.green_highlight)); // Halo
        circleProgressColor.setAntiAlias(true);
        circleProgressColor.setStyle(Paint.Style.STROKE);
        circleProgressColor.setStrokeWidth(barWidth);
        circleProgressColor.setDither(true);

        circleRing = new Paint();
        circleRing.setColor(Color.GRAY);// Set default background color to Gray
        circleRing.setAntiAlias(true);
        circleRing.setStyle(Paint.Style.STROKE);
        circleRing.setDither(true);

        transparentColorFill = new Paint(); // clear circle in
        transparentColorFill.setColor(Color.TRANSPARENT); // Set default background color to black transparent
        transparentColorFill.setAlpha(0xFF);
        transparentColorFill.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentColorFill.setAntiAlias(true);
        transparentColorFill.setStyle(Paint.Style.FILL);
        transparentColorFill.setStrokeWidth(barWidth);
        transparentColorFill.setDither(true);

        transparentColorStroke = new Paint(); // clear circle in
        transparentColorStroke.setColor(Color.TRANSPARENT); // Set default background color to black transparent
        transparentColorStroke.setAlpha(0xFF);
        transparentColorStroke.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentColorStroke.setAntiAlias(true);
        transparentColorStroke.setStyle(Paint.Style.STROKE);
        transparentColorStroke.setStrokeWidth(barWidth + 5);
        transparentColorStroke.setDither(true);

        backgroundColor = new Paint(); // Paint background color for the transparent inner circle
        backgroundColor.setAntiAlias(true);
        backgroundColor.setColor(Color.parseColor("#33000000"));
        backgroundColor.setStyle(Paint.Style.FILL);
        backgroundColor.setDither(true);
    }

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public CircularSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initDrawable();
    }

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CircularSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initDrawable();
    }

    /**
     * Instantiates a new circular seek bar.
     *
     * @param context the context
     */
    public CircularSeekBar(Context context) {
        super(context);
        mContext = context;
        initDrawable();
    }

    /**
     * Inits the drawable.
     */
    public void initDrawable() {
        progressMark = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.controler_normal);
        progressMarkPressed = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.controler_normal);

        padding = (int) mContext.getResources().getDimension(R.dimen.padding_slider);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = getWidth(); // Get View Width
        height = getHeight(); // Get View Height

        size = (width > height) ? height : width; // Choose the smaller between width and height to make a square

        cx = width / 2; // Center X for circle
        cy = height / 2; // Center Y for circle
        outerRadius = size / 2 - padding; // Radius of the outer circle (ajout : - padding)

        startPointX = cx; // 12 O'clock X coordinate
        startPointY = cy - outerRadius; // 12 O'clock Y coordinate
        if (angle == 0) {
            markPointX = startPointX; // Initial location of the marker X coordinate
            markPointY = startPointY; // Initial location of the marker Y coordinate
        }

        innerRadius = outerRadius - barWidth; // Radius of the inner circle

        // dimensions of the rect
        left = cx - outerRadius; // Calculate left bound of our rect
        right = cx + outerRadius; // Calculate right bound of our rect
        top = cy - outerRadius; // Calculate top bound of our rect
        bottom = cy + outerRadius; // Calculate bottom bound of our rect

        rect.set(left, top, right, bottom); // assign size to rect

        float leftMask = cx - size / 2; // Calculate left bound of our rect
        float rightMask = cx + size / 2; // Calculate right bound of our rect
        float topMask = cy - size / 2; // Calculate top bound of our rect
        float bottomMask = cy + size / 2; // Calculate bottom bound of our rect
        rectMask.set(leftMask, topMask, rightMask, bottomMask);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        dx = getXFromAngle();
        dy = getYFromAngle();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null); // put the View in a software layer

        // Progress
        canvas.drawArc(rect, startAngle, angle, false, circleProgressColor);
 //       canvas.drawArc(rectMask, -90 + angle, 360 - angle, true, transparentColorFill);
  //      canvas.drawCircle(cx, cy, outerRadius + barWidth + 1, transparentColorStroke);

        // Circle inside
     //   canvas.drawCircle(cx, cy, innerRadius, transparentColorFill);
        canvas.drawCircle(cx, cy, innerRadius, backgroundColor);

        drawMarkerAtProgress(canvas);

        super.onDraw(canvas);
    }

    /**
     * Draw marker at the current progress point onto the given canvas.
     *
     * @param canvas the canvas
     */
    public void drawMarkerAtProgress(Canvas canvas) {
        if (IS_PRESSED) {
            canvas.drawBitmap(progressMarkPressed, dx, dy, null);
        } else {
            canvas.drawBitmap(progressMark, dx, dy, null);
        }
    }

    /**
     * Gets the X coordinate of the arc's end arm's point of intersection with
     * the circle
     *
     * @return the X coordinate
     */
    public float getXFromAngle() {
        int size1 = progressMark.getWidth();
        int size2 = progressMarkPressed.getWidth();
        int adjust = (size1 > size2) ? size1 : size2;
        float x = markPointX - (adjust / 2);
        return x;
    }

    /**
     * Gets the Y coordinate of the arc's end arm's point of intersection with
     * the circle
     *
     * @return the Y coordinate
     */
    public float getYFromAngle() {
        int size1 = progressMark.getHeight();
        int size2 = progressMarkPressed.getHeight();
        int adjust = (size1 > size2) ? size1 : size2;
        float y = markPointY - (adjust / 2);
        return y;
    }

    /**
     * Get the angle.
     *
     * @return the angle
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Set the angle.
     *
     * @param angle the new angle
     */
    public void setAngle(int angle) {
        this.angle = angle;
        float donePercent = (((float) this.angle) / 360) * 100;
        float progress = (donePercent / 100) * getMaxProgress();
        setProgressPercent(Math.round(donePercent));
        CALLED_FROM_ANGLE = true;
        setProgress(Math.round(progress));
    }

    /**
     * Sets the seek bar change listener.
     *
     * @param listener the new seek bar change listener
     */
    public void setSeekBarChangeListener(OnSeekChangeListener listener) {
        mListener = listener;
    }

    /**
     * Gets the seek bar change listener.
     *
     * @return the seek bar change listener
     */
    public OnSeekChangeListener getSeekBarChangeListener() {
        return mListener;
    }

    /**
     * Gets the bar width.
     *
     * @return the bar width
     */
    public int getBarWidth() {
        return barWidth;
    }

    /**
     * Sets the bar width.
     *
     * @param barWidth the new bar width
     */
    public void setBarWidth(int barWidth) {
        this.barWidth = barWidth;
    }

    /**
     * The listener interface for receiving onSeekChange events. The class that
     * is interested in processing a onSeekChange event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>setSeekBarChangeListener(OnSeekChangeListener)<code> method. When
     * the onSeekChange event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnSeekChangeEvent
     */
    public interface OnSeekChangeListener {
        /**
         * On progress change.
         *
         * @param view        the view
         * @param newProgress the new progress
         */
        public void onProgressChange(CircularSeekBar view, int newProgress);
    }

    /**
     * Gets the max progress.
     *
     * @return the max progress
     */
    public int getMaxProgress() {
        return maxProgress;
    }

    /**
     * Sets the max progress.
     *
     * @param maxProgress the new max progress
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * Gets the progress.
     *
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(int progress) {
        if (this.progress != progress) {
            this.progress = progress;
            if (!CALLED_FROM_ANGLE) {
                int newPercent = (this.progress * 100) / this.maxProgress;
                int newAngle = (newPercent * 360) / 100;
                this.setAngle(newAngle);
                this.setProgressPercent(newPercent);
            }
            mListener.onProgressChange(this, this.getProgress());
            CALLED_FROM_ANGLE = false;
        }
    }

    /**
     * Gets the progress percent.
     *
     * @return the progress percent
     */
    public int getProgressPercent() {
        return progressPercent;
    }

    /**
     * Sets the progress percent.
     *
     * @param progressPercent the new progress percent
     */
    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    /**
     * Sets the ring background color.
     *
     * @param color the new ring background color
     */
    public void setRingBackgroundColor(int color) {
        circleRing.setColor(color);
    }

    /**
     * Sets the back ground color.
     *
     * @param color the new back ground color
     */
    public void setBackGroundColor(int color) {
        transparentColorFill.setColor(color);
    }

    /**
     * Sets the progress color.
     *
     * @param color the new progress color
     */
    public void setProgressColor(int color) {
        circleProgressColor.setColor(color);
        circleProgressColor.setShadowLayer(haloSize, 2, 2, color);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean up = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moved(x, y, up);
                break;
            case MotionEvent.ACTION_MOVE:
                moved(x, y, up);
                break;
            case MotionEvent.ACTION_UP:
                up = true;
                moved(x, y, up);
                break;
        }
        return true;
    }

    /**
     * Moved.
     *
     * @param x  the x
     * @param y  the y
     * @param up the up
     */
    private void moved(float x, float y, boolean up) {
        float distance = (float) Math.sqrt(Math.pow((x - cx), 2) + Math.pow((y - cy), 2));
        if (distance < outerRadius + adjustmentFactor && distance > innerRadius - adjustmentFactor && !up) {
            IS_PRESSED = true;

            markPointX = (float) (cx + outerRadius * Math.cos(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));
            markPointY = (float) (cy + outerRadius * Math.sin(Math.atan2(x - cx, cy - y) - (Math.PI / 2)));

            float degrees = (float) ((float) ((Math.toDegrees(Math.atan2(x - cx, cy - y)) + 360.0)) % 360.0);
            // and to make it count 0-360
            if (degrees < 0) {
                degrees += 2 * Math.PI;
            }

            setAngle(Math.round(degrees));
            invalidate();
        } else {
            IS_PRESSED = false;
            invalidate();
        }
    }

    /**
     * Gets the adjustment factor.
     *
     * @return the adjustment factor
     */
    public float getAdjustmentFactor() {
        return adjustmentFactor;
    }

    /**
     * Sets the adjustment factor.
     *
     * @param adjustmentFactor the new adjustment factor
     */
    public void setAdjustmentFactor(float adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public float getCx() {
        return cx;
    }

    public void setCx(float cx) {
        this.cx = cx;
    }

    public float getCy() {
        return cy;
    }

    public void setCy(float cy) {
        this.cy = cy;
    }
}