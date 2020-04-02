package com.example.mapme.backend;

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
import android.widget.Toast;

import com.example.mapme.activities.AddObjectActivity;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.milestones.MilestoneBitmapDisplayer;
import org.osmdroid.views.overlay.milestones.MilestoneManager;
import org.osmdroid.views.overlay.milestones.MilestonePathDisplayer;
import org.osmdroid.views.overlay.milestones.MilestonePixelDistanceLister;

import java.util.ArrayList;
import java.util.List;

public class PaintingSurface extends View {

    private AddObjectActivity currentActivity;

    public void setMode(Mode mode) {
        this.drawingMode = mode;
    }

    private Mode drawingMode = Mode.Polyline;

    public enum Mode {
        Polyline,
        Polygon,
        PolygonHole,
        PolylineAsPath
    }

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


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(bitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    public void init(AddObjectActivity activity, MapView mapView) {
        currentActivity = activity;
        this.mapView = mapView;
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
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
                //only plot a line unless there's at least one item
                switch (drawingMode) {
                    case Polyline:
                    case PolylineAsPath:
                        final boolean asPath = drawingMode == Mode.PolylineAsPath;
                        final int color = Color.argb(100, 100, 100, 100);
                        final Polyline line = new Polyline(mapView);
                        // line.usePath(true);
                        line.setInfoWindow(
                                new BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView));
                        line.getOutlinePaint().setColor(color);
                        line.setTitle("New polyline" + (asPath ? " as Path" : ""));
                        line.setPoints(geoPoints);
                        line.showInfoWindow();
                        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
                        currentActivity.showPopupWindow(mapView);
                        //example below

                        line.setOnClickListener(new Polyline.OnClickListener() {
                            @Override
                            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                                Toast.makeText(mapView.getContext(), "polyline with " + polyline.getPoints().size() + "pts was tapped", Toast.LENGTH_LONG).show();
                                currentActivity.showPopupWindow(mapView);
                                return false;
                            }
                        });


                        if (withArrows) {
                            final Paint arrowPaint = new Paint();
                            arrowPaint.setColor(color);
                            arrowPaint.setStrokeWidth(10.0f);
                            arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                            arrowPaint.setAntiAlias(true);
                            final Path arrowPath = new Path(); // a simple arrow towards the right
                            arrowPath.moveTo(-10, -10);
                            arrowPath.lineTo(10, 0);
                            arrowPath.lineTo(-10, 10);
                            arrowPath.close();
                            final List<MilestoneManager> managers = new ArrayList<>();
                            managers.add(new MilestoneManager(
                                    new MilestonePixelDistanceLister(50, 50),
                                    new MilestonePathDisplayer(0, true, arrowPath, arrowPaint)
                            ));
                            line.setMilestoneManagers(managers);
                        }
                        line.setSubDescription(line.getBounds().toString());
                        mapView.getOverlayManager().add(line);
                        lastPolygon = null;
                        break;
                    case Polygon:
                        Polygon polygon = new Polygon(mapView);
                        polygon.setInfoWindow(
                                new BasicInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView));
                        polygon.getFillPaint().setColor(Color.argb(75, 255, 0, 0));
                        polygon.setPoints(geoPoints);
                        polygon.setTitle("New polygon");
                        polygon.showInfoWindow();
                        currentActivity.showPopupWindow(mapView);
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
                                polygon.onClickDefault(polygon, mapView, eventPos);
                                currentActivity.showPopupWindow(mapView);
                                return false;
                            }
                        });
                        //polygon.setSubDescription(BoundingBox.fromGeoPoints(polygon.getPoints()).toString());
                        mapView.getOverlayManager().add(polygon);
                        lastPolygon = polygon;
                        break;
                    case PolygonHole:
                        if (lastPolygon != null) {
                            List<List<GeoPoint>> holes = new ArrayList<>();
                            holes.add(geoPoints);
                            lastPolygon.setHoles(holes);
                        }
                        break;
                }

                mapView.invalidate();
            }
        }

        pts.clear();

    }

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

    public void destroy() {
        mapView = null;
        this.lastPolygon = null;
    }


}

