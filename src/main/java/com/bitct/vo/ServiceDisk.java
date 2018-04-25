package com.bitct.vo;

public class ServiceDisk {
	private String fileSystem;
	private String capacity;
	private String used;
	private String usable;
	private String percentUsed;
	private String dir;
	
	
	public ServiceDisk() {
		super();
	}
	public ServiceDisk(String fileSystem, String capacity, String used,
			String usable, String percentUsed, String dir) {
		super();
		this.fileSystem = fileSystem;
		this.capacity = capacity;
		this.used = used;
		this.usable = usable;
		this.percentUsed = percentUsed;
		this.dir = dir;
	}
	public String getFileSystem() {
		return fileSystem;
	}
	public void setFileSystem(String fileSystem) {
		this.fileSystem = fileSystem;
	}
	public String getCapacity() {
		return capacity;
	}
	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	public String getUsable() {
		return usable;
	}
	public void setUsable(String usable) {
		this.usable = usable;
	}
	public String getPercentUsed() {
		return percentUsed;
	}
	public void setPercentUsed(String percentUsed) {
		this.percentUsed = percentUsed;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	@Override
	public String toString() {
		return "ServiceDisk [fileSystem=" + fileSystem + ", capacity="
				+ capacity + ", used=" + used + ", usable=" + usable
				+ ", percentUsed=" + percentUsed + ", dir=" + dir + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((capacity == null) ? 0 : capacity.hashCode());
		result = prime * result + ((dir == null) ? 0 : dir.hashCode());
		result = prime * result
				+ ((fileSystem == null) ? 0 : fileSystem.hashCode());
		result = prime * result
				+ ((percentUsed == null) ? 0 : percentUsed.hashCode());
		result = prime * result + ((usable == null) ? 0 : usable.hashCode());
		result = prime * result + ((used == null) ? 0 : used.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceDisk other = (ServiceDisk) obj;
		if (capacity == null) {
			if (other.capacity != null)
				return false;
		} else if (!capacity.equals(other.capacity))
			return false;
		if (dir == null) {
			if (other.dir != null)
				return false;
		} else if (!dir.equals(other.dir))
			return false;
		if (fileSystem == null) {
			if (other.fileSystem != null)
				return false;
		} else if (!fileSystem.equals(other.fileSystem))
			return false;
		if (percentUsed == null) {
			if (other.percentUsed != null)
				return false;
		} else if (!percentUsed.equals(other.percentUsed))
			return false;
		if (usable == null) {
			if (other.usable != null)
				return false;
		} else if (!usable.equals(other.usable))
			return false;
		if (used == null) {
			if (other.used != null)
				return false;
		} else if (!used.equals(other.used))
			return false;
		return true;
	}
	
	
}
