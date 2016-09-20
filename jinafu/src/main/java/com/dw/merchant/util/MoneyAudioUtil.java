package com.dw.merchant.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;

import com.dw.merchant.Constants;
import com.dw.merchant.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by lixue on 2016/1/8.
 */

public class MoneyAudioUtil implements SoundPool.OnLoadCompleteListener {

    //音频加载成功标识
    private static final int SOUND_LOAD_OK = 1;
    //将金额分为：整数，小数部分，并分别保存
    private static ArrayList<Integer> integer_money = new ArrayList<Integer>();
    private static ArrayList<Integer> decimal_money = new ArrayList<Integer>();
    //创建一个可以容纳20个音频流SoundPool对象
    public SoundPool mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
    Vector<Integer> mKillSoundQueue = new Vector<Integer>();
    private String TAG = MoneyAudioUtil.class.getSimpleName() + "_lx";
    //标识加载的音频
    private ArrayList<String> typeList = new ArrayList<String>();
    private MyHandler mHandler = new MyHandler();
    private AudioManager mAudioManager;
    private Map<String, Integer> soundMap = new HashMap<String, Integer>();
    private long delay = 1000;
    //播放速率
    private float rate = 1.0f;
    private long seperateTime = 700;

    /**
     * @param context
     * @param resultCode 收款成功与否的返回值，11 收款成功，10 收款失败 21 核销成功 20核销失败
     * @param value      待收款数额
     */
    public void Audio(Context context, Integer resultCode, String value) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        switch (resultCode) {
            case 11:
                //收款成功
                String[] money = value.split("\\.");
                if (money.length > 0) {
                    //取出money中的整数部分
                    String integer_str = money[0] + "";
                    for (int i = 0; i < integer_str.length(); i++) {
//                        Log.v(TAG, "money中整数中的数字分别为：" + integer_str.substring(i, i + 1));
                        integer_money.add(Integer.valueOf(integer_str.substring(i, i + 1)));
                    }

                    if (money.length > 1) {
                        String decimal_str = money[1] + "";
                        for (int i = 0; i < decimal_str.length(); i++) {
//                            Log.v(TAG, "money中小数中的数字分别为：" + decimal_str.substring(i, i + 1));
                            decimal_money.add(Integer.valueOf(decimal_str.substring(i, i + 1)));
                        }
                        // add pay success video
                        addSound(context, Constants.TYPE_RECEIVE_RESULT_SUCCESS, R.raw.receive_success);
                        // handle integer money
                        handleIntegerMoney(context, integer_money);
                        //handle decimal money
                        handleDecimalMoney(context, decimal_money);
                    } else {
                        // add pay success video
                        addSound(context, Constants.TYPE_RECEIVE_RESULT_SUCCESS, R.raw.receive_success);
                        // handle integer money
                        handleIntegerMoney(context, integer_money);
                        addMoneyAudio(context, 1);//圆
                    }
                }
                break;
            case 10:
                //收款失败
                addSound(context, Constants.TYPE_RECEIVE_RESULT_FAIL, R.raw.receive_fail);
                break;
            case 21:
                //核销成功
                addSound(context, Constants.TYPE_VERIFY_RESULT_SUCCESS, R.raw.verify_success);
                break;
            case 20:
                //核销失败
                addSound(context, Constants.TYPE_VERIFY_RESULT_FAIL, R.raw.verify_fail);
                break;

        }

        //音频加载成功之后开始播放
        mSoundPool.setOnLoadCompleteListener(this);
    }

    /**
     * 分别添加价格中整数部分对应的音频
     *
     * @param money the integer value of total money
     */
    public void handleIntegerMoney(Context context, ArrayList<Integer> money) {
        int lenght = money.size();
//        Log.v(TAG, "lenght==" + lenght);
        int temp = lenght;
        if (lenght == 1) {
//            Log.v(TAG, "一位数！" + money.get(0));

            addNumberAudio(context, money.get(0));
//            addMoneyAudio(context,1);//圆
        } else if (lenght == 2) {
            for (int j = 0; j < lenght; j++) {
                if (temp != 1) {
                    if (money.get(j) != 1) {
                        addNumberAudio(context, money.get(j));
                    }
                    addMoneyAudio(context, temp);
                } else {
                    if (money.get(j) != 0) {
                        addNumberAudio(context, money.get(j));
                    }
                }
                temp--;
            }
//            addMoneyAudio(context,1);//圆
        } else if (lenght > 2) {
            //判断除最高位，其他位是否都为零
            int sum1 = 0;
            int sum2 = 0;
            int sum3 = 0;
            for (int i = 0; i < lenght; i++) {
                if (i + 1 < lenght) {
                    sum1 += money.get(i + 1);
                }
                if (i + 2 < lenght) {
                    sum2 += money.get(i + 2);
                }
                if (i + 3 < lenght) {
                    sum3 += money.get(i + 3);
                }
            }
            if (sum1 == 0) {
                addNumberAudio(context, money.get(0));
                addMoneyAudio(context, temp);
            } else if ((lenght == 4 || lenght == 5) && sum1 != 0 && sum2 == 0) {
                addNumberAudio(context, money.get(0));
                addMoneyAudio(context, temp);
                addNumberAudio(context, money.get(1));
                addMoneyAudio(context, (temp - 1));
            } else if (lenght == 5 && sum2 != 0 && sum3 == 0) {
                addNumberAudio(context, money.get(0));
                addMoneyAudio(context, temp);
                addNumberAudio(context, money.get(1));
                if (money.get(1) != 0) {
                    addMoneyAudio(context, (temp - 1));
                }
                addNumberAudio(context, money.get(2));
                addMoneyAudio(context, (temp - 2));
            } else {
                for (int m = 0; m < lenght; m++) {
                    if (m <= 1) {
                        addNumberAudio(context, money.get(m));
                        if (money.get(m) != 0) {
                            addMoneyAudio(context, temp);
                        }
                    } else {
                        int temp1 = money.get(m);
                        int temp2 = money.get(m - 1);
                        if (temp1 != temp2 || (temp1 == temp2 && temp2 != 0)) {
                            if (temp != 1) {
                                addNumberAudio(context, money.get(m));
                                if (money.get(m) != 0) {
                                    addMoneyAudio(context, temp);
                                }
                            } else {
                                if (money.get(m) != 0) {
                                    addNumberAudio(context, money.get(m));
                                }
                            }
                        }
                    }
                    temp--;
                }
            }
//            addMoneyAudio(context,1);//圆
        }
    }


    /**
     * 分别添加价格中小数部分对应的音频
     *
     * @param money the decimal value of total money
     */
    public void handleDecimalMoney(Context context, ArrayList<Integer> money) {
        int lenght = money.size();
        if (lenght == 1) {
            money.add(0);
        }
        int sum = 0;
        for (int i = 0; i < lenght; i++) {
            sum += money.get(i);
        }
        if (sum != 0) {
            addMoneyAudio(context, 13);//点
//            if (money.get(0) != 0) {
            addNumberAudio(context, money.get(0));
//                addMoneyAudio(context,11);
//            }
            if (money.get(1) != 0) {
                addNumberAudio(context, money.get(1));
//                addMoneyAudio(context,12);
            }
        }
        addMoneyAudio(context, 1);//圆
    }

    /**
     * 添加待播放音频
     *
     * @param key
     * @param SoundID
     */
    public void addSound(Context context, String key, int SoundID) {
        soundMap.put(key, mSoundPool.load(context, SoundID, 1));
        typeList.add(key);
    }

    /**
     * 添加各位数字相应的音频
     *
     * @param value
     */
    public void addNumberAudio(Context context, Integer value) {
        switch (value) {
            case 0:
                addSound(context, Constants.TYPE_NUMBER_ZERO, R.raw.zero);
                break;
            case 1:
                addSound(context, Constants.TYPE_NUMBER_ONE, R.raw.one);
                break;
            case 2:
                addSound(context, Constants.TYPE_NUMBER_TWO, R.raw.two);
                break;
            case 3:
                addSound(context, Constants.TYPE_NUMBER_THREE, R.raw.three);
                break;
            case 4:
                addSound(context, Constants.TYPE_NUMBER_FOUR, R.raw.four);
                break;
            case 5:
                addSound(context, Constants.TYPE_NUMBER_FIVE, R.raw.five);
                break;
            case 6:
                addSound(context, Constants.TYPE_NUMBER_SIX, R.raw.six);
                break;
            case 7:
                addSound(context, Constants.TYPE_NUMBER_SEVEN, R.raw.seven);
                break;
            case 8:
                addSound(context, Constants.TYPE_NUMBER_EIGHT, R.raw.eight);
                break;
            case 9:
                addSound(context, Constants.TYPE_NUMBER_NINE, R.raw.nine);
                break;
        }
    }

    /**
     * 货币单位对应的音频
     *
     * @param value
     */
    public void addMoneyAudio(Context context, Integer value) {
        switch (value) {
            case 5:
                addSound(context, Constants.TYPE_MONEY_WAN, R.raw.wan);
                break;
            case 4:
                addSound(context, Constants.TYPE_MONEY_THOUSAND, R.raw.thousand);
                break;
            case 3:
                addSound(context, Constants.TYPE_MONEY_HUNDRED, R.raw.hundred);
                break;
            case 2:
                addSound(context, Constants.TYPE_MONEY_TEN, R.raw.ten);
                break;
            case 1:
                addSound(context, Constants.TYPE_MONEY_YUAN, R.raw.yuan);
                break;
            case 11:
                addSound(context, Constants.TYPE_MONEY_JIAO, R.raw.jiao);
                break;
            case 12:
                addSound(context, Constants.TYPE_MONEY_FEN, R.raw.fen);
                break;
            case 13:
                addSound(context, Constants.TYPE_MONEY_POINT, R.raw.point);
                break;
        }

    }


    /**
     * 播放单个音频
     *
     * @param key
     */
    public void playSound(String key) {
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        final float volume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        int soundId = mSoundPool.play(
                soundMap.get(key), streamVolume,
                streamVolume, 1, 0, rate);
        mKillSoundQueue.add(soundId);
        // schedule the current sound to stop after set milliseconds
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (!mKillSoundQueue.isEmpty()) {
                    mSoundPool.stop(mKillSoundQueue
                            .firstElement());
                }
            }
        }, delay);
    }

    /**
     * 连续播放多个音频
     *
     * @param keys
     * @throws InterruptedException
     */
    public void playMutilSounds(ArrayList<String> keys)
            throws InterruptedException {
        int temp = 0;
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//        Log.v(TAG,"音量："+streamVolume);
        for (String key : keys) {
            if (soundMap.containsKey(key)) {
                int soundId = mSoundPool.play(
                        soundMap.get(key),
                        streamVolume, streamVolume, 1, 0,
                        rate);
                //sleep for a while for SoundPool play
                Thread.sleep(seperateTime);
                mKillSoundQueue.add(soundId);
                temp++;
            }
            //间隔停歇seperateTime时间
            if (temp == 1) {
                Thread.sleep(seperateTime);
            }
        }
        // schedule the current sound to stop after set milliseconds
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (!mKillSoundQueue.isEmpty()) {
                    mSoundPool.stop(mKillSoundQueue.firstElement());
                }
            }
        }, delay);
    }

    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Message msg = mHandler.obtainMessage(SOUND_LOAD_OK);
        msg.arg1 = sampleId;
        mHandler.sendMessage(msg);
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SOUND_LOAD_OK:
                    try {
                        playMutilSounds(typeList);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    integer_money.clear();
                    decimal_money.clear();
                    typeList.clear();
                    soundMap.clear();
                    mKillSoundQueue.clear();
                    break;
            }
        }
    }

}
