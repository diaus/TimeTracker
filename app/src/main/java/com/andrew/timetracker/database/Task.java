package com.andrew.timetracker.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by andrew on 15.08.2016.
 */

@Entity
public class Task {

	@Id(autoincrement = true)
	private Long id;

	@Unique
	@NotNull
	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Generated(hash = 207136854)
	public Task(Long id, @NotNull String name) {
		this.id = id;
		this.name = name;
	}

	@Generated(hash = 733837707)
	public Task() {
	}
}
