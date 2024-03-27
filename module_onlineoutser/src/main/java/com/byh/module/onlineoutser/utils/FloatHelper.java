package com.byh.module.onlineoutser.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.byh.module.onlineoutser.R;
import com.byh.module.onlineoutser.im.MoveScaleView;

public class FloatHelper {

  static AppCompatActivity mActivity;
  static Fragment mFragment;
  static View view;

  //目前是将页面浮动在Activity，后期可兼容浮动App中
  public static void floatFragment(AppCompatActivity appCompatActivity, Fragment fragment) {

    toFloat(appCompatActivity, fragment);
  }

  private static void toFloat(AppCompatActivity activity, Fragment fragment) {
    mActivity = activity;
    mFragment = fragment;
    ViewGroup floatView = activity.findViewById(R.id.float_view_id);
    if (floatView == null) {
      return;
    }
    if (floatView.getChildCount() > 0)
      return;
    floatView.setVisibility(View.VISIBLE);
    view = LayoutInflater.from(activity).inflate(R.layout.float_layout, floatView, false);
    floatView.addView(view, new ViewGroup.LayoutParams(-1, -1));
    view.post(new Runnable() {
      @Override
      public void run() {
        setFloatMax(mActivity);
        view.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            setFloatMax(mActivity);
          }
        });

        mActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, mFragment)//悬浮view float_layout的布局
                .commitAllowingStateLoss();
      }
    });
  }

  public static void setFloatMin(AppCompatActivity activity) {

    @SuppressLint("CutPasteId")
    ViewGroup floatView = activity.getWindow().findViewById(R.id.float_view_id);
    if (floatView == null)
      return;
    MoveScaleView v = (MoveScaleView) floatView.getChildAt(0);
    if (v == null)
      return;
    v.setMin();
  }

  public static void setFloatMax(AppCompatActivity activity) {
    @SuppressLint("CutPasteId")
    ViewGroup floatView = activity.getWindow().findViewById(R.id.float_view_id);
    if (floatView == null)
      return;

    MoveScaleView v = (MoveScaleView) floatView.getChildAt(0);
    if (v == null)
      return;
    v.setMax();
  }

  public static void dismiss(AppCompatActivity activity) {
    if(null!=activity){
      ViewGroup floatView = activity.findViewById(R.id.float_view_id);
      if (floatView != null) {
        floatView.removeAllViews();
        floatView.setVisibility(View.GONE);
      }
    }
  }

}
