package day.cloudy.apps.assistant;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.util.Log;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class AssistInteractionSession extends VoiceInteractionSession {

    private static final String TAG = AssistInteractionSession.class.getSimpleName();

    public AssistInteractionSession(Context context) {
        super(context);
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        super.onHandleAssist(data, structure, content);
        Log.d(TAG, "onHandleAssist: ");
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
        super.onHandleScreenshot(screenshot);
        Log.d(TAG, "onHandleScreenshot: ");
    }
}
