package RAID;

import java.io.Serializable;

public class MetaFile implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String fileName;
	private String dateAdded;
	private String addedBy;
	private int partNumber;
	private int partsAmount;
	private int padding;

	public MetaFile(String fileName, String dateAdded, String addedBy, int partNumber, int partsAmount, int padding)
	{
		this.addedBy = addedBy;
		this.dateAdded = dateAdded;
		this.fileName = fileName;
		this.partNumber = partNumber;
		this.partsAmount = partsAmount;
		this.padding = padding;
	}
	
	public MetaFile getNextMetaFile()
	{
		return new MetaFile(fileName, dateAdded, addedBy, ++partNumber, partsAmount, padding);
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

	public int getPadding()
	{
		return padding;
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
		else if (!fileName.equals(((MetaFile) obj).fileName))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return fileName + "\t\t\t" + dateAdded;
	}
}
