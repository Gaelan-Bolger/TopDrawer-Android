package day.cloudy.apps.assistant;

import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

/**
 * Created by Gaelan Bolger on 12/15/2016.
 */
public class AssistInteractionSessionService extends VoiceInteractionSessionService {

    @Override
    public VoiceInteractionSession onNewSession(Bundle bundle) {
        return new AssistInteractionSession(this);
    }
}
