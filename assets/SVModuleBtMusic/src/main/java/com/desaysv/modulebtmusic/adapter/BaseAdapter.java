package com.desaysv.modulebtmusic.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final ArrayList<T> items = new ArrayList<>();

    public BaseAdapter() {
        setHasStableIds(true);
    }

    public void add(T object) {
        synchronized (items) {
            items.add(object);
        }
        notifyDataSetChanged();
    }

    public void add(int index, T object) {
        synchronized (items) {
            items.add(index, object);
        }
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> collection) {
        if (collection != null) {
            synchronized (items) {
                items.clear();
                items.addAll(collection);
            }
            notifyDataSetChanged();
        }
    }

    public void addAll(T... items) {
        addAll(Arrays.asList(items));
    }

    public void clear() {
        synchronized (items) {
            items.clear();
        }
        notifyDataSetChanged();
    }

    public void remove(T object) {
        synchronized (items) {
            items.remove(object);
        }
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        synchronized (items) {
            if (items.size() > position) {
                return items.get(position);
            } else {
                return null;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}