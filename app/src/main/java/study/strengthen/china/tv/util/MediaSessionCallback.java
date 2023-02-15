package study.strengthen.china.tv.util;

import android.support.v4.media.session.MediaSessionCompat;
import xyz.doikki.videoplayer.player.VideoView;
public class MediaSessionCallback extends MediaSessionCompat.Callback {

	private static final int PLAYLIST_SIZE = 2;

	private VideoView movieView;
	private int indexInPlaylist;

	public MediaSessionCallback(VideoView movieView) {
		this.movieView = movieView;
		indexInPlaylist = 1;
	}

	@Override
	public void onPlay() {
		super.onPlay();
		movieView.resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		movieView.pause();
	}

	@Override
	public void onSkipToNext() {
		super.onSkipToNext();
//		movieView.startVideo();
//		if (indexInPlaylist < PLAYLIST_SIZE) {
//			indexInPlaylist++;
//			if (indexInPlaylist >= PLAYLIST_SIZE) {
//				updatePlaybackState(
//						PlaybackStateCompat.STATE_PLAYING,
//						MEDIA_ACTIONS_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS,
//						movieView.getCurrentPosition(),
//						movieView.getVideoResourceId());
//			} else {
//				updatePlaybackState(
//						PlaybackStateCompat.STATE_PLAYING,
//						MEDIA_ACTIONS_ALL,
//						movieView.getCurrentPosition(),
//						movieView.getVideoResourceId());
//			}
//		}
	}

	@Override
	public void onSkipToPrevious() {
		super.onSkipToPrevious();
//		movieView.startVideo();
//		if (indexInPlaylist > 0) {
//			indexInPlaylist--;
//			if (indexInPlaylist <= 0) {
//				updatePlaybackState(
//						PlaybackStateCompat.STATE_PLAYING,
//						MEDIA_ACTIONS_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT,
//						movieView.getCurrentPosition(),
//						movieView.getVideoResourceId());
//			} else {
//				updatePlaybackState(
//						PlaybackStateCompat.STATE_PLAYING,
//						MEDIA_ACTIONS_ALL,
//						movieView.getCurrentPosition(),
//						movieView.getVideoResourceId());
//			}
//		}
	}
}