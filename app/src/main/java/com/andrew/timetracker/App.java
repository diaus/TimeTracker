package com.andrew.timetracker;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.andrew.timetracker.database.DaoMaster;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.MyDatabaseOpenHelper;
import com.andrew.timetracker.settings.Settings;

import org.greenrobot.greendao.database.Database;

import java.util.Locale;

/**
 * Created by andrew on 15.08.2016.
 */
public class App extends Application {
	/** A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher. */
	public static final boolean ENCRYPTED = true;

	private DaoSession daoSession;

	@Override
	public void onCreate() {
		super.onCreate();

		setLocale();

		MyDatabaseOpenHelper helper = new MyDatabaseOpenHelper(this, ENCRYPTED ? "timetracker-db-encrypted" : "timetracker-db");
		Database db = ENCRYPTED ? helper.getEncryptedWritableDb("Ld3A3qNg2KJDrO6") : helper.getWritableDb();
		daoSession = new DaoMaster(db).newSession();

		// init singltons
		Settings.init(this);
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

	public DaoSession getDaoSession() {
		return daoSession;
	}

}
