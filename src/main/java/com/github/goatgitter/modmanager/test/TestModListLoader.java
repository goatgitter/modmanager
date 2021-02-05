package com.github.goatgitter.modmanager.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.goatgitter.modmanager.util.ModListLoader;

public class TestModListLoader {
	private ModListLoader _instance = null;
	private String testFileLine1 = "testFile.jar" + System.lineSeparator();
	private String testFileFullName = "C:\\dev\\TestSpecialCharö\\Desktop\\Games\\Minecraft\\mods\\modmanager\\newModList.txt";
	private Path testPath = null;
	private Path testDir = null;
	
	@BeforeEach
	void setUp() throws Exception {
		_instance = new ModListLoader();
		createTestFile();
	}

	@AfterEach
	void tearDown() throws Exception {
		_instance = null;
		deleteTestFile();
		testPath = null;
		testDir = null;
	}
	
	private void createTestFile()
	{
		testPath = Paths.get(testFileFullName);
		testDir = testPath.getParent();
		if (!Files.exists(testDir))
		{
			try {
				Files.createDirectories(testDir);
			} catch (IOException e) {
				e.printStackTrace();
				fail("Unable to create test directory");
			}
		}
		try {
			Files.createFile(testPath);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to create test file");
		}
		
		try {
			Files.write(testPath, testFileLine1.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to write to test file");
		}
	}
	
	private void deleteTestFile()
	{
		try {
			Files.deleteIfExists(testPath);
		} catch (IOException e) {			
			e.printStackTrace();
			fail("Unable to delete test file");
		}
		Path testDirParent = testDir.getParent();
		try {
			FileUtils.deleteDirectory(testDir.toFile());
			FileUtils.deleteDirectory(testDirParent.toFile());
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unable to delete test directories");
		}
	}

	@Test
	void testReadFile() {
		String result = _instance.readFile(testPath);
		assertNotNull(result, "Result should NOT be null");
		assertNotEquals(result, "", "Result should not be empty string");
		assertEquals(result,testFileLine1,"Result should equal testFileLine1");
	}

}
