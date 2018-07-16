package com.yaokantv.yksdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.yaokantv.api.JsonParser;
import com.yaokantv.api.model.KeyCode;
import com.yaokantv.api.model.RemoteControl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NormalDeviceActivity extends AppCompatActivity {

    private GridView gridView;
    private HashMap<String, KeyCode> codeDatas = new HashMap<String, KeyCode>();
    private List<String> codeKeys = new ArrayList<String>();
    JsonParser jsonParser = new JsonParser();
    private String rcCommand = "";
    RemoteControl control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_device);
        Intent intent = getIntent();
        rcCommand = intent.getStringExtra("rcCommand");
        control = new JsonParser().parseObjecta(intent.getStringExtra("remoteControl"), RemoteControl.class);

        initView();

    }

    private void initView() {
        gridView = findViewById(R.id.codeGridView);
        codeDatas = new HashMap<>();
        Type type = new TypeToken<HashMap<String, KeyCode>>() {
        }.getType();
        //解析数据
        codeDatas = jsonParser.parseObjecta(rcCommand, type);
        codeKeys = new ArrayList<>(codeDatas.keySet());
        ExpandAdapter expandAdapter = new ExpandAdapter(getApplicationContext(), codeKeys);
        gridView.setAdapter(expandAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String key = codeKeys.get(position);
                KeyCode keyCode = codeDatas.get(key);
                String code;
                code = keyCode.getSrcCode();//从Api中取到的code
                if (!TextUtils.isEmpty(code)) {

                }
            }
        });
    }

    public class ExpandAdapter extends BaseAdapter {

        private Context mContext;

        private LayoutInflater inflater;

        public List<String> keys;

        public ExpandAdapter(Context c, List<String> keys) {
            super();
            this.mContext = c;
            this.keys = keys;
            inflater = LayoutInflater.from(mContext);

        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.yk_ctrl_adapter_expand, null);
            }
            TextView keyBtn = convertView.findViewById(R.id.key_btn);
            keyBtn.setText(keys.get(position));
            return convertView;
        }
    }
}
