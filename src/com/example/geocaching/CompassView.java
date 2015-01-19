package com.example.geocaching;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

/*
 * This class is solely used to draw a compass for the user using the 
 * values calculated in Compass class
 */
public class CompassView extends View {

	private float north;
	private float bearing;
	Compass context;

	Paint paint;
	Paint paintNorth;
	Paint paintCurrent;
	Paint paintCache;

	/*
	 * Initializes different paints to draw the Compass
	 */
	public CompassView(Compass context) {
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub

		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.STROKE);
		paint.setTextSize(50);
		paint.setStrokeWidth(3);
		paint.setAntiAlias(true);

		paintNorth = new Paint();
		paintNorth.setColor(Color.GREEN);
		paintNorth.setStyle(Style.FILL_AND_STROKE);
		paintNorth.setTextSize(16);
		paintNorth.setStrokeWidth(3);
		paintNorth.setAntiAlias(true);

		paintCurrent = new Paint();
		paintCurrent.setColor(Color.BLUE);
		paintCurrent.setStyle(Style.FILL_AND_STROKE);
		paintCurrent.setTextSize(16);
		paintCurrent.setStrokeWidth(3);
		paintCurrent.setAntiAlias(true);

		paintCache = new Paint();
		paintCache.setColor(Color.GREEN);
		paintCache.setStyle(Style.FILL_AND_STROKE);
		paintCache.setTextSize(16);
		paintCache.setStrokeWidth(3);
		paintCache.setAntiAlias(true);
	}
	
	
	/*
	 * The North/South triangles are drawn using this method
	 */
	public void drawTriangle(Canvas canvas, Point p1, Point p2, Point p3, Point p4) {
		Paint paint = new Paint();
		
		paint.setAlpha(128);
		paint.setStrokeWidth(4);
		paint.setColor(android.graphics.Color.RED);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);

		Path path = new Path();
		path.setFillType(FillType.EVEN_ODD);
		path.moveTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);
		path.lineTo(p1.x, p1.y);
		path.close();
		
		Path path2 = new Path();
		path2.setFillType(FillType.EVEN_ODD);
		path2.moveTo(p2.x, p2.y);
		path2.lineTo(p4.x, p4.y);
		path2.lineTo(p1.x, p1.y);
		path2.close();

		canvas.drawPath(path, paint);
		
		paint.setColor(Color.BLUE);
		
		canvas.drawPath(path2, paint);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 * 
	 * Overwritten method to draw on the screen to create the compass
	 */

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float x = this.getWidth() / 2;
		float y = this.getHeight() / 2;
		float radius = (float) (this.getWidth() / 2.5);

		north = context.heading;
		bearing = context.bearingToDest;
		float radNorth = (float) ((float) (north * 0.0174532925) - Math.PI / 2);
		float radBearing = (float) ((float) (bearing * 0.0174532925) - Math.PI / 2);
		System.out.println("Bearing: " + bearing);
		this.setBackgroundColor(getResources().getColor(R.color.background));

		float compassRadNorth = radNorth;
		float compassRadBearing = radBearing;
		

		if (compassRadNorth < 0)
		{
			compassRadNorth = (float) (2 * Math.PI + compassRadNorth);
		}
		if (compassRadBearing < 0)
		{
			compassRadBearing = (float) (2 * Math.PI + compassRadBearing);
		}
		
		float north = (float) ((compassRadNorth) * radius);
		//float cache = (float) ((compassRadBearing) * radius);

		Path path = new Path();
		path.addCircle(x, y, radius, Path.Direction.CW);

		float circumference = (float) (2 * radius * Math.PI);
		float minusDegrees = (float) (3.8 * (circumference / 360));
		if (compassRadNorth < 0)
			canvas.drawTextOnPath("N", path, circumference + north
					- minusDegrees, -5, paint);
		else
			canvas.drawTextOnPath("N", path, north % circumference
					- minusDegrees, -5, paint);

		canvas.drawTextOnPath("E", path, (float) ((north + circumference / 4)
				% circumference - minusDegrees / 1.6), -5, paint);
		canvas.drawTextOnPath("S", path, (float) ((north + 2 * circumference / 4)
				% circumference - minusDegrees / 1.6), -5, paint);
		canvas.drawTextOnPath("W", path, (float) ((north + 3 * circumference / 4)
				% circumference - minusDegrees * 1.15), -5, paint);

		canvas.drawCircle(x, y, radius, paint);
		canvas.drawCircle(x, y, radius / 3, paint);
		canvas.drawCircle(x, y, 2 * radius / 3, paint);
		
		float divider = 60 - this.context.accuracy;
		if (divider == 60)
			divider = 40;
		
		float divider2 = (float) (5 * .1 * this.context.accuracy);
		if (divider2 == 0)
			divider2 = (float) 10;

		// draw triangle
		Point p1 = new Point(
				(int) ((this.getWidth() / 2 + ((float) (this.getWidth() / divider) * Math
						.cos(radNorth + Math.PI / 2)))),
				(int) ((this.getHeight() / 2 + ((float) (this.getWidth() / divider) * Math
						.sin(radNorth + Math.PI / 2)))));

		Point p2 = new Point(
				(int) ((this.getWidth() / 2 + ((float) (this.getWidth() / divider) * Math
						.cos(radNorth + 3 * Math.PI / 2)))),
				(int) ((this.getHeight() / 2 + ((float) (this.getWidth() / divider) * Math
						.sin(radNorth + 3 * Math.PI / 2)))));

		Point p3 = new Point(
				(int) ((this.getWidth() / 2 + ((float) (this.getWidth() / divider2) * Math
						.cos(radNorth)))),
				(int) ((this.getHeight() / 2 + ((float) (this.getWidth() / divider2) * Math
						.sin(radNorth)))));

		Point p4 = new Point(
				(int) (this.getWidth() / 2 + ((float) (this.getWidth() / divider2) * Math
						.cos(radNorth + Math.PI))),
				(int) (this.getHeight() / 2 + ((float) (this.getWidth() / divider2) * Math
						.sin(radNorth + Math.PI))));

		//East to West
		canvas.drawLine(
				(float) (this.getWidth() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.cos(radNorth + Math.PI / 2))),
				(float) (this.getHeight() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.sin(radNorth + Math.PI / 2))),
				(float) (this.getWidth() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.cos(radNorth + 3 * Math.PI / 2))),
				(float) (this.getHeight() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.sin(radNorth + 3 * Math.PI / 2))), paint);
		
		drawTriangle(canvas, p1, p2, p3, p4);


		paintCache.setColor(Color.GREEN);
		// draw cache
		canvas.drawCircle((float) (this.getWidth() / 2 + ((float) (this
				.getWidth() / 2.5) * Math.cos(radBearing))), (float) (this
				.getHeight() / 2 + ((float) (this.getWidth() / 2.5) * Math
				.sin(radBearing))), 10, paintCache);


		paintCache.setColor(Color.RED);
		// line to north
		canvas.drawLine(
				this.getWidth() / 2,
				this.getHeight() / 2,
				(float) (this.getWidth() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.cos(radNorth))),
				(float) (this.getHeight() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.sin(radNorth))), paintCache);

		paintCache.setColor(Color.BLUE);
		// line to south
		canvas.drawLine(
				this.getWidth() / 2,
				this.getHeight() / 2,
				(float) (this.getWidth() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.cos(radNorth + Math.PI))),
				(float) (this.getHeight() / 2 + ((float) (this.getWidth() / 2.5) * Math
						.sin(radNorth + Math.PI))), paintCache);

	}
	
	/*
	 * Calculates the density-pixels from an amount of pixels.
	 * Useful when dealing with multiple platfroms with different
	 * screen sizes
	 */
	double getDPFromPixels(double pixels) {
	    DisplayMetrics metrics = new DisplayMetrics();
	    context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
	    switch(metrics.densityDpi){
	     case DisplayMetrics.DENSITY_LOW:
	                pixels = pixels * 0.75;
	                break;
	     case DisplayMetrics.DENSITY_MEDIUM:
	                 //pixels = pixels * 1;
	                 break;
	     case DisplayMetrics.DENSITY_HIGH:
	                 pixels = pixels * 1.5;
	                 break;
	    }
	    return pixels;
	}

}
