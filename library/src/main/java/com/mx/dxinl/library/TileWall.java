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
import android.view.View;
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
    private float dividerWidth;
    /**
     * This flag will work only when both width measure spec mode and height measure spec mode
     * are {@link MeasureSpec#EXACTLY}.
     */
    private boolean forceDividing;

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

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TileWall);
        numOfRows = typedArray.getInt(
                R.styleable.TileWall_numOfRows, DEF_NUM_OF_ROW_AND_COLUMNS);
        numOfColumns = typedArray.getInt(
                R.styleable.TileWall_numOfColumns, DEF_NUM_OF_ROW_AND_COLUMNS);
        dividerWidth = typedArray.getDimension(
                R.styleable.TileWall_dividerWidth,
                getResources().getDimension(R.dimen.deafult_divider_width));
        dividerColor = typedArray.getColor(
                R.styleable.TileWall_dividerColor,
                getResources().getColor(R.color.gray_400));
        forceDividing = typedArray.getBoolean(R.styleable.TileWall_forceDividing, false);
        typedArray.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                requestLayout();
            }

            @Override
            public void onInvalidated() {
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
        boolean needToUpdateView = true;
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
        }

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST) {
            forceDividing = false;
            int totalWidth = measureChildrenWidth(widthMeasureSpec, heightMeasureSpec);

            // To show all items, we must check whether total height is too large to show.
            // If yes, we shall decrease it.
            if (widthMode == MeasureSpec.AT_MOST && totalWidth > widthSize) {
                totalWidth = widthSize;
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY);
        }

        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST) {
            forceDividing = false;
            int totalHeight = measureChildrenHeight(widthMeasureSpec, heightMeasureSpec);

            // To show all items, we must check whether total height is too large to show.
            // If yes, we shall decrease it.
            if (heightMode == MeasureSpec.AT_MOST && totalHeight > heightSize) {
                totalHeight = heightSize;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY);
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

            setOnChildClickListener(child, i);
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

    private void setOnChildClickListener(View child, final int position) {
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performItemClick(v, position, v.getId());
            }
        });
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
                    "The argument(count) cannot be larger than children's count in TileWallLayout.");
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
        int totalDividerWidth = (int) (dividerWidth * (columnsCount + 1));
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
        int totalDividerHeight = (int) (dividerWidth * (rowsCount + 1));
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
        if (!forceDividing && count < numOfColumns) {
            int totalDividerWidth = (int) (dividerWidth * (count + 1));
            int totalDividerHeight = (int) (dividerWidth * 2);
            width = (getWidth() - totalDividerWidth - paddingHorizontal) / count;
            height = (getHeight() - totalDividerHeight - paddingVertical);
        } else {
            int totalDividerWidth = (int) (dividerWidth * (numOfColumns + 1));
            width = (getWidth() - totalDividerWidth - paddingHorizontal) / numOfColumns;

            int rowsCount = count / numOfColumns + (count % numOfColumns > 0 ? 1 : 0);
            if (!forceDividing && rowsCount < numOfRows) {
                int totalDividerHeight = (int) (dividerWidth * (rowsCount + 1));
                height = (getHeight() - totalDividerHeight - paddingVertical) / rowsCount;
            } else {
                int totalDividerHeight = (int) (dividerWidth * (numOfRows + 1));
                height = (getHeight() - totalDividerHeight - paddingVertical) / numOfRows;
            }
        }

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

                int left = (int) ((dividerWidth * (j + 1)) + width * j) + paddingLeft;
                int right = left + width;
                int top = (int) ((dividerWidth * (i + 1)) + height * i) + paddingTop;
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

    /**
     * This method is not supported.
     *
     * @return ignored
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public View getSelectedView() {
        throw new UnsupportedOperationException("getSelectedView() is not supported in TileWallLayout.");
    }

    /**
     * This method is not supported.
     *
     * @param position ignored
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public void setSelection(int position) {
        throw new UnsupportedOperationException("setSelection(int) is not supported in TileWallLayout.");
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
        int halfDividerWidth = (int) (dividerWidth / 2f);

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }

            int left = child.getLeft() - halfDividerWidth;
            int top = child.getTop() - halfDividerWidth;
            int right = child.getRight() + halfDividerWidth;
            int bottom = child.getBottom() + halfDividerWidth;
            canvas.drawLine(left, top - halfDividerWidth, left, bottom + halfDividerWidth, paint);
            canvas.drawLine(left - halfDividerWidth, top, right + halfDividerWidth, top, paint);
            canvas.drawLine(right, top - halfDividerWidth, right, bottom + halfDividerWidth, paint);
            canvas.drawLine(left - halfDividerWidth, bottom, right + halfDividerWidth, bottom, paint);
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
    public TileWall setDividerWidth(float dividerWidth) {
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

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int viewType) {
            super(width, height);
            this.viewType = viewType;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
