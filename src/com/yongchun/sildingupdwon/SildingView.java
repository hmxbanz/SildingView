package com.yongchun.sildingupdwon;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SildingView extends FrameLayout {

	private View mHeaderView;
	private View mContentView;
	// header、content默认高度
	private int headerViewHeight = 0;
	private int contentViewHeight = 0;
	// 上拉边界,在距离顶部多少位置停止上拉
	private int topOffset = 0;
	// 是否可以下拉
	private boolean isPullEnable = true;
	// 是否可以上拉
	private boolean isPushEnable = true;
	// 上拉是否覆盖header（false 平移header）
	private boolean isCover = true;

	private OnDragListener listener;

	private ViewDragHelper dragHelper;
	// 可移动距离
	private int dragRange;
	// 当前content 移动了多少距离(距离top位置)
	private int contentViewtop = 0;
	// 阻尼系数
	private static final float FRICTION = 2f;

	private int mTouchSlop = 0;

	private boolean isDragStart = true;

	// Used for scrolling
	private boolean dispatchingChildrenDownFaked = false;
	private boolean dispatchingChildrenContentView = false;
	private float dispatchingChildrenStartedAtY = Float.MAX_VALUE;

	public interface OnDragListener {
		void onPull();

		void onPush();

		void onPullEnd();

		void onPushEnd();
	}

	public SildingView(Context context) {
		this(context, null);
	}

	public SildingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SildingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.SildingView);
		// int headerView = ta.getResourceId(R.styleable.SildingView_headerView,
		// -1);
		// if (headerView != -1) {
		// setHeaderView(headerView);
		// }
		// int contentView =
		// ta.getResourceId(R.styleable.SildingView_contentView,
		// -1);
		// if (contentView != -1) {
		// setContentView(contentView);
		// }
		int position = ta.getInt(R.styleable.SildingView_topOffset, 0);
		setTopOffset(position);
		boolean isPull = ta
				.getBoolean(R.styleable.SildingView_pullEnable, true);
		setPullEnable(isPull);
		boolean isPush = ta
				.getBoolean(R.styleable.SildingView_pushEnable, true);
		setPushEnable(isPush);
		boolean isCover = ta.getBoolean(R.styleable.SildingView_isCover, true);
		setCover(isCover);
		ta.recycle();

		dragHelper = ViewDragHelper.create(this, 1.0f, new DragCallBack());
		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		if (getChildCount() < 2) {
			throw new RuntimeException(
					"Content view must contains two child views at least.");
		}
		mHeaderView = getChildAt(0);
		mContentView = getChildAt(1);
	}

	private class DragCallBack extends ViewDragHelper.Callback {
		@Override
		public boolean tryCaptureView(View view, int arg1) {
			// 可以移动的View
			return view == mContentView;
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// 移动大于有效距离
			if (Math.abs(yvel) < mTouchSlop
					&& contentViewtop < headerViewHeight) {
				return;
			}
			// 手势后续操作
			int finalTop = yvel > 0 || contentViewtop >= headerViewHeight ? headerViewHeight
					: topOffset;
			dragHelper.settleCapturedViewAt(releasedChild.getLeft(), finalTop);
			postInvalidate();
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return dragRange;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			super.onViewPositionChanged(changedView, left, top, dx, dy);
			contentViewtop = top;
			requestLayout();
			if (dispatchingChildrenContentView) {
				resetDispatchingContentView();
			}
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int maxTop = isPushEnable ? topOffset : headerViewHeight;
			int maxBottom = isPullEnable ? getHeight() : headerViewHeight;
			// 下拉阻尼效果
			int newTop = top > headerViewHeight ? (int) (contentViewtop + dy
					/ FRICTION) : top;
			// 控制上下边界
			return Math.min(Math.max(newTop, maxTop), maxBottom);
		}
	}

	@Override
	public void computeScroll() {
		if (dragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		try {
			boolean intercept = isDragStart
					&& dragHelper.shouldInterceptTouchEvent(ev);
			return intercept;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		final int action = MotionEventCompat.getActionMasked(event);
		if (!dispatchingChildrenContentView) {
			try {
				dragHelper.processTouchEvent(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (action == MotionEvent.ACTION_MOVE && contentViewtop == 0) {
			dispatchingChildrenContentView = true;
			if (!dispatchingChildrenDownFaked) {
				dispatchingChildrenStartedAtY = event.getY();
				event.setAction(MotionEvent.ACTION_DOWN);
				dispatchingChildrenDownFaked = true;
			}
			mContentView.dispatchTouchEvent(event);
		}

		if (dispatchingChildrenContentView
				&& dispatchingChildrenStartedAtY < event.getY()) {
			resetDispatchingContentView();
		}

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL) {
			resetDispatchingContentView();
			mContentView.dispatchTouchEvent(event);
		}

		return true;
	}

	private void resetDispatchingContentView() {
		dispatchingChildrenDownFaked = false;
		dispatchingChildrenContentView = false;
		dispatchingChildrenStartedAtY = Float.MAX_VALUE;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		dragRange = getHeight();
		// 获取header、content的高度
		if (headerViewHeight == 0 || contentViewHeight == 0) {
			contentViewtop = headerViewHeight = mHeaderView.getHeight();
			contentViewHeight = mContentView.getHeight();
		}
		final int contentTopTemp = contentViewtop;
		// header是平移模式，则设置top
		int headerTop = isCover || contentTopTemp > headerViewHeight ? t: contentTopTemp - headerViewHeight;
		// 下拉 header跟随效果
		int headerBottom = isCover && contentTopTemp <= headerViewHeight ? headerViewHeight
				: contentTopTemp;
		mHeaderView.layout(l, headerTop, r, headerBottom);
		mContentView.layout(l, contentTopTemp, r, b);
//		resetContentHeight(contentTopTemp);
//		resetHeaderHeight(contentTopTemp);
	}

	private void resetHeaderHeight(int contentTopTemp) {
		if (mHeaderView != null && mHeaderView.getHeight() != 0) {
			LayoutParams layoutParams = (LayoutParams) mHeaderView
					.getLayoutParams();
			layoutParams.height = contentTopTemp > headerViewHeight ? contentTopTemp
					: headerViewHeight;
			mHeaderView.setLayoutParams(layoutParams);
		}
	}

	private void resetContentHeight(int contentTopTemp) {
		if (mContentView != null && mContentView.getHeight() != 0) {
			LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
			layoutParams.height = getHeight() - contentTopTemp;
			mContentView.setLayoutParams(layoutParams);
		}
	}

	private void handleSlide(final int top) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				dragHelper.smoothSlideViewTo(mContentView, getPaddingLeft(),top);
				computeScroll();
			}
		});
	}

	public void setHeaderView(int res) {
		setHeaderView(LayoutInflater.from(getContext()).inflate(res, this,
				false));
	}

	public void setHeaderView(View view) {
		if (mHeaderView != null)
			this.removeView(mHeaderView);
		mHeaderView = view;
		addView(mHeaderView);
	}

	public View getHeaderView() {
		return mHeaderView;
	}

	public void setContentView(int res) {
		setContentView(LayoutInflater.from(getContext()).inflate(res, this,
				false));
	}

	public void setContentView(View view) {
		if (mContentView != null)
			this.removeView(mContentView);
		mContentView = view;
		addView(mContentView);
	}

	public View getContentView() {
		return mContentView;
	}

	public void setTopOffset(int position) {
		this.topOffset = position;
		if (contentViewtop < headerViewHeight) {
			handleSlide(position);
		}
	}

	public int getTopOffset() {
		return topOffset;
	}

	public void setPullEnable(boolean isPullEnable) {
		this.isPullEnable = isPullEnable;
		if (contentViewtop > headerViewHeight) {
			handleSlide(headerViewHeight);
		}
	}

	public boolean isPullEnable() {
		return isPullEnable;
	}

	public void setPushEnable(boolean isPushEnable) {
		this.isPushEnable = isPushEnable;
		if (contentViewtop < headerViewHeight) {
			handleSlide(headerViewHeight);
		}
	}

	public boolean isPushEnable() {
		return isPushEnable;
	}

	public void setCover(boolean isCover) {
		this.isCover = isCover;
		if (contentViewtop < headerViewHeight) {
			handleSlide(headerViewHeight);
		}
	}

	public void setDragStart(boolean isDragStart) {
		this.isDragStart = isDragStart;
	}

	public boolean isCover() {
		return isCover;
	}

	public void setOnDragListener(OnDragListener listener) {
		this.listener = listener;
	}

}
