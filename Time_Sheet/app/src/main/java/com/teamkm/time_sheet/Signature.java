package com.teamkm.time_sheet;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.jar.Attributes;

public class Signature extends AppCompatActivity {

    TimeReport[] weekdays1;
    TimeReport[] weekdays2;

    String jsonMyObject1;
    String jsonMyObject2;
    String employeename;
    String employeeId;
    String employeeDate;

    Toolbar toolbar;
    Button btn_get_sign, mClear, mGetSign, mCancel;

    File file;
    Dialog dialog;
    LinearLayout mContent;
    View view;
    signature mSignature;
    Bitmap bitmap;

    // Creating Separate Directory for saving Generated Images
    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/DigitSign/";
    String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    String StoredPath = DIRECTORY + pic_name + ".png";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject1 = extras.getString("weekdays1");
            jsonMyObject2 = extras.getString("weekdays2");
            employeename= extras.getString("employeename");
            employeeId= extras.getString("employeeId");
            employeeDate= extras.getString("employeeDate");
        }

         weekdays1 = new Gson().fromJson(jsonMyObject1, TimeReport[].class);
         weekdays2 = new Gson().fromJson(jsonMyObject2, TimeReport[].class);


        // Setting ToolBar as ActionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Button to open signature panel
        btn_get_sign = (Button) findViewById(R.id.signature);

        // Method to create Directory, if the Directory doesn't exists
        file = new File(DIRECTORY);
        if (!file.exists()) {
            file.mkdir();
        }

        // Dialog Function
        dialog = new Dialog(Signature.this);
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        btn_get_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Function call for Digital Signature
                dialog_action();

            }
        });

    }


    public void dialog_action() {

        mContent = (LinearLayout) dialog.findViewById(R.id.linearLayout);
        mSignature = new signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mClear = (Button) dialog.findViewById(R.id.clear);
        mGetSign = (Button) dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) dialog.findViewById(R.id.cancel);
        view = mContent;

        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("tag", "Panel Cleared");
                mSignature.clear();
                mGetSign.setEnabled(false);
            }
        });
        mGetSign.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Log.v("tag", "Panel Saved");
                view.setDrawingCacheEnabled(true);
                mSignature.save(view, StoredPath);
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
                // Calling the same class
                recreate();
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("tag", "Panel Cancelled");
                dialog.dismiss();
                // Calling the same class
                recreate();
            }
        });
        dialog.show();
    }



    public class signature extends View
    {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }


        public void save(View v, String StoredPath) {
            Log.v("tag", "Width: " + v.getWidth());
            Log.v("tag", "Height: " + v.getHeight());
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            Canvas canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);
                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                createPDF(StoredPath);
                File delsig = new File(StoredPath);
                boolean del = delsig.delete();
                mFileOutStream.flush();
                mFileOutStream.close();
            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }
        }


        public void clear() {
            path.reset();
            invalidate();
        }


        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }



    }





//    public void Submit(View view)
//{
//    createPDF(view);
//}

    public String[] calculatePayPeriods(String strdate) throws ParseException
    {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String[] strdates= new String[4];
        int[] sub = {0,-6,-7,-13};
        Date date = new SimpleDateFormat("MM/dd/yyyy").parse(strdate);

        for(int i =0;i<4;i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DAY_OF_YEAR, sub[i]);
            Date startdate = cal.getTime();
            String reportDate = df.format(startdate);
            strdates[i]= reportDate;
        }



        return strdates;
    }




    public void createPDF(String Imgpath)
    {
        //TextView txt = (TextView)findViewById(R.id.txt1);

        Document doc = new Document();

        String output =  Environment.getExternalStorageDirectory()+"/TIMESHEET.pdf";

        String state = Environment.getExternalStorageState();

        StatFs statFsI = new StatFs(Environment.getRootDirectory().getAbsolutePath());

        StatFs statFsE = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());

        String readperm = Manifest.permission.READ_EXTERNAL_STORAGE;
        String writeperm = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        String[] perm = {writeperm,readperm};

        int i = 0;

        //requestPermissions(perm,200);



        try {
            PdfWriter.getInstance(doc, new FileOutputStream(output));
            doc.open();
            //doc.add(new Paragraph(txt.getText().toString()));


            String strEmpID = employeeId;
            String strEmpName = employeename;
            String stredPP = employeeDate;

            String[] total1 = total(weekdays1);
            String[] total2 = total(weekdays2);
            String[] emp = {strEmpName,strEmpID,stredPP};
            setPage(doc, emp,weekdays1,total1,weekdays2,total2,Imgpath);
            doc.close();
            System.out.println("File created at"+ Environment.DIRECTORY_DOWNLOADS);
            sendMail(output);
            Toast.makeText(getApplicationContext(),"Send email successfully",Toast.LENGTH_SHORT);
            finishAffinity();
        }
        catch (DocumentException e)
        {
            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    public void sendMail(String path)
    {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT,"Timesheet");
        email.putExtra(Intent.EXTRA_TEXT, "Hello, the time sheet for");
        Uri uri = Uri.parse("file://"+path);
        email.putExtra(Intent.EXTRA_STREAM, uri);
        email.setType("message/rfc822");
        startActivity(email);
    }


    public String[] total(TimeReport[] weeks)
    {

        int ireg = 0;
        int ihol = 0;
        int ivac = 0;
        int isic = 0;
        int ioh = 0;
        int ioc = 0;
        int itot = 0;


        for(int i = 0; i < weeks.length-1;i++)
        {
            ireg += Integer.parseInt(weeks[i].reg);
            ihol += Integer.parseInt(weeks[i].hol);
            isic += Integer.parseInt(weeks[i].sic);
            ivac += Integer.parseInt(weeks[i].vac);
            ioh  += Integer.parseInt(weeks[i].oh);
            ioc  += Integer.parseInt(weeks[i].oc);
            itot  += Integer.parseInt(weeks[i].tot);
        }


        String[] str =  {"Total Time Report",Integer.toString(ireg),Integer.toString(ihol),Integer.toString(ivac),
                Integer.toString(isic), Integer.toString(ioh),Integer.toString(ioc),
                Integer.toString(itot)};
        return str;
    }



    public static void setPage(Document document, String[] emp, TimeReport[] weektime, String[] tot, TimeReport[] weektime2, String[] tot2,String ImgPath) throws BadElementException,DocumentException
    {
        String[] headers = {"Employee ID","Employee Name", "Pay Period Date"};
        String[] values = {emp[1],emp[0],emp[2]};

        Anchor anchor = new Anchor("BI WEEKLY DOCUMENT");
        anchor.setName("BI WEEKLY DOCUMENT");

        Chapter catPart = new Chapter(new Paragraph(anchor), 1);

        Paragraph subPara = new Paragraph("Details");
        Section subCatPart = catPart.addSection(subPara);

        subCatPart.add(Chunk.NEWLINE);
        subCatPart.add(Chunk.NEWLINE);

        subCatPart.add(new Paragraph(""));
        createTable(subCatPart,headers,values, 3 );

        subCatPart.add(Chunk.NEWLINE);
        subCatPart.add(Chunk.NEWLINE);

        String[] h2 = {"Pay Group Description","Bi-weekly Student"};
        String[] v2 = {};

        createTable(subCatPart,h2,v2,2);

        String[] h3 = {"Work Department Name/Mail Code","Computing, College of/0280"};
        String[] v3 = {};
        createTable(subCatPart,h3,v3,2);

        String[] h4 = {"Home Department Name/Mail Code","Computing, College of/0280"};
        String[] v4 = {};
        createTable(subCatPart,h4,v4,2);


        subCatPart.add(Chunk.NEWLINE);

        Paragraph subPara2 = new Paragraph("Week 1");
        Section subCatPart2 = catPart.addSection(subPara2);
        subCatPart2.add(new Paragraph(""));


        subCatPart2.add(Chunk.NEWLINE);


        String[] h5 = {"TIME REPORT"};
        String[] v5 = {};
        createTable(subCatPart2,h5,v5,1);


        // subCatPart.add(Chunk.NEWLINE);
        String[] h6 = {"BY DAY","Reg","Hol","Vac","Sick","Other Hours","Other Code","Total Hours"};
        String[] v6 = {};
        createTable(subCatPart2,h6,v6,8);

        String[] weeks = {"Thursday","Friday","Saturday","Sunday","Monday","Tuesday","Wednesday"};
        String[] v7 = {};
        for(int i = 0; i<7;i++) {

            String[] h7 = {weeks[i], weektime[i].reg, weektime[i].hol,
                    weektime[i].vac, weektime[i].sic, weektime[i].oh, weektime[i].oc, weektime[i].tot};
            createTable(subCatPart2,h7,v7,8);
        }
        //String[] h8 = {"Total Time Report","","","","","","",""};
        String[] h8 = tot;
        String[] v8 = {};
        createTable(subCatPart2,h8,v8,8);

        subCatPart2.add(Chunk.NEWLINE);


        String[] h9 = {"TIME DISTRIBUTION"};
        String[] v9 = {};
        createTable(subCatPart2,h9,v9,1);

        String[] h10 = {"BY PROJECT","Reg","Hol","Vac","Sick","Other Hours","Other Code","Total Hours"};
        String[] v10 = {};
        createTable(subCatPart2,h6,v6,8);

        String[] pjts = {"2301TUEXP"};
        String[] v11 = {};
        for(int i = 0; i<pjts.length;i++) {

            String[] h11 = {pjts[i], weektime[7].reg, weektime[7].hol,
                    weektime[7].vac, weektime[7].sic, weektime[7].oh, weektime[7].oc, weektime[7].tot};
            createTable(subCatPart2,h11,v11,8);
        }
        String[] h12 = {"Total Time Distribution", weektime[7].reg, weektime[7].hol,
                weektime[7].vac, weektime[7].sic, weektime[7].oh, weektime[7].oc, weektime[7].tot};
        String[] v12 = {};
        createTable(subCatPart2,h12,v12,8);

        subCatPart2.add(Chunk.NEWLINE);
        subCatPart2.add(Chunk.NEWLINE);
        subCatPart2.add(Chunk.NEWLINE);
        subCatPart2.add(Chunk.NEWLINE);
        subCatPart2.add(Chunk.NEWLINE);
        subCatPart2.add(Chunk.NEWLINE);

        //Week 2

        Paragraph subPara3 = new Paragraph("Week 2");
        Section subCatPart3 = catPart.addSection(subPara3);
        subCatPart3.add(new Paragraph(""));

        subCatPart3.add(Chunk.NEWLINE);

        String[] h13 = {"TIME REPORT"};
        String[] v13 = {};
        createTable(subCatPart3,h13,v13,1);





        // subCatPart.add(Chunk.NEWLINE);
        String[] h14 = {"BY PROJECT","Reg","Hol","Vac","Sick","Other Hours","Other Code","Total Hours"};
        String[] v14 = {};
        createTable(subCatPart3,h14,v14,8);

        String[] weeks2 = {"Thursday","Friday","Saturday","Sunday","Monday","Tuesday","Wednesday"};
        String[] v15 = {};
        for(int i = 0; i<7;i++) {

            String[] h15 = {weeks[i], weektime2[i].reg, weektime2[i].hol,
                    weektime2[i].vac, weektime2[i].sic, weektime2[i].oh, weektime2[i].oc, weektime2[i].tot};
            createTable(subCatPart3,h15,v15,8);
        }
        // String[] h16 = {"Total Time Report","","","","","","",""};
        String[] h16 =tot2;
        String[] v16 = {};
        createTable(subCatPart3,h16,v16,8);

        subCatPart3.add(Chunk.NEWLINE);


        String[] h17 = {"TIME DISTRIBUTION"};
        String[] v17 = {};
        createTable(subCatPart3,h17,v17,1);

        String[] h18 = {"BY PROJECT","Reg","Hol","Vac","Sick","Other Hours","Other Code","Total Hours"};
        String[] v18 = {};
        createTable(subCatPart3,h18,v18,8);

        String[] pjts2 = {"2301TUEXP"};
        String[] v19 = {};
        for(int i = 0; i<pjts2.length;i++) {

            String[] h19 = {pjts2[i], weektime2[7].reg, weektime2[7].hol,
                    weektime2[7].vac, weektime2[7].sic, weektime2[7].oh, weektime2[7].oc, weektime2[7].tot};
            createTable(subCatPart3,h19,v19,8);
        }
        String[] h20 = {"Total Time Distribution", weektime2[7].reg, weektime2[7].hol,
                weektime2[7].vac, weektime2[7].sic, weektime2[7].oh, weektime2[7].oc, weektime2[7].tot};
        String[] v20 = {};
        createTable(subCatPart3,h20,v20,8);

        subCatPart3.add(Chunk.NEWLINE);
        subCatPart3.add(Chunk.NEWLINE);


        //Week 2

        Paragraph subPara4 = new Paragraph("Signature");
        Section subCatPart4 = catPart.addSection(subPara4);
        subCatPart4.add(new Paragraph("I do hereby certify that the hours shown on the above time report are true and correct to the best of my knowledge and belief."));

        subCatPart4.add(Chunk.NEWLINE);
        subCatPart4.add(Chunk.NEWLINE);

        try
        {
            Image imgsig = Image.getInstance(ImgPath);
            imgsig.scaleAbsolute(80, 30);
            //catPart.add(imgsig);
            subCatPart4.add(new Chunk(imgsig,0,0,true));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }




        subCatPart4.add(new Paragraph("Employee signature"));
        subCatPart4.add(new Paragraph(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())));

        subCatPart4.add(Chunk.NEWLINE);
        subCatPart4.add(Chunk.NEWLINE);


        subCatPart4.add(new Paragraph("Departmental Approval/Date"));




//        String[] h25 = {"   ","    "};
//        String[] v25 = {"Employee's Signature/Date","Departmental Approval/Date"};
//        createTable(subCatPart4,h25,v25,2);

        subCatPart4.add(Chunk.NEWLINE);
        subCatPart4.add(Chunk.NEWLINE);


        String[] h21 = {"Other Hours","Codes"};
        String[] v22 = {};
        String[] v21 = {"Campus Closed Day","CCD"};
        String[] h22 = {"Call Back Overtime","CLL"};
        String[] h23 = {"Jury Duty","JRY"};
        String[] h24 = {"Miltary Duty","MIL"};

        createTable(subCatPart4,h21,v21,2);
        createTable(subCatPart4,h22,v22,2);
        createTable(subCatPart4,h23,v22,2);
        createTable(subCatPart4,h24,v22,2);


        document.add(catPart);
    }

    public static  void createTable(Section sub, String[] values,String[] headers, int cols) throws BadElementException
    {



        PdfPTable pt = new PdfPTable(cols);

        if(headers.length > 0) {
            for (int i = 0; i < values.length; i++) {
                PdfPCell pc = new PdfPCell(new Phrase(headers[i].toString()));
                pc.setHorizontalAlignment(Element.ALIGN_CENTER);
                pc.setNoWrap(false);
                pt.addCell(pc);
            }
            pt.setHeaderRows(1);
        }
        if(values.length >0)
        {
            for(int i = 0; i < values.length;i++) {
                pt.addCell(values[i]);
            }
        }
        sub.add(pt);
    }


//    public class DrawingView extends View{
//
//
//        public int width=0;
//        public int height = 0;
//        private Bitmap mBitmap;
//        private Canvas mCanvas;
//        private Path mPath;
//        private Paint mBitmapPaint;
//        Context context;
//        private Paint circlePaint;
//        private Path circlePath;
//
//
//        public DrawingView(Context c)
//        {
//            super(c);
//            context = c;
//            mPath = new Path();
//            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
//            circlePaint = new Paint();
//            circlePath = new Path();
//            circlePaint.setAntiAlias(true);
//            circlePaint.setColor(Color.BLACK);
//            circlePaint.setStyle(Paint.Style.STROKE);
//            circlePaint.setStrokeJoin(Paint.Join.MITER);
//            circlePaint.setStrokeWidth(2f);
//        }
//
//        @Override
//        protected void onSizeChanged(int w, int h, int oldw, int oldh)
//        {
//            super.onSizeChanged(w,h,oldw,oldh);
//            mBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
//            mCanvas = new Canvas(mBitmap);
//        }
//
//
//        @Override
//        protected void onDraw(Canvas canvas)
//        {
//            super.onDraw(canvas);
//            canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
//            canvas.drawPath(mPath,mPaint);
//            canvas.drawPath(circlePath,circlePaint);
//        }
//
//        private  float mX,mY;
//        private static final float TOUCH_TOLERANCE= 4;
//
//        private void touch_start(float x, float y)
//        {
//            mPath.reset();
//            mPath.moveTo(x,y);
//            mX=x;
//            mY=y;
//        }
//
//        private void touch_move(float x, float y)
//        {
//            float dx =Math.abs(x-mX);
//            float dy = Math.abs(y-mY);
//
//            if( dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
//            {
//                mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
//                mX = x;
//                mY = y;
//
//                circlePath.reset();
//                circlePath.addCircle(mX,mY,30,Path.Direction.CW);
//            }
//        }
//
//        private void touch_up()
//        {
//            mPath.lineTo(mX,mY);
//            circlePath.reset();
//
//            mCanvas.drawPath(mPath,mPaint);
//            mPath.reset();
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent event)
//        {
//            float x = getX();
//            float y = getY();
//
//            switch (event.getAction()){
//
//                case MotionEvent.ACTION_DOWN:
//                    touch_start(x,y);
//                    invalidate();
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    touch_move(x,y);
//                    invalidate();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    touch_up();
//                    invalidate();
//                    break;
//            }
//
//            return true;
//        }
//
//
//
//
//
//    } //end of Drawing view
//
//    private void OnCapture()
//    {
//        View v = lay;
//        v.setDrawingCacheEnabled(true);
//        bmp = Bitmap.createBitmap(v.getDrawingCache());
//        v.setDrawingCacheEnabled(false);
//        try{
//            file = new File(Environment.getExternalStorageDirectory().toString(),"SCREEN"+System.currentTimeMillis()+".png");
//            Log.e("here","-----------"+file);
//            FileOutputStream fos = new FileOutputStream(file);
//            bmp.compress(Bitmap.CompressFormat.PNG,100, fos);
//            fos.flush();
//            fos.close();
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//    }
}
