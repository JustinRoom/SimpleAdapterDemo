package jsc.kit.adapter.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/16 17:22
 *
 * @author jsc
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int left;
    private int top;
    private int right;
    private int bottom;
    private boolean showFirstLeft = false;
    private boolean showFirstTop = false;
    private boolean showLastRight = false;
    private boolean showLastBottom = false;

    public SpaceItemDecoration(int space) {
        this(space, space, space, space);
    }

    public SpaceItemDecoration(int lr, int tb) {
        this(lr, tb, lr, tb);
    }

    public SpaceItemDecoration(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public SpaceItemDecoration showFirstLeft(boolean show) {
        showFirstLeft = show;
        return this;
    }

    public SpaceItemDecoration showFirstTop(boolean show) {
        showFirstTop = show;
        return this;
    }

    public SpaceItemDecoration showLastRight(boolean show) {
        showLastRight = show;
        return this;
    }

    public SpaceItemDecoration showLastBottom(boolean show) {
        showLastBottom = show;
        return this;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.Adapter adapter = parent.getAdapter();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (null == adapter || null == layoutManager || adapter.getItemCount() == 0)
            return;

        int viewIndex = parent.getChildAdapterPosition(view);
        if (layoutManager instanceof GridLayoutManager) {
            outRect.set(left, top, right, bottom);
            int spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            int rowCount = adapter.getItemCount() / spanCount;
            if (adapter.getItemCount() % spanCount > 0)
                rowCount++;

            int rowIndex = viewIndex / spanCount;
            int columnIndex = viewIndex % spanCount;

            if (!showFirstTop && rowIndex == 0)
                outRect.top = 0;
            if (!showLastBottom && rowIndex == rowCount - 1)
                outRect.bottom = 0;
            if (!showFirstLeft && columnIndex == 0)
                outRect.left = 0;
            if (!showLastRight && columnIndex == spanCount - 1)
                outRect.right = 0;
        } else if (layoutManager instanceof LinearLayoutManager) {
            outRect.set(left, top, right, bottom);
            switch (((LinearLayoutManager) layoutManager).getOrientation()) {
                case LinearLayoutManager.HORIZONTAL:
                    if (!showFirstLeft && viewIndex == 0)
                        outRect.left = 0;
                    if (!showLastRight && viewIndex == adapter.getItemCount() - 1)
                        outRect.right = 0;
                    break;
                case LinearLayoutManager.VERTICAL:
                    if (!showFirstTop && viewIndex == 0)
                        outRect.top = 0;
                    if (!showLastBottom && viewIndex == adapter.getItemCount() - 1)
                        outRect.bottom = 0;
                    break;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {

        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }
}
