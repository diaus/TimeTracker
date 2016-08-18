package com.andrew.timetracker.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by andrew on 17.08.2016.
 */
@Entity
public class Timeline {

	@Id(autoincrement = true)
	private Long id;

	@NotNull
	@Index
	private long taskId;

	@ToOne(joinProperty = "taskId")
	private Task task;

	@NotNull
	@Index
	private Date startTime;

	@Index
	private Date stopTime;

	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 1942392019)
	public void refresh() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.refresh(this);
	}

	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 713229351)
	public void update() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.update(this);
	}

	/**
	 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
	 * Entity must attached to an entity context.
	 */
	@Generated(hash = 128553479)
	public void delete() {
		if (myDao == null) {
			throw new DaoException("Entity is detached from DAO context");
		}
		myDao.delete(this);
	}

	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 254774866)
	public void setTask(@NotNull Task task) {
		if (task == null) {
			throw new DaoException(
					"To-one property 'taskId' has not-null constraint; cannot set to-one to null");
		}
		synchronized (this) {
			this.task = task;
			taskId = task.getId();
			task__resolvedKey = taskId;
		}
	}

	/** To-one relationship, resolved on first access. */
	@Generated(hash = 1810838705)
	public Task getTask() {
		long __key = this.taskId;
		if (task__resolvedKey == null || !task__resolvedKey.equals(__key)) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			TaskDao targetDao = daoSession.getTaskDao();
			Task taskNew = targetDao.load(__key);
			synchronized (this) {
				task = taskNew;
				task__resolvedKey = __key;
			}
		}
		return task;
	}

	@Generated(hash = 100676365)
	private transient Long task__resolvedKey;

	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 126532443)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getTimelineDao() : null;
	}

	/** Used for active entity operations. */
	@Generated(hash = 157161418)
	private transient TimelineDao myDao;

	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;

	public Date getStopTime() {
		return this.stopTime;
	}

	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Long getTaskId() {
		return this.taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Generated(hash = 2030884093)
	public Timeline(Long id, long taskId, @NotNull Date startTime, Date stopTime) {
		this.id = id;
		this.taskId = taskId;
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

	@Generated(hash = 1903543528)
	public Timeline() {
	}

	public int getSpentSeconds() {
		return (int) (((getStopTime() != null ? getStopTime().getTime() : new Date().getTime()) - getStartTime().getTime()) / 1000);
	}
}
