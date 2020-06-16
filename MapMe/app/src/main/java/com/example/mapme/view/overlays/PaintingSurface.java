package com.example.mapme.view.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.mapme.view.AddObjectActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for AddPolylineActivity and AddPolygonActivity.
 */
public class PaintingSurface extends View {

    public enum Mode {
        Polyline,
        Polygon
    }

    private AddObjectActivity currentActivity;
    private PaintingSurface.Mode drawingMode = PaintingSurface.Mode.Polyline;
    private Canvas canvas;
    private final Path path;
    private MapView mapView;
    private final List<Point> points = new ArrayList<>();
    private final Paint paint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    transient Polygon lastPolygon;

    /**
     * Sets drawing mode.
     *
     * @param mode
     */
    public void setMode(PaintingSurface.Mode mode) {
        this.drawingMode = mode;
    }

    /**
     * Constructor.
     *
     * @param context
     * @param attrs
     */
    public PaintingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(0xFFFF0000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(12);
    }

    /**
     * Creates new canvas.
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        this.canvas = new Canvas(bitmap);
    }

    /**
     * Draws new path.
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    /**
     * Initializes painting surface.
     *
     * @param activity
     * @param mapView
     */
    public void init(AddObjectActivity activity, MapView mapView) {
        currentActivity = activity;
        this.mapView = mapView;
    }

    /**
     * Start painting.
     *
     * @param x
     * @param y
     */
    private void touch_start(float x, float y) {
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * While painting.
     *
     * @param x
     * @param y
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    /**
     * Save object when finish painting.
     */
    private void touch_up() {
        path.lineTo(mX, mY);
        canvas.drawPath(path, paint);
        path.reset();
        if (mapView != null) {
            Projection projection = mapView.getProjection();
            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            final Point unrotatedPoint = new Point();
            for (int i = 0; i < points.size(); i++) {
                projection.unrotateAndScalePoint(points.get(i).x, points.get(i).y, unrotatedPoint);
                GeoPoint iGeoPoint = (GeoPoint) projection.fromPixels(unrotatedPoint.x, unrotatedPoint.y);
                geoPoints.add(iGeoPoint);
            }
            if (geoPoints.size() > 2) {
                switch (drawingMode) {
                    case Polyline:
                        final int color = Color.argb(100, 100, 100, 100);
                        final Polyline line = new Polyline(mapView);
                        line.getOutlinePaint().setColor(color);
                        line.setTitle("Polyline");
                        line.setPoints(geoPoints);
                        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
                        line.setId(currentActivity.saveToDatabase(line));
                        currentActivity.showInfoAddReferenceOrEditObject(mapView, line.getId(), line);
                        line.setOnClickListener(new Polyline.OnClickListener() {
                            @Override
                            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                                currentActivity.showInfoEditObject(mapView, line.getId());
                                return false;
                            }
                        });
                        mapView.getOverlayManager().add(line);
                        lastPolygon = null;
                        break;
                    case Polygon:
                        Polygon polygon = new Polygon(mapView);
                        polygon.getFillPaint().setColor(Color.argb(75, 255, 0, 0));
                        polygon.setPoints(geoPoints);
                        polygon.setTitle("Polygon");
                        polygon.setId(currentActivity.saveToDatabase(polygon));
                        currentActivity.showInfoAddReferenceOrEditObject(mapView, polygon.getId(), polygon);
                        polygon.setOnClickListener(new Polygon.OnClickListener() {
                            @Override
                            public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                                lastPolygon = polygon;
                                currentActivity.showInfoEditObject(mapView, polygon.getId());
                                return false;
                            }
                        });
                        mapView.getOverlayManager().add(polygon);
                        lastPolygon = polygon;
                        break;
                }
                mapView.invalidate();
            }
        }
        points.clear();
    }

    /**
     * Touch event.
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        points.add(new Point((int) x, (int) y));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    /**
     * Destroy mapView and polygon.
     */
    public void destroy() {
        mapView = null;
        this.lastPolygon = null;
    }

}

