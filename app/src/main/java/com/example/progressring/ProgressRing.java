package com.example.progressring;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class ProgressRing extends View {
    private int progressStartColor; // 进度条起始颜色
    private int progressEndColor;   // 进度条结束颜色
    private int bgStartColor;       // 背景起始颜色
    private int bgMidColor;         // 背景中间颜色
    private int bgEndColor;         // 背景结束颜色
    private int progress;           // 目标进度 (0-100)
    private float progressWidth;    // 进度条宽度
    private int startAngle;         // 进度条起始角度
    private int sweepAngle;         // 进度条覆盖的角度
    private boolean showAnim;       // 是否启用动画
    private int curProgress = 0;    // 当前进度（用于动画）

    private int mMeasureHeight, mMeasureWidth; // View 的测量宽高
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG); // 背景画笔
    private Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG); // 进度画笔
    private RectF pRectF; // 绘制的矩形区域
    private float unitAngle; // 计算每 1% 进度对应的角度

    public ProgressRing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressRing);
        progressStartColor = ta.getColor(R.styleable.ProgressRing_pr_progress_start_color, Color.YELLOW);
        progressEndColor = ta.getColor(R.styleable.ProgressRing_pr_progress_end_color, progressStartColor);
        bgStartColor = ta.getColor(R.styleable.ProgressRing_pr_bg_start_color, Color.LTGRAY);
        bgMidColor = ta.getColor(R.styleable.ProgressRing_pr_bg_mid_color, bgStartColor);
        bgEndColor = ta.getColor(R.styleable.ProgressRing_pr_bg_end_color, bgStartColor);
        progress = ta.getInt(R.styleable.ProgressRing_pr_progress, 0);
        progressWidth = ta.getDimension(R.styleable.ProgressRing_pr_progress_width, 8f);
        startAngle = ta.getInt(R.styleable.ProgressRing_pr_start_angle, 150);
        sweepAngle = ta.getInt(R.styleable.ProgressRing_pr_sweep_angle, 240);
        showAnim = ta.getBoolean(R.styleable.ProgressRing_pr_show_anim, true);
        ta.recycle();

        unitAngle = (float) (sweepAngle / 100.0); // 计算每 1% 进度所对应的角度

        // 配置画笔
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setStrokeWidth(progressWidth);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(progressWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        mMeasureWidth = getMeasuredWidth();
        mMeasureHeight = getMeasuredHeight();
        if(pRectF == null) {
            float halfProgressWidth = progressWidth / 2;
            pRectF = new RectF(halfProgressWidth + getPaddingLeft(),
                    halfProgressWidth + getPaddingTop(),
                    mMeasureWidth - halfProgressWidth - getPaddingRight(),
                    mMeasureHeight - halfProgressWidth - getPaddingBottom());
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        //如果动画关闭直接绘制最终进度
        if(!showAnim){
            curProgress = progress;
        }
        drawBg(canvas);
        drawProgress(canvas);

        if(curProgress<progress){
            curProgress++;
            postInvalidate();
        }

    }

    private void drawBg(Canvas canvas) {
        float halfSweep = sweepAngle / 2;
        for(int i=sweepAngle, st=(int)(curProgress*unitAngle); i>st; i--) {
            if(i-sweepAngle > 0) {
                bgPaint.setColor(getGradient((i - halfSweep) / halfSweep, bgMidColor, bgEndColor));
            } else {
                bgPaint.setColor(getGradient((halfSweep - i) / halfSweep, bgMidColor, bgStartColor));
            }
            canvas.drawArc(pRectF, startAngle+i, 1, false, bgPaint);
        }

    }


    private void drawProgress(Canvas canvas) {
        for(int i=0, end=(int) (curProgress*unitAngle); i<=end; i++) {
            progressPaint.setColor(getGradient(i/(float)end, progressStartColor, progressEndColor));
            canvas.drawArc(pRectF, startAngle+i, 1, false, progressPaint);
        }
    }

    private int getGradient(float fraction, int startColor, int endColor) {
        if(fraction > 1) fraction = 1;
        int alphaStart = Color.alpha(startColor);
        int redStart = Color.red(startColor);
        int blueStart = Color.blue(startColor);
        int greenStart = Color.green(startColor);
        int alphaEnd = Color.alpha(endColor);
        int redEnd = Color.red(endColor);
        int blueEnd = Color.blue(endColor);
        int greenEnd = Color.green(endColor);

        return Color.argb(
                (int) (alphaStart + fraction * (alphaEnd-alphaStart)),
                (int) (redStart + fraction * (redEnd - redStart)),
                (int) (greenStart + fraction * (greenEnd - greenStart)),
                (int) (blueStart + fraction * (blueEnd - blueStart))
        );
    }
}