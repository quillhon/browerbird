package courses.bowerbird.models;

import java.io.Serializable;

public class Item implements Serializable {
	
	private static final long serialVersionUID = 64L;
	
	private int id;
	private String name;
	private int quota;
	private boolean isFinsihed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuota() {
		return quota;
	}

	public void setQuota(int quota) {
		this.quota = quota;
	}

	public boolean isFinsihed() {
		return isFinsihed;
	}

	public void setFinsihed(boolean isFinsihed) {
		this.isFinsihed = isFinsihed;
	}
}
