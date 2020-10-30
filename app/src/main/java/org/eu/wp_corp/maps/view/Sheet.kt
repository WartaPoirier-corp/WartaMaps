package org.eu.wp_corp.maps.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.eu.wp_corp.maps.R


class Sheet @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var path: Path

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        val cornerRadius = resources.getDimension(R.dimen.foreground_radius)
        this.path = Path();
        this.path.addRoundRect(
            RectF(0F, 0F, width.toFloat(), cornerRadius + height),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW,
        );
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }

}
