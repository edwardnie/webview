//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package layaair.game.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import main.utils.UpdateData;
import main.utils.Utils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import layaair.game.conch.LayaConch5;

public class ConchCanvas extends GLSurfaceView {
    private static String TAG = "MainCanvas";
    protected boolean m_bReleasing = false;
    public TouchFilter m_TouchFilter = new TouchFilter();

    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while((error = egl.eglGetError()) != 12288) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", new Object[]{prompt, Integer.valueOf(error)}));
        }

    }

    public ConchCanvas(Context context) {
        super(context);
        this.init(false, 0, 0);
    }

    public void destory() {
        this.m_TouchFilter = null;
    }

    private void init(boolean translucent, int depth, int stencil) {
        if(translucent) {
            this.getHolder().setFormat(-3);
        }

        this.setEGLContextFactory(new ConchCanvas.ContextFactory());
        this.setEGLConfigChooser(translucent?new ConchCanvas.ConfigChooser(8, 8, 8, 8, depth, stencil):new ConchCanvas.ConfigChooser(5, 6, 5, 0, depth, stencil));
        this.setRenderer(new ConchCanvas.Renderer());
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(this.m_bReleasing) {
            return true;
        } else {
            super.onTouchEvent(event);
            if(event != null) {
                this.m_TouchFilter.onTouchEvent(event);
            }

            return true;
        }
    }

    public static class Renderer implements android.opengl.GLSurfaceView.Renderer {
        public static Renderer me;
        public static Bitmap screenBitmap;
        public static boolean takeScreenshot;
        public GL10 m_lastgl;
        public int m_nlastW;
        public int m_nlastH;

        public static UpdateData updateData;

        private Renderer() {
            me = this;
            this.m_lastgl = null;
            this.m_nlastW = 0;
            this.m_nlastH = 0;
        }


        public void onDrawFrame(GL10 gl) {
            ConchJNI.onDrawFrame();
            if (takeScreenshot) {
                screenBitmap = Utils.screenShot(gl, m_nlastW, m_nlastH);
                updateData.onReceived();
                takeScreenshot = false;
            }
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            if(LayaConch5.ms_layaConche != null) {
                boolean isHorizontion = LayaConch5.ms_layaConche.getHorizontalScreen();
                if(isHorizontion && width < height) {
                    this.m_nlastW = height;
                    this.m_nlastH = width;
                    this.m_lastgl = gl;
                    Log.e("", ">>>>>>>>>>>>surfaceChangedhw w=" + this.m_nlastW + ",h=" + this.m_nlastH + " gl=" + gl);
                    ConchJNI.OnGLReady(this.m_nlastW, this.m_nlastH);
                } else if(width != this.m_nlastW || height != this.m_nlastH || gl != this.m_lastgl) {
                    this.m_nlastW = width;
                    this.m_nlastH = height;
                    this.m_lastgl = gl;
                    Log.e("", ">>>>>>>>>>>>surfaceChanged w=" + width + ",h=" + height + " gl=" + gl);
                    ConchJNI.OnGLReady(width, height);
                }
            } else {
                Log.e("", ">>>>>>>>>>>>>surface not ready");
            }
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }
    }

    private static class ConfigChooser implements EGLConfigChooser {
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2;
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            this.mRedSize = r;
            this.mGreenSize = g;
            this.mBlueSize = b;
            this.mAlphaSize = a;
            this.mDepthSize = depth;
            this.mStencilSize = stencil;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, (EGLConfig[])null, 0, num_config);
            int numConfigs = num_config[0];
            if(numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            } else {
                EGLConfig[] configs = new EGLConfig[numConfigs];
                egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);
                return this.chooseConfig(egl, display, configs);
            }
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            EGLConfig[] arr$ = configs;
            int len$ = configs.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                EGLConfig config = arr$[i$];
                int d = this.findConfigAttrib(egl, display, config, 12325, 0);
                int s = this.findConfigAttrib(egl, display, config, 12326, 0);
                if(d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = this.findConfigAttrib(egl, display, config, 12324, 0);
                    int g = this.findConfigAttrib(egl, display, config, 12323, 0);
                    int b = this.findConfigAttrib(egl, display, config, 12322, 0);
                    int a = this.findConfigAttrib(egl, display, config, 12321, 0);
                    if(r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }

            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            return egl.eglGetConfigAttrib(display, config, attribute, this.mValue)?this.mValue[0]:defaultValue;
        }

        static {
            s_configAttribs2 = new int[]{12324, 4, 12323, 4, 12322, 4, 12352, EGL_OPENGL_ES2_BIT, 12344};
        }
    }

    private static class ContextFactory implements EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 12440;

        private ContextFactory() {
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            Log.w(ConchCanvas.TAG, "creating OpenGL ES 2.0 context");
            ConchCanvas.checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = new int[]{EGL_CONTEXT_CLIENT_VERSION, 2, 12344};
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            ConchCanvas.checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }
}
