package com.example.administrator.neuroelectricstimulator

import android.content.Context
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

/**
 * author:Chance_Zheng.
 * date:  On 2018-05-30
 */
class SlideDelete @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private var leftView: View? = null
    private var rightView: View? = null
    private var helper: ViewDragHelper? = null

    private val callback = object : ViewDragHelper.Callback() {
        //手势滑动时
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }


        //拖动控件水平移动
        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            var left = left
            //对左右越界问题的处理
            if (child === leftView) {
                //处理两边的越界问题
                if (left >= 0) {
                    left = 0
                } else if (left <= -rightView!!.measuredWidth) {
                    left = -rightView!!.measuredWidth
                }
            } else if (child === rightView) {
                //只处理右边的越界问题,因为左侧越界的时看不到该View
                if (left <= leftView!!.measuredWidth - rightView!!.measuredWidth) {
                    left = leftView!!.measuredWidth - rightView!!.measuredWidth
                } else if (left >= leftView!!.measuredWidth) {
                    left = leftView!!.measuredWidth
                }
            }
            return left
        }

        //监听控件移动状态
        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            //如果左边控件拖动,我们要让右边控件也重新布局
            if (changedView === leftView) {
                rightView!!.layout(rightView!!.left + dx, 0, rightView!!.right + dx, rightView!!.bottom + dy)
            } else if (changedView === rightView) {
                leftView!!.layout(leftView!!.left + dx, 0, leftView!!.right + dx, leftView!!.bottom + dy)
            }
        }

        /**
         * 解决滑动一半松手时,View的复位
         * @param releasedChild 松开的View
         * @param xvel
         * @param yvel
         */
        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            //松开后,什么时候打开rightView,什么时候关闭leftView
            //临界值,rightView.getLeft() 和 屏幕的宽度-rightView.getWidth()/2
            if (releasedChild === leftView) {
                if (rightView!!.left < measuredWidth - rightView!!.measuredWidth / 2) {
                    //使用ViewDragHelper来滑动
                    helper!!.smoothSlideViewTo(rightView!!, measuredWidth - rightView!!.measuredWidth, 0)

                    invalidate()
                } else {
                    helper!!.smoothSlideViewTo(rightView!!, measuredWidth, 0)
                    invalidate()
                }
            }
        }
    }

    init {
        helper = ViewDragHelper.create(this, callback)
    }

    //需要重写computeScroll
    override fun computeScroll() {
        //判断是否要继承滑动
        if (helper!!.continueSettling(true)) {
            //invalidate();
            //兼容使用
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //对当前组合View的测量,不使用的话,也可以自己设置
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //第一步获取里面子View
        leftView = getChildAt(0)
        rightView = getChildAt(1)
        //第二步给子View提供相应的布局
        val leftL = 0
        val leftT = 0
        val leftR = leftView!!.measuredWidth
        val leftB = leftView!!.measuredHeight
        leftView!!.layout(leftL, leftT, leftR, leftB)

        //给rightView提供相应的布局
        val rightL = leftView!!.measuredWidth
        val rightT = 0
        val rightR = leftView!!.measuredWidth + rightView!!.measuredWidth
        val rightB = rightView!!.measuredHeight
        rightView!!.layout(rightL, rightT, rightR, rightB)

    }

    //View的事件传递
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //1,要消费该事件,所以直接返回true
        //2,使用ViewDragHelper来实现滑动效果
        helper!!.processTouchEvent(event)
        return true
    }
}