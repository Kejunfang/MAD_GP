package com.example.mad_gp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide; // 必须确保有 Glide
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;

public class DailyTipsDetails extends AppCompatActivity {

    private TextView tvArticleTitle, tvArticleContent, tvArticleAuthor, tvArticleDate;
    private ImageView ivArticleImage;
    private CollapsingToolbarLayout collapsingToolbar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_tips_details);

        // --- 1. UI 沉浸式设置 ---
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.setStatusBarColor(Color.TRANSPARENT);

        // --- 2. 初始化 Firebase ---
        db = FirebaseFirestore.getInstance();

        // --- 3. 绑定控件 ---
        AppBarLayout appBarLayout = findViewById(R.id.appBar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageButton btnBack = findViewById(R.id.btnBack);

        ivArticleImage = findViewById(R.id.ivArticleImage);
        tvArticleTitle = findViewById(R.id.tvArticleTitle);
        tvArticleContent = findViewById(R.id.tvArticleContent);
        tvArticleAuthor = findViewById(R.id.tvArticleAuthor);
        tvArticleDate = findViewById(R.id.tvArticleDate);

        // 初始化 Toolbar 样式
        collapsingToolbar.setTitle("");
        toolbar.setBackgroundColor(Color.TRANSPARENT);

        // --- 4. 处理滚动时的标题变化 ---
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    // 折叠状态：显示标题，白色背景，黑色按钮
                    collapsingToolbar.setTitle(tvArticleTitle.getText());
                    collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.text_main));
                    toolbar.setBackgroundColor(getResources().getColor(R.color.white));
                    btnBack.setColorFilter(getResources().getColor(R.color.text_main));
                    isShow = true;
                } else if (verticalOffset == 0 || isShow) {
                    // 展开状态：隐藏标题，透明背景
                    collapsingToolbar.setTitle("");
                    toolbar.setBackgroundColor(Color.TRANSPARENT);
                    btnBack.setColorFilter(getResources().getColor(R.color.text_main));
                    isShow = false;
                }
            }
        });

        // 返回按钮逻辑
        btnBack.setOnClickListener(v -> finish());

        // --- 5. 获取数据 ---
        // 获取 Intent 传过来的 ID
        String tipId = getIntent().getStringExtra("TIP_ID");

        if (tipId != null) {
            loadTipDetails(tipId);
        } else {
            Toast.makeText(this, "Error: No tip ID found.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- 加载文章详情 ---
    private void loadTipDetails(String tipId) {
        db.collection("daily_tips").document(tipId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String content = documentSnapshot.getString("content");
                        String author = documentSnapshot.getString("author");
                        String date = documentSnapshot.getString("date");

                        // 获取图片链接 (这里是你上传到 Firebase Storage 后的 URL)
                        String imageUrl = documentSnapshot.getString("imageUrl");

                        // 处理内容中的换行符 (Firebase Console 里输入的 \n 有时会变成 \\n)
                        if (content != null) {
                            content = content.replace("\\n", "\n");
                        }

                        // 设置文字
                        tvArticleTitle.setText(title);
                        tvArticleContent.setText(content);
                        tvArticleAuthor.setText(author != null ? author : "MentaLeaf Team");
                        tvArticleDate.setText("•  " + (date != null ? date : ""));

                        // --- 关键修改：加载网络图片 ---
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl) // 直接加载 URL
                                    .placeholder(R.drawable.studydesk) // 加载过程中显示的默认图 (可以换成别的 loading 图)
                                    .error(R.drawable.studydesk)       // 如果链接错误或加载失败显示的图
                                    .centerCrop()
                                    .into(ivArticleImage);
                        } else {
                            // 如果数据库里没有 imageUrl，显示默认图
                            ivArticleImage.setImageResource(R.drawable.studydesk);
                        }
                    } else {
                        Toast.makeText(this, "Article not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load article: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}