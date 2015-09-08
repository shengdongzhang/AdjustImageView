package com.ddxce.open;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * @author Shengdong Zhang
 * @version  1.0
 * @// FIXME: 15/9/8
 */

public class AdjustImageView extends ImageView {
    public interface Action {
        void onClick();
    }
    Matrix matrix = new Matrix(); //设置矩阵
    Matrix savedMatrix = new Matrix(); //触摸矩阵
    Matrix startMatrix = new Matrix(); //0度矩阵 centerInside
    Matrix angle90Matrix = new Matrix(); //90度矩阵 centerInside
    Matrix angle180Matrix = new Matrix(); //180度矩阵 centerInside
    Matrix angle270Matrix = new Matrix(); //270度矩阵 centerInside
    Matrix animationEndMatrix = new Matrix();

    float insideHorizontalScale, insideVerticalScale;
    float cropHorizontalScale, cropVerticalScale;

    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    private static final String TAG = "AdjustSelfImageView";
    private static final int DOUBLE_CLICK_TIME = 400;
    int mode = NONE;

    PointF start = new PointF();
    float oldDist = 1f;
    float oldRotate = 0f;
    float rotateDis;

    private int clickCount;
    private long lastClickTime;

    private int imageWidth, imageHeight;
    private float scale;
    private float angle;
    private PointF center;

    private Handler handler = new Handler();
    private Action action;

    private boolean isChange;
    private boolean animation;
    private boolean isLock;
    private boolean isDoubleClick;
    private boolean canHorizontal;
    private boolean canVertical;
    private boolean canRotate;
    private boolean isSetting;
    private int deviceWidth, deviceHeight;
    private PointF leftTop, rightTop, leftBottom, rightBottom;
    private Context context;
    public AdjustImageView(Context context) {
        this(context, null);
    }

    public AdjustImageView(Context context, AttributeSet paramAttributeSet) {
        super(context, paramAttributeSet);
        this.context = context;
        center = new PointF(getDeviceWidth() / 2, (getDeviceHeight() - getStatusBarHeight()) / 2);
        this.setScaleType(ScaleType.MATRIX);
        initData();
        deviceWidth = getDeviceWidth();
        deviceHeight = getDeviceHeight();
    }

    public void setLock(boolean isLock) {
        this.isLock = isLock;
    }

    public boolean isLock() {
        return isLock;
    }

    private void initData() {
        scale = 1.0f;
        angle = 0.0f;
        oldDist = 1.0f;
        oldRotate = 0.0f;
        isChange = false;
        isLock = true;
        canHorizontal = canVertical = true;
        clickCount = 0;
        animation = false;
        leftTop = new PointF();
        rightTop = new PointF();
        leftBottom = new PointF();
        rightBottom = new PointF();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isLock)
            return false;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(this.getImageMatrix());
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");
                mode = DRAG;
                isChange = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = getDistance(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    oldRotate = getRotation(event);
                    mode = ZOOM;
                    canRotate = false;
                    Log.d(TAG, "mode=ZOOM");
                }
                isChange = false;
                break;
            case MotionEvent.ACTION_UP:
                if(!isChange) {
                    clickCount++;
                    if(clickCount % 2 == 1) {
                        isDoubleClick = false;
                        lastClickTime = System.currentTimeMillis();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!isDoubleClick) {
                                    clickCount = 0;
                                    if(action != null)
                                        action.onClick();
                                }
                            }
                        }, DOUBLE_CLICK_TIME);
                    }
                    else if(clickCount % 2 == 0) {
                        long now = System.currentTimeMillis();
                        if(now - lastClickTime < DOUBLE_CLICK_TIME) {
                            isDoubleClick = true;
                            animation = true;
                            onDoubleClick(event);
                        }
                        lastClickTime = 0;
                    }
                }
                else {
                    Matrix adjust = adjust(matrix);
                    if(!isSame(adjust, matrix)) {
                        isDoubleClick = false;
                        animation = true;
                        animationEndMatrix.set(adjust);
                    }
                }
                mode = NONE;
                isChange = false;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (mode == DRAG && event.getPointerCount() >= 1) {
                    if(Math.abs(event.getY() - start.y) < deviceHeight / 8 && !canScrolled(event.getX() < start.x)) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if((event.getX() - start.x) * (event.getX() - start.x)
                            + (event.getY() - start.y) * (event.getY() - start.y) > 100) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                        isChange = true;
                    }
                } else if (mode == ZOOM && event.getPointerCount() >= 2) {
                    float newDist = getDistance(event);
                    float rotation =  getRotation(event) - oldRotate;
                    if (newDist > 10f) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                        matrix.set(savedMatrix);
                        float tempScale = newDist / oldDist;
                        if(!canRotate && Math.abs(rotation) > 20f) {
                            canRotate = true;
                            rotateDis = rotation;
                        }
                        if(canRotate) {
                            matrix.postRotate(rotation - rotateDis, center.x, center.y);
                        }
                        matrix.postScale(tempScale, tempScale, center.x, center.y);
                        isChange = true;
                    }
                    else {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                }
                break;
            default:
        }
        if(animation) {
            animation = false;
            matrixAnimation(matrix, animationEndMatrix);
            matrix.set(animationEndMatrix);
        }
        else {
            this.setImageMatrix(matrix);
        }
        update(matrix);
        return true;
    }

    private void onDoubleClick(MotionEvent event) {
        update(matrix);
        float x = event.getX();
        float y = event.getY();
        int index = -1;
        if(isSame(matrix, startMatrix)) {
            index = 0;
            animationEndMatrix.set(startMatrix);
            animationEndMatrix.postScale(cropHorizontalScale, cropHorizontalScale, x, y);
        }
        else if(isSame(matrix, angle90Matrix)) {
            index = 1;
            animationEndMatrix.set(angle90Matrix);
            animationEndMatrix.postScale(cropVerticalScale, cropVerticalScale, x, y);
        }
        else if(isSame(matrix, angle180Matrix)) {
            index = 2;
            animationEndMatrix.set(angle180Matrix);
            animationEndMatrix.postScale(cropHorizontalScale, cropHorizontalScale, x, y);
        }
        else if(isSame(matrix, angle270Matrix)) {
            index = 3;
            animationEndMatrix.set(angle270Matrix);
            animationEndMatrix.postScale(cropVerticalScale, cropVerticalScale, x, y);
        }
        if(index != -1) {
            animationEndMatrix.set(adjust(animationEndMatrix));
        }
        else {
            animationEndMatrix.set(rotate());
        }
    }

    private boolean isSame(Matrix a, Matrix b) {
        float[] nowFloat = new float[9];
        a.getValues(nowFloat);
        float[] orgFloat = new float[9];
        b.getValues(orgFloat);
        for(int i = 0; i < 9; ++i) {
            if(Math.abs(nowFloat[i] - orgFloat[i]) > 1e-6) {
                return false;
            }
        }
        return true;
    }

    private PointF getPointF(Matrix m, PointF originPointF) {
        float[] values = new float[9];
        m.getValues(values);
        PointF pointF = new PointF();
        pointF.x = values[Matrix.MSCALE_X] * originPointF.x + values[Matrix.MSKEW_X] * originPointF.y
                + values[Matrix.MTRANS_X];
        pointF.y = values[Matrix.MSKEW_Y] * originPointF.x + values[Matrix.MSCALE_Y] * originPointF.y
                + values[Matrix.MTRANS_Y];
        return pointF;
    }

    private float min(float a, float b, float c, float d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }

    private float max(float a, float b, float c, float d) {
        return Math.max(a, Math.max(b, Math.max(c, d)));
    }

    private RectF getRect(Matrix matrix) {
        updatePointF(matrix);
        PointF a = new PointF();
        PointF b = new PointF();
        a.x = min(leftTop.x, leftBottom.x, rightTop.x, rightBottom.x);
        a.y = min(leftTop.y, leftBottom.y, rightTop.y, rightBottom.y);
        b.x = max(leftTop.x, leftBottom.x, rightTop.x, rightBottom.x);
        b.y = max(leftTop.y, leftBottom.y, rightTop.y, rightBottom.y);
        return new RectF(a.x, a.y, b.x, b.y);
    }

    private void updatePointF(Matrix matrix) {
        leftTop = getPointF(matrix, new PointF(0, 0));
        rightTop = getPointF(matrix, new PointF(imageWidth, 0));
        leftBottom = getPointF(matrix, new PointF(0, imageHeight));
        rightBottom = getPointF(matrix, new PointF(imageWidth, imageHeight));
    }

    private float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取旋转角度
    private float getRotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        if(Math.abs(delta_x) < 1e-6) {
            if(event.getY(0) > event.getY(1)) {
                return 90.0f;
            }
            return 270.f;
        }
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        imageWidth = bm.getWidth();
        imageHeight = bm.getHeight();
        isSetting = false;
    }

    private void setSetting() {
        setLock(true);
        center = new PointF(getWidth() / 2.0f, this.getHeight() / 2.0f);
        float[] initFloat = new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1};

        int width = getWidth();
        Log.d("width", width + "");
        if(width == 0 && context != null)
            width = getDeviceWidth();
        int height = getHeight();
        Log.d("height", height + "");
        if(height == 0 && context != null)
            height = getDeviceHeight() - getStatusBarHeight();
        float scale, newWidth, newHeight, cropScale;
        int nowImageWidth = imageWidth;
        int nowImageHeight = imageHeight;
        if(nowImageWidth * height >= nowImageHeight * width) { //横
            scale = width * 1.0f / nowImageWidth;
            newWidth = width;
            newHeight = nowImageHeight * scale;

            cropScale = height / newHeight;
        }
        else { //竖
            scale = height * 1.0f / nowImageHeight;
            newWidth = nowImageWidth * scale;
            newHeight = height;

            cropScale = width / newWidth;
        }
        insideHorizontalScale = scale;
        cropHorizontalScale = cropScale;

        startMatrix.setValues(initFloat);
        startMatrix.postScale(scale, scale, 0, 0);
        startMatrix.postTranslate((width - newWidth) / 2.0f, (height - newHeight) / 2.0f);
        matrix.set(startMatrix);

        update(startMatrix);

        angle180Matrix.setValues(initFloat);
        angle180Matrix.postScale(scale, scale, 0, 0);
        angle180Matrix.postRotate(180, 0, 0);
        angle180Matrix.postTranslate((width + newWidth) / 2.0f, (height + newHeight) / 2.0f);


        nowImageWidth = imageHeight;
        nowImageHeight = imageWidth;
        if(nowImageWidth * height >= nowImageHeight * width) { //横
            scale = width * 1.0f / nowImageWidth;
            newWidth = width;
            newHeight = nowImageHeight * scale;
            cropScale = height / newHeight;
        }
        else { //竖
            scale = height * 1.0f / nowImageHeight;
            newWidth = nowImageWidth * scale;
            newHeight = height;

            cropScale = width / newWidth;
        }
        insideVerticalScale = scale;
        cropVerticalScale = cropScale;

        angle90Matrix.setValues(initFloat);
        angle90Matrix.postScale(scale, scale, 0, 0);
        angle90Matrix.postRotate(90, 0, 0);
        angle90Matrix.postTranslate((width + newWidth) / 2.0f, (height - newHeight) / 2.0f);

        angle270Matrix.setValues(initFloat);
        angle270Matrix.postScale(scale, scale, 0, 0);
        angle270Matrix.postRotate(-90, 0, 0);
        angle270Matrix.postTranslate((width - newWidth) / 2.0f, (height + newHeight) / 2.0f);
        this.setScaleType(ScaleType.MATRIX);
        this.setImageMatrix(startMatrix);

        initData();
        setLock(false);
    }

    public void toInit() {
        toInit(false);
    }

    public void toInit(boolean animation) {
        if(animation) {
            matrixAnimation(matrix, startMatrix);
        }
        else {
            setImageMatrix(startMatrix);
        }
        matrix.set(startMatrix);
    }

    public void setAction(Action action) {
        this.action = action;
    }

    private boolean isOverEdge(float x, boolean moveToLeft) {
        if(moveToLeft) {
            if(x >= getWidth())
                return false;
            return true;
        }
        else {
            if(x < 0)
                return false;
            return true;
        }
    }

    private Matrix rotate() {
        Matrix targetMatrix;
        if(angle < 45.f)
            targetMatrix = startMatrix;
        else if(angle < 135.f)
            targetMatrix = angle90Matrix;
        else if(angle < 225.f)
            targetMatrix = angle180Matrix;
        else if(angle < 315.f)
            targetMatrix = angle270Matrix;
        else
            targetMatrix = startMatrix;
        return targetMatrix;
    }

    private Matrix adjust(Matrix m) {
        Matrix result = new Matrix();
        //调整rotate
        if(Math.abs(angle - 0) < 1e-6 || Math.abs(angle - 90) < 1e-6
                || Math.abs(angle - 180) < 1e-6 || Math.abs(angle - 270) < 1e-6) {
            result.set(m);
        }
        else {
            result = rotate();
            return result;
        }
        //调整scale
        if(Math.abs(angle - 0) < 1e-6 || Math.abs(angle - 180) < 1e-6) {
            if(scale < insideHorizontalScale)  {
                result.postScale(insideHorizontalScale / scale,
                        insideHorizontalScale / scale, center.x, center.y);
            }
            else {
                float maxScale = Math.max(cropHorizontalScale, Math.max(insideHorizontalScale, 1.0f));
                if(scale > maxScale) {
                    result.postScale(maxScale / scale, maxScale / scale, center.x, center.y);
                }
            }
        }
        else if(Math.abs(angle - 90) < 1e-6 || Math.abs(angle - 270) < 1e-6) {
            if(scale < insideVerticalScale) {
                result.postScale(insideVerticalScale / scale, insideVerticalScale / scale, center.x, center.y);
            }
            else {
                float maxScale = Math.max(cropVerticalScale, Math.max(insideVerticalScale, 1.0f));
                if(scale > maxScale) {
                    result.postScale(maxScale / scale, maxScale / scale , center.x, center.y);
                }
            }
        }
        update(result);

        //调整 translate
        RectF rectF = getRect(result);
        float imageWidth = rectF.right - rectF.left;
        float imageHeight = rectF.bottom - rectF.top;
        int width = getWidth();
        if(width == 0 && context != null)
            width = getDeviceWidth();
        int height = getHeight();
        if(height == 0 && context != null)
            height = getDeviceHeight() - getStatusBarHeight();
        float offX = 1.0f, offY = 1.0f;
        if(rectF.left > 0) {
            if(rectF.right - width >= rectF.left) {
                offX = -rectF.left;
            }
            else {
                offX = -(rectF.right - width) - (width - imageWidth) / 2.0f;
            }
        }
        else if(rectF.right < width) { // rectF.left <= 0
            if(Math.abs(rectF.left) >= width - rectF.right) {
                offX = width - rectF.right;
            }
            else {
                offX = Math.abs(rectF.left) + (width - imageWidth) / 2.0f;
            }
        }
        if(rectF.top > 0) {
            if(rectF.bottom - height >= rectF.top) {
                offY = -rectF.top;
            }
            else {
                offY = -(rectF.bottom - height) - (height - imageHeight) / 2.0f;
            }
        }
        else if(rectF.bottom < height) {
            if(Math.abs(rectF.top) >= height - rectF.bottom) {
                offY = height - rectF.bottom;
            }
            else {
                offY = Math.abs(rectF.top) + (height - imageHeight) / 2.0f;
            }
        }
        result.postTranslate(offX, offY);
        return result;
    }

    private boolean canScrolled(boolean moveToLeft) {
        boolean flag = true;
        float[] values = new float[9];
        matrix.getValues(values);
        float x = values[Matrix.MSCALE_X] * 0 + values[Matrix.MSKEW_X] * 0
                + values[Matrix.MTRANS_X];
        flag &= isOverEdge(x, moveToLeft);
        x = values[Matrix.MSCALE_X] * 0 + values[Matrix.MSKEW_X] * imageHeight
                + values[Matrix.MTRANS_X];
        flag &= isOverEdge(x, moveToLeft);
        x = values[Matrix.MSCALE_X] * imageWidth + values[Matrix.MSKEW_X] * 0
                + values[Matrix.MTRANS_X];
        flag &= isOverEdge(x, moveToLeft);
        x = values[Matrix.MSCALE_X] * imageWidth + values[Matrix.MSKEW_X] * imageHeight
                + values[Matrix.MTRANS_X];
        flag &= isOverEdge(x, moveToLeft);
        return !flag;
    }

    private void update(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        float x = values[Matrix.MSCALE_X] * imageWidth + values[Matrix.MSKEW_X] * 0
                + values[Matrix.MTRANS_X] - values[Matrix.MTRANS_X];
        float y = values[Matrix.MSKEW_Y] * imageWidth + values[Matrix.MSCALE_Y] * 0
                + values[Matrix.MTRANS_Y] - values[Matrix.MTRANS_Y];
        angle = (float) Math.toDegrees(Math.atan2(y, x));
        if(angle < 0.0f)
            angle += 360.0f;
        scale = (float) Math.sqrt(x * x + y * y) / imageWidth;

        x = values[Matrix.MSCALE_X] * (imageWidth / 2.0f) + values[Matrix.MSKEW_X] * (imageHeight / 2.0f)
                + values[Matrix.MTRANS_X];
        y = values[Matrix.MSKEW_Y] * (imageWidth / 2.0f) + values[Matrix.MSCALE_Y] * (imageHeight / 2.0f)
                + values[Matrix.MTRANS_Y];
        center.x = x;
        center.y = y;
    }

    protected void onDraw(Canvas canvas) {
        if(!isSetting) {
            setSetting();
            isSetting = true;
        }
        super.onDraw(canvas);
    }

    private void matrixAnimation(Matrix start, Matrix end) {
        if(isLock())
            return;
        setLock(true);
        final float[] startFolat = new float[9];
        start.getValues(startFolat);
        float[] endFloat = new float[9];
        end.getValues(endFloat);
        final float[] disFloat = new float[9];
        final int frameSize = 10;
        for(int i = 0; i < 9; ++i) {
            disFloat[i] = (endFloat[i] - startFolat[i]) * 1.0f / frameSize;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i = 1; i <= frameSize; i++) {
                    float[] nowFloat = new float[9];
                    for(int j = 0; j < 9; ++j) {
                        nowFloat[j] = startFolat[j] + disFloat[j] * i;
                    }
                    final Matrix now = new Matrix();
                    now.setValues(nowFloat);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            setImageMatrix(now);
                        }
                    });
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                setLock(false);
            }
        }).start();
    }

    private int getDeviceWidth() {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    private int getDeviceHeight() {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }

    private int getStatusBarHeight() {
        Rect frame = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }
}
