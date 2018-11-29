package com.uuch.android_zxinglibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.uuch.android_zxinglibrary.utils.CheckPermissionUtils;
import com.uuch.android_zxinglibrary.utils.StudentDB;
import com.uuch.android_zxinglibrary.utils.StudentMangment;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;
import me.zhouzhuo.zzexcelcreator.ZzExcelCreator;
import me.zhouzhuo.zzexcelcreator.ZzFormatCreator;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class MainActivity extends BaseActivity implements PermissionCallbacks{
    /**
     * 扫描跳转Activity RequestCode
     */
    public static final int REQUEST_CODE = 111;
    /**
     * 选择系统图片Request Code
     */
    public static final int REQUEST_IMAGE = 112;

    public static final int REQUEST_STUDENT_MANAGMENT = 113;

    public static final String DB_TAG = "DB_";//"DB_";

    public Button button_scan = null;
    /*public Button button2 = null;
    public Button button3 = null;
    public Button button4 = null;*/
    private Spinner     spinnerSubject  = null;
    private Spinner     spinnerGrade    = null;
    private Spinner     spinnerCalssx   = null;
    private Spinner     spinnerBatch    = null;
    private EditText    editTextDate    = null;

    private int year = 2018;
    private int month = 8;
    private int day = 28;


    ///<database
    private StudentDB studentDB = null;
    private  SQLiteDatabase dbR = null;
    private  SQLiteDatabase dbW = null;

    String fileName  = "TEST1";
    String fileNamePre = "TEST2";
    String sheetName = null;
    String sheetNamePre = null;

    String fileDBName = null;

    ZzExcelCreator excelCreator = null;
    WritableCellFormat format   = null;

    //初始化tts监听对象
    TextToSpeech tts = null;

    ///< 文件相关参数
    private static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aAMyBaby/";//"/ZzExcelCreator/";

    @Override
    protected void onDestroy() {
        /*try {
            excelCreator.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }*/

        if (tts != null)
        {
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 初始化组件
         */
        initView();
        //初始化权限
        initPermission();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                // 如果装载TTS引擎成功
                if (status == TextToSpeech.SUCCESS)
                {
                    // 设置使用美式英语朗读
                    int result = tts.setLanguage(Locale.CHINA);
                    // 如果不支持所设置的语言
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE)
                    {
                        Toast.makeText(MainActivity.this, "TTS暂时不支持这种语言的朗读。", 50000)
                                .show();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);//这里是调用menu文件夹中的main.xml，在登陆界面label右上角的三角里显示其他功能
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        Uri uri       = null;
        switch (item.getItemId()){
            case R.id.add_delete_data:
                //Toast.makeText(getApplicationContext(), "touch add_delete_data", 1000).show();
                intent = new Intent(MainActivity.this, StudentMangment.class);
                startActivity(intent);
                return true;
            case R.id.get_qr_code:
                intent = new Intent(MainActivity.this, ThreeActivity.class);
                startActivity(intent);
                //Toast.makeText(getApplicationContext(), "touch get_qr_code", 1000).show();
                return true;
            case R.id.show_result:
                intent          = new Intent();
                Bundle bundle   = new Bundle();
                bundle.putString("OpenMode", "ReadOnly");// 只读模式
                bundle.putBoolean("SendCloseBroad", true);
                bundle.putString("ThirdPackage", getPackageName());
                bundle.putBoolean("ClearTrace", false);
                bundle.putBoolean("ClearBuffer", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setClassName("cn.wps.moffice_eng",
                        "cn.wps.moffice.documentmanager.PreStartActivity2");

                File file = new File(PATH + fileName + ".xls");

                if(!file.exists()){
                    return false;
                }

                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.N){
                    uri = FileProvider.getUriForFile(MainActivity.this,
                            BuildConfig.APPLICATION_ID+".fileprovider",
                            file);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }else{
                    uri = Uri.fromFile(file);
                }

                //Uri uri = Uri.fromFile(new File(PATH + fileName + ".xls"));
                Log.e("liqil1", uri+"");

                intent.setData(uri);
                intent.putExtras(bundle);
                try {
                    startActivity(intent);
                } catch (Exception e){
                    System.out.println("打开wps异常："+e.toString());
                    e.printStackTrace();
                    return false;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 初始化权限事件
     */
    private void initPermission() {
        //检查权限
        String[] permissions = CheckPermissionUtils.checkPermission(this);
        if (permissions.length == 0) {
            //权限都申请了
            //是否登录
        } else {
            //申请权限
            ActivityCompat.requestPermissions(this, permissions, 100);
        }
    }
    /**
     * 初始化组件
     */
    @SuppressLint("DefaultLocale")
    private void initView() {
        button_scan = (Button) findViewById(R.id.button_scan);
        /*button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);*/

        editTextDate    = (EditText) findViewById(R.id.date);
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //Date date = new Date(System.currentTimeMillis());
        //editTextDate.setText(simpleDateFormat.format(date));
        Calendar mycalendar=Calendar.getInstance();
        year=mycalendar.get(Calendar.YEAR); //获取Calendar对象中的年
        month=mycalendar.get(Calendar.MONTH)+1;//获取Calendar对象中的月
        day=mycalendar.get(Calendar.DAY_OF_MONTH);//获取这个月的第几天
        editTextDate.setText(String.format("%d-%d-%d", year, month, day));

        spinnerSubject  = (Spinner) findViewById(R.id.subject_spinner);
        spinnerGrade    = (Spinner) findViewById(R.id.grade_spinner);
        spinnerCalssx   = (Spinner) findViewById(R.id.classx_spinner);
        spinnerBatch    = (Spinner) findViewById(R.id.batch_spinner);

        /**
         * 打开默认二维码扫描界面
         *
         * 打开系统图片选择界面
         *
         * 定制化显示扫描界面
         *
         * 测试生成二维码图片
         */

        button_scan.setOnClickListener(new ButtonOnClickListener(button_scan.getId()));
        /*button2.setOnClickListener(new ButtonOnClickListener(button2.getId()));
        button3.setOnClickListener(new ButtonOnClickListener(button3.getId()));
        button4.setOnClickListener(new ButtonOnClickListener(button4.getId()));*/
        editTextDate.setOnClickListener(new ButtonOnClickListener(editTextDate.getId()));

        ///< 注册长按事件，长按该按键，停止录入
        button_scan.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "long touch", 1000).show();
                return true;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);

                    String[] splited = result.split("\\s+");
                    if(splited.length == 2)
                        storeResultToExcel(splited[1]);
                    //else
                    tts.speak(splited[0],TextToSpeech.QUEUE_ADD, null);
                        //Toast.makeText(this, "解析结果:" +s result, Toast.LENGTH_LONG).show();
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                }
            }
        }

        /**
         * 选择系统图片并解析
         */
        else if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            Toast.makeText(MainActivity.this, "解析结果:" + result, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            Toast.makeText(MainActivity.this, "解析二维码失败", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        else if (requestCode == REQUEST_CAMERA_PERM) {
            Toast.makeText(this, "从设置页面返回...", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    /**
     * 请求CAMERA权限码
     */
    public static final int REQUEST_CAMERA_PERM = 101;


    /**
     * EsayPermissions接管权限处理逻辑
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @AfterPermissionGranted(REQUEST_CAMERA_PERM)
    public void cameraTask(int viewId) {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // Have permission, do the thing!
            onClick(viewId);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, "需要请求camera权限",
                    REQUEST_CAMERA_PERM, Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Toast.makeText(this, "执行onPermissionsGranted()...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "执行onPermissionsDenied()...", Toast.LENGTH_SHORT).show();
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, "当前App需要申请camera权限,需要打开设置页面么?")
                    .setTitle("权限申请")
                    .setPositiveButton("确认")
                    .setNegativeButton("取消", null /* click listener */)
                    .setRequestCode(REQUEST_CAMERA_PERM)
                    .build()
                    .show();
        }
    }

    /**
     * 按钮点击监听
     */
    class ButtonOnClickListener implements View.OnClickListener{

        private int buttonId;

        public ButtonOnClickListener(int buttonId) {
            this.buttonId = buttonId;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.date){
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                        MainActivity.this.year = year;
                        month = monthOfYear+1;
                        day = dayOfMonth;
                        editTextDate.setText(String.format("%d-%d-%d",year,month,day));
                    }
                }, year, month, day).show();
            }

            if(v.getId() == R.id.button_scan) {
                cameraTask(buttonId);
            }
            /*if (v.getId() == R.id.button2) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            } else if (v.getId() == R.id.button4) {
                Intent intent = new Intent(MainActivity.this, ThreeActivity.class);
                startActivity(intent);
            } else {
                cameraTask(buttonId);
            }*/
        }
    }


    /**
     * 按钮点击事件处理逻辑
     * @param buttonId
     */
    private void onClick(int buttonId) {
        switch (buttonId) {
            case R.id.button_scan:
                createExcel();
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            /*case R.id.button3:
                intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;*/
            default:
                break;
        }
    }

    private void storeResultToExcel(String studentID){
        Cursor cursor = null;
        int dbItemCount = 0;

        //initDB();

        if(dbR != null) {
            cursor = dbR.query(StudentDB.TABLE_NAME,
                    null, null, null, null, null,
                    "StudentId asc");
            dbItemCount = cursor.getCount();
        }

        try {
            int index = 1;
            ZzExcelCreator creator = null;
            Log.i("liqil","storeResultToExcel"+"   "+PATH + "   " + fileName + "   " + sheetName);

            if(dbItemCount > 0)
                creator = ZzExcelCreator.getInstance()
                        .openExcel(new File(PATH + fileName + ".xls"))
                        .openSheet(sheetName);

            while (cursor!=null && cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("StudentName"));
                String id = cursor.getString(cursor.getColumnIndex("StudentId"));

                creator.fillContent(0, index, name, format)
                    .fillContent(1, index, id, format);
                    //.fillContent(2, index, "N", format);
                index++;
            }

            for(int i=0; i<dbItemCount; i++) {
                String id = creator.getCellContent(1, i+1); ///<col row
                if(id.equals(studentID)){
                    creator.fillContent(2, i+1, "Y", format);
                }
            }
            if(dbItemCount > 0)
                creator.close();

        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
    }

    private int setupExcel(String path, String fileName, String sheetName) {
        /*try {
            ZzExcelCreator
                    .getInstance()
                    .createExcel(PATH, fileName)
                    .createSheet(sheetName)
                    .close();
            //return true;
        } catch (IOException | WriteException e) {
            e.printStackTrace();
            return false;
        } catch (BiffException e) {
            e.printStackTrace();
            return false;
        }*/

        /* 读取数据库，初始化excel列表
            姓名      学号     Y/N
            张三      001     1
            李四      002     0
        */
        //if ((!fileName.equals(fileNamePre)) || (!sheetName.equals(sheetNamePre))) {
            try {
                /*if(null !=excelCreator){
                    excelCreator.close();
                }*/
                Log.i("liqil","setupExcel"+"   "+path + "   " + fileName + "   " + sheetName);

                ZzExcelCreator
                        .getInstance()
                        .createExcel(path, fileName)
                        .createSheet(sheetName)
                        .close();

                    /*excelCreator = ZzExcelCreator
                            .getInstance()
                            .openExcel(new File(PATH + File.separator + fileName + ".xls"))
                            .openSheet(sheetName);*/

                format = ZzFormatCreator
                        .getInstance()
                        .createCellFont(WritableFont.ARIAL)
                        .setAlignment(Alignment.CENTRE, VerticalAlignment.CENTRE)
                        .setFontSize(10)
                        .setFontColor(Colour.ROSE)
                        .getCellFormat();
                ZzExcelCreator
                        .getInstance()
                        .openExcel(new File(path + fileName + ".xls"))
                        .openSheet(sheetName)
                        .fillContent(0, 0, "姓名", format)
                        .fillContent(1, 0, "学号", format)
                        .fillContent(2, 0, "Y/N", format)
                        .close();

                    /*excelCreator.fillContent(0, 0, "姓名", format)
                            .fillContent(1, 0, "学号", format)
                            .fillContent(2, 0, "Y/N", format);*/

                //storeResultToExcel("NoBody");
                    //excelCreator.close();
            } catch (WriteException e) {
                e.printStackTrace();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            } catch (BiffException e) {
                e.printStackTrace();
                return 0;
            }
        //}

        return 1;
    }

    //@SuppressLint("StaticFieldLeak")
    @SuppressLint("StaticFieldLeak")
    private void createExcel() {

        /*fileName = String.format("%d年", year) + "_" +
                spinnerSubject.getSelectedItem().toString().trim() + "_" +
                spinnerGrade.getSelectedItem().toString().trim() + "年级" + "_" +
                spinnerCalssx.getSelectedItem().toString().trim() + "班";*/
        initDB();

        fileNamePre = fileName;
        sheetNamePre = sheetName;

        if(year < 7) {
            fileName = String.format("%d", year) + "_" +
                    spinnerSubject.getSelectedItem().toString().trim() +"_semester"+"_1"+
                    "_grade"+spinnerGrade.getSelectedItem().toString().trim() + "_" +
                    spinnerCalssx.getSelectedItem().toString().trim();
        } else {
            fileName = String.format("%d", year) + "_" +
                    spinnerSubject.getSelectedItem().toString().trim() +"_semester"+"_2"+
                    "_grade"+spinnerGrade.getSelectedItem().toString().trim() + "_" +
                    spinnerCalssx.getSelectedItem().toString().trim();
        }

        sheetName = editTextDate.getText().toString().trim() + "_" +
                spinnerBatch.getSelectedItem().toString().trim();
        //Toast.makeText(getApplicationContext(), fileName + "_" + sheetName, 1000).show();

        ///< create excel file
        new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... params) {
                /*Log.i("liqil", "开始调用 setupExcel!");
                if (0 == setupExcel(PATH, params[0], params[1])) {
                    return 0;
                }
                return 1;*/
                try {
                    ZzExcelCreator
                            .getInstance()
                            .createExcel(PATH, params[0])
                            .createSheet(params[1])
                            .close();
                    return 1;
                } catch (IOException | WriteException e) {
                    e.printStackTrace();
                    return 0;
                } catch (BiffException e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer aVoid) {
                super.onPostExecute(aVoid);
                if (aVoid == 1) {
                    ;//Toast.makeText(MainActivity.this, "表格创建成功！请到" + PATH + "路径下查看~", Toast.LENGTH_SHORT).show();
                    Log.i("liqil", "表格创建成功！");
                } else {
                    ;//Toast.makeText(MainActivity.this, "表格创建失败！", Toast.LENGTH_SHORT).show();
                    Log.i("liqil", "表格创建失败！");
                }
            }
            //}.execute(fileName, sheetName);
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, fileName, sheetName);

        ///< add title
        new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... params) {
                Log.i("liqil", "开始设定title!");
                try {
                    /*format = ZzFormatCreator
                            .getInstance()
                            .createCellFont(WritableFont.ARIAL)
                            .setAlignment(Alignment.CENTRE, VerticalAlignment.CENTRE)
                            .setFontSize(10)
                            .setFontColor(Colour.ROSE)
                            .getCellFormat();*/
                    ZzExcelCreator
                            .getInstance()
                            .openExcel(new File(PATH + fileName + ".xls"))
                            .openSheet(sheetName)
                            .fillContent(0, 0, "姓名", format)
                            .fillContent(1, 0, "学号", format)
                            .fillContent(2, 0, "Y/N", format)
                            .close();
                    return 1;
                } catch (WriteException e) {
                    e.printStackTrace();
                    return 0;
                } catch (IOException e) {
                    e.printStackTrace();
                    return 0;
                } catch (BiffException e) {
                    e.printStackTrace();
                    return 0;
                }
            }

            @Override
            protected void onPostExecute(Integer aVoid) {
                super.onPostExecute(aVoid);
                if (aVoid == 1) {
                    ;//Toast.makeText(MainActivity.this, "表格创建成功！请到" + PATH + "路径下查看~", Toast.LENGTH_SHORT).show();
                    Log.i("liqil", "表格title创建成功！");
                } else {
                    ;//Toast.makeText(MainActivity.this, "表格创建失败！", Toast.LENGTH_SHORT).show();
                    Log.i("liqil", "表格title创建失败！");
                }
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, fileName, sheetName);

        ///< excel学生信息
        new AsyncTask<String, Void, Integer>() {

            @Override
            protected Integer doInBackground(String... params) {
                Log.i("liqil", "开始调用 storeResultToExcel!");
                storeResultToExcel("NoBody");
                /*try {
                    ZzExcelCreator.getInstance()
                            .openExcel(new File(PATH + fileName + ".xls"))
                            .openSheet(sheetName)
                            .fillContent(1, 1, "hello", format)
                            .close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (BiffException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }*/
                return 1;
            }

            @Override
            protected void onPostExecute(Integer aVoid) {
                super.onPostExecute(aVoid);
                if (aVoid == 1) {
                    ;//Toast.makeText(MainActivity.this, "表格创建成功！请到" + PATH + "路径下查看~", Toast.LENGTH_SHORT).show();
                    Log.i("liqil", "从数据库更新数据成功!");
                } else {
                    Log.i("liqil", "从数据库更新数据失败!")
                    ;//Toast.makeText(MainActivity.this, "表格创建失败！", Toast.LENGTH_SHORT).show();
                }
            }
        //}.execute(fileName, sheetName);
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, fileName, sheetName);
    }


    ///< database
    private void initDB( ){
        fileDBName = DB_TAG+String.format("%d", year) +"_"+
                spinnerGrade.getSelectedItem().toString().trim() + "_grade" + "_" +
                spinnerCalssx.getSelectedItem().toString().trim() + "_classx";

        String dbName = fileDBName;

        //closeDB();

        if (null != studentDB) {
            if (dbName.equals(studentDB.getDatabaseName())) {
                ///< database has created
                Log.i("liqil", "database has created .... ");
                return;
            } else {
                ///< close old database
                Log.i("liqil", "old database closed.... ");
                closeDB();
            }
        }
        studentDB = new StudentDB(MainActivity.this, dbName);
        dbR = studentDB.getReadableDatabase();
        dbW = studentDB.getWritableDatabase();
    }

    private void closeDB( ){
        if(studentDB != null)  studentDB.close();
        if(dbW != null)     dbW.close();
        if(dbR != null)     dbR.close();
        studentDB  = null;
        fileDBName = null;
    }
}

