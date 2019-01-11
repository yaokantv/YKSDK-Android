package com.yaokantv.yksdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yaokantv.api.Utility;
import com.yaokantv.model.AirConCatogery;
import com.yaokantv.model.AirStatus;
import com.yaokantv.model.AirV3Command;
import com.yaokantv.model.KeyCode;
import com.yaokantv.model.RemoteControl;
import com.yaokantv.model.kyenum.AirV3KeyMode;
import com.yaokantv.model.kyenum.Mode;
import com.yaokantv.model.kyenum.Speed;
import com.yaokantv.model.kyenum.Temp;
import com.yaokantv.model.kyenum.WindH;
import com.yaokantv.model.kyenum.WindV;

import java.util.HashMap;

public class AirDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_show;

    private HashMap<String, KeyCode> codeDatas = new HashMap<>();

    private Button mode_btn, wspeed_btn, tbspeed_btn, lrwspped_btn, power_btn, temp_add_btn, temp_rdc_btn;

    private static final int V3 = 3;

    private int airVerSion = V3;

    private AirV3Command airEvent = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_device);
        initDevice();
        initView();
        setOnClickListener();
    }

    private RemoteControl remoteControl;

    private void initDevice() {
        remoteControl = DataHolder.getInstance().getExtra();
        if (!Utility.isEmpty(remoteControl)) {
            airVerSion = remoteControl.getVersion();
            codeDatas = remoteControl.getRcCommand();
            airEvent = getAirEvent(codeDatas);
        }
        Log.e("aaaa", remoteControl.getJson());
    }

    private void initView() {
        tv_show = findViewById(R.id.tv_show);
        mode_btn = findViewById(R.id.mode_btn);
        wspeed_btn = findViewById(R.id.wspeed_btn);
        tbspeed_btn = findViewById(R.id.tbspeed_btn);
        lrwspped_btn = findViewById(R.id.lrwspped_btn);
        power_btn = findViewById(R.id.power_btn);
        temp_add_btn = findViewById(R.id.temp_add_btn);
        temp_rdc_btn = findViewById(R.id.temp_rdc_btn);
        //v1没有左右扫风，上下扫风，风量
        if (airVerSion != V3) {
            wspeed_btn.setVisibility(View.GONE);
            tbspeed_btn.setVisibility(View.GONE);
            lrwspped_btn.setVisibility(View.GONE);
        }

        findViewById(R.id.get_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注意！！！！注意！！！！注意！！！！注意！！！！
                //此方法是从SDK抽离出来的，不参与onRefreshUI（）的UI逻辑，需要用户自己实现。
                KeyCode keyCode = airEvent.getAirCode(Mode.WIND, Speed.S0, WindV.OFF, WindH.OFF, Temp.T24);
                KeyCode keyCode2 = airEvent.getAirCode(Mode.WIND, Speed.S1, WindV.OFF, WindH.OFF, Temp.T24);
                KeyCode keyCode3 = airEvent.getAirCode(Mode.WIND, Speed.S2, WindV.OFF, WindH.OFF, Temp.T24);
                if (keyCode != null) {
                }
            }
        });
    }

    private void setOnClickListener() {
        mode_btn.setOnClickListener(this);
        wspeed_btn.setOnClickListener(this);
        tbspeed_btn.setOnClickListener(this);
        lrwspped_btn.setOnClickListener(this);
        power_btn.setOnClickListener(this);
        temp_add_btn.setOnClickListener(this);
        temp_rdc_btn.setOnClickListener(this);
    }

    private AirV3Command getAirEvent(HashMap<String, KeyCode> codeDatas) {
        AirV3Command airEvent = null;
        if (!Utility.isEmpty(codeDatas)) {
            airEvent = new AirV3Command(codeDatas);
        }
        return airEvent;
    }

    @Override
    public void onClick(View v) {
        if (Utility.isEmpty(airEvent)) {
            //toast("airEvent is null");
            return;
        }
        //空调必须先打开，才能操作其他按键
        if (airEvent.isOff() && !(v.getId() == R.id.power_btn)) {
            //toast("请先打开空调");
            return;
        }
        KeyCode mKeyCode = null;
        switch (v.getId()) {
            case R.id.power_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.Power);
                break;
            case R.id.mode_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.Mode);
                break;
            case R.id.wspeed_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.Speed);
                break;
            case R.id.tbspeed_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.WindUp);
                break;
            case R.id.lrwspped_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.WindLeft);
                break;
            case R.id.temp_add_btn:
                mKeyCode = airEvent.getNextValueByCatogery(AirConCatogery.Temp);
                break;
            case R.id.temp_rdc_btn:
                mKeyCode = airEvent.getForwardValueByCatogery(AirConCatogery.Temp);
                break;
            default:
                break;
        }
        if (!Utility.isEmpty(mKeyCode)) {
            Log.i("key: ", mKeyCode.getSrcCode());
            onRefreshUI(airEvent.getCurrStatus());
        }
    }

    private void onRefreshUI(AirStatus airStatus) {
        if (Utility.isEmpty(airStatus)) {
            return;
        }
        String content = "";
        if (airEvent.isOff()) {
            content = "空调已关闭";
        } else {
            content = getContent(airStatus);
        }
        tv_show.setText(content);
    }

    //
    private String getContent(AirStatus airStatus) {
        String mode = airStatus.getMode().getName();
        String temp = "";
        if (!TextUtils.isEmpty(mode) && airEvent != null) {
            AirV3KeyMode keyMode = null;
            switch (mode) {
                case "r":
                    keyMode = airEvent.getrMode();
                    break;
                case "h":
                    keyMode = airEvent.gethMode();
                    break;
                case "d":
                    keyMode = airEvent.getdMode();
                    break;
                case "w":
                    keyMode = airEvent.getwMode();
                    break;
                case "a":
                    keyMode = airEvent.getaMode();
                    break;
            }
            if (keyMode != null) {
                if (keyMode.isSpeed()) {
                    setBtnStatus(wspeed_btn, true);
                } else {
                    setBtnStatus(wspeed_btn, false);
                }
                if (keyMode.isU()) {
                    setBtnStatus(tbspeed_btn, true);
                } else {
                    setBtnStatus(tbspeed_btn, false);
                }
                if (keyMode.isL()) {
                    setBtnStatus(lrwspped_btn, true);
                } else {
                    setBtnStatus(lrwspped_btn, false);
                }
                if (keyMode.isTemp()) {
                    setBtnStatus(temp_add_btn, true);
                    setBtnStatus(temp_rdc_btn, true);
                } else {
                    temp = "--";
                    setBtnStatus(temp_add_btn, false);
                    setBtnStatus(temp_rdc_btn, false);
                }
            }
        }
        String content;
        content = "模式：" + airStatus.getMode().getChName()
                + "\n风量：" + airStatus.getSpeed().getChName()
                + "\n左右扫风：" + airStatus.getWindLeft().getChName()
                + "\n上下扫风：" + airStatus.getWindUp().getChName()
                + "\n温度：" + (TextUtils.isEmpty(temp) ? airStatus.getTemp().getChName() : temp);
        return content;
    }

    void setBtnStatus(TextView textView, boolean status) {
        textView.setEnabled(status);
        textView.setTextColor(status ? getResources().getColor(android.R.color.white) : getResources().getColor(android.R.color.black));
    }
}
