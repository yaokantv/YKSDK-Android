package com.yaokantv.yksdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.yaokantv.api.YKanHttpListener;
import com.yaokantv.api.YkanIRInterface;
import com.yaokantv.api.YkanIRInterfaceImpl;
import com.yaokantv.model.BaseResult;
import com.yaokantv.model.DeviceType;
import com.yaokantv.model.KeyCode;
import com.yaokantv.model.MatchRemoteControl;
import com.yaokantv.model.MatchRemoteControlResult;
import com.yaokantv.model.OneKeyMatchKey;
import com.yaokantv.model.RemoteControl;
import com.yaokantv.model.YKError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class OneKeyMatchActivity extends Activity implements View.OnClickListener, YKanHttpListener {
    public static final String TAG = OneKeyMatchActivity.class.getName();
    private int tid;
    private int bid;
    private YkanIRInterface ykanInterface;
    private ProgressDialogUtils dialogUtils;
    private ListView listView;
    private List<MatchRemoteControl> list = new ArrayList<>();
    private ControlAdapter controlAdapter;
    private RemoteControl remoteControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onekey_match);
        if (getIntent() != null) {
            initView();
            dialogUtils = new ProgressDialogUtils(this);
            // 遥控云数据接口分装对象对象
            ykanInterface = new YkanIRInterfaceImpl();
        }
        tid = Integer.valueOf(AIR_TID);
    }

    private void initView() {
        listView = findViewById(R.id.lv_control);
        controlAdapter = new ControlAdapter();
        listView.setAdapter(controlAdapter);
    }

    String key;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_match:
                switch (tid) {
                    case DeviceType.AIRCONDITION:
                        key = "on";
                        break;
                    case DeviceType.MULTIMEDIA:
                        key = "ok";
                        break;
                    default:
                        key = "power";
                }
                showDlg();
                break;
        }
    }

    //Test1 康佳电视机
    private static final String TV_TID = "2";
    private static final String TV_BID = "426";
    //康佳电视机电源键
    private static final String TV_POWER_KEY = "iNJ0HC696TupFe+iM1HgaxaILEiGAf3mC0y6wvBcd3BAMmr+WlKguK5eWwJZ9MDyS9k2/5UbB8aLdPNHEkr/65vwwUeFo6VHBThGQG9FZPpOPz2JKrY2F5g/S93Exs5efsqjky/CVG7L9vzapJ55cdH8D0Jm5ZHlc3QYanj9q1EbTSQAv0Wc7VoYvzT1PTIUUDJi4hXTUuPLpvQui7Awvg==";
    //康佳电视机信号源键
    private static final String TV_SIGNAL_KEY = "GI+tAUXYamhCzXBINv7VFMMPNIHrPVnS+WaQWCuzBrud53M+9u5C5OLNG9f7eMyt1oJIZtOXlpMALD2HPTknIFNPHJgSc1UruY6FREIVjdl0JSMvaRgXBz62LkQYmY8IcVf0yNQdZvWIo1HTwzOlAjo2KgHAg+1axVznMADAhkznHlfTcTb9hmwHpPM0+LtretMhlpX9UzNJ0FNMhn9Q2EpkEnSsDpIsFX7tJz2XxERV6op4c+e2jSVC6OVDl3lGmYl/+sAciMXF+E3U4KP+jioiRwCXPCDpnwOzIT47rO3Ba4y0yuGqTHhRuSXT4bfQ";

    //Test2 格力空调
    private static final String AIR_TID = "7";
    private static final String AIR_BID = "104";
    private static final String AIR_ON_KEY = "dsVAOCXmNov35ih3fpX+wovriA29kSAPHQi1NvRsijiUxqSgHm54NNTX/9VqoVeCm0+XI2qThd0d1FFvgJjlxBVzzZnI9lceibR1D4fXbxjUwZjNIU8QRUUTnMPFX5B/+STlpybyWHPPm+YdD3XmSXmJnAjtomiorokbhi4Jr0HLLox8WenlzGG83iOfASUbqA7Tmc5OSzXNkj7xA2Z5Ua15jjGLL10t1VTZ3XS1vK8T39MyKf8xddYcmsMuXMC2VX+Y6pQPATE8z/+CL6NG/v7lzqMU2UOHjgImDa7G//2CpuFty7xPBXkap7A1xJAnrvV29Rm3AdaMzLmovmQr5AOVZ4ElmDn9AkSJpw3bkf18pIEg4EHhcO6VPLKDlIK3jv0ko5oE9qcEkPAnZHSUjg==";

    private void showDlg() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(OneKeyMatchActivity.this).setMessage("请学习后上传 " + key + " 键").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO 将学习到的cmd传入接口；
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                ykanInterface.oneKeyMatched("mac", "code", "tid", "bid", key, OneKeyMatchActivity.this);
                                //空调
                                ykanInterface.oneKeyMatched("mac", AIR_ON_KEY, AIR_TID, AIR_BID, key, OneKeyMatchActivity.this);
                                //电视机
//                                ykanInterface.oneKeyMatched("mac", TV_POWER_KEY, TV_TID, TV_BID, key, OneKeyMatchActivity.this);
//                                ykanInterface.oneKeyMatched("mac", TV_SIGNAL_KEY, TV_TID, TV_BID, key, OneKeyMatchActivity.this);
                            }
                        }).start();
                    }
                }).create().show();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onSuccess(BaseResult baseResult) {
        if (isFinishing()) {
            return;
        }
        dialogUtils.sendMessage(ProgressDialogUtils.DISMISS);
        if (baseResult instanceof OneKeyMatchKey) {
            key = ((OneKeyMatchKey) baseResult).getNext_cmp_key();
            showDlg();
        } else if (baseResult instanceof RemoteControl) {
            remoteControl = (RemoteControl) baseResult;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(OneKeyMatchActivity.this).setMessage("匹配成功，进入测试").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
                }
            });
        } else if (baseResult instanceof MatchRemoteControlResult) {
            final MatchRemoteControlResult result = (MatchRemoteControlResult) baseResult;
            Log.e("tttt", result.toString());
            if (result != null && result.getRs() != null && result.getRs().size() > 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.clear();
                        list.addAll(result.getRs());
                        controlAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    public void onFail(final YKError ykError) {
        dialogUtils.sendMessage(ProgressDialogUtils.DISMISS);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(OneKeyMatchActivity.this).setMessage(ykError.getError()).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });
    }

    private class ControlAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            OneKeyMatchActivity.ControlAdapter.ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.lv_control, parent, false);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.tv_rc_name);
                holder.btnOn = convertView.findViewById(R.id.btn_on);
                holder.btnOff = convertView.findViewById(R.id.btn_off);
                holder.btnSOff = convertView.findViewById(R.id.btn_s_off);
                holder.btnSOn = convertView.findViewById(R.id.btn_s_on);
                holder.tvDl = convertView.findViewById(R.id.dl);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(list.get(position).getName() + "-" + list.get(position).getRmodel());

            holder.btnOff.setVisibility(View.VISIBLE);
            holder.btnOn.setVisibility(View.VISIBLE);
            holder.btnSOff.setVisibility(View.VISIBLE);
            holder.btnSOn.setVisibility(View.VISIBLE);
            if (list.get(position).getRcCommand().size() <= 2) {
                holder.btnSOff.setVisibility(View.GONE);
                holder.btnSOn.setVisibility(View.GONE);
            }
            holder.btnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendCMD("on", position);
                }
            });
            holder.btnOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendCMD("off", position);
                }
            });
            holder.btnSOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendCMD("u0", position);
                }
            });
            holder.btnSOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendCMD("u1", position);
                }
            });
            holder.tvDl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogUtils.sendMessage(ProgressDialogUtils.SHOW);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ykanInterface
                                    .getRemoteDetails("", list.get(position).getRid(), new YKanHttpListener() {
                                        @Override
                                        public void onSuccess(BaseResult baseResult) {
                                            dialogUtils.sendMessage(ProgressDialogUtils.DISMISS);
                                            if (baseResult != null) {
                                                remoteControl = (RemoteControl) baseResult;
                                            }
                                        }

                                        @Override
                                        public void onFail(final YKError ykError) {
                                            dialogUtils.sendMessage(ProgressDialogUtils.DISMISS);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialog.Builder(OneKeyMatchActivity.this).setMessage(ykError.getError()).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    }).create().show();
                                                }
                                            });
                                            Log.e(TAG, "ykError:" + ykError.toString());
                                        }
                                    });
                        }
                    }).start();
                }
            });
            return convertView;
        }

        private class ViewHolder {
            TextView name = null;
            Button btnOn = null;
            Button btnOff = null;
            Button btnSOff = null;
            Button btnSOn = null;
            TextView tvDl = null;
        }
    }

    private void sendCMD(String mode, int position) {
        HashMap<String, KeyCode> map = list.get(position).getRcCommand();
        Set<String> set = map.keySet();
        String key = null;
        for (String s : set) {
            if (s.contains(mode)) {
                key = s;
            }
        }
//        driverControl.sendCMD(map.get(key).getSrcCode());
    }
}
