package jsc.kit.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/22 21:57 Saturday
 *
 * @author jsc
 */
public abstract class BaseHeaderFooterAdapter<H, D, F, E, VH extends BaseHeaderFooterAdapter.BaseViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * header 视图
     */
    public static final int TYPE_HEADER = 1000;
    /**
     * data 数据视图
     */
    public static final int TYPE_DATA = 1001;
    /**
     * footer 视图
     */
    public static final int TYPE_FOOTER = 1002;
    /**
     * 空列表视图
     */
    public static final int TYPE_EMPTY = 1003;
    /**
     * 未知视图
     */
    public static final int UNKNOWN = 1999;

    @IntDef({TYPE_HEADER, TYPE_DATA, TYPE_FOOTER, TYPE_EMPTY, UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewType {
    }

    private RecyclerView recyclerView;
    private int headerLayoutId = -1;
    private int dataLayoutId = -1;
    private int footerLayoutId = -1;
    private int emptyLayoutId = -1;
    private List<H> headers = new ArrayList<>();
    private List<D> data = new ArrayList<>();
    private List<F> footers = new ArrayList<>();
    private List<E> empties = new ArrayList<>();
    private OnCreateViewHolderListener onCreateViewHolderListener = null;
    private OnItemClickListener<H, D, F, E> onItemClickListener = null;
    private OnItemLongClickListener<H, D, F, E> onItemLongClickListener = null;
    private OnItemChildClickListener<H, D, F, E> onItemChildClickListener = null;
    private OnItemChildLongClickListener<H, D, F, E> onItemChildLongClickListener = null;
    private View.OnClickListener defaultItemClickListener = null;
    private View.OnLongClickListener defaultItemLongClickListener = null;
    private View.OnClickListener defaultChildClickListener = null;
    private View.OnLongClickListener defaultChildLongClickListener = null;

    private View.OnClickListener getDefaultItemClickListener() {
        if (defaultItemClickListener == null)
            defaultItemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ensureBoundedRecyclerView();
                    int position = recyclerView.getChildAdapterPosition(v) - getCustomHeaderSize();
                    int viewType = getItemViewType(position);
                    switch (viewType) {
                        case TYPE_HEADER:
                            if (getOnItemClickListener() != null)
                                getOnItemClickListener().onHeaderItemClick(v, position, getHeaderAt(position));
                            break;
                        case TYPE_DATA:
                            if (getOnItemClickListener() != null)
                                getOnItemClickListener().onDataItemClick(v, position, getDataAt(position));
                            break;
                        case TYPE_FOOTER:
                            if (getOnItemClickListener() != null)
                                getOnItemClickListener().onFooterItemClick(v, position, getFooterAt(position));
                            break;
                        case TYPE_EMPTY:
                            if (getOnItemClickListener() != null)
                                getOnItemClickListener().onEmptyItemClick(v, position, getEmptyAt(position));
                            break;
                        case UNKNOWN:
                            break;
                    }
                }
            };
        return defaultItemClickListener;
    }

    private View.OnLongClickListener getDefaultItemLongClickListener() {
        if (defaultItemLongClickListener == null)
            defaultItemLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ensureBoundedRecyclerView();
                    int position = recyclerView.getChildAdapterPosition(v) - getCustomHeaderSize();
                    int viewType = getItemViewType(position);
                    switch (viewType) {
                        case TYPE_HEADER:
                            return getOnItemLongClickListener() != null && getOnItemLongClickListener().onHeaderItemLongClick(v, position, getHeaderAt(position));
                        case TYPE_DATA:
                            return getOnItemLongClickListener() != null && getOnItemLongClickListener().onDataItemLongClick(v, position, getDataAt(position));
                        case TYPE_FOOTER:
                            return getOnItemLongClickListener() != null && getOnItemLongClickListener().onFooterItemLongClick(v, position, getFooterAt(position));
                        case TYPE_EMPTY:
                            return getOnItemLongClickListener() != null && getOnItemLongClickListener().onEmptyItemLongClick(v, position, getEmptyAt(position));
                        case UNKNOWN:
                        default:
                            return false;
                    }
                }
            };
        return defaultItemLongClickListener;
    }

    private View.OnClickListener getDefaultChildClickListener() {
        if (defaultChildClickListener == null)
            defaultChildClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ensureBoundedRecyclerView();
                    View itemView = recyclerView.findContainingItemView(v);
                    int position = itemView == null ? -1 : recyclerView.getChildAdapterPosition(itemView) - getCustomHeaderSize();
                    int viewType = getItemViewType(position);
                    switch (viewType) {
                        case TYPE_HEADER:
                            if (getOnItemChildClickListener() != null)
                                getOnItemChildClickListener().onHeaderItemChildClick(v, position, getHeaderAt(position));
                            break;
                        case TYPE_DATA:
                            if (getOnItemChildClickListener() != null)
                                getOnItemChildClickListener().onDataItemChildClick(v, position, getDataAt(position));
                            break;
                        case TYPE_FOOTER:
                            if (getOnItemChildClickListener() != null)
                                getOnItemChildClickListener().onFooterItemChildClick(v, position, getFooterAt(position));
                            break;
                        case TYPE_EMPTY:
                            if (getOnItemChildClickListener() != null)
                                getOnItemChildClickListener().onEmptyItemChildClick(v, position, getEmptyAt(position));
                            break;
                        case UNKNOWN:
                            break;
                    }
                }
            };
        return defaultChildClickListener;
    }

    private View.OnLongClickListener getDefaultChildLongClickListener() {
        if (defaultChildLongClickListener == null)
            defaultChildLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ensureBoundedRecyclerView();
                    View itemView = recyclerView.findContainingItemView(v);
                    int position = itemView == null ? -1 : recyclerView.getChildAdapterPosition(itemView) - getCustomHeaderSize();
                    int viewType = getItemViewType(position);
                    switch (viewType) {
                        case TYPE_HEADER:
                            return getOnItemChildLongClickListener() != null && getOnItemChildLongClickListener().onHeaderItemChildLongClick(v, position, getHeaderAt(position));
                        case TYPE_DATA:
                            return getOnItemChildLongClickListener() != null && getOnItemChildLongClickListener().onDataItemChildLongClick(v, position, getDataAt(position));
                        case TYPE_FOOTER:
                            return getOnItemChildLongClickListener() != null && getOnItemChildLongClickListener().onFooterItemChildLongClick(v, position, getFooterAt(position));
                        case TYPE_EMPTY:
                            return getOnItemChildLongClickListener() != null && getOnItemChildLongClickListener().onEmptyItemChildLongClick(v, position, getEmptyAt(position));
                        case UNKNOWN:
                        default:
                            return false;
                    }
                }
            };
        return defaultChildLongClickListener;
    }

    //constructor

    public BaseHeaderFooterAdapter() {
    }

    public BaseHeaderFooterAdapter(int dataLayoutId) {
        setDataLayoutId(dataLayoutId);
    }


    //header

    public List<H> getHeaders() {
        return headers;
    }

    public int getHeaderSize() {
        return headers.size();
    }

    public int toHeaderIndex(int position) {
        return position;
    }

    public int toHeaderPosition(int index) {
        return index;
    }

    @Nullable
    public H getHeaderAt(int position) {
        if (position < 0 || position >= getHeaderSize())
            return null;
        return headers.get(position);
    }

    public void setHeaders(List<H> headers) {
        this.headers.clear();
        if (headers != null && !headers.isEmpty())
            this.headers.addAll(headers);
        notifyDataSetChanged();
    }

    public void addHeaders(List<H> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.headers.addAll(headers);
            notifyDataSetChanged();
        }
    }

    public void addHeader(H header) {
        if (header != null) {
            headers.add(header);
            notifyItemInserted(toCustomPosition(getHeaderSize()));
        }
    }

    public void addHeader(int index, H header) {
        if (header != null) {
            headers.add(index, header);
            notifyItemInserted(toCustomPosition(toHeaderPosition(index)));
        }
    }

    public void clearHeaders() {
        if (!headers.isEmpty()) {
            headers.clear();
            notifyDataSetChanged();
        }
    }

    public void removeHeader(int index) {
        if (index < 0 || index >= getHeaderSize())
            return;
        int pos = toHeaderPosition(index);
        headers.remove(index);
        notifyItemRemoved(toCustomPosition(pos));
    }

    public void updateHeader(int index, H header) {
        if (index < 0 || index >= getHeaderSize())
            return;
        if (header != null)
            headers.set(index, header);
        notifyItemChanged(toCustomPosition(toHeaderPosition(index)));
    }

    //data

    public List<D> getData() {
        return data;
    }

    public int getDataSize() {
        return data.size();
    }

    public int toDataIndex(int position) {
        return position - getHeaderSize();
    }

    public int toDataPosition(int index) {
        return getHeaderSize() + index;
    }

    @Nullable
    public D getDataAt(int position) {
        int index = toDataIndex(position);
        if (index < 0 || index >= getDataSize())
            return null;
        return data.get(index);
    }

    public void setData(List<D> data) {
        this.data.clear();
        if (data != null && !data.isEmpty())
            this.data.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(List<D> data) {
        if (data != null && !data.isEmpty()) {
            this.data.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void addData(D data) {
        if (data != null) {
            this.data.add(data);
            notifyItemInserted(toCustomPosition(getHeaderSize() + getDataSize()));
        }
    }

    public void addData(int index, D data) {
        if (data != null) {
            this.data.add(index, data);
            notifyItemInserted(toCustomPosition(toDataPosition(index)));
        }
    }

    public void clearData() {
        if (!data.isEmpty()) {
            data.clear();
            notifyDataSetChanged();
        }
    }

    public void removeData(int index) {
        if (index < 0 || index >= getDataSize())
            return;
        int pos = toDataPosition(index);
        data.remove(index);
        notifyItemRemoved(toCustomPosition(pos));
    }

    public void updateData(int index, D data) {
        if (index < 0 || index >= getDataSize())
            return;
        if (data != null)
            this.data.set(index, data);
        notifyItemChanged(toCustomPosition(toDataPosition(index)));
    }

    //footer

    public List<F> getFooters() {
        return footers;
    }

    public int getFooterSize() {
        return footers.size();
    }

    public int toFooterIndex(int position) {
        return position - getHeaderSize() - getInternalSize();
    }

    public int toFooterPosition(int index) {
        return getHeaderSize() + getInternalSize() + index;
    }

    @Nullable
    public F getFooterAt(int position) {
        int index = toFooterIndex(position);
        if (index < 0 || index >= getFooterSize())
            return null;
        return footers.get(index);
    }

    public void setFooters(List<F> footers) {
        this.footers.clear();
        if (footers != null && !footers.isEmpty())
            this.footers.addAll(footers);
        notifyDataSetChanged();
    }

    public void addFooters(List<F> footers) {
        if (footers != null && !footers.isEmpty()) {
            this.footers.addAll(footers);
            notifyDataSetChanged();
        }
    }

    public void addFooter(F footer) {
        if (footer != null) {
            this.footers.add(footer);
            notifyItemInserted(toCustomPosition(getItemCount()));
        }
    }

    public void addFooter(int index, F footer) {
        if (footer != null) {
            this.footers.add(index, footer);
            notifyItemInserted(toCustomPosition(toFooterPosition(index)));
        }
    }

    public void clearFooters() {
        if (!footers.isEmpty()) {
            footers.clear();
            notifyDataSetChanged();
        }
    }

    public void removeFooter(int index) {
        if (index < 0 || index >= getFooterSize())
            return;
        int pos = toFooterPosition(index);
        footers.remove(index);
        notifyItemRemoved(toCustomPosition(pos));
    }

    public void updateFooter(int index, F footer) {
        if (index < 0 || index >= getFooterSize())
            return;
        if (footer != null)
            this.footers.set(index, footer);
        notifyItemChanged(toCustomPosition(toFooterPosition(index)));
    }

    //empties

    public List<E> getEmpties() {
        return empties;
    }

    public int getEmptySize() {
        return empties.size();
    }

    public int toEmptyIndex(int position) {
        return position - getHeaderSize();
    }

    public int toEmptyPosition(int index) {
        return getHeaderSize() + index;
    }

    @Nullable
    public E getEmptyAt(int position) {
        int index = toEmptyIndex(position);
        if (index < 0 || index >= getEmptySize())
            return null;
        return empties.get(index);
    }

    public void setEmpties(List<E> empties) {
        this.empties.clear();
        if (empties != null && !empties.isEmpty())
            this.empties.addAll(empties);
        if (isEmptyData())
            notifyDataSetChanged();
    }

    public void addEmpties(List<E> empties) {
        if (empties != null && !empties.isEmpty()) {
            this.empties.addAll(empties);
            if (isEmptyData())
                notifyDataSetChanged();
        }
    }

    public void addEmpty(E empty) {
        if (empty != null) {
            this.empties.add(empty);
            if (isEmptyData())
                notifyItemInserted(toCustomPosition(getItemCount()));
        }
    }

    public void addEmpty(int index, E empty) {
        if (empty != null) {
            this.empties.add(index, empty);
            if (isEmptyData())
                notifyItemInserted(toCustomPosition(toEmptyPosition(index)));
        }
    }

    public void clearEmpties() {
        if (!empties.isEmpty()) {
            empties.clear();
            if (isEmptyData())
                notifyDataSetChanged();
        }
    }

    public void removeEmpty(int index) {
        if (index < 0 || index >= getEmptySize())
            return;
        int pos = toEmptyPosition(index);
        empties.remove(index);
        notifyItemRemoved(toCustomPosition(pos));
    }

    public void updateEmpty(int index, E empty) {
        if (index < 0 || index >= getEmptySize())
            return;
        if (empty != null)
            this.empties.set(index, empty);
        notifyItemChanged(toCustomPosition(toEmptyPosition(index)));
    }

    public OnCreateViewHolderListener getOnCreateViewHolderListener() {
        return onCreateViewHolderListener;
    }

    public void addOnCreateViewHolderListener(OnCreateViewHolderListener onCreateViewHolderListener) {
        this.onCreateViewHolderListener = onCreateViewHolderListener;
    }

    public void addOnChildClickListener(@NonNull VH holder, @IdRes int id) {
        holder.addOnChildClickListener(id, onItemChildClickListener == null ? null : getDefaultChildClickListener());
    }

    public void addOnChildLongClickListener(@NonNull VH holder, @IdRes int id) {
        holder.addOnChildLongClickListener(id, onItemChildLongClickListener == null ? null : getDefaultChildLongClickListener());
    }

    public final void bindRecyclerView(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setAdapter(this);
    }

    private void ensureBoundedRecyclerView() {
        if (recyclerView == null)
            throw new IllegalStateException("Please bind RecyclerView first by calling method bindRecyclerView(@NonNull RecyclerView recyclerView).");
    }

    protected int getCustomHeaderSize() {
        return 0;
    }

    public int toCustomPosition(int position) {
        return position + getCustomHeaderSize();
    }

    public boolean isEmptyData() {
        return getData().isEmpty();
    }

    private int getInternalSize() {
        return isEmptyData() ? getEmptySize() : getDataSize();
    }

    @Override
    public int getItemCount() {
        return getHeaderSize() + getInternalSize() + getFooterSize();
    }

    @ViewType
    @Override
    public int getItemViewType(int position) {
        if (position >= getHeaderSize() + getInternalSize() + getFooterSize())
            return UNKNOWN;
        if (position >= getHeaderSize() + getInternalSize())
            return TYPE_FOOTER;
        if (position >= getHeaderSize())
            return isEmptyData() ? TYPE_EMPTY : TYPE_DATA;
        if (position >= 0)
            return TYPE_HEADER;
        return UNKNOWN;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.addOnItemClickListener(onItemClickListener == null ? null : getDefaultItemClickListener());
        holder.addOnItemLongClickListener(onItemLongClickListener == null ? null : getDefaultItemLongClickListener());
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                onBindHeaderViewHolder(holder, position, getHeaderAt(position));
                break;
            case TYPE_DATA:
                onBindDataViewHolder(holder, position, getDataAt(position));
                break;
            case TYPE_FOOTER:
                onBindFooterViewHolder(holder, position, getFooterAt(position));
                break;
            case TYPE_EMPTY:
                onBindEmptyViewHolder(holder, position, getEmptyAt(position));
                break;
            case UNKNOWN:
                break;
        }
    }

    protected void onBindHeaderViewHolder(@NonNull VH holder, int position, H headerBean) {
    }

    protected void onBindDataViewHolder(@NonNull VH holder, int position, D dataBean) {
    }

    protected void onBindFooterViewHolder(@NonNull VH holder, int position, F footerBean) {
    }

    protected void onBindEmptyViewHolder(@NonNull VH holder, int position, E emptyBean) {
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {

        SparseArray<View> childrenCache = new SparseArray<>();

        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public <V extends View> V findViewById(@IdRes int id) {
            View child = childrenCache.get(id);
            if (child == null) {
                child = itemView.findViewById(id);
                if (child != null)
                    childrenCache.put(id, child);
            }
            return (V) child;
        }

        public BaseViewHolder setText(@IdRes int id, CharSequence txt) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setText(txt);
            return this;
        }

        public BaseViewHolder setTextColor(@IdRes int id, @ColorInt int color) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextColor(color);
            return this;
        }

        public BaseViewHolder setTextSize(@IdRes int id, float size) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextSize(size);
            return this;
        }

        public BaseViewHolder setTextSize(@IdRes int id, int unit, float size) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextSize(unit, size);
            return this;
        }

        public BaseViewHolder setVisibility(@IdRes int id, int visibility) {
            View view = findViewById(id);
            if (view != null)
                view.setVisibility(visibility);
            return this;
        }

        public BaseViewHolder setSelected(@IdRes int id, boolean selected) {
            View view = findViewById(id);
            if (view != null)
                view.setSelected(selected);
            return this;
        }

        public BaseViewHolder setEnabled(@IdRes int id, boolean enable) {
            View view = findViewById(id);
            if (view != null)
                view.setEnabled(enable);
            return this;
        }

        public BaseViewHolder setBackgroundColor(@IdRes int id, @ColorInt int color) {
            View view = findViewById(id);
            if (view != null)
                view.setBackgroundColor(color);
            return this;
        }

        public BaseViewHolder setBackgroundResource(@IdRes int id, @DrawableRes int resId) {
            View view = findViewById(id);
            if (view != null)
                view.setBackgroundResource(resId);
            return this;
        }

        public BaseViewHolder setBackground(@IdRes int id, Drawable background) {
            View view = findViewById(id);
            if (view != null)
                view.setBackground(background);
            return this;
        }

        public BaseViewHolder setImageDrawable(@IdRes int id, Drawable drawable) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageDrawable(drawable);
            return this;
        }

        public BaseViewHolder setImageBitmap(@IdRes int id, Bitmap bitmap) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageBitmap(bitmap);
            return this;
        }

        public BaseViewHolder setImageResource(@IdRes int id, @DrawableRes int resId) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageResource(resId);
            return this;
        }

        public BaseViewHolder setTag(@IdRes int id, final Object tag) {
            View view = findViewById(id);
            if (view != null)
                view.setTag(tag);
            return this;
        }

        public BaseViewHolder setTag(@IdRes int id, int key, final Object tag) {
            View view = findViewById(id);
            if (view != null)
                view.setTag(key, tag);
            return this;
        }

        public BaseViewHolder setCompoundDrawables(@IdRes int id,
                                                   @Nullable Drawable left,
                                                   @Nullable Drawable top,
                                                   @Nullable Drawable right,
                                                   @Nullable Drawable bottom) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setCompoundDrawables(left, top, right, bottom);
            return this;
        }

        void setPositionTag(int position) {
            itemView.setTag(R.id.recycler_default_view_position, position);
        }

        void addOnItemClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }

        void addOnItemLongClickListener(View.OnLongClickListener listener) {
            itemView.setOnLongClickListener(listener);
        }

        void addOnChildClickListener(@IdRes int id, View.OnClickListener listener) {
            View child = findViewById(id);
            if (child != null) {
                child.setOnClickListener(listener);
            }
        }

        void addOnChildLongClickListener(@IdRes int id, View.OnLongClickListener listener) {
            View child = findViewById(id);
            if (child != null) {
                child.setOnLongClickListener(listener);
            }
        }
    }

    //getter and setter

    @LayoutRes
    public int getHeaderLayoutId() {
        return headerLayoutId;
    }

    public void setHeaderLayoutId(@LayoutRes int headerLayoutId) {
        this.headerLayoutId = headerLayoutId;
    }

    @LayoutRes
    public int getDataLayoutId() {
        return dataLayoutId;
    }

    public void setDataLayoutId(@LayoutRes int dataLayoutId) {
        this.dataLayoutId = dataLayoutId;
    }

    @LayoutRes
    public int getFooterLayoutId() {
        return footerLayoutId;
    }

    public void setFooterLayoutId(@LayoutRes int footerLayoutId) {
        this.footerLayoutId = footerLayoutId;
    }

    @LayoutRes
    public int getEmptyLayoutId() {
        return emptyLayoutId;
    }

    public void setEmptyLayoutId(@LayoutRes int emptyLayoutId) {
        this.emptyLayoutId = emptyLayoutId;
    }

    public OnItemClickListener<H, D, F, E> getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener<H, D, F, E> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemLongClickListener<H, D, F, E> getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<H, D, F, E> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public OnItemChildClickListener<H, D, F, E> getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener<H, D, F, E> onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public OnItemChildLongClickListener<H, D, F, E> getOnItemChildLongClickListener() {
        return onItemChildLongClickListener;
    }

    public void setOnItemChildLongClickListener(OnItemChildLongClickListener<H, D, F, E> onItemChildLongClickListener) {
        this.onItemChildLongClickListener = onItemChildLongClickListener;
    }
}
