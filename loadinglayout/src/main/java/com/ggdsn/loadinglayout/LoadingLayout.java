package com.ggdsn.loadinglayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 该view希望提供下列功能
 * <ol>
 * <li>不需要添加太多view到原layout中，增加原layout负担</li>
 * <li>可以方便的指定什么时候显示什么</li>
 * <li>可以对每种提示指定位置、样式、提示和回调</li>
 * <li>可以局部切换显示，也可以全局</li>
 * </ol>
 * 对于第一点，本view已经打包了error, loading状态下要显示的view作为layout中的子view，它们拥有默认的样式。使用时只需要添加本view即可。
 * <br/>
 * 对于第三点，本view可以通过loadingLayoutId和errorLayoutId指定加载的布局和错误提示布局
 * <br/>
 * 对于第四点，本view可以作为一个view整体放在需要的图层中，可以指定具体大小
 * <br/>
 * Created by LiaoXingyu on 18/01/2017.
 */
public class LoadingLayout extends FrameLayout {

	private static final String 加载中 = "加载中";
	private int loadingLayoutId;
	private int loadingTextId;
	private int errorLayoutId;
	private int errorTextId;
	private View loadingLayout;
	private TextView loadingText;
	private View errorLayout;
	private TextView errorText;
	private int contentViewId;
	private View contentView;

	public LoadingLayout(Context context) {
		super(context);
		init(context, null, 0);
	}

	public LoadingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public LoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	void init(Context context, AttributeSet attrs, int defStyleAttr) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LoadingLayout, 0, 0);
		try {
			loadingLayoutId = a.getResourceId(R.styleable.LoadingLayout_loadingLayoutId, 0);
			loadingTextId = a.getResourceId(R.styleable.LoadingLayout_loadingTextId, 0);
			errorLayoutId = a.getResourceId(R.styleable.LoadingLayout_errorLayoutId, 0);
			errorTextId = a.getResourceId(R.styleable.LoadingLayout_errorTextId, 0);
			contentViewId = a.getResourceId(R.styleable.LoadingLayout_contentViewId, 0);
		} finally {
			a.recycle();
		}

		LayoutInflater inflater = LayoutInflater.from(context);
		if (loadingLayoutId == 0) {
			loadingText = new TextView(context, attrs, android.R.attr.textViewStyle);
			loadingText.setGravity(Gravity.CENTER);
			loadingText.setText(加载中);
			LinearLayout linearLayout = new LinearLayout(context);
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			linearLayout.addView(new ProgressBar(context, attrs, android.R.attr.progressBarStyle),
				new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT));
			linearLayout.addView(loadingText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
			loadingLayout = linearLayout;
		} else {
			// FIXME: 08/03/2017 没有查找原本已经在布局中的子view
			loadingLayout = inflater.inflate(loadingLayoutId, null);
			loadingText = (TextView) loadingLayout.findViewById(loadingTextId);
		}
		LayoutParams loadingLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		loadingLayoutParams.gravity = Gravity.CENTER;
		loadingLayout.setVisibility(GONE);
		addView(loadingLayout, loadingLayoutParams);

		if (errorLayoutId == 0) {
			errorText = new TextView(context, attrs, android.R.attr.textViewStyle);
			errorText.setGravity(Gravity.CENTER);
			errorLayout = errorText;
		} else {
			errorLayout = inflater.inflate(errorLayoutId, null);
			errorText = (TextView) errorLayout.findViewById(errorTextId);
			if (errorText == null) {
				throw new IllegalStateException("设置了error layout id，但是没设置error text id");
			}
		}
		LayoutParams errorLP = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		errorLP.gravity = Gravity.CENTER;
		errorLayout.setVisibility(GONE);
		addView(errorLayout, errorLP);
	}

	public void showLoading(String msg) {
		showContent(false);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.setVisibility(VISIBLE);
		}
		if (errorLayout != null) {
			errorLayout.setVisibility(GONE);
		}
		if (loadingText != null) {
			loadingText.setText(msg);
		}
	}

	public void hideLoading() {
		loadingLayout.setVisibility(GONE);
		showContent(true);
	}

	public void showError(String msg) {
		showContent(false);
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
			if (!layoutParams.notHide) {
				child.setVisibility(GONE);
			}
		}
		if (errorLayout != null) {
			errorLayout.setVisibility(VISIBLE);
		}
		if (errorText != null) {
			errorText.setText(msg);
		}
	}

	/**
	 * 设置contentView， 作为显示内容的view，但此view不一定存在于该layout内
	 * @param contentView
	 */
	public void setContentView(View contentView) {
		this.contentView = contentView;
	}

	private void showContent(boolean show) {
		if (contentView == null && contentViewId != 0) {
			contentView = getRootView().findViewById(contentViewId);
		}

		if (contentView != null) {
			contentView.setVisibility(show ? VISIBLE : INVISIBLE);
		}
	}

	public static class LayoutParams extends FrameLayout.LayoutParams {

		private boolean notHide;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			TypedArray typedArray = c.getTheme().obtainStyledAttributes(attrs, R.styleable.LoadingLayout_Layout, 0, 0);
			try {
				notHide = typedArray.getBoolean(R.styleable.LoadingLayout_Layout_notHide, false);
			} finally {
				typedArray.recycle();
			}
		}

		public LayoutParams(int width, int height, boolean notHide) {
			super(width, height);
			this.notHide = notHide;
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(int width, int height, int gravity) {
			super(width, height, gravity);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}
	}

	@Override protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
		return new LayoutParams(lp);
	}

	@Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}
}
