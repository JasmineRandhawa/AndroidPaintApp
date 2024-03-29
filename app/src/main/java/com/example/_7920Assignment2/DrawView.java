package com.example._7920Assignment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/* Drawing triangle operations - draw , ontouch , color changes, shape changer */
public class DrawView extends View {

    private final int TOUCH_TOLERANCE = 4;
    private final List<PathData> pdList;
    private final Context context;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private final Paint mPaint;

    private List<PathPoint> pointList;
    private Path mPath;
    PathData prevPathData;

    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;
    private int longPressTimer = 2000;

    private boolean isFill = false;
    private boolean isLine = false;
    private boolean isTriangle = false;
    private boolean isSquare = false;
    private boolean isCircle = false;
    private boolean isCustom = false;
    private boolean isRhombus = false;
    //private boolean isErase ;
    List<Integer> xCoordinateList, yCoordinateList;
    private String drawingMode;
    private String selectedShape;
    private int selectedColor ;
    private List<PathData> removedPaths;

    //set drawwing mode
    public void SetDrawingMode(String drawingModeString) {
        drawingMode = drawingModeString;
    }

    // set paint color
    public void SetPaintColor(int color) {
        selectedColor = color;
    }

    // set  erasor
    //public void SetEraser() {
        //isErase = true;
    //}

    // Ui refresh of screem
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.setDrawingCacheEnabled(true);
        mBitmap = Bitmap.createBitmap(525, 610, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // Draw dunctions
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBitmap = Bitmap.createBitmap(525, 610, Bitmap.Config.ARGB_8888);
        if (mBitmap != null) {
            if(pdList!=null && pdList.size()>0) {

                for (PathData pd :pdList) {
                    int selectecColor = pd.getSelectedColor();

                    mPaint.setColor(selectecColor);
                    if(pd.getPath()!=null) {
                        if (pd.getIsFill())
                            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        else
                            mPaint.setStyle(Paint.Style.STROKE);
                        List<PathPoint> finalPoints = new ArrayList<PathPoint>();
                        finalPoints.addAll(pd.getPathPointList());
                        mPaint.setColor(selectecColor);
                        canvas.drawPath(pd.getPath(), mPaint);
                    }
                }
            }
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(selectedColor);
            canvas.drawPath(mPath, mPaint);
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        invalidate();
    }


    //show alert when shape not selected
    public void ShowAlert(String message)
    {
        int toastDurationInMilliSeconds = 400;
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);

        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 400 ) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }
            public void onFinish() {
                toast.cancel();
            }
        };
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_dialog, null);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        toastCountDown.start();
    }
    private boolean addFlag = false;
    private boolean removeFlag = false;
    private int mX, mY;
    private long startTime = 0;
    private long endTime = 0;


    //constructor
    public DrawView(Context cntxt) {
        super(cntxt);
        context = cntxt;
        selectedShape = Shape.Custom;
        selectedColor = -1;
        drawingMode = Shape.FreeHandDrawingMode;
        removedPaths = new ArrayList<>();
        mPath = new Path();
        pdList = new ArrayList<>();
        pointList = new ArrayList<>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    //set shape and fill-unfill
    public void SetShape(String shapeString) {
        selectedShape = shapeString;
        isLine = selectedShape.equals(Shape.Line);
        isCustom = selectedShape.equals(Shape.Custom);
        isTriangle = selectedShape.equals(Shape.TriangleStroke) || selectedShape.equals(Shape.TriangleSolid);
        isCircle = selectedShape.equals(Shape.CircleStroke) || selectedShape.equals(Shape.CircleSolid);
        isSquare = selectedShape.equals(Shape.SquareStroke) || selectedShape.equals(Shape.SquareSolid);
        isRhombus = selectedShape.equals(Shape.RhombusStroke) || selectedShape.equals(Shape.RhombusSolid);

        if (selectedShape.equals(Shape.Line) ||
                selectedShape.equals(Shape.TriangleStroke) ||
                selectedShape.equals(Shape.CircleStroke) ||
                selectedShape.equals(Shape.SquareStroke) ||
                selectedShape.equals(Shape.Custom) ||
                selectedShape.equals(Shape.RhombusStroke))
            isFill = false;
        else if (selectedShape.equals(Shape.TriangleSolid) ||
                selectedShape.equals(Shape.CircleSolid) ||
                selectedShape.equals(Shape.SquareSolid) ||
                selectedShape.equals(Shape.RhombusSolid))
            isFill = true;

    }

    public List<PathPoint> GetPoints() {
        List<PathPoint> points = new ArrayList<>();
        int ydiff = mEndY - mStartY, xdiff = mEndX - mStartY;
        double slope = (double) (mEndY - mStartY) / (mEndX - mStartX);
        double x, y;
        int quantity = 200;

        --quantity;

        for (double i = 0; i < quantity; i++) {
            y = slope == 0 ? 0 : ydiff * (i / quantity);
            x = slope == 0 ? xdiff * (i / quantity) : y / slope;
            points.add(new PathPoint((int) Math.round(x) + mStartX, (int) Math.round(y) + mStartY));
        }
        points.add(new PathPoint(mEndX, mEndY));
        return points;
    }

    public boolean CheckPointContainedInLine(PathPoint p, boolean breakFromLoop)
    {
        for (int i = 0; i <= 50; i++) {
            iloop:
            if (breakFromLoop)
                break iloop;
            for (int j = 0; j <= 50; j++) {
                jloop:
                if (breakFromLoop) break jloop;
                if ((p.x == mX + i && p.y == mY + j) ||
                        (p.x == mX + i && p.y == mY - j) || (p.x == mX - i && p.y == mY + j) ||
                        (p.x == mX - i && p.y == mY - j) || (p.x == mX + i && p.y == mY) ||
                        (p.x == mX - i && p.y == mY) || (p.x == mX && p.y == mY - j) ||
                        (p.x == mX && p.y == mY + j)) {
                    breakFromLoop = true;
                }
            }
        }
        return breakFromLoop;
    }


    public boolean  IsInTriangle(PathPoint p, PathPoint p0, PathPoint p1, PathPoint p2) {
        int dX = p.x-p2.x;
        int dY = p.y-p2.y;
        int dX21 = p2.x-p1.x;
        int dY12 = p1.y-p2.y;
        int D = dY12*(p0.x-p2.x) + dX21*(p0.y-p2.y);
        int s = dY12*dX + dX21*dY;
        int t = (p2.y-p0.y)*dX + (p0.x-p2.x)*dY;
        if (D<0) return s<=0 && t<=0 && s+t>=D;
        return s>=0 && t>=0 && s+t<=D;
    }

    public void Erasepaths() {
        if (endTime - startTime > longPressTimer ){//|| isErase) {
            startTime = 0;
            endTime = 0;

            if (pdList != null && pdList.size() > 0) {
                for (PathData pd : pdList) {
                    if (pd.getPath() != null) {
                        if (!pd.getSelectedShape().equals(Shape.TriangleSolid) && !pd.getSelectedShape().equals(Shape.TriangleStroke)
                        && !pd.getSelectedShape().equals(Shape.Line)
                        && !pd.getSelectedShape().equals(Shape.Custom)) {
                            RectF rectF = new RectF();
                            pd.getPath().computeBounds(rectF, true);
                            Region r = new Region();
                            r.setPath(pd.getPath(), new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                            if (r.contains(mX, mY)) {
                                removedPaths.add(pd);
                            }
                        }
                        else if(pd.getSelectedShape().equals(Shape.TriangleSolid) || pd.getSelectedShape().equals(Shape.TriangleStroke))
                        {
                          List<PathPoint> corners = pd.getPathPointList();
                           boolean isInTriangle =  IsInTriangle(new PathPoint(mX,mY),corners.get(0),corners.get(1),corners.get(2));
                            if (isInTriangle)
                                removedPaths.add(pd);
                        }
                        else if (pd.getSelectedShape().equals(Shape.Line) || pd.getSelectedShape().equals(Shape.Custom)) {
                            boolean breakFromLoop = false;
                            List<PathPoint> points = pd.getPathPointList();
                           if(points!=null && points.size()>0) {
                               for (int index = 0; index <= points.size() - 1; index++) {
                                   pointloop:
                                   if (breakFromLoop) break pointloop;
                                   PathPoint p = points.get(index);
                                   boolean isContains = CheckPointContainedInLine(p, breakFromLoop);
                                   if (isContains) {
                                       pd.setPathIndex(index);
                                       if (removedPaths.size() > 0) {
                                           List<PathData> remPathData= new ArrayList<>();
                                           remPathData.addAll(removedPaths)  ;
                                           for (PathData pdd : remPathData)
                                               if (pdd.getPathIndex() != index)
                                                   removedPaths.add(pd);
                                       } else
                                           removedPaths.add(pd);
                                       breakFromLoop = true;
                                   }
                               }
                           }

                        }
                    }
                }
                if(removedPaths!=null && removedPaths.size()>0) {
                    for (PathData pdd : removedPaths) {
                        if(pdList.size()>0)
                        pdList.remove(pdd);

                    }
                    List<PathData> remPathData= new ArrayList<>();
                    remPathData.addAll(removedPaths)  ;
                    removedPaths= new ArrayList<>();
                    removedPaths.add(remPathData.get(remPathData.size()-1));
                    remPathData= new ArrayList<>();
                    mPath = new Path();
                    invalidate();
                    //if(isErase)
                    //  isErase=false;
                }
                //removedPaths = new ArrayList<>();
            }
        }
    }

    // on touch event
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        mX = x;
        mY = y;
        if (selectedColor == -1) {
            ShowAlert("Please select color!");
            return false;
        }
        //if(isErase) {
           // Erasepaths();
           // }
        //else
            if (isCustom) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    startTime = event.getEventTime();
                    mPath = new Path();
                    mPath.moveTo(mStartX, mStartY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    endTime = 0;
                    float dx = Math.abs(mStartX - mEndX);
                    float dy = Math.abs(mStartY - mEndY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mEndX = x;
                        mEndY = y;
                        mPath.lineTo(mEndX, mEndY);
                    }
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endTime = event.getEventTime();
                    if (endTime - startTime > longPressTimer)
                        Erasepaths();
                    else {
                        List<PathPoint> pathPoints = new ArrayList<PathPoint>();
                        pathPoints.addAll(MyPointClass.GetPoints(mPath));
                        pdList.add(new PathData(mPath, pathPoints, selectedColor, isFill,selectedShape));
                        removedPaths = new ArrayList<>();
                    }
                    invalidate();
                    mPath = new Path();
                    break;
                default:
                    return false;
            }
            return true;
        } else if (!isCustom && drawingMode.equals(Shape.FreeHandDrawingMode) && isSquare) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    startTime = event.getEventTime();
                    mPath = new Path();
                    xCoordinateList = new ArrayList<>();
                    yCoordinateList = new ArrayList<>();
                    xCoordinateList.add(mStartX);
                    yCoordinateList.add(mStartY);
                    mPath.moveTo(mStartX, mStartY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    endTime = 0;
                    float dx = Math.abs(mStartX - mEndX);
                    float dy = Math.abs(mStartY - mEndY);
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        mEndX = x;
                        mEndY = y;
                        mPath.quadTo(mStartX, mStartY, (x + mStartX) / 2, (y + mStartY) / 2);
                    }

                    if (!isLine) {
                        mStartX = x;
                        mStartY = y;
                    }
                    xCoordinateList.add(mEndX);
                    yCoordinateList.add(mEndY);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endTime = event.getEventTime();
                    if (endTime - startTime > longPressTimer)
                        Erasepaths();
                    else {
                        mPath = new Path();
                        List<PathPoint> cornerPoints = new ArrayList<>();
                        int minX = MyPointClass.findMin(xCoordinateList);
                        int minY = MyPointClass.findMin(yCoordinateList);
                        int maxX = MyPointClass.findMax(xCoordinateList);
                        int maxY = MyPointClass.findMax(yCoordinateList);
                        cornerPoints.add(new PathPoint(minX, minY));
                        cornerPoints.add(new PathPoint(maxX, minY));
                        cornerPoints.add(new PathPoint(maxX, maxY));
                        cornerPoints.add(new PathPoint(minX, maxY));
                        DrawRectangle(cornerPoints);
                    }
                    invalidate();
                    xCoordinateList = new ArrayList<>();
                    yCoordinateList = new ArrayList<>();


                    break;

                default:
                    return false;
            }
            return true;

        } else if (!isCustom && drawingMode.equals(Shape.FreeHandDrawingMode) && !isSquare) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    startTime = event.getEventTime();
                    mPath.moveTo(mStartX, mStartY);
                    pointList.add(new PathPoint(mStartX, mStartY));
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    endTime = 0;
                    float dx = Math.abs(mStartX - mEndX);
                    float dy = Math.abs(mStartY - mEndY);
                    if (selectedShape.equals(Shape.Line))
                        pointList.add(new PathPoint(mStartX, mStartY));
                    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                        if (isLine) {
                            mEndX = x;
                            mEndY = y;
                            mPath.lineTo(mEndX, mEndY);
                        } else
                            mPath.quadTo(mStartX, mStartY, (x + mStartX) / 2, (y + mStartY) / 2);
                    }

                    if (!isLine) {
                        mStartX = x;
                        mStartY = y;
                    }
                    pointList.add(new PathPoint(mEndX, mEndY));
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    endTime = event.getEventTime();
                    if (endTime - startTime > longPressTimer)
                        Erasepaths();
                    else {
                        if (isTriangle) {
                            mEndX = (int) event.getX();
                            mEndY = (int) event.getY();
                            List<PathPoint> cornerPoints = MyPointClass.GetPathCornersTriangle(mPath);
                            DrawTriangle(cornerPoints);
                        } else if (isRhombus) {
                            mEndX = (int) event.getX();
                            mEndY = (int) event.getY();
                            List<PathPoint> cornerPoints = MyPointClass.GetPathCornersRhombus(mPath);
                            DrawRhombus(cornerPoints);
                        }
                   /* else if (isSquare) {
                        mEndX = (int) event.getX();
                        mEndY = (int) event.getY();
                        List<PathPoint> cornerPoints = MyPointClass.GetPathCornersTriangle(mPath);
                        //cornerPoints = MyPointClass.AllignSquareLines(cornerPoints);
                        //DrawSquare(cornerPoints);
                        Path path= MyPointClass.AllignSquareLines(cornerPoints);
                        pdList.add(new PathData(path, pointList, selectedColor, isFill));
                         removedPaths = new ArrayList<>();
                    }*/
                        // else if (isSquare)
                        //   DrawSquare();
                        else if (isCircle)
                            DrawCircle();
                        else if (isLine) {
                            DrawLine();
                        }
                    }
                    mPath = new Path();
                    pointList = new ArrayList<>();
                    invalidate();

                    break;

                default:
                    return false;
            }
            return true;

        } else if (!isCustom && drawingMode.equals(Shape.AutomaticDrawingmMode)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    startTime = event.getEventTime();
                    pointList.add(new PathPoint(mStartX, mStartY));
                    break;
                case MotionEvent.ACTION_MOVE:
                    endTime = 0;
                    mEndX = (int) event.getX();
                    mEndY = (int) event.getY();
                    pointList.add(new PathPoint(mEndX, mEndY));
                    break;
                case MotionEvent.ACTION_UP:
                    endTime = event.getEventTime();
                    if (endTime - startTime > longPressTimer)
                        Erasepaths();
                    else {
                        mEndX = (int) event.getX();
                        mEndY = (int) event.getY();
                        Path mPath = new Path();
                        float distance = MyPointClass.DistanceBetweenTwoPoints(mStartX, mEndX, mStartY, mEndY);
                        if (isTriangle) {
                            int radius = (int) MyPointClass.CalculateRadius(mStartX, mStartY, mEndX, mEndY);
                            mPath.reset();
                            mPath.moveTo(mStartX, mStartY - radius);
                            mPath.lineTo(mStartX - radius, mStartY + radius);
                            mPath.lineTo(mStartX + radius, mStartY + radius);
                            mPath.lineTo(mStartX, mStartY - radius);
                            mPath.close();
                            pointList = new ArrayList<>();
                            pointList.add(new PathPoint(mStartX, mStartY - radius));
                            pointList.add(new PathPoint(mStartX - radius, mStartY + radius));
                            pointList.add(new PathPoint(mStartX + radius, mStartY + radius));
                            pdList.add(new PathData(mPath, pointList, selectedColor, isFill,selectedShape));

                        } else if (isCircle) {
                            mPath.addCircle(mStartX, mStartY, distance / 2, Path.Direction.CW);
                            pdList.add(new PathData(mPath, pointList, selectedColor, isFill,selectedShape));
                            pointList = new ArrayList<>();
                        }
                        else if (isSquare) {
                            mPath.addRect(mStartX, mStartY, mEndX + distance / 2, mEndY + distance / 2, Path.Direction.CW);
                            pdList.add(new PathData(mPath, pointList, selectedColor, isFill,selectedShape));
                            pointList = new ArrayList<>();
                        }
                        else if (isLine) {
                            mPath.moveTo(mStartX, mStartY);
                            mPath.lineTo(mEndX, mEndY);
                            pdList.add(new PathData(mPath, GetPoints(), selectedColor, isFill,selectedShape));
                            pointList = new ArrayList<>();
                        }
                    }

                    invalidate();
                    break;
                default:
                    return false;
            }
            return true;
        }
        return true;
    }

    // Draw free hand Line
    public void DrawLine()
    {
        Path path = new Path();
        path.moveTo(mStartX, mStartY);
        path.lineTo(mEndX, mEndY);
        pdList.add(new PathData(path, GetPoints(), selectedColor, isFill,selectedShape));
        removedPaths = new ArrayList<>();
    }

    // Draw free hand Circle
    public void DrawCircle()
    {
        PathPoint pathMidpoint = MyPointClass.CalculatePathMidPoint(mPath);
        PathPoint circleCenterPoint = MyPointClass.CalculateCircleCenter(mStartX, pathMidpoint.getX(), mStartY, pathMidpoint.getY());
        float radius = MyPointClass.DistanceBetweenTwoPoints(mStartX, circleCenterPoint.getX(), mStartY, circleCenterPoint.getY());
        Path path = new Path();
        path.addCircle(circleCenterPoint.getX(), circleCenterPoint.getY(), radius, Path.Direction.CW);
        List<PathPoint> finalPoints = new ArrayList<>();
        finalPoints.add(new PathPoint(mStartX, mStartY));
        finalPoints.add(new PathPoint(circleCenterPoint.getX(), circleCenterPoint.getY()));
        pdList.add(new PathData(path, finalPoints, selectedColor, isFill,selectedShape));
        removedPaths = new ArrayList<>();
    }

    // Draw free hand Triangle
    public void DrawTriangle(List<PathPoint> cornerPoints)
    {
        if (cornerPoints != null && cornerPoints.size() == 3) {
            Path pathObj = new Path();
            pathObj.moveTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            for (int i = 1; i <= cornerPoints.size() - 1; i++) {
                pathObj.lineTo(cornerPoints.get(i).getX(), cornerPoints.get(i).getY());
            }
            pathObj.lineTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            pdList.add(new PathData(pathObj, cornerPoints, selectedColor, isFill,selectedShape));
            removedPaths = new ArrayList<>();
        }
    }

    // Draw free hand Rhombus
    public void DrawRhombus(List<PathPoint> cornerPoints) {
        if (cornerPoints != null && cornerPoints.size() == 4) {
            Path pathObj = new Path();
            pathObj.moveTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            for (int i = 1; i <= cornerPoints.size() - 1; i++) {
                pathObj.lineTo(cornerPoints.get(i).getX(), cornerPoints.get(i).getY());
            }
            pathObj.lineTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            pdList.add(new PathData(pathObj, cornerPoints, selectedColor, isFill,selectedShape));
            removedPaths = new ArrayList<>();
        }
    }

    // Draw free hand Square
    public void DrawSquare() {

        PathPoint pathMidpoint = MyPointClass.CalculatePathMidPoint(mPath);
        PathPoint circleCenterPoint = MyPointClass.CalculateCircleCenter(mStartX, pathMidpoint.getX(), mStartY, pathMidpoint.getY());
        float radius = MyPointClass.DistanceBetweenTwoPoints(mStartX, circleCenterPoint.getX(), mStartY, circleCenterPoint.getY());
        mPath = new Path();
        Path path = new Path();
        radius = 3*radius/4;
        path.addRect((float) circleCenterPoint.getX() - radius, (float) circleCenterPoint.getY() - radius,
                (float) circleCenterPoint.getX() + radius, (float) circleCenterPoint.getY() + radius, Path.Direction.CW);
        List<PathPoint> finalPoints = new ArrayList<>();
        finalPoints.add(new PathPoint(mStartX, mStartY));
        finalPoints.add(new PathPoint(circleCenterPoint.getX(), circleCenterPoint.getY()));
        pdList.add(new PathData(path, finalPoints, selectedColor, isFill,selectedShape));
        removedPaths = new ArrayList<>();

       /* if (cornerPoints != null && cornerPoints.size()  == 4) {
            Path pathObj = new Path();
            pathObj.moveTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            for (int i = 1; i <= cornerPoints.size() - 1; i++) {
                pathObj.lineTo(cornerPoints.get(i).getX(), cornerPoints.get(i).getY());
            }
            pathObj.lineTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            pdList.add(new PathData(pathObj, cornerPoints, selectedColor, isFill));
             removedPaths = new ArrayList<>();
        }*/
    }

    // Draw free hand Rectangle
    public void DrawRectangle(List<PathPoint> cornerPoints) {
        if (cornerPoints != null && cornerPoints.size() == 4) {
            Path pathObj = new Path();
            pathObj.moveTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            for (int i = 1; i <= cornerPoints.size() - 1; i++) {
                pathObj.lineTo(cornerPoints.get(i).getX(), cornerPoints.get(i).getY());
            }
            pathObj.lineTo(cornerPoints.get(0).getX(), cornerPoints.get(0).getY());
            pdList.add(new PathData(pathObj, cornerPoints, selectedColor, isFill,selectedShape));
            removedPaths = new ArrayList<>();
        }
    }

    // undo drawing steps
    public void UndoDrawing() {

        if (removedPaths.size() > 0) {
            PathData erasePathData = removedPaths.get(removedPaths.size() - 1);
            if (erasePathData != null) {
                pdList.add(erasePathData);
                erasePathData = null;
                removedPaths = new ArrayList<>();
                addFlag = true;
            }
        } else if (pdList.size() > 0) {
            prevPathData = pdList.get(pdList.size() - 1);
            pdList.remove(pdList.size() - 1);
            removeFlag = true;
        }
        mBitmap = Bitmap.createBitmap(525, 610, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (mBitmap != null) {
            for (PathData pd : pdList) {
                List<PathPoint> finalPoints = new ArrayList<PathPoint>();
                finalPoints.addAll(pd.getPathPointList());
                int selectecColor = pd.getSelectedColor();
                if (pd.getIsFill())
                    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                else
                    mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(selectecColor);
                mCanvas.drawPath(pd.getPath(), mPaint);
            }
            mCanvas.drawBitmap(mBitmap, 0, 0, null);
        }
        invalidate();
    }

    // undo drawing steps-
    public void RedoDrawing() {
        mBitmap = Bitmap.createBitmap(525, 610, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (removeFlag) {
            pdList.add(prevPathData);
            prevPathData = null;
            ;
            removeFlag = false;
        } else if (addFlag) {
            pdList.remove(pdList.size() - 1);
            addFlag = false;
        }
        if (mBitmap != null) {
            for (PathData pd : pdList) {
                List<PathPoint> finalPoints = new ArrayList<PathPoint>();
                finalPoints.addAll(pd.getPathPointList());
                int selectecColor = pd.getSelectedColor();
                if (pd.getIsFill())
                    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                else
                    mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(selectecColor);
                mCanvas.drawPath(pd.getPath(), mPaint);
            }
            mCanvas.drawBitmap(mBitmap, 0, 0, null);
        }
        invalidate();

    }


    //save drawing
    public  String saveDrawing() throws FileNotFoundException {
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String targetDirPath = storagePath + "/Pictures/";
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            if (false == targetDir.mkdirs()) {

                return "";
            }
        }
        if (targetDir.isDirectory()) {
            String[] children = targetDir.list();
            if(children!=null) {
                for (int i = 0; i < children.length; i++) {
                    new File(targetDir, children[i]).delete();
                }
            }
        }
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String strDate = dateFormat.format(date);
        String fileName = "Drawing"+strDate;
        String filePath = targetDirPath + "Drawing"+strDate + ".png";
        FileOutputStream fos = new FileOutputStream(filePath);
        try {
            this.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, fos);
            this.getDrawingCache().setHasAlpha(true);
            MediaStore.Images.Media.insertImage(context.getContentResolver(), this.getDrawingCache(),
                                           "drawing", "drawing");
            Toast.makeText(context, "Drawing Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            String error =e.getMessage();
            Toast.makeText(context, "Error in saving", Toast.LENGTH_LONG).show();
            return "";
        } finally {
            try {
                fos.close();
                return fileName;
            } catch (IOException e) {
                String error =e.getMessage();
                Toast.makeText(context, "Error in saving", Toast.LENGTH_LONG).show();
                return "";
            }
        }
    }
}
