package RAID;

public class File
{
	private String fileName;
	private String dateAdded;
	private String addedBy;
	
	public File(String fileName, String dateAdded, String addedBy)
	{
		this.addedBy = addedBy;
		this.dateAdded = dateAdded;
		this.fileName = fileName;
	}

	public String getDateAdded()
	{
		return dateAdded;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getAddedBy()
	{
		return addedBy;
	}

	@Override
	public int hashCode()
	{
		return fileName.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		else if (!fileName.equals(((File) obj).fileName))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return fileName + "\t\t\t" + dateAdded;
	}

}
