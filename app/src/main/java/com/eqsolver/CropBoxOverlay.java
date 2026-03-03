package com.eqsolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that draws a crop box overlay on the camera preview.
 * Shows a clear rectangle where the equation should be positioned,
 * with dimmed areas outside the box.
 */
public class CropBoxOverlay extends View {

    private Paint boxPaint;
    private Paint dimPaint;
    private Paint clearPaint;
    private RectF cropRect;

    public CropBoxOverlay(Context context) {
        super(context);
        init();
    }

    public CropBoxOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CropBoxOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Paint for the crop box border
        boxPaint = new Paint();
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4f);
        boxPaint.setAntiAlias(true);

        // Paint for dimming the area outside the box
        dimPaint = new Paint();
        dimPaint.setColor(Color.BLACK);
        dimPaint.setAlpha(120); // Semi-transparent

        // Paint for clearing the area inside the box
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        cropRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            return;
        }

        // Calculate crop box dimensions (centered, 80% width, 30% height)
        float boxWidth = width * 0.8f;
        float boxHeight = height * 0.3f;
        float left = (width - boxWidth) / 2f;
        float top = (height - boxHeight) / 2f;

        cropRect.set(left, top, left + boxWidth, top + boxHeight);

        // Draw semi-transparent overlay over entire view
        canvas.drawRect(0, 0, width, height, dimPaint);

        // Clear the area inside the crop box
        canvas.drawRect(cropRect, clearPaint);

        // Draw the crop box border
        canvas.drawRoundRect(cropRect, 12f, 12f, boxPaint);

        // Draw corner brackets for better visibility
        drawCornerBrackets(canvas, cropRect);
    }

    /**
     * Draws corner brackets around the crop box for better visual feedback.
     */
    private void drawCornerBrackets(Canvas canvas, RectF rect) {
        Paint bracketPaint = new Paint();
        bracketPaint.setColor(Color.WHITE);
        bracketPaint.setStyle(Paint.Style.STROKE);
        bracketPaint.setStrokeWidth(6f);
        bracketPaint.setStrokeCap(Paint.Cap.ROUND);
        bracketPaint.setAntiAlias(true);

        float bracketLength = 40f;

        // Top-left corner
        canvas.drawLine(rect.left, rect.top + bracketLength, rect.left, rect.top, bracketPaint);
        canvas.drawLine(rect.left, rect.top, rect.left + bracketLength, rect.top, bracketPaint);

        // Top-right corner
        canvas.drawLine(rect.right - bracketLength, rect.top, rect.right, rect.top, bracketPaint);
        canvas.drawLine(rect.right, rect.top, rect.right, rect.top + bracketLength, bracketPaint);

        // Bottom-left corner
        canvas.drawLine(rect.left, rect.bottom - bracketLength, rect.left, rect.bottom, bracketPaint);
        canvas.drawLine(rect.left, rect.bottom, rect.left + bracketLength, rect.bottom, bracketPaint);

        // Bottom-right corner
        canvas.drawLine(rect.right - bracketLength, rect.bottom, rect.right, rect.bottom, bracketPaint);
        canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom - bracketLength, bracketPaint);
    }

    /**
     * Returns the crop rectangle in view coordinates.
     */
    public RectF getCropRect() {
        return new RectF(cropRect);
    }

    /**
     * Gets the crop region as percentages of the view dimensions.
     * Returns [leftPercent, topPercent, widthPercent, heightPercent]
     */
    public float[] getCropRegionPercent() {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0) {
            return new float[]{0.1f, 0.35f, 0.8f, 0.3f};
        }

        float leftPercent = cropRect.left / width;
        float topPercent = cropRect.top / height;
        float widthPercent = cropRect.width() / width;
        float heightPercent = cropRect.height() / height;

        return new float[]{leftPercent, topPercent, widthPercent, heightPercent};
    }
}
