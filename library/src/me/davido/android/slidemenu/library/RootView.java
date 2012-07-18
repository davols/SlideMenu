package me.davido.android.slidemenu.library;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
/**
 * TODO Helper functions to animate close and open ofc. From start to finish. 
 * 
 */

public class RootView extends ViewGroup{
	private static final boolean DEBUG = true;
	private static final String TAG = "RootView";


	private boolean isMenuOpen; 


	private EdgeEffectCompat mLeftEdge;
	private EdgeEffectCompat mRightEdge;

	/**
	 * Callback invoked when the menu is opened.
	 */
	public static interface OnMenuOpenListener {
		/**
		 * Invoked when the menu becomes fully open.
		 */
		public void onMenuOpened();
	}

	/**
	 * Callback invoked when the menu is closed.
	 */
	public static interface OnMenuCloseListener {
		/**
		 * Invoked when the menu becomes fully closed.
		 */
		public void onMenuClosed();
	}

	private boolean mIsBeingDragged;
	private boolean mIsUnableToDrag;
	private int mTouchSlop;
	private float mInitialMotionX;
	/**
	 * Position of the last motion event.
	 */
	private float mLastMotionX;
	private float mLastMotionY;
	/**
	 * ID of the active pointer. This is used to retain consistency during
	 * drags/flings if multiple pointers are used.
	 */
	private int mActivePointerId = INVALID_POINTER;
	/**
	 * Sentinel value for no current active pointer.
	 * Used by {@link #mActivePointerId}.
	 */
	private static final int INVALID_POINTER = -1;

	/**
	 * Determines speed during touch scrolling
	 */
	private VelocityTracker mVelocityTracker;
	private int mMinimumVelocity;
	private int mMaximumVelocity;
	private int mFlingDistance;


	/**
	 * Indicates that the pager is in an idle, settled state. The current page
	 * is fully in view and no animation is in progress.
	 */
	public static final int SCROLL_STATE_IDLE = 0;

	/**
	 * Indicates that the pager is currently being dragged by the user.
	 */
	public static final int SCROLL_STATE_DRAGGING = 1;

	/**
	 * Indicates that the pager is in the process of settling to a final position.
	 */
	public static final int SCROLL_STATE_SETTLING = 2;

	private int mScrollState = SCROLL_STATE_IDLE;

	/**
	 * Margin for the content to "overlap" the menu. 
	 */
	private int mMenuMargin; 


	private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
	private static final float MAXIMUM_ACCELERATION = 2000.0f;

	private static final int MSG_ANIMATE = 1000;
	private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

	private boolean mTracking;


	private OnMenuOpenListener mOnMenuOpenListener;
	private OnMenuCloseListener mOnMenuCloseListener;


	private float mAnimatedAcceleration;
	private float mAnimatedVelocity;
	private float mAnimationPosition;
	private long mAnimationLastTime;
	private long mCurrentAnimationTime;

	private boolean mAnimating;

	private final int mMaximumMajorVelocity;
	private final int mMaximumAcceleration;



	public RootView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public RootView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);


		TypedArray a = context.obtainStyledAttributes(attrs, me.davido.android.slidemenu.library.R.styleable.RootView, defStyle, 0);

		float menuMargin = a.getDimension(R.styleable.RootView_menuMargin, 44);

		a.recycle();


		final float density = getResources().getDisplayMetrics().density;



		mMenuMargin = (int) (menuMargin * density * 0.5f);


		mLeftEdge = new EdgeEffectCompat(context);
		mRightEdge = new EdgeEffectCompat(context);
		final ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		isMenuOpen = false;





		mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
		mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);



	}



	public void smoothOpenMenu() {
		//TODO! 
		isMenuOpen=true;
	}
	public void smoothCloseMenu() {
		//TODO! 
		isMenuOpen=false;
	}


	/**
	 * TODO check that there is two childs and only two childs. 
	 *
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (DEBUG) Log.d(TAG,"onMeasure nr of childs:"+getChildCount());

		final int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
		final int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
		final int width = widthSpecSize - getPaddingLeft() - getPaddingRight();
		final int height = heightSpecSize - getPaddingTop() - getPaddingBottom();


		final View menuContent = getChildAt(0);
		int menudWidthSpec = MeasureSpec.makeMeasureSpec((width-mMenuMargin), MeasureSpec.EXACTLY);
		int menuheightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		menuContent.measure(menudWidthSpec, menuheightSpec);


		final View content = getChildAt(1);
		int contentdWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int contentdheightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		content.measure(contentdWidthSpec, contentdheightSpec);



		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}


	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (DEBUG) Log.d(TAG,"onLayout m: "+mMenuMargin);


		getChildAt(0).layout(0, 0, getWidth()-mMenuMargin, getHeight());
		getChildAt(1).layout(0, 0, getWidth(),getHeight());



	}



	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onMotionEvent will be called and we do the actual
		 * scrolling there.
		 */

		final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

		// Always take care of the touch gesture being complete.
		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			// Release the drag.
			if (DEBUG) Log.v(TAG, "Intercept done!");
			mIsBeingDragged = false;
			mIsUnableToDrag = false;
			mActivePointerId = INVALID_POINTER;
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			return false;
		}

		// Nothing more to do here if we have decided whether or not we
		// are dragging.
		if (action != MotionEvent.ACTION_DOWN) {
			if (mIsBeingDragged) {
				if (DEBUG) Log.v(TAG, "Intercept returning true!");
				return true;
			}
			if (mIsUnableToDrag) {
				if (DEBUG) Log.v(TAG, "Intercept returning false!");
				return false;
			}
		}

		switch (action) {
		case MotionEvent.ACTION_MOVE: {
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
			 * whether the user has moved far enough from his original down touch.
			 */

			/*
			 * Locally do absolute value. mLastMotionY is set to the y value
			 * of the down event.
			 */
			final int activePointerId = mActivePointerId;
			if (activePointerId == INVALID_POINTER) {
				// If we don't have a valid id, the touch down wasn't on content.
				break;
			}

			final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
			final float x = MotionEventCompat.getX(ev, pointerIndex);
			final float dx = x - mLastMotionX;
			final float xDiff = Math.abs(dx);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float yDiff = Math.abs(y - mLastMotionY);
			if (DEBUG) Log.v(TAG, "Moved x to " + x + "," + y + " diff=" + xDiff + "," + yDiff);

			if (canScroll(this, false, (int) dx, (int) x, (int) y)) {
				// Nested view has scrollable area under this point. Let it be handled there.
				mInitialMotionX = mLastMotionX = x;
				mLastMotionY = y;
				return false;
			}
			if (xDiff > mTouchSlop && xDiff > yDiff) {
				if (DEBUG) Log.v(TAG, "Starting drag!");
				mIsBeingDragged = true;
				//setScrollState(SCROLL_STATE_DRAGGING);
				mLastMotionX = x;

			} else {
				if (yDiff > mTouchSlop) {
					// The finger has moved enough in the vertical
					// direction to be counted as a drag...  abort
					// any attempt to drag horizontally, to work correctly
					// with children that have scrolling containers.
					if (DEBUG) Log.v(TAG, "Starting unable to drag!");
					mIsUnableToDrag = true;
				}
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			/*
			 * Remember location of down touch.
			 * ACTION_DOWN always refers to pointer index 0.
			 */
			mLastMotionX = mInitialMotionX = ev.getX();
			mLastMotionY = ev.getY();
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

			if (mScrollState == SCROLL_STATE_SETTLING) {
				// Let the user 'catch' the pager as it animates.
				mIsBeingDragged = true;
				mIsUnableToDrag = false;
				setScrollState(SCROLL_STATE_DRAGGING);
			} else {
				//	completeScroll();
				mIsBeingDragged = false;
				mIsUnableToDrag = false;
			}

			if (DEBUG) Log.v(TAG, "Down at " + mLastMotionX + "," + mLastMotionY
					+ " mIsBeingDragged=" + mIsBeingDragged
					+ "mIsUnableToDrag=" + mIsUnableToDrag);
			break;
		}

		case MotionEventCompat.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			break;
		}

		if (!mIsBeingDragged) {
			// Track the velocity as long as we aren't dragging.
			// Once we start a real drag we will track in onTouchEvent.
			if (mVelocityTracker == null) {
				mVelocityTracker = VelocityTracker.obtain();
			}
			mVelocityTracker.addMovement(ev);
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mIsBeingDragged;
	}




	/**
	 * Tests scrollability within child views of v given a delta of dx.
	 *
	 * @param v View to test for horizontal scrollability
	 * @param checkV Whether the view v passed should itself be checked for scrollability (true),
	 *               or just its children (false).
	 * @param dx Delta scrolled in pixels
	 * @param x X coordinate of the active touch point
	 * @param y Y coordinate of the active touch point
	 * @return true if child views of v can be scrolled by delta of dx.
	 */
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof ViewGroup) {
			final ViewGroup group = (ViewGroup) v;
			final int scrollX = v.getScrollX();
			final int scrollY = v.getScrollY();
			final int count = group.getChildCount();
			// Count backwards - let topmost views consume scroll distance first.
			for (int i = count - 1; i >= 0; i--) {
				// TODO: Add versioned support here for transformed views.
				// This will not work for transformed views in Honeycomb+
				final View child = group.getChildAt(i);
				if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
						y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
						canScroll(child, true, dx, x + scrollX - child.getLeft(),
								y + scrollY - child.getTop())) {
					return true;
				}
			}
		}

		return checkV && ViewCompat.canScrollHorizontally(v, -dx);
	}
	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = MotionEventCompat.getActionIndex(ev);
		final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
		if (pointerId == mActivePointerId) {
			// This was our active pointer going up. Choose a new
			// active pointer and adjust accordingly.
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
			mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
			if (mVelocityTracker != null) {
				mVelocityTracker.clear();
			}
		}
	}

	private void setScrollState(int newState) {
		if (mScrollState == newState) {
			return;
		}
		mScrollState = newState;

	}





	@Override
	public boolean onTouchEvent(MotionEvent ev) {


		if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
			// Don't handle edge touches immediately -- they may actually belong to one of our
			// descendants.
			return false;
		}


		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		boolean needsInvalidate = false;

		switch (action & MotionEventCompat.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			if(DEBUG) Log.d(TAG,"ACTIONDOWN");
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			completeScroll();

			// Remember where the motion event started
			mLastMotionX = mInitialMotionX = ev.getX();
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			break;
		}
		case MotionEvent.ACTION_MOVE:
			if(DEBUG) Log.d(TAG,"ACTIONMOVE");
			if (!mIsBeingDragged) {
				final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
				final float x = MotionEventCompat.getX(ev, pointerIndex);
				final float xDiff = Math.abs(x - mLastMotionX);
				final float y = MotionEventCompat.getY(ev, pointerIndex);
				final float yDiff = Math.abs(y - mLastMotionY);

				if (DEBUG) Log.v(TAG, "Moved x to " + x + "," + y + " diff=" + xDiff + "," + yDiff);
				if (xDiff > mTouchSlop && xDiff > yDiff) {
					if (DEBUG) Log.v(TAG, "Starting drag!");
					mIsBeingDragged = true;
					mLastMotionX = x;
					setScrollState(SCROLL_STATE_DRAGGING);
				}
			}
			if (mIsBeingDragged) {
				// Scroll to follow the motion event
				final int activePointerIndex = MotionEventCompat.findPointerIndex(
						ev, mActivePointerId);
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final float deltaX = mLastMotionX - x;
				mLastMotionX = x;
				float oldScrollX = getScrollX();

				float scrollX = oldScrollX + deltaX;
				final int width = getWidth();

				/*
				 * Negative scroll to open menu, positive to remove it. 
				 * 
				 */

				int currentLeft = getChildAt(1).getLeft();

				if((currentLeft-scrollX)>getWidth()-mMenuMargin) {
					float over = -scrollX; 
					needsInvalidate = mLeftEdge.onPull(over / width);
					scrollX = 0;
				}
				if((currentLeft-scrollX) < 0) { 
					float over = -scrollX;
					needsInvalidate = mLeftEdge.onPull(over / width);
					scrollX=0;
				}

				// Don't lose the rounded component
				mLastMotionX += scrollX - (int) scrollX;

				/**TODO might want to use the scrollTO and override onscroll. On the TODO list and use the scroller instead of 
				 * using a hangler. 
				 */
				//	scrollTo((int) scrollX, getScrollY());
				getChildAt(1).offsetLeftAndRight((int) -scrollX);
				if(DEBUG) Log.d(TAG,"onMove pageScrolled call: "+scrollX);
				//Not used in this case. 
				pageScrolled((int) scrollX);
			}
			break;
			// end of case MotionEvent.ACTION_MOVE:
			//Outside of all cases. 
		case MotionEvent.ACTION_UP:
			if(DEBUG) Log.d(TAG,"ACTIONUP");
			if (mIsBeingDragged) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int initialVelocity = (int) VelocityTrackerCompat.getXVelocity(
						velocityTracker, mActivePointerId);

				final int widthWithMargin = getWidth();
				final int scrollX = getScrollX();


				final int activePointerIndex =
						MotionEventCompat.findPointerIndex(ev, mActivePointerId);
				final float x = MotionEventCompat.getX(ev, activePointerIndex);
				final int totalDelta = (int) (x - mInitialMotionX);


				performFling(initialVelocity,totalDelta);

				mActivePointerId = INVALID_POINTER;
				endDrag();
				needsInvalidate = mLeftEdge.onRelease() | mRightEdge.onRelease();
			}
			break;


		case MotionEvent.ACTION_CANCEL:
			if(DEBUG) Log.d(TAG,"ACTIONCANCEL");
			if (mIsBeingDragged) {
				//TODO use performfling.s
				setCurrentItemInternal(false,true, true, 0);
				mActivePointerId = INVALID_POINTER;
				endDrag();
				needsInvalidate = mLeftEdge.onRelease() | mRightEdge.onRelease();
			}
			break;
		case MotionEventCompat.ACTION_POINTER_DOWN: {
			if(DEBUG) Log.d(TAG,"ACTION_POINTER_DOWN");
			final int index = MotionEventCompat.getActionIndex(ev);
			final float x = MotionEventCompat.getX(ev, index);
			mLastMotionX = x;
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}
		case MotionEventCompat.ACTION_POINTER_UP:
			if(DEBUG) Log.d(TAG,"ACTION_POINTER_UP");
			onSecondaryPointerUp(ev);
			mLastMotionX = MotionEventCompat.getX(ev,
					MotionEventCompat.findPointerIndex(ev, mActivePointerId));
			break;
		}
		if (needsInvalidate) {
			invalidate();
		}
		return true;
	}




	private void pageScrolled(int xpos) {
		final int widthWithMargin = getWidth();
		final int position = xpos / widthWithMargin;
		final int offsetPixels = xpos % widthWithMargin;
		final float offset = (float) offsetPixels / widthWithMargin;

		onPageScrolled(position, offset, offsetPixels);

	}

	/**
	 * This method will be invoked when the scrolling is performed. 
	 * 
	 * @param position Position index of the first page currently being displayed.
	 *                 Page position+1 will be visible if positionOffset is nonzero.
	 * @param offset Value from [0, 1) indicating the offset from the page at position.
	 * @param offsetPixels Value in pixels indicating the offset from position.
	 */
	protected void onPageScrolled(int position, float offset, int offsetPixels) {
		// Offset any decor views if needed - keep them on-screen at all times.

		final int scrollX = getScrollX();
		int paddingLeft = getPaddingLeft();
		int paddingRight = getPaddingRight();
		final int width = getWidth();

		if(DEBUG) Log.d(TAG,"OnPageScrolled position:"+position+" offset :"+offset+" offsetPixels:"+offsetPixels);


	}


	private void endDrag() {
		mIsBeingDragged = false;
		mIsUnableToDrag = false;

		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	private void completeScroll() {

	}

	void setCurrentItemInternal(boolean change, boolean smoothScroll, boolean always) {
		setCurrentItemInternal(change, smoothScroll, always, 0);

	}

	void setCurrentItemInternal(boolean change, boolean smoothScroll, boolean always, int velocity) {
		Log.d("VEL","setCurrentItemInternal Vel: "+velocity);
		Log.d("Root2","Smooth: "+smoothScroll);
		//positive vel for open. negative for close. 

	}


	private boolean shouldChange(int velocity, int deltaX) {
		return Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity;
	}

	private void performFling(int initialVelocity, int totalDelta) {

		//positive vel for open. negative for close. 
		if(DEBUG) Log.d("FLING","init: "+initialVelocity +" totalDelta:"+totalDelta);
		mAnimationPosition = getChildAt(1).getLeft();
		mAnimatedVelocity = initialVelocity;
		if(isMenuOpen) {
			if(shouldChange(initialVelocity,totalDelta)) {
				// We are expanded and are now going to animate away.
				if(DEBUG) Log.d("FLING","We are expanded and are now going to animate it closed.");

				if (initialVelocity > 0) {
					mAnimatedVelocity = 0;
				}
			}
			else {
				if(DEBUG) Log.d("FLING","We are expanded, but they didn't move sufficiently to cause vel:");

				// We are expanded, but they didn't move sufficiently to cause
				// us to retract.  Animate back to the expanded position.
				mAnimatedAcceleration = mMaximumAcceleration;
				if (initialVelocity < 0) {
					mAnimatedVelocity = 0;
				}


			}


		}
		else {
			//Closed. 
			if(shouldChange(initialVelocity,totalDelta)) {
				//We are collapsed. we are going to animate it opened. 
				if(DEBUG) Log.d("FLING","We are collapsed. we are going to animate it opened. ");

				// We are collapsed, and they moved enough to allow us to expand.
				mAnimatedAcceleration = mMaximumAcceleration;
				if (initialVelocity < 0) {
					mAnimatedVelocity = 0;
				}

			}
			else {
				if(DEBUG) 	Log.d("FLING","We are collapsed, but they didn't move sufficiently to cause");

				//We are collapsed, but they didn't move sufficiently to cause
				// us to retract.  Animate back to the expanded position.
				mAnimatedAcceleration = -mMaximumAcceleration;
				if (initialVelocity > 0) {
					mAnimatedVelocity = 0;
				}

			}

		}
		if(DEBUG) 	Log.d("FLING","The values we had are: "+initialVelocity +" totalDelta:"+totalDelta);


		long now = SystemClock.uptimeMillis();
		mAnimationLastTime = now;
		mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
		mAnimating = true;
		mHandler.removeMessages(MSG_ANIMATE);
		mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
		stopTracking();

	}
	private final Handler mHandler = new SlidingHandler();

	private void stopTracking() {

		mTracking = false;



		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}


	private class SlidingHandler extends Handler {
		public void handleMessage(Message m) {
			switch (m.what) {
			case MSG_ANIMATE:
				doAnimation();
				break;
			}
		}
	}


	private void doAnimation() {
		if (mAnimating) {
			Log.d("ANIM","Increment animation posNow:"+mAnimationPosition);
			incrementAnimation();
			Log.d("ANIM","Animating posNow:"+mAnimationPosition);
			if (mAnimationPosition >= getWidth()-mMenuMargin) {
				Log.d("FLING","If1");
				mAnimating = false;
				openMenu();
			} else if (mAnimationPosition <= 0) {
				Log.d("ANIM","elseif");
				mAnimating = false;
				closeMenu();
			} else {
				Log.d("ANIM","else");
				//	moveHandle((int) mAnimationPosition);
				getChildAt(1).offsetLeftAndRight((int)mAnimationPosition-getChildAt(1).getLeft());
				mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
				mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
						mCurrentAnimationTime);
			}
		}
	}





	private void incrementAnimation() {
		long now = SystemClock.uptimeMillis();
		float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
		final float position = mAnimationPosition;
		final float v = mAnimatedVelocity;                                // px/s
		final float a = mAnimatedAcceleration;                            // px/s/s
		mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
		mAnimatedVelocity = v + (a * t);                                  // px/s
		mAnimationLastTime = now;                                         // ms
	}


	private void closeMenu() {
		int leftNow = getChildAt(1).getLeft();
		getChildAt(1).offsetLeftAndRight(-leftNow);

		if (!isMenuOpen) {
			return;
		}

		isMenuOpen = false;
		if (mOnMenuCloseListener != null) {
			mOnMenuCloseListener.onMenuClosed();
		}
	}

	private void openMenu() {

		int leftNow = getChildAt(1).getLeft();
		//Move distance from leftNow to width-margin. 
		getChildAt(1).offsetLeftAndRight((getWidth()-mMenuMargin)-leftNow);

		if (isMenuOpen) {
			return;
		}

		isMenuOpen = true;

		if (mOnMenuOpenListener != null) {
			mOnMenuOpenListener.onMenuOpened();
		}
	}
	public void animateClose() {
		prepareTracking();
		performFling(mMaximumAcceleration,1);


	}
	public void animateOpen() {


		prepareTracking();
		performFling(-mMaximumAcceleration,1);

	}

	private void prepareTracking() {
		mTracking = true;
		mVelocityTracker = VelocityTracker.obtain();
		boolean opening = !isMenuOpen;
		if (opening) {
			mAnimatedAcceleration = mMaximumAcceleration;
			mAnimatedVelocity = mMaximumMajorVelocity;
			mAnimationPosition = getChildAt(1).getLeft();

			mAnimating = true;
			mHandler.removeMessages(MSG_ANIMATE);
			long now = SystemClock.uptimeMillis();
			mAnimationLastTime = now;
			mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
			mAnimating = true;
		} else {
			if (mAnimating) {
				mAnimating = false;
				mHandler.removeMessages(MSG_ANIMATE);
			}
			//moveHandle(position);
		}
	}
}
