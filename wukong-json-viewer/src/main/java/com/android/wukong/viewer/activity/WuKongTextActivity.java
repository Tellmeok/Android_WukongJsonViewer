package com.android.wukong.viewer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.wukong.viewer.WuKongApi;
import com.android.wukong.viewer.R;
import com.android.wukong.viewer.util.json.WuKongJSONUtil;

public class WuKongTextActivity extends AppCompatActivity {

    public static final String INTENT_KEY_OF_TITLE = "title";
    public static final String INTENT_KEY_OF_CONTENTS = "text_view_contents";
    public static final String INTENT_KEY_OF_FILEPATH = "text_view_file_path";
    public static final String INTENT_KEY_OF_EDITABLE = "is_editable";

    public static interface SaveEvent {
        public void onSave(String content);
    }

    public SaveEvent saveEvent = null;

    protected TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wukong_activity_text_view);

        String title = getIntent().getStringExtra(INTENT_KEY_OF_TITLE);
        if (title == null) {
            title = "__To_Be_Set__";
        }

        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.kWuKong_toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setTitle(title);
        }

        textView = findViewById(R.id.kWuKong_text_view);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        // 设置内容
        String contents = getIntent().getStringExtra(INTENT_KEY_OF_CONTENTS);
        String filePath = getIntent().getStringExtra(INTENT_KEY_OF_FILEPATH);
        if (contents != null) {
            textView.setText(contents);
        } else if (filePath != null) {
            String text = WuKongJSONUtil.readFileToText(filePath);
            textView.setText(text);
        }
    }

    private static final int __itemId_edit__ = 10088;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra(INTENT_KEY_OF_EDITABLE, false)) {
            MenuItem item = menu.add(Menu.NONE, __itemId_edit__, Menu.NONE, "编辑");
            item.setIcon(R.mipmap.wukong_icon_edit);
            item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {     // back button
            finish();
        }
        if (itemId == __itemId_edit__) {
            CharSequence titleObj = item.getTitle();
            if (titleObj == null) return true;
            String title = titleObj.toString();
            if (title.equals("编辑")) {
                item.setIcon(R.mipmap.wukong_icon_save);
                item.setTitle("保存");
                setEditTextMode();
            } else if (title.equals("保存")) {
                item.setIcon(R.mipmap.wukong_icon_edit);
                item.setTitle("编辑");
                saveTextEvent();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditTextMode() {
        EditText editText = new EditText(this);
        editText.setHint("请输入...");
        editText.setTextSize(20);
        editText.setGravity(Gravity.TOP);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.WHITE);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.kWuKong_toolbar_under_line);
        editText.setLayoutParams(layoutParams);

        editText.setText(textView.getText());
        ((ViewGroup) textView.getParent()).addView(editText);
        ((ViewGroup) textView.getParent()).setTag(editText);
    }

    private void saveTextEvent() {
        EditText editText = (EditText) ((ViewGroup) textView.getParent()).getTag();
        if (editText != null) {
            textView.setText(editText.getText());
            ((ViewGroup) textView.getParent()).removeView(editText);

            String content = textView.getText().toString();

            String filePath = getIntent().getStringExtra(INTENT_KEY_OF_FILEPATH);
            if (filePath != null) {
                WuKongJSONUtil.writeTextToFile(content, filePath);
            }

            if (saveEvent != null) {
                try {
                    saveEvent.onSave(content);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Static Method
     */
    public static void show(String title, String content) {
        show(title, content, null, false);
    }

    public static void show(String title, String content, String filePath, boolean isEditable) {
        Intent intent = new Intent(WuKongApi.getTopActivity(), WuKongTextActivity.class);
        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_TITLE, title);
        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_CONTENTS, content);
        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_FILEPATH, filePath);
        intent.putExtra(WuKongTextActivity.INTENT_KEY_OF_EDITABLE, isEditable);
        WuKongApi.getTopActivity().startActivity(intent);
    }
}
