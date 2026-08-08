package com.genymobile.scrcpy.wrappers;

import com.genymobile.scrcpy.DisplayInfo;
import com.genymobile.scrcpy.Size;

import android.hardware.display.VirtualDisplay;
import android.os.IInterface;
import android.view.Surface;

import java.lang.reflect.Method;

public final class DisplayManager {
    private final IInterface manager;
    private Method createVirtualDisplayMethod;

    public DisplayManager(IInterface manager) {
        this.manager = manager;
    }

    public DisplayInfo getDisplayInfo(int displayId) {
        try {
            Object displayInfo = manager.getClass().getMethod("getDisplayInfo", int.class).invoke(manager, displayId);
            if (displayInfo == null) {
                return null;
            }
            Class<?> cls = displayInfo.getClass();
            // width and height already take the rotation into account
            int width = cls.getDeclaredField("logicalWidth").getInt(displayInfo);
            int height = cls.getDeclaredField("logicalHeight").getInt(displayInfo);
            int rotation = cls.getDeclaredField("rotation").getInt(displayInfo);
            int layerStack = cls.getDeclaredField("layerStack").getInt(displayInfo);
            int flags = cls.getDeclaredField("flags").getInt(displayInfo);
            return new DisplayInfo(displayId, new Size(width, height), rotation, layerStack, flags);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public int[] getDisplayIds() {
        try {
            return (int[]) manager.getClass().getMethod("getDisplayIds").invoke(manager);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Method getCreateVirtualDisplayMethod() throws NoSuchMethodException {
        if (createVirtualDisplayMethod == null) {
            createVirtualDisplayMethod = android.hardware.display.DisplayManager.class
                    .getMethod("createVirtualDisplay", String.class, int.class, int.class, int.class, Surface.class);
        }
        return createVirtualDisplayMethod;
    }

    /**
     * Create a virtual display mirroring {@code displayIdToMirror} via the hidden static method
     * {@code android.hardware.display.DisplayManager.createVirtualDisplay(String, int, int, int, Surface)}.
     * <p>
     * This is the modern replacement for {@code SurfaceControl.createDisplay(String, boolean)}, which was
     * removed in Android 14 (API 34) and caused a black screen on Android 14/15/16+.
     */
    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int displayIdToMirror, Surface surface) throws Exception {
        Method method = getCreateVirtualDisplayMethod();
        return (VirtualDisplay) method.invoke(null, name, width, height, displayIdToMirror, surface);
    }
}
