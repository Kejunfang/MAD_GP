package com.example.mad_gp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class ChatList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list); // 确保XML文件名一致

        // --- 1. 返回按钮 ---
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // --- 2. 搜索框 (装饰用) ---
        EditText etSearch = findViewById(R.id.etSearchChat);
        etSearch.setOnClickListener(v ->
                Toast.makeText(ChatList.this, "Search feature coming soon", Toast.LENGTH_SHORT).show()
        );

        // --- 3. 配置聊天列表项 (Chat Rows) ---

        // 获取 XML 中 include 的布局
        View item1 = findViewById(R.id.chatItem1);
        View item2 = findViewById(R.id.chatItem2);
        View item3 = findViewById(R.id.chatItem3);
        View item4 = findViewById(R.id.chatItem4);

        // 使用辅助方法设置每一行的数据
        // 参数：View, 名字, 最新消息, 时间, 是否有未读消息(红点)

        // 聊天 1: Dr. Sarah (有未读)
        setupChatRow(item1, "Dr. Sarah Tan", "Remember to practice your breathing...", "10:30 AM", true);

        // 聊天 2: Mr. John (无未读)
        setupChatRow(item2, "Mr. John Lee", "See you next Thursday!", "Yesterday", false);

        // 聊天 3: Alex (朋友)
        setupChatRow(item3, "Alex Chen", "The workshop was amazing!", "Yesterday", false);

        // 聊天 4: 这是一个空的模版，我们可以随便填
        setupChatRow(item4, "Support Team", "Welcome to MentaLeaf!", "Mon", false);
    }

    /**
     * 这是一个辅助方法，用来设置单行聊天的数据和点击事件
     */
    private void setupChatRow(View rowView, String name, String message, String time, boolean hasUnread) {
        if (rowView == null) return;

        // 找到这一行里面的控件 (对应 item_chat_row.xml 里的 ID)
        TextView tvName = rowView.findViewById(R.id.tvName);
        TextView tvMessage = rowView.findViewById(R.id.tvLastMessage);
        TextView tvTime = rowView.findViewById(R.id.tvTime);
        MaterialCardView badge = rowView.findViewById(R.id.badgeContainer);

        // 设置文字
        tvName.setText(name);
        tvMessage.setText(message);
        tvTime.setText(time);

        // 设置红点显隐
        if (hasUnread) {
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }

        // 设置点击跳转
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到聊天详情页 (ChatPage)
                Intent intent = new Intent(ChatList.this, Chat.class);

                // (可选) 你可以把名字传过去，让下一页标题变
                intent.putExtra("USER_NAME", name);

                startActivity(intent);
            }
        });
    }
}