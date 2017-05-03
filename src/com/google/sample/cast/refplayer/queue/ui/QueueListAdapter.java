/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cast.refplayer.queue.ui;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.queue.QueueDataProvider;
import com.google.sample.cast.refplayer.utils.Utils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;

/**
 * A ListAdapter for showing queue items.
 */
public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.QueueHolder> implements
        DraggableItemAdapter<QueueListAdapter.QueueHolder>,
        SwipeableItemAdapter<QueueListAdapter.QueueHolder> {

    private static final String TAG = "QueueListAdapter";
    private final QueueDataProvider mProvider;
    private final VideoCastManager mCastManager;
    private static final int sPlayResource = R.drawable.ic_play_arrow_grey600_48dp;
    private static final int sPauseResource = R.drawable.ic_pause_grey600_48dp;
    private static final int sDragHandlerDarkResource = R.drawable.ic_drag_updown_grey_24dp;
    private static final int sDragHandlerLightResource = R.drawable.ic_drag_updown_white_24dp;
    private static int sWhiteColor;
    private static int sBlackColor;
    private static int sYellowColor;
    private static int sGreyColor;
    private View.OnClickListener mItemViewOnClickListener;
    private static final float mAspectRatio = 1f;
    private EventListener mEventListener;

    @Override
    public QueueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.queue_row, parent, false);
        return new QueueHolder(v);
    }

    @Override
    public void onBindViewHolder(QueueHolder holder, int position) {
        Log.d(TAG, "[upcoming] onBindViewHolder() for position: " + position);
        final MediaQueueItem item = mProvider.getItem(position);
        holder.mContainer.setTag(R.string.queue_tag_item, item);
        holder.mPlayPause.setTag(R.string.queue_tag_item, item);
        holder.mPlayUpcoming.setTag(R.string.queue_tag_item, item);
        holder.mStopUpcoming.setTag(R.string.queue_tag_item, item);

        // set listeners
        holder.mContainer.setOnClickListener(mItemViewOnClickListener);
        holder.mPlayPause.setOnClickListener(mItemViewOnClickListener);
        holder.mPlayUpcoming.setOnClickListener(mItemViewOnClickListener);
        holder.mStopUpcoming.setOnClickListener(mItemViewOnClickListener);

        MediaInfo info = item.getMedia();
        MediaMetadata metaData = info.getMetadata();
        holder.mTitleView.setText(metaData.getString(MediaMetadata.KEY_TITLE));
        holder.mDescriptionView.setText(metaData.getString(MediaMetadata.KEY_SUBTITLE));
        AQuery aq = new AQuery(holder.itemView);
        if (!metaData.getImages().isEmpty()) {
            aq.id(holder.mImageView).width(64)
                    .image(metaData.getImages().get(0).getUrl().toString(), true, true, 0,
                            R.drawable.default_video, null, 0, mAspectRatio);
        }

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int swipeState = holder.getSwipeStateFlags();
        int bgResId = (item == mProvider.getUpcomingItem()) ? R.drawable.bg_item_upcoming_state
                : R.drawable.bg_item_normal_state;

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0)) {

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_dragging_active_state;
            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                if (item == mProvider.getCurrentItem()) {
                    holder.updateControlsStatus(QueueHolder.ControlStatus.CURRENT);
                    updateImageResource(holder.mPlayPause);
                } else if (item == mProvider.getUpcomingItem()) {
                    holder.updateControlsStatus(QueueHolder.ControlStatus.UPCOMING);
                } else {
                    holder.updateControlsStatus(QueueHolder.ControlStatus.NONE);
                    holder.mPlayPause.setVisibility(View.GONE);
                }
            }
            holder.mContainer.setBackgroundResource(bgResId);
        } else {
            if (item == mProvider.getCurrentItem()) {
                holder.updateControlsStatus(QueueHolder.ControlStatus.CURRENT);
                updateImageResource(holder.mPlayPause);
            } else if (item == mProvider.getUpcomingItem()) {
                holder.updateControlsStatus(QueueHolder.ControlStatus.UPCOMING);
            } else {
                holder.updateControlsStatus(QueueHolder.ControlStatus.NONE);
                holder.mPlayPause.setVisibility(View.GONE);
            }
        }

    }

    /**
     * {@code null} means it should be hidden, otherwise returns the resource that should be used
     */
    private void updateImageResource(ImageButton button) {
        int status = mCastManager.getPlaybackStatus();
        switch (status) {
            case MediaStatus.PLAYER_STATE_PLAYING:
                button.setImageResource(sPauseResource);
                break;
            case MediaStatus.PLAYER_STATE_PAUSED:
                button.setImageResource(sPlayResource);
                break;
            default:
                button.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return QueueDataProvider.getInstance().getCount();
    }

    public QueueListAdapter(Context context) {
        mCastManager = VideoCastManager.getInstance();
        mProvider = QueueDataProvider.getInstance();
        mProvider.setOnQueueDataChangedListener(new QueueDataProvider.OnQueueDataChangedListener() {
            @Override
            public void onQueueDataChanged() {
                notifyDataSetChanged();
            }
        });
        mItemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag(R.string.queue_tag_item) != null) {
                    MediaQueueItem item = (MediaQueueItem) v.getTag(R.string.queue_tag_item);
                    Log.d(TAG, item.getItemId() + "");
                }
                onItemViewClick(v);
            }
        };
        setHasStableIds(true);
        sWhiteColor = context.getResources().getColor(R.color.white);
        sGreyColor = context.getResources().getColor(android.R.color.secondary_text_light);
        sBlackColor = context.getResources().getColor(R.color.black);
        sYellowColor = context.getResources().getColor(R.color.ccl_mini_upcoming_upnext_color);
    }

    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(v);
        }
    }

    public static class QueueHolder extends AbstractDraggableSwipeableItemViewHolder {

        private Context mContext;
        private final ImageButton mPlayPause;
        private View mControls;
        private View mUpcomingControls;
        private ImageButton mPlayUpcoming;
        private ImageButton mStopUpcoming;
        public ImageView mImageView;
        public ViewGroup mContainer;
        public ImageView mDragHandle;
        public TextView mTitleView;
        public TextView mDescriptionView;

        private enum ControlStatus {
            CURRENT, UPCOMING, NONE
        }

        public QueueHolder(View v) {
            super(v);
            mContext = v.getContext();
            mContainer = (ViewGroup) v.findViewById(R.id.container);
            mDragHandle = (ImageView) v.findViewById(R.id.drag_handle);
            mTitleView = (TextView) v.findViewById(R.id.textView1);
            mDescriptionView = (TextView) v.findViewById(R.id.textView2);
            mImageView = (ImageView) v.findViewById(R.id.imageView1);
            mPlayPause = (ImageButton) v.findViewById(R.id.play_pause);
            mControls = v.findViewById(R.id.controls);
            mUpcomingControls = v.findViewById(R.id.controls_upcoming);
            mPlayUpcoming = (ImageButton) v.findViewById(R.id.play_upcoming);
            mStopUpcoming = (ImageButton) v.findViewById(R.id.stop_upcoming);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }

        private void updateControlsStatus(ControlStatus status) {
            int bgResId = R.drawable.bg_item_normal_state;
            mTitleView.setTextAppearance(mContext, R.style.Base_TextAppearance_AppCompat_Subhead);
            mDescriptionView.setTextAppearance(mContext,
                    R.style.Base_TextAppearance_AppCompat_Caption);
            mTitleView.setTextColor(sBlackColor);
            mDescriptionView.setTextColor(sGreyColor);
            switch (status) {
                case CURRENT:
                    bgResId = R.drawable.bg_item_normal_state;
                    mControls.setVisibility(View.VISIBLE);
                    mPlayPause.setVisibility(View.VISIBLE);
                    mUpcomingControls.setVisibility(View.GONE);
                    mDragHandle.setImageResource(sDragHandlerDarkResource);
                    break;
                case UPCOMING:
                    mControls.setVisibility(View.VISIBLE);
                    mPlayPause.setVisibility(View.GONE);
                    mUpcomingControls.setVisibility(View.VISIBLE);
                    mDragHandle.setImageResource(sDragHandlerLightResource);
                    bgResId = R.drawable.bg_item_upcoming_state;
                    mTitleView.setTextAppearance(mContext,
                            R.style.TextAppearance_AppCompat_Small_Inverse);
                    mTitleView.setTextAppearance(mTitleView.getContext(),
                            R.style.Base_TextAppearance_AppCompat_Subhead_Inverse);
                    mDescriptionView.setTextAppearance(mContext,
                            R.style.Base_TextAppearance_AppCompat_Caption);
                    mTitleView.setTextColor(sWhiteColor);
                    mDescriptionView.setTextColor(sYellowColor);
                    mDescriptionView.setText(R.string.ccl_mini_upnext);
                    break;
                default:
                    mControls.setVisibility(View.GONE);
                    mPlayPause.setVisibility(View.GONE);
                    mUpcomingControls.setVisibility(View.GONE);
                    mDragHandle.setImageResource(sDragHandlerDarkResource);
                    break;
            }
            mContainer.setBackgroundResource(bgResId);
        }
    }

    @Override
    public boolean onCheckCanStartDrag(QueueHolder queueHolder, int x, int y) {
        final View containerView = queueHolder.mContainer;
        final View dragHandleView = queueHolder.mDragHandle;

        final int offsetX = containerView.getLeft() + (int) (
                ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = containerView.getTop() + (int) (
                ViewCompat.getTranslationY(containerView) + 0.5f);

        return Utils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG,
                "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }
        mProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int onGetSwipeReactionType(QueueHolder queueHolder, int x, int y) {
        if (onCheckCanStartDrag(queueHolder, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        } else {
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
        }
    }

    @Override
    public void onSetSwipeBackground(QueueHolder queueHolder, int type) {
        int bgRes = 0;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        queueHolder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public int onSwipeItem(QueueHolder queueHolder, int result) {
        Log.d(TAG, "onSwipeItem(result = " + result + ")");

        switch (result) {
            // swipe right --- remove
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            // swipe left -- pin
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            // other --- do nothing
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(QueueHolder queueHolder, int result, int reaction) {
        Log.d(TAG, "onPerformAfterSwipeReaction(result = " + result + ", reaction = " + reaction
                + ")");

        final int position = queueHolder.getPosition();
        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            mProvider.removeFromQueue(position);
            notifyItemRemoved(position);

            if (mEventListener != null) {
                mEventListener.onItemRemoved(position);
            }
        } else if (reaction
                == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) {
            notifyItemChanged(position);

            if (mEventListener != null) {
                mEventListener.onItemPinned(position);
            }
        }
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    public interface EventListener {

        void onItemRemoved(int position);

        void onItemPinned(int position);

        void onItemViewClicked(View v);
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getItemId();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mProvider.setOnQueueDataChangedListener(null);
    }

}
