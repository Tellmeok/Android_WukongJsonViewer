package com.android.wukong.viewer.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;


import com.android.wukong.viewer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义弹出菜单视图类
 */
public class WuKongAction {
    public Dialog dialog;

    public TextView textViewTitle;
    public TextView textViewCancel;

    private ScrollView contentScrollView;
    private LinearLayout contentLinearLayout;

    public List<ActionSheetItem> sheetItemList;

    public Context getContext() {
        return WuKongAlert.getTopActivity();
    }

    public WuKongAction builder() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.wukong_action_sheet_view, null);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);

        view.setMinimumWidth(outMetrics.widthPixels);
        view.setBackgroundColor(Color.argb(0, 79, 79, 79));

        dialog = new Dialog(getContext(), 0);
        dialog.setContentView(view);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        // dialogWindow.setDimAmount(0f);
        dialogWindow.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.x = 0;
        layoutParams.y = 0;
        dialogWindow.setAttributes(layoutParams);
        dialogWindow.setWindowAnimations(R.style.WechatTheme_ActionSheetStyle);

        contentScrollView = (ScrollView) view.findViewById(R.id.wukongSheetScrollView);
        contentLinearLayout = (LinearLayout) view.findViewById(R.id.wukongSheetContentLayout);
        textViewTitle = (TextView) view.findViewById(R.id.wukongSheetTitleText);
        textViewCancel = (TextView) view.findViewById(R.id.wukongSheetCancelText);
        textViewCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        return this;
    }

    public WuKongAction setTitle(String title) {
        textViewTitle.setText(title);
        if (title == null) {
            textViewTitle.setVisibility(View.GONE);
        } else {
            textViewTitle.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public WuKongAction setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public WuKongAction setCanceledOnTouchOutside(boolean cancel) {
        dialog.setCanceledOnTouchOutside(cancel);
        return this;
    }

    public WuKongAction addSheetItem(String strItem, Object object, int color, Drawable drawable, OnSheetItemClickListener listener) {
        if (sheetItemList == null) {
            sheetItemList = new ArrayList<>();
        }
        sheetItemList.add(new ActionSheetItem(strItem, object, color, drawable, listener));
        return this;
    }

    private void setSheetItems() {
        if (sheetItemList == null || sheetItemList.size() <= 0) {
            return;
        }

        int size = sheetItemList.size();

        for (int i = 1; i <= size; i++) {
            final int index = i;
            final ActionSheetItem sheetItem = sheetItemList.get(i - 1);
            String itemText = sheetItem.name;
            Drawable drawable = sheetItem.drawable;

            TextView textView = new TextView(getContext());
            if (itemText.contains("android.text.Html.fromHtml()")) {
                textView.setText(android.text.Html.fromHtml(itemText.replace("android.text.Html.fromHtml()", "")));
            } else {
                textView.setText(itemText);
            }
            textView.setTextSize(18);
            textView.setGravity(Gravity.CENTER);

            if (drawable != null) {
                textView.setBackground(drawable);
            } else {
                textView.setBackgroundResource(R.drawable.wukong_action_sheet_item_background);
            }
            textView.setTextColor(sheetItem.color);

            float scale = getContext().getResources().getDisplayMetrics().density;
            int height = (int) (45 * scale + 0.5f);

            // 空行
            if (itemText.isEmpty()) {
                textView.setBackgroundColor(Color.TRANSPARENT);
                height = 18;
            }

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            layoutParams.topMargin = 1;
            textView.setLayoutParams(layoutParams);
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sheetItem.itemClickListener.onClick(index, sheetItem);
                    dialog.dismiss();
                }
            });

            contentLinearLayout.addView(textView);
        }
    }

    public void show() {
        setSheetItems();
        dialog.show();
    }

    public interface OnSheetItemClickListener {
        void onClick(int which, ActionSheetItem sheetItem);
    }

    public class ActionSheetItem {
        public String name;     // title text

        public Object object;   // holder object

        public int color;       // text color

        public Drawable drawable;   // background drawable

        public OnSheetItemClickListener itemClickListener;

        public ActionSheetItem(String name, Object object, int color, OnSheetItemClickListener itemClickListener) {
            this.name = name;
            this.color = color;
            this.object = object;
            this.itemClickListener = itemClickListener;
        }

        public ActionSheetItem(String name, Object object, int color, Drawable drawable, OnSheetItemClickListener itemClickListener) {
            this(name, object, color, itemClickListener);
            this.drawable = drawable;
        }

    }

    /**
     * Static Methods
     */
    public static WuKongAction showActions(String title, String[] items, OnSheetItemClickListener listener) {
        return showActions(title, items, null, listener);
    }

    public static WuKongAction showActions(String title, String[] items, int[] colors, OnSheetItemClickListener listener) {
        return showActions(title, items, colors, null, listener);
    }

    public static WuKongAction showActions(String title, String[] items, int[] colors, Drawable[] drawables, OnSheetItemClickListener listener) {
        final WuKongAction dialog = new WuKongAction();
        dialog.builder();
        dialog.setTitle(title);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        if (colors == null) {
            colors = new int[]{Color.BLACK};
        }

        Drawable[] drawableList = new Drawable[items.length];
        for (int i = 0; i < items.length; i++) {
            Drawable drawable = drawables != null && drawables.length > i ? drawables[i] : null;
            if (drawable == null) {
                StateListDrawable d = new StateListDrawable();
                d.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.RED));
                d.addState(new int[]{}, new ColorDrawable(Color.GRAY));
                drawable = d;
            }
            drawableList[i] = drawable;
        }

        for (int i = 0; i < items.length; i++) {
            String actionString = items[i];
            int color = colors.length > i ? colors[i] : colors[colors.length - 1];
            dialog.addSheetItem(actionString, null, color, drawableList[i], listener);
        }

        // show
        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });

        return dialog;
    }

}
