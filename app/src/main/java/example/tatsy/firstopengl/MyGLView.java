package example.tatsy.firstopengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyGLView extends GLSurfaceView {
    static final String TAG = "MyGLView: ";

    MyRenderer myRenderer;

    float prevX = -1.0f;
    float prevY = -1.0f;
    float angle = 0.0f;

    public MyGLView(Context context) {
        super(context);
        myRenderer = new MyRenderer(getResources());
        setEGLContextClientVersion(3);
        setRenderer(myRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float nowX = event.getX();
                float dx = prevX - nowX;
                angle -= dx / 10.0f;
                myRenderer.setAngle(angle);
                this.requestRender();
                prevX = nowX;
                break;

            case MotionEvent.ACTION_DOWN:
                prevX = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                prevX = -1.0f;
                break;
        }
        return true;
    }
}
