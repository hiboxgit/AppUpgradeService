package com.lachesis.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.InputFilter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lachesis.common.R;


/**
 * 标准对话框
 * <p>
 * 如果想要只显示一个按钮，只setLeftButton就可以了
 * <p>
 * 使用示例：
 * SimpleDialog simpleDialog = new SimpleDialog(this);
 * simpleDialog.setTitle("内存卡提示")
 * .setText("内存卡空间不足，无法录像，是否需要格式内存卡？")
 * .setLeftButton(null)
 * .setRightButton("格式化", new SimpleDialog.OnClickListener() {
 *
 * @Override public void onClick(Dialog dialog) {
 * // do something
 * // ...
 * dialog.dismiss();
 * }
 * })
 * .show();
 * <p>
 * Created by zhongweiguang on 2016/12/3.
 */

public class SimpleDialog extends Dialog {

    private TextView titleTv;
    private TextView textTv;
    private EditText editText;
    private Button leftBtn;
    private Button rightBtn;
    private ImageView dividerHorizontal;
    private ImageView dividerVertical;
    private ListView listview;

    private OnButtonClickListener leftBtnOnClickListener;
    private OnButtonClickListener rightBtnOnClickListener;
    private OnItemClickListener mOnItemClickListener;

    public SimpleDialog(Context context) {
        super(context, R.style.SimpleDialog);
        initView(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int width = getContext().getResources().getDimensionPixelOffset(R.dimen.simple_dialog_width);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = width;
        getWindow().setAttributes(layoutParams);
    }


    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.simple_dialog, null);

        setContentView(view);
        setCanceledOnTouchOutside(false);
        setCancelable(false);

        titleTv = (TextView) findViewById(R.id.titleTv);
        textTv = (TextView) findViewById(R.id.textView);
        editText = (EditText) findViewById(R.id.editText);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        rightBtn = (Button) findViewById(R.id.rightBtn);
        dividerHorizontal = (ImageView) findViewById(R.id.dividerHorizontal);
        dividerVertical = (ImageView) findViewById(R.id.dividerVertical);
        listview = (ListView) findViewById(R.id.listview);
        textTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        setButtonBg(context.getResources().getDrawable(R.drawable.upgrade_button_selector));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position, view);
                }
            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leftBtnOnClickListener != null) {
                    leftBtnOnClickListener.onClick(SimpleDialog.this);
                } else {
                    dismiss();
                }
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightBtnOnClickListener != null) {
                    rightBtnOnClickListener.onClick(SimpleDialog.this);
                } else {
                    dismiss();
                }
            }
        });
    }

    public SimpleDialog setTitle(String title) {
        titleTv.setText(title);
        titleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setTitleRes(@StringRes int resId) {
        titleTv.setText(resId);
        titleTv.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setText(String text) {
        textTv.setText(text);
        textTv.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setTextRes(@StringRes int resId) {
        textTv.setText(resId);
        textTv.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 设置text内容居中显示
     */
    public SimpleDialog setTextCenter() {
        textTv.setGravity(Gravity.CENTER);
        return this;
    }

    public EditText getInputTextView() {
        return editText;
    }

    /**
     * 获取输入的内容
     */
    public String getInputText() {
        return editText.getText().toString();
    }

    /**
     * @see TextView#setInputType(int)
     */
    public SimpleDialog setInputType(int type) {
        editText.setInputType(type);
        return this;
    }

    public SimpleDialog setInputMaxLength(int maxLength) {
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 设置输入框显示的内容
     */
    public SimpleDialog setInputText(String text) {
        editText.setText(text);
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setInputTextRes(@StringRes int resId) {
        editText.setText(resId);
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 设置输入框的提示内容
     */
    public SimpleDialog setInputHint(String hint) {
        editText.setHint(hint);
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setInputHintRes(@StringRes int resId) {
        editText.setHint(resId);
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 显示输入框
     */
    public SimpleDialog showInputTextView() {
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setListViewAdapter(BaseAdapter adapter) {
        listview.setVisibility(View.VISIBLE);
        listview.setAdapter(adapter);
        return this;
    }

    public SimpleDialog setOnItemClicListener(OnItemClickListener onItemClicListener) {
        mOnItemClickListener = onItemClicListener;
        return this;
    }

    /**
     * 会显示布局中默认的文字“取消”
     *
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setLeftButton(OnButtonClickListener listener) {
        leftBtnOnClickListener = listener;

        leftBtn.setVisibility(View.VISIBLE);
        dividerHorizontal.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setLeftButton(String text, OnButtonClickListener listener) {
        leftBtnOnClickListener = listener;

        leftBtn.setText(text);
        leftBtn.setVisibility(View.VISIBLE);
        dividerHorizontal.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setLeftButton(@StringRes int resId, OnButtonClickListener listener) {
        leftBtnOnClickListener = listener;
        leftBtn.setText(resId);
        leftBtn.setVisibility(View.VISIBLE);
        dividerHorizontal.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 会显示布局中默认的文字“确定”
     *
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setRightButton(OnButtonClickListener listener) {
        rightBtnOnClickListener = listener;

        rightBtn.setVisibility(View.VISIBLE);
        dividerVertical.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setRightButton(String text, OnButtonClickListener listener) {
        rightBtnOnClickListener = listener;

        rightBtn.setText(text);
        rightBtn.setVisibility(View.VISIBLE);
        dividerVertical.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * @param listener 设置null时，点击会自动dismiss()
     */
    public SimpleDialog setRightButton(@StringRes int resId, OnButtonClickListener listener) {
        rightBtnOnClickListener = listener;

        rightBtn.setText(resId);
        rightBtn.setVisibility(View.VISIBLE);
        dividerVertical.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setBackground(int color){

        return this;
    }

    public SimpleDialog setTitleTextColor(int color){
        titleTv.setTextColor(color);
        return this;
    }

    public SimpleDialog setContentTextColor(int color){
        textTv.setTextColor(color);
        return this;
    }

    public SimpleDialog setLeftBtnTextColor(int color){
        leftBtn.setTextColor(color);
        return this;
    }
    public SimpleDialog setRightBtnTextColor(int color){
        rightBtn.setTextColor(color);
        return this;
    }

    public SimpleDialog updateLeftButtonText(String text) {
        leftBtn.setText(text);
        return this;
    }

    public SimpleDialog updateRightButtonText(String text) {
        rightBtn.setText(text);
        return this;
    }

    public SimpleDialog setInputTextColor(int color){
        editText.setTextColor(color);
        editText.setVisibility(View.VISIBLE);
        return this;
    }

    public SimpleDialog setButtonBg(Drawable drawable){
        leftBtn.setBackground(drawable);
        rightBtn.setBackground(drawable);
        return this;
    }

    public Button getLeftBtn() {
        return leftBtn;
    }

    public Button getRightBtn() {
        return rightBtn;
    }

    public TextView getTextTv() {
        return textTv;
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public interface OnButtonClickListener {

        void onClick(Dialog dialog);

    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
}
