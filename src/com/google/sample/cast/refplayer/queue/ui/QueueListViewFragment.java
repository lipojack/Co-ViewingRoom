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

import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;
import com.google.sample.cast.refplayer.CastApplication;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.queue.QueueDataProvider;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.BasicSwapTargetTranslationInterpolator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;

/**
 * A fragment to show the list of queue items.
 */
public class QueueListViewFragment extends Fragment {

    private static final String TAG = "QueueListViewFragment";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private VideoCastManager mCastManager;
    private QueueDataProvider mProvider;

    public QueueListViewFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mProvider = QueueDataProvider.getInstance();

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss
        // animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager
                .setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager
                .setSwapTargetTranslationInterpolator(new BasicSwapTargetTranslationInterpolator());
        /*
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) getResources().getDrawable(R.drawable.material_shadow_z3));
            */
        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //adapter
        final QueueListAdapter myItemAdapter = new QueueListAdapter(
                getActivity().getApplicationContext());
        myItemAdapter.setEventListener(new QueueListAdapter.EventListener() {
            @Override
            public void onItemRemoved(int position) {
                Log.d(TAG, "onItemRemoved() at position: " + position);
            }

            @Override
            public void onItemPinned(int position) {
                Log.d(TAG, "onItemPinned() at position: " + position);
            }

            @Override
            public void onItemViewClicked(View v) {
                switch(v.getId()) {
                    case R.id.container:
                        Log.d(TAG, "onItemViewClicked() container " + v.getTag(R.string.queue_tag_item));
                        onContainerClicked(v);
                        break;
                    case R.id.play_pause:
                        Log.d(TAG, "onItemViewClicked() play-pause " + v.getTag(R.string.queue_tag_item));
                        onPlayPauseClicked(v);
                        break;
                    case R.id.play_upcoming:
                        mProvider.onUpcomingPlayClicked(v,
                                (MediaQueueItem) v.getTag(R.string.queue_tag_item));
                        break;
                    case R.id.stop_upcoming:
                        mProvider.onUpcomingStopClicked(v,
                                (MediaQueueItem) v.getTag(R.string.queue_tag_item));
                        break;
                }
            }
        });

        mAdapter = myItemAdapter;

        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        final GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);
        /*
        if (!supportsViewElevation()) {
            mRecyclerView.addItemDecoration(
                    new ItemShadowDecorator((NinePatchDrawable) getResources().getDrawable(
                            R.drawable.material_shadow_z1)));
        }
        */
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        mCastManager = VideoCastManager.getInstance();
    }

    private void onPlayPauseClicked(View view) {
        try {
            mCastManager.togglePlayback();
        } catch (CastException | TransientNetworkDisconnectionException |NoConnectionException e) {
            Log.e(TAG, "Failed to toggle playback status");
        }
    }

    private void onContainerClicked(View view) {
        MediaQueueItem item = (MediaQueueItem) view.getTag(R.string.queue_tag_item);
        try {
            if (mProvider.isQueueDetached()) {
                Log.d(TAG, "Is detached: itemId = " + item.getItemId());

                int currentPosition = mProvider.getPositionByItemId(item.getItemId());
                MediaQueueItem[] items = Utils.rebuildQueue(mProvider.getItems());
                ((CastApplication) getActivity().getApplicationContext())
                        .loadQueue(items, currentPosition);
                // temporary mCastManager.queueLoad(items, currentPosition, MediaStatus.REPEAT_MODE_REPEAT_OFF,
                // temporary        null);
            } else {
                int currentItemId = mProvider.getCurrentItemId();
                if (currentItemId == item.getItemId()) {
                    // we selected the one that is currently playing so we take the user to the
                    // full screen controller
                    mCastManager.onTargetActivityInvoked(getActivity());
                } else {
                    // a different item in the queue was selected so we jump there
                    mCastManager.queueJumpToItem(item.getItemId(), null);
                }
            }
        } catch (TransientNetworkDisconnectionException | NoConnectionException e) {
            Log.e(TAG, "Failed to start playback of the new item");
        }
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }
}
