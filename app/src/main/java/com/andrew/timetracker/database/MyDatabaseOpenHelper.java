package com.andrew.timetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseOpenHelper;

/**
 * Created by andrew on 05.09.2016.
 */
public class MyDatabaseOpenHelper extends DaoMaster.OpenHelper {


	public MyDatabaseOpenHelper(Context context, String name) {
		super(context, name);
	}

	public MyDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
		super(context, name, factory);
	}

	@Override
	public void onUpgrade(Database db, int oldVersion, int newVersion) {
		if (newVersion == oldVersion) return;

		switch (oldVersion){
			case 4: upgradeFromVersion_4(db); break;
		}
	}

	private void upgradeFromVersion_4(Database db) {
		// remove 'unique' from field NAME
		db.execSQL("CREATE TABLE \"TASK_temp\" (" + //
				  "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
				  "\"NAME\" TEXT NOT NULL);"); // 1: name
		db.execSQL("INSERT INTO TASK_temp SELECT * FROM TASK");
		db.execSQL("DROP TABLE TASK");
		db.execSQL("ALTER TABLE TASK_temp RENAME TO TASK");

		// add parent_id column
		db.execSQL("ALTER TABLE TASK ADD COLUMN PARENT_ID INTEGER");
	}
}
