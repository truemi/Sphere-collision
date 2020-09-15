package com.example.administrator.myapplication.widget;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.myapplication.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2018-05-17.
 */

public class BallView {

    private Context context;
    private World world;//世界
    private int pWidth;//父控件的宽度
    private int pHeight;//父控件的高度
    private ViewGroup mViewGroup;//父控件
    private float density = 0.7f;//物质密度
    private float friction = 0.2f;//摩擦系数
    private float restitution = 0.5f;//恢复系数
    private final Random random;
    private boolean startEnable = true;//是否开始绘制
    private int velocityIterations = 3;//迭代速度
    private int positionIterations = 10;//位置迭代
    private float dt = 1f / 60;//刷新时间
    private int ratio = 50;//物理世界与手机虚拟比例
    private Paint paint;
    private boolean isInitWorld = false;
    private Canvas canvas;

    public BallView(Context context, ViewGroup viewGroup) {
        this.context = context;
        this.mViewGroup = viewGroup;
        random = new Random();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }


    public void onDraw(Canvas canvas) {
        this.canvas = canvas;
        if (world != null) {
            // 初始化圆形世界边界
            initCircleWorldBounds(canvas);
            // 初始化矩形世界边界
//            initWorldBounds(canvas);
        }
        if (!startEnable)
            return;
        world.step(dt, velocityIterations, positionIterations);
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mViewGroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.body_tag); //从view中获取绑定的刚体
            if (body != null) {
                //获取刚体的位置信息
                view.setX(metersToPixels(body.getPosition().x) - view.getWidth() / 2);
                view.setY(metersToPixels(body.getPosition().y) - view.getHeight() / 2);
                view.setRotation(radiansToDegrees(body.getAngle() % 360));
            }
        }

        mViewGroup.invalidate();//更新view的位置

    }

    /**
     * @param b
     */
    public void onLayout(boolean b) {
        createWorld(b, this.context);
    }

    /**
     * 创建物理世界
     */
    private void createWorld(boolean haveDifferent, Context context) {
        if (world == null) {
            world = new World(new Vec2(0f, 10f));//创建世界,设置重力方向
        }
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = mViewGroup.getChildAt(i);
            int tag = 0;
            if (childAt.getTag() != null) {
                tag = (int) childAt.getTag();
            }
            Body body = (Body) childAt.getTag(R.id.body_tag);
            if (tag != -1 && (body == null || haveDifferent)) {
                createBody(world, childAt);
            }
        }
    }

    /**
     * 创建刚体
     */
    private void createBody(World world, View view) {
        if (world == null) return;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;

        //设置初始参数，为view的中心点
        bodyDef.position.set(pixelsToMeters(view.getX() + view.getWidth() / 2),
                pixelsToMeters(view.getY() + view.getHeight() / 2));
        Shape shape = null;
        Boolean isCircle = (Boolean) view.getTag(R.id.circle_tag);
        if (isCircle != null && isCircle) {
            shape = createCircle(view);
        }
        FixtureDef fixture = new FixtureDef();
        fixture.setShape(shape);
        fixture.friction = friction;
        fixture.restitution = restitution;
        fixture.density = density;

        //用世界创建出刚体
        Body body = world.createBody(bodyDef);
        body.createFixture(fixture);
        view.setTag(R.id.body_tag, body);
        //初始化物体的运动行为
        body.setLinearVelocity(new Vec2(random.nextFloat(), random.nextFloat()));

    }

    /**
     * 设置世界边界 矩形
     */
    private void initWorldBounds(Canvas canvas) {

        Rect rect = new Rect(10, 10, pWidth, pHeight);
        canvas.drawRect(rect, paint);
        if (isInitWorld) {
            return;
        }
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;//设置零重力,零速度
        float bodyWidth = pixelsToMeters(pWidth);
        float bodyHeight = pixelsToMeters(pHeight);
        float bodyRatio = pixelsToMeters(ratio);
        PolygonShape polygonShape1 = new PolygonShape();
        polygonShape1.setAsBox(bodyWidth, bodyRatio);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape1;
        fixtureDef.density = 1f;//物质密度
        fixtureDef.friction = 0.4f;//摩擦系数
        fixtureDef.restitution = 0.3f;//恢复系数

        bodyDef.position.set(0, -bodyRatio);
        Body bodyTop = world.createBody(bodyDef);//世界中创建刚体
        bodyTop.createFixture(fixtureDef);//刚体添加夹具

        bodyDef.position.set(0, bodyHeight + bodyRatio);
        Body bodyBottom = world.createBody(bodyDef);//世界中创建刚体
        bodyBottom.createFixture(fixtureDef);

        PolygonShape polygonShape2 = new PolygonShape();
        polygonShape2.setAsBox(bodyRatio, bodyHeight);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = polygonShape2;
        fixtureDef2.density = 0.5f;//物质密度
        fixtureDef2.friction = 0.3f;//摩擦系数
        fixtureDef2.restitution = 0.5f;//恢复系数

        bodyDef.position.set(-bodyRatio, bodyHeight);
        Body bodyLeft = world.createBody(bodyDef);//世界中创建刚体
        bodyLeft.createFixture(fixtureDef2);//刚体添加物理属性

        bodyDef.position.set(bodyWidth + bodyRatio, 0);
        Body bodyRight = world.createBody(bodyDef);//世界中创建刚体
        bodyRight.createFixture(fixtureDef2);//刚体添加物理属性
        isInitWorld = true;
    }

    /**
     * 设置世界边界 圆形
     */
    private void initCircleWorldBounds(Canvas canvas) {
        // 绘制圆形边框
        canvas.drawCircle(pWidth / 2, pHeight / 2, pHeight / 2, paint);
        if (isInitWorld) {
            return;
        }
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;//设置零重力,零速度
        bodyDef.position.set(0, 0);
        Body bodyTop = world.createBody(bodyDef);//世界中创建刚体
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 1f;//物质密度
        fixtureDef.friction = 0.3f;//摩擦系数
        fixtureDef.restitution = 0.5f;//恢复系数


        //设置圆形刚体边界
        ArrayList positions = polygon2(36, Double.valueOf(pHeight / 2).intValue());
        for (int i = 0; i < positions.size(); i++) {
            float[] xy = (float[]) positions.get(i);
            float x = xy[0];
            float y = xy[1];
            float segmentlength = xy[2];
            float angle = xy[3];
            PolygonShape polygonShape = new PolygonShape();
            // 设置具有方向的shape
            polygonShape.setAsBox(0, pixelsToMeters(segmentlength), new Vec2(pixelsToMeters(x), pixelsToMeters(y)), angle);
            fixtureDef.shape = polygonShape;
            bodyTop.createFixture(fixtureDef);//刚体添加夹具
        }
        isInitWorld = true;
    }

    /**
     * 创建圆形描述
     */
    private Shape createCircle(View view) {
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(pixelsToMeters(view.getWidth() / 2));
        return circleShape;

    }

    /**
     * 随机运动
     * 施加一个脉冲,立刻改变速度
     */
    public void rockBallByImpulse() {
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Vec2 mImpulse = new Vec2(random.nextInt(1000), random.nextInt());
            View view = mViewGroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.body_tag);
            if (body != null) {
                body.applyLinearImpulse(mImpulse, body.getPosition(), true);
                Log.e("btn", "有脉冲");
            } else {
                Log.e("btn", "body == null");
            }
        }
    }

    /**
     * 向指定位置移动
     */
    public void rockBallByImpulse(float x, float y) {
        int childCount = mViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Vec2 mImpulse = new Vec2(x * 50, y * 50);
            View view = mViewGroup.getChildAt(i);
            Body body = (Body) view.getTag(R.id.body_tag);
            if (body != null) {
                body.applyLinearImpulse(mImpulse, body.getPosition(), true);
            }
        }
    }

    public float metersToPixels(float meters) {
        return meters * ratio;
    }

    public float pixelsToMeters(float pixels) {
        return pixels / ratio;
    }


    /**
     * 弧度转角度
     *
     * @param radians
     * @return
     */
    private float radiansToDegrees(float radians) {
        return radians / 3.14f * 180f;

    }

    /**
     * 大小发生变化
     *
     * @param pWidth
     * @param pHeight
     */
    public void onSizeChanged(int pWidth, int pHeight) {

        this.pWidth = pWidth - 10;
        this.pHeight = pHeight - 10;
    }

    private void setStartEnable(boolean b) {
        startEnable = b;
    }

    public void onStart() {
        setStartEnable(true);
    }

    public void onStop() {
        setStartEnable(false);
    }

    /**
     * 多边形定点坐标计算
     *
     * @param n      边数
     * @param startX 起始点x坐标
     * @param startY 起始点y坐标
     * @param r      半径
     * @return
     */
    public ArrayList polygon(int n, float startX, float startY, int r) {
        ArrayList<float[]> doubles = new ArrayList<>();
        double theta = 2 * Math.PI / n;
        for (int i = 0; i < n + 1; i++) {
            float x, y = 0f;
            if (i > 0) {
                x = Double.valueOf(startX - r * Math.sin(theta * i)).floatValue();
                y = Double.valueOf(startY + r - r * Math.cos(theta * i)).floatValue();
            } else {
                x = startX;
                y = startY;
            }
            float[] xy = new float[2];
            xy[0] = x;
            xy[1] = y;
            doubles.add(xy);
        }
        return doubles;
    }

    /**
     * 根据半径获取多边形每个点的坐标位置
     *
     * @param n 多边形边数
     * @param r 半径
     * @return
     */
    public ArrayList polygon2(int n, int r) {
        float segmentlength = Double.valueOf(r * Math.sin(Math.PI / n)).floatValue();
        ArrayList<float[]> doubles = new ArrayList<>();
        double theta = 2 * Math.PI / n;
        for (int i = 0; i < n + 1; i++) {
            float x, y = 0f;
            x = Double.valueOf(r * Math.cos(theta * i)).floatValue();
            y = Double.valueOf(r * Math.sin(theta * i)).floatValue();
            float[] xy = new float[4];
            xy[0] = x + r;
            xy[1] = y + r;
            xy[2] = segmentlength;
            xy[3] = Double.valueOf(theta * i).floatValue();
            doubles.add(xy);
        }
        return doubles;
    }

}
