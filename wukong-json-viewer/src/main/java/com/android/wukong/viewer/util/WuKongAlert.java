package com.android.wukong.viewer.util;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.wukong.viewer.WuKongApi;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class WuKongAlert {

    public static Context getTopActivity() {
        return WuKongApi.getTopActivity();
    }

    // AlertDialog.Builder 的创建倒不用在主线程, 但show必须在主线程

    public static abstract class AlertDialogBuilderHandler {

        public void beforeShow(AlertDialog.Builder builder, AlertDialog dialog) {
        }

        public void afterShow(AlertDialog dialog) {
        }

    }

    // 普通消息对话框
    public static void show(String title, String message) {
        show(title, message, "确定", null, null, null, null);
    }

    public static void show(String title, String message, AlertDialogBuilderHandler handler) {
        show(title, message, "确定", null, null, null, handler);
    }

    public static void showWarning(String title, String message) {
        show(title, message, "确定", null, null, null, new AlertDialogBuilderHandler() {
            @Override
            public void beforeShow(AlertDialog.Builder builder, AlertDialog dialog) {
                builder.setCancelable(false);
            }
        });
    }

    public static void show(String title, String message, String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        show(title, message, positiveButtonText, positiveListener, null, null, null);
    }

    public static void showWarning(String title, String message, String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        show(title, message, positiveButtonText, positiveListener, null, null, new AlertDialogBuilderHandler() {
            @Override
            public void beforeShow(AlertDialog.Builder builder, AlertDialog dialog) {
                builder.setCancelable(false);
            }
        });
    }

    public static void showWarning(String title, String message
            , String positiveButtonText, DialogInterface.OnClickListener positiveListener
            , String negativeButtonText, DialogInterface.OnClickListener negativeListener) {
        show(title, message, positiveButtonText, positiveListener, negativeButtonText, negativeListener, new AlertDialogBuilderHandler() {
            @Override
            public void beforeShow(AlertDialog.Builder builder, AlertDialog dialog) {
                builder.setCancelable(false);
            }
        });
    }

    public static void show(String title, final String message,
                            String positiveButtonText, DialogInterface.OnClickListener positiveListener,
                            String negativeButtonText, DialogInterface.OnClickListener negativeListener) {
        show(title, message, positiveButtonText, positiveListener, negativeButtonText, negativeListener, null);
    }

    public static void show(final String title, final String message,
                            final String positiveButtonText, final DialogInterface.OnClickListener positiveListener,
                            final String negativeButtonText, final DialogInterface.OnClickListener negativeListener, final AlertDialogBuilderHandler handler) {

        show(title, message,
                positiveButtonText, positiveListener,
                negativeButtonText, negativeListener,
                null, null, handler
        );
    }

    public static void show(final String title, final String message,
                            final String positiveButtonText, final DialogInterface.OnClickListener positiveListener,
                            final String negativeButtonText, final DialogInterface.OnClickListener negativeListener,
                            final String neutralButtonText, final DialogInterface.OnClickListener neutralListener,
                            final AlertDialogBuilderHandler handler) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Context mActivity = getTopActivity();
                if (mActivity == null) {
                    return;
                }

                // java.lang.IllegalArgumentException: View=DecorView@343bf6a["Requesting ..."] not attached to window manager
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
                dialogBuilder.setTitle(title);
                dialogBuilder.setMessage(message);

                if (positiveButtonText != null) {
                    dialogBuilder.setPositiveButton(positiveButtonText, positiveListener);
                }
                if (negativeButtonText != null) {
                    dialogBuilder.setNegativeButton(negativeButtonText, negativeListener);
                }
                if (neutralButtonText != null) {
                    dialogBuilder.setNeutralButton(neutralButtonText, neutralListener);
                }

                AlertDialog dialog = dialogBuilder.create();
                if (handler != null) {
                    handler.beforeShow(dialogBuilder, dialog);
                }

                // android.view.WindowLeaked: Activity SplashActivity has leaked window DecorView@713940f[SplashActivity] that was originally added here
                dialog.show();

                if (handler != null) {
                    handler.afterShow(dialog);
                }
            }
        });
    }

    // 单选对话框
    public static void showSingleChoiceDialog(String title, String[] items, int checkedItem, DialogInterface.OnClickListener itemsListener,
                                              String positiveButtonText, DialogInterface.OnClickListener positiveListener) {
        showSingleChoiceDialog(title, items, checkedItem, itemsListener, positiveButtonText, positiveListener, null, null);

    }

    public static void showSingleChoiceDialog(final String title, final String[] items, final int checkedItem, final DialogInterface.OnClickListener itemsListener,
                                              final String positiveButtonText, final DialogInterface.OnClickListener positiveListener,
                                              final String negativeButtonText, final DialogInterface.OnClickListener negativeListener) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                Context mActivity = getTopActivity();
                if (mActivity == null) {
                    return;
                }
                AlertDialog.Builder normalDialog = new AlertDialog.Builder(mActivity);
                normalDialog.setTitle(title);

                final int[] youSelected = new int[1];
                normalDialog.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
                    youSelected[0] = which;
                    if (itemsListener != null) {
                        itemsListener.onClick(dialog, which);
                    }
                });

                normalDialog.setPositiveButton(positiveButtonText, (dialog, which) -> {
                    if (positiveListener != null) {
                        positiveListener.onClick(dialog, youSelected[0]);
                    }
                });

                if (negativeButtonText != null) {
                    normalDialog.setNegativeButton(negativeButtonText, negativeListener);
                }


                normalDialog.show();
            }
        });

    }

    // 操作对话框
    public static void showActionsDialog(final String title, final String[] actionItems, final DialogInterface.OnClickListener itemsListener) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Context mActivity = getTopActivity();
            if (mActivity == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            if (title != null) {
                builder.setTitle(title);
            }
            builder.setItems(actionItems, itemsListener);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }


    // 输入框对话框
    public static AlertDialog.Builder showEditTextDialog(final String title, final String hints, final String positiveButtonText, final DialogInterface.OnClickListener positiveListener) {
        return showEditTextDialog(title, hints, positiveButtonText, positiveListener, null, null);
    }

    public static AlertDialog.Builder showEditTextDialog(final String title, final String hints,
                                                         final String positiveButtonText, final DialogInterface.OnClickListener positiveListener,
                                                         final String negativeButtonText, final DialogInterface.OnClickListener negativeListener) {
        return showEditTextDialog(title, hints, null, positiveButtonText, positiveListener, negativeButtonText, negativeListener);
    }

    public static AlertDialog.Builder showEditTextDialog(final String title, final String hints, final String text,
                                                         final String positiveButtonText, final DialogInterface.OnClickListener positiveListener,
                                                         final String negativeButtonText, final DialogInterface.OnClickListener negativeListener) {
        final Context mActivity = getTopActivity();
        if (mActivity == null) {
            return null;
        }
        final AlertDialog.Builder inputDialog = new AlertDialog.Builder(mActivity);

        new Handler(Looper.getMainLooper()).post(() -> {
            EditText editText = new EditText(mActivity);
            if (hints != null) {
                editText.setHint(hints);
            }
            if (text != null) {
                editText.setText(text);
            }
            inputDialog.setTitle(title).setView(editText);
            inputDialog.setPositiveButton(positiveButtonText, positiveListener);
            if (negativeButtonText != null) {
                inputDialog.setNegativeButton(negativeButtonText, negativeListener);    // negativeListener is null is OK
            }

            inputDialog.show();
        });
        return inputDialog;
    }

    public static String getTextInShowedEditTextDialog(DialogInterface dialogInterface) {
        Object mAlert = WuKongReflect.getFieldValue(dialogInterface, "mAlert");
        EditText mView = (EditText) WuKongReflect.getFieldValue(mAlert, "mView");
        return mView != null && mView.getText() != null ? mView.getText().toString() : "";
    }

    // ProgressDialog 的 create与show 都必须在主线程
    // 进度条对话框
    public static int ProgressStyle = ProgressDialog.STYLE_SPINNER;

    public static ProgressDialogEx showProgressDialog(String title) {
        return showProgressDialog(title, ProgressStyle);
    }

    public static ProgressDialogEx showProgressDialog(final String title, final int progressStyle) {
        Context mActivity = getTopActivity();
        return showProgressDialog(title, progressStyle, mActivity);
    }

    public static ProgressDialogEx showProgressDialog(final String title, final int progressStyle, Context mActivity) {
        if (mActivity == null) {
            mActivity = getTopActivity();
        }
        if (mActivity == null) {
            return null;
        }
        int MAX_PROGRESS = 100;
        // 必须在主线程 或者 调用了Looper.prepare()的线程: Can't create handler inside thread that has not called Looper.prepare()
        final ProgressDialogEx progressDialog = new ProgressDialogEx(mActivity);
        progressDialog.setProgress(0);          // 这个方法调用倒不用必须在主线程
        progressDialog.setTitle(title);         // 这个也是不必在主线程，也可以
        // progressDialog.setMessage("xxx");    // 必须在主线程
        progressDialog.setProgressStyle(progressStyle);
        progressDialog.setMax(MAX_PROGRESS);

        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();                  // 必须在主线程
        return progressDialog;
    }

    public static class ProgressDialogEx extends ProgressDialog {

        public Object vessel = null;

        public ProgressDialogEx(Context context) {
            super(context);
        }

        @Override
        public void setMessage(CharSequence message) {
            final CharSequence text = message;
            new Handler(Looper.getMainLooper()).post(() -> superSetMessage(text));
        }

        @Override
        public void setTitle(CharSequence title) {
            final CharSequence text = title;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                superSetTitle(text);
            } else {
                new Handler(Looper.getMainLooper()).post(() -> superSetTitle(text));
            }
        }

        @Override
        public void show() {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                superShow();
            } else {
                new Handler(Looper.getMainLooper()).post(this::superShow);
            }
        }

        private void superSetMessage(CharSequence message) {
            super.setMessage(message);
        }

        private void superSetTitle(CharSequence title) {
            super.setTitle(title);
        }

        private void superShow() {
            super.show();
        }
    }

    // 日期选择
    public static abstract class DatePickerDialogCallback {
        public void onSure(DatePicker view, int year, int month, int dayOfMonth, Date date) {
        }
    }

    public static DatePickerDialog showDatePicker(final DatePickerDialogCallback datePickerDialogCallback) {
        return showDatePicker(null, datePickerDialogCallback);
    }

    public static DatePickerDialog showDatePicker(String title, final DatePickerDialogCallback datePickerDialogCallback) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final Context mContext = getTopActivity();

        int style = android.R.style.Theme_DeviceDefault_Dialog;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            style = android.R.style.Theme_DeviceDefault_Dialog_Alert;
        }
        final DatePickerDialog mDatePickerDialog = new DatePickerDialog(mContext,
                style,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, year1);
                    cal.set(Calendar.MONTH, month1);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    Date date = cal.getTime();
                    if (datePickerDialogCallback != null) {
                        datePickerDialogCallback.onSure(view, year1, month1, dayOfMonth, date);
                    }
                }, year, month, day);

        new Handler(Looper.getMainLooper()).post(mDatePickerDialog::show);

        if (title != null) {
            mDatePickerDialog.setTitle(title);  // set text first, then the mTitleView var is assigned
            new Handler(Looper.getMainLooper()).post(() -> {
                Object mAlert = WuKongReflect.getFieldValue(mDatePickerDialog, "mAlert"); // AlertController
                TextView mTitleView = (TextView) WuKongReflect.getFieldValue(mAlert, "mTitleView"); // TextView
                if (mTitleView != null) {
                    mTitleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    // mTitleView.setGravity(Gravity.CENTER);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mTitleView.getLayoutParams();
                    params.bottomMargin = 20;
                    mTitleView.setLayoutParams(params);
                }

            });
        }
        return mDatePickerDialog;
    }

}
