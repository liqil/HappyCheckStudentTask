package com.uuch.android_zxinglibrary.utils;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.BaseSwipListAdapter;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.uuch.android_zxinglibrary.BaseActivity;
import com.uuch.android_zxinglibrary.R;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.uuch.android_zxinglibrary.R.drawable.*;

public class StudentMangment extends BaseActivity {
    public static final String DB_TAG = "DB_";

    private     String      fileName        = null;

    private     TextView    editTextDate    = null;
    private     Spinner     spinnerSubject  = null;
    private     Spinner     spinnerGrade    = null;
    private     Spinner     spinnerCalssx   = null;
    //private     Spinner     spinnerBatch    = null;

    private     TextView    editTextStudentName = null;
    private     TextView    editTextStudentNum  = null;
    private     Button      btnAdd              = null;
    private     Button      btnDel              = null;
    private     StudentDB   studentDB           = null;
    private     SQLiteDatabase dbW              = null;
    private     SQLiteDatabase dbR              = null;

    private int year = 2018;
    private int month = 8;
    private int day = 28;

    //private List<ApplicationInfo> mAppList;
    private List<String> mAppList = new ArrayList<>();
    private AppAdapter mAdapter;
    private SwipeMenuListView mListView;

    private Bitmap mBitmap = null;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_mangment);

        initView();
        //Intent intent=getIntent();
        //dbName = intent.getStringExtra("dbName");
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void updateQRCodeBitmap(String id,String name)
    {
        String textContent = name + "      " + id;

        Log.i("liqil3", "abc " + imageView.getHeight() +" abc "+ imageView.getWidth());

        mBitmap = CodeUtils.createImage(textContent, imageView.getWidth(), imageView.getHeight(),
                BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher));
        //final String[] splited = textContent.split("\\s+");
        //Toast.makeText(StudentMangment.this, "iv_icon_click", Toast.LENGTH_SHORT).show();
        imageView.setImageBitmap(mBitmap);

        Log.i("liqil3", "def " + mBitmap.getHeight() +" def "+ mBitmap.getWidth());

        editTextStudentName.setText(name);
        editTextStudentNum.setText(id);
    }

    private void updateStudentInfoList() {
        Cursor cursor = dbR.query(StudentDB.TABLE_NAME,
                null,null, null,null, null,
                "StudentId asc");
        Log.i("liqil", "000000000 ------------->");

        mAppList.clear();

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("StudentName"));
            String id = cursor.getString(cursor.getColumnIndex("StudentId"));
            String sexy = cursor.getString(cursor.getColumnIndex("Sex"));

            Log.i("liqil", "99999999999 ------------->"
                    + "  " + name + "  " + id + "  " + sexy);

            String listItem = String.format("%s",id)+"      "+name;

            if(true != mAppList.contains(listItem)) {
                //if(mAppList.indexOf(listItem) == -1){
                mAppList.add(listItem);
                //Log.i("liqil", "000000000000000------------->"
                //        + "  "+ listItem);
            }
            mAdapter.notifyDataSetChanged();
        }
    }


    private void initSwipeMenuView(){
        //mAppList = getPackageManager().getInstalledApplications(0);

        mListView = (SwipeMenuListView) findViewById(R.id.listView);

        mAdapter = new AppAdapter();
        mListView.setAdapter(mAdapter);

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(15);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                //String item = mAppList.get(position);
                switch (index) {
                    case 0:
                        break;
                    case 1:
                        String listItem = mAppList.get(position);
                        //Log.i("liqil", "66666666666666 .... " + listItem);
                        mAppList.remove(position);
                        mAdapter.notifyDataSetChanged();
                        ///< 拆分字符串
                        String[] splited = listItem.split("\\s+");
                        deleteDBItem(Integer.valueOf(splited[0]), null);
                        /*String regEx = "[^0-9]";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m  = p.matcher(listItem);
                        String str = m.replaceAll("").trim();
                        Log.i("liqil", "7777777777777 .... " + str);
                        deleteDBItem(Integer.valueOf(str), null);*/
                        break;
                }
                return false;
            }
        });
    }

    private void deleteDBItem(int id, String name) {
        if(id != -1){
            dbW.delete(StudentDB.TABLE_NAME, "StudentID=?", new String[]{String.format("%d",id)});
        /*    cursor = dbW.query(StudentDB.TABLE_NAME,
                    new String[]{"StudentId","StudentName","Sex"},
                    "StudentName=?", new String[]{name},
                    null, null, null);*/
            Log.i("liqil", "delete databaseItem using id .... ");
        }

        if(null != name){
            //dbW.delete(StudentDB.TABLE_NAME, "StudentName=?", new String[]{name});
            Log.i("liqil", "delete databaseItem using name .... ");
        }
    }

    private void initDB( ){
        /*fileName = DB_TAG+String.format("%d", year) + "_" +
                spinnerSubject.getSelectedItem().toString().trim() + "_" +
                spinnerGrade.getSelectedItem().toString().trim() + "_grade" + "_" +
                spinnerCalssx.getSelectedItem().toString().trim() + "_classx";*/
        fileName = DB_TAG+String.format("%d", year) +"_"+
                spinnerGrade.getSelectedItem().toString().trim() + "_grade" + "_" +
                spinnerCalssx.getSelectedItem().toString().trim() + "_classx";

        String dbName = fileName;

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
        studentDB = new StudentDB(StudentMangment.this, dbName);
        dbR = studentDB.getReadableDatabase();
        dbW = studentDB.getWritableDatabase();
    }

    private void closeDB( ){
        if(studentDB != null)  studentDB.close();
        if(dbW != null)     dbW.close();
        if(dbR != null)     dbR.close();
        studentDB = null;
        fileName = null;
    }

    private void initView() {
        editTextDate = (EditText) findViewById(R.id.date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        editTextDate.setText(simpleDateFormat.format(date));

        spinnerSubject = (Spinner) findViewById(R.id.subject_spinner);
        spinnerGrade = (Spinner) findViewById(R.id.grade_spinner);
        spinnerCalssx = (Spinner) findViewById(R.id.classx_spinner);
        //spinnerBatch    = (Spinner) findViewById(R.id.batch_spinner);

        editTextStudentName = (EditText) findViewById(R.id.student_name);
        editTextStudentNum = (EditText) findViewById(R.id.student_num);
        btnAdd = (Button) findViewById(R.id.btn_add_data);
        btnDel = (Button) findViewById(R.id.btn_read_db_data);


        btnAdd.setOnClickListener(new ButtonOnClickListener(btnAdd.getId()));
        btnDel.setOnClickListener(new ButtonOnClickListener(btnDel.getId()));

        /*editTextDate.setOnClickListener(new StudentMangment.ButtonOnClickListener(editTextDate.getId()));
        editTextStudentName.setOnClickListener(new StudentMangment.ButtonOnClickListener(editTextStudentName.getId()));
        editTextStudentNum.setOnClickListener(new StudentMangment.ButtonOnClickListener(editTextStudentNum.getId()));
        */

        editTextStudentName.setOnTouchListener(OnEditTextTouchListener);
        editTextStudentNum.setOnTouchListener(OnEditTextTouchListener);
        editTextDate.setOnTouchListener(OnEditTextTouchListener);

        initSwipeMenuView();

        imageView = (ImageView) findViewById(R.id.image_code);


        //Bitmap bitmap = new Bitmap(R.drawable.ic_launcher);

        //bitmap.
        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        Log.i("liqil3", "rrrr  " + imageView.getWidth() + "  " + imageView.getHeight());

        //imageView.setImageResource(R.drawable.ic_launcher);
        //
        // imageView.setImageDrawable();
    }

    private OnTouchListener OnEditTextTouchListener = new OnTouchListener(){
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            EditText edit = findViewById(view.getId());
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                switch (view.getId()) {
                    case R.id.student_name:
                    case R.id.student_num:
                        edit.setText("");
                        InputMethodManager inputManager =
                                (InputMethodManager) edit.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(edit, 0);
                        break;
                    case R.id.date:
                        new DatePickerDialog(StudentMangment.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                  int dayOfMonth) {
                                StudentMangment.this.year = year;
                                StudentMangment.this.month = monthOfYear+1;
                                StudentMangment.this.day = dayOfMonth;
                                editTextDate.setText(String.format("%d-%d-%d",year,month,day));
                            }
                        }, 2018, 8, 28).show();
                        break;
                }
            }
            return false;
        }
    };


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
            /*switch (v.getId()){
                case R.id.subject_spinner:
                case R.id.grade_spinner:
                case R.id.classx_spinner:
                case R.id.batch_spinner:
                case R.id.date:
                    ///< close db
                    //closeDB(fileName);
                    break;
            }*/

            if(v.getId() == R.id.btn_add_data){
                if(0==editTextStudentName.length()
                        || 0==editTextStudentNum.length()) {
                    Toast.makeText(getApplicationContext(), "请添加学生信息", 1000).show();
                    return;
                }

                initDB();
                Log.i("liqil", "open database------------->"+fileName+"   "+studentDB.getDatabaseName());

                ///< add db
                ContentValues cv = new ContentValues();
                int id = 0;
                String name = null;

                try{
                    id = Integer.parseInt(editTextStudentNum.getText().toString());
                }  catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(), "学号栏请输入纯数字", 1000).show();
                    return;
                }
                name = editTextStudentName.getText().toString().trim();

                Log.i("liqil", "22222222222------------->"+String.format("%s",id)+"  "+dbR.toString());

                ///< check whether exist
                Cursor cursorName = dbR.query(StudentDB.TABLE_NAME,
                        new String[]{"StudentId","StudentName","Sex"},
                        "StudentName=?", new String[]{name},
                        null, null, null);
                Cursor cursorID = dbR.query(StudentDB.TABLE_NAME,
                        new String[]{"StudentId","StudentName","Sex"},
                        "StudentId=?", new String[]{String.format("%d",id)},
                        null, null, null);
                Cursor cursor = null;
                if(cursorName.getCount() > 0)       cursor = cursorName;
                else if(cursorID.getCount() > 0)    cursor = cursorID;
                else                                cursor = null;
                if(cursor != null){
                    cursor.moveToFirst();
                    Toast.makeText(getApplicationContext(), "该学生信息已经存在", 1000).show();
                    String namex = cursor.getString(cursor.getColumnIndex("StudentName"));
                    String idx   = cursor.getString(cursor.getColumnIndex("StudentId"));
                    String sexy  = cursor.getString(cursor.getColumnIndex("Sex"));
                    //Log.i("liqil", "4444444444444444------------->"
                    //        +"  "+ name +"  "+ id +"  "+ sexy);
                    return;
                }

                cv.put("StudentId",     id);
                cv.put("StudentName",   name);
                cv.put("Sex",          "暂时为空");
                dbW.insert(StudentDB.TABLE_NAME,null,cv);
                Log.i("liqil", "将要增加数据项------------->"
                        +"  "+String.format("%s",id)+"  "+name);

                Toast.makeText(getApplicationContext(), "执行增加数据库...", 1000).show();

                updateStudentInfoList();

                updateQRCodeBitmap(editTextStudentNum.getText().toString(),editTextStudentName.getText().toString());

                //tts.speak("hello china ",TextToSpeech.QUEUE_ADD, null);
            }

            if (v.getId() == R.id.btn_read_db_data) {
                initDB();

                updateStudentInfoList();
            }

            if(v.getId() == R.id.date){
                new DatePickerDialog(StudentMangment.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        StudentMangment.this.year = year;
                        StudentMangment.this.month = monthOfYear+1;
                        StudentMangment.this.day = dayOfMonth;
                        editTextDate.setText(String.format("%d-%d-%d",year,month,day));
                    }

                }, 2018, 8, 28).show();

            }
        }
    }

/*    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_mangment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }*/
    class AppAdapter extends BaseSwipListAdapter {

        @Override
        public int getCount() {
            return mAppList.size();
        }

        @Override
        public String getItem(int position) {
            return mAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(),
                        R.layout.item_list_app, null);
                new ViewHolder(convertView);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            //String item = getItem(position);
            //holder.iv_icon.setImageDrawable(item.loadIcon(getPackageManager()));
            //holder.tv_name.setText(item.loadLabel(getPackageManager()));

            String textContent = getItem(position);
            if (TextUtils.isEmpty(textContent)) {
                Toast.makeText(StudentMangment.this, "您的输入为空!", Toast.LENGTH_SHORT).show();
                return null;
            }

 /*           mBitmap = CodeUtils.createImage(textContent, 400, 400,
                                BitmapFactory.decodeResource(getResources(),
                                R.mipmap.ic_launcher));*/
            final String[] splited = textContent.split("\\s+");

            holder.tv_name.setText(textContent);
            holder.iv_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(StudentMangment.this, "iv_icon_click", Toast.LENGTH_SHORT).show();

                    updateQRCodeBitmap(splited[0], splited[1]);
                    /*imageView.setImageBitmap(mBitmap);
                    editTextStudentNum.setText(splited[0]);
                    editTextStudentName.setText(splited[1]);*/
                }
            });
            holder.tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(StudentMangment.this,"iv_name_click",Toast.LENGTH_SHORT).show();
                    updateQRCodeBitmap(splited[0], splited[1]);
                    /*imageView.setImageBitmap(mBitmap);
                    editTextStudentNum.setText(splited[0]);
                    editTextStudentName.setText(splited[1]);*/
                }
            });
            return convertView;
        }

        class ViewHolder {
            ImageView iv_icon;
            TextView tv_name;

            public ViewHolder(View view) {
                iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                tv_name = (TextView) view.findViewById(R.id.tv_name);
                view.setTag(this);
            }
        }

        @Override
        public boolean getSwipEnableByPosition(int position) {
            //if(position % 2 == 0){
            //    return false;
            //}
            return true;
        }
    }
}
