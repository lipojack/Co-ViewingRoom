package com.google.sample.cast.refplayer.chatting;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.util.Log;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.sample.cast.refplayer.R;

import android.app.Service;
import android.os.Vibrator;

public class MessagesAdapter extends ArrayAdapter<ChatMessage>{

	private static final String TAG = "MessagesAdapter";
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"HH:mm", Locale.US);

	Context context;
	
	public MessagesAdapter(Context context) {
		super(context, R.layout.row_message);
		this.context = context;
	}

	private class ViewHolder {
		TextView labelUsername, labelMessage, labelTime, index;
		View containerSender, containerMessage, miniImage;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view=createOrReuseView(convertView,parent);
		final ViewHolder holder=(ViewHolder) view.getTag();
		ChatMessage messageToShow = getItem(position);
		holder.index.setText(messageToShow.getIndex());
		holder.labelMessage.setText(messageToShow.getMessage());
		if(messageToShow.getIdentity().charAt(0)=='0') {
			holder.miniImage.setBackgroundResource(0);
		}else if(messageToShow.getIdentity().charAt(0)=='2') {
			//holder.containerMessage.setBackgroundResource(R.drawable.background_system_message);
            //holder.labelMessage.setTextAppearance(context.getApplicationContext(), R.style.identityText);
			holder.miniImage.setBackgroundResource(R.drawable.video);
		}else if(messageToShow.getIdentity().charAt(0)=='3') {
			holder.miniImage.setBackgroundResource(R.drawable.snapshot);
		}

		LinearLayout.LayoutParams messageParams = (LayoutParams) holder.containerMessage
				.getLayoutParams();
		if (messageToShow.isSystemMessage()) {
			messageParams.gravity = Gravity.CENTER;
			holder.containerMessage
					.setBackgroundResource(R.drawable.background_system_message);
			holder.containerSender.setVisibility(View.GONE);
		} else {
			holder.containerSender.setVisibility(View.VISIBLE);
			holder.labelUsername.setText(messageToShow.getUsername()+":");
			//holder.labelTime.setText(dateFormat.format(messageToShow.getDate()));
			holder.labelTime.setText(messageToShow.getDate());
			if (messageToShow.isIncomingMessage()) {
				messageParams.gravity = Gravity.LEFT;
				holder.containerMessage.setBackgroundResource(R.drawable.background_incoming_message);
			} else {
				messageParams.gravity = Gravity.RIGHT;
				holder.containerMessage.setBackgroundResource(R.drawable.background_outgoing_message);
			}
		}

		return view;
	}

	

	private View createOrReuseView(View convertView, ViewGroup parent) {
		Log.d(TAG, "========== createOrReuseView");
		final View view;
		final ViewHolder holder;
		if (convertView == null) {
			// Create the row
			view = LayoutInflater.from(getContext()).inflate(
					R.layout.row_message, parent, false);
			holder = new ViewHolder();
			holder.labelUsername = (TextView) view.findViewById(R.id.label_username);
			holder.labelTime = (TextView) view.findViewById(R.id.label_date);
			holder.labelMessage = (TextView) view.findViewById(R.id.label_message);
			holder.index = (TextView) view.findViewById(R.id.index);
			holder.miniImage = view.findViewById(R.id.miniImage);
			holder.containerSender = view.findViewById(R.id.container_sender);
			holder.containerMessage = view.findViewById(R.id.container_message);
			// Save the holder reference
			view.setTag(holder);
		} else {
			// Recover the saved holder
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		return view;
	}
}
