package jsc.exam.com.adapter.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class FiveTwoImageView extends AppCompatImageView {
    public FiveTwoImageView(Context context) {
        super(context);
    }

    public FiveTwoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FiveTwoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int width = getMeasuredWidth();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(width * 2 / 5, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
