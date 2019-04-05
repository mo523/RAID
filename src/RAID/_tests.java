package RAID;

import java.util.HashSet;

public class _tests
{
	public static void main(String[] args)
	{
		HashSet<File> files = new HashSet<>();
		files.add(new File("test1", "10/23/1910", "1"));
		files.add(new File("test1", "1", "1"));
		files.forEach(System.out::println);
	}
}
