package com.lvt4j.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * 手势密码工具类
 * @author LV
 * 使用说明：
 * 		在Activity中定义内部类继承该类，重写doInput方法，该方法参数即为用户输入手势密码
 * 可能问题：
 * 		出现不兼容Android版本问题时，尝试修改removeGlobalOnLayoutListener()方法为removeOnGlobalLayoutListener()
 *
 */
public abstract class TGesturesPassword {

	private static final int nodeInterval = 25;

	private RelativeLayout gestures_password;
	private List<Node> nodes = new ArrayList<Node>();
	
	private int normalImgResId;
	private int lightImgResId;
	
	/**
	 * 手势密码矩阵中每个节点信息
	 * @author LV
	 *
	 */
	private class Node {
		public ImageView image;
		public int idx;
		public int left;
		public int right;
		public int top;
		public int bottom;
		public boolean isHighLight;	//是否划过
		public void hightLight() {
			isHighLight = true;
			image.setBackgroundResource(lightImgResId);
		}
		public void closeHightLight() {
			isHighLight = false;
			image.setBackgroundResource(normalImgResId);
		}
	}
	
	/**
	 *  划线工具类
	 * @author LV
	 *
	 */
	private class TDrawLine extends View {
		private Canvas canvas;// 画布
		private Bitmap bitmap;// 位图
		private Paint paint;//画笔
		private List<Node> matchNodes = new ArrayList<TGesturesPassword.Node>();
		
		public TDrawLine(Context context) {
			super(context);
			paint = new Paint(Paint.DITHER_FLAG);// 创建一个画笔
			bitmap = Bitmap.createBitmap(960, 960, Bitmap.Config.ARGB_8888); // 设置位图的宽高
			paint.setStyle(Style.STROKE);// 设置非填充
			paint.setStrokeWidth(10);// 笔宽5像素
			paint.setColor(Color.rgb(4, 115, 157));// 设置颜色
			paint.setAntiAlias(true);// 不显示锯齿
			canvas = new Canvas();
			canvas.setBitmap(bitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
		/**
		 * 判断坐标是否在一个节点内
		 */
		private Node getNode(float x,float y) {
			for (Node node : nodes)
				if (x>node.left && x<node.right &&
						y>node.top && y<node.bottom)
					return node;
			return null;
		}
		/**
		 * 画出两个节点之间的线
		 * @param nodeFrom
		 * @param nodeTo
		 */
		private void drawLine(Node nodeFrom,Node nodeTo) {
			float fromX = (nodeFrom.left+nodeFrom.right)/2;
			float fromY = (nodeFrom.top+nodeFrom.bottom)/2;
			float toX = (nodeTo.left+nodeTo.right)/2;
			float toY = (nodeTo.top+nodeTo.bottom)/2;
			canvas.drawLine(fromX,fromY,toX,toY,paint);
		}
		/**
		 * 画出节点和一个坐标之间的线
		 * @param nodeFrom
		 * @param x
		 * @param y
		 */
		private void drawLine(Node nodeFrom,float x,float y) {
			float fromX = (nodeFrom.left+nodeFrom.right)/2;
			float fromY = (nodeFrom.top+nodeFrom.bottom)/2;
			canvas.drawLine(fromX,fromY,x,y,paint);
		}
		/**
		 * 触摸事件
		 */
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			Node node = getNode(x, y);
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				cleanAllLine();
				Node lastNode = drawMatchNode();
				if (node==null && lastNode!=null) {
					drawLine(lastNode, x,y);
				} else if (node!=null && node.isHighLight) {
					drawLine(node, x,y);
				} else if (node!=null && !node.isHighLight) {
					node.hightLight();
					matchNodes.add(node);
					if (lastNode!=null) {
						drawLine(lastNode, node);
					}
					drawLine(node, x,y);
				}
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				doInput(getCurInputAndClean());
			default:
				break;
			}
			return true;
		}
		private void cleanAllLine() {
			canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		}
		/**
		 * 将已点亮的节点之间的线画出来
		 * @return
		 */
		private Node drawMatchNode() {
			Node lastNode = null;
			for (Node node : matchNodes) {
				if (lastNode != null) {
					drawLine(lastNode, node);
				}
				lastNode = node;
			}
			return lastNode;
		}
		/**
		 * 返回用户输入的手势密码，并清空画线及节点等信息
		 * @return
		 */
		private String getCurInputAndClean() {
			cleanAllLine();
			String curInput = "";
			for (Node node : matchNodes) {
				curInput += node.idx;
				node.closeHightLight();
			}
			matchNodes = new ArrayList<TGesturesPassword.Node>();
			return curInput;
		}
	}
	/**
	 * 用户输入完手势密码后的处理动作
	 * @param inputPWD
	 */
	public abstract void doInput(String inputPWD); 
	
	/**
	 * 构造函数
	 * @param parentLayout 手势密码要放入的Layout
	 * @param context
	 */
	public TGesturesPassword(ViewGroup parentLayout, Context context,int bgResId,int normalImgResId,int lightImgResId) {
		super();
		this.normalImgResId = normalImgResId;
		this.lightImgResId = lightImgResId;
		//生成手势密码所在的RelativeLayout
		gestures_password = new RelativeLayout(context);
		gestures_password.setBackgroundResource(bgResId);
		parentLayout.addView(gestures_password);
		//定义手势密码所用的RelativeLayout宽高和每个节点的宽高及位置
		gestures_password.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@SuppressWarnings("deprecation")
					public void onGlobalLayout() {
						int w = gestures_password.getWidth();
						int h = gestures_password.getHeight();
						h = Math.min(w, h);
						w = Math.min(w, h);
						LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) gestures_password
								.getLayoutParams();
						lp.width = w;
						lp.height = h;
						gestures_password.setLayoutParams(lp);
						int nodeSize = (w - 6 * nodeInterval) / 3;
						for (int i = 0; i < 9; i++) {
							Node node = nodes.get(i);
							RelativeLayout.LayoutParams nodelp = (RelativeLayout.LayoutParams) node.image
									.getLayoutParams();
							nodelp.width = nodeSize;
							nodelp.height = nodeSize;
							int row = i / 3;
							int col = i % 3;
							nodelp.leftMargin = col * nodeSize + (2 * col + 1)
									* nodeInterval;
							nodelp.topMargin = row * nodeSize + (2 * row + 1)
									* nodeInterval;
							node.image.setLayoutParams(nodelp);
							node.left = nodelp.leftMargin;
							node.right = nodelp.leftMargin + nodeSize;
							node.top = nodelp.topMargin;
							node.bottom = nodelp.topMargin + nodeSize;
						}
						gestures_password.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);
					}
				});
		//定义画线工具
		TDrawLine tDrawLine = new TDrawLine(context);
		RelativeLayout.LayoutParams tDrawLinelp = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gestures_password.addView(tDrawLine, tDrawLinelp);
		//生成9个节点
		for (int i = 0; i < 9; i++) {
			ImageView nodeImage = new ImageView(context);
			nodeImage.setBackgroundResource(normalImgResId);
			gestures_password.addView(nodeImage);
			Node node = new Node();
			node.image = nodeImage;
			node.idx = i;
			nodes.add(node);
		}
		
	}
}
