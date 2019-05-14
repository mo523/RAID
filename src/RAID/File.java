package RAID;

public class File
{
	private String fileName;
	private String dateAdded;
	private String addedBy;
	private int partNumber;
	private int partsAmount;
	private byte[] data;

	public File(String fileName, String dateAdded, String addedBy, int partNumber, int partsAmount)
	{
		this.addedBy = addedBy;
		this.dateAdded = dateAdded;
		this.fileName = fileName;
		this.partNumber = partNumber;
		this.partsAmount = partsAmount;
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

	public int getPartNumber()
	{
		return partNumber;
	}

	public int getPartsAmount()
	{
		return partsAmount;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}

	public byte[] getData()
	{
		return data;
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
