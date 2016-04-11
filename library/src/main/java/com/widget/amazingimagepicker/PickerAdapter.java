package com.widget.amazingimagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.widget.amazingimagepicker.holder.GridItemViewHolder;
import com.widget.amazingimagepicker.holder.HeaderViewHolder;
import com.widget.amazingimagepicker.model.Bucket;
import com.widget.amazingimagepicker.model.Content;
import com.widget.amazingimagepicker.model.Thumbnail;
import com.widget.amazingimagepicker.superslim.GridSLM;
import com.widget.amazingimagepicker.superslim.LayoutManager;
import com.widget.amazingimagepicker.superslim.LinearSLM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    private List<LineItem> itemList = new ArrayList<>();
    private List<Content> contentList = new ArrayList<>();
    private int numColumns = 4;
    private OnContentClickListener onUriClick;
    private Content selectedItem;

    public PickerAdapter(List<Content> itemList, int numColumns, OnContentClickListener onUriClick) {
        this.numColumns = numColumns;
        this.onUriClick = onUriClick;
        setData(itemList);
    }

    public void setData(List<Content> itemList) {
        Bucket lastBucket = null;
        int sectionManager = -1;
        int headerCount = 0;
        int sectionFirstPosition = 0;
        this.contentList = itemList;
        Collections.sort(itemList, new Comparator<Content>() {
            @Override
            public int compare(Content lhs, Content rhs) {
                long lValue = Long.valueOf(lhs.getBucket().getId());
                long rValue = Long.valueOf(rhs.getBucket().getId());
                return lValue > rValue ? 1 : lValue == rValue ? lhs.getDate() < rhs.getDate() ? 1 : lhs.getDate() == rhs.getDate() ? 0 : -1 : -1;
            }
        });
        for (int i = 0; i < itemList.size(); i++) {
            Content item = itemList.get(i);
            if (!item.getBucket().equals(lastBucket)) {
                sectionManager = (sectionManager + 1) % 2;
                sectionFirstPosition = i + headerCount;
                lastBucket = item.getBucket();
                headerCount += 1;
                this.itemList.add(new LineItem(item.getBucket().getName(), LinearSLM.ID, sectionFirstPosition));
            }
            this.itemList.add(new LineItem(item, GridSLM.ID, sectionFirstPosition));
        }
    }

    public Content getItem(int position) {
        return contentList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_item, parent, false);
            return new HeaderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item, parent, false);
            return new GridItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        View itemView = holder.itemView;
        LineItem item = itemList.get(position);
        final Context context = holder.itemView.getContext();

        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        lp.setNumColumns(numColumns);
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            lp.headerDisplay = LayoutManager.LayoutParams.HEADER_INLINE | (lp.headerDisplay & LayoutManager.LayoutParams.HEADER_OVERLAY) | (lp.headerDisplay & LayoutManager.LayoutParams.HEADER_STICKY);
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.headerEndMarginIsAuto = true;
            lp.headerStartMarginIsAuto = true;
            headerViewHolder.title.setText(item.title);
            headerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        } else {
            final GridItemViewHolder gridItemViewHolder = (GridItemViewHolder) holder;

            if (selectedItem == null && position == 1) {
                selectedItem = item.content;
                gridItemViewHolder.selected.setVisibility(View.VISIBLE);
            } else if (selectedItem == item.content) {
                gridItemViewHolder.selected.setVisibility(View.VISIBLE);
            } else {
                gridItemViewHolder.selected.setVisibility(View.INVISIBLE);
            }

            final Content content = itemList.get(position).content;
            final Uri imageURI = content.getImageUri();
            switch (content.getType()) {
                case IMAGE:
                    final Object loadedUri = gridItemViewHolder.image.getTag(R.id.image_tag);
                    if (loadedUri == null || !loadedUri.equals(imageURI)) {
                        Glide.with(context)
                                .load(imageURI)
                                .listener(new RequestListener<Uri, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        gridItemViewHolder.image.setTag(R.id.image_tag, null);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        gridItemViewHolder.image.setTag(R.id.image_tag, model);
                                        return false;
                                    }
                                })
                                .into(gridItemViewHolder.image);
                    }
                    gridItemViewHolder.duration.setText(null);
                    gridItemViewHolder.itemView.setOnClickListener(new OnGridClickListener(content));
                    break;
                case VIDEO:
                    Thumbnail thumbnail = content.getThumbnail();
                    if (thumbnail == null) {
                        Glide.with(context)
                                .using(new VideoLoader(imageURI.toString()), Bitmap.class)
                                .load(imageURI)
                                .as(Bitmap.class)
                                .decoder(new BitmapDecoder(imageURI.toString()))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(new BitmapImageViewTarget(gridItemViewHolder.image));
                    } else {
                        Glide.with(context)
                                .load(imageURI)
                                .into(gridItemViewHolder.image);
                    }
                    gridItemViewHolder.duration.setText(getFormattedDuration(content.getDuration()));
                    break;
                default:
                    break;
            }
            gridItemViewHolder.itemView.setOnClickListener(new OnGridClickListener(content));
        }

        lp.setSlm(item.sectionManager == 0 ? LinearSLM.ID : GridSLM.ID);
        lp.setFirstPosition(item.sectionFirstPosition);
        itemView.setLayoutParams(lp);
    }

    private String getFormattedDuration(int duration) {
        final int HOUR = 60 * 60 * 1000;
        final int MINUTE = 60 * 1000;
        final int SECOND = 1000;

        int durationHour = duration / HOUR;
        int durationMin = (duration % HOUR) / MINUTE;
        int durationSec = (duration % MINUTE) / SECOND;

        return String.format("%02d:%02d:%02d", durationHour, durationMin, durationSec);
    }

    public class BitmapDecoder implements ResourceDecoder<Bitmap, Bitmap> {

        private final String id;

        public BitmapDecoder(String id) {
            this.id = id;
        }

        @Override
        public Resource<Bitmap> decode(final Bitmap source, int width, int height) throws IOException {
            return new Resource<Bitmap>() {
                @Override
                public Bitmap get() {
                    return source;
                }

                @Override
                public int getSize() {
                    return 0;
                }

                @Override
                public void recycle() {
                }
            };
        }

        @Override
        public String getId() {
            return id;
        }
    }

    public class VideoLoader implements ModelLoader<Uri, Bitmap> {

        private final String id;

        public VideoLoader(String id) {
            this.id = id;
        }

        @Override
        public DataFetcher<Bitmap> getResourceFetcher(final Uri model, int width, int height) {
            return new DataFetcher<Bitmap>() {
                @Override
                public Bitmap loadData(Priority priority) throws Exception {
                    return ThumbnailUtils.createVideoThumbnail(model.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
                }

                @Override
                public void cleanup() {
                }

                @Override
                public String getId() {
                    return id;
                }

                @Override
                public void cancel() {
                }
            };
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public boolean isHeader(int position) {
        return itemList.get(position).isHeader;
    }

    public static class LineItem {

        public int sectionManager;
        public int sectionFirstPosition;
        public boolean isHeader;
        public Content content;
        public String title;

        public LineItem(Content content, int sectionManager,
                        int sectionFirstPosition) {
            this.isHeader = false;
            this.content = content;
            this.sectionManager = sectionManager;
            this.sectionFirstPosition = sectionFirstPosition;
        }

        public LineItem(String title, int sectionManager,
                        int sectionFirstPosition) {
            this.isHeader = true;
            this.title = title;
            this.sectionManager = sectionManager;
            this.sectionFirstPosition = sectionFirstPosition;
        }
    }

    public interface OnContentClickListener {
        void onClick(Content content);
    }

    private class OnGridClickListener implements View.OnClickListener {

        private final Content content;

        private OnGridClickListener(Content content) {
            this.content = content;
        }

        @Override
        public void onClick(View v) {
            v.findViewById(R.id.selected).setVisibility(View.VISIBLE);
            selectedItem = content;
            onUriClick.onClick(content);
            notifyDataSetChanged();
        }
    }
}
