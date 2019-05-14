package RAID;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ParityBackup
{
	static int padding = 0;

	public static void main(String[] args) throws IOException
	{
		readFile();
		readParts();
	}

	public static void readParts() throws IOException
	{
		byte[][] contents = new byte[11][];
		File file;
		int bad = 0;
		int good = 0;
		for (int i = 0; i < 11; i++)
		{
			file = new File("C:/users/moshe/desktop/tests/test_" + i);

			try
			{
				contents[i] = Files.readAllBytes(file.toPath());
				good = contents[i].length;
			}
			catch (IOException e)
			{
				bad = i;
			}
			System.out.println("Read part: " + i);
		}
		System.out.println("Missing part: " + bad);
		int ba = good;
		contents[bad] = new byte[ba];
		for (int i = 0; i < ba; i++)
		{
			byte b = contents[0][i];
			for (int j = 1; j < contents.length; j++)
				if (j == bad)
					continue;
				else
					b ^= contents[j][i];
			contents[bad][i] = b;
		}
		System.out.println("Finished recovery, writing file now");
		FileOutputStream fos = new FileOutputStream("c:/users/moshe/desktop/tests/test_new");
		byte[] total = new byte[ba * 10 - padding];
		for (int i = 0; i < total.length; i++)
			total[i] = contents[i / ba][i % ba];
		fos.write(total);
		System.out.println("Writing complete");
		fos.close();
	}

	public static void readFile() throws IOException
	{

		File file;
		file = new File("C:/users/moshe/desktop/tests/test");
		byte[] fileContent = Files.readAllBytes(file.toPath());
		int fileSize = fileContent.length;
		int splitSize = fileSize / 10;
		System.out.println(fileSize);
		if (fileSize % 10 != 0)
		{
			splitSize++;
			padding = 10 - fileSize % 10;
			System.out.println("Padding required " + padding);
		}
		byte[][] split = new byte[11][splitSize];
		System.out.println("Read file");

		for (int i = 0; i < 10; i++)
			for (int j = 0; j < split[i].length; j++)
				if (i * splitSize + j >= fileSize)
					break;
				else
					split[i][j] = fileContent[i * splitSize + j];

		for (int i = 0; i < splitSize; i++)
		{
			byte b = split[0][i];
			for (int j = 1; j < 10; j++)
				b ^= split[j][i];
			split[split.length - 1][i] = b;
		}
		System.out.println("Finished XOR");
		Random random = new Random();
		boolean dropped = false;
		for (int i = 0; i < 11; i++)
		{
			System.out.println("Writing part: " + i);
			if (!dropped && random.nextDouble() <= .15)
			{
				dropped = true;
				System.out.println("Failed to write part: " + i);
			}
			else
			{
				FileOutputStream fos = new FileOutputStream("c:/users/moshe/desktop/tests/test_" + i);
				fos.write(split[i]);
				fos.close();
			}
		}
	}
}
