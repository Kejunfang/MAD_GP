package com.example.mad_gp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Chat extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // 确保对应你的聊天页面 XML

        // --- 1. 初始化控件 ---
        ImageButton btnBack = findViewById(R.id.btnBack);
        FloatingActionButton btnSend = findViewById(R.id.btnSend); // XML里用的FAB
        EditText etMessage = findViewById(R.id.etMessage);
        TextView tvUserName = findViewById(R.id.headerContainer).findViewById(R.id.tvName);
        // 注意：如果你 XML 里标题那个 TextView 没有 ID，你需要去 activity_chat.xml 给名字那个 TextView 加个 ID，比如 android:id="@+id/tvChatName"
        // 假设这里暂时没法改 XML，我们先略过改名，直接处理逻辑

        // --- 2. 接收传过来的名字 (可选优化) ---
        String nameFromIntent = getIntent().getStringExtra("USER_NAME");
        if (nameFromIntent != null) {
            // 如果你在 XML 里给标题 TextView 加了 ID (比如 tvChatName)，可以在这里 findViewById 并 setText
            // TextView title = findViewById(R.id.tvChatName);
            // title.setText(nameFromIntent);
        }

        // --- 3. 返回按钮 ---
        btnBack.setOnClickListener(v -> finish());

        // --- 4. 发送按钮 ---
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = etMessage.getText().toString().trim();

                if (!msg.isEmpty()) {
                    // 模拟发送成功
                    Toast.makeText(Chat.this, "Message Sent!", Toast.LENGTH_SHORT).show();

                    // 清空输入框
                    etMessage.setText("");
                } else {
                    // 输入为空时的提示
                    Toast.makeText(Chat.this, "Please type a message", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}