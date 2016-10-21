package com.mx.dxinl.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * Created by Deng Xinliang on 2016/7/22.
 * <p>
 * Tile wall widget.
 */
public class TileWall extends AdapterView<BaseAdapter> {
    private static final int DEF_NUM_OF_ROW_AND_COLUMNS = 3;

    private BaseAdapter mAdapter;
    private DataSetObserver mDataSetObserver;
    private boolean changedAdapter;

    private Paint paint;
    private int numOfColumns;
    private int numOfRows;
    private int dividerColor;
    private int dividerWidth;
    private int touchSlop;
    private float downMotionX;
    private float downMotionY;
    /**
     * This flag will work only when both width measure spec mode and height measure spec mode
     * are {@link MeasureSpec#EXACTLY}.
     */
    private boolean forceDividing;
    private boolean needToUpdateView;
    private boolean isDragging;

    /**
     * Reference of child that user taps on.
     */
    private View pressedChild;
    /**
     * Position of child that user taps on.
     */
    private int pressedPosition;

    public TileWall(Context context) {
        this(context, null);
    }

    public TileWall(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileWall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TileWall(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setWillNotDraw(false);

        touchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TileWall);
        numOfRows = typedArray.getInt(
                R.styleable.TileWall_numOfRows, DEF_NUM_OF_ROW_AND_COLUMNS);
        numOfColumns = typedArray.getInt(
                R.styleable.TileWall_numOfColumns, DEF_NUM_OF_ROW_AND_COLUMNS);
        dividerWidth = typedArray.getDimensionPixelSize(R.styleable.TileWall_dividerWidth,
                getResources().getDimensionPixelSize(R.dimen.deafult_divider_width));
        dividerColor = typedArray.getColor(
                R.styleable.TileWall_dividerColor,
                getResources().getColor(R.color.grey_400));
        forceDividing = typedArray.getBoolean(R.styleable.TileWall_forceDividing, false);
        typedArray.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                needToUpdateView = true;
                requestLayout();
            }

            @Override
            public void onInvalidated() {
                needToUpdateView = true;
                requestLayout();
            }
        };
    }

    @Override
    public BaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(BaseAdapter adapter) {
        needToUpdateView = true;
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        if (mAdapter == null || !mAdapter.getClass().equals(adapter.getClass())) {
            changedAdapter = true;
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mDataSetObserver);

        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        if (changedAdapter) {
            addNewChildren();
            changedAdapter = false;
            needToUpdateView = false;
        } else if (count < mAdapter.getCount()) {
            addChildren();
        } else if (count > mAdapter.getCount()) {
            removeChildren();
        }

        if (needToUpdateView) {
            final int newCount = getChildCount();
            updateChildren(count < newCount ? count : newCount);
            needToUpdateView = false;
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // If both width measure mode and height measure mode are EXACTLY,
        // do not need to calculate width and height anymore.
        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        forceDividing = false;

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int totalWidth = measureChildrenWidth(widthMeasureSpec, heightMeasureSpec);
        int totalHeight = measureChildrenHeight(widthMeasureSpec, heightMeasureSpec);

        int realWidth = widthSize;
        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            // To show all items, must check whether total width is too large to show.
            // If yes, reduce it.
            if (widthMode == MeasureSpec.AT_MOST && totalWidth > widthSize) {
                realWidth = widthSize;
            } else {
                realWidth = totalWidth;
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth, MeasureSpec.EXACTLY);
        }

        int realHeight = heightSize;
        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            // To show all items, must check whether total height is too large to show.
            // If yes, reduce it.
            if (heightMode == MeasureSpec.AT_MOST && totalHeight > heightSize) {
                realHeight = heightSize;
            } else {
                realHeight = totalHeight;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY);
        }

        // If width measure mode is UNSPECIFIED, reduce width basing on height;
        // Else if height measure mode is UNSPECIFIED, reduce height basing on width.
        if (widthMode == MeasureSpec.UNSPECIFIED && heightMode != MeasureSpec.UNSPECIFIED) {
            if (totalHeight > realHeight) {
                realWidth = (int) (totalWidth * realHeight / (float) totalHeight);
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(realWidth, MeasureSpec.EXACTLY);
            }
        } else if (widthMode != MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            if (totalWidth > realWidth) {
                realHeight = (int) (totalHeight * realWidth / (float) totalWidth);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void addNewChildren() {
        removeAllViewsInLayout();

        int adapterCount = mAdapter.getCount();
        addNewChildLayout(0, adapterCount);
    }

    private void addChildren() {
        int count = getChildCount();
        int adapterCount = mAdapter.getCount();
        addNewChildLayout(count, adapterCount);

        count = getChildCount();
        checkChildCount(count, adapterCount);
    }

    private void addNewChildLayout(int count, int adapterCount) {
        for (int i = count; i < adapterCount && i < numOfColumns * numOfRows; i++) {
            View child = mAdapter.getView(i, null, this);
            ViewGroup.LayoutParams params = child.getLayoutParams();
            LayoutParams lp;
            if (params == null) {
                lp = generateDefaultLayoutParams();
            } else if (!checkLayoutParams(params)) {
                lp = generateLayoutParams(params);
            } else {
                lp = (LayoutParams) params;
            }
            lp.viewType = mAdapter.getItemViewType(i);
            child.setLayoutParams(lp);

            addViewInLayout(child, i, lp, true);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downMotionX = event.getX();
                downMotionY = event.getY();
                pressedPosition = findMotionItem(event.getX(), event.getY());
                if (pressedPosition != INVALID_POSITION) {
                    pressedChild = getChildAt(pressedPosition);
                    pressedChild.setPressed(true);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                if (pressedPosition != INVALID_POSITION && isOutOfChildBounds(moveX, moveY)) {
                    resetPressedChild();
                    return false;
                }

                if (isDragging) {
                    return false;
                }

                float distX = moveX - downMotionX;
                float distY = moveY - downMotionY;
                if (isStartDragging(distX, distY)) {
                    isDragging = true;
                    return false;
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    isDragging = false;
                }
                resetPressedChild();
                break;

            case MotionEvent.ACTION_UP:
                if (!isDragging && pressedPosition != INVALID_POSITION) {
                    pressedChild.setPressed(false);
                    performItemClick(pressedChild, pressedPosition, mAdapter.getItemId(pressedPosition));
                    return true;
                } else {
                    isDragging = false;
                    resetPressedChild();
                }
        }
        return false;
    }

    /**
     * Find out which child user tapped, and return its position.
     *
     * @param x     Tap position on X-axis
     * @param y     Tap position on Y-axis
     * @return      Position of child user tapped.
     */
    private int findMotionItem(float x, float y) {
        int width = getWidth();
        int height = getHeight();
        int count = getChildCount();
        int columnsNum = numOfColumns;
        int rowsNum = numOfRows;
        float unitWidth;
        float unitHeight;
        if (forceDividing || count == numOfColumns * numOfRows) {
            unitWidth = (float) width / numOfColumns;
            unitHeight = (float) height / numOfRows;
        } else if (count <= numOfColumns) {
            unitWidth = (float) width / count;
            unitHeight = height;
            columnsNum = count;
            rowsNum = 1;
        } else {
            unitWidth = (float) width / numOfColumns;
            rowsNum = count / numOfColumns + (count % numOfColumns > 0 ? 1 : 0);
            unitHeight = (float) width / rowsNum;
        }

        int columnIndex = -1;
        while (columnIndex < columnsNum) {
            columnIndex++;
            if (x > unitWidth * columnIndex && x < unitWidth * (columnIndex + 1)) {
                break;
            }
        }

        int rowIndex = -1;
        while (rowIndex < rowsNum) {
            rowIndex++;
            if (y > unitHeight * rowIndex && y < unitHeight * (rowIndex + 1)) {
                break;
            }
        }

        int index = rowIndex * columnsNum + columnIndex;
        if (index >= count) {
            return INVALID_POSITION;
        } else {
            return index;
        }
    }

    /**
     * Judge whether user move out of the child that was tapped.
     *
     * @param moveX     Current position on X-axis
     * @param moveY     Current position on Y-axis
     * @return          True if out of child's bounds, else false.
     */
    private boolean isOutOfChildBounds(float moveX, float moveY) {
        return moveX > pressedChild.getRight() || moveY > pressedChild.getBottom()
                || moveX < pressedChild.getLeft() || moveY < pressedChild.getTop();
    }

    /**
     * Judge whether we can think the user is scrolling.
     *
     * @param distX     Moving distance on X-axis
     * @param distY     Moving distance on Y-axis
     * @return          True if user is scrolling, else false.
     */
    private boolean isStartDragging(float distX, float distY) {
        return distX * distX + distY * distY > touchSlop * touchSlop;
    }

    /**
     * When the motion events were dealt, reset the reference that point to the child which tapped
     * by user. Reset the {@link #pressedPosition} too.
     */
    private void resetPressedChild() {
        if (pressedPosition != INVALID_POSITION) {
            pressedChild.setPressed(false);
            pressedChild = null;
            pressedPosition = INVALID_POSITION;
        }
    }

    private void removeChildren() {
        int count = getChildCount();
        int adapterCount = mAdapter.getCount();
        for (int i = count - 1; i >= adapterCount; i--) {
            removeViewInLayout(getChildAt(i));
        }

        count = getChildCount();
        checkChildCount(count, adapterCount);
    }

    private void checkChildCount(int count, int adapterCount) {
        int maxCount = numOfColumns * numOfRows;
        if (adapterCount > maxCount) {
            adapterCount = maxCount;
        }

        if (count != adapterCount) {
            throw new IllegalArgumentException(
                    "Count of children do not equal to BaseAdapter.getCount()");
        }
    }

    private void updateChildren(int count) {
        if (count > getChildCount()) {
            throw new IllegalArgumentException(
                    "The argument(count) cannot be larger than children's count in TileWall.");
        }

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();

            View updateChild;
            int itemViewType = mAdapter.getItemViewType(i);
            if (params.viewType == itemViewType) {
                updateChild = mAdapter.getView(i, getChildAt(i), this);
            } else {
                updateChild = mAdapter.getView(i, null, this);
                params.viewType = itemViewType;
            }

            if (updateChild != child) {
                removeViewInLayout(child);
                addViewInLayout(updateChild, i, params, true);
            }
        }
    }

    private int measureChildrenWidth(int widthMeasureSpec, int heightMeasureSpec) {
        int widthHint = MeasureSpec.getSize(widthMeasureSpec);
        int maxWidth = 0;
        int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }

            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec,
                    getPaddingBottom() + getPaddingTop(), p.height);
            int lpWidth = p.width;
            int childWidthSpec;
            if (lpWidth > 0) {
                childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth, MeasureSpec.EXACTLY);
            } else {
                childWidthSpec = MeasureSpec.makeMeasureSpec(widthHint, MeasureSpec.UNSPECIFIED);
            }
            child.measure(childWidthSpec, childHeightSpec);

            int measuredWidth = child.getMeasuredWidth();
            if (measuredWidth > maxWidth) {
                maxWidth = measuredWidth;
            }
        }

        int paddingHorizontal = getPaddingLeft() + getPaddingRight();
        int columnsCount = count > numOfColumns ? numOfColumns : count;
        int totalDividerWidth = dividerWidth * (columnsCount + 1);
        return maxWidth * columnsCount + totalDividerWidth + paddingHorizontal;
    }

    private int measureChildrenHeight(int widthMeasureSpec, int heightMeasureSpec) {
        int heightHint = MeasureSpec.getSize(heightMeasureSpec);
        int maxHeight = 0;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }

            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                    getPaddingLeft() + getPaddingRight(), p.width);
            int lpHeight = p.height;
            int childHeightSpec;
            if (lpHeight > 0) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
            } else {
                childHeightSpec = MeasureSpec.makeMeasureSpec(heightHint, MeasureSpec.UNSPECIFIED);
            }
            child.measure(childWidthSpec, childHeightSpec);

            int measuredHeight = child.getMeasuredHeight();
            if (measuredHeight > maxHeight) {
                maxHeight = measuredHeight;
            }
        }

        int paddingVertical = getPaddingTop() + getPaddingBottom();
        int rowsCount = (count / numOfColumns) + (count % numOfColumns > 0 ? 1 : 0);
        int totalDividerHeight = dividerWidth * (rowsCount + 1);
        return maxHeight * rowsCount + totalDividerHeight + paddingVertical;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int paddingHorizontal = paddingLeft + paddingRight;
        int paddingVertical = paddingTop + paddingBottom;

        int width;
        int height;
        int totalDividerWidth;
        int totalDividerHeight;
        int columnsCount;
        int rowsCount;

        if (!forceDividing && count < numOfColumns) {
            totalDividerWidth = dividerWidth * (count * 2);
            totalDividerHeight = dividerWidth * 2;
            columnsCount = count;
            rowsCount = 1;
        } else {
            totalDividerWidth = dividerWidth * (numOfColumns * 2);
            columnsCount = numOfRows;

            rowsCount = count / numOfColumns + (count % numOfColumns > 0 ? 1 : 0);
            if (!forceDividing && rowsCount < numOfRows) {
                totalDividerHeight = dividerWidth * (rowsCount * 2);
            } else {
                totalDividerHeight = dividerWidth * (numOfRows * 2);
                rowsCount = numOfRows;
            }
        }

        // Calculate items' width
        int actualWidth = getWidth() - totalDividerWidth - paddingHorizontal;
        width = actualWidth / columnsCount;

        // Calculate item's height
        int actualHeight = getHeight() - totalDividerHeight - paddingVertical;
        height = actualHeight / rowsCount;

        for (int i = 0; i < numOfRows; i++) {
            for (int j = 0; j < numOfColumns; j++) {
                int index = i * numOfColumns + j;
                if (index >= count) {
                    return;
                }

                View child = getChildAt(index);
                if (child == null) {
                    continue;
                }

                int left = dividerWidth * (j * 2 + 1) + width * j + paddingLeft;
                int right = left + width;
                int top = dividerWidth * (i * 2 + 1) + height * i + paddingTop;
                int bottom = top + height;

                int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

                child.layout(left, top, right, bottom);
            }
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof LayoutParams;
    }

    @Deprecated
    @Override
    public View getSelectedView() {
        return null;
    }

    @Deprecated
    @Override
    public void setSelection(int position) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dividerWidth > 0) {
            drawDivider(canvas);
        }
    }

    private void drawDivider(Canvas canvas) {
        int count = getChildCount();
        paint.setColor(dividerColor);
        paint.setStrokeWidth(dividerWidth);

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }

            int left = child.getLeft();
            int top = child.getTop();
            int right = child.getRight();
            int bottom = child.getBottom();
            // Elements in first column draw left divider
            if (i % numOfColumns == 0) {
                canvas.drawLine(left - dividerWidth, top - dividerWidth * 1.5f,
                        left - dividerWidth, bottom + dividerWidth * 1.5f, paint);
            }
            // Elements in first row draw top divider
            if (i < numOfColumns) {
                canvas.drawLine(left - dividerWidth * 1.5f, top - dividerWidth,
                        right + dividerWidth * 1.5f, top - dividerWidth, paint);
            }

            // All elements draw right and bottom divider
            canvas.drawLine(right + dividerWidth, top - dividerWidth * 1.5f,
                    right + dividerWidth, bottom + dividerWidth * 1.5f, paint);
            canvas.drawLine(left - dividerWidth * 1.5f, bottom + dividerWidth,
                    right + dividerWidth * 1.5f, bottom + dividerWidth, paint);
        }
    }

    @SuppressWarnings("unused")
    public TileWall setNumColumns(int numOfColumns) {
        this.numOfColumns = numOfColumns;
        return this;
    }

    @SuppressWarnings("unused")
    public TileWall setNumRows(int numOfRows) {
        this.numOfRows = numOfRows;
        return this;
    }

    @SuppressWarnings("unused")
    public TileWall setDividerWidth(int dividerWidth) {
        this.dividerWidth = dividerWidth;
        return this;
    }

    @SuppressWarnings("unused")
    public TileWall setDividerColor(@ColorRes int resId) {
        dividerColor = getResources().getColor(resId);
        return this;
    }

    @SuppressWarnings("unused")
    public TileWall setForceDividing(boolean forceDividing) {
        this.forceDividing = forceDividing;
        return this;
    }

    private static final class LayoutParams extends ViewGroup.LayoutParams {
        int viewType;

        @SuppressWarnings("unused")
        LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        @SuppressWarnings("unused")
        LayoutParams(int width, int height) {
            super(width, height);
        }

        LayoutParams(int width, int height, int viewType) {
            super(width, height);
            this.viewType = viewType;
        }

        LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
