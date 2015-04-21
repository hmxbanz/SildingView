package com.yongchun.sildingupdwon;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class SildingView extends RelativeLayout {

	private View mHeaderView;
	private View mContentView;
	// header、content默认高度
	private int headerViewHeight = 0;
	private int contentViewHeight = 0;
	// 上拉边界,在距离顶部多少位置停止上拉
	private int topPosition = 0;
	// 是否可以下拉
	private boolean isPullEnable = true;
	// 是否可以上拉
	private boolean isPushEnable = true;
	// 上拉是否覆盖header（false 平移header）
	private boolean isCover = true;

	private OnDragListener listener;

	private ViewDragHelper dragHelper = ViewDragHelper.create(this,
			new DragCallBack());

	// 当前content 移动了多少距离(距离top位置)
	private int contentViewtop = 0;
	// 阻尼系数
	private static final float FRICTION = 1.5f;

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
		int headerView = ta.getResourceId(R.styleable.SildingView_headerView, -1);
		if (headerView != -1) {
			setHeaderView(headerView);
		}
		int contentView = ta
				.getResourceId(R.styleable.SildingView_contentView, -1);
		if (contentView != -1) {
			setContentView(contentView);
		}
		int position = ta.getInt(R.styleable.SildingView_topPosition, 0);
		setTopPosition(position);
		boolean isPull = ta.getBoolean(R.styleable.SildingView_pullEnable, true);
		setPullEnable(isPull);
		boolean isPush = ta.getBoolean(R.styleable.SildingView_pushEnable, true);
		setPushEnable(isPush);
		boolean isCover = ta.getBoolean(R.styleable.SildingView_isCover, true);
		setCover(isCover);
		ta.recycle();
	}

	private class DragCallBack extends ViewDragHelper.Callback {
		@Override
		public boolean tryCaptureView(View view, int arg1) {
			// 可以移动的View
			return view.equals(mContentView);
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			// 松开后回到的位置
			int finalTop;
			// 下拉，且位置在header下面
			if (contentViewtop >= headerViewHeight) {
				finalTop = headerViewHeight;
			} else {
				// 位置在上面判断手势，下拉回到初始位置、上拉(isPushEnable ? topPosition :
				// headerViewHeight)
				finalTop = yvel > 0 ? headerViewHeight : topPosition;
			}
			// dragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
			dragHelper.smoothSlideViewTo(releasedChild,
					releasedChild.getLeft(), finalTop);
			postInvalidate();
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top,
				int dx, int dy) {
			contentViewtop = top;
			requestLayout();
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int maxTop = isPushEnable ? topPosition : headerViewHeight;
			int maxBottom = isPullEnable ? getHeight() : headerViewHeight;
			// 下拉阻尼效果
			int newTop = top > headerViewHeight ? (int) (contentViewtop + dy
					/ FRICTION) : top;
			// 控制上下边界
			return Math.min(Math.max(newTop, maxTop), maxBottom);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return dragHelper.shouldInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		dragHelper.processTouchEvent(ev);
		return true;
	}

	@Override
	public void computeScroll() {
		if (dragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		// 获取header、content的高度
		if (headerViewHeight == 0 || contentViewHeight == 0) {
			contentViewtop = headerViewHeight = mHeaderView.getHeight();
			contentViewHeight = mContentView.getHeight();
		}
		final int contentTopTemp = contentViewtop;
		resetHeaderHeight(contentTopTemp);
		resetContentHeight(contentTopTemp);
		// header是平移模式，则设置top
		int headerTop = !isCover && contentTopTemp <= headerViewHeight ? contentTopTemp
				- headerViewHeight
				: t;
		// 下拉 header跟随效果
		int headerBottom = contentTopTemp > headerViewHeight ? contentTopTemp
				: headerViewHeight;
		mHeaderView.layout(l, headerTop, r, headerBottom);
		mContentView.layout(l, contentTopTemp, r, getHeight());
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
			LayoutParams layoutParams = (LayoutParams) mContentView
					.getLayoutParams();
			layoutParams.height = getHeight() - contentTopTemp;
			mContentView.setLayoutParams(layoutParams);
		}
	}

	private void handleSlide(final int top) {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				dragHelper.smoothSlideViewTo(mContentView, getPaddingLeft(),
						top);
				postInvalidate();
			}
		});
	}

	public void setHeaderView(int res) {
		setHeaderView(LayoutInflater.from(getContext()).inflate(res, null));
	}

	public void setHeaderView(View view) {
		if (mHeaderView != null)
			this.removeView(mHeaderView);
		mHeaderView = view;
		addView(mHeaderView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
	}

	public View getHeaderView() {
		return mHeaderView;
	}

	public void setContentView(int res) {
		setContentView(LayoutInflater.from(getContext()).inflate(res, null));
	}

	public void setContentView(View view) {
		if (mContentView != null)
			this.removeView(mContentView);
		mContentView = view;
		addView(mContentView, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

	public View getContentView() {
		return mContentView;
	}

	public void setTopPosition(int position) {
		this.topPosition = position;
		if (contentViewtop < headerViewHeight) {
			handleSlide(position);
		}
	}

	public int getTopPosition() {
		return topPosition;
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

	public boolean isCover() {
		return isCover;
	}

	public void setOnDragListener(OnDragListener listener) {
		this.listener = listener;
	}

}
