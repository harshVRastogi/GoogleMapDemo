package com.harsh.mapdemo;

import android.location.Location;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.harsh.mapdemo.common.MapUtil;
import com.harsh.mapdemo.model.Post;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Harsh Rastogi on 7/28/2017.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private  List<Post> data;
    private final LinearLayoutManager mLinearLayoutManager;
    private final RecyclerView recyclerView;
    private OnPostChangeListener onPostChangeListener;
    private boolean flagFirst = true;

    public PostAdapter(RecyclerView recyclerView, List<Post> data) {
        this.data = data;
        this.recyclerView = recyclerView;
        mLinearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    int pos = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (pos < 0) {
                        return;
                    }
                    if (onPostChangeListener != null) {
                        onPostChangeListener.onPostChange(data.get(pos));
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (flagFirst) {
                    if (onPostChangeListener != null) {
                        onPostChangeListener.onPostChange(data.get(0));
                        flagFirst = false;
                    }
                }
            }
        });


        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_view, parent, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Post post = data.get(position);
        holder.title.setText(post.getTitle());
        holder.tag.setText(post.getTag());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onPostChangeListener != null) {
                    onPostChangeListener.onPostChange(data.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView tag;
        private ImageView imgView;
        private View container;

        public ViewHolder(View itemView) {
            super(itemView);
            this.container = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            tag = (TextView) itemView.findViewById(R.id.tag);
            imgView = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }

    public void setOnPostChangeListener(OnPostChangeListener onPostChangeListener) {
        this.onPostChangeListener = onPostChangeListener;
    }

    public List<Post> getData() {
        return data;
    }

    public void setData(List<Post> data){
        this.data = data;
        notifyDataSetChanged();
    }
}
