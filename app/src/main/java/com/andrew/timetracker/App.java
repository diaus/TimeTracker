package com.andrew.timetracker;

import android.app.Application;

import com.andrew.timetracker.database.DaoMaster;
import com.andrew.timetracker.database.DaoSession;

import org.greenrobot.greendao.database.Database;

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

		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "timetracker-db-encrypted" : "timetracker-db");
		Database db = ENCRYPTED ? helper.getEncryptedWritableDb("Ld3A3qNg2KJDrO6") : helper.getWritableDb();
		daoSession = new DaoMaster(db).newSession();
	}

	public DaoSession getDaoSession() {
		return daoSession;
	}

}
