package com.luck.picture.lib.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.R;
import com.luck.picture.lib.camera.listener.CaptureListener;
import com.luck.picture.lib.camera.listener.ClickListener;
import com.luck.picture.lib.camera.listener.TypeListener;
import com.luck.picture.lib.tools.DoubleUtils;
import com.luck.picture.lib.tools.ScreenUtils;

import static com.luck.picture.lib.camera.CustomCameraView.BUTTON_STATE_ONLY_CAPTURE;
import static com.luck.picture.lib.camera.CustomCameraView.BUTTON_STATE_ONLY_RECORDER;

/**
 * =====================================
 * 作    者: 陈嘉桐 445263848@qq.com
 * 版    本：1.0.4
 * 创建日期：2017/4/26
 * 描    述：集成各个控件的布局
 * =====================================
 */

public class CaptureLayout extends FrameLayout {

    private CaptureListener captureListener;    //拍照按钮监听
    private TypeListener typeListener;          //拍照或录制后接结果按钮监听
    private ClickListener leftClickListener;    //左边按钮监听
    private ClickListener rightClickListener;   //右边按钮监听

    public void setTypeListener(TypeListener typeListener) {
        this.typeListener = typeListener;
    }

    public void setCaptureListener(CaptureListener captureListener) {
        this.captureListener = captureListener;
    }

    private CaptureButton btn_capture;      //拍照按钮
    private TypeButton btn_confirm;         //确认按钮
    private TypeButton btn_cancel;          //取消按钮
    private ReturnButton btn_return;        //返回按钮
    private ImageView iv_custom_left;            //左边自定义按钮
    private ImageView iv_custom_right;            //右边自定义按钮
    private TextView txt_tip;               //提示文本
    private TextView txt_rephoto;           //重拍
    private TextView txt_use;               //使用照片
    private TextView txt_bottom;            //底部文字

    private int layout_width;
    private int layout_height;
    private int button_size;
    private int iconLeft = 0;
    private int iconRight = 0;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout_width = outMetrics.widthPixels;
        } else {
            layout_width = outMetrics.widthPixels / 2;
        }
        button_size = (int) (layout_width / 4.5f);
        layout_height = button_size + (button_size / 5) * 2 + 40;

        initView();
        initEvent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(layout_width, layout_height);
    }

    public void initEvent() {
        //默认TypeButton为隐藏
        iv_custom_right.setVisibility(GONE);
        btn_cancel.setVisibility(GONE);
        btn_confirm.setVisibility(GONE);
    }

    public void startTypeBtnAnimator() {
        //拍照录制结果后的动画
        if (this.iconLeft != 0)
            iv_custom_left.setVisibility(GONE);
        else
            btn_return.setVisibility(GONE);
        if (this.iconRight != 0)
            iv_custom_right.setVisibility(GONE);

//        txt_rephoto.setVisibility(VISIBLE);
//        txt_use.setVisibility(VISIBLE);

        btn_capture.setVisibility(GONE);
        txt_rephoto.setVisibility(VISIBLE);
        txt_use.setVisibility(VISIBLE);
//        btn_cancel.setVisibility(VISIBLE);
        btn_cancel.setVisibility(GONE);
//        btn_confirm.setVisibility(VISIBLE);
        btn_confirm.setVisibility(GONE);
        btn_cancel.setClickable(false);
        btn_confirm.setClickable(false);
        iv_custom_left.setVisibility(GONE);
        txt_bottom.setVisibility(GONE);

        layout_height = button_size/3;
        ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4, 0);
        ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator_cancel, animator_confirm);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                btn_cancel.setClickable(true);
                btn_confirm.setClickable(true);
            }
        });
        set.setDuration(500);
        set.start();
    }


    private void initView() {
        setWillNotDraw(false);
        //拍照按钮
        btn_capture = new CaptureButton(getContext(), button_size);
        FrameLayout.LayoutParams btn_capture_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        btn_capture_param.setMargins(0,0,0,ScreenUtils.dip2px(getContext(),20));
        btn_capture_param.gravity = Gravity.CENTER;
        btn_capture.setLayoutParams(btn_capture_param);
        btn_capture.setCaptureListener(new CaptureListener() {
            @Override
            public void takePictures() {
                if (captureListener != null) {
                    captureListener.takePictures();
                }
                startAlphaAnimation();
            }

            @Override
            public void recordShort(long time) {
                if (captureListener != null) {
                    captureListener.recordShort(time);
                }
            }

            @Override
            public void recordStart() {
                if (captureListener != null) {
                    captureListener.recordStart();
                }
                startAlphaAnimation();
            }

            @Override
            public void recordEnd(long time) {
                if (captureListener != null) {
                    captureListener.recordEnd(time);
                }
                startTypeBtnAnimator();
            }

            @Override
            public void recordZoom(float zoom) {
                if (captureListener != null) {
                    captureListener.recordZoom(zoom);
                }
            }

            @Override
            public void recordError() {
                if (captureListener != null) {
                    captureListener.recordError();
                }
            }
        });

        txt_bottom = new TextView(getContext());
        LayoutParams txt_bottom_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        txt_bottom_param.gravity = Gravity.BOTTOM;
        txt_bottom_param.setMargins(0, 0, 0, 0);
        txt_bottom.setText("拍照");
        txt_bottom.setTextColor(0xFFFFFFFF);
        txt_bottom.setGravity(Gravity.CENTER_HORIZONTAL);
        txt_bottom.setLayoutParams(txt_bottom_param);
        txt_bottom.setVisibility(VISIBLE);

        //重拍
        txt_rephoto = new TextView(getContext());
        LayoutParams rephoto_param = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rephoto_param.gravity = Gravity.LEFT;
        rephoto_param.setMargins(ScreenUtils.dip2px(getContext(),16), 0, 0, ScreenUtils.dip2px(getContext(),20));
        txt_rephoto.setText("重拍");
        txt_rephoto.setTextColor(0xFFFFFFFF);
        txt_rephoto.setLayoutParams(rephoto_param);
//        txt_rephoto.setPadding(ScreenUtils.dip2px(getContext(),16),0,0,ScreenUtils.dip2px(getContext(),20));
        txt_rephoto.setGravity(Gravity.CENTER_VERTICAL);
        txt_rephoto.setVisibility(GONE);
        txt_rephoto.setOnClickListener(v -> {
            if (typeListener != null) {
                typeListener.cancel();
            }
        });

        //使用图片
        txt_use = new TextView(getContext());
        LayoutParams use_param = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        use_param.gravity = Gravity.RIGHT;
        use_param.setMargins(0, 0, ScreenUtils.dip2px(getContext(),16), ScreenUtils.dip2px(getContext(),20));
        txt_use.setText("使用图片");
        txt_use.setTextColor(Color.parseColor("#2065FF"));
        txt_use.setLayoutParams(use_param);
//        txt_use.setPadding(0,0, ScreenUtils.dip2px(getContext(),16),ScreenUtils.dip2px(getContext(),20));
        txt_use.setGravity(Gravity.CENTER_VERTICAL);
        txt_use.setVisibility(GONE);
        txt_use.setOnClickListener(v -> {
            if (typeListener != null) {
                typeListener.confirm();
            }
        });

        //取消按钮
        btn_cancel = new TypeButton(getContext(), TypeButton.TYPE_CANCEL, button_size);
        final LayoutParams btn_cancel_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL;
        btn_cancel_param.setMargins((layout_width / 4) - button_size / 2, 0, 0, 0);
        btn_cancel.setLayoutParams(btn_cancel_param);
        btn_cancel.setOnClickListener(view -> {
            if (typeListener != null) {
                typeListener.cancel();
            }
        });

        //确认按钮
        btn_confirm = new TypeButton(getContext(), TypeButton.TYPE_CONFIRM, button_size);
        LayoutParams btn_confirm_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        btn_confirm_param.setMargins(0, 0, (layout_width / 4) - button_size / 2, 0);
        btn_confirm.setLayoutParams(btn_confirm_param);
        btn_confirm.setOnClickListener(view -> {
            if (typeListener != null) {
                typeListener.confirm();
            }
        });

        //返回按钮
        btn_return = new ReturnButton(getContext(), (int) (button_size / 2.5f));
        LayoutParams btn_return_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btn_return_param.gravity = Gravity.CENTER_VERTICAL;
        btn_return_param.setMargins(layout_width / 6, 0, 0, 0);
        btn_return.setLayoutParams(btn_return_param);
        btn_return.setOnClickListener(v -> {
            if (leftClickListener != null) {
                leftClickListener.onClick();
            }
        });
        btn_return.setVisibility(GONE);
        //左边自定义按钮
        iv_custom_left = new ImageView(getContext());
        LayoutParams iv_custom_param_left = new LayoutParams((int) (button_size / 2.5f), (int) (button_size / 2.5f));
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL;
        iv_custom_param_left.setMargins(layout_width / 6, 0, 0, 0);
        iv_custom_left.setLayoutParams(iv_custom_param_left);
        iv_custom_left.setOnClickListener(v -> {
            if (leftClickListener != null) {
                leftClickListener.onClick();
            }
        });

        //右边自定义按钮
        iv_custom_right = new ImageView(getContext());
        LayoutParams iv_custom_param_right = new LayoutParams((int) (button_size / 2.5f), (int) (button_size / 2.5f));
        iv_custom_param_right.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        iv_custom_param_right.setMargins(0, 0, layout_width / 6, 0);
        iv_custom_right.setLayoutParams(iv_custom_param_right);
        iv_custom_right.setOnClickListener(v -> {
            if (rightClickListener != null) {
                rightClickListener.onClick();
            }
        });

        txt_tip = new TextView(getContext());
        LayoutParams txt_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        txt_param.gravity = Gravity.CENTER_HORIZONTAL;
        txt_param.setMargins(0, 0, 0, 0);

        txt_tip.setText(getCaptureTip());

        txt_tip.setTextColor(0xFFFFFFFF);
        txt_tip.setGravity(Gravity.CENTER);
        txt_tip.setLayoutParams(txt_param);
        txt_tip.setVisibility(GONE);

        this.addView(btn_capture);
        this.addView(btn_cancel);
        this.addView(btn_confirm);
        this.addView(btn_return);
        this.addView(iv_custom_left);
        this.addView(iv_custom_right);
        this.addView(txt_tip);
        this.addView(txt_rephoto);
        this.addView(txt_use);
        this.addView(txt_bottom);

    }

    private String getCaptureTip() {
        int buttonFeatures = btn_capture.getButtonFeatures();
        switch (buttonFeatures) {
            case BUTTON_STATE_ONLY_CAPTURE:
                return getContext().getString(R.string.picture_photo_pictures);
            case BUTTON_STATE_ONLY_RECORDER:
                return getContext().getString(R.string.picture_photo_recording);
            default:
                return getContext().getString(R.string.picture_photo_camera);
        }
    }

    public void resetCaptureLayout() {
        btn_capture.resetState();
        button_size = (int) (layout_width / 4.5f);
        layout_height = button_size + (button_size / 5) * 2 + 40;
        btn_cancel.setVisibility(GONE);
        btn_confirm.setVisibility(GONE);
        btn_capture.setVisibility(VISIBLE);
        txt_bottom.setVisibility(VISIBLE);
        txt_rephoto.setVisibility(GONE);
        txt_use.setVisibility(GONE);
        txt_tip.setText(getCaptureTip());
//        txt_tip.setVisibility(View.VISIBLE);
        if (this.iconLeft != 0)
            iv_custom_left.setVisibility(VISIBLE);
        else
//            btn_return.setVisibility(VISIBLE);
            btn_return.setVisibility(GONE);
        if (this.iconRight != 0)
            iv_custom_right.setVisibility(VISIBLE);
    }


    public void startAlphaAnimation() {
        txt_tip.setVisibility(View.GONE);
    }

    public void setTextWithAnimation(String tip) {
        txt_tip.setText(tip);
        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0f);
        animator_txt_tip.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                txt_tip.setText(getCaptureTip());
                txt_tip.setAlpha(1f);
            }
        });
        animator_txt_tip.setDuration(2500);
        animator_txt_tip.start();
    }

    public void setDuration(int duration) {
        btn_capture.setDuration(duration);
    }

    public void setMinDuration(int duration) {
        btn_capture.setMinDuration(duration);
    }

    public void setButtonFeatures(int state) {
        btn_capture.setButtonFeatures(state);
        txt_tip.setText(getCaptureTip());
    }

    public void setTip(String tip) {
        txt_tip.setText(tip);
    }

    public void showTip() {
        txt_tip.setVisibility(GONE);
    }

    public void setIconSrc(int iconLeft, int iconRight) {
        this.iconLeft = iconLeft;
        this.iconRight = iconRight;
        if (this.iconLeft != 0) {
            iv_custom_left.setImageResource(iconLeft);
            iv_custom_left.setVisibility(VISIBLE);
            btn_return.setVisibility(GONE);
        } else {
            iv_custom_left.setVisibility(GONE);
//            btn_return.setVisibility(VISIBLE);
            btn_return.setVisibility(GONE);
        }
        if (this.iconRight != 0) {
            iv_custom_right.setImageResource(iconRight);
            iv_custom_right.setVisibility(VISIBLE);
        } else {
            iv_custom_right.setVisibility(GONE);
        }
    }

    public void setLeftClickListener(ClickListener leftClickListener) {
        this.leftClickListener = leftClickListener;
    }

    public void setRightClickListener(ClickListener rightClickListener) {
        this.rightClickListener = rightClickListener;
    }
}