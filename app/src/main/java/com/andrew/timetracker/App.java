package com.andrew.timetracker;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.andrew.timetracker.database.DaoMaster;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.MyDatabaseOpenHelper;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.events.DbChangesEvent;
import com.andrew.timetracker.settings.Settings;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.greendao.database.Database;

import java.util.Date;
import java.util.Locale;

/**
 * Created by andrew on 15.08.2016.
 */
public class App extends Application {

	private DaoSession daoSession;
	private TimelineDao mTimelineDao;
	private TaskDao mTaskDao;

	@Override
	public void onCreate() {
		super.onCreate();

		setLocale();

		// DATABASE
		MyDatabaseOpenHelper helper = new MyDatabaseOpenHelper(this, "timetracker-db-encrypted");
		Database db = helper.getEncryptedWritableDb("Ld3A3qNg2KJDrO6");
		daoSession = new DaoMaster(db).newSession();
		mTimelineDao = daoSession.getTimelineDao();
		mTaskDao = daoSession.getTaskDao();

		// init singltons
		Settings.init(this);

		EventBus.getDefault().register(this);
	}

	@Subscribe
	public void handleDbChangeEvent(DbChangesEvent event) {
		if (event.sender == this) return;
		updateNotification();
	}

	private void setLocale() {
		Locale defaultLocale = Locale.getDefault();
		String lang = defaultLocale.getLanguage();
		if (!lang.equals("ru")) return;

		Locale locale = new Locale("uk");
		Locale.setDefault(locale);

		Resources resources = getResources();

		Configuration configuration = resources.getConfiguration();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			configuration.setLocale(locale);
		} else {
			configuration.locale = locale;
		}

		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
	}

	public void updateNotification() {

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

		Timeline timeline = mTimelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).limit(1).unique();
		boolean isStarted = timeline != null;
		if (!isStarted){
			timeline = mTimelineDao.queryBuilder().orderDesc(TimelineDao.Properties.StopTime).limit(1).unique();
			if (timeline == null) {
				// no timeline records at all
				notificationManager.cancel(0);
				return;
			}
		}

		Task task = mTaskDao.load(timeline.getTaskId());

//		Intent intent = new Intent(this, NotificationReceiver.class);
//		PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);

		Intent intent = MainActivity.getNotificationIntent(this);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

		Notification notification = new NotificationCompat.Builder(this)
				  //.setTicker("test me")
				  .setSmallIcon(getNotificationIcon(isStarted))
				  .setContentTitle(task.getName())
				  .setContentText(getString(isStarted ? R.string.notification_subtitle_started : R.string.notification_subtitle_stopped))
				  .setContentIntent(pi)
				  .setAutoCancel(false)
				  .setDefaults(Notification.FLAG_NO_CLEAR)
				  //.setOngoing(true)
				  //.setColor(ContextCompat.getColor(this, isStarted ? R.color.started_task : R.color.stopped_task))
				  .build();

		notificationManager.notify(0, notification);

	}

	private int getNotificationIcon(boolean isStarted) {
		// TODO: Lollipop
//		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
//			return R.drawable.started_circle;
//		} else {
//		}
		return isStarted ? R.drawable.started_circle : R.drawable.stopped_circle;
	}

	public DaoSession getDaoSession() {
		return daoSession;
	}

}
