package com.dudu.upgrade.fota.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.dudu.upgrade.R;

/**
 * 圆形加载进度条
 *
 * @author brave
 */
public class CommonCircleView extends View {

    private final Paint paint;
    private final Context mContext;
    private Resources res;
    private int max = 100;
    private int progress = 0;
    private int ringWidth;
    // 圆环的颜色
    private int ringColor;
    // 进度条颜色
    private int progressColor;

    private String textProgress;

    //控件宽度
    private int mWidth;
    //控制半经长度
    private int mDiameter;

    private Animation operatingAnim;

    public CommonCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        this.paint = new Paint();
        this.res = context.getResources();
        this.paint.setAntiAlias(true); // 消除锯齿

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.iot_custom_view);

        this.ringColor = a.getColor(R.styleable.iot_custom_view_iot_ringColor,
                Color.WHITE);// 背景
        this.progressColor = a.getColor(R.styleable.iot_custom_view_iot_progressColor,
                Color.WHITE);// 绿色进度条*/
        this.ringWidth = dip2px(context, a.getInt(R.styleable.iot_custom_view_iot_integerSize, 0));
        this.mDiameter = dip2px(context, a.getInt(R.styleable.iot_custom_view_iot_diameter, 0));
        this.progress = a.getInt(R.styleable.iot_custom_view_iot_progressSize, 0);

        a.recycle();

    }

    public CommonCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonCircleView(Context context) {
        this(context, null);
    }


    /**
     * @param animXml The anim defined in xml
     * */
    public void loadXmlAnim(int animXml){
        operatingAnim = AnimationUtils.loadAnimation(mContext, animXml);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
    }

    public void startAnim(){
        if (operatingAnim != null) {
            this.clearAnimation();
            this.startAnimation(operatingAnim);
        }
    }

    public void stopAnim(){
        if (operatingAnim != null) {
            this.clearAnimation();
        }
    }

    /**
     * 设置进度条最大值
     *
     * @param max
     */
    public synchronized void setMax(int max) {
        if (max < 0)
            max = 0;
        if (progress > max)
            progress = max;
        this.max = max;
    }

    /**
     * 获取进度条最大值
     *
     * @return
     */
    public synchronized int getMax() {
        return max;
    }

    /**
     * 设置加载进度，取值范围在0~之间
     *
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if (progress >= 0 && progress <= max) {
            this.progress = progress;
            invalidate();
        }
    }

    /**
     * 获取当前进度值
     *
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }

    /**
     * 设置圆环背景色
     *
     * @param ringColor
     */
    public void setRingColor(int ringColor) {
        this.ringColor = res.getColor(ringColor);
    }

    /**
     * 设置进度条颜色
     *
     * @param progressColor
     */
    public void setProgressColor(int progressColor) {
        this.progressColor = res.getColor(progressColor);
    }

    /**
     * 设置圆环半径
     *
     * @param ringWidth
     */
    public void setRingWidthDip(int ringWidth) {
        this.ringWidth = dip2px(mContext, ringWidth);
    }

    /**
     * 通过不断画弧的方式更新界面，实现进度增加
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int center = this.getWidth() / 2;
        int radios = center - ringWidth / 2;

        // 绘制圆环
        this.paint.setStyle(Paint.Style.STROKE); // 绘制空心圆
        this.paint.setColor(ringColor);
        this.paint.setStrokeWidth(ringWidth);
        canvas.drawCircle(center, center, radios, this.paint);
        RectF oval = new RectF(center - radios, center - radios, center
                + radios, center + radios);
        this.paint.setColor(progressColor);


        //画有弧度的
        if (progress > 0) {
            this.paint.setAntiAlias(true);
            this.paint.setDither(true);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeJoin(Paint.Join.ROUND);
            this.paint.setStrokeCap(Paint.Cap.ROUND)
            ;
            canvas.drawArc(oval, 270, (360 * progress / max), false, paint); //逆时针转为负
        }

        super.onDraw(canvas);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
