package com.ruptech.chinatalk.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.Utils;

import java.io.File;

import static butterknife.ButterKnife.findById;

public class RecordButton extends Button {
	private class ObtainDecibelThread extends Thread {

		private volatile boolean running = true;

		public void exit() {
			running = false;
		}

		@Override
		public void run() {
			while (running) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					if (BuildConfig.DEBUG)
						Log.e(TAG, e.getMessage(), e);
				}
				if (recorder == null || !running) {
					break;
				}
				int x = recorder.getMaxAmplitude();
				if (x != 0) {
					int f = (int) (10 * Math.log(x) / Math.log(10));
					if (f < 26)
						volumeHandler.sendEmptyMessage(0);
					else if (f < 32)
						volumeHandler.sendEmptyMessage(1);
					else if (f < 38)
						volumeHandler.sendEmptyMessage(2);
					else
						volumeHandler.sendEmptyMessage(3);

					if (BuildConfig.DEBUG)
						Log.i("volume", "f: " + f);
				}

			}
		}

	}

	public interface OnFinishedRecordListener {
		public void onFinishedRecord(File mFileName);
	}

	static class ShowVolumeHandler extends Handler {
		private final RecordButton recordButton;

		public ShowVolumeHandler(RecordButton recordButton) {
			this.recordButton = recordButton;
		}

		@Override
		public void handleMessage(Message msg) {
			long intervalTime = System.currentTimeMillis() - startTime;
			if (intervalTime > 7000) {
				volumnView.setImageResource(red_res[msg.what]);
			} else if (intervalTime > 5000) {
				volumnView.setImageResource(yellow_res[msg.what]);
			} else {
				volumnView.setImageResource(green_res[msg.what]);
			}

			timeView.setText("" + intervalTime / 1000);

			// 超过7秒，停止录音
			if (intervalTime > 7900) {
				recordButton.finishRecord(false);
			}

		}
	}

	protected final String TAG = Utils.CATEGORY
			+ RecordButton.class.getSimpleName();

	private File mFile = null;

	private OnFinishedRecordListener finishedListener;

	private static final int MIN_INTERVAL_TIME = 2000;// 2s

	private static long startTime;
	private Dialog recordIndicator;

	private static int[] green_res = { R.drawable.mic_green_2,
			R.drawable.mic_green_3, R.drawable.mic_green_4,
			R.drawable.mic_green_5 };

	private static int[] yellow_res = { R.drawable.mic_yellow_2,
			R.drawable.mic_yellow_3, R.drawable.mic_yellow_4,
			R.drawable.mic_yellow_5 };
	private static int[] red_res = { R.drawable.mic_red_2,
			R.drawable.mic_red_3, R.drawable.mic_red_4, R.drawable.mic_red_5 };
	private static ImageView volumnView;

	private MediaRecorder recorder;

	private ObtainDecibelThread thread;

	private Handler volumeHandler;

	private View recordingView;

	private static TextView timeView;

	private final OnDismissListener onDismiss = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			stopRecording();
		}
	};

	public RecordButton(Context context) {
		super(context);
		init();
	}

	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void cancelRecord() {
		stopRecording();
		recordIndicator.dismiss();

		Toast.makeText(getContext(), R.string.cancel_voice, Toast.LENGTH_SHORT)
				.show();
		mFile.delete();
	}

	private void finishRecord(boolean bubbleEvent) {
		stopRecording();
		recordIndicator.dismiss();

		long intervalTime = System.currentTimeMillis() - startTime;
		if (intervalTime < MIN_INTERVAL_TIME) {
			this.setEnabled(true);
			Toast.makeText(getContext(), R.string.voice_time_too_short,
					Toast.LENGTH_SHORT).show();
			mFile.delete();
			return;
		}

		if (bubbleEvent && finishedListener != null)
			finishedListener.onFinishedRecord(mFile);
	}

	private void init() {
		volumeHandler = new ShowVolumeHandler(this);
	}

	private void initDialogAndStartRecord() throws Exception {
		this.setEnabled(false);
		startTime = System.currentTimeMillis();
		recordIndicator = new Dialog(getContext(),
				R.style.like_toast_dialog_style);

		recordingView = View.inflate(getContext(),
				R.layout.view_voice_recording, null);

		timeView = (TextView) findById(recordingView, R.id.time);
		timeView.setText("0");

		volumnView = (ImageView) findById(recordingView, R.id.volumn);
		volumnView.setImageResource(R.drawable.mic_green_2);
		recordIndicator.setContentView(recordingView, new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		recordIndicator.setOnDismissListener(onDismiss);
		LayoutParams lp = recordIndicator.getWindow().getAttributes();
		lp.gravity = Gravity.CENTER;

		startRecording();
		recordIndicator.show();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			try {
				initDialogAndStartRecord();
			} catch (Exception e) {
				Toast.makeText(getContext(),
						R.string.record_button_cannot_initialize_mediarecorder,
						Toast.LENGTH_SHORT).show();
				Utils.sendClientException(e);
			}
			break;
		case MotionEvent.ACTION_UP:
			finishRecord(true);
			break;
		case MotionEvent.ACTION_CANCEL:// 当手指移动到view外面，会cancel
			cancelRecord();
			break;
		}

		return super.onTouchEvent(event);
	}

	public void setOnFinishedRecordListener(OnFinishedRecordListener listener) {
		finishedListener = listener;
	}

	private void startRecording() throws Exception {
		mFile = new File(Utils.getVoiceFolder(getContext()),
				System.currentTimeMillis() + ".amr");
		try {
			recorder = new MediaRecorder();
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			recorder.setOutputFile(mFile.getAbsolutePath());

			recorder.prepare();

			recorder.start();
			thread = new ObtainDecibelThread();
			thread.start();
		} catch (Exception e) {
			Toast.makeText(getContext(),
					R.string.record_button_cannot_initialize_mediarecorder,
					Toast.LENGTH_SHORT).show();
			Utils.sendClientException(e);
		}
	}

	private void stopRecording() {
		if (thread != null) {
			thread.exit();
			thread = null;
		}
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
	}

}
