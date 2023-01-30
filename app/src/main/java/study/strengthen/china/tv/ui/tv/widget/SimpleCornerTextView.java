package study.strengthen.china.tv.ui.tv.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import study.strengthen.china.tv.R;


public class SimpleCornerTextView extends AppCompatTextView {

    int leftDrawableWidth=0;
    int leftDrawableHeight=0;
    int topDrawableWidth=0;
    int topDrawableHeight=0;
    int rightDrawableWidth=0;
    int rightDrawableHeight=0;
    int bottomDrawableWidth=0;
    int bottomDrawableHeight=0;
    public final static int POSITION_LEFT=0;
    public final static int POSITION_TOP=1;
    public final static int POSITION_RIGHT=2;
    public final static int POSITION_BOTTOM=3;
    public Drawable drawableLeft;
    public Drawable drawableTop;
    public Drawable drawableRight;
    public Drawable drawableBottom;
    private float borderWidth;


    public SimpleCornerTextView(Context context) {
        this(context, null);
        loadShapeAttribute(context, null);
    }

    public SimpleCornerTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadShapeAttribute(context, attrs);
    }

    public SimpleCornerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadShapeAttribute(context, attrs);
    }

    private void loadShapeAttribute(Context context, AttributeSet attrs){
        GradientDrawable gradientDrawable = new GradientDrawable();
        this.setBackgroundDrawable(gradientDrawable);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleCornerTextView);
        if (a != null){
            int bgColor = a.getColor(R.styleable.SimpleCornerTextView_bgColor, Color.TRANSPARENT);
            int borderColor = a.getColor(R.styleable.SimpleCornerTextView_borderColor, Color.WHITE);

            borderWidth = a.getDimension(R.styleable.SimpleCornerTextView_borderWidth, 0f);

            float radius = a.getDimension(R.styleable.SimpleCornerTextView_radius, 0f);
            float topLeftRadius = a.getDimension(R.styleable.SimpleCornerTextView_topLeftRadius, radius);
            float topRightRadius = a.getDimension(R.styleable.SimpleCornerTextView_topRightRadius, radius);
            float bottomRightRadius = a.getDimension(R.styleable.SimpleCornerTextView_bottomRightRadius, radius);
            float bottomLeftRadius = a.getDimension(R.styleable.SimpleCornerTextView_bottomLeftRadius, radius);
            GradientDrawable drawable = (GradientDrawable) this.getBackground();
            drawable.setCornerRadii(new float[]{topLeftRadius,topLeftRadius,topRightRadius,topRightRadius,
                    bottomRightRadius,bottomRightRadius, bottomLeftRadius,bottomLeftRadius});
            drawable.setColor(bgColor);
            drawable.setStroke((int)borderWidth, borderColor);

            leftDrawableWidth = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableWidth_left,0);
            leftDrawableHeight = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableHeight_left, 0);
            topDrawableWidth = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableWidth_top,0);
            topDrawableHeight = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableHeight_top, 0);
            rightDrawableWidth = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableWidth_right,0);
            rightDrawableHeight = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableHeight_right, 0);
            bottomDrawableWidth = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableWidth_bottom,0);
            bottomDrawableHeight = a.getDimensionPixelSize(R.styleable.SimpleCornerTextView_drawableHeight_bottom, 0);
        }
        a.recycle();
        setCompoundDrawablesWithIntrinsicBounds(drawableLeft,drawableTop,drawableRight,drawableBottom);

    }

    public void setBackgroundColor(int bgColor){
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setColor(bgColor);
    }

    public void setBackgroundResColor(int bgColor){
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setColor(getResources().getColor(bgColor));
    }

    public void setTextResColor(int color) {
        setTextColor(getResources().getColor(color));
    }

    public void setStroke(int width, int color){
        GradientDrawable drawable = (GradientDrawable) this.getBackground();
        drawable.setStroke(width, color);
    }

    public void setBorderResColor(int color){
        setStroke((int)borderWidth,getResources().getColor(color));
    }

    /**
     * Sets the Drawables (if any) to appear to the left of, above, to the
     * right of, and below the text. Use {@code null} if you do not want a
     * Drawable there. The Drawables' bounds will be set to their intrinsic
     * bounds.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawablesRelative} or related methods.
     * 这里重写这个方法，来设置上下左右的drawable的大小
     *
     * @attr ref android.R.styleable#TextView_drawableLeft
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableRight
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        this.drawableLeft = left;
        this.drawableTop = top;
        this.drawableRight = right;
        this.drawableBottom = bottom;

        if (left != null) {
            left.setBounds(0, 0, leftDrawableWidth,leftDrawableHeight);
        }
        if (right != null) {
            right.setBounds(0, 0, rightDrawableWidth,rightDrawableHeight);
        }
        if (top != null) {
            top.setBounds(0, 0, topDrawableWidth,topDrawableHeight);
        }
        if (bottom != null) {
            bottom.setBounds(0, 0, bottomDrawableWidth,bottomDrawableHeight);
        }

        setCompoundDrawables(left, top, right, bottom);
    }

    /*
     * 代码中动态设置drawable的宽高度
     * */
    public void setDrawableSize(int width, int height,int position) {
        if (position==this.POSITION_LEFT) {
            leftDrawableWidth = width;
            leftDrawableHeight = height;
        }
        if (position==this.POSITION_TOP) {
            topDrawableWidth = width;
            topDrawableHeight = height;
        }
        if (position==this.POSITION_RIGHT) {
            rightDrawableWidth = width;
            rightDrawableHeight = height;
        }
        if (position==this.POSITION_BOTTOM) {
            bottomDrawableWidth = width;
            bottomDrawableHeight = height;
        }

        setCompoundDrawablesWithIntrinsicBounds(drawableLeft,drawableTop,drawableRight,drawableBottom);
    }
}
