package example.tatsy.firstopengl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLES31;

import android.opengl.Matrix;
import android.renderscript.Matrix4f;
import android.util.Log;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyRenderer implements Renderer {

    private Resources res;

    private int[] vao = new int[1];
    private int[] vBuffer = new int[1];
    private int[] iBuffer = new int[1];

    private int shader;

    private FloatBuffer positions;
    private FloatBuffer normals;
    private IntBuffer indices;

    private int width = 1;
    private int height = 1;

    public MyRenderer(Resources res) {
        super();
        this.res = res;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int[] versions = new int[2];
        GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, versions, 0);
        GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, versions, 1);
        Log.d("[DATA]", String.format("OpenGL %d.%d", versions[0], versions[1]));

        GLES31.glEnable(GLES31.GL_DEPTH_TEST);
        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Load data.
        loadData();

        // Initialize VAO.
        GLES31.glGenVertexArrays(1, vao, 0);
        GLES31.glBindVertexArray(vao[0]);

        Log.d("[DATA]", String.format("length = %d\n", positions.capacity()));
        GLES31.glGenBuffers(1, vBuffer, 0);
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vBuffer[0]);
        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, 4 * positions.capacity() * 2, null, GLES31.GL_STATIC_DRAW);
        GLES31.glBufferSubData(GLES31.GL_ARRAY_BUFFER, 0, 4 * positions.capacity(), positions);
        GLES31.glBufferSubData(GLES31.GL_ARRAY_BUFFER, 4 * positions.capacity(), 4 * normals.capacity(), normals);

        GLES31.glEnableVertexAttribArray(0);
        GLES31.glEnableVertexAttribArray(1);
        GLES31.glVertexAttribPointer(0, 3, GLES31.GL_FLOAT, false, 0, 0);
        GLES31.glVertexAttribPointer(1, 3, GLES31.GL_FLOAT, false, 0, 4 * positions.capacity());

        GLES31.glGenBuffers(1, iBuffer, 0);
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, iBuffer[0]);
        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, 4 * indices.capacity(), indices, GLES31.GL_STATIC_DRAW);

        GLES31.glBindVertexArray(0);

        // Compile shaders.
        compileShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        GLES31.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES31.glUseProgram(shader);
        GLES31.glBindVertexArray(vao[0]);

        GLES31.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        float[] pMat = new float[16];
        float[] vMat = new float[16];
        float[] mMat = new float[16];
        Matrix.perspectiveM(pMat, 0, 30.0f, (float)width / (float)height, 0.1f, 100.0f);
        Matrix.setLookAtM(vMat, 0, 0.6f, 0.8f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.setIdentityM(mMat, 0);

        float[] mvpMat = new float[16];
        float[] mvMat = new float[16];
        Matrix.multiplyMM(mvMat, 0, vMat, 0, mMat, 0);
        Matrix.multiplyMM(mvpMat, 0, pMat, 0, mvMat, 0);

        int lightLoc = GLES31.glGetUniformLocation(shader, "uLightPos");
        GLES31.glUniform3f(lightLoc, -5.0f, 5.0f, 5.0f);

        int mvpLoc = GLES31.glGetUniformLocation(shader, "uMVPMat");
        GLES31.glUniformMatrix4fv(mvpLoc, 1, false, mvpMat, 0);

        int mvLoc = GLES31.glGetUniformLocation(shader, "uMVMat");
        GLES31.glUniformMatrix4fv(mvLoc, 1, false, mvMat, 0);

        GLES31.glDrawElements(GLES31.GL_TRIANGLES, indices.capacity(), GLES31.GL_UNSIGNED_INT, 0);

        GLES31.glUseProgram(0);
        GLES31.glBindVertexArray(0);
    }

    private void compileShader() {
        String line;
        String source;
        int[] status = new int[1];

        int vs = GLES31.glCreateShader(GLES31.GL_VERTEX_SHADER);
        source = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.openRawResource(R.raw.render_vs)))) {
            while ((line = reader.readLine()) != null) {
                source += line + "\n";
            }
        } catch (IOException e) {
            Log.e("[Exception]", e.getMessage());
        }
        GLES31.glShaderSource(vs, source);
        GLES31.glCompileShader(vs);
        GLES31.glGetShaderiv(vs, GLES31.GL_COMPILE_STATUS, status, 0);
        if (status[0] == GLES31.GL_FALSE) {
            Log.e("[ERROR]", "Failed to compile vertex shader!!");
            Log.e("[ERROR]", GLES31.glGetShaderInfoLog(vs));
            Log.d("[DATA]", source);
        }

        int fs = GLES31.glCreateShader(GLES31.GL_FRAGMENT_SHADER);
        source = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(res.openRawResource(R.raw.render_fs)))) {
            while ((line = reader.readLine()) != null) {
                source += line + "\n";
            }
        } catch (IOException e) {
            Log.e("[Exception]", e.getMessage());
        }

        GLES31.glShaderSource(fs, source);
        GLES31.glCompileShader(fs);
        GLES31.glGetShaderiv(fs, GLES31.GL_COMPILE_STATUS, status, 0);
        if (status[0] == GLES31.GL_FALSE) {
            Log.e("[ERROR]", "Failed to compile fragment shader!!");
            Log.e("[ERROR]", GLES31.glGetShaderInfoLog(fs));
            Log.d("[DATA]", source);
        }

        shader = GLES31.glCreateProgram();
        GLES31.glAttachShader(shader, vs);
        GLES31.glAttachShader(shader, fs);
        GLES31.glLinkProgram(shader);

        int[] linked = new int[1];
        GLES31.glGetProgramiv(shader, GLES31.GL_LINK_STATUS, linked, 0);
        if (linked[0] == GLES31.GL_FALSE) {
            Log.e("[ERROR]", "Shader linkage is failed!!");
            Log.e("[ERROR", GLES31.glGetProgramInfoLog(shader));
        }
    }

    private void loadData() {
        InputStream input = res.openRawResource(R.raw.bunny);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String line;

        List<Float> tempPos = new ArrayList<>();
        List<Float> tempNrm = new ArrayList<>();
        List<Integer> tempIds = new ArrayList<>();

        Pattern pat = Pattern.compile("([0-9]+)//([0-9]+)");

        try {
            while ((line = reader.readLine()) != null) {
                int pos = 0;
                while (pos < line.length() && line.charAt(pos) == ' ') {
                    pos++;
                }

                if (pos == line.length()) continue;

                if (line.charAt(pos) == '#') continue;

                if (line.charAt(pos) == 'v') {
                    if (line.charAt(pos + 1) == 'n') {
                        String[] items = line.split(" ");
                        tempNrm.add(Float.parseFloat(items[1]));
                        tempNrm.add(Float.parseFloat(items[2]));
                        tempNrm.add(Float.parseFloat(items[3]));
                    } else {
                        String[] items = line.split(" ");
                        tempPos.add(Float.parseFloat(items[1]));
                        tempPos.add(Float.parseFloat(items[2]));
                        tempPos.add(Float.parseFloat(items[3]));
                    }
                } else if (line.charAt(pos) == 'f') {
                    String[] items = line.split(" ");

                    for (int k = 1; k <= 3; k++) {
                        Matcher mat = pat.matcher(items[k]);
                        if (mat.matches()) {
                            int iv = Integer.parseInt(mat.group(1));
                            int in = Integer.parseInt(mat.group(2));
                            if (iv == in) {
                                tempIds.add(iv - 1);
                            } else {
                                Log.e("[ERROR]", "Vertex and normal indices do not match!!");
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        positions = ByteBuffer.allocateDirect(4 * tempPos.size()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < tempPos.size(); i++) {
            positions.put(tempPos.get(i));
        }
        positions.position(0);

        normals = ByteBuffer.allocateDirect(4 * tempNrm.size()).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < tempNrm.size(); i++) {
            normals.put(tempNrm.get(i));
        }
        normals.position(0);

        indices = ByteBuffer.allocateDirect(4 * tempIds.size()).order(ByteOrder.nativeOrder()).asIntBuffer();
        for (int i = 0; i < tempIds.size(); i++) {
            indices.put(tempIds.get(i));
        }
        indices.position(0);
    }
}
