# TileWall

	This is a practice of custom view.

## Preview
![tupian](preview.png)
## How to Use
### Java code
```java
TileWall mTileWall = new TileWall(context)
        .setNumColumns(numOfColumns)
        .setNumRows(numOfRows)
        .setDividerColor(dividerColor)
		.setDividerWidth(dividerWidth)
        .setForceDividing(true);

mTileWall.setAdapter(new BaseAdaper() {...});
```
### Or XML code
```xml
<com.mx.dxinl.library.TileWall
    android:id="@+id/tileWall"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:padding="@dimen/activity_horizontal_margin"
    app:dividerColor="@color/gray_400"
    app:dividerWidth="@dimen/test_divider_width_1"
    app:numOfColumns="3"
    app:numOfRows="3" />
```
This view support multi view type also.

### Notice
```setForceDividing(boolean)``` will work only when both width measure spec mode and height measure spec mode are ```MeasureSpec.EXACTLY```.
