package com.scconsulting.signature;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableRow.LayoutParams;

public class Signature extends Activity {
	
	private final static String TAG = "Signature";
	private int result;
	
	public static final int REQUEST_CODE = 1001;
	public static final String EXTRA_ID = "id";
	
	private int screenHeight;
	private int screenWidth;
	private Canvas canvasNew;

	static final int ShapeDotSmall = 1;
	static final int ShapeDotMedium = 2;
	static final int ShapeDotLarge = 3;

	private static final int ShapeFreehand = 5;
	private static final int ShapeErase = 6;
	private int shapeObject = ShapeFreehand;

	private int setButtonId = 1000;
	private int i = 0;
	
	private Paint mPaint;

	private float startX;
	private float startY;
	private float stopX;
	private float stopY;
	
	public static Panel panel;
	
	private Bitmap bMap;
	private Bitmap bMapNew;
	
	private LinearLayout llMain;
	private LinearLayout ll;
	private LinearLayout ll5;
	private Rect rect;
	private int top = 0;
	private int left = 10;
	private int right = 10;
	private int howHigh = 200;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		/*
         * Lock screen orientation.
         * This will prevent orientation change resetting screen and losing markup already done.
         */
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else {
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        DisplayMetrics dm = getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
        mPaint = new Paint();
        initializePaint();

        if (mPaint.getStrokeWidth() < 3) {
        	mPaint.setStrokeWidth(3);
        }
        
        bMap = null;
    }
    
    
    public void onPause(){
    	super.onPause();
    	
    	panel.surfaceDestroyed(panel.getHolder());
    	
   		switch (result) {
   		case RESULT_CANCELED:
   			break;
   		case RESULT_OK:
   			break;
   		default:
   			break;
        }
    }
    
    public void onStop(){
    	super.onStop();
    	panel.surfaceDestroyed(panel.getHolder());
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	panel.surfaceDestroyed(panel.getHolder());
    }
    
   public void onResume(){
	   super.onResume();
	   
	   result = RESULT_OK;
    }
    
    public void onStart(){
    	super.onStart();
    	
    	panel = new Panel(this);
    	
        ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        layoutParams.setMargins(0, 4, 0, 0);
        ll.setId(setButtonId*3);
        
        LinearLayout ll1 = new LinearLayout(getApplicationContext());
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsLL1 = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParamsLL1.setMargins(left, 0, 0, 0);
        layoutParamsLL1.width = 0;
        layoutParamsLL1.weight = 2;
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        
        //Declare a new view (here a button)
        Button bb = new Button(getApplicationContext());
        bb.setText("Save");
		bb.setId(setButtonId*1 + i);
		bb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
		bb.setTextColor(Color.rgb(153, 0, 255));
				
		/*
		 * Create an OnClickListener() for the new button
		 */
		bb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				i = view.getId() - (setButtonId*1);
				
				if ( saveBitmap() ) {
				    //Intent intent = new Intent();
				    //setResult(RESULT_OK, intent);
				    //finish();
				}
				
			}
		});
		bb.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		
        
		LinearLayout llBtn = new LinearLayout(getApplicationContext());
        llBtn.setOrientation(LinearLayout.HORIZONTAL);
        llBtn.setBackgroundColor(Color.WHITE);
		llBtn.addView(bb, btnParams);
		
		//Add the button to the linear layout
		ll1.addView(llBtn, layoutParams);

        i++;
        
        LinearLayout ll2 = new LinearLayout(getApplicationContext());
        ll2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsLL2 = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParamsLL2.setMargins(0, 0, 0, 0);
        layoutParamsLL2.width = 0;
        layoutParamsLL2.weight = 2;

        bb = new Button(getApplicationContext());
        bb.setText(getString(R.string.button_delete));
		bb.setId(setButtonId*1 + i);
		bb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
		bb.setTextColor(Color.rgb(153, 0, 255));
		/*
		 * Create an OnClickListener() for the new button
		 */
		bb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				i = view.getId() - (setButtonId*1);
				ClearBitmap();
			}
		});
		bb.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		

		llBtn = new LinearLayout(getApplicationContext());
        llBtn.setOrientation(LinearLayout.HORIZONTAL);
        llBtn.setBackgroundColor(Color.WHITE);
		llBtn.addView(bb, btnParams);
		
		//Add the button to the linear layout
		ll2.addView(llBtn, layoutParams);
        
        i++;

        LinearLayout ll3 = new LinearLayout(getApplicationContext());
        ll3.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsLL3 = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParamsLL3.setMargins(0, 0, right, 0);
        layoutParamsLL3.width = 0;
        layoutParamsLL3.weight = 2;

        bb = new Button(getApplicationContext());
        bb.setText(getString(R.string.button_clear));
		bb.setId(setButtonId*1 + i);
		bb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
		bb.setTextColor(Color.rgb(153, 0, 255));
		/*
		 * Create an OnClickListener() for the new button
		 */
		bb.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				i = view.getId() - (setButtonId*1);
				panel.clear();
			}
		});
		bb.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		
		llBtn = new LinearLayout(getApplicationContext());
        llBtn.setOrientation(LinearLayout.HORIZONTAL);
        llBtn.setBackgroundColor(Color.WHITE);
		llBtn.addView(bb, btnParams);
		
		//Add the button to the linear layout
		ll3.addView(llBtn, layoutParams);

        ll.addView(ll1, layoutParamsLL1);
        ll.addView(ll2, layoutParamsLL2);
        ll.addView(ll3, layoutParamsLL3);
        
        // Add LinearLayout with bottom padding and blue background.
        // The bottom padding forms a blue line.
        ll5 = new LinearLayout(getApplicationContext());
        ll5.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParamsLL5 = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ll5.setBackgroundColor(0xFF0000FF);
        ll5.setPadding(0, 0, 0, 2);
        
    	llMain = new LinearLayout(getApplicationContext());

        llMain.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(
             LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //llMain.setBackgroundColor(0xFF00FFFF); // Teal
        llMain.setBackgroundColor(0xFFFFFFFF);
        
        // Add the linear layout containing the button(s) to the main linear layout
        llMain.addView(ll, layoutParams);
        llMain.addView(ll5, layoutParamsLL5);
        
        panel.setId(setButtonId*2);
        // Add the linear layout containing the Panel to the main linear layout
        llMain.addView(panel, layoutParams);
        
    	setContentView(llMain); // Set view to main Linear Layout, NOT the custom Panel
    	
        
		//***********
    	// Need to set up the new bitmap and canvas here for onDraw().
    	// Get real dimensions in surfaceCreated().
    	bMapNew = Bitmap.createBitmap(screenWidth, howHigh + 30,Bitmap.Config.ARGB_8888);
		canvasNew = new Canvas(bMapNew);
    }
    
    private void initializePaint() {

    	mPaint.setDither(true);
	    mPaint.setColor(Color.BLACK);
	    mPaint.setStyle(Paint.Style.STROKE);
    	mPaint.setStrokeJoin(Paint.Join.ROUND);
    	mPaint.setStrokeCap(Paint.Cap.ROUND);
    	mPaint.setStrokeWidth(3);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menusig, menu);
    	return true;
   	}
    public boolean onOptionsItemSelected(MenuItem item){
    	
    	switch(item.getItemId()){

    	case R.id.itemFreehand:
    		shapeObject = ShapeFreehand;
    		if (mPaint.getStrokeWidth() == 12) {
    			/**
    			 * Previously set to Eraser.
    			 * Set to original defaults.
    			 */
    			mPaint.setStrokeWidth(3);
    			mPaint.setColor(Color.BLACK);
    		}
    		return true;
    	
    	case R.id.itemErase:
    		shapeObject = ShapeFreehand;
    		mPaint.setStrokeWidth(12);
    		mPaint.setColor(Color.WHITE);
    		return true;
     
    	case R.id.itemColorBlack:
    		mPaint.setColor(Color.BLACK);
    		return true;
    	
    	case R.id.itemColorWhite:
    		mPaint.setColor(Color.WHITE);
    		return true;
   	
    	case R.id.itemColorBlue:
    		mPaint.setColor(Color.BLUE);
    		return true;
    	
    	case R.id.itemColorRed:
    		mPaint.setColor(Color.RED);
    		return true;

    	case R.id.itemColorGreen:
    		mPaint.setColor(Color.GREEN);
    		return true;
    		
    	case R.id.itemDotSmall:
    		shapeObject = ShapeDotSmall;
    		return true;

    	case R.id.itemDotMedium:
    		shapeObject = ShapeDotMedium;
    		return true;
    	
    	case R.id.itemDotLarge:
    		shapeObject = ShapeDotLarge;
    		return true;
    		
    	case R.id.itemBrushWidth1:
    		mPaint.setStrokeWidth(1);
    		return true;

    	case R.id.itemBrushWidth2:
    		mPaint.setStrokeWidth(2);
    		return true;
    		
    	case R.id.itemBrushWidth3:
    		mPaint.setStrokeWidth(3);
    		return true;
    	
    	case R.id.itemBrushWidth4:
    		mPaint.setStrokeWidth(4);
    		return true;
    	
    	case R.id.itemBrushWidth5:
    		mPaint.setStrokeWidth(5);
    		return true;
    	
    	case R.id.itemBrushWidth6:
    		mPaint.setStrokeWidth(6);
    		return true;
    	
    	}
   
    	return false;
    } 

    public boolean saveBitmap() {
    	/*
    	 * Create bitmap from contents of rectangle,
    	 * inside the lines.
    	 */
    	bMap = Bitmap.createBitmap(bMapNew, rect.left+1, rect.top+1, rect.width()-2, rect.height()-2);

		final File photoFile = getPhotoFile(this);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(photoFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bMap.compress(CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
			fos.close();
			
			scanMedia(photoFile);	// Run media scan to refresh Gallery, and make this file visible
			Toast.makeText(Signature.this, "Signature stored", Toast.LENGTH_SHORT).show();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		
		return true;
    }
    
    private File getPhotoFile(Context context){

    	final File fPath = new File( Environment.getExternalStorageDirectory(), getString(R.string.app_name) );
        
       	if (fPath.mkdir()) {
       		Log.i(TAG, "Signature folder created");
       	}

        return new File(fPath, "signature.jpg");
    }
    
    public void ClearBitmap() {

    	AlertDialog.Builder builder = new AlertDialog.Builder(
    			Signature.this);
        builder.setCancelable(true);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this signature?");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Yes, Delete",
                new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
        		dialog.dismiss();
                
        		final File photoFile = getPhotoFile(getApplicationContext());
        		if (photoFile.exists()) {
        			photoFile.delete();
        			Toast.makeText(Signature.this, "Signature deleted", Toast.LENGTH_SHORT).show();
        		}
        		
        		//Intent intent = new Intent();
        		//setResult(RESULT_OK, intent);
        		//finish();
        		
        		return;
                        
        	}
        });
        builder.setNegativeButton("No, Cancel",
        		new DialogInterface.OnClickListener() {
        	@Override
            public void onClick(DialogInterface dialog, int which) {
     			dialog.dismiss();
                
       		    //Intent intent = new Intent();
       			//setResult(RESULT_OK, intent);
       			//finish();

   		    	return;
       			
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    	
    }

    class Panel extends SurfaceView implements SurfaceHolder.Callback {

    	public SurfThread _thread;
    	public Panel(Context context) {
    		super(context);
        	getHolder().addCallback(this);
            _thread = new SurfThread(getHolder(), this);
            
            setFocusable(true);
            setFocusableInTouchMode(true);
            setDrawingCacheEnabled(false);
            
        }
        
    	@Override
        public boolean onTouchEvent(MotionEvent event) {
        	synchronized (_thread.getSurfaceHolder()) {
	        		
        		switch (event.getAction()) {
        		case MotionEvent.ACTION_DOWN:
        			startX = event.getX();
        			startY = event.getY();
        				
        			break;
       		
        		case MotionEvent.ACTION_MOVE:
        			stopX = event.getX();
        			stopY = event.getY();

        			if (shapeObject == ShapeFreehand | shapeObject == ShapeErase) {
        				canvasNew.drawLine(startX, startY, stopX, stopY, mPaint);
        			}
        			startX = stopX;
        			startY = stopY;
        			break;
       		
        		case MotionEvent.ACTION_UP:
        			stopX = event.getX();
       				stopY = event.getY();
        				
       				switch (shapeObject) {
       				case ShapeDotSmall:
       					// Draw fill first, then circle.
       					mPaint.setAntiAlias(false);
       					mPaint.setStyle(Paint.Style.FILL);
       					canvasNew.drawCircle(startX, startY,2, mPaint);
    	        			
       					mPaint.setAntiAlias(true);
       					mPaint.setStyle(Paint.Style.STROKE);
       					canvasNew.drawCircle(startX, startY, 2, mPaint);
    	        			
       					shapeObject = ShapeFreehand;	// Just one dot, then reset to freehand.
       					break;
       				
       				case ShapeDotMedium:
       					// Draw fill first, then circle.
       					mPaint.setAntiAlias(false);
       					mPaint.setStyle(Paint.Style.FILL);
       					canvasNew.drawCircle(startX, startY, 8, mPaint);
    	        			
       					mPaint.setAntiAlias(true);
       					mPaint.setStyle(Paint.Style.STROKE);
       					canvasNew.drawCircle(startX, startY, 8, mPaint);
    	        			
       					shapeObject = ShapeFreehand;	// Reset to freehand drawing after 1 dot.
       				
       					break;
       			
       				case ShapeDotLarge:
       					// Draw fill first, then circle.
       					mPaint.setAntiAlias(false);
       					mPaint.setStyle(Paint.Style.FILL);
       					canvasNew.drawCircle(startX, startY, 16, mPaint);
       				
       					mPaint.setAntiAlias(true);
       					mPaint.setStyle(Paint.Style.STROKE);
       					canvasNew.drawCircle(startX, startY, 16, mPaint);
    	        			
       					shapeObject = ShapeFreehand;	// Reset to freehand drawing after 1 dot.
       					
       					break;
       			
       				case ShapeFreehand:
       					canvasNew.drawLine(startX, startY, stopX, stopY, mPaint);
       				
       					break;
       			
       				case ShapeErase:
       					canvasNew.drawLine(startX, startY, stopX, stopY, mPaint);
       				
       					break;
       			
       				default:
       					canvasNew.drawLine(startX, startY, stopX, stopY, mPaint);
       				
       					break;
           		    	
       				}
        		}
        	}
        	
        	return true;
    
        }
 
        @Override
        public void onDraw(Canvas canvas) {
        	
       		canvas.drawBitmap(bMapNew, 0, 0, null);

	    }
        
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // TODO Auto-generated method stub
        }
 
        public void surfaceCreated(SurfaceHolder holder) {
            _thread.setRunning(true);
            _thread.start();
            
            int panelHeight = panel.getHeight();
            int llHeight = ll.getHeight() + ll5.getHeight();
        	
        	bMapNew = Bitmap.createBitmap(screenWidth, panelHeight, Bitmap.Config.ARGB_8888);
    		canvasNew = new Canvas(bMapNew);
    		canvasNew.drawColor(0xFFFFFFFF);	// White
    		
    		drawBlueRect(llHeight);
    		
        }
        
        private void drawBlueRect(int height) {
        	/*
    		 * Draw a blue rectangle on the canvas.
    		 * The person signs the screen inside the rectangle.
    		 * Contents of rectangle saved later as a bitmap image. 
    		 */
    		int saveColor = mPaint.getColor();
    		float saveStroke = mPaint.getStrokeWidth();
    		mPaint.setColor(Color.BLUE);
    		mPaint.setStrokeWidth(1);
    		top = ((screenHeight - height)-howHigh)/2;
    		
    		rect = new Rect(left, top, screenWidth - left, top + howHigh-1);
    		canvasNew.drawRect(rect, mPaint);
    		
    		mPaint.setColor(saveColor);
    		mPaint.setStrokeWidth(saveStroke);
        }
        
        public void clear() {
        	canvasNew.drawColor(0xFFFFFFFF);	// White
        	drawBlueRect(ll.getHeight() + ll5.getHeight());
        }
        
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Tell thread to shut down & wait for it to finish
            boolean retry = true;
            _thread.setRunning(false);
            while (retry) {
                try {
                    _thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
        }
        
    }
 
    class SurfThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private Panel _panel;
        private boolean _run = false;
 
        public SurfThread(SurfaceHolder surfaceHolder, Panel panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }
 
        public void setRunning(boolean run) {
            _run = run;
        }
 
        public SurfaceHolder getSurfaceHolder() {
            return _surfaceHolder;
        }
 
        @SuppressLint("WrongCall")
		@Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
    
	private void scanMedia(File f) {
	    Uri uri = Uri.fromFile(f);
	    Intent scanFileIntent = new Intent(
	            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
	    sendBroadcast(scanFileIntent);
	}

}