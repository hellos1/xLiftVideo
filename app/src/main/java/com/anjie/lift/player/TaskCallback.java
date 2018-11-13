package com.anjie.lift.player;

import android.media.MediaPlayer;

public interface TaskCallback
{
    void onFinish();

    void notifyPlayElement(PlayerElement element);

    void notifyMediaPrepared(MediaPlayer mp);
}
