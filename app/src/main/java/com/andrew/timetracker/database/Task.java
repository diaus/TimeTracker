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

	@NotNull
	private String name;

	private Long parentId;

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

	public Long getParentId() {
		return this.parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	@Generated(hash = 576039145)
	public Task(Long id, @NotNull String name, Long parentId) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
	}

	@Generated(hash = 733837707)
	public Task() {
	}
}
