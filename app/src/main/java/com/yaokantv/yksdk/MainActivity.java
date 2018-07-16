package com.yaokantv.yksdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.yaokantv.api.JsonParser;
import com.yaokantv.api.Utility;
import com.yaokantv.api.YKanHttpListener;
import com.yaokantv.api.YkanIRInterface;
import com.yaokantv.api.YkanIRInterfaceImpl;
import com.yaokantv.api.model.BaseResult;
import com.yaokantv.api.model.Brand;
import com.yaokantv.api.model.BrandResult;
import com.yaokantv.api.model.DeviceType;
import com.yaokantv.api.model.DeviceTypeResult;
import com.yaokantv.api.model.KeyCode;
import com.yaokantv.api.model.MatchRemoteControl;
import com.yaokantv.api.model.MatchRemoteControlResult;
import com.yaokantv.api.model.RemoteControl;
import com.yaokantv.api.model.YKError;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {

    private ProgressDialogUtils dialogUtils;

    private YkanIRInterface ykanInterface;

    private String TAG = MainActivity.class.getSimpleName();

    private List<DeviceType> deviceType = new ArrayList<>();// 设备类型

    private List<Brand> brands = new ArrayList<>(); // 品牌

    private List<MatchRemoteControl> remoteControls = new ArrayList<>();// 遥控器列表

    private List<String> nameType = new ArrayList<>();
    private List<String> nameBrands = new ArrayList<>();
    private List<String> nameRemote = new ArrayList<>();

    private MatchRemoteControlResult controlResult = null;// 匹配列表

    private RemoteControl remoteControl = null; // 遥控器对象

    private MatchRemoteControl currRemoteControl = null; // 当前匹配的遥控器对象

    private DeviceType currDeviceType = null; // 当前设备类型

    private Brand currBrand = null; // 当前品牌

    private Spinner spType, spBrands, spRemotes;

    private ArrayAdapter<String> typeAdapter, brandAdapter, remoteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 遥控云数据接口分装对象对象
        ykanInterface = new YkanIRInterfaceImpl(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean b = ykanInterface.init("");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (b) {
                            Toast.makeText(MainActivity.this, "初始化成功！！！", Toast.LENGTH_SHORT).show();
                            initView();
                        } else {
                            Toast.makeText(MainActivity.this, "初始化失败！！！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void initView() {
        dialogUtils = new ProgressDialogUtils(this);

        spType = findViewById(R.id.spType);
        spBrands = findViewById(R.id.spBrand);
        spRemotes = findViewById(R.id.spData);

        typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nameType);
        brandAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nameBrands);
        remoteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nameRemote);
        typeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        brandAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        remoteAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);
        spBrands.setAdapter(brandAdapter);
        spRemotes.setAdapter(remoteAdapter);
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                currDeviceType = deviceType.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                currBrand = brands.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spRemotes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                currRemoteControl = remoteControls.get(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.match:
                if (currRemoteControl != null) {
                    if (currRemoteControl.getRcCommand() != null && currRemoteControl.getRcCommand().size() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        CharSequence c[] = new CharSequence[currRemoteControl.getRcCommand().size()];
                        final String sCode[] = new String[currRemoteControl.getRcCommand().size()];
                        Iterator<Map.Entry<String, KeyCode>> iterator = currRemoteControl.getRcCommand().entrySet().iterator();
                        int i = 0;
                        while (iterator.hasNext()) {
                            Map.Entry<String, KeyCode> entry = iterator.next();
                            c[i] = entry.getKey();
                            sCode[i] = entry.getValue().getSrcCode();
                            i++;
                        }
                        builder.setTitle("测试匹配").setItems(c, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String code = sCode[which];
                                dialog.dismiss();
                            }
                        }).create().show();
                    }
                }
                break;
            default:
                new DownloadThread(v.getId()).start();
                break;
        }
    }

    class DownloadThread extends Thread {
        private int viewId;
        String result = "";

        public DownloadThread(int viewId) {
            this.viewId = viewId;
        }

        @Override
        public void run() {
            dialogUtils.sendMessage(1);

            final Message message = mHandler.obtainMessage();
            switch (viewId) {
                case R.id.getDeviceType:
                    ykanInterface.getDeviceType("mac", new YKanHttpListener() {
                        @Override
                        public void onSuccess(BaseResult baseResult) {
                            DeviceTypeResult deviceResult = (DeviceTypeResult) baseResult;
                            deviceType = deviceResult.getRs();
                            result = deviceResult.toString();
                            message.what = 0;
                            Log.d(TAG, " getDeviceType result:" + result);
                        }

                        @Override
                        public void onFail(YKError ykError) {
                            Log.e(TAG, "ykError:" + ykError.toString());
                        }
                    });
                    break;
                case R.id.getBrandByType:
                    // int type = 7 ;//1:机顶盒，2：电视机
                    if (currDeviceType != null) {
                        ykanInterface
                                .getBrandsByType("mac", currDeviceType.getTid(), new YKanHttpListener() {
                                    @Override
                                    public void onSuccess(BaseResult baseResult) {
                                        BrandResult brandResult = (BrandResult) baseResult;
                                        brands = brandResult.getRs();
                                        result = brandResult.toString();
                                        message.what = 1;
                                        Log.d(TAG, " getBrandByType result:" + brandResult);
                                    }

                                    @Override
                                    public void onFail(YKError ykError) {
                                        Log.e(TAG, "ykError:" + ykError.toString());
                                    }
                                });
                    } else {
                        result = "请调用获取设备接口";
                    }
                    break;
                case R.id.getMatchedDataByBrand:
                    if (currBrand != null) {
                        ykanInterface.getRemoteMatched("mac",
                                currBrand.getBid(), currDeviceType.getTid(), new YKanHttpListener() {
                                    @Override
                                    public void onSuccess(BaseResult baseResult) {
                                        controlResult = (MatchRemoteControlResult) baseResult;
                                        remoteControls = controlResult.getRs();
                                        result = controlResult.toString();
                                        message.what = 2;
                                        Log.d(TAG, " getMatchedDataByBrand result:" + result);
                                    }

                                    @Override
                                    public void onFail(YKError ykError) {
                                        Log.e(TAG, "ykError:" + ykError.toString());
                                    }
                                });
                    } else {
                        result = "请调用获取设备接口";
                    }
                    break;
                case R.id.getDetailByRCID:
                    if (!Utility.isEmpty(currRemoteControl)) {
                        ykanInterface
                                .getRemoteDetails("mac", currRemoteControl.getRid(), new YKanHttpListener() {
                                    @Override
                                    public void onSuccess(BaseResult baseResult) {
                                        if (baseResult != null) {
                                            remoteControl = (RemoteControl) baseResult;
                                            result = remoteControl.toString();
                                            message.what = 3;
                                        }
                                    }

                                    @Override
                                    public void onFail(YKError ykError) {
                                        Log.e(TAG, "ykError:" + ykError.toString());
                                    }
                                });
                    } else {
                        result = "请调用匹配数据接口";
                        Log.e(TAG, " getDetailByRCID 没有遥控器设备对象列表");
                    }
                    Log.d(TAG, " getDetailByRCID result:" + result);
                    break;
                default:
                    break;
            }
            message.obj = result;
            mHandler.sendMessage(message);
            dialogUtils.sendMessage(0);
        }
    }

    JsonParser jsonParser = new JsonParser();
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (deviceType != null) {
                        nameType.clear();
                        for (int i = 0; i < deviceType.size(); i++) {
                            nameType.add(deviceType.get(i).getName());
                        }
                    }
                    typeAdapter.notifyDataSetInvalidated();
                    spType.setAdapter(typeAdapter);
                    break;
                case 1:
                    if (brands != null) {
                        nameBrands.clear();
                        for (int i = 0; i < brands.size(); i++) {
                            nameBrands.add(brands.get(i).getName());
                        }
                    }
                    brandAdapter.notifyDataSetInvalidated();
                    spBrands.setAdapter(brandAdapter);
                    break;
                case 2:
                    if (remoteControls != null) {
                        nameRemote.clear();
                        for (int i = 0; i < remoteControls.size(); i++) {
                            nameRemote.add(remoteControls.get(i).getName() + "-"
                                    + remoteControls.get(i).getRmodel());
                        }
                    }
                    remoteAdapter.notifyDataSetInvalidated();
                    spRemotes.setAdapter(remoteAdapter);
                    break;
                case 3:
                    Intent intent;
                    if (remoteControl != null && remoteControl.getRcCommand() != null && remoteControl.gettId() != 7) {//普通设备
                        intent = new Intent(MainActivity.this, NormalDeviceActivity.class);
                        try {
                            intent.putExtra("remoteControl", jsonParser.toJson(remoteControl));
                            intent.putExtra("rcCommand", jsonParser.toJson(remoteControl.getRcCommand()));
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException:" + e.getMessage());
                        }
                        startActivity(intent);
                    } else if (remoteControl != null && remoteControl.getRcCommand() != null && remoteControl.gettId() == 7) {//空调设备
                        intent = new Intent(MainActivity.this, AirDeviceActivity.class);
                        try {
                            intent.putExtra("remoteControl", jsonParser.toJson(remoteControl));
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException:" + e.getMessage());
                        }
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    public void onBackPressed() {
        finish();
    }


}