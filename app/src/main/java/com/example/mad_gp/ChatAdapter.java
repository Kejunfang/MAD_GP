package com.example.mad_gp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ★★★ 修改点：这里加载新的 item_message.xml ★★★
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.tvMessage.setText(msg.getMessage());

        // 根据发送者调整样式
        if (msg.getSenderId().equals(currentUserId)) {
            // 我发的：靠右，绿色背景
            holder.messageContainer.setGravity(Gravity.END);
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_sent);
            holder.tvMessage.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            // 对方发的：靠左，灰色背景
            holder.messageContainer.setGravity(Gravity.START);
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_received);
            holder.tvMessage.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMessage;
        public LinearLayout messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // 绑定 item_message.xml 里的 ID
            tvMessage = itemView.findViewById(R.id.tvMessageContent);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
}