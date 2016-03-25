package com.jinsen47.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jinsen47.slidingactionview.R;
import com.jinsen47.util.BitmapUtil;

/**
 * Created by Jinsen on 15/12/26.
 */
public class SlidingActionView extends View {
    private static final String TAG = SlidingActionView.class.getSimpleName();

    private final int defaultWidth = 300;
    private final int defaultHeight = 100;

    private final int defaultCenterDiameter = 100;
    private final int defaultMovingTextOffset = 100;
    private final int defaultMovingTextColor = 0xff9e9e9e;
    private final int defaultMovingTextSize = 16;
    private final int defaultMovingCircleRadius = 41;
    private final int defaultMovingCircleColor = 0xfff7f7f7;

    private final int defaultAnimatorDuration = 200;

    private int strokeWidth = 6;
    private int actionLayerColor = Color.WHITE;

    private int mWidth;
    private int mHeight;
    private float ratio;
    private float mDensity;

    // 中图片资源, 已经经过取圆处理
    private Bitmap centerActionLayerBitmap;
    private Bitmap centerIconBitmap;

    // 中图片的位置区域
    private RectF centerRectF;

    // 左右文字
    private String leftMovingText = "暂停";
    private String rightMovingText = "停止";

    private Paint mPaint;
    private Paint mStrokePaint;

    private Path pathLeft;
    private Path pathRight;

    // 圆角矩形区域
    private RectF layerRectF;
    private ValueAnimator layerDownAnimator;
    private ValueAnimator layerUpAnimator;
    private int layerStartColor = 0xffefefef;
    private int layerEndColor = 0xfff2f2f2;

    private ValueAnimator.AnimatorUpdateListener layerAnimatorUpdateListener;

    private SlidingActionListener mListener;

    private float mMovingCircleOffsetX = 0;
    private float mDownX;

    // 初始化是否完成flag
    private boolean isInitComplete;
    // 控件状态
    private ActionState mState = ActionState.STANDBY;

    public SlidingActionView(Context context) {
        super(context);
        init(context, null);
    }

    public SlidingActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SlidingActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        isInitComplete = false;
        mDensity = context.getResources().getDisplayMetrics().density;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mStrokePaint = new Paint();
        mStrokePaint.setStrokeWidth(strokeWidth);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        initBitmap();
        initAnimators();
        initPath();
        isInitComplete = true;
    }

    private void initBitmap() {
        centerActionLayerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.center_action_layer_bg);
    }

    private void initAnimators() {
        layerAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (layerRectF == null) {
                    layerRectF = new RectF(0, ((int) (-0.5 * mHeight)), 0, mHeight / 2);
                }
                float cur = ((float) animation.getAnimatedValue());
                layerRectF.left = -1 * cur / 2;
                layerRectF.right = cur / 2;
                postInvalidate();
            }
        };
        layerDownAnimator = ValueAnimator.ofFloat(0, defaultWidth * mDensity);
        layerDownAnimator.setDuration(defaultAnimatorDuration);
        layerDownAnimator.addUpdateListener(layerAnimatorUpdateListener);
        layerDownAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mState == ActionState.ANIMATE_DOWN) {
                    setState(ActionState.MOVING);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        layerUpAnimator = ValueAnimator.ofFloat(defaultWidth * mDensity, 0);
        layerUpAnimator.setDuration(defaultAnimatorDuration);
        layerUpAnimator.addUpdateListener(layerAnimatorUpdateListener);
        layerUpAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mState == ActionState.ANIMATE_UP) {
                    setState(ActionState.STANDBY);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void initPath() {
        pathLeft = new Path();
        pathLeft.moveTo(-60 * mDensity, 0);
        pathLeft.lineTo(-50 * mDensity, 10 * mDensity);
        pathLeft.lineTo(-50 * mDensity, -10 * mDensity);
        pathLeft.lineTo(-60 * mDensity, 0);

        pathRight = new Path();
        pathRight.moveTo(60 * mDensity, 0);
        pathRight.lineTo(50 * mDensity, 10 * mDensity);
        pathRight.lineTo(50 * mDensity, -10 * mDensity);
        pathRight.lineTo(60 * mDensity, 0);
    }

    private void generateAndRunMovingCircleAnimator(float start) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, 0.0f);
        animator.setDuration(defaultAnimatorDuration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cur = ((float) animation.getAnimatedValue());
                mMovingCircleOffsetX = cur;
                postInvalidate();
            }
        });
        animator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setState(ActionState.ANIMATE_UP);
                layerUpAnimator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private synchronized void setState(ActionState newState) {
//        Log.d(TAG, "new state : " + newState.name());
        mState = newState;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isInitComplete) return;

        // 将canvas中心变换到中间
        canvas.save();
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        // 将canvas按比例缩放
        canvas.scale(ratio, ratio, 0, 0);

        drawIconAndText(canvas);
        if (mState == ActionState.ANIMATE_DOWN || mState == ActionState.ANIMATE_UP) {
            drawActionLayer(canvas);
        }
        if (mState == ActionState.MOVING) {
            drawMovingLayer(canvas);
        }
        canvas.restore();
    }

    private void drawIconAndText(Canvas canvas) {
        float left, top;

        // 画中间的icon
        canvas.save();

        left = ((float) (-1 * defaultCenterDiameter * mDensity / 2.0));
        top = ((float) (-1 * defaultCenterDiameter * mDensity / 2.0));

        if (centerRectF == null) {
            centerRectF = new RectF(left, top, left + defaultCenterDiameter * mDensity, top + defaultCenterDiameter * mDensity);
        }
        if (centerIconBitmap != null) {
            canvas.drawBitmap(centerIconBitmap, left, top, mPaint);
        }

        canvas.restore();
    }

    private void drawActionLayer(Canvas canvas) {
        canvas.save();
        mPaint.setColor(actionLayerColor);
        if (layerRectF == null) {
            layerRectF = new RectF(0, ((int) (-0.5 * canvas.getHeight())), 0, canvas.getHeight() / 2);
        }
        mPaint.setShader(new LinearGradient(0, ((int) (-0.5 * canvas.getHeight())),0, canvas.getHeight() / 2, layerStartColor, layerEndColor, Shader.TileMode.CLAMP));
        if (Math.abs(layerRectF.width() - centerActionLayerBitmap.getWidth()) > 10) {
            canvas.drawRoundRect(layerRectF, canvas.getHeight() / 2, canvas.getHeight() / 2, mPaint);
        } else {
            canvas.drawBitmap(centerActionLayerBitmap, ((float) (-1 * centerActionLayerBitmap.getWidth() / 2.0)), ((float) (-1 * centerActionLayerBitmap.getHeight() / 2.0)), null);
        }
        mPaint.setShader(null);
        drawActionLayerText(canvas);
        drawActionLayerTriangle(canvas);
        canvas.restore();
    }

    private void drawActionLayerText(Canvas canvas) {
        canvas.save();
        if (layerRectF.width() > 250 * mDensity) {
            mPaint.setColor(defaultMovingTextColor);
            mPaint.setTextSize(defaultMovingTextSize * mDensity);
            mPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetricsInt font = mPaint.getFontMetricsInt();

            int baseline = font.bottom;
            canvas.drawText(leftMovingText, -1 * mDensity * defaultMovingTextOffset, baseline, mPaint);
            canvas.drawText(rightMovingText, mDensity * defaultMovingTextOffset, baseline, mPaint);
        }
        canvas.restore();
    }

    private void drawActionLayerTriangle(Canvas canvas) {
        canvas.save();
        if (layerRectF.width() > 120 * mDensity) {
            mPaint.setColor(0xffdddddd);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(pathLeft, mPaint);
            canvas.drawPath(pathRight, mPaint);
        }
        canvas.restore();
    }

    private void drawMovingLayer(Canvas canvas) {
        canvas.save();
        drawActionLayer(canvas);
        mPaint.setColor(defaultMovingCircleColor);
        float circleX = mMovingCircleOffsetX;
        float border = mWidth / 2 - (defaultMovingCircleRadius * mDensity + 20);
        float radius = defaultMovingCircleRadius * mDensity;
        if (circleX <= -1 * border || circleX >= border) {
            circleX = circleX < 0 ? -1 * border: border;
        }
        mStrokePaint.setShader(new RadialGradient(circleX, 0, radius, 0xff434343, layerStartColor, Shader.TileMode.CLAMP));
        canvas.drawCircle(circleX, 0, radius, mPaint);
        canvas.drawCircle(circleX, 0, radius, mStrokePaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TouchArea area = getTouchArea(event.getX(), event.getY());
        if (area == TouchArea.Center || mState == ActionState.MOVING) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (mState != ActionState.MOVING) {
                        mState = ActionState.STANDBY;
                    } else {
                        float border = mWidth / 2 - (defaultMovingCircleRadius * mDensity + 20);
                        if (mMovingCircleOffsetX <= -1 * border) {
                            if (mListener != null) {
                                mListener.onLeftEdgeReached();
                            }
                        } else if (mMovingCircleOffsetX >= border) {
                            if (mListener != null) {
                                mListener.onRightEdgeReached();
                            }
                        }
                        generateAndRunMovingCircleAnimator(mMovingCircleOffsetX);
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    setState(ActionState.ANIMATE_DOWN);
                    layerDownAnimator.start();
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
                case MotionEvent.ACTION_MOVE:
                    setState(ActionState.MOVING);
                    mMovingCircleOffsetX = event.getX() - mDownX;
                    invalidate();
                    break;
                default:
                    break;
            }
            invalidate();
        }
        return true;
    }

    private TouchArea getTouchArea(float x, float y) {
        if (centerRectF.contains(translateX(x), translateY(y))) {
            return TouchArea.Center;
        } else {
            return TouchArea.Other;
        }
    }

    private float translateX(float x) {
        return x - mWidth / 2;
    }

    private float translateY(float y) {
        return y - mHeight / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float density = getResources().getDisplayMetrics().density;
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        }

        if (widthMode == MeasureSpec.AT_MOST) {
            mWidth = ((int) (defaultWidth * density));
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        }

        if (heightMode == MeasureSpec.AT_MOST) {
            mHeight = ((int) (defaultHeight * density));
        }

        if (mWidth / 3 > mHeight) {
            mWidth = mHeight * 3;
        } else {
            mHeight = mWidth / 3;
        }

        ratio = widthSize / density / defaultWidth;
        setMeasuredDimension(mWidth, mHeight);
    }

    public void setCenterIcon(int resId) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), resId);
        setCenterIcon(b);
    }

    public void setCenterIcon(Bitmap bitmap) {
        centerIconBitmap = BitmapUtil.toRoundBitmap(bitmap);
    }

    public void setActionListener(SlidingActionListener listener) {
        mListener = listener;
    }

    private enum ActionState {STANDBY, ANIMATE_DOWN, ANIMATE_UP, MOVING}
    private enum TouchArea {Center, Other}
    public interface SlidingActionListener {
        void onLeftEdgeReached();
        void onRightEdgeReached();
    }
}