package utils;

/**
 * Created by sivanookala on 24/11/16.
 */
public class ApplicationContext {

    static ApplicationContext context = new ApplicationContext();
    IGcmUtils gcmUtils;

    private ApplicationContext() {
        gcmUtils = new GcmUtils();
    }

    public static ApplicationContext defaultContext() {
        return context;
    }

    public IGcmUtils getGcmUtils() {
        return gcmUtils;
    }

    public void setGcmUtils(IGcmUtils gcmUtils) {
        this.gcmUtils = gcmUtils;
    }
}
