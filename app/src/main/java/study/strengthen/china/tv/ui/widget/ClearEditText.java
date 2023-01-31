package study.strengthen.china.tv.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.widget.AppCompatEditText;

import study.strengthen.china.tv.R;

public class ClearEditText extends AppCompatEditText implements View.OnFocusChangeListener, TextWatcher {
	public Drawable e;

	public boolean f;

	public ClearEditText(Context paramContext) {
		this(paramContext, null);
	}

	public ClearEditText(Context paramContext, AttributeSet paramAttributeSet) {
		this(paramContext, paramAttributeSet, 16842862);
	}

	public ClearEditText(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		Drawable drawable = getCompoundDrawables()[2];
		this.e = drawable;
		if (drawable == null)
			this.e = getResources().getDrawable(R.drawable.sea_delete);
		drawable = this.e;
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), this.e.getIntrinsicHeight());
		setClearIconVisible(false);
		setOnFocusChangeListener(this);
		addTextChangedListener(this);
	}

	public void afterTextChanged(Editable paramEditable) {}

	public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {}

	public void onFocusChange(View paramView, boolean paramBoolean) {
		this.f = paramBoolean;
		boolean bool = false;
		if (paramBoolean) {
			paramBoolean = bool;
			if (getText().length() > 0)
				paramBoolean = true;
			setClearIconVisible(paramBoolean);
		} else {
			setClearIconVisible(false);
		}
	}

	public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
		if (this.f) {
			boolean bool;
			if (paramCharSequence.length() > 0) {
				bool = true;
			} else {
				bool = false;
			}
			setClearIconVisible(bool);
		}
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		int i = paramMotionEvent.getAction();
		boolean bool = true;
		if (i == 1 && getCompoundDrawables()[2] != null) {
			if (paramMotionEvent.getX() <= (getWidth() - getTotalPaddingRight()) || paramMotionEvent.getX() >= (getWidth() - getPaddingRight()))
				bool = false;
			if (bool)
				setText("");
		}
		return super.onTouchEvent(paramMotionEvent);
	}

	public void setClearIconVisible(boolean paramBoolean) {
		Drawable drawable;
		if (paramBoolean) {
			drawable = this.e;
		} else {
			drawable = null;
		}
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], drawable, getCompoundDrawables()[3]);
	}
}
