package com.example.nioscope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PixelGridView extends View {


//number of row and column

    int horizontalGridCount = 500;
    int verticalGridCount = 800;


    private Drawable horiz;
    private Drawable vert;
    private final float width;

    private Paint paint;

    public PixelGridView(@NonNull Context context) {
        this(context, null);
        paint = new Paint();
        // Line color
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        // Line width in pixels
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
    }

    public PixelGridView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        horiz = new ColorDrawable(Color.BLACK); horiz.setAlpha(160);
        vert = new ColorDrawable(Color.BLACK); vert.setAlpha(160);
        width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.9f, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        horiz.setBounds(left, 0, right, (int) width);
        vert.setBounds(0, top, (int) width, bottom);
    }

//
//
//
//
//    private float getLinePosition(int lineNumber) {
//        int lineCount = horizontalGridCount;
//        return (1f / (lineCount + 1)) * (lineNumber + 1f);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // drawTask.start();
        int count = horizontalGridCount;
        for (int n = 0; n <= count; n++) {

            float pos = n * (getHeight()/count);
            // Draw horizontal line
            canvas.translate(0, pos);
            horiz.draw(canvas);
            canvas.translate(0, - pos);

//            // Draw vertical line
//            canvas.translate(pos * getWidth(), 0);
//            vert.draw(canvas);
//            canvas.translate(- pos * getWidth(), 0);
        }


        int countV = verticalGridCount;
        for (int n = 0; n <= countV; n++) {
            float pos = n * (getWidth()/countV);
            // Draw vertical line
            canvas.translate(pos, 0);
            vert.draw(canvas);
            canvas.translate(- pos, 0);
        }
        drawLine(canvas);
        //drawTask.end(count);
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        // drawTask.start();
//        int count = horizontalGridCount;
//        for (int n = 0; n < count; n++) {
//            float pos = getLinePosition(n);
//
//            // Draw horizontal line
//            canvas.translate(0, pos * getHeight());
//            horiz.draw(canvas);
//            canvas.translate(0, - pos * getHeight());
//
////            // Draw vertical line
////            canvas.translate(pos * getWidth(), 0);
////            vert.draw(canvas);
////            canvas.translate(- pos * getWidth(), 0);
//        }
//
//
//        int countV = verticalGridCount;
//        for (int n = 0; n < count; n++) {
//            float pos = getLinePosition(n);
//            // Draw vertical line
//            canvas.translate(pos * getWidth(), 0);
//            vert.draw(canvas);
//            canvas.translate(- pos * getWidth(), 0);
//        }
//        //drawTask.end(count);
//    }

    public void drawLine(Canvas canvas){
        // Set a pixels value to offset the line from canvas edge
        int offset = 50;
        // Draw a line on canvas at the center position
        canvas.drawLine(
                offset, // startX
                canvas.getHeight() / 2, // startY
                canvas.getWidth() - offset, // stopX
                canvas.getHeight() / 2, // stopY
                paint // Paint
        );
    }


}
