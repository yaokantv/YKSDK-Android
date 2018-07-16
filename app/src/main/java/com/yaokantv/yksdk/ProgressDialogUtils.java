package com.yaokantv.yksdk;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.yaokantv.api.Utility;

public class ProgressDialogUtils {

    private ProgressDialog dialog;

    private Context ctx;

    public final static int SHOW = 1, DISMISS = 0;

    public ProgressDialogUtils(Context ctx) {
        this.ctx = ctx;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW:
                    if (Utility.isEmpty(dialog)) {
                        dialog = new ProgressDialog(ctx);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setMessage("正在加载数据");
                    }
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    break;
                case DISMISS:
                    if (!Utility.isEmpty(dialog) && dialog.isShowing()) {
                        try {
                            dialog.dismiss();
                        } catch (Exception exp) {
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public void sendMessage(int what) {
        handler.sendEmptyMessage(what);
    }

    public void setMessage(String message) {
        if (!Utility.isEmpty(dialog)) {
            dialog.setMessage(message);
        } else {
            dialog = new ProgressDialog(ctx);
            dialog.setMessage(message);
        }
    }

    public void setCancelable(boolean cancelable) {
        if (!Utility.isEmpty(dialog)) {
            dialog.setCancelable(cancelable);
        }
    }
}
