package com.yaokantv.yksdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yaokantv.api.JsonParser;
import com.yaokantv.api.Utility;
import com.yaokantv.api.model.AirConCatogery;
import com.yaokantv.api.model.AirEvent;
import com.yaokantv.api.model.AirStatus;
import com.yaokantv.api.model.AirV1Command;
import com.yaokantv.api.model.AirV3Command;
import com.yaokantv.api.model.KeyCode;
import com.yaokantv.api.model.RemoteControl;
import com.yaokantv.api.model.kyenum.Mode;
import com.yaokantv.api.model.kyenum.Speed;
import com.yaokantv.api.model.kyenum.Temp;
import com.yaokantv.api.model.kyenum.WindH;
import com.yaokantv.api.model.kyenum.WindV;

import java.util.HashMap;

public class AirDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_show;

    private HashMap<String, KeyCode> codeDatas = new HashMap<>();

    private JsonParser jsonParser = new JsonParser();

    private Button mode_btn, wspeed_btn, tbspeed_btn, lrwspped_btn, power_btn, temp_add_btn, temp_rdc_btn;

    private static final int V3 = 3;

    private int airVerSion = V3;

    private AirEvent airEvent = null;


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
        Intent intent = getIntent();
        remoteControl = jsonParser.parseObjecta(intent.getStringExtra("remoteControl"), RemoteControl.class);
        if (!Utility.isEmpty(remoteControl)) {
            airVerSion = remoteControl.getVersion();
            codeDatas = remoteControl.getRcCommand();
            airEvent = getAirEvent(codeDatas);
        }

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

    private AirEvent getAirEvent(HashMap<String, KeyCode> codeDatas) {
        AirEvent airEvent = null;
        if (!Utility.isEmpty(codeDatas)) {
            if (airVerSion == V3) {
                airEvent = new AirV3Command(codeDatas);
            } else {
                airEvent = new AirV1Command(codeDatas);
            }
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
        String content = "";
        if (airVerSion == 1) {
            content = "模式：" + airStatus.getMode().getChName()
                    + "\n温度：" + airStatus.getTemp().getChName();
        } else {
            content = "模式：" + airStatus.getMode().getChName()
                    + "\n风量：" + airStatus.getSpeed().getChName()
                    + "\n左右扫风：" + airStatus.getWindLeft().getChName()
                    + "\n上下扫风：" + airStatus.getWindUp().getChName()
                    + "\n温度：" + airStatus.getTemp().getChName();
        }
        return content;

    }
}
