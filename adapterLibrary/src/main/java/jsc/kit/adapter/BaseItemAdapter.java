package jsc.kit.adapter;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 仅仅支持数据的Adapter
 * <br>Email:1006368252@qq.com
 * <br>QQ:1006368252
 * create time: 2018/12/22 21:57 Saturday
 *
 * @author jsc
 */
public class BaseItemAdapter<T> extends RecyclerView.Adapter<BaseItemAdapter.BaseItemViewHolder<T>> {

    public interface OnItemClickListener<T> {
        void onItemClick(@NonNull View itemView, int position, T item);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(@NonNull View itemView, int position, T item);
    }

    public interface OnItemChildClickListener<T> {
        void onItemChildClick(@NonNull View child, int position, T item);
    }

    public interface OnItemChildLongClickListener<T> {
        boolean onItemChildLongClick(@NonNull View child, int position, T item);
    }

    public interface OnCreateViewHolderListener<T> {
        View onCreateItemView(@NonNull ViewGroup viewGroup);

        void afterCreateViewHolder(@NonNull BaseItemAdapter.BaseItemViewHolder<T> holder);
    }

    private List<T> items = new ArrayList<>();
    private @LayoutRes
    int itemLayoutId = -1;
    private OnItemClickListener<T> onItemClickListener = null;
    private OnItemLongClickListener<T> onItemLongClickListener = null;
    private OnItemChildClickListener<T> onItemChildClickListener = null;
    private OnItemChildLongClickListener<T> onItemChildLongClickListener = null;
    private OnCreateViewHolderListener<T> onCreateViewHolderListener = null;

    public BaseItemAdapter() {
    }

    public BaseItemAdapter(@LayoutRes int itemLayoutId) {
        setItemLayoutId(itemLayoutId);
    }

    public final void bindRecyclerView(@NonNull RecyclerView recyclerView){
        recyclerView.setAdapter(this);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items.clear();
        if (items != null)
            this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void addItems(List<T> items) {
        if (items != null && !items.isEmpty()) {
            addItems(getItemCount() - items.size(), items);
        }
    }

    public void addItems(int position, List<T> items) {
        if (items != null && !items.isEmpty()) {
            this.items.addAll(items);
            notifyItemRangeInserted(position, items.size());
        }
    }

    public void addItem(T item) {
        if (item != null) {
            addItem(getItemCount(), item);
        }
    }

    public void addItem(int position, T item) {
        if (item != null) {
            items.add(position, item);
            notifyItemInserted(position);
        }
    }

    public T getItemAt(int position) {
        return (position >= 0 && position < getItemCount()) ? items.get(position) : null;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public BaseItemViewHolder<T> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = onCreateViewHolderListener == null ? null : onCreateViewHolderListener.onCreateItemView(viewGroup);
        if (itemView == null) {
            if (getItemLayoutId() == -1)
                throw new IllegalArgumentException("Please set item layout first.");
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(getItemLayoutId(), viewGroup, false);
        }
        BaseItemViewHolder<T> holder = new BaseItemViewHolder<>(itemView);
        holder.bindItemClickListener(onItemClickListener);
        holder.bindItemLongClickListener(onItemLongClickListener);
        holder.bindItemChildClickListener(onItemChildClickListener);
        holder.bindItemChildLongClickListener(onItemChildLongClickListener);
        if (onCreateViewHolderListener != null)
            onCreateViewHolderListener.afterCreateViewHolder(holder);
        return holder;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder<T> holder, int position) {
        holder.bindItem(getItemAt(position));
        onBindItemViewHolder(holder, position, getItemAt(position));
    }

    public static class BaseItemViewHolder<T> extends RecyclerView.ViewHolder {

        SparseArray<View> childrenCache = new SparseArray<>();
        View.OnClickListener itemClickListener = null;
        View.OnLongClickListener itemLongClickListener = null;
        View.OnClickListener itemChildClickListener = null;
        View.OnLongClickListener itemChildLongClickListener = null;
        OnItemClickListener<T> listener1 = null;
        OnItemChildClickListener<T> listener2 = null;
        OnItemLongClickListener<T> listener3 = null;
        OnItemChildLongClickListener<T> listener4 = null;
        T item;

        public BaseItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bindItemClickListener(OnItemClickListener<T> listener1) {
            this.listener1 = listener1;
            itemView.setOnClickListener(listener1 == null ? null : getItemClickListener());
        }

        public void bindItemLongClickListener(OnItemLongClickListener<T> listener3) {
            this.listener3 = listener3;
            itemView.setOnLongClickListener(listener3 == null ? null : getItemLongClickListener());
        }

        public void bindItemChildClickListener(OnItemChildClickListener<T> listener2) {
            this.listener2 = listener2;
        }

        public void bindItemChildLongClickListener(OnItemChildLongClickListener<T> listener4) {
            this.listener4 = listener4;
        }

        public void bindItem(T item) {
            this.item = item;
        }

        private View.OnClickListener getItemClickListener() {
            if (itemClickListener == null)
                itemClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener1 != null)
                            listener1.onItemClick(itemView, getAdapterPosition(), item);
                    }
                };
            return itemClickListener;
        }

        private View.OnLongClickListener getItemLongClickListener() {
            if (itemLongClickListener == null)
                itemLongClickListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return listener3 != null && listener3.onItemLongClick(itemView, getAdapterPosition(), item);
                    }
                };
            return itemLongClickListener;
        }

        private View.OnClickListener getItemChildClickListener() {
            if (itemChildClickListener == null)
                itemChildClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener2 != null)
                            listener2.onItemChildClick(v, getAdapterPosition(), item);
                    }
                };
            return itemChildClickListener;
        }

        private View.OnLongClickListener getItemChildLongClickListener() {
            if (itemChildLongClickListener == null)
                itemChildLongClickListener = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return listener4 != null && listener4.onItemChildLongClick(v, getAdapterPosition(), item);
                    }
                };
            return itemChildLongClickListener;
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

        public void removeChild(View child) {
            childrenCache.remove(child.getId());
        }

        public BaseItemViewHolder setText(@IdRes int id, CharSequence txt) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setText(txt);
            return this;
        }

        public BaseItemViewHolder setTextColor(@IdRes int id, @ColorInt int color) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextColor(color);
            return this;
        }

        public BaseItemViewHolder setTextSize(@IdRes int id, float size) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextSize(size);
            return this;
        }

        public BaseItemViewHolder setTextSize(@IdRes int id, int unit, float size) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setTextSize(unit, size);
            return this;
        }

        public BaseItemViewHolder setVisibility(@IdRes int id, int visibility) {
            View view = findViewById(id);
            if (view != null)
                view.setVisibility(visibility);
            return this;
        }

        public BaseItemViewHolder setSelected(@IdRes int id, boolean selected) {
            View view = findViewById(id);
            if (view != null)
                view.setSelected(selected);
            return this;
        }

        public BaseItemViewHolder setEnabled(@IdRes int id, boolean enable) {
            View view = findViewById(id);
            if (view != null)
                view.setEnabled(enable);
            return this;
        }

        public BaseItemViewHolder setBackgroundColor(@IdRes int id, @ColorInt int color) {
            View view = findViewById(id);
            if (view != null)
                view.setBackgroundColor(color);
            return this;
        }

        public BaseItemViewHolder setBackgroundResource(@IdRes int id, @DrawableRes int resId) {
            View view = findViewById(id);
            if (view != null)
                view.setBackgroundResource(resId);
            return this;
        }

        public BaseItemViewHolder setBackground(@IdRes int id, Drawable background) {
            View view = findViewById(id);
            if (view != null)
                view.setBackground(background);
            return this;
        }

        public BaseItemViewHolder setImageDrawable(@IdRes int id, Drawable drawable) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageDrawable(drawable);
            return this;
        }

        public BaseItemViewHolder setImageBitmap(@IdRes int id, Bitmap bitmap) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageBitmap(bitmap);
            return this;
        }

        public BaseItemViewHolder setImageResource(@IdRes int id, @DrawableRes int resId) {
            View view = findViewById(id);
            if (view instanceof ImageView)
                ((ImageView) view).setImageResource(resId);
            return this;
        }

        public BaseItemViewHolder setTag(@IdRes int id, final Object tag) {
            View view = findViewById(id);
            if (view != null)
                view.setTag(tag);
            return this;
        }

        public BaseItemViewHolder setTag(@IdRes int id, int key, final Object tag) {
            View view = findViewById(id);
            if (view != null)
                view.setTag(key, tag);
            return this;
        }

        public BaseItemViewHolder setCompoundDrawables(@IdRes int id,
                                                       @Nullable Drawable left,
                                                       @Nullable Drawable top,
                                                       @Nullable Drawable right,
                                                       @Nullable Drawable bottom) {
            View view = findViewById(id);
            if (view instanceof TextView)
                ((TextView) view).setCompoundDrawables(left, top, right, bottom);
            return this;
        }

        public BaseItemViewHolder addClickListener(@IdRes int id) {
            View view = findViewById(id);
            if (view != null)
                view.setOnClickListener(getItemChildClickListener());
            return this;
        }

        public BaseItemViewHolder addLongClickListener(@IdRes int id) {
            View view = findViewById(id);
            if (view != null)
                view.setOnLongClickListener(getItemChildLongClickListener());
            return this;
        }
    }

    public @LayoutRes
    int getItemLayoutId() {
        return itemLayoutId;
    }

    public void setItemLayoutId(@LayoutRes int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener<T> onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public void setOnItemChildLongClickListener(OnItemChildLongClickListener<T> onItemChildLongClickListener) {
        this.onItemChildLongClickListener = onItemChildLongClickListener;
    }

    public void setOnCreateViewHolderListener(OnCreateViewHolderListener<T> onCreateViewHolderListener) {
        this.onCreateViewHolderListener = onCreateViewHolderListener;
    }

    protected void onBindItemViewHolder(@NonNull BaseItemViewHolder<T> holder, int position, T item) {
    }
}
