package com.example.wifilist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public DividerItemDecoration(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            // 计算每个分隔线的位置
            RecyclerView.ViewHolder child = parent.getChildViewHolder(parent.getChildAt(i));
            int top = child.itemView.getBottom();
            int bottom = top + divider.getIntrinsicHeight();

            // 绘制分隔线
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}