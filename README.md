#一、集成SDK

1.将SDK拷贝到libs目录下后，在工程里的app→src→build.gradle 根目录添加以下代码
    repositories{  
        flatDir {        
        dirs 'libs'       
       }  
    }
2. 工程里的app→src→build.gradle 的根目录的 dependencies 标签里面添加
    implementation(name:’yksdk',   ext:'aar')
    implementation 'com.google.code.gson:gson:2.8.2'

## 1.1 初始化YKSDK
    // 初始化SDK
    YkanIRInterfaceImpl ykanInterface = new YkanIRInterfaceImpl(getApplicationContext());
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
    