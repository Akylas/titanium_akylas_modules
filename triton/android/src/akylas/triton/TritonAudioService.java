package akylas.triton;

import android.app.Service;
import android.media.MediaPlayer;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.util.TiConvert;

import ti.modules.titanium.audio.streamer.AudioService;
import ti.modules.titanium.audio.streamer.AudioStreamerService;

import com.tritondigital.player.CuePoint;
import com.tritondigital.player.MediaPlayer.OnCuePointReceivedListener;
import com.tritondigital.player.MediaPlayer.OnStateChangedListener;
import com.tritondigital.player.StreamUrlBuilder;
import com.tritondigital.player.TritonPlayer;

/**
 * A backbround {@link Service} used to keep music playing between activities
 * and when the user moves Apollo into the background.
 */
public class TritonAudioService extends AudioStreamerService {
    
    private static final String TAG = "TritonAudioService";
    protected static String getCmdPrefix() {
        return "akylas.triton.player";
    }
    private Bundle currentCuePoint = null;
    

    @Override
    protected void closeCursor() {
        super.closeCursor();
        currentCuePoint = null;
    }
    
    
    @Override
    protected KrollDict getTrackEvent() {
        KrollDict data = super.getTrackEvent();
        if (currentCuePoint != null) {
            data.put("track", convertCuePoint(currentCuePoint));
        }
        return data;
    }
    
    @Override
    public void updateMetadata() {
        HashMap<String, Object> metadata = null;
        if (proxy != null && proxy.hasProperty(TiC.PROPERTY_METADATA)) {
            metadata = (HashMap<String, Object>) proxy
                    .getProperty(TiC.PROPERTY_METADATA);
        } else if (currentCuePoint != null) {
            metadata = convertCuePoint(currentCuePoint);
        } else if (mCursor instanceof HashMap) {
            metadata = (HashMap<String, Object>) mCursor;
        }
        
        updateMetadata(metadata);
    }
    

    public void updateMetadata(Bundle cuePoint) {
        currentCuePoint = cuePoint;
        notifyChange(cmds.META_CHANGED);
    }
    
    @Override
    public void onStartPlaying(final BasePlayingItem playingItem) {
        
//        currentCuePoint = ((PlayingItem) playingItem).getSbmPlayer().getLastReceivedCuePoint();
        currentCuePoint = ((BroadcastPlayer) mPlayer).getLastReceivedCuePoint();
        super.onStartPlaying(playingItem);
    }
    
    static final HashMap<String, String> CUEPOINTS = new HashMap<String, String>() {
        {
            put(CuePoint.TRACK_ALBUM_NAME, "album");
            put(CuePoint.TRACK_ARTIST_NAME, "artist");
            put(CuePoint.CUE_TITLE, "title");
            put(CuePoint.TRACK_GENRE, "genre");
            put(CuePoint.CUE_TIME_DURATION, "duration");
            put(CuePoint.TRACK_ALBUM_YEAR, "year");
            put(CuePoint.TRACK_COVER_URL, "artwork");
        }
    };
    
    private HashMap<String, Object> convertCuePoint(final Bundle cuePoint) {
        HashMap<String, Object> result = new HashMap<>();
        for (String key : cuePoint.keySet())
        {
            Object value = cuePoint.get(key);
            String realKey = CUEPOINTS.get(key);
            if (value != null)
            {
                result.put((realKey != null) ? realKey : key, value.toString());
            }
        }
        return result;
    }
    
    @Override
    protected AudioPlayer createPlayer(AudioService audioService) {
        return new BroadcastPlayer((TritonAudioService) audioService);
    }
    
//    public class StreamPlayingItem extends AudioStreamerService.PlayingItem {
//        SbmPlayer sbmPlayer = null;
//        private int mCuePointIdx;
//        /**
//         * Trying to sync the music and the metadata.
//         *
//         * We are doing it only once because of issues in Android's media player timer.
//         * We are ignoring the first few cue points because they will come as soon as
//         * the stream starts and not at their real position.
//         */
//        private void syncSbmAndPlayer(Bundle cuePoint, final CompatMediaPlayer player)
//        {
//            if ((sbmPlayer != null) && (cuePoint != null) && (mCuePointIdx == 2))
//            {
//                int cuePointPosition = cuePoint.getInt(CuePoint.POSITION_IN_STREAM, -1);
//                if (cuePointPosition != -1)
//                {
//                    try
//                    {
//                        int playerPosition = player.getCurrentPosition();
//                        sbmPlayer.setOffset(cuePointPosition - playerPosition);
//                    }
//                    catch (IllegalStateException e) {}
//                }
//            }
//
//            mCuePointIdx++;
//        }
//
//        StreamPlayingItem(final Player multiplayer, String audioPath, final String sbmUrl) {
//            super(audioPath);
//            // Create SBM player
//            Bundle sbmPlayerSettings = new Bundle();
//            sbmPlayerSettings.putString(SbmPlayer.SETTINGS_URL, sbmUrl);
//            sbmPlayer = new SbmPlayer(multiplayer.getService(), sbmPlayerSettings);
//            sbmPlayer.setOnCuePointReceivedListener(multiplayer);
//            sbmPlayer.setOnStateChangedListener(multiplayer);
//        }
//        
//        public SbmPlayer getSbmPlayer() {
//            return sbmPlayer;
//        }
//
//        public void release() {
//            if (sbmPlayer != null) {
//                sbmPlayer.release();
//                sbmPlayer = null;
//            }
//        }
//    }
    
    public class BroadcastPlayingItem extends AudioStreamerService.PlayingItem {
        private final Bundle mSettings;
        protected BroadcastPlayingItem(Bundle settings) {
            super(settings.getString(TritonPlayer.SETTINGS_BROADCASTER));
            mSettings = settings;
        }
    }
    
    private final class BroadcastPlayer extends AudioPlayer implements OnCuePointReceivedListener, OnStateChangedListener{
        private TritonPlayer mPlayer;

        
        protected PlayingItem mPlayingItem = null;
        
        public BroadcastPlayer(final TritonAudioService service) {
            super(service);
        }

        public Bundle getLastReceivedCuePoint() {
            if (mPlayer != null) {
                return mPlayer.getLastReceivedCuePoint();
            }
            return null;
        }
        @Override
        public long duration() {
            if (mPlayer != null) {
                return mPlayer.getDuration();
            }
            return -1;
        }

        @Override
        public int getAudioSessionId() {
            return 0;
        }

        @Override
        public boolean isPaused() {
            if (mPlayer != null) {
                return mPlayer.getState() == TritonPlayer.STATE_PAUSED;
            }
            return false;
        }

        @Override
        public boolean isPlaying() {
            if (mPlayer != null) {
                return mPlayer.getState() == TritonPlayer.STATE_PLAYING;
            }
            return false;
        }

        @Override
        public boolean openFile(Object object, boolean preparingNext) {
            if (preparingNext) {
                return false;
            }
            if (mPlayer != null) {
                release();
            }
            HashMap<String, Object> props = (HashMap<String, Object>) object;
            final Service service = mService.get();

            HashMap<String, String> streamParams = new HashMap<String, String>();
//            streamParams.put(StreamUrlBuilder.BANNERS, "300x50,320x50,320x480");

            Bundle settings = new Bundle();
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals(TritonPlayer.SETTINGS_STREAM_PARAMS) && value instanceof HashMap) {
                    for (Map.Entry<String, Object> entry2 : ((HashMap<String, Object>) value)
                            .entrySet()) {
                        String key2 = entry.getKey();
                        Object value2 = entry.getValue();
                        streamParams.put(key2, TiConvert.toString(value2));
                    }
                } else {
                    
                    if (value instanceof Boolean) {
                        settings.putBoolean(key, TiConvert.toBoolean(value));
                    } else if (value instanceof String) {
                        String str = TiConvert.toString(value);
                        if (key.equals(TritonPlayer.SETTINGS_MOUNT)) {
                            settings.putBoolean("is_preprod", str.startsWith("BASIC_CONFIG"));
                        }
                        settings.putString(key, str);
                    } else if (value instanceof Float) {
                        settings.putFloat(key, TiConvert.toFloat(value));
                    }
                }
            }
            
            settings.putSerializable(TritonPlayer.SETTINGS_STREAM_PARAMS,
                    streamParams);

//            settings.putBoolean(TritonPlayer.SETTINGS_ENABLE_LOCATION_TRACKING,
//                    true);
//            settings.putString(TritonPlayer.SETTINGS_BROADCASTER, BROADCASTER);
//            settings.putString(TritonPlayer.SETTINGS_MOUNT, MOUNT);
//            settings.putString(TritonPlayer.SETTINGS_STATION_NAME, STATION_NAME);
//            settings.putSerializable(TritonPlayer.SETTINGS_STREAM_PARAMS,
//                    streamParams);
            mPlayingItem = new BroadcastPlayingItem(settings);
            mPlayer = new TritonPlayer(service, settings);
            mPlayer.setOnCuePointReceivedListener(this);
            mPlayer.setOnStateChangedListener(this);
            mIsInitialized = true;
            mIsPreparing = false;
            start();
            mService.get().onStartPlaying(mPlayingItem);

            return mIsInitialized;
        }

        @Override
        public void pause() {
            stop();
        }

        @Override
        public long position() {
            if (mPlayer != null) {
                return mPlayer.getPosition();
            }
            return -1;
        }
        
        private boolean releasing = false;
        @Override
        public void release() {
            if (mPlayer != null && !releasing) {
                releasing = true;
                mPlayer.release();
                mPlayer = null;
            }
            mIsInitialized = false;
            releasing = false;
        }

        @Override
        public long seek(long position) {
            if (mPlayer != null) {
                mPlayer.seekTo((int) position);
            }
            return position;
        }

        @Override
        public void setAudioSessionId(int arg0) {            
        }


        @Override
        public void setVolume(float volume) {
            if (mPlayer != null) {
                mPlayer.setVolume(volume);
            }
        }

        @Override
        public void start() {
            if (mPlayer != null) {
                if (!isPlaying()) {
                    setState(State.STATE_CONNECTING);
                    mPlayer.play();
                    startProgressTimer();
                }
            }
        }

        @Override
        public void stop() {
            mIsInitialized = false;
            if (mPlayer != null) {
                mPlayer.stop();
            }
            mPlayingItem = null;
//            setState(State.STATE_STOPPED);
            stopProgressTimer();
        }

        @Override
        public void onStateChanged(com.tritondigital.player.MediaPlayer player, int state)
        {
            Log.d(TAG, "onStateChanged: " + state);
//            if (mIsPreparing && (state == TritonPlayer.STATE_PLAYING)) {
//                mIsPreparing = false;
//                if (mIsInitialized) {
//                    mService.get().onStartPlaying(mPlayingItem);
//                }
//            }
            if (state == TritonPlayer.STATE_ERROR)
            {
                int code = player.getErrorCode();
                if (code == 0) {
                    code = -1;
                }
                boolean needsStop = true;
                String msg = TritonPlayer.debugErrorToStr(code);
                if (code == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
//                    msg = "Media server died";
                    needsStop = true;
                }
                if (code == -1004) {
                    code = 404;
//                    msg = "File can't be accessed";
                    needsStop = true;
                    mPlayingItem = null;
                }
                mService.get().onError(code, msg);
                if (needsStop) {
                    mIsInitialized = false;
                    mPlayingItem = null;
                    getService().acquireWakeLock(30000);
                    mHandler.sendEmptyMessage(TRACK_ENDED);
                    mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
                }
                switch (code) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    
                    release();
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(SERVER_DIED), 2000);
                    break;
                default:
                    break;
                }
            } else if (state == TritonPlayer.STATE_PLAYING) {
                setState(State.STATE_PLAYING);
            } else if (state == TritonPlayer.STATE_PAUSED) {
                setState(State.STATE_PAUSED);
            } else if (state == TritonPlayer.STATE_STOPPED) {
                setState(State.STATE_STOPPED);
                release();
            }
            
        }

        @Override
        public void onCuePointReceived(com.tritondigital.player.MediaPlayer player, Bundle cuePoint)
        {
            if (mIsInitialized) {
                ((TritonAudioService)getService()).updateMetadata(cuePoint);
            }
        }
        
    }
    
//    private final class Player extends AudioStreamerService.MultiPlayer implements OnCuePointReceivedListener, OnStateChangedListener {
//        public Player(final TritonAudioService service) {
//           super(service);
//        }
//
//        @Override
//        public boolean openFile(final Object object, final boolean preparingNext) {
//            synchronized (this) {
//                if (!(object instanceof HashMap)) {
//                    return false;
//                }
//
//                try {
//                    HashMap<String, Object> props = (HashMap<String, Object>) object;
//                    final Service service = mService.get();
//                    
////                    HashMap<String, String> streamParams = new HashMap<String, String>();
////                    streamParams.put(StreamUrlBuilder.BANNERS, "300x50,320x50,320x480");
////
////                    Bundle settings = new Bundle();
////                    for (Map.Entry<String, Object> entry : props.entrySet()) {
////                        String key = entry.getKey();
////                        Object value = entry.getValue();
////                        if (key.equals("stream") && value instanceof HashMap) {
////                            for (Map.Entry<String, Object> entry2 : ((HashMap<String, Object>) value).entrySet()) {
////                                String key2 = entry.getKey();
////                                Object value2 = entry.getValue();
////                                streamParams.put(key2, TiConvert.toString(value2));
////                            }
////                        } else {
////                            if (value instanceof Boolean) {
////                                settings.putBoolean(key, TiConvert.toBoolean(value));
////                            } else if (value instanceof String) {
////                                settings.putString(key, TiConvert.toString(value));
////                            } else if (value instanceof Float) {
////                                settings.putFloat(key, TiConvert.toFloat(value));
////                            }
////                        }
////                    }
////                    settings.putSerializable(TritonPlayer.SETTINGS_STREAM_PARAMS,       streamParams);
//
//                    // TODO: We will remove this when the demo station will be moved to a production server
//                    
//                    String sbmId = SbmPlayer.generateSbmId();
//                    StreamUrlBuilder builder = new StreamUrlBuilder(service);
//                    builder.addQueryParameter("sbmid", sbmId);
//                    
//                    builder.setHost(TiConvert.toString(props, "host"));
//                    builder.enableLocationTracking(TiConvert.toBoolean(props, "locationTracking", false));
//                    if (props.containsKey(StreamUrlBuilder.BANNERS)) {
//                        builder.addQueryParameter(StreamUrlBuilder.BANNERS, TiConvert.toString(props, StreamUrlBuilder.BANNERS));
//                    }
//                    if (preparingNext) {
//                        mNextIsPreparing = false;
//                    } else {
//                        mIsPreparing = false;
//                    }
//                    
//                    String audioUrl = builder.build();
//                    String sbmUrl = TiConvert.toString(props, "sbmUrl");
//                    sbmUrl +=  "?sbmid=" + sbmId;
//                    
//                    if (preparingNext) {
//                        mNextIsPreparing = true;
//                        mNextPlayingItem = new PlayingItem(this, audioUrl, sbmUrl);
//                        setNextDataSource(mNextPlayingItem.path);
//                    } else {
//                        mIsPreparing = true;
//                        mPlayingItem = new PlayingItem(this, audioUrl, sbmUrl);
//                        
//                        setDataSource(mPlayingItem.path);
//                    }
//                } catch (Throwable t) {
//                    Log.w(TAG, "Issue while initializing : ", t);
//                    return false;
//                }
//                if (!preparingNext) {
//                    if (mNextIsPreparing) {
//                        setState(State.STATE_STARTING);
//                    } else if (mIsInitialized) {
//                        setState(State.STATE_INITIALIZED);
//                    }
//                    if (mIsInitialized && !mIsPreparing) {
//                        mService.get().onStartPlaying(mPlayingItem);
//                    }
//                }
//
//                return mIsInitialized;
//            }
//        }
//
//        /**
//         * Starts or resumes playback.
//         */
//        public void start() {
//            ((PlayingItem)mPlayingItem).getSbmPlayer().play();
//            super.start();
//        }
//
//        /**
//         * Resets the MediaPlayer to its uninitialized state.
//         */
//        public void stop() {
//            if (mPlayingItem != null) {
//                ((PlayingItem)mPlayingItem).release();
//                mPlayingItem = null;
//            }
//            super.stop();
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public void onCompletion(final MediaPlayer mp) {
//            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
//                mCurrentMediaPlayer.release();
//                mCurrentMediaPlayer = mNextMediaPlayer;
//                if (mPlayingItem != null) {
//                    ((PlayingItem) mPlayingItem).release();
//                    mPlayingItem = null;
//                }
//                mPlayingItem = mNextPlayingItem;
//                mIsPreparing = mNextIsPreparing;
//                mNextMediaPlayer = null;
//                mNextPlayingItem = null;
//                mNextIsPreparing = false;
//                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
//            } else {
//                super.onCompletion(mp);
//            }
//        }
//
//        @Override
//        public void onStateChanged(com.tritondigital.player.MediaPlayer player, int state)
//        {
//            Log.d(TAG, "onStateChanged: " + state);
//        }
//
//
//        @Override
//        public void onCuePointReceived(com.tritondigital.player.MediaPlayer player, Bundle cuePoint)
//        {
//            if (player == ((PlayingItem) mPlayingItem).getSbmPlayer()) { 
//                ((PlayingItem) mPlayingItem).syncSbmAndPlayer(cuePoint, mCurrentMediaPlayer);
//                ((TritonAudioService)getService()).updateMetadata(cuePoint);
//            } else if (mNextPlayingItem != null && player == ((PlayingItem) mNextPlayingItem).getSbmPlayer()) {
//                ((PlayingItem) mPlayingItem).syncSbmAndPlayer(cuePoint, mNextMediaPlayer);
//            }
//        }
//    }
    
    @Override
    protected void next() {
    }
    @Override
    protected void prev() {
    }
}
