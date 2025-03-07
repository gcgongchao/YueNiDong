package com.qinjia.adapter;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeMessage;
import com.gotye.api.GotyeMessageType;
import com.gotye.api.GotyeUser;
import com.gotye.api.Media;
import com.qinjia.activity.ChatPage;
import com.qinjia.activity.ShowBigImage;
import com.qinjia.util.BitmapUtil;
import com.qinjia.util.GotyeVoicePlayClickListener;
import com.qinjia.util.ImageCache;
import com.qinjia.util.TimeUtil;
import com.qinjia.util.ToastUtil;
import com.yuenidong.activity.R;
import com.yuenidong.common.AppData;

public class ChatMessageAdapter extends BaseAdapter {

	public static final int TYPE_RECEIVE_TEXT = 0;
	public static final int TYPE_RECEIVE_IMAGE = 1;
	public static final int TYPE_RECEIVE_VOICE = 2;
	public static final int TYPE_RECEIVE_USER_DATA = 3;

	public static final int TYPE_SEND_TEXT = 4;
	public static final int TYPE_SEND_IMAGE = 5;
	public static final int TYPE_SEND_VOICE = 6;
	public static final int TYPE_SEND_USER_DATA = 7;

	public static final int MESSAGE_DIRECT_RECEIVE = 1;
	public static final int MESSAGE_DIRECT_SEND = 0;

	private ChatPage chatPage;
	private List<GotyeMessage> messageList;

	private LayoutInflater inflater;
	private String currentLoginName;

	private GotyeAPI api;

    private String friendName;

	public ChatMessageAdapter(ChatPage activity, List<GotyeMessage> messageList,String friendName) {
		this.chatPage = activity;
		this.messageList = messageList;
		inflater = activity.getLayoutInflater();
		api = GotyeAPI.getInstance();
		currentLoginName = api.getCurrentLoginUser().getName();
        this.friendName=friendName;
	}

	public void addMsgToBottom(GotyeMessage msg) {
		messageList.add(msg);
		notifyDataSetChanged();
	}

	public void updateMessage(GotyeMessage msg) {
		int position = messageList.indexOf(msg);
		if (position < 0) {
			return;
		}
		messageList.remove(position);
		messageList.add(position, msg);
		notifyDataSetChanged();
	}

	public void updateChatMessage(GotyeMessage msg) {
		if (messageList.contains(msg)) {
			int index = messageList.indexOf(msg);
			messageList.remove(index);
			messageList.add(index, msg);
			notifyDataSetChanged();
		}
	}

	// public GotyeMessageProxy getLastMessage() {
	// if (messageList == null || messageList.size() == 0) {
	// return null;
	// } else {
	// return messageList.get(messageList.size() - 1);
	// }
	// }

	public void addMessagesToTop(List<GotyeMessage> histMessages) {
		messageList.addAll(0, histMessages);
	}

	public void addMessageToTop(GotyeMessage msg) {
		messageList.add(0, msg);
	}

	@Override
	public int getCount() {
		return messageList.size();
	}

	@Override
	public GotyeMessage getItem(int position) {
		return position >= 0 ? messageList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		GotyeMessage message = getItem(position);
		if (message.getType() == GotyeMessageType.GotyeMessageTypeText) {
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_TEXT
					: TYPE_SEND_TEXT;
		}
		if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_IMAGE
					: TYPE_SEND_IMAGE;

		}
		if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_VOICE
					: TYPE_SEND_VOICE;
		}
		if (message.getType() == GotyeMessageType.GotyeMessageTypeUserData) {
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? TYPE_RECEIVE_USER_DATA
					: TYPE_SEND_USER_DATA;
		}
		return -1;// invalid
	}

	public int getViewTypeCount() {
		return 8;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final GotyeMessage message = getItem(position);
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = createViewByMessage(message, position);
			if (message.getType() == GotyeMessageType.GotyeMessageTypeImage) {
				holder.iv = ((ImageView) convertView
						.findViewById(R.id.iv_sendPicture));
				holder.head_iv = (ImageView) convertView
						.findViewById(R.id.iv_userhead);
				holder.tv = (TextView) convertView
						.findViewById(R.id.percentage);
				holder.pb = (ProgressBar) convertView
						.findViewById(R.id.progressBar);
				holder.staus_iv = (ImageView) convertView
						.findViewById(R.id.msg_status);
				holder.tv_userId = (TextView) convertView
						.findViewById(R.id.tv_userid);
			} else if (message.getType() == GotyeMessageType.GotyeMessageTypeAudio) {
				holder.iv = ((ImageView) convertView
						.findViewById(R.id.iv_voice));
				holder.head_iv = (ImageView) convertView
						.findViewById(R.id.iv_userhead);
				holder.tv = (TextView) convertView.findViewById(R.id.tv_length);
				holder.pb = (ProgressBar) convertView
						.findViewById(R.id.pb_sending);
				holder.staus_iv = (ImageView) convertView
						.findViewById(R.id.msg_status);
				holder.tv_userId = (TextView) convertView
						.findViewById(R.id.tv_userid);
				holder.iv_read_status = (ImageView) convertView
						.findViewById(R.id.iv_unread_voice);
			} else {
				holder.pb = (ProgressBar) convertView
						.findViewById(R.id.pb_sending);
				holder.staus_iv = (ImageView) convertView
						.findViewById(R.id.msg_status);
				holder.head_iv = (ImageView) convertView
						.findViewById(R.id.iv_userhead);
				// 这里是文字内容
				holder.tv = (TextView) convertView
						.findViewById(R.id.tv_chatcontent);
				holder.tv_userId = (TextView) convertView
						.findViewById(R.id.tv_userid);
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (holder.tv_userId != null) {
//			holder.tv_userId.setText(message.getSender().name);
			holder.tv_userId.setText(friendName);
		}

		switch (message.getType()) {
		// 根据消息type显示item
		case GotyeMessageTypeImage: // 图片
			handleImageMessage(message, holder, position, convertView);
			break;
		case GotyeMessageTypeAudio: // 语音
			handleVoiceMessage(message, holder, position, convertView);
			break;
		default:
			handleTextMessage(message, holder, position);
			break;
		}

		TextView timestamp = (TextView) convertView
				.findViewById(R.id.timestamp);

		// if (position == 0) {
		timestamp.setText(TimeUtil.dateToMessageTime(message.getDate() * 1000));
		timestamp.setVisibility(View.VISIBLE);
		// } else {
		// 两条消息时间离得如果稍长，显示时间
		// if (TimeUtil.needShowTime(message.getDate(), messageList.get(position
		// - 1).getDate())) {
		// timestamp.setText(TimeUtil.toLocalTimeString(message.getDate()*1000));
		// timestamp.setVisibility(View.VISIBLE);
		// } else {
		// timestamp.setVisibility(View.GONE);
		// }
		// }
		setIcon(holder.head_iv, message.getSender().name);
		return convertView;
	}

	private void handleImageMessage(final GotyeMessage message,
			final ViewHolder holder, final int position, View convertView) {
		holder.iv.setImageResource(R.drawable.ic_launcher);
		setImageMessage(holder.iv, message, holder);
		// setIcon(holder.head_iv, message.gotyeMessage.getSender().name);

		if (getDirect(message) == MESSAGE_DIRECT_SEND) {
			switch (message.getStatus()) {
			case GotyeMessage.STATUS_SENT: // 发送成功
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.GONE);
				break;
			case GotyeMessage.STATUS_SENDFAILED: // 发送失败
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.VISIBLE);
				break;
			case GotyeMessage.STATUS_SENDING: // 发送中
				holder.pb.setVisibility(View.VISIBLE);
				holder.staus_iv.setVisibility(View.GONE);
				break;
			default:
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.GONE);
			}
		}
	}

	private void handleTextMessage(GotyeMessage message, ViewHolder holder,
			final int position) {
		// 设置内容
		String extraData=message.getExtraData()==null?null:new String(message.getExtraData());
		if(extraData!=null){
			if(message.getType()==GotyeMessageType.GotyeMessageTypeText){
				holder.tv.setText(message.getText()+"\n额外数据："+extraData);
			}else{
				holder.tv.setText("自定义消息："+new String(message.getUserData())+"\n额外数据："+extraData);
			}
		}else{
			if(message.getType()==GotyeMessageType.GotyeMessageTypeText){
				holder.tv.setText(message.getText());
			}else{
				holder.tv.setText("自定义消息："+new String(message.getUserData()));
			}
		}
		
		// 设置长按事件监听
		if (getDirect(message) == MESSAGE_DIRECT_SEND) {
			switch (message.getStatus()) {
			case GotyeMessage.STATUS_SENT: // 发送成功
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.GONE);
				break;
			case GotyeMessage.STATUS_SENDFAILED: // 发送失败
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.VISIBLE);
				break;
			case GotyeMessage.STATUS_SENDING: // 发送中
				holder.pb.setVisibility(View.VISIBLE);
				holder.staus_iv.setVisibility(View.GONE);
				break;
			default:
				holder.pb.setVisibility(View.GONE);
				holder.staus_iv.setVisibility(View.GONE);
			}
		}
	}

	private void handleVoiceMessage(final GotyeMessage message,
			final ViewHolder holder, final int position, View convertView) {
		holder.tv.setText(TimeUtil.getVoiceTime(message.getMedia()
				.getDuration()));
		holder.iv.setOnClickListener(new GotyeVoicePlayClickListener(message,
				holder.iv, this, chatPage));
		boolean isPlaying = isPlaying(message);
		if (isPlaying) {
			AnimationDrawable voiceAnimation;
			if (getDirect(message) == MESSAGE_DIRECT_RECEIVE) {
				holder.iv.setImageResource(R.anim.voice_from_icon);
			} else {
				holder.iv.setImageResource(R.anim.voice_to_icon);
			}
			voiceAnimation = (AnimationDrawable) holder.iv.getDrawable();
			voiceAnimation.start();
		} else {
			if (getDirect(message) == MESSAGE_DIRECT_RECEIVE) {
				holder.iv.setImageResource(R.drawable.chatfrom_voice_playing);
			} else {
				holder.iv.setImageResource(R.drawable.chatto_voice_playing);
			}
		}
		if (getDirect(message) == MESSAGE_DIRECT_RECEIVE) {
			if (message.getStatus() == GotyeMessage.ACK_UNREAD) {// if
				// holder.iv_read_status.setVisibility(View.INVISIBLE);
				holder.iv_read_status.setVisibility(View.VISIBLE);
			} else {
				holder.iv_read_status.setVisibility(View.INVISIBLE);
			}
			return;
		}

		// until here, deal with send voice msg
		switch (message.getStatus()) {
		case GotyeMessage.STATUS_SENT:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		case GotyeMessage.STATUS_SENDFAILED:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.VISIBLE);
			break;
		case GotyeMessage.STATUS_SENDING:
			holder.pb.setVisibility(View.VISIBLE);
			holder.staus_iv.setVisibility(View.GONE);
			break;
		default:
			holder.pb.setVisibility(View.GONE);
			holder.staus_iv.setVisibility(View.GONE);
		}
		switch (message.getMedia().getStatus()) {
		case Media.MEDIA_STATUS_DOWNLOADING:
			holder.pb.setVisibility(View.VISIBLE);
			break;
		default:
			holder.pb.setVisibility(View.GONE);
			break;
		}
	}

	private boolean isPlaying(GotyeMessage msg) {
		long id = msg.getDbId();
		long pid = chatPage.getPlayingId();
		if (id == pid) {
			return true;
		} else {
			return false;
		}

	}

	private View createViewByMessage(GotyeMessage message, int position) {
		switch (message.getType()) {
		case GotyeMessageTypeImage:
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
					.inflate(R.layout.layout_row_received_picture, null) : inflater
					.inflate(R.layout.layout_row_sent_picture, null);

		case GotyeMessageTypeAudio:
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
					.inflate(R.layout.layout_row_received_voice, null) : inflater
					.inflate(R.layout.layout_row_sent_voice, null);
		case GotyeMessageTypeUserData:
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
					.inflate(R.layout.layout_row_received_message, null) : inflater
					.inflate(R.layout.layout_row_sent_message, null);
		default:
			return getDirect(message) == MESSAGE_DIRECT_RECEIVE ? inflater
					.inflate(R.layout.layout_row_received_message, null) : inflater
					.inflate(R.layout.layout_row_sent_message, null);
		}
	}

	private ImageCache cache = ImageCache.getInstance();

	private void setIcon(ImageView iconView, String name) {
		Bitmap bmp = cache.get(name);
		if (bmp != null) {
			iconView.setImageBitmap(bmp);
		} else {
			GotyeUser user = api.requestUserInfo(name, false);
			if (user != null && user.getIcon() != null) {
				bmp = cache.get(user.getIcon().path);
				if (bmp != null) {
					iconView.setImageBitmap(bmp);
					cache.put(name, bmp);
				} else {
					bmp = BitmapUtil.getBitmap(user.getIcon().getPath());
					if (bmp != null) {
						iconView.setImageBitmap(bmp);
						cache.put(name, bmp);
					} else {
						iconView.setImageResource(R.drawable.mini_avatar_shadow);
					}
				}
			} else {
				iconView.setImageResource(R.drawable.mini_avatar_shadow);
			}
		}
	}

	private void setImageMessage(ImageView msgImageView,
			final GotyeMessage msg, ViewHolder holder) {
		Bitmap cacheBm = cache.get(msg.getMedia().getPath());
		if (cacheBm != null) {
			msgImageView.setImageBitmap(cacheBm);
			holder.pb.setVisibility(View.GONE);
		} else if (msg.getMedia().getPath() != null) {
			Bitmap bm = BitmapUtil.getBitmap(msg.getMedia().getPath());
			if (bm != null) {
				msgImageView.setImageBitmap(bm);
				cache.put(msg.getMedia().getPath(), bm);
			}
			holder.pb.setVisibility(View.GONE);
		}
		msgImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(chatPage, ShowBigImage.class);
				String path = msg.getMedia().getPath_ex();
				if (!TextUtils.isEmpty(path) && new File(path).exists()) {
					Uri uri = Uri.fromFile(new File(path));
					intent.putExtra("uri", uri);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					chatPage.startActivity(intent);
                    AppData.getContext().startActivity(intent);
				} else {
					ToastUtil.show(chatPage, "正在下载...");
					api.downloadMessage(msg);
					return;
				}

			}
		});
		// holder.pb.setVisibility(View.VISIBLE);

	}

	private int getDirect(GotyeMessage message) {
		if (message.getSender().name.equals(currentLoginName)) {
			return MESSAGE_DIRECT_SEND;
		} else {
			return MESSAGE_DIRECT_RECEIVE;
		}
	}

	public void downloadDone(GotyeMessage msg) {
		if (msg.getType() == GotyeMessageType.GotyeMessageTypeImage) {
			// if (TextUtils.isEmpty(msg.getMedia().getPath_ex())) {
			// ToastUtil.show(chatPage, "图片下载失败");
			// return;
			// }
		}
		if (messageList.contains(msg)) {
			int index = messageList.indexOf(msg);
			messageList.remove(index);
			messageList.add(index, msg);
			notifyDataSetChanged();
		}
	}

	public static class ViewHolder {
		ImageView iv;
		TextView tv;
		ProgressBar pb;
		ImageView staus_iv;
		ImageView head_iv;
		TextView tv_userId;
		ImageView playBtn;
		TextView timeLength;
		TextView size;
		LinearLayout container_status_btn;
		LinearLayout ll_container;
		ImageView iv_read_status;
		// 显示已读回执状态
		TextView tv_ack;
		// 显示送达回执状态
		TextView tv_delivered;

		TextView tv_file_name;
		TextView tv_file_size;
		TextView tv_file_download_state;
	}

	public void refreshData(List<GotyeMessage> list) {
		// TODO Auto-generated method stub
		this.messageList = list;
		notifyDataSetChanged();
	}
}
