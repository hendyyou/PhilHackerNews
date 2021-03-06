package com.philosophicalhacker.philhackernews;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.test.InstrumentationTestCase;

import java.io.File;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by MattDupree on 7/17/15.
 */
public class CachingTests extends InstrumentationTestCase {

    private static final long LAUNCH_TIMEOUT = 1000;
    public static final String ANDROID_SETTINGS_APP = "com.android.settings";
    public static final int UI_ELEMENT_TIMEOUT = 5000;
    private static final String PHILHACKERNEWS_APP = "com.philosophicalhacker.philhackernews";
    private UiDevice mDevice;

    @Inject
    File mHackerNewsDatabaseFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Instrumentation instrumentation = getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
        Context applicationContext = getInstrumentation().getTargetContext().getApplicationContext();
        //noinspection ResourceType
        ObjectGraph objectGraph = (ObjectGraph) applicationContext.getSystemService(
                PhilHackerNewsApplication.OBJECT_GRAPH);
        objectGraph.inject(this);
    }



    public void testMainActivityShowsStoriesFromCache() {
        launchApp(PHILHACKERNEWS_APP);
        waitForStoryDataToLoad();
        toggleAirplaneMode();
        launchApp(PHILHACKERNEWS_APP);
        verifyStoryDataIsVisible();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        toggleAirplaneMode();
        //TODO Figure out how to isolate tests from one another. Currently, the next test seems to start before this method completes, causing a No Such Table exception.
        SQLiteDatabase.deleteDatabase(mHackerNewsDatabaseFile);
    }

    //----------------------------------------------------------------------------------
    // Helpers
    //----------------------------------------------------------------------------------
    private void waitForStoryDataToLoad() {
        mDevice.wait(Until.findObject(By.text("9897306")), UI_ELEMENT_TIMEOUT);
    }

    private void toggleAirplaneMode() {
        launchApp(ANDROID_SETTINGS_APP);
        onMoreNetworksButton().click();
        onAirplaneModeCheckBox().click();
        onOkayButton().click();
    }

    private UiObject2 onOkayButton() {
        return mDevice.wait(Until.findObject(By.text("OK")), UI_ELEMENT_TIMEOUT);
    }

    private void verifyStoryDataIsVisible() {
        assertNotNull(mDevice.wait(Until.findObject(By.text("999+")), UI_ELEMENT_TIMEOUT));
    }

    private UiObject2 onMoreNetworksButton() {
        // Verify the test is displayed in the Ui
        return mDevice.wait(Until.findObject(By.text("More networks")), UI_ELEMENT_TIMEOUT);
    }

    private UiObject2 onAirplaneModeCheckBox() {
        return mDevice.wait(Until.findObject(By.res("android:id/checkbox")), UI_ELEMENT_TIMEOUT);
    }

    private void launchApp(String appPackage) {
        Context context = getInstrumentation().getContext();
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(appPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        mDevice.wait(Until.hasObject(By.pkg(appPackage).depth(0)), LAUNCH_TIMEOUT);
    }
}
