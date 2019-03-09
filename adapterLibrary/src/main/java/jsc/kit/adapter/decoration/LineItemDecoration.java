package jsc.kit.adapter.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class LineItemDecoration extends RecyclerView.ItemDecoration {

    private Paint paint;
    private Rect rect = new Rect();
    private int leftMargin;
    private int rightMargin;
    private int lineHeight;

    public LineItemDecoration() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
    }

    public LineItemDecoration setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public LineItemDecoration setLineColor(@ColorInt int lineColor) {
        paint.setColor(lineColor);
        return this;
    }

    public LineItemDecoration setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        return this;
    }

    public LineItemDecoration setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
        return this;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            rect.set(leftMargin, child.getBottom(), child.getRight() - rightMargin, child.getBottom() + lineHeight);
            c.drawRect(rect, paint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = lineHeight;
    }
}