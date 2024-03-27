package com.byh.module.onlineoutser.im;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class MoveScaleView extends LinearLayout {

    //data
    private int touchSlop;

    //data
    private boolean hasMovingLong = false; //判断时候移动很大，如果移动过，即使回到原位，也不会触发点击事件

    private OnClickListener clickListener;

    private boolean canMove;

    private float scaleRatio = 0.3f;

    private View parentView;
    private float defaultX = 0;
    private float defaultY = 0;
    private float lastX = 0;
    private float lastY = 0;

    public MoveScaleView(Context context) {
        this(context, null);
    }

    public MoveScaleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MoveScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        //   this.setBackgroundColor(Color.BLACK);
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (parentView == null)
            parentView = (View) getParent();

        if (!canMove)
            return true;
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hasMovingLong = false;
                    defaultX = getX();
                    defaultY = getY();
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(lastX - event.getRawX()) < touchSlop
                            && Math.abs(lastY - event.getRawY()) < touchSlop) {
                        return true;
                    } else {
                        hasMovingLong = true;
                    }

                    setX(defaultX + event.getRawX() - lastX);

                    float toY = defaultY + event.getRawY() - lastY;
                    if (toY > 0 && toY < parentView.getMeasuredHeight() - getMeasuredHeight())
                        setY(toY);
                    break;

                case MotionEvent.ACTION_UP:
                    //如果移动距离很小，认为是点击
                    if (!hasMovingLong && getDistance(defaultX - getX(), defaultY - getY()) < 3) {
                        if (clickListener != null) {
                            clickListener.onClick(MoveScaleView.this);
                        }
                    }
                    autoSide();
                    break;
            }
        }
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        clickListener = l;
    }

    public void closeMove() {
        canMove = false;
    }

    public void openMove() {
        canMove = true;
    }

    private int getDistance(float xDiff, float yDiff) {
        float all = (float) (Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
        return (int) Math.sqrt(all);

    }

    public void setMax() {
        setX(0);
        setY(0);
        closeMove();
        View rootView = (View) getParent();
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = rootView.getMeasuredWidth();
        params.height = rootView.getMeasuredHeight();
        setLayoutParams(params);
    }

    public void setMin() {
        setX(0);
        setY(0);
        openMove();
        View rootView = (View) getParent();
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = (int) (rootView.getMeasuredWidth() * scaleRatio);
        params.height = (int) (rootView.getMeasuredHeight() * scaleRatio);
        setLayoutParams(params);
    }

    //自动停靠
    private void autoSide() {
        //Max下不用停靠
        if (!canMove)
            return;
        View rootView = (View) getParent();
        float maxWidth = rootView.getMeasuredWidth();
        float preX = getX() > maxWidth / 2 ? maxWidth - getMeasuredWidth() : 0;
        ObjectAnimator.ofFloat(this, "translationX", getX(), preX)
                .setDuration(300)
                .start();
    }
}
