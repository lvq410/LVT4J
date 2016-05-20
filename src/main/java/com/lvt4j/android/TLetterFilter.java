package com.lvt4j.android;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class TLetterFilter extends TextView {
	private TextView show;
	private WindowManager.LayoutParams showlp = new WindowManager.LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.TYPE_APPLICATION,
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			PixelFormat.TRANSLUCENT);
	private boolean isShowing;
	private WindowManager windowManager;
	private OnFilterListener onFilterListener;
	private float height;
	private char curFilterChar = '0';

	private int bgColor = 0;
	private int highLightColor = 0;

	public TLetterFilter(Context context) {
		this(context, null, 0);
	}

	public TLetterFilter(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TLetterFilter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setText("#\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\nR\nS\nT\nU\nV\nW\nX\nY\nZ");
		setGravity(Gravity.CENTER_HORIZONTAL);
		show = new TextView(context);
		show.setTextSize(55);
		show.setGravity(Gravity.CENTER);
		show.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		showlp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
				| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
		isShowing = false;
	}

	@Override
	public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);
		this.bgColor = color;
	}

	public void setShowDrawableResource(int resid) {
		show.setBackgroundResource(resid);
	}

	@Override
	public void setHighlightColor(int color) {
		super.setHighlightColor(color);
		highLightColor = color;
	}

	public TextView getShow() {
		return show;
	}

	public void setOnFliterListener(OnFilterListener onFilterListener) {
		this.onFilterListener = onFilterListener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isShowing) {
			isShowing = true;
			 windowManager.addView(show, showlp);
		}
		super.setBackgroundColor(highLightColor);
		float dim = height / 27;
		char filterChar = (char) ('A' + event.getY() / dim - 1);
		if (filterChar < 'A') {
			filterChar = '#';
		}
		if (filterChar > 'Z') {
			filterChar = 'Z';
		}
		if (filterChar != curFilterChar) {
			setFilterChar(filterChar);
		}
		if (event.getAction() == MotionEvent.ACTION_UP
				|| event.getAction() == MotionEvent.ACTION_CANCEL) {
			if (onFilterListener != null) {
				onFilterListener.onEndFilter(curFilterChar);
			}
			hideShow();
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int width = right - left;
		setTextSize(width--);
		height = bottom - top;
		FontMetrics fm = getPaint().getFontMetrics();
		float textHeight = (float) (Math.ceil(fm.descent - fm.ascent) + 2);
		while (textHeight * 27 > height) {
			setTextSize(width--);
			if (width < 0)
				break;
			fm = getPaint().getFontMetrics();
			textHeight = (float) (Math.ceil(fm.descent - fm.ascent) + 2);
		}
		float lineSpaceCount = (float) (height - (textHeight - 1.8) * 27);
		setLineSpacing(lineSpaceCount / 26, 1);
		super.onLayout(changed, left, top, right, bottom);
	}

	private void setFilterChar(char filterChar) {
		curFilterChar = filterChar;
		show.setText(String.valueOf(curFilterChar));
		if (onFilterListener != null) {
			onFilterListener.onFilter(curFilterChar);
		}
	}

	public void hideShow() {
		isShowing = false;
		 windowManager.removeView(show);
		super.setBackgroundColor(bgColor);
	}

	public static interface OnFilterListener {
		public abstract void onFilter(char filterChar);

		public abstract void onEndFilter(char filterChar);
	}
}
