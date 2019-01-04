package com.facepp.demo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facepp.demo.bean.FaceActionInfo;
import com.facepp.demo.util.ConUtil;
import com.facepp.demo.util.DialogUtil;
import com.facepp.demo.util.ICamera;

import java.util.ArrayList;
import java.util.HashMap;

import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends Activity implements OnClickListener {

    private int min_face_size = 40;  // 200
    private int detection_interval = 30; // 100
    private String resolution = "640*480";
    private ArrayList<HashMap<String, Integer>> cameraSize;
    private RelativeLayout mListRel;
    private ListView mListView;
    private ListAdapter mListAdapter;
    private LayoutInflater mInflater;
    private boolean isShowListView;
    private HashMap<String, Integer> resolutionMap;
    private DialogUtil mDialogUtil;


    private boolean isStartRecorder, is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isOneFaceTrackig = true, isFaceCompare;

    private String[] editValues = {min_face_size + "", resolution, detection_interval + "", "YES", "Fast"};

    LinearLayout cameraSide;
    LinearLayout generateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initData();
        onClickListener();
        requestCameraPerm();
    }

    private void init() {
        mDialogUtil = new DialogUtil(this);

        cameraSide = findViewById(R.id.cameraSide);

        //set title
        TextView title = findViewById(R.id.title_layout_titleText);
        title.setText(getResources().getString(R.string.title));

        //vanish the back button
        findViewById(R.id.title_layout_returnRel).setVisibility(View.GONE);

        //configure take picture button
        Button enterBtn = findViewById(R.id.landmark_enterBtn);
        enterBtn.setText(getResources().getString(R.string.detect_face));
        enterBtn.setOnClickListener(this);


        mInflater = LayoutInflater.from(this);
        findViewById(R.id.activity_rootRel).setOnClickListener(this);

        mListRel = findViewById(R.id.landmark_listRel);
        mListRel.setOnClickListener(this);
        mListView = findViewById(R.id.landmark_list);
        mListView.setVerticalScrollBarEnabled(false);

        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
    }

    private void initData() {
        //setup cameraSideButton
        cameraSide.setOnClickListener(this);
        TextView cameraSideText = cameraSide.findViewById(R.id.cameraSideText);
        ImageView cameraSideImg = cameraSide.findViewById(R.id.cameraSidePic);
        if (isBackCamera) {
            cameraSideImg.setImageDrawable(getResources().getDrawable(R.drawable.backphone));
            cameraSideText.setText(R.string.back_camera);
        } else {
            cameraSideImg.setImageDrawable(getResources().getDrawable(R.drawable.frontphone));
            cameraSideText.setText(R.string.front_camera);
        }

        //setup generate button
        ImageView generateButtonLogo = findViewById(R.id.morph_mania_logo);
        Glide.with(MainActivity.this).load(R.drawable.ic_face).into(generateButtonLogo);
        generateButton = findViewById(R.id.landmark_logo);
        generateButton.setOnClickListener(this);
    }

    private void onclickImageItem(int index, boolean isSelect) {
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        EXTERNAL_STORAGE_REQ_CAMERA_CODE);
            } else
                getCameraSizeList();
        } else
            getCameraSizeList();
    }

    public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE)
            getCameraSizeList();
        if ((requestCode == EXTERNAL_STORAGE_REQ_AUDIO_CODE) && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
            startRecord();
    }

    //oppo vivo Second permission to access the adapter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 101) {
            String error = data.getStringExtra("errorcode");
            ConUtil.showToast(this, "sdk init error, code: " + error);
        }
        getCameraSizeList();
    }

    private void getCameraSizeList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int cameraId = 1;
                if (isBackCamera)
                    cameraId = 0;
                cameraSize = ICamera.getCameraPreviewSize(cameraId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
                        rlp.width = ConUtil.dip2px(MainActivity.this, 200);
                        rlp.height = ConUtil.dip2px(MainActivity.this, 55) * cameraSize.size();
                        mListView.setLayoutParams(rlp);
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void isShowListView() {
        isShowListView = !isShowListView;
        if (isShowListView)
            mListRel.setVisibility(View.GONE);
        else
            mListRel.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if (ID == R.id.cameraSide) {
            isBackCamera = !isBackCamera;
            TextView t = v.findViewById(R.id.cameraSideText);
            ImageView img = v.findViewById(R.id.cameraSidePic);
            if (isBackCamera) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.backphone));
                t.setText(R.string.back_camera);
            } else {
                img.setImageDrawable(getResources().getDrawable(R.drawable.frontphone));
                t.setText(R.string.front_camera);
            }
        }

        if (ID == R.id.landmark_enterBtn) {
            if (isStartRecorder)
                if (resolutionMap != null) {
                    int width = resolutionMap.get("width");
                    int height = resolutionMap.get("height");
                    if (width == 1056 && height == 864)
                        resolutionMap = null;
                    if (isBackCamera) {
                        if (width == 960 && height == 720)
                            resolutionMap = null;
                        if (width == 800 && height == 480)
                            resolutionMap = null;
                    }
                }

            FaceActionInfo faceActionInfo = new FaceActionInfo();
            faceActionInfo.isStartRecorder = isStartRecorder;
            faceActionInfo.is3DPose = is3DPose;
            faceActionInfo.isdebug = isDebug;
            faceActionInfo.isROIDetect = isROIDetect;
            faceActionInfo.is106Points = is106Points;
            faceActionInfo.isBackCamera = isBackCamera;
            faceActionInfo.faceSize = min_face_size;
            faceActionInfo.interval = detection_interval;
            faceActionInfo.resolutionMap = resolutionMap;
            faceActionInfo.isFaceProperty = isFaceProperty;
            faceActionInfo.isOneFaceTrackig = isOneFaceTrackig;
            faceActionInfo.trackModel = editValues[4]; //editItemTexts[4].getText().toString().trim();
            faceActionInfo.isFaceCompare = isFaceCompare;

            startActivityForResult(new Intent(MainActivity.this, OpenglActivity.class).putExtra("FaceAction", faceActionInfo), 101);
        }
    }


    public static final int EXTERNAL_STORAGE_REQ_AUDIO_CODE = 11;

    private void startRecord() {
        isStartRecorder = !isStartRecorder;
        onclickImageItem(0, isStartRecorder);
    }


    private void startRecordWithPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_AUDIO_CODE);
            } else
                startRecord();
        } else
            startRecord();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.ACTION_DOWN == event.getAction() && mListRel.getVisibility() != View.GONE) {
            isShowListView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (cameraSize == null)
                return 0;
            Log.w("ceshi", "cameraSize.size() === " + cameraSize.size());
            return cameraSize.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHoder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.cameralist_item, null);
                holder = new ViewHoder();
                holder.name = convertView.findViewById(R.id.cameralist_item_nameText);
                convertView.setTag(holder);
            } else {
                holder = (ViewHoder) convertView.getTag();
            }

            HashMap<String, Integer> map = cameraSize.get(position);

            String hoderNameText = map.get("width") + " * " + map.get("height");
            holder.name.setText(hoderNameText);
            return convertView;
        }

        class ViewHoder {
            TextView name, time, num;
        }
    }

    private void onClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isShowListView();
                resolutionMap = cameraSize.get(position);
                String str = resolutionMap.get("width") + "*" + resolutionMap.get("height");
//                DialogUtil.setTextSzie(editItemTexts[1], str.length());
//                editItemTexts[1].setText(str);
            }
        });
    }
}