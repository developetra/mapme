package com.example.mapme.view.overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.mapme.model.GeoJsonHelper;
import com.example.mapme.view.AddObjectActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.milestones.MilestoneBitmapDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import org.osmdroid.views.overlay.milestones.MilestonePixelDistanceLister;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for AddPolylineActivity and AddPolygonActivity.
 */
public class PaintingSurface extends View {

    public enum Mode {
        Polyline,
        Polygon,
        PolylineAsPath
    }

    private AddObjectActivity currentActivity;
    private Mode drawingMode = Mode.Polyline;
    protected boolean withArrows = false;
    private Canvas mCanvas;
    private Path mPath;
    private MapView mapView;
    private List<Point> pts = new ArrayList<>();
    private final Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private GeoJsonHelper geoJsonHelper = new GeoJsonHelper();
    transient Polygon lastPolygon = null;

    /**
     * Sets drawing mode.
     *
     * @param mode
     */
    public void setMode(Mode mode) {
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
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
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
        this.mCanvas = new Canvas(bitmap);
    }

    /**
     * Draws new path.
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
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
        mPath.reset();
        mPath.moveTo(x, y);
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
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    /**
     * Finish painting.
     */
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit path to offscreen
        mCanvas.drawPath(mPath, mPaint);
        // reset mPath to not double draw
        mPath.reset();
        if (mapView != null) {
            Projection projection = mapView.getProjection();
            ArrayList<GeoPoint> geoPoints = new ArrayList<>();
            final Point unrotatedPoint = new Point();
            for (int i = 0; i < pts.size(); i++) {
                projection.unrotateAndScalePoint(pts.get(i).x, pts.get(i).y, unrotatedPoint);
                GeoPoint iGeoPoint = (GeoPoint) projection.fromPixels(unrotatedPoint.x, unrotatedPoint.y);
                geoPoints.add(iGeoPoint);
            }
            if (geoPoints.size() > 2) {
                // only plot a line unless there is at least one item
                switch (drawingMode) {
                    case Polyline:
                    case PolylineAsPath:
                        final boolean asPath = drawingMode == Mode.PolylineAsPath;
                        final int color = Color.argb(100, 100, 100, 100);
                        final Polyline line = new Polyline(mapView);
                        line.getOutlinePaint().setColor(color);
                        line.setTitle("Polyline" + (asPath ? " as Path" : ""));
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
                        line.setSubDescription(line.getBounds().toString());
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
                        if (withArrows) {
                            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), org.osmdroid.library.R.drawable.round_navigation_white_48);
                            final List<MilestoneManager> managers = new ArrayList<>();
                            managers.add(new MilestoneManager(
                                    new MilestonePixelDistanceLister(20, 200),
                                    new MilestoneBitmapDisplayer(90, true, bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2)
                            ));
                            polygon.setMilestoneManagers(managers);
                        }
                        polygon.setOnClickListener(new Polygon.OnClickListener() {
                            @Override
                            public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                                lastPolygon = polygon;
                                currentActivity.showInfoEditObject(mapView, polygon.getId());
                                return false;
                            }
                        });
                        //polygon.setSubDescription(BoundingBox.fromGeoPoints(polygon.getPoints()).toString());
                        mapView.getOverlayManager().add(polygon);
                        lastPolygon = polygon;
                        break;
                }

                mapView.invalidate();
            }
        }

        pts.clear();

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
        pts.add(new Point((int) x, (int) y));
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

