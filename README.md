# 一、集成SDK

1.将yksdk.aar拷贝到libs目录下后，在工程里的app→src→build.gradle 根目录添加以下代码

    repositories{  
        flatDir {        
        dirs 'libs'       
       }  
    }
2. 工程里的app→src→build.gradle 的根目录的 dependencies 标签里面添加

        implementation(name:’yksdk',   ext:'aar')
        implementation 'com.google.code.gson:gson:2.8.2'

如图：
![image](https://github.com/yaokantv/YKSDK-Android/blob/master/img/cap1.jpg)

3.在自己的application里面，实现如下方法：
    
    public class App extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
    
        }
        @Override
        protected void attachBaseContext(Context base) {
            super.attachBaseContext(base);
            com.jiagu.sdk.yksdkProtected.install(this);
        }
    }

4.添加网络请求权限

     <uses-permission android:name="android.permission.INTERNET" />
 
# 二、Api调用 
    
## 1.1 初始化YKSDK
    // 初始化SDK
    YkanIRInterfaceImpl ykanInterface = new YkanIRInterfaceImpl(getApplicationContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean b = ykanInterface.init("此处填入App ID");

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

**注意：只有在初始化完成之后，App才能完成后续的操作。**

## 1.2 获取遥控云所有的设备类型   

    【示例代码】
    /**
    * @param mac：设备Mac地址
    * @param listener:http请求回调方法
    */
    ykanInterface.getDeviceType(currGizWifiDevice.getMacAddress(), new YKanHttpListener() {
        @Override
        public void onSuccess(BaseResult baseResult) {
            DeviceTypeResult deviceResult = (DeviceTypeResult) baseResult;
            ...
        }
        @Override
        public void onFail(YKError ykError) {
            Log.e(TAG, "ykError:" + ykError.toString());
        }
    });
    
## 1.3 获取设备类型下面的所有品牌   

    【示例代码】
    /**
     * @param mac:设备Mac地址
     * @param type：设备型号
     * @param listener:http请求回调方法
     */
    ykanInterface.getBrandsByType(currGizWifiDevice.getMacAddress(), currDeviceType.getTid(), new 
        YKanHttpListener() {
        @Override
        public void onSuccess(BaseResult baseResult) {
            BrandResult brandResult = (BrandResult) baseResult;
        }
        @Override
        public void onFail(YKError ykError) {
            Log.e(TAG, "ykError:" + ykError.toString());
        }
    });

## 1.4 根据品牌id和设备类型获取匹配的遥控器对象集合   

    【示例代码】
    /**
     * @param mac:设备Mac地址
     * @param bid:品牌ID
     * @param type:设备类型ID
     * @param listener:http请求回调方法
     */
     ykanInterface.getRemoteMatched(currGizWifiDevice.getMacAddress(),currBrand.getBid(), currDeviceType.getTid(), new YKanHttpListener() {
        @Override
        public void onSuccess(BaseResult baseResult) {
            controlResult = (MatchRemoteControlResult) baseResult;
        }
        @Override
        public void onFail(YKError ykError) {
            Log.e(TAG, "ykError:" + ykError.toString());
        }
    });
    
## 1.5 根据遥控ID获取遥控器相关详细信息 

    【示例代码】
    /**
     * @param mac:设备Mac地址
     * @param rcID:遥控器ID
     * @param listener:http请求回调方法
     */
     ykanInterface.getRemoteDetails(currGizWifiDevice.getMacAddress(), currRemoteControl.getRid(), new YKanHttpListener() {
        @Override
        public void onSuccess(BaseResult baseResult) {
            if (baseResult != null) {
                remoteControl = (RemoteControl) baseResult;
            }
        }
        @Override
        public void onFail(YKError ykError) {
            Log.e(TAG, "ykError:" + ykError.toString());
        }
    });

## 1.6 获取遥控器码库

根据遥控器tid判断是否为空调设备 
    
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
        
每个遥控器对象里面都有RcCommand这个属性，取得之后用JsonParser解析就能得到遥控器里所有的码值。

    Type type = new TypeToken<HashMap<String, KeyCode>>(){}.getType();
    //解析数据
    HashMap<String, KeyCode> codeDatas = new JsonParser().parseObjecta(rcCommand, type);
    
## 1.7 一键匹配

遥控器一键匹配的本质是一个将学习到的红外码上传到云端服务器匹配的过程，该过程分为一下几个步骤：

 step1：第一次调用一键匹配接口时，先确认匹配遥控器的类型
 
     if(遥控器是空调或者汽车多媒体(SDK暂未提供)){
        key="ok";
    }else{
        key="power";
    }
  
 step2:学习成功后，调用一键匹配接口将红外码值上传到云端
 
    
    ykanInterface.oneKeyMatched(gizWifiDevice.getMacAddress(), studyValue, tid + "", bid + "", key, OneKeyMatchActivity.this);
 
 step3:解析回调
 
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
                            toYKWifiDeviceControlActivity();
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
    
回调有四种情况，我们逐一分析。
    
    当回调成功时
    1.如果结果属于OneKeyMatchKey时，则说明要继续匹配下一个键，键值包含在对象里，将全局对象key替换后，重复step2操作。(这里返回的key为英文，[需要翻译成中文的请戳这里](https://github.com/yaokantv/YKCenterSDKExample_for_AS/wiki/%E9%81%A5%E6%8E%A7%E5%99%A8%E5%AF%B9%E5%BA%94%E9%94%AE%E5%90%8D))
    2.如果结果属于RemoteControl时，则说明匹配成功，这时你只需要把匹配成功的遥控器保存下来就可以了
    3.如果结果属于MatchRemoteControlResult时，则说明匹配回来的是空调遥控器数组(一般匹配空调都会以数组的形式返回)，数组里面包含遥控器的几个按键，这时你可以用来测试实体家电，测试成功之后再下载完整的遥控器数据。如下图所示![image](https://github.com/yaokantv/YKCenterSDKExample_for_AS/blob/master/app/img/one_key.png)
    4.除以上3中情况，其他情况均为匹配失败。

