package example.tatsy.firstopengl;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FirstOpenGL extends Activity {
    static final String TAG = "FirstOpenGL: ";

    MyGLView myGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myGLView = new MyGLView(this);
        setContentView(myGLView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myGLView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myGLView.onPause();
    }
}