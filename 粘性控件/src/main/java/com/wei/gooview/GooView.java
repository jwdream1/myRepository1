package com.wei.gooview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.wei.gooview.util.GeometryUtil;
import com.wei.gooview.util.Utils;


/**
 * 粘性控件
 * @author poplar
 *
 */
public class GooView extends View {
	
	private OnUpdateListener onUpdateListener;
	
	public OnUpdateListener getOnUpdateListener() {
		return onUpdateListener;
	}

	public void setOnUpdateListener(OnUpdateListener onUpdateListener) {
		this.onUpdateListener = onUpdateListener;
	}

	public interface OnUpdateListener{
		
		void onDisappear();
		
		void onReset(boolean isOutOfRange);
	}

	private Paint paint;

	public GooView(Context context) {
		this(context, null);
	}

	public GooView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GooView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(18f);
		textPaint.setTextAlign(Align.CENTER);
	}
	// 拖拽圆圆心
	PointF mDragCenter = new PointF(90f, 90f);
	float mDragRadius = 16f;
	// 固定圆圆心
	PointF mStickCenter = new PointF(150f, 150f);
	float mStickRadius = 12f;
	
	// 固定圆的两个附着点坐标
	PointF[] mStickPoints = new PointF[]{
		new PointF(250f, 250f),	
		new PointF(250f, 350f)	
	};
	// 控制点坐标
	PointF mControlPoint = new PointF(150f, 300f);
	
	// 拖拽圆的两个附着点坐标
	PointF[] mDragPoints = new PointF[]{
		new PointF(50f, 250f),	
		new PointF(50f, 350f)	
	};
	private int statusBarHeight;
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		float yOffset = mStickCenter.y - mDragCenter.y;
		float xOffset = mStickCenter.x - mDragCenter.x;
		Double lineK = null;
		if(xOffset != 0){
			lineK = (double) (yOffset / xOffset);
		}

		// 根据两圆圆心的距离, 计算出固定圆的半径
		float tempStickRadius = computeStickRadius();
		
		// 计算四个附着点坐标
		mDragPoints = GeometryUtil.getIntersectionPoints(mDragCenter, mDragRadius, lineK);
		mStickPoints = GeometryUtil.getIntersectionPoints(mStickCenter, tempStickRadius, lineK);
		
		// 一个控制点坐标
		mControlPoint = GeometryUtil.getMiddlePoint(mDragCenter, mStickCenter);
		
		// 向上平移画布
		canvas.save();
		canvas.translate(0, -statusBarHeight);
		
		// 画出最大范围(参考使用)
		paint.setStyle(Style.STROKE);
		canvas.drawCircle(mStickCenter.x, mStickCenter.y, farestDistance, paint);
		paint.setStyle(Style.FILL);
		
		if(!isDisappear){
			// 没有消失的时候, 才绘制内容
			
			if(!isOutOfRange){
				// 画出附着点(参考使用)
				paint.setColor(Color.BLUE);
				canvas.drawCircle(mDragPoints[0].x, mDragPoints[0].y, 3f, paint);
				canvas.drawCircle(mDragPoints[1].x, mDragPoints[1].y, 3f, paint);
				canvas.drawCircle(mStickPoints[0].x, mStickPoints[0].y, 3f, paint);
				canvas.drawCircle(mStickPoints[1].x, mStickPoints[1].y, 3f, paint);
				paint.setColor(Color.RED);
				
				// 画连接部分
				Path path = new Path();
				// 跳到点1
				path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
				// 从  点1->点2  画曲线
				path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
				// 从 点2->点3 画直线
				path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
				// 从  点3->点4  画曲线
				path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x,  mStickPoints[1].y);
				path.close();
				canvas.drawPath(path, paint);
				
				// 画固定圆
				canvas.drawCircle(mStickCenter.x, mStickCenter.y, tempStickRadius, paint);
			}
			
			// 画拖拽圆
			canvas.drawCircle(mDragCenter.x, mDragCenter.y, mDragRadius, paint);
			canvas.drawText("86", mDragCenter.x, mDragCenter.y + mDragRadius / 2, textPaint);
		}
		
		canvas.restore();
	}
	
	float farestDistance = 80f;
	// 是否超出范围
	private boolean isOutOfRange = false;
	// 是否消失
	private boolean isDisappear = false;
	private Paint textPaint;
	/**
	 * 根据两圆圆心的距离, 计算出固定圆的半径
	 * @return
	 */
	private float computeStickRadius() {
		
		// 0f -> 80f
		float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
		
		// 如果distance大于 farestDistance, 则取farestDistance
		distance = Math.min(distance, farestDistance);
		
		float percent = distance / farestDistance;
		
		System.out.println("percent: " + percent);
		
		// 0.0 -> 1.0
		// 12f -> 3f
		return evaluate(percent, mStickRadius, mStickRadius * 0.25f);
	}
    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x;
		float y;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			x = event.getRawX();
			y = event.getRawY();
			//按下时判断是否在范围外,如果在范围之外,则不处理
			mDragCenter.set(x,y);
			float distance = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
			if(distance>farestDistance){
				return false;
			}
			updateDragCenter(x, y);

			isOutOfRange = false;
			isDisappear = false;

			break;
		case MotionEvent.ACTION_MOVE:
			x = event.getRawX();
			y = event.getRawY();
			updateDragCenter(x, y);
			
			// 超出范围, 断开
			float d = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
			if(d > farestDistance){
				isOutOfRange = true;
				invalidate();
			}
			
			break;
		case MotionEvent.ACTION_UP:
			if(isOutOfRange){
				// 刚刚超出了范围

				float dis = GeometryUtil.getDistanceBetween2Points(mDragCenter, mStickCenter);
				if(dis > farestDistance){
					//- 超出范围, 断开, 松手, 消失
					isDisappear = true;
					invalidate();
					
					if(onUpdateListener != null){
						onUpdateListener.onDisappear();
					}
				}else {
					// - 超出范围, 断开, 又放回去了, 恢复
					updateDragCenter(mStickCenter.x, mStickCenter.y);
					if(onUpdateListener != null){
						onUpdateListener.onReset(true);
					}
					
				}
			} else {
//				- 没有超出范围, 松手, 回弹, 恢复
				final PointF startP = new PointF(mDragCenter.x, mDragCenter.y);
				
				ValueAnimator animator = ValueAnimator.ofFloat(1.0f);
				animator.addUpdateListener(new AnimatorUpdateListener() {
					
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// 0.0f -> 1.0f
						float animatedFraction = animation.getAnimatedFraction();
						System.out.println("animatedFraction: " + animatedFraction);
						
						// 计算从开始点startP到结束点mStickCenter之间的所有点
						PointF p = GeometryUtil.getPointByPercent(startP, mStickCenter, animatedFraction);
						updateDragCenter(p.x, p.y);
					}
				});
				animator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						if(onUpdateListener != null){
							onUpdateListener.onReset(false);
						}
					}
				});
				
				animator.setInterpolator(new OvershootInterpolator(4));
				animator.setDuration(500);
				animator.start();
			}
			
			break;

		default:
			break;
		}
		
		return true;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		statusBarHeight = Utils.getStatusBarHeight(this);
	}

	private void updateDragCenter(float x, float y) {
		mDragCenter.set(x, y);
		invalidate();
	}
}











